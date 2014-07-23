package fr.icodem.db4labs.component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// TODO if message is larger than container, then container is hidden !
public class MessageLabel extends Label implements Initializable {

    public static enum State {
        Info("message_info"),
        Warning("message_warning"),
        Error("message_error");

        public final String style;

        private State(String style) {
            this.style = style;
        }
    }

    @FXML private Label label;

    private Control inputControl;
    private String property;

    public MessageLabel() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/component/message_label.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        label.getStyleClass().add("message-label");
    }

    public void setMessage(Message message) {
        if (message == null) {
            clear();
            return;
        }

        label.getStyleClass().removeAll(State.Info.style,
                                        State.Warning.style,
                                        State.Error.style);

        if (inputControl != null) {
            inputControl.getStyleClass().removeAll(State.Info.style,
                                        State.Warning.style,
                                        State.Error.style);
        }

        switch (message.getSeverity()) {
            case Info:
                label.getStyleClass().add(State.Info.style);
                if (inputControl != null) inputControl.getStyleClass().add(State.Info.style);
                break;
            case Warning:
                label.getStyleClass().add(State.Warning.style);
                if (inputControl != null) inputControl.getStyleClass().add(State.Warning.style);
                break;
            case Error:
                label.getStyleClass().add(State.Error.style);
                if (inputControl != null) inputControl.getStyleClass().add(State.Error.style);
                break;
        }

        if (message != null && message.getText() != null) {
            label.setText(message.getText());
        } else {
            label.setText("??? message missing ???");
        }
    }

    private void clear() {
        label.setText("");
        label.getStyleClass().removeAll(State.Info.style,
                State.Warning.style,
                State.Error.style);
        if (inputControl != null) inputControl.getStyleClass().removeAll(State.Info.style,
                                                           State.Warning.style,
                                                           State.Error.style);
    }

    // getters and setters
    public Control getInputControl() {
        return inputControl;
    }

    public void setInputControl(Control inputControl) {
        this.inputControl = inputControl;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
