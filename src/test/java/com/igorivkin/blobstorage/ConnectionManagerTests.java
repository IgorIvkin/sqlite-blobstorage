package com.igorivkin.blobstorage;

import com.igorivkin.blobstorage.database.ConnectionManager;
import com.igorivkin.blobstorage.exceptions.GenericDatabaseException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class ConnectionManagerTests {
    @Autowired
    private ConnectionManager connectionManager;

    @Test
    public void connectionManagerGetConnection() throws SQLException {
        try (Connection newConnection = this.connectionManager.getConnection()) {
            assertNotEquals(newConnection, null);
        }
    }

    @Test
    public void connectionManagerGetConnectionByIdVolume() throws SQLException {
        try(Connection newConnection = this.connectionManager.getConnection(1)) {
            assertNotEquals(newConnection, null);
        }
    }

    @Test
    public void connectionManagerManualClose() throws SQLException, GenericDatabaseException {
        // Normally the connections should be created in context try-with-resource
        // like in the test "connectionManagerGetConnection" but the system provides also
        // manual closing in case if something special is needed. So i've decided to make test
        // case to check that.
        Connection newConnection = this.connectionManager.getConnection();
        try {
            assertNotEquals(newConnection, null);
            assertFalse(newConnection.isClosed());
        }
        finally {
            this.connectionManager.closeConnection(newConnection);
        }
        assertTrue(newConnection.isClosed());
    }
}
