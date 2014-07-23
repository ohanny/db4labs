package fr.icodem.db4labs.event;

import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.database.ConnectionProperties;

import java.util.UUID;

public class LoadDatabaseEvent {
    private UUID uuid;
    private AppName appName;
    private ConnectionProperties connectionProperties;

    /**
     * used to load in H2 internal database
     * @param appName
     */
    public LoadDatabaseEvent(AppName appName) {
        this.appName = appName;
        this.uuid = UUID.randomUUID();
    }

    public LoadDatabaseEvent(AppName appName, ConnectionProperties connectionProperties) {
        this.appName = appName;
        this.connectionProperties = connectionProperties;
        this.uuid = UUID.randomUUID();
    }

    public AppName getAppName() {
        return appName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }
}
