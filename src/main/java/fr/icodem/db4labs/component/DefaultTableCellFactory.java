package fr.icodem.db4labs.component;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import fr.icodem.db4labs.database.PersistentObject;

import java.util.Map;

public class DefaultTableCellFactory
        implements Callback<TableColumn<PersistentObject, Map>, TableCell<PersistentObject, Map>> {

    private String property;

    @Override
    public TableCell<PersistentObject, Map> call(TableColumn<PersistentObject, Map> persistentDataMapTableColumn) {
        return new TableCell<PersistentObject, Map>() {
            @Override
            protected void updateItem(Map item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    setText(format(item));
                } else {
                    setText(null);
                }
            }
        };
    }

    private String format(Map item) {
        Object value = item.get(property);

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
