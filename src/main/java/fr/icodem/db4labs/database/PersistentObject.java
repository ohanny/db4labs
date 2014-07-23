package fr.icodem.db4labs.database;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Map;

public class PersistentObject {
    private String table;

    // properties : use during persistence operations (sql, json)
    private ObservableMap<String, Object> properties = FXCollections.observableHashMap();
    private MapProperty<String, Object> propertiesProperty = new SimpleMapProperty<>();

    // used to cache objects for application logic (objects, values...),
    // not used during persistence operations (sql, json)
    private ObservableMap<String, Object> objects;

    public PersistentObject() {
        propertiesProperty.setValue(properties);
    }

    public PersistentObject(String table) {
        this();
        this.table = table;
    }

    public void merge(PersistentObject source) {
        merge(source, false);
    }

    public void merge(PersistentObject source, boolean ignoreObjects) {
        // properties
        for (Map.Entry<String, Object> entry : source.getProperties().entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        // objects
        if (ignoreObjects) return;
        if (source.objects != null) {
            if (objects == null) {
                objects = FXCollections.observableHashMap();
            }
            for (Map.Entry<String, Object> entry : source.objects.entrySet()) {
                objects.put(entry.getKey(), entry.getValue());
            }
        } else objects = null;
    }

    public void clearContent() {
        if (properties != null) properties.clear();
        if (objects != null) objects.clear();
    }

    public PersistentObject setProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public PersistentObject setObject(String name, Object value) {
        if (objects == null) {
            objects = FXCollections.observableHashMap();
        }
        objects.put(name, value);
        return this;
    }

    public Object getObject(String name) {
        if (objects == null) return null;
        return objects.get(name);
    }

    public void moveObject(String oldName, String newName) {
        if (objects != null && objects.get(oldName) != null) {
            objects.put(newName, getObject(oldName));
            objects.remove(oldName);
        }
    }

    public PersistentObject clone() {
        PersistentObject clone = new PersistentObject(table);
        clone.properties = FXCollections.observableHashMap();
        clone.properties.putAll(properties);
        if (objects != null) {
            clone.objects = FXCollections.observableHashMap();
            clone.objects.putAll(objects);
        }
        return clone;
    }

/*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistentObject that = (PersistentObject) o;

        if (!table.equals(that.table)) return false;

        // check pk

        return true;
    }

*/
/*   todo hascode persistent object ?
    @Override
    public int hashCode() {
        return table.hashCode();
    }
*/

    // getters and setters
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public MapProperty<String, Object> propertiesProperty() {
        return propertiesProperty;
    }

    public Map<String, Object> getObjects() {
        return objects;
    }

    @Override
    public String toString() {
        return "PersistentObject{" +
                "properties=" + properties +
                ", table='" + table + '\'' +
                '}';
    }
}
