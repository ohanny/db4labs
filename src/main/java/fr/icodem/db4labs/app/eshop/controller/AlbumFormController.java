package fr.icodem.db4labs.app.eshop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.util.List;

public class AlbumFormController {

    @FXML private TextField releaseDateAlbumTextField;
    @FXML private TextField lengthAlbumTextField;
    @FXML private TextField artistsTextField;

    @FXML private List<MessageBinder> messageBinders;

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

    public List<MessageBinder> getMessageBinders() {
        return messageBinders;
    }
}
