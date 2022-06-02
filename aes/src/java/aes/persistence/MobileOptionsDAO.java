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

    public MobileOptionsDAO(EntityManager entityManager) throws NamingException {
        super(MobileOptions.class);
        this.setEntityManager(entityManager);

    }

    public MobileOptions create(MobileOptions entity) {
        try {
            super.insertOrUpdate(entity);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    public void edit(Long userId, MobileOptions entity) throws SQLException{

        //String userEmail = securityContext.getUserPrincipal().getName();
        User u = getEntityManager().find(User.class, userId);

        entity.setUser(u);
        entity.setDrinkNotificationTime(entity.getDrinkNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));
        entity.setTipNotificationTime(entity.getTipNotificationTime().withOffsetSameInstant(OffsetDateTime.now().getOffset()));

        super.insertOrUpdate(entity);

    }

    public MobileOptions find(Long userId) throws SQLException {
        try {
            MobileOptions op = (MobileOptions) getEntityManager().createQuery("SELECT mo FROM MobileOptions mo WHERE mo.user.id=:userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return op;
        } catch (NoResultException e) {
            MobileOptions entity = new MobileOptions();
            entity.setUser(getEntityManager().find(User.class, userId));

            entity.setAllowTipNotifications(false);
            entity.setTipNotificationTime(OffsetTime.of(12, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setAllowDrinkNotifications(false);
            entity.setDrinkNotificationTime(OffsetTime.of(19, 0, 0, 0, OffsetDateTime.now().getOffset()));

            entity.setNotificationToken("");

            super.insertOrUpdate(entity);
            return entity;

        }
    }

}
