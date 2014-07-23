package fr.icodem.db4labs.database;

import fr.icodem.db4labs.app.AppName;

import java.util.ArrayList;
import java.util.List;

public class DbDescriptor {
    private AppName appName;
    private String dbName;
    private String description;
    private List<TableDescriptor> tables;

    public void addTable(TableDescriptor table) {
        if (tables == null) tables = new ArrayList<>();
        tables.add(table);
    }

    public TableDescriptor getTable(String name) {
        for (TableDescriptor td : tables) {
            if (name != null && name.equals(td.getName())) {
                return td;
            }
        }
        return null;
    }

    // getters and setters
    public AppName getAppName() {
        return appName;
    }

    public void setAppName(AppName appName) {
        this.appName = appName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TableDescriptor> getTables() {
        return tables;
    }

    public void setTables(List<TableDescriptor> tables) {
        this.tables = tables;
    }
}
