package fr.icodem.db4labs.app.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import fr.icodem.db4labs.container.AppContainer;

public class ApplicationLayoutController {

    @Inject private AppContainer container;

    @FXML
    public void loadDatabase() {
        container.navigateTo("app/load_database", null);
    }
}
