package fr.icodem.db4labs.app.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.app.bat.service.EateryService;
import fr.icodem.db4labs.event.DataImportDoneEvent;
import fr.icodem.db4labs.app.eshop.service.ProductService;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import fr.icodem.db4labs.container.AppContainer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;

public class ApplicationLayoutController {

    @FXML
    Parent root;

    @FXML
    MenuItem menuItemImportFile;

    @FXML
    MenuItem menuItemImportFolder;

    @Inject private AppContainer container;
    @Inject private ProductService productService;
    @Inject private EateryService eateryService;
    @Inject private AppNameProvider appNameProvider;

    @FXML
    public void loadDatabase() {
        container.navigateTo("app/load_database", null);
    }

    @FXML void importXMLFile() {
        // get stage
        Stage stage = (Stage) root.getScene().getWindow();

        // open file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML", "*.xml"));

        File file = fileChooser.showOpenDialog(stage);
        try {
            if (file != null) {

                switch (appNameProvider.getAppName()) {
                    case EShopMedia:
                    case EShopGrocery:
                        productService.importData(file);
                        break;

                    case BookATable:
                        //eateryService.importData(file);
                        break;
                }

                menuItemImportFile.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void importXMLFolder() {
        // get stage
        Stage stage = (Stage) root.getScene().getWindow();

        // open directory chooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select XML Folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = directoryChooser.showDialog(stage);
        try {
            if (file != null) {

                switch (appNameProvider.getAppName()) {
                    case EShopMedia:
                    case EShopGrocery:
                        productService.importData(file);
                        break;

                    case BookATable:
                        //eateryService.importData(file);
                        break;
                }

                menuItemImportFolder.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void importDone(DataImportDoneEvent event) {
        // update table
        Platform.runLater(() -> {
            menuItemImportFile.setDisable(false);
            menuItemImportFolder.setDisable(false);

            // popup message
            String message = "Data import done";
            if (event.getState().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
                message = "Data import failed";
            }
            else if (event.getState().equals(WorkerStateEvent.WORKER_STATE_CANCELLED)) {
                message = "Data import cancelled";
            }

            Popup popup = new Popup();

            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(180);
            messageLabel.setMaxHeight(90);

            Button button = new Button("OK");
            button.setOnAction(e -> popup.hide());

            BorderPane pane = new BorderPane();
            pane.setCenter(messageLabel);
            pane.setBottom(button);

            Rectangle rectangle = new Rectangle(200, 120, Color.ALICEBLUE);
            rectangle.setStroke(Color.GRAY);
            rectangle.setStrokeWidth(2);

            StackPane contentPane = new StackPane(rectangle, pane);
            popup.getContent().add(contentPane);

            BorderPane.setAlignment(button, Pos.CENTER);
            BorderPane.setMargin(button, new Insets(10, 0, 10, 0));

            Stage stage = (Stage) root.getScene().getWindow();
            popup.show(stage,
                    (stage.getWidth() - popup.getWidth()) / 2 + stage.getX(),
                    (stage.getHeight() - popup.getHeight()) / 2 + stage.getY());

        });
    }

}
