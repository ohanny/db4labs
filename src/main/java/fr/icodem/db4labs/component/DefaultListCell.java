package fr.icodem.db4labs.component;

import javafx.scene.control.ListCell;
import fr.icodem.db4labs.database.PersistentObject;

public class DefaultListCell extends ListCell<PersistentObject> {

    private String property;
    private String objectName;

    @Override
    protected void updateItem(PersistentObject item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
                setText(format(item));
        } else {
                setText("Make a choice...");
        }
    }

    private String format(PersistentObject item) {
        Object value = null;
        if (objectName != null) {
            value = item.getObject(objectName);
        } else {
            value = item.getProperty(property);
        }
        String str = (value == null)?"":value.toString();

        return str;
    }

    // getters and setters
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}
