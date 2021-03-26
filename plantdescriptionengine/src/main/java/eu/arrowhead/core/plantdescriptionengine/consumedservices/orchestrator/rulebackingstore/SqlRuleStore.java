package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

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
 * Class that reads and writes Orchestration rules to file.
 * <p>
 * The PDE needs to keep track of which Orchestration rules it has created, and
 * to which Plant Description Entry each rule belongs. This information is
 * stored in memory, but it also needs to be persisted to permanent storage in
 * case the PDE is restarted. This class provides that functionality, writing
 * rules and their relationship to Plant Descriptions to file.
 */
public class SqlRuleStore implements RuleStore {

    private Connection connection;

    public void init(final String username, final String password) throws RuleStoreException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://localhost/arrowhead", username, password);

            final Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS pde_rule (id INT);");
            final ResultSet resultSet = statement.executeQuery("select * from pde_rule");

            while (resultSet.next()) {
                System.out.println(resultSet.getString("id"));
            }

        } catch (final ClassNotFoundException | SQLException e) {
            throw new RuleStoreException("Failed to initialize rule store", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> readRules() throws RuleStoreException {
        try {
            final Set<Integer> result = new HashSet<>();
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("select * from pde_rule");

            while (resultSet.next()) {
                result.add(resultSet.getInt("id"));
            }

            return result;

        } catch (final SQLException e) {
            throw new RuleStoreException("Failed to read rules", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRules(final Set<Integer> rules) throws RuleStoreException {
        Objects.requireNonNull(rules, "Expected rules.");

        try {

            final String sql = "INSERT INTO pde_rule(id) VALUES(?)";
            final PreparedStatement statement = connection.prepareStatement(sql);

            for (final Integer rule : rules) {
                statement.setInt(1, rule);
                statement.executeUpdate();
            }

        } catch (final SQLException e) {
            throw new RuleStoreException("Failed to write orchestration rules to database", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuleStoreException
     */
    @Override
    public void removeAll() throws RuleStoreException {
        try {
            final Statement statement = connection.createStatement();
            statement.execute("DELETE FROM pde_rule;");
        } catch (final SQLException e) {
            throw new RuleStoreException("Failed to delete orchestration rules", e);
        }
    }
}