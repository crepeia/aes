package aes.persistence;

import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;
	
	static {
		try {
			// Create the SessionFactory from standard (hibernate.cfg.xml) 
			// config file.
			//sessionFactory = new Configuration().configure().buildSessionFactory();
                        Configuration configuration = new Configuration();
                        configuration.configure();
                        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
                        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			// Log the exception. 
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
