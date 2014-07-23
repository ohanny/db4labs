package fr.icodem.db4labs.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.icodem.db4labs.app.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Internal database
 */
@Singleton
public class H2Database {

    private Connection connection;

    @Inject private ConnectionProvider cnProvider;
    @Inject private DbDescriptorProvider dbDescriptorProvider;
    @Inject private DbSaver dbSaver;
    @Inject private Config config;

    private final static String DB_NAME = "db4labs";

    // todo use connection pool
    private void openConnection() {
        try {
            Class.forName("org.h2.Driver");
            String workingPath = config.getConfigPath().toAbsolutePath().toString();
            connection = DriverManager.getConnection("jdbc:h2:" + workingPath + "/" + DB_NAME, "sa", "");
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            cnProvider.setConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cnProvider.setConnection(null);
            System.out.println("fin h2 manager : cleanAll()");
        }
    }

    public void loadDatabase(Path data, String schema) throws Exception {
        cleanAll();

        openConnection();

        DbLoader dbLoader = DbLoader.getInstance(connection, DbType.H2);
        DbDescriptor descriptor = dbLoader.load(data, schema);
        dbDescriptorProvider.setDescriptor(descriptor);
    }

    public void saveDatabase(Path path) throws Exception {
        if (connection != null && !connection.isClosed()) {
            dbSaver.save(path);
        }
    }

    public void cleanAll() {
        closeConnection();

        try {
            Path dbFile = config.getConfigPath().resolve(DB_NAME + ".h2.db");
            if (Files.exists(dbFile)) Files.delete(dbFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
