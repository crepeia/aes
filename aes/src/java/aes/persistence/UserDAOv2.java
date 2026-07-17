package aes.persistence;

import aes.model.User;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * @author luansb
 */
@ApplicationScoped
public class UserDAOv2 extends GenericDAOv2<User> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(UserDAOv2.class.getName());
    private static final int SALT_SIZE = 16;

    public UserDAOv2() {
        super(User.class);
    }
    
    private void protectOrphanCollections(User user) {
        User managed = find(user.getId());
        if (managed == null) {
            return;
        }
        if (user.getAppointmentsUser() == null) {
            user.setAppointmentsUser(managed.getAppointmentsUser());
        }
        if (user.getAvailablesUser() == null) {
            user.setAvailablesUser(managed.getAvailablesUser());
        }
    }
    
    @Override
    @Transactional
    public User insertOrUpdate(User user) {
        if (user.getId() <= 0) {
            persist(user);
            return user;
        }
        protectOrphanCollections(user);
        return merge(user);
    }

    public List<User> findByEmail(String email) {
        return list("email", email);
    }
    
    public User checkCredentials(String email, String providedPassword) throws EncrypterException {
        if (email == null || providedPassword == null) {
            return null;
        }
        List<User> users = findByEmail(email);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        if (Encrypter.compareHash(providedPassword, user.getPassword(), user.getSalt())) {
            return user;
        }
        return null;
    }

    private void applyPassword(User user, String rawPassword) throws EncrypterException {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia.");
        }
        byte[] salt = Encrypter.generateRandomSecureSalt(SALT_SIZE);
        user.setSalt(salt);
        user.setPassword(Encrypter.hashPassword(rawPassword, salt));
    }

    @Transactional
    public User createUser(User user, String password) throws EncrypterException {
        applyPassword(user, password);
        User saved = insertOrUpdate(user);
        LOG.log(Level.INFO, "Usuario ''{0}'' cadastrado.", user.getEmail());
        return saved;
    }

    @Transactional
    public User changePassword(User user, String newPassword) throws EncrypterException {
        applyPassword(user, newPassword);
        User saved = insertOrUpdate(user);
        LOG.log(Level.INFO, "Senha do usuario ''{0}'' redefinida.", user.getEmail());
        return saved;
    }

    @Transactional
    public User createRecoveryCode(User user) {
        user.setRecoverCode(GenerateCode.generate());
        return insertOrUpdate(user);
    }

    @Transactional
    public User registerSignIn(User user, Date when) {
        user.setSignInDate(when);
        return insertOrUpdate(user);
    }
}