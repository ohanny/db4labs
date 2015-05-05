package fr.icodem.db4labs.app.bat.event;

import fr.icodem.db4labs.database.PersistentObject;

public class EateryAddedEvent {
    private PersistentObject eatery;

    public EateryAddedEvent(PersistentObject eatery) {
        this.eatery = eatery;
    }

    public PersistentObject getEatery() {
        return eatery;
    }

}
