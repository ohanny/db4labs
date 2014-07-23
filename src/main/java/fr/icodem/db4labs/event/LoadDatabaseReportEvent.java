package fr.icodem.db4labs.event;

import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.dbtools.service.TaskState;

import java.util.UUID;

public class LoadDatabaseReportEvent {

    private AppName appName;
    private TaskState state;
    private UUID uuid;

    public LoadDatabaseReportEvent(UUID uuid, AppName appName, TaskState state) {
        this.uuid = uuid;
        this.appName = appName;
        this.state = state;
    }

    public AppName getAppName() {
        return appName;
    }

    public TaskState getState() {
        return state;
    }

    public UUID getUuid() {
        return uuid;
    }
}
