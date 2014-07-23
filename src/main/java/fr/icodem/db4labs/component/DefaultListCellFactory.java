package fr.icodem.db4labs.component;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import fr.icodem.db4labs.database.PersistentObject;

public class DefaultListCellFactory
        implements Callback<ListView<PersistentObject>, ListCell<PersistentObject>> {

    private String property;
    private String objectName;

    @Override
    public ListCell<PersistentObject> call(ListView<PersistentObject> listView) {
        DefaultListCell cell = new DefaultListCell();
        cell.setProperty(property);
        cell.setObjectName(objectName);
        return cell;
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
