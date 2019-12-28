package aes.persistence;

import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;


public class HibernateUtil {

	private static final SessionFactory sessionFactory;
	
	static {
		try {
			// Create the SessionFactory from standard (hibernate.cfg.xml) 
			// config file.
			//sessionFactory = new Configuration().configure().buildSessionFactory();
                        Configuration configuration = new Configuration();
                        configuration.configure();
                        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()). buildServiceRegistry();
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
