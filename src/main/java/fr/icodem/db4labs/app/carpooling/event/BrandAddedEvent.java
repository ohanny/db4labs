package fr.icodem.db4labs.app.carpooling.event;

import fr.icodem.db4labs.database.PersistentObject;

public class BrandAddedEvent {
    private PersistentObject brand;

    public BrandAddedEvent(PersistentObject brand) {
        this.brand = brand;
    }

    public PersistentObject getBrand() {
        return brand;
    }

}
