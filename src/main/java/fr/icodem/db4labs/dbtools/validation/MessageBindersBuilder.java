package fr.icodem.db4labs.dbtools.validation;

import javafx.scene.control.Control;

public class MessageBindersBuilder {

    private MessageBinders binders;
    private MessageBinder currentBinder;

    public MessageBindersBuilder bind(String property) {
        currentBinder = new MessageBinder();
        currentBinder.setProperty(property);

        if (binders == null) {
            binders = new MessageBinders();
        }
        binders.add(currentBinder);

        return this;
    }

    public MessageBindersBuilder to(Control target) {
        currentBinder.setTarget(target);
        return this;
    }

    public MessageBinders build() {
        return binders;
    }
}
