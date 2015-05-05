package fr.icodem.db4labs.app.controller;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import fr.icodem.db4labs.app.AppNameProvider;
import fr.icodem.db4labs.app.bat.service.EateryService;
import fr.icodem.db4labs.database.ConnectionProvider;
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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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

    /* TODO remove temporary code
    @Inject private ConnectionProvider provider;

    @FXML
    public void fix() {
        Map<String, Integer> mapPages = readXML();
        process(mapPages);
    }

    private Map<String, Integer> readXML() {
        File dir = new File("/Users/Olivier/Desktop/eshop-export");

        Map<String, Integer> mapPages = new HashMap<>();

        Stream.of(dir.listFiles())
                .filter(f -> f.getName().endsWith(".xml"))
                .forEach(f -> {
                    try (InputStream is = new FileInputStream(f)) {
                        // base properties
                        String type = null;
                        String name = null;
                        int pages = 0;

                        // parse XML content
                        XMLInputFactory factory = XMLInputFactory.newInstance();

                        XMLStreamReader reader = factory.createXMLStreamReader(is);

                        String currentElement = null;
                        while (reader.hasNext()) {
                            boolean typeOK = false;
                            boolean nameOK = false;
                            boolean pagesOK = false;
                            int eventType = reader.next();
                            switch (eventType) {
                                case XMLEvent.CDATA:
                                    //case XMLEvent.SPACE:
                                case XMLEvent.CHARACTERS:
                                    switch (currentElement) {
                                        case "type":
                                            type = reader.getText();
                                            typeOK = true;
                                            break;

                                        case "name":
                                            name = reader.getText();
                                            nameOK = true;
                                            break;

                                        case "pages":
                                            try {
                                                pages = Integer.parseInt(reader.getText());
                                            } catch (Exception e) {}
                                            pagesOK = true;
                                            break;
                                    }
                                    break;

                                case XMLEvent.START_ELEMENT:
                                    currentElement = reader.getLocalName();
                                    break;
                            }

                            if (typeOK && pagesOK && nameOK) break;
                        }


                        if (pages > 0 && name != null && "book".equals(type)) {
                            System.out.printf("%-150s%d\r\n", name, pages);
                            mapPages.put(name, pages);
                        }

                    } catch (Exception e) {}
                });

        return mapPages;

    }

    public void process(Map<String, Integer> mapPages) {

        int processed = 0;
        int skipped = 0;

        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            Connection connection = provider.getConnection();

            String select = "select p.id, p.name, b.pages from Product as p " +
                    "inner join Book b on p.id = b.id " +
                    "where b.pages = 0";

            ps = connection.prepareStatement(select);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                int pages = rs.getInt(3);

                if (mapPages.get(name) != null) {
                    processed++;
                } else {
                    skipped++;
                }
                System.out.printf("%-6d%-150s%d\r\n", id, name, pages);

                if (mapPages.get(name) != null) {
                    pages = mapPages.get(name);
                    String update = "update book set pages = ? where id = ?";
                    ps1 = connection.prepareStatement(update);
                    ps1.setInt(1, pages);
                    ps1.setInt(2, id);

                    int found = ps1.executeUpdate();
                    connection.commit();

                    if (ps1 != null) try {
                        ps1.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }


            }

            System.out.println("Processed : " + processed + ", skipped : " + skipped);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (ps != null) try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    */

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
                        eateryService.importData(file);
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
                        eateryService.importData(file);
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
