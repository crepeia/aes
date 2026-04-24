package aes.persistence;

import aes.model.EmaAnswer;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.sql.SQLException;

public class EmaAnswerDAO extends GenericDAO<EmaAnswer> {

    public EmaAnswerDAO() throws NamingException {
        super(EmaAnswer.class);
    }

    public void saveAnswer(EmaAnswer answer, EntityManager entityManager) throws SQLException {
        // Aproveitamos o método insertOrUpdate que já existe no seu GenericDAO
        super.insertOrUpdate(answer, entityManager);
    }
}