/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

/**
 *
 * @author Leonorico
 */

public class RESTApiResponse {

    private String message;
    private Object entityData;
    
    public RESTApiResponse() {}
    
    public RESTApiResponse(String message, Object data) {
        this.message = message;
        this.entityData = data;
    }
    
    public RESTApiResponse(String message) {
        this.message = message;
    }
    
    public RESTApiResponse(Object data) {
        this.entityData = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getEntityData() {
        return entityData;
    }

    public void setEntityData(Object data) {
        this.entityData = data;
    }
    
    
}
