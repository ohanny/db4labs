package fr.icodem.db4labs.app.carpooling.event;

import fr.icodem.db4labs.database.PersistentObject;

public class ModelAddedEvent {
    private PersistentObject model;

    public ModelAddedEvent(PersistentObject model) {
        this.model = model;
    }

    public PersistentObject getModel() {
        return model;
    }

}
