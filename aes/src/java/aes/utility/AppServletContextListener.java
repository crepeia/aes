/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author patri
 */
public class AppServletContextListener implements ServletContextListener {
    //private static Properties properties = new Properties();
    private static ServletContext servletContext;
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent){
        String key = servletContextEvent.getServletContext().getInitParameter("key");
        servletContext = servletContextEvent.getServletContext();    
    }
    
    public static ServletContext getServletContext(){
        return servletContext;
    }
    
 

    /*public static Properties getProperties(){
        return properties;
    }*/
}
