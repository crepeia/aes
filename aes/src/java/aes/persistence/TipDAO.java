package aes.persistence;

import aes.model.Tip;
import javax.naming.NamingException;

/**
 *
 * @author patri
 */
/**
 * @Deprecated
 * This class is no longer in use, because tip is not in the databank anymore.
 * Challenge is in .properties file and should be used from there, instead.
**/
@Deprecated
public class TipDAO extends GenericDAO<Tip>{
    
    public TipDAO() throws NamingException {
        super(Tip.class);
    }
}
