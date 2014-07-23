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

public class BookFormController implements Initializable {

    @FXML private TextField releaseDateBookTextField;
    @FXML private TextField isbnTextField;
    @FXML private TextField languageTextField;
    @FXML private TextField pagesTextField;
    @FXML private TextField authorsTextField;

    @FXML private MessageBinders messageBinders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("release_date").to(releaseDateBookTextField)
                .bind("isbn").to(isbnTextField)
                .bind("language").to(languageTextField)
                .bind("pages").to(pagesTextField)
                .bind("authors").to(authorsTextField)
                .build();
    }

    // getters and setters
    public TextField getReleaseDateBookTextField() {
        return releaseDateBookTextField;
    }

    public TextField getIsbnTextField() {
        return isbnTextField;
    }

    public TextField getLanguageTextField() {
        return languageTextField;
    }

    public TextField getPagesTextField() {
        return pagesTextField;
    }

    public MessageBinders getMessageBinders() {
        return messageBinders;
    }

    public TextField getAuthorsTextField() {
        return authorsTextField;
    }
}
