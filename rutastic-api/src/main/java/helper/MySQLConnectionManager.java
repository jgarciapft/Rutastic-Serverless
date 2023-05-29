package helper;

import dao.factories.DAOAbstractFactory;
import dao.factories.DAOFactoryJDBC;
import dao.implementations.DAODependencyConfigurator;
import dao.implementations.DAOImplJDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class MySQLConnectionManager {

    private static MySQLConnectionManager sInstance = null;

    private String readOnlyDBUrl;
    private String writeDBUrl;
    private String user;
    private String password;

    private Connection readOnlyConnection;
    private Connection writeConnection;

    private MySQLConnectionManager() {
    }

    /**
     * Set up this manager with MySQL DB instance parameters and attempt to open a connection to the database through
     * MySQL JDBC Driver
     *
     * @param readOnlyEndpoint Read-only endpoint of the MySQL instance
     * @param writeEndpoint    Writeable endpoint of the MySQL instance
     * @param port             TCP port where the instance accepts incoming connections
     * @param user             DB user
     * @param password         Password for the DB user
     * @param schema           which schema (DB) to use
     */
    public void setUpAndConnect(String readOnlyEndpoint, String writeEndpoint, int port, String user, String password, String schema) {

        // Update attributes

        this.readOnlyDBUrl = String.format("jdbc:mysql://%s:%d/%s", readOnlyEndpoint, port, schema);
        this.writeDBUrl = String.format("jdbc:mysql://%s:%d/%s", writeEndpoint, port, schema);
        this.user = user;
        this.password = password;

        // Try connecting to the MySQL

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect();
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading JDBC Driver: " + e.getMessage());
        }
    }

    private void connect() {
        try {
            Connection readOnlyConnection = DriverManager.getConnection(this.readOnlyDBUrl, this.user, this.password);
            Connection writeConnection = DriverManager.getConnection(this.writeDBUrl, this.user, this.password);

            if (readOnlyConnection != null && writeConnection != null) {

                readOnlyConnection.setReadOnly(true);
                readOnlyConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); // Most relaxed isolation level

                writeConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); // Most relaxed isolation level
                writeConnection.setAutoCommit(false); // Enforce explicit commit-rollback calls

                // Store the connection

                this.readOnlyConnection = readOnlyConnection;
                this.writeConnection = writeConnection;

                // Register DAOFactories and configure its dependencies

                DAODependencyConfigurator<DAOImplJDBC> jdbcDaoDependencyConfigurator = (dao, dependencies) -> {
                    HashMap<String, Object> dependenciesMap = new HashMap<>();

                    dependenciesMap.put(DAOImplJDBC.READONLY_CONNECTION_IDENTIFIER, readOnlyConnection);
                    dependenciesMap.put(DAOImplJDBC.WRITE_CONNECTION_IDENTIFIER, writeConnection);

                    dao.configureDependencies(dependenciesMap);
                };

                DAOAbstractFactory.get().registerDAOFactory(new DAOFactoryJDBC(), jdbcDaoDependencyConfigurator);
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to AuroraDB MySQL Cluster: " + e.getMessage());
        }
    }

    public Connection getReadOnlyConnection() {
        return readOnlyConnection;
    }

    public Connection getWriteConnection() {
        return writeConnection;
    }

    public static MySQLConnectionManager getInstance() {
        if (sInstance == null)
            sInstance = new MySQLConnectionManager();

        return sInstance;
    }
}
