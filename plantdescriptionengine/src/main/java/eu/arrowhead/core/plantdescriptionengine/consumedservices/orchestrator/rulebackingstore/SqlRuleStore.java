package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import java.util.HashSet;
import java.util.Set;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    private Connection connection = null;

    public void init() throws RuleStoreException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connection = DriverManager
                    .getConnection("jdbc:mysql://localhost/arrowhead?"
                            + "user=root&password=password");

            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS pde_rule (id INT);");
            ResultSet resultSet = statement.executeQuery("select * from pde_rule");

            while (resultSet.next()) {
                System.out.println(resultSet.getString("id"));
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuleStoreException("Failed to initialize rule store", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Integer> readRules() throws RuleStoreException {

        try {
            final var result = new HashSet<Integer>();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from pde_rule");

            while (resultSet.next()) {
                result.add(resultSet.getInt("id"));
            }

            return result;

        } catch (SQLException e) {
            throw new RuleStoreException("Failed to read rules", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRules(Set<Integer> rules) throws RuleStoreException {

        try {

            final String sql = "INSERT INTO pde_rule(id) VALUES(?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (Integer rule : rules) {
                statement.setInt(1, rule);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
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
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM pde_rule;");
        } catch (SQLException e) {
            throw new RuleStoreException("Failed to delete orchestration rules", e);
        }
    }
}