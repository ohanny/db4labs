package fr.icodem.db4labs.app.eshop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.util.List;

public class MovieFormController {

    @FXML private TextField releaseDateMovieTextField;
    @FXML private TextField lengthMovieTextField;
    @FXML private TextField languagesMovieTextField;
    @FXML private TextField directorTextField;
    @FXML private TextField actorsTextField;

    @FXML private List<MessageBinder> messageBinders;

    // getters and setters
    public TextField getReleaseDateMovieTextField() {
        return releaseDateMovieTextField;
    }

    public TextField getLengthMovieTextField() {
        return lengthMovieTextField;
    }

    public TextField getLanguagesMovieTextField() {
        return languagesMovieTextField;
    }

    public TextField getDirectorTextField() {
        return directorTextField;
    }

    public TextField getActorsTextField() {
        return actorsTextField;
    }

    public List<MessageBinder> getMessageBinders() {
        return messageBinders;
    }
}
