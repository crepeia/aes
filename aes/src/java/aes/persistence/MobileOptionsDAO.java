/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.MobileOptions;
import aes.model.User;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class MobileOptionsDAO extends GenericDAO<MobileOptions> {

    public MobileOptionsDAO() throws NamingException {
        super(MobileOptions.class);
    }

    public MobileOptions create(MobileOptions entity, EntityManager entityManager) {
        try {
            super.insertOrUpdate(entity, entityManager);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    public void edit(Long userId, MobileOptions entity, EntityManager entityManager) throws SQLException{

        //String userEmail = securityContext.getUserPrincipal().getName();
        User u = entityManager.find(User.class, userId);

        entity.setUser(u);
        entity.setDrinkNotificationTime(entity.getDrinkNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));
        entity.setTipNotificationTime(entity.getTipNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));

        super.insertOrUpdate(entity, entityManager);

    }

    public MobileOptions find(Long userId, EntityManager entityManager) throws SQLException {
        try {
            MobileOptions op = (MobileOptions) entityManager.createQuery("SELECT mo FROM MobileOptions mo WHERE mo.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return op;
        } catch (NoResultException e) {
            MobileOptions entity = new MobileOptions();
            entity.setUser(entityManager.find(User.class, userId));

            entity.setAllowTipNotifications(false);
            entity.setTipNotificationTime(OffsetTime.of(12, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setAllowDrinkNotifications(false);
            entity.setDrinkNotificationTime(OffsetTime.of(19, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setNotificationToken("");

            super.insertOrUpdate(entity, entityManager);
            return entity;

        }
    }

}
