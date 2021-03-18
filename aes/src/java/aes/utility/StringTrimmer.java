/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author bruno
 */

@FacesConverter(value = "stringTrimmer")
public class StringTrimmer implements Converter
{

  @Override
  public Object getAsObject(FacesContext context, UIComponent component, String value)
  {
    return value != null ? value.trim() : null;
  }

  @Override
  public String getAsString(FacesContext context, UIComponent component, Object value)
  {
    return value.toString();
  }

}
