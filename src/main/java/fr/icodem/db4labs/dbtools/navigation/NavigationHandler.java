package fr.icodem.db4labs.dbtools.navigation;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import fr.icodem.db4labs.container.ControllerFactory;
import fr.icodem.db4labs.event.NavigationEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationHandler {

    @Inject
    private StageHandler stageHandler;

    @Inject
    private ControllerFactory controllerFactory;

    private enum PageType {Home, Application}

    private Deque<String> history = new ArrayDeque<>();

    @Subscribe
    public void navigate(NavigationEvent event) {

        if (event.isBack()) {
            if (history.size() > 1) {
                history.pop();
                navigate(history.peek(), null);
            }
        } else {
            if (history.size() == 1000) history.removeLast();// history max size
            history.push(event.getPage());
            navigate(event.getPage(), event.getTitle());
        }

    }

    //@Subscribe TODO delete
    private void navigate(String page, String title) {
        Stage stage = stageHandler.getStage(page, title);

        PageType pageType =(page.startsWith("app/"))?PageType.Application:PageType.Home;

        try {
            Scene scene;
            if (stage.getScene() == null) {
                // build node from fxml
                String fxml = "/fxml/" + page + ".fxml";
                Parent targetNode = FXMLLoader.load(getClass().getResource(fxml), null,
                        new JavaFXBuilderFactory(), controllerFactory);

                // set page descriptor
                PageDescriptor descriptor = new PageDescriptor(page);
                targetNode.setUserData(descriptor);

                switch (pageType) {
                    case Home:
                        // a new scene is created when a page is displayed
                        scene = new Scene(targetNode);
                        break;

                    // for application pages, we don't change the scene
                    // each page is added to the layout
                    case Application:
                        Parent layout = FXMLLoader.load(getClass().getResource("/fxml/app/app_layout.fxml"), null,
                                new JavaFXBuilderFactory(), controllerFactory);

                        // add page to layout
                        StackPane stackPane = (StackPane)layout.lookup("#stackPane");
                        stackPane.getChildren().add(targetNode);
                        scene = new Scene(layout);

                        // set stylesheet
                        scene.getStylesheets().add("styles/app.css");
                        break;
                    default:
                        return;
                }
                stage.setScene(scene);
            }
            else {
                switch (pageType) {
                    case Home:
                        // build node from fxml
                        String fxml = "/fxml/" + page + ".fxml";
                        Parent targetNode = FXMLLoader.load(getClass().getResource(fxml), null,
                                new JavaFXBuilderFactory(), controllerFactory);

                        // set page descriptor
                        PageDescriptor descriptor = new PageDescriptor(page);
                        targetNode.setUserData(descriptor);

                        double width = stage.getScene().getWidth();
                        double height = stage.getScene().getHeight();
                        scene = new Scene(targetNode, width, height);
                        //Platform.setImplicitExit(false);
                        stage.setScene(scene);
                        break;

                    case Application:
                        Node layout = stage.getScene().getRoot();
                        StackPane stackPane = (StackPane)layout.lookup("#stackPane");

                        boolean found = false;
                        for (Node child : stackPane.getChildren()) {
                            descriptor = (PageDescriptor) child.getUserData();
                            if (descriptor != null) {
                                if (page.equals(descriptor.getPage())) {
                                    child.setVisible(true);
                                    found = true;
                                    continue;
                                }
                                child.setVisible(false);
                            }
                        }
                        if (!found) {
                            // build node from fxml
                            fxml = "/fxml/" + page + ".fxml";
                            targetNode = FXMLLoader.load(getClass().getResource(fxml), null,
                                    new JavaFXBuilderFactory(), controllerFactory);

                            // set page descriptor
                            descriptor = new PageDescriptor(page);
                            targetNode.setUserData(descriptor);

                            // add page to layout
                            stackPane.getChildren().add(targetNode);
                        }
                        break;
                }
            }

            stageHandler.show(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PageDescriptor {
        private String page;

        private PageDescriptor(String page) {
            this.page = page;
        }

        private String getPage() {
            return page;
        }

        private void setPage(String page) {
            this.page = page;
        }
    }

}
