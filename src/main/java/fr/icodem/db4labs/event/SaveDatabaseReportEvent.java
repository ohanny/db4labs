package fr.icodem.db4labs.event;

import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.dbtools.service.TaskState;

public class SaveDatabaseReportEvent {

    private AppName database;
    private TaskState state;

    public SaveDatabaseReportEvent(AppName database, TaskState state) {
        this.database = database;
        this.state = state;
    }

    public AppName getDatabase() {
        return database;
    }

    public TaskState getState() {
        return state;
    }
}
