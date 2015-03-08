package fr.icodem.db4labs.app.eshop.event;

import fr.icodem.db4labs.database.PersistentObject;

public class ProductAddedEvent {
    private PersistentObject product;

    public ProductAddedEvent(PersistentObject product) {
        this.product = product;
    }

    public PersistentObject getProduct() {
        return product;
    }

}
