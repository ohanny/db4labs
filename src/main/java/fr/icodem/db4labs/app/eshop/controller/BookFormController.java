package fr.icodem.db4labs.app.eshop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.util.List;

public class BookFormController {

    @FXML private TextField releaseDateBookTextField;
    @FXML private TextField isbnTextField;
    @FXML private TextField languageTextField;
    @FXML private TextField pagesTextField;
    @FXML private TextField authorsTextField;

    @FXML private List<MessageBinder> messageBinders;

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

    public List<MessageBinder> getMessageBinders() {
        return messageBinders;
    }

    public TextField getAuthorsTextField() {
        return authorsTextField;
    }
}
