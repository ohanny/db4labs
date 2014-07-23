package fr.icodem.db4labs.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXML;
import fr.icodem.db4labs.app.AppName;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.event.LoadDatabaseEvent;
import fr.icodem.db4labs.event.LoadDatabaseReportEvent;
import fr.icodem.db4labs.event.SaveDatabaseReportEvent;
import fr.icodem.db4labs.dbtools.service.TaskState;

import java.util.UUID;

@Singleton
public class HomeController {

    @Inject private AppContainer container;

    @Inject private AppNameProvider appNameProvider;

    private UUID loadDbUUID;

    @FXML
    protected void loadEShop() {
        appNameProvider.setAppName(AppName.EShop);
        loadApp();
    }

    @FXML
    protected void loadEShopMedia() {
        appNameProvider.setAppName(AppName.EShopMedia);
        loadApp();
    }

    @FXML
    protected void loadEShopGrocery() {
        appNameProvider.setAppName(AppName.EShopGrocery);
        loadApp();
    }

    @FXML
    protected void loadBookATable() {
        appNameProvider.setAppName(AppName.BookATable);
        loadApp();
    }

    private void loadApp() {
        container.navigateTo("task_processing", null);
        LoadDatabaseEvent event = new LoadDatabaseEvent(appNameProvider.getAppName());
        loadDbUUID = event.getUuid();
        container.post(event);
    }

    @Subscribe
    public void loadingComplete(LoadDatabaseReportEvent event) {
        if (!event.getUuid().equals(loadDbUUID) || TaskState.Failed.equals(event.getState())) {
            return;
        }

/*
        if (TaskState.Failed.equals(event.getState())) {
            return;
        }
*/

        // loading ok
        switch (event.getAppName()) {
            case EShop:
            case EShopMedia:
            case EShopGrocery:
                container.navigateTo("app/eshop/eshop", event.getAppName().getConfig().getTitle());
                break;
            case BookATable:
                container.navigateTo("app/bat/book_a_table", event.getAppName().getConfig().getTitle());
                break;
        }
    }

    @Subscribe
    public void saveDone(SaveDatabaseReportEvent event) {
        container.navigateTo("home", null);
    }

}
