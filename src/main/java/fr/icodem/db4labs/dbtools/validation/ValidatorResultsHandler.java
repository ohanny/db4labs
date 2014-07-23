package fr.icodem.db4labs.dbtools.validation;

import javafx.scene.Node;
import fr.icodem.db4labs.component.Message;
import fr.icodem.db4labs.component.MessageLabel;
import fr.icodem.db4labs.database.PersistentObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidatorResultsHandler {

    public void clearValidationMessages(Node form, MessageBinders messageBinders) {
        clearMessages(form, messageBinders);
    }

    public boolean handle(Validator validator, Node form,
                          MessageBinders messageBinders,
                          PersistentObject... dataList) {
        // clear messages
        clearMessages(form, messageBinders);

        // perform validation
        ValidatorResults results = validator.validate(dataList);

        // validation errors
        if (!results.isValid()) {
            // convert validator results into error messages
            List<Message> messages = convert(results);

            // display validation messages on ui
            displayMessages(messages, form, messageBinders);

            // validation failed
            return false;
        }

        // validation ok
        return true;
    }


    private List<Message> convert(ValidatorResults results) {
        List<Message> messages = new ArrayList<>();
        for (ValidatorResult result : results.getResults()) {
            Message m = new Message(result.getMessage(), result.getProperty());
            switch (result.getState()) {
                case Error:
                    m.setSeverity(Message.Severity.Error);
                    break;
                case Warning:
                    m.setSeverity(Message.Severity.Warning);
                    break;
            }
            messages.add(m);
        }
        return messages;
    }

    private void displayMessages(List<Message> messages, Node form, MessageBinders messageBinders) {
        // TODO remove MessageLabel ?
        Set<Node> labels = form.lookupAll("MessageLabel");
        for (Node node : labels) {
            MessageLabel label = (MessageLabel)node;
            if (label.getProperty() != null) {
                Message m = getMessage(label.getProperty(), messages);
                label.setMessage(m);
            }
        }

        if (messageBinders == null) return;

        for (MessageBinder mb : messageBinders) {
            if (mb.getProperty() != null) {
                Message m = getMessage(mb.getProperty(), messages);
                mb.setMessage(m);
            }
        }
    }

    private void clearMessages(Node form, MessageBinders messageBinders) {
        if (messageBinders != null) {
            for (MessageBinder mb : messageBinders) {
                if (mb.getProperty() != null) {
                    mb.setMessage(null);
                }
            }
        }

    }

    private Message getMessage(String property, List<Message> messages) {
        for (Message message : messages) {
            if (property.equals(message.getTarget())) {
                return message;
            }
        }
        return null;
    }
}
