package aes.persistence;

import aes.model.QuestionUser;
import javax.naming.NamingException;

/**
 *
 * @author LuanBarbs
 */
public class QuestionUserDAO extends GenericDAO<QuestionUser>{
    
    public QuestionUserDAO() throws NamingException {
        super(QuestionUser.class);
    }
}
