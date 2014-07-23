package fr.icodem.db4labs.app.eshop.controller;

import fr.icodem.db4labs.dbtools.validation.MessageBinders;
import fr.icodem.db4labs.dbtools.validation.MessageBindersBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import fr.icodem.db4labs.dbtools.validation.MessageBinder;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GroceryProductFormController implements Initializable {

    @FXML private TextField quantityTextField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextArea compositionTextArea;
    @FXML private TextArea nutritionalValueTextArea;

    @FXML private MessageBinders messageBinders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // message binders
        messageBinders = new MessageBindersBuilder()
                .bind("quantity").to(quantityTextField)
                .bind("unit").to(unitComboBox)
                .bind("composition").to(compositionTextArea)
                .bind("nutritionalValue").to(nutritionalValueTextArea)
                .build();
    }

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

    public TextArea getNutritionalValueTextArea() {
        return nutritionalValueTextArea;
    }
}
