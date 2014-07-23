package fr.icodem.db4labs.dbtools.service;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.app.Config;
import fr.icodem.db4labs.event.SaveDatabaseEvent;
import fr.icodem.db4labs.event.SaveDatabaseReportEvent;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.H2Database;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveDatabaseService extends Service<Void> {

    @Inject private H2Database h2Database;

    @Inject private AppContainer container;

    @Inject private AppNameProvider appNameProvider;

    @Inject private Config config;

    private SaveDatabaseEvent event;

    @PostConstruct
    public void init() {
        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                container.post(new SaveDatabaseReportEvent(event.getDatabase().getAppName(), TaskState.Completed));
            }
        });
    }

    @Subscribe
    public void save(SaveDatabaseEvent event) {
        this.event = event;
        restart();
    }

    private void save() {

        try {
            AppName.AppConfig appCfg = appNameProvider.getAppName().getConfig();
            Path dataPath = config.getDataPath();
            Path exportPath = dataPath.resolve(appCfg.getDataFilename());

            h2Database.saveDatabase(exportPath);

            System.out.println(event);
            h2Database.cleanAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            protected Void call() throws Exception {
                save();
                return null;
            }
        };
    }

}
