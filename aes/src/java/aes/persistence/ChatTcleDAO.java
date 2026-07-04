package aes.persistence;

import aes.model.ChatTCLE;
import aes.model.User;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author luansb
 */
public class ChatTcleDAO extends GenericDAO<ChatTCLE> {
    public ChatTcleDAO() throws NamingException {
        super(ChatTCLE.class);
    }

    public ChatTCLE getLatestByUser(User user, EntityManager em) {
        List<ChatTCLE> result = em.createQuery(
                "SELECT c FROM ChatTCLE c "
                + "WHERE c.user = :user "
                + "ORDER BY c.id DESC",
                ChatTCLE.class)
                .setParameter("user", user)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }
}
