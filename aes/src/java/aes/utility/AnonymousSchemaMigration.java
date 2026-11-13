package aes.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

/**
 *
 * @author luansb
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class AnonymousSchemaMigration {
    private static final Logger LOGGER = Logger.getLogger(AnonymousSchemaMigration.class.getName());

    @Resource(lookup = "AesJNDI")
    private DataSource dataSource;

    @PersistenceUnit(unitName = "aesPU")
    private EntityManagerFactory entityManagerFactory;
    
    private String firstValue(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getString(1) : null;
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private String columnType(Connection connection, String table, String column) throws SQLException {
        String type = firstValue(connection, "SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "' AND COLUMN_NAME = '" + column + "'");

        if (type == null) {
            throw new SQLException("Coluna não encontrada: " + table + "." + column);
        }

        return type.toLowerCase(Locale.ROOT).replaceAll("\\(\\d+\\)", "").trim();
    }
    
    private void migrateAnonymousKeys(Connection connection) throws SQLException {
        String column = firstValue(connection, "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_anonymous_key' AND COLUMN_NAME = 'active_slot'");

        if (column == null) {
            execute(connection, "ALTER TABLE tb_anonymous_key ADD COLUMN active_slot INT NULL");
        }

        String duplicate = firstValue(connection, "SELECT 1 FROM tb_anonymous_key WHERE revoked = 0 GROUP BY instance_id HAVING COUNT(*) > 1 LIMIT 1");

        if (duplicate != null) {
            throw new SQLException("Existem múltiplas chaves ativas para uma instalação. A migração não escolherá automaticamente qual revogar.");
        }

        execute(connection, "UPDATE tb_anonymous_key SET active_slot = CASE WHEN revoked = 0 THEN 1 ELSE NULL END WHERE NOT (active_slot <=> CASE WHEN revoked = 0 THEN 1 ELSE NULL END)");

        String activeIndex = firstValue(connection, "SELECT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_anonymous_key' AND NON_UNIQUE = 0 GROUP BY INDEX_NAME HAVING GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) = 'instance_id,active_slot'");

        if (activeIndex == null) {
            execute(connection, "ALTER TABLE tb_anonymous_key ADD UNIQUE INDEX uk_anonymous_key_active (instance_id, active_slot)");
        }

        String oldIndexSql = "SELECT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_anonymous_key' AND NON_UNIQUE = 0 GROUP BY INDEX_NAME HAVING COUNT(*) = 1 AND MAX(COLUMN_NAME) = 'instance_id'";
        String oldIndex;

        while ((oldIndex = firstValue(connection, oldIndexSql)) != null) {
            execute(connection, "ALTER TABLE tb_anonymous_key DROP INDEX `" + oldIndex.replace("`", "``") + "`");
        }

        LOGGER.info("[ANON MIGRATION] Histórico e índice de chaves ativas verificados.");
    }
    
    private void migrateAnonymousTokenForeignKey(Connection connection) throws SQLException {
        String orphan = firstValue(connection, "SELECT 1 FROM tb_authentication_token t LEFT JOIN tb_anonymous_key k ON k.id = t.anonymous_key_id WHERE t.anonymous_key_id IS NOT NULL AND k.id IS NULL LIMIT 1");

        if (orphan != null) {
            throw new SQLException("Existem tokens apontando para chaves anônimas inexistentes. Nenhum token será apagado automaticamente.");
        }

        String parentType = columnType(connection, "tb_anonymous_key", "id");

        if (!"bigint".equals(parentType) && !"bigint unsigned".equals(parentType)) {
            throw new SQLException("Tipo inesperado para tb_anonymous_key.id: " + parentType);
        }

        String childType = columnType(connection, "tb_authentication_token", "anonymous_key_id");

        if (!parentType.equals(childType)) {
            execute(connection, "ALTER TABLE tb_authentication_token MODIFY COLUMN anonymous_key_id " + parentType + " NULL");
        }

        String foreignKey = firstValue(connection, "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_authentication_token' AND COLUMN_NAME = 'anonymous_key_id' AND REFERENCED_TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME = 'tb_anonymous_key' AND REFERENCED_COLUMN_NAME = 'id'");

        if (foreignKey == null) {
            execute(connection, "ALTER TABLE tb_authentication_token ADD CONSTRAINT FK2shls6ak3o802rrqrurn628af FOREIGN KEY (anonymous_key_id) REFERENCES tb_anonymous_key (id)");
        }

        LOGGER.info("[ANON MIGRATION] Tipo e chave estrangeira dos tokens verificados.");
    }
    
    @PostConstruct
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }

            migrateAnonymousKeys(connection);
            migrateAnonymousTokenForeignKey(connection);

            LOGGER.info("[ANON MIGRATION] Migração concluída.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "[ANON MIGRATION] Falha ao atualizar o esquema anônimo.", ex);
            throw new IllegalStateException("Falha na migração do esquema anônimo.", ex);
        }
    }
}