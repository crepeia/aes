package aes.persistence;

import aes.model.SensorData;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.sql.SQLException;

public class SensorDataDAO extends GenericDAO<SensorData> {

    public SensorDataDAO() throws NamingException {
        super(SensorData.class);
    }

    public void saveSensorData(SensorData data, EntityManager entityManager) throws SQLException {
        super.insertOrUpdate(data, entityManager);
    }
}