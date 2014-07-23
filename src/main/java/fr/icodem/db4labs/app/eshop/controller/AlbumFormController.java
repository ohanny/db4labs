package fr.icodem.db4labs.app.eshop.controller;

import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import fr.icodem.db4labs.dbtools.validation.MessageBindersBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AlbumFormController implements Initializable {

    @FXML private TextField releaseDateAlbumTextField;
    @FXML private TextField lengthAlbumTextField;
    @FXML private TextField artistsTextField;

    @FXML private MessageBinders messageBinders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("release_date").to(releaseDateAlbumTextField)
                .bind("length").to(lengthAlbumTextField)
                .bind("artists").to(artistsTextField)
                .build();
    }

    // getters and setters
    public TextField getReleaseDateAlbumTextField() {
        return releaseDateAlbumTextField;
    }

    public TextField getLengthAlbumTextField() {
        return lengthAlbumTextField;
    }

    public TextField getArtistsTextField() {
        return artistsTextField;
    }

    public MessageBinders getMessageBinders() {
        return messageBinders;
    }
}
