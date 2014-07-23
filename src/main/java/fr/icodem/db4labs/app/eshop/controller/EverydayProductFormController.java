package fr.icodem.db4labs.app.eshop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.util.List;

public class EverydayProductFormController {

    @FXML private TextField quantityTextField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextArea compositionTextArea;

    @FXML private List<MessageBinder> messageBinders;

    // getters and setters
    public List<MessageBinder> getMessageBinders() {
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
