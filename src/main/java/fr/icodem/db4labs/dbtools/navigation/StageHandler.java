package fr.icodem.db4labs.dbtools.navigation;

import com.google.inject.Inject;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.database.DbDescriptorProvider;
import fr.icodem.db4labs.event.SaveDatabaseEvent;

public class StageHandler {
    private Stage primaryStage;
    private Stage appStage;

    private Stage currentStage;

    @Inject
    private AppContainer container;

    @Inject
    private DbDescriptorProvider dbDescriptorProvider;

    public Stage getStage(String page, String title) {
        // application stage
        if (page.startsWith("app/")) {
            if (appStage == null) {
                appStage = new Stage();
                appStage.setTitle(title);//TODO ajouter titre
                appStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        container.navigateTo("task_processing", null);
                        container.post(new SaveDatabaseEvent(dbDescriptorProvider.getDescriptor()));
                    }
                });
            }
            return appStage;
        }

        // home stage
        return primaryStage;
    }

    public void show(Stage stage) {
        if (stage == currentStage) return;// already visible

        // show primary stage
        if (stage == primaryStage) {
            if (appStage != null) {
                appStage.close();
                appStage = null;
            }
            primaryStage.show();
            currentStage = primaryStage;
        }

        // show app stage
        if (stage == appStage) {
            primaryStage.hide();
            appStage.show();
            currentStage = appStage;
        }

    }

    // getters and setters
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            private int count;
            @Override
            public void handle(WindowEvent event) {
                count++;
                //if (count < 2) event.consume();
            }
        });

    }
}
