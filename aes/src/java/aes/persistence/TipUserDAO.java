/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Tip;
import aes.model.TipUser;
import aes.model.TipUserKey;
import aes.model.User;
import aes.utility.AppServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class TipUserDAO extends GenericDAO<TipUser> {
    
    
    public TipUserDAO() throws NamingException {
        super(TipUser.class);
    }
    
 
    public TipUser getLatestTip(User user, EntityManager entityManager) throws SQLException {
        List<TipUser> tipUserList = this.list("user", user,entityManager);
        if(tipUserList.isEmpty()){
            sendNewTip(user, entityManager);
            tipUserList = this.list("user", user, entityManager);
        }
        return (tipUserList.get(tipUserList.size()-1));
    }
    
    public void sendNewTip(User user, EntityManager entityManager){
        TipUser tipUser = new TipUser();
        try {
            List<Long> tipsIds = new ArrayList<Long>();
            
            Properties properties = new Properties();
            String propertiesPath = AppServletContextListener.getServletContext().getInitParameter("messagesPath") + "_pt.properties";
            
            InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesPath);
            properties.load(input);
            
            boolean continueSearch = true;
            int counter = 1;
            
            while(continueSearch) {
                String key = properties.getProperty("tip.description." + counter, "");
                if (key.isEmpty()) {
                    continueSearch = false;
                } else {
                    tipsIds.add(Long.parseLong(key));
                    counter++;
                }
            }
            
            List<Long> tipUserIds;
            tipUserIds = entityManager.createQuery("SELECT tu.tipId FROM TipUser tu WHERE tu.user.id=:userId")
                    .setParameter("userId", user.getId())
                    .getResultList();
            
            List<Long> possibleTipsList = new ArrayList<Long>();
            
            for(Long tip: tipUserIds) {
                if (!tipsIds.contains(tip)) {
                    possibleTipsList.add(tip);
                }
            }

            if (possibleTipsList.isEmpty()) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.WARNING, "Não há dicas a serem enviadas para o usuário " + user.getEmail() + ".");
            } else {
                Random rand = new Random();
                Long tipId = possibleTipsList.get(rand.nextInt(possibleTipsList.size()));
                Calendar cal = Calendar.getInstance();
                
                TipUserKey tipUserKey = new TipUserKey(tipId, user.getId());

                tipUser.setId(tipUserKey);
                tipUser.setUser(entityManager.find(User.class, user.getId()));
                tipUser.setTipId(tipId);
                
                tipUser.setDateCreated(cal.getTime());
                
                update(tipUser, entityManager);               
            }
            
        } catch (SQLException | IOException ex) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public void createTip(TipUser entity, EntityManager em) throws SQLException {       
        super.insertOrUpdate(entity, em);
    }


    public void like(TipUser entity, EntityManager entityManager) throws SQLException {
        entity.setLiked(true);
        super.insertOrUpdate(entity, entityManager);
    }
    

    public void dislike(TipUser entity, EntityManager entityManager) throws SQLException {
        entity.setLiked(false);
        super.insertOrUpdate(entity, entityManager);
    }
    
    public void read(TipUser entity, EntityManager entityManager) throws SQLException {
        entity.setReadByUser(true);
        super.update(entity, entityManager);
    }
    
    public TipUser findByUserAndTip(Long userId, Long tipId, EntityManager entityManager) {
        List<TipUser> result = entityManager.createQuery("SELECT tu FROM TipUser tu WHERE tu.user.id =: userId AND tu.id.tipId =: tipId")
            .setParameter("userId", userId)
            .setParameter("tipId", tipId)
            .setMaxResults(1)
            .getResultList();
            
        return result.isEmpty() ? null : result.get(0);
    }
  
    public List<TipUser> findByUser(Long userId, EntityManager entityManager) {
        List<TipUser> list = (List<TipUser>) entityManager.createQuery("SELECT tu FROM TipUser tu WHERE tu.user.id =: userId")
            .setParameter("userId", userId)
            .getResultList();
        
        return list;  
    }
    
    public List<TipUser> findByDate(String sd, String ed, String userEmail, EntityManager entityManager) throws ParseException {
  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(sd);   
        Date endDate = sdf.parse(ed);

        //String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        
        List<TipUser> list = (List<TipUser>)  entityManager.createQuery("SELECT tu FROM TipUser tu WHERE tu.user.email=:email AND (tu.dateCreated BETWEEN :start AND :end)")
                .setParameter("email", userEmail)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        
        return list;
    }

   /* public String countREST() {
         javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<TipUser> rt = cq.from(TipUser.class);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return q.getSingleResult().toString();
        //return ((Long) q.getSingleResult()).intValue();
       // return String.valueOf(super.count());
    }*/
}
