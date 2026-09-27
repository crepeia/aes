/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.DailyLog;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class DailyLogDAO extends GenericDAO<DailyLog>{
    
    public DailyLogDAO() throws NamingException {
        super(DailyLog.class);
    }

    public static class IdempotencyConflict extends SQLException {
        public IdempotencyConflict() { super("IDEMPOTENCY_CONFLICT"); }
    }

    /**
     * Keyed mobile writes only. Do not call GenericDAO's transaction-opening
     * methods here: log, challenges and receipt must commit or roll back together.
     */
    public String editOrCreateOnce(DailyLog input, long ownerId, String key,
            boolean challenges, LocalDate savedOn, EntityManager em,
            javax.transaction.UserTransaction transaction) throws SQLException {
        try {
            transaction.begin();
            em.joinTransaction();
            // Serialize this user's dose writes across threads/server instances.
            aes.model.User user = em.find(aes.model.User.class, ownerId, javax.persistence.LockModeType.PESSIMISTIC_WRITE);
            em.refresh(user, javax.persistence.LockModeType.PESSIMISTIC_WRITE);
            String receiptKey = ownerId + ":" + key;
            String hash = doseHash(input, challenges, savedOn);
            aes.model.DailyLogOperation previous = em.find(aes.model.DailyLogOperation.class, receiptKey);
            if (previous != null) {
                if (!hash.equals(previous.getRequestHash())) throw new IdempotencyConflict();
                String response = previous.getResponseJson();
                transaction.commit();
                return response;
            }
            aes.model.Record record = em.find(aes.model.Record.class, input.getRecord().getId());
            if (record == null || record.getUser().getId() != ownerId) throw new SQLException("INVALID_DOSE_OWNER");

            List<DailyLog> matches = em.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate", DailyLog.class)
                    .setParameter("recordId", record.getId()).setParameter("logDate", input.getLogDate()).getResultList();
            if (matches.size() > 1) throw new SQLException("DUPLICATE_LEGACY_DAILY_LOG");
            String action = matches.isEmpty() ? "create" : "edit";
            DailyLog log = matches.isEmpty() ? new DailyLog() : matches.get(0);
            log.setRecord(record);
            log.setLogDate(input.getLogDate());
            log.setDrinks(input.getDrinks());
            log.setContext(input.getContext());
            log.setConsequences(input.getConsequences());
            if (matches.isEmpty()) em.persist(log);
            org.primefaces.json.JSONObject effects = new org.primefaces.json.JSONObject();
            if (challenges) effects = applyDoseChallenges(em, user, log, savedOn);
            em.flush();
            String response = new org.primefaces.json.JSONObject()
                    .put("id", log.getId()).put("action", action).put("idempotencyKey", key)
                    .put("challengeChanges", effects).toString();
            aes.model.DailyLogOperation receipt = new aes.model.DailyLogOperation();
            receipt.setKey(receiptKey);
            receipt.setRequestHash(hash);
            receipt.setResponseJson(response);
            em.persist(receipt);
            em.flush();
            transaction.commit();
            return response;
        } catch (Exception error) {
            try {
                if (transaction.getStatus() != javax.transaction.Status.STATUS_NO_TRANSACTION) transaction.rollback();
            } catch (Exception rollbackError) { error.addSuppressed(rollbackError); }
            if (error instanceof IdempotencyConflict) throw (IdempotencyConflict) error;
            throw new SQLException("DOSE_TRANSACTION_FAILED", error);
        }
    }

    static String doseHash(DailyLog log, boolean challenges, LocalDate savedOn) throws Exception {
        // A fixed-order array distinguishes null/empty and covers all applied fields.
        String canonical = new org.primefaces.json.JSONArray()
                .put(log.getRecord().getId()).put(log.getLogDate().toString()).put(log.getDrinks())
                .put(log.getContext() == null ? org.primefaces.json.JSONObject.NULL : log.getContext())
                .put(log.getConsequences() == null ? org.primefaces.json.JSONObject.NULL : log.getConsequences())
                .put(challenges).put(savedOn.toString()).toString();
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte value : digest) hash.append(String.format("%02x", value & 0xff));
        return hash.toString();
    }

    private org.primefaces.json.JSONObject applyDoseChallenges(EntityManager em, aes.model.User user,
            DailyLog log, LocalDate savedOn) throws Exception {
        List<aes.model.ChallengeUser> existing = em.createQuery(
                "SELECT ch FROM ChallengeUser ch WHERE ch.user.id=:userId AND ch.challengeId IN (5,6)",
                aes.model.ChallengeUser.class).setParameter("userId", user.getId()).getResultList();
        org.primefaces.json.JSONArray upsert = new org.primefaces.json.JSONArray();
        org.primefaces.json.JSONArray removed = new org.primefaces.json.JSONArray();
        // Preserve the mobile rules in constants/Challenges.js: 5 + streak * 1/2.
        // Completion day is the day of the user's save, not the later retry day.
        completeDoseChallenge(em, user, existing, 5L, savedOn, savedOn, 1, upsert);
        if (log.getDrinks() == 0) {
            completeDoseChallenge(em, user, existing, 6L, savedOn, log.getLogDate(), 2, upsert);
        } else {
            for (aes.model.ChallengeUser challenge : existing) {
                if (Long.valueOf(6).equals(challenge.getChallengeId()) && log.getLogDate().equals(challenge.getDateCompleted())) {
                    removed.put(challenge.getId());
                    em.remove(challenge);
                }
            }
        }
        return new org.primefaces.json.JSONObject().put("upsert", upsert).put("removed", removed);
    }

    private void completeDoseChallenge(EntityManager em, aes.model.User user,
            List<aes.model.ChallengeUser> existing, long challengeId, LocalDate completionDay,
            LocalDate streakDay, int modifier, org.primefaces.json.JSONArray result) throws Exception {
        boolean completed = existing.stream().anyMatch(ch -> Long.valueOf(challengeId).equals(ch.getChallengeId())
                && completionDay.equals(ch.getDateCompleted()));
        if (completed) return;
        java.util.Set<LocalDate> days = new java.util.HashSet<>();
        for (aes.model.ChallengeUser ch : existing) {
            if (Long.valueOf(challengeId).equals(ch.getChallengeId())) days.add(ch.getDateCompleted());
        }
        LocalDate missingDay = streakDay.minusDays(1);
        while (days.contains(missingDay)) missingDay = missingDay.minusDays(1);
        long streak = java.time.temporal.ChronoUnit.DAYS.between(missingDay, streakDay);
        aes.model.ChallengeUser challenge = new aes.model.ChallengeUser();
        challenge.setUser(user);
        challenge.setChallengeId(challengeId);
        challenge.setDateCreated(completionDay);
        challenge.setDateCompleted(completionDay);
        challenge.setScore(5L + streak * modifier);
        em.persist(challenge);
        em.flush();
        result.put(new org.primefaces.json.JSONObject().put("id", challenge.getId())
                .put("challengeId", challengeId).put("userId", user.getId()).put("score", challenge.getScore())
                .put("dateCreated", completionDay.toString()).put("dateCompleted", completionDay.toString()));
    }

    
    public DailyLog find(long recordId, LocalDate logDate, EntityManager entityManager)throws NoResultException{
            
        DailyLog dl = (DailyLog) entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
            .setParameter("recordId", recordId)
            .setParameter("logDate", logDate)
            .getSingleResult();
        return dl;
            
    }
    
    public boolean edit(DailyLog entity, EntityManager entityManager){
        
        try {
            DailyLog dl = find(entity.getRecord().getId(), entity.getLogDate(), entityManager);
            dl.setDrinks(entity.getDrinks());
            dl.setContext(entity.getContext());
            dl.setConsequences(entity.getConsequences());
            
            super.update(dl, entityManager);
            //action = "edit";
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DailyLogDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }catch (NoResultException ex){
             return false;
        }
    
    }
    

    public DailyLog editOrCreate(DailyLog entity, EntityManager entityManager) throws SQLException {
        //String action = "";
        try {
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            //Date t = format.parse(entity.getLogDate().toString());
            
            DailyLog dl = (DailyLog) entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId AND dl.logDate=:logDate")
                .setParameter("recordId", entity.getRecord().getId())
                .setParameter("logDate", entity.getLogDate())
                .getSingleResult();
                dl.setDrinks(entity.getDrinks());
                dl.setContext(entity.getContext());
                dl.setConsequences(entity.getConsequences());
                super.insertOrUpdate(dl, entityManager);
                //action = "edit";
                return dl;
            
        } catch( NoResultException e ) {
             super.insertOrUpdate(entity, entityManager);
             return entity;
            //action = "create";
            //return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).build();
           
        } //catch (Exception e) {
           // Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, e);
        //    return null;
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
       // }
        
//        try {
//            return Response.status(Response.Status.OK).entity(new JSONObject().put("action", action).toString()).type(MediaType.APPLICATION_JSON).build();
//        } catch (JSONException ex) {
//            Logger.getLogger(DailyLogFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
//        }
        
 
    }
     

    public void remove(Long id, EntityManager entityManager) throws SQLException {
        super.delete(super.find(id, entityManager), entityManager);
        //super.remove(super.find(id));
    }


    public List<DailyLog> find(Long recordId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:recordId")
                .setParameter("recordId", recordId)
                .getResultList();
        
    }
    
}
