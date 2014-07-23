package fr.icodem.db4labs.dbtools.validation;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import fr.icodem.db4labs.component.Message;

public class MessageBinder {
    public static enum State {
        Info("message_info"),
        Warning("message_warning"),
        Error("message_error");

        public final String style;

        private State(String style) {
            this.style = style;
        }
    }

    private String property;
    private Control target;

    public void setMessage(Message message) {
        if (message == null) {
            clear();
            return;
        }

        if (target != null) {
            target.getStyleClass().removeAll(State.Info.style,
                                              State.Warning.style,
                                              State.Error.style);
            Tooltip tooltip = new Tooltip();
            tooltip.setText(message.getText());
            target.setTooltip(tooltip);

            switch (message.getSeverity()) {
                case Info:
                    target.getStyleClass().add(State.Info.style);
                    tooltip.getStyleClass().add(State.Info.style);
                    break;
                case Warning:
                    target.getStyleClass().add(State.Warning.style);
                    tooltip.getStyleClass().add(State.Warning.style);
                    break;
                case Error:
                    target.getStyleClass().add(State.Error.style);
                    tooltip.getStyleClass().add(State.Error.style);
                    break;
            }
        }



    }

    private void clear() {
        if (target != null) {
            target.getStyleClass().removeAll(State.Info.style,
                    State.Warning.style,
                    State.Error.style);
            target.setTooltip(null);
/*
            if (target.getTooltip() != null) {
                target.getTooltip().setText("removed");
            }
*/
        }
    }

    // getters et setters
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = (Control)target;
    }
}
