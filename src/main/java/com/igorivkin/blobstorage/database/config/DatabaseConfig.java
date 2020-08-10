package com.igorivkin.blobstorage.database.config;

import org.springframework.stereotype.Component;
import org.sqlite.SQLiteConfig;

@Component
public class DatabaseConfig {
    private SQLiteConfig connectionConfig;

    public DatabaseConfig() {
        connectionConfig = new SQLiteConfig();

        // Journal mode WAL allows to improve concurrency between readers and writers
        // using multiply readers at the same time.
        // https://www.sqlite.org/wal.html
        connectionConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);

        // We will use basic encoding UTF-8 for everything.
        // https://www.sqlite.org/pragma.html#pragma_encoding
        connectionConfig.setEncoding(SQLiteConfig.Encoding.UTF8);
    }

    public SQLiteConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(SQLiteConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}
