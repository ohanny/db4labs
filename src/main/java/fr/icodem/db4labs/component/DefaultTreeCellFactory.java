package fr.icodem.db4labs.component;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import fr.icodem.db4labs.database.PersistentObject;

public class DefaultTreeCellFactory
        implements Callback<TreeView<PersistentObject>, TreeCell<PersistentObject>> {

    private String property;

    @Override
    public TreeCell<PersistentObject> call(TreeView<PersistentObject> listView) {
        DefaultTreeCell cell = new DefaultTreeCell();
        cell.setProperty(property);
        return cell;
    }


    // getters and setters
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
