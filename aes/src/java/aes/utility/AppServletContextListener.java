/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;


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
        servletContext = servletContextEvent.getServletContext();    
    }
    
    public static ServletContext getServletContext(){
        return servletContext;
    }
    
}
