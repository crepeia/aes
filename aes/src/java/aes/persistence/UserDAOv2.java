package aes.persistence;

import aes.model.User;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;

/**
 *
 * @author luansb
 */
@Dependent
public class UserDAOv2 extends GenericDAOv2<User> {
    public UserDAOv2() {
        super();
    }
    
    // Valida as credenciais do usuário
    public User checkCredentials(String email, String providedPassword) throws EncrypterException {
        List<User> users = list("email", email);
        
        if (users.isEmpty()) {
            return null;
        }
        
        User user = users.get(0);

        if (Encrypter.compareHash(providedPassword, user.getPassword(), user.getSalt())) {
            return user;
        }

        return null;
    }
    
    // Cria um usuário completo
    public User createUser(User user, String password) throws EncrypterException {
        byte[] salt = Encrypter.generateRandomSecureSalt(16);
        
        user.setSalt(salt);
        user.setPassword(Encrypter.hashPassword(password, salt));
        
        User saved = insertOrUpdate(user);

        Logger.getLogger(UserDAOv2.class.getName())
            .log(Level.INFO, "Usuário ''{0}'' cadastrado.", user.getEmail());

        return saved;
    }
    
    // Gera código de recuperação
    public User createRecoveryCode(User user) {
        user.setRecoverCode(GenerateCode.generate());

        return insertOrUpdate(user);
    }
    
    // Busca usuários pelo e-mail
    public List<User> findByEmail(String email) {
        return list("email", email);
    }
}
