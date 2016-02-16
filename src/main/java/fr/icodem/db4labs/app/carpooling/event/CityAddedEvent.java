package fr.icodem.db4labs.app.carpooling.event;

import fr.icodem.db4labs.database.PersistentObject;

public class CityAddedEvent {
    private PersistentObject city;

    public CityAddedEvent(PersistentObject city) {
        this.city = city;
    }

    public PersistentObject getCity() {
        return city;
    }

}
