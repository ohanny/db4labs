package fr.icodem.db4labs.database;

import com.google.inject.Singleton;

import java.sql.Connection;

// todo use connection pool
@Singleton
public class ConnectionProvider {
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
