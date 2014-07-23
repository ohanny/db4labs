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

public class MovieFormController implements Initializable {

    @FXML private TextField releaseDateMovieTextField;
    @FXML private TextField lengthMovieTextField;
    @FXML private TextField languagesMovieTextField;
    @FXML private TextField directorTextField;
    @FXML private TextField actorsTextField;

    @FXML private MessageBinders messageBinders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("release_date").to(releaseDateMovieTextField)
                .bind("length").to(lengthMovieTextField)
                .bind("languages").to(languagesMovieTextField)
                .bind("director").to(directorTextField)
                .bind("actors").to(actorsTextField)
                .build();
    }

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

    public MessageBinders getMessageBinders() {
        return messageBinders;
    }
}
