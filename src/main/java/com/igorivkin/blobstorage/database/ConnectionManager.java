package com.igorivkin.blobstorage.database;

import com.igorivkin.blobstorage.database.config.DatabaseConfig;
import com.igorivkin.blobstorage.exceptions.GenericDatabaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class ConnectionManager {
    @Value("${blobstorage.jdbc.connectionstring}")
    private String connectionString;

    @Value("${blobstorage.databasepath}")
    private String databasePath;

    @Value("${blobstorage.jdbc.volumename}")
    private String volumeName;

    private final DatabaseConfig databaseConfig;

    public ConnectionManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Returns database connection using a given connection string.
     * @param connectionString string representing params of connection
     * @return database connection to interact with
     * @throws SQLException it attempts to establish connection to SQLite
     */
    public Connection getConnection(String connectionString) throws SQLException {
        Connection connection = null;
        connection = DriverManager.getConnection(connectionString, databaseConfig.getConnectionConfig().toProperties());
        return connection;
    }

    /**
     * In case if the only volume id is defined then it can build connection using
     * default database volume name.
     * @param volumeId id of blob volume
     * @return database connection to interact with
     * @throws SQLException it attempts to establish connection to SQLite
     */
    public Connection getConnection(int volumeId) throws SQLException {
        String formattedConnectionString = String.format(this.connectionString, String.format(this.volumeName, volumeId));
        return this.getConnection(formattedConnectionString);
    }

    /**
     * In case if no connection string is defined then use the default one
     * @return database connection to interact with
     * @throws SQLException it attempts to establish connection to SQLite
     */
    public Connection getConnection() throws SQLException {
        return this.getConnection(String.format(this.connectionString, String.format(this.volumeName, 1)));
    }

    /**
     * Closes the given connection.
     * @param connection connection to close
     */
    public void closeConnection(Connection connection) throws GenericDatabaseException, SQLException {
        if(connection == null) {
            throw new GenericDatabaseException("Cannot close connection, given connection is null");
        } else {
            if(!connection.isClosed()) {
                connection.close();
            }
        }
    }

    /**
     * Returns connection string with inserted volume name.
     * @param volumeName volume name to build connection string
     * @return connection string with volume name
     */
    public String getConnectionString(String volumeName) {
        return String.format(this.connectionString, volumeName);
    }

    /**
     * Returns default connection string.
     * @return default connection string
     */
    public String getConnectionString() {
        return this.connectionString;
    }

    /**
     * Returns current database path.
     * @return current database path
     */
    public String getDatabasePath() {
        return this.databasePath;
    }

    /**
     * Returns default volume name.
     * @return default volume name
     */
    public String getVolumeName() {
        return this.volumeName;
    }
}
