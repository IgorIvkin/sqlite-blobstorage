package com.igorivkin.blobstorage.blobvolume;

import com.igorivkin.blobstorage.blobitem.BlobStoredItemAddress;
import com.igorivkin.blobstorage.database.ConnectionManager;
import com.igorivkin.blobstorage.blobitem.BlobItem;
import com.igorivkin.blobstorage.exceptions.GenericDatabaseException;
import com.igorivkin.blobstorage.blobitem.BlobItemValidator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.*;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlobVolume {
    private final ConnectionManager connectionManager;
    private final BlobItemValidator blobItemValidator;

    private String volumeName;
    private File volumeFile;
    private int volumeId;

    public BlobVolume(ConnectionManager connectionManager,
                      BlobItemValidator blobItemValidator) {
        this.connectionManager = connectionManager;
        this.blobItemValidator = blobItemValidator;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public File getVolumeFile() {
        return volumeFile;
    }

    public void setVolumeFile(File volumeFile) {
        this.volumeFile = volumeFile;
    }

    public int getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(int volumeId) {
        this.volumeId = volumeId;
    }

    public void initialize(int newVolumeIndex) throws SQLException {
        this.setVolumeName(String.format(this.connectionManager.getVolumeName(), newVolumeIndex));
        String createEntitiesTable = "CREATE TABLE IF NOT EXISTS entities (\n"
                                     + " id integer PRIMARY KEY,\n"
                                     + " mime_type text NOT NULL,\n"
                                     + " status integer NOT NULL,\n"
                                     + " content blob\n"
                                     + ");";
        this.ddl(createEntitiesTable);
        File volumeFile = new File(this.connectionManager.getDatabasePath() + this.getVolumeName());
        this.setVolumeFile(volumeFile);
        this.setVolumeId(newVolumeIndex);
    }

    /**
     * Execute statement without preparing any kind of params. Mostly applicable to
     * DDL-statements like creation of tables.
     * @param query SQL-query to execute
     * @throws SQLException it attempts to open connection and execute statement
     */
    public void ddl(String query) throws SQLException {
        try (Connection connection = this.connectionManager.getConnection(this.getConnectionString())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            }
        }
    }

    /**
     * Inserts the entity given by its parameters like mime-type, status and content
     * to a database.
     * @param item entity representing the file that need to be stored in blob storage
     * @return number of affected rows, normally 1
     * @throws SQLException it attempts to store data to SQL-database
     * @throws GenericDatabaseException it attempts to validate data before the insert
     */
    public BlobStoredItemAddress insert(BlobItem item) throws SQLException, GenericDatabaseException {
        if(this.blobItemValidator.validate(item)) {
            String sql = "INSERT INTO entities(mime_type, status, content) VALUES(?, ?, ?)";
            try (Connection connection = this.connectionManager.getConnection(this.getConnectionString())) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, item.getMimeType());
                    statement.setInt(2, item.getStatus());
                    statement.setBytes(3, item.getContent());
                    statement.executeUpdate();
                    ResultSet resultSet = statement.getGeneratedKeys();
                    if(resultSet.next()) {
                        BlobStoredItemAddress storedItem = new BlobStoredItemAddress();
                        storedItem.setId(resultSet.getLong(1));
                        storedItem.setVolumeId(this.getVolumeId());
                        return storedItem;
                    } else {
                        throw new GenericDatabaseException("Cannot get generated key for an inserted item");
                    }
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Deletes the entity by its given ID.
     * @throws SQLException it attempts to delete from sqlite database
     */
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM entities WHERE id = ?";
        try (Connection connection = this.connectionManager.getConnection(this.getConnectionString())) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Returns an entity by its given id.
     * @param id id of entity to return
     * @return entity that corresponds to its ID, null if nothing was found.
     * @throws SQLException it attempts to select from sqlite database.
     */
    public BlobItem getById(long id) throws SQLException {
        String sql = "SELECT * FROM entities WHERE id = ?";
        try (Connection connection = this.connectionManager.getConnection(this.getConnectionString())) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if(resultSet.next()) {
                        BlobItem item = new BlobItem();
                        item.setId(resultSet.getLong("id"));
                        item.setMimeType(resultSet.getString("mime_type"));
                        item.setStatus(resultSet.getInt("status"));
                        item.setContent(resultSet.getBytes("content"));
                        return item;
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * A shortcut to get connection string based on connection manager's
     * connection string and current volume name.
     * @return a connection string to establish the connection
     */
    private String getConnectionString() {
        return this.connectionManager.getConnectionString(this.getVolumeName());
    }
}
