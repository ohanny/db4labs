package fr.icodem.db4labs.component;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class HomeMenu extends Pane {
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView itemImage;

    private String image;
    private EventHandler selectEventHandler;

    public HomeMenu() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/component/home_menu.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    protected void selectItem() {
        if (selectEventHandler !=null){
            selectEventHandler.handle(null);
        }
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public String getDescription() {
        return descriptionLabel.getText();
    }

    public void setDescription(String description) {
        descriptionLabel.setText(description);
    }

    public void setOnSelectEvent(EventHandler eventHandler){
        selectEventHandler = eventHandler;
    }
    public EventHandler getOnSelectEvent(){
        return selectEventHandler;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String url) {
        this.image = url;
        itemImage.setImage(new Image(url));
    }

}
