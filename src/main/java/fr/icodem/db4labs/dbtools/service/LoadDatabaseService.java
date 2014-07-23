package fr.icodem.db4labs.dbtools.service;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.app.Config;
import fr.icodem.db4labs.database.ConnectionProperties;
import fr.icodem.db4labs.database.DbLoader;
import fr.icodem.db4labs.database.H2Database;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.event.LoadDatabaseReportEvent;
import fr.icodem.db4labs.event.LoadDatabaseEvent;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

// todo singleton => concurrency ?
// todo use multi threading for better performance
public class LoadDatabaseService extends Service<Void> {

    @Inject private H2Database h2Database;

    @Inject private AppContainer container;

    @Inject private Config config;

    private LoadDatabaseEvent event;

    @PostConstruct
    public void init() {
        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                container.post(new LoadDatabaseReportEvent(event.getUuid(), event.getAppName(), TaskState.Completed));
            }
        });
        setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                container.post(new LoadDatabaseReportEvent(event.getUuid(), event.getAppName(), TaskState.Failed));
            }
        });
    }

    @Subscribe
    public void load(LoadDatabaseEvent event) {
        this.event = event;
        restart();
    }

    private void load() throws Exception {
        AppName.AppConfig appCfg = event.getAppName().getConfig();

        Path dataPath = config.getDataPath();
        Path archivePath;

        archivePath = dataPath.resolve(appCfg.getDataFilename());
        load(archivePath, appCfg.getSchema());

    }

    private void load(Path dataPath, String schema) throws Exception {
        if (event.getConnectionProperties() != null) {
            loadExternalDb(event.getConnectionProperties(), dataPath, schema);
        }  else {
            loadInternalH2(dataPath, schema);
        }
    }

    private void loadExternalDb(ConnectionProperties props, Path dataPath, String schema) throws Exception {

        Class.forName(props.getDriver());

        try (Connection connection = DriverManager.getConnection(props.getUrl(),
                                                                 props.getUsername(),
                                                                 props.getPassword())) {
            connection.setAutoCommit(false);
            DbLoader dbLoader = DbLoader.getInstance(connection, props.getDbType());
            dbLoader.load(dataPath, schema);

        } catch (Exception e) {
            throw e;
        }
    }


    private void loadInternalH2(Path dataPath, String schema) throws Exception {
        try {
            h2Database.loadDatabase(dataPath, schema);
        } catch (Exception e) {
            h2Database.cleanAll();
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            protected Void call() throws Exception {
                try {
                    load();
                } catch (Exception e) {
                    h2Database.cleanAll();
                    e.printStackTrace();
                    throw e;
                }
                return null;
            }
        };
    }

}
