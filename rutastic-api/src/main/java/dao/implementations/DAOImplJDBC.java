package dao.implementations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * JDBC DAO Implementation to operate a database using the JDBC middleware
 *
 * @see DAOImplementation
 */
public interface DAOImplJDBC extends DAOImplementation {

    String READONLY_CONNECTION_IDENTIFIER = "readonly-connection";
    String WRITE_CONNECTION_IDENTIFIER = "write-connection";

    /**
     * Configure dependencies for JDBC DAO classes. Essentially set the connection to the database
     *
     * @param dependencies Collection of the DAO dependencies identified by a string
     */
    @Override
    default void configureDependencies(Map<String, Object> dependencies) {
        Object readOnlyConnection = dependencies.getOrDefault(READONLY_CONNECTION_IDENTIFIER, null);
        Object writeConnection = dependencies.getOrDefault(WRITE_CONNECTION_IDENTIFIER, null);
        boolean dependenciesConfigured = true;

        try {
            if (readOnlyConnection instanceof Connection && ((Connection) readOnlyConnection).isValid(1))
                setReadOnlyConnection((Connection) readOnlyConnection);
            else dependenciesConfigured = false;

            if (writeConnection instanceof Connection && ((Connection) writeConnection).isValid(1))
                setWriteConnection((Connection) writeConnection);
            else dependenciesConfigured = false;

            setDependenciesConfigured(dependenciesConfigured);
        } catch (SQLException e) {
            System.err.println("Error while configuring dependencies for " + this.getClass().getSimpleName() + ": " + e.getMessage());
            setDependenciesConfigured(false);
        }
    }

    void setReadOnlyConnection(Connection connection);

    void setWriteConnection(Connection connection);

}
