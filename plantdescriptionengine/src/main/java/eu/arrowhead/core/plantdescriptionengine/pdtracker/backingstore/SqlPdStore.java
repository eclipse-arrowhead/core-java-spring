package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Class that reads and writes Plant Description Entries to an SQL database.
 */
public class SqlPdStore implements PdStore {

    // TODO: We store Plant Descriptions in their raw JSON form.
    // In the future, we'll want to create separate tables for each subfield
    // of a Plant Description.
    private final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS plant_description (id INT, plant_description TEXT);";
    private final String SQL_SELECT_ALL = "select * from plant_description;";
    private final String SQL_INSERT_PD = "INSERT INTO plant_description(id, plant_description) VALUES(?, ?);";
    private final String SQL_DELETE_ONE = "DELETE FROM plant_description where id=?;";
    private final String SQL_DELETE_ALL = "DELETE FROM plant_description;";

    private Connection connection;

    /**
     * Throws an {@code IllegalStateException} if this instance has not been
     * initialized.
     */
    private void ensureInitialized() {
        if (connection == null) {
            throw new IllegalStateException("SqlPdStore has not been initialized.");
        }
    }

    /**
     * Initializes the PD store for use by connecting to the database and
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
    ) throws PdStoreException {

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
            throw new PdStoreException("Failed to initialize PD store", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlantDescriptionEntryDto> readEntries() throws PdStoreException {
        ensureInitialized();
        try {
            final List<PlantDescriptionEntryDto> result = new ArrayList<>();
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SQL_SELECT_ALL);

            while (resultSet.next()) {
                final String jsonText = resultSet.getString("plant_description");
                final byte[] bytes = jsonText.getBytes(StandardCharsets.UTF_8);
                final PlantDescriptionEntryDto entry = PlantDescriptionEntryDto.decodeJson(Buffer.wrap(bytes));
                result.add(entry);
            }

            return result;

        } catch (final SQLException e) {
            throw new PdStoreException("Failed to read Plant Description entries", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final PlantDescriptionEntryDto entry) throws PdStoreException {
        ensureInitialized();
        Objects.requireNonNull(entry, "Expected entry.");

        try {
            final PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PD);
            final byte[] bytes = new byte[2048]; // TODO: Remove arbitrary limit
            final Buffer buffer = Buffer.wrap(bytes);
            buffer.clear();
            entry.encodeJson(buffer);
            buffer.close();
            statement.setInt(1, entry.id());
            statement.setString(2, new String(bytes));
            statement.executeUpdate();

        } catch (final SQLException e) {
            throw new PdStoreException("Failed to write Plant Description entry to database", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final int id) throws PdStoreException {
        ensureInitialized();
        try {
            final PreparedStatement statement = connection.prepareStatement(SQL_DELETE_ONE);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new PdStoreException("Failed to delete Plant Description entry", e);
        }
    }

    /**
     * Removes all Plant Description Entries from the store.
     */
    public void removeAll() throws PdStoreException {
        ensureInitialized();
        try {
            final Statement statement = connection.createStatement();
            statement.execute(SQL_DELETE_ALL);
        } catch (final SQLException e) {
            throw new PdStoreException("Failed to delete Plant Description entries", e);
        }
    }
}