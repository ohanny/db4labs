package fr.icodem.db4labs.event;

import fr.icodem.db4labs.database.DbDescriptor;

public class SaveDatabaseEvent {
    private DbDescriptor database;

    public SaveDatabaseEvent(DbDescriptor database) {
        this.database = database;
    }

    public DbDescriptor getDatabase() {
        return database;
    }
}
