package fr.icodem.db4labs.controller;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import fr.icodem.db4labs.event.LoadDatabaseEvent;
import fr.icodem.db4labs.event.LoadDatabaseReportEvent;
import fr.icodem.db4labs.event.SaveDatabaseEvent;
import fr.icodem.db4labs.dbtools.service.TaskState;

public class TaskProcessingController {

    @FXML
    private Label messageLabel;

    @FXML
    private ProgressIndicator indicator;

    @Subscribe
    public void loadDatabaseInProcess(LoadDatabaseEvent event) {
        messageLabel.setText("Please wait, loading schemas...");
    }

    @Subscribe
    public void saveDatabaseInProcess(SaveDatabaseEvent event) {
        messageLabel.setText("Please wait, saving schemas...");
    }

    @Subscribe
    public void loadDatabaseReport(LoadDatabaseReportEvent event) {
        if (TaskState.Failed.equals(event.getState())) {
            messageLabel.setText("Loading schemas failed !");
            indicator.setVisible(false);
        }
    }

}
