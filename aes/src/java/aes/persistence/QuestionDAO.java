/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Question;
import javax.naming.NamingException;

/**
 *
 * @author Matheus Carvalho
 */
public class QuestionDAO extends GenericDAO<Question>{
    
    public QuestionDAO() throws NamingException {
        super(Question.class);
    }
    
}
