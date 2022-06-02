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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    
    
    public TipUserDAO(EntityManager entityManager) throws NamingException {
        super(TipUser.class);
        this.setEntityManager(entityManager);
    }
    
 
    public TipUser getLatestTip(User user) throws SQLException {
        TipUser tipUser = new TipUser();
        tipUser.setTip(new Tip());
            
        List<TipUser> tipUserList = this.list("user", user);
        if(tipUserList.isEmpty()){
            sendNewTip(user);
            tipUserList = this.list("user", user);
        }
        return (tipUserList.get(tipUserList.size()-1));
 

    }
    
    
    public void sendNewTip(User user){
        TipUser tipUser = new TipUser();
        tipUser.setTip(new Tip());
        try {
            
            List<Tip> possibleTipsList;
            possibleTipsList = getEntityManager().createQuery("SELECT t FROM Tip t WHERE t.id NOT IN (SELECT tu.tip.id FROM TipUser tu WHERE tu.user.id=:userId)")
                    .setParameter("userId", user.getId())
                    .getResultList();


            if (possibleTipsList.isEmpty()) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.WARNING, "Não há dicas a serem enviadas para o usuário " + user.getEmail() + ".");
            } else {
                Random rand = new Random();
                Tip newtip = possibleTipsList.get(rand.nextInt(possibleTipsList.size()));
                Calendar cal = Calendar.getInstance();
                
                TipUserKey tipUserKey = new TipUserKey(newtip.getId(), user.getId());

                tipUser.setId(tipUserKey);
                tipUser.setUser(getEntityManager().find(User.class, user.getId()));
                tipUser.setTip(getEntityManager().find(Tip.class, newtip.getId()));
                
                tipUser.setDateCreated(cal.getTime());
                
                update(tipUser);               
            }
            
        } catch (SQLException ex) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public TipUser createTip(TipUser entity) throws SQLException {
            entity.setUser(getEntityManager().find(User.class, entity.getId().getUserId()));
            entity.setTip(getEntityManager().find(Tip.class, entity.getId().getTipId()));
            
            if(entity.getDateCreated()== null){
                entity.setDateCreated(new Date());
            }
            
            super.update(entity);
            //return Response.status(Response.Status.OK).build();
            return entity;

    }


    public TipUser like(TipUser entity) throws SQLException {
        TipUser newEntity = super.find(entity.getId());
 
        
        newEntity.setLiked(entity.isLiked());

        super.update(newEntity);
        return newEntity;
    }
    

    public TipUser dislike(TipUser entity) throws SQLException {
        TipUser newEntity = super.find(entity.getId());
        newEntity.setLiked(entity.isLiked());
        /*
        if(newEntity.isLiked() != null && newEntity.isLiked() == false){
            newEntity.setLiked(null);
        } else {
            newEntity.setLiked(false);
        }
        */
        super.update(newEntity);
        return newEntity;
    }
    

    public TipUser unlike(TipUser entity) throws SQLException {
        TipUser newEntity = super.find(entity.getId());
        newEntity.setLiked(null);
        super.update(newEntity);
        return newEntity;
    }
    

    public TipUser read(TipUser entity) throws SQLException {
        TipUser newEntity = super.find(entity.getId());
               
        if(newEntity==null){
            newEntity = createTip(entity);     
        }
        
        if(!newEntity.isReadByUser()){
            newEntity.setReadByUser(true);
            super.update(newEntity);
        }
        

        return newEntity;
    }
    
  
    public List<TipUser> findByUser(String uId) {
   
        List<TipUser> list = (List<TipUser>) getEntityManager().createQuery("SELECT tu FROM TipUser tu WHERE tu.user.id=:userId")
                .setParameter("userId", Long.parseLong(uId))
                .getResultList();
        return list;
                 
    }


    public List<TipUser> findByDate(String sd, String ed, String userEmail) throws ParseException {
  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(sd);   
        Date endDate = sdf.parse(ed);

        //String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        
        List<TipUser> list = (List<TipUser>)  getEntityManager().createQuery("SELECT tu FROM TipUser tu WHERE tu.user.email=:email AND (tu.dateCreated BETWEEN :start AND :end)")
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
