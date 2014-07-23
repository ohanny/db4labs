package fr.icodem.db4labs.component;

import javafx.scene.control.TreeCell;
import fr.icodem.db4labs.database.PersistentObject;

public class DefaultTreeCell extends TreeCell<PersistentObject> {

    private String property;

    @Override
    protected void updateItem(PersistentObject item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            if (item.getObject("treeview.root") != null) {// root rendering
                setText((String)item.getObject("treeview.root"));
            } else {
                setText(format(item));
            }
        } else {
                setText("");
        }
    }

    private String format(PersistentObject item) {
        Object value = item.getProperty(property);
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
}
