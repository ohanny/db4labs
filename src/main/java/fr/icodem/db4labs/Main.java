package fr.icodem.db4labs;

import javafx.application.Application;
import javafx.stage.Stage;
import fr.icodem.db4labs.container.AppContainer;
import fr.icodem.db4labs.dbtools.navigation.StageHandler;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // configure primary stage
        primaryStage.setTitle("Database for Labs");
        primaryStage.setResizable(false);

        // start application container
        AppContainer container = AppContainer.getInstance();
        container.start();
        container.getInstance(StageHandler.class).setPrimaryStage(primaryStage);

        // show home page
        container.navigateTo("home", null);

    }

    @Override
    public void stop() throws Exception {
        AppContainer container = AppContainer.getInstance();
        container.stop();

        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}