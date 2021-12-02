package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class that reads and writes Orchestration rules to an SQL database.
 */
public class SqlRuleStore implements RuleStore {

    private static final Logger logger = LoggerFactory.getLogger(RuleStore.class);

    private final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `pde_rule` (`id` bigint(20) PRIMARY KEY, `plant_description_id` bigint(20) NOT NULL ) ENGINE = InnoDB DEFAULT CHARSET = utf8";

    private final String SQL_SELECT_RULES = "select id from pde_rule where plant_description_id=?;";
    private final String SQL_REPLACE_RULE = "REPLACE INTO pde_rule(id, plant_description_id) VALUES(?, ?);";
    private final String SQL_DELETE_RULES = "DELETE FROM pde_rule where plant_description_id=?;";
    private final String ID = "id";

    private Connection connection;

    /**
     * Throws an {@code IllegalStateException} if this instance has not been'
     * initialized.
     */
    private void ensureInitialized() {
        if (connection == null) {
            throw new IllegalStateException("SqlRuleStore has not been initialized.");
        }
    }

    /**
     * Initializes the rule store for use by connecting to the database and
     * creating the necessary tables.
     *
     * @param driverClassName The driver class for the mysql database.
     * @param connectionUrl   URL of the database connection.
     * @param username        Username to use when connecting to the database.
     * @param password        Password to use when connecting to the database.
     */
    public void init(
        final String driverClassName,
        final String connectionUrl,
        final String username,
        final String password
    ) throws RuleStoreException {

        Objects.requireNonNull(driverClassName, "Expected database driver name.");
        Objects.requireNonNull(connectionUrl, "Expected connection URL.");
        Objects.requireNonNull(username, "Expected username.");
        Objects.requireNonNull(password, "Expected password.");

        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(connectionUrl, username, password);
            final Statement statement = connection.createStatement();
            statement.execute(SQL_CREATE_TABLE);
        } catch (final ClassNotFoundException | SQLException e) {
            connection = null;
            throw new RuleStoreException("Failed to initialize rule store", e);
        }
    }

    @Override
    public Set<Integer> readRules(final int plantDescriptionId) throws RuleStoreException {
        ensureInitialized();

        try {
            connection.setAutoCommit(true);
            final Set<Integer> result = new HashSet<>();
            final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_RULES);
            statement.setInt(1, plantDescriptionId);
            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                result.add(resultSet.getInt(ID));
            }

            return result;

        } catch (final SQLException e) {
            throw new RuleStoreException("Failed to read rules", e);
        }
    }

    @Override
    public void writeRules(final int plantDescriptionId, final Set<Integer> rules) throws RuleStoreException {
        Objects.requireNonNull(rules, "Expected rules.");
        ensureInitialized();

        try {
            connection.setAutoCommit(false);
            final PreparedStatement statement = connection.prepareStatement(SQL_REPLACE_RULE);
            for (final Integer rule : rules) {
                statement.setInt(1, rule);
                statement.setInt(2, plantDescriptionId);
                statement.executeUpdate();
            }
            connection.commit();
        } catch (final SQLException e) {
            rollback();
            throw new RuleStoreException("Failed to write orchestration rules to database", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Failed to set DB connection's auto-commit mode to true", e);
            }
        }
    }

    @Override
    public void removeRules(final int plantDescriptionId) throws RuleStoreException {
        ensureInitialized();
        try {
            connection.setAutoCommit(true);
            final PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RULES);
            statement.setInt(1, plantDescriptionId);
            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuleStoreException("Failed to delete orchestration rules", e);
        }
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.error("DB Rollback failed.", e);
        }
    }
}
