package resources;

import dao.factories.DAOAbstractFactory;
import dao.factories.DAOFactoryJDBC;
import dao.implementations.DAOImplJDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class MySQLConnectionManager {

    private static MySQLConnectionManager sInstance = null;

    private String dbURL;
    private String user;
    private String password;

    private Connection connection;

    private MySQLConnectionManager() {
    }

    /**
     * Set up this manager with MySQL DB instance parameters and attempt to open a connection to the database through
     * MySQL JDBC Driver
     *
     * @param host     Hostname of the machine that hosts the MySQL instance
     * @param port     TCP port where the instance accepts incoming connections
     * @param user     DB user
     * @param password Password for the DB user
     * @param schema   which schema (DB) to use
     */
    public void setUpAndConnect(String host, int port, String user, String password, String schema) {

        // Update attributes

        this.dbURL = "jdbc:mysql://" + host + ":" + port + "/" + schema;
        this.user = user;
        this.password = password;

        // Try connecting to the MySQL

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            reconnect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reopens the current connection if it's closed. If it's still opened, it closes it and reopens it again
     */
    public void reconnect() {

        // Create DB connection, configure it and store it as an attribute of the servlet context

        try {

            // If the current connection isn't closed, close it before opening a new one

            if (connection != null && !connection.isClosed())
                connection.close();

            Connection tempConn = DriverManager.getConnection(dbURL, this.user, this.password);

            // Check that the connection could be established correctly

            if (tempConn != null) {

                // Set options

                tempConn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); // Most relaxed isolation level
                tempConn.setAutoCommit(false); // Enforce explicit commit-rollback calls

                // Store the connection

                connection = tempConn;

                // Register DAOFactories and configure its dependencies

                DAOAbstractFactory.get().registerDAOFactory(new DAOFactoryJDBC(), (dao, dependencies) -> {
                    HashMap<String, Object> dependenciesMap = new HashMap<>();
                    dependenciesMap.put(DAOImplJDBC.CONNECTION_IDENTIFIER, connection);
                    dao.configureDependencies(dependenciesMap);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static MySQLConnectionManager getInstance() {
        if (sInstance == null)
            sInstance = new MySQLConnectionManager();

        return sInstance;
    }

}
