package fr.icodem.db4labs.database;

import java.util.ArrayList;
import java.util.List;

public class ConstraintDescriptor {
    private String name;
    private String type;
    private List<String> columns;
    private String tableRef;
    private List<String> columnsRef;

    public void addColumn(String column) {
        if (columns == null) columns = new ArrayList<>();
        columns.add(column);
    }

    public void addColumnRef(String columnRef) {
        if (columnsRef == null) columnsRef = new ArrayList<>();
        columnsRef.add(columnRef);
    }

    @Override
    public String toString() {
        return "ConstraintDescriptor{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", columns=" + columns +
                ", tableRef='" + tableRef + '\'' +
                ", columnsRef=" + columnsRef +
                '}';
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableRef() {
        return tableRef;
    }

    public void setTableRef(String tableRef) {
        this.tableRef = tableRef;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumnsRef() {
        return columnsRef;
    }

    public void setColumnsRef(List<String> columnsRef) {
        this.columnsRef = columnsRef;
    }
}
