package fr.icodem.db4labs.app.eshop.controller;

import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EverydayProductFormController {

    @FXML private TextField quantityTextField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextArea compositionTextArea;

    @FXML private MessageBinders messageBinders;

    // getters and setters
    public MessageBinders getMessageBinders() {
        return messageBinders;
    }

    public TextField getQuantityTextField() {
        return quantityTextField;
    }

    public ComboBox<String> getUnitComboBox() {
        return unitComboBox;
    }

    public TextArea getCompositionTextArea() {
        return compositionTextArea;
    }
}
