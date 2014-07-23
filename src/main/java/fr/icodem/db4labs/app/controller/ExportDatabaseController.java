package fr.icodem.db4labs.app.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.ConnectionProperties;
import fr.icodem.db4labs.database.DbType;
import fr.icodem.db4labs.event.LoadDatabaseEvent;
import fr.icodem.db4labs.event.LoadDatabaseReportEvent;
import fr.icodem.db4labs.dbtools.service.TaskState;

import java.util.UUID;

public class ExportDatabaseController {

    @Inject
    private AppContainer container;

    @Inject
    private AppNameProvider appNameProvider;

    @FXML
    private ProgressIndicator indicator;

    private UUID loadDbUUID;

    @FXML
    public void loadDatabase() {
        String url = "jdbc:mysql://localhost:3306";
        String driver = "com.mysql.jdbc.Driver";
        String username = "root";
        String password = "olivier";

        ConnectionProperties targetProps = new ConnectionProperties(DbType.MySql, url, driver, username, password);

        LoadDatabaseEvent event = new LoadDatabaseEvent(appNameProvider.getAppName(), targetProps);
        loadDbUUID = event.getUuid();
        container.post(event);
        indicator.setVisible(true);
        System.out.println("ExportDatabaseController.exportToDatabase");
    }

    @FXML
    public void goBack() {
        container.navigateBack();
    }

    @Subscribe
    public void loadComplete(LoadDatabaseReportEvent event) {
        if (!event.getUuid().equals(loadDbUUID) || TaskState.Failed.equals(event.getState())) {
            return;
        }

        System.out.println("exportComplete");
        indicator.setVisible(false);
    }

}
