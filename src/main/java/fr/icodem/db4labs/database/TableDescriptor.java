package fr.icodem.db4labs.database;

import java.util.ArrayList;
import java.util.List;

public class TableDescriptor {
    private String name;
    private List<ColumnDescriptor> columns;
    private List<ConstraintDescriptor> constraints;

    private SqlDescriptor select;
    private SqlDescriptor selectByPK;
    private SqlDescriptor insert;// auto incremented columns are ignored
    private SqlDescriptor insertWithIdentityColumn;// auto incremented columns are included
    private SqlDescriptor update;
    private SqlDescriptor delete;
    private SqlDescriptor count;

    public void addColumn(ColumnDescriptor column) {
        if (columns == null) columns = new ArrayList<>();
        columns.add(column);
    }

    public void addConstraint(ConstraintDescriptor constraint) {
        if (constraints == null) constraints = new ArrayList<>();
        constraints.add(constraint);
    }

    public DataType getType(String columnName) {
        ColumnDescriptor column = getColumn(columnName);
        if (column != null) {
            if (column.getType() == null) {
                throw new IllegalArgumentException("Type is null for column " + columnName + " of table " + name);
            }
            return column.getType();
        }
        throw new IllegalArgumentException("Column does not exist in table " + name + " : " + columnName);
    }

    public ColumnDescriptor getColumn(String columnName) {
        for (ColumnDescriptor column : columns) {
            if (columnName != null && columnName.equals(column.getName())) {
                return column;
            }
        }
        return null;
    }

    public boolean isPrimaryKeyIdentity() {
        return getIdentityColumn() != null;
    }

    public ColumnDescriptor getIdentityColumn() {
        for (ColumnDescriptor column : columns) {
            if (column.isPrimaryKey() && column.isIdentity())  {
                return column;
            }
        }
        return null;
    }

    public String getIdentityColumnName() {
        ColumnDescriptor cd = getIdentityColumn();
        if (cd == null) return null;
        return cd.getName();
    }

    public int getColumnCount() {
        if (columns == null) return 0;
        else return columns.size();
    }

    @Override
    public String toString() {
        return "TableDescriptor{" +
                "name='" + name + '\'' +
                ", select=" + select +
                ", selectByPK=" + selectByPK +
                ", insert=" + insert +
                ", insertWithIdentityColumn=" + insertWithIdentityColumn +
                ", update=" + update +
                ", delete=" + delete +
                ", count=" + count +
                '}';
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnDescriptor> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDescriptor> columns) {
        this.columns = columns;
    }

    public SqlDescriptor getInsert() {
        return insert;
    }

    public void setInsert(SqlDescriptor insert) {
        this.insert = insert;
    }

    public SqlDescriptor getSelect() {
        return select;
    }

    public void setSelect(SqlDescriptor select) {
        this.select = select;
    }

    public SqlDescriptor getSelectByPK() {
        return selectByPK;
    }

    public void setSelectByPK(SqlDescriptor selectByPK) {
        this.selectByPK = selectByPK;
    }

    public SqlDescriptor getInsertWithIdentityColumn() {
        return insertWithIdentityColumn;
    }

    public void setInsertWithIdentityColumn(SqlDescriptor insertWithIdentityColumn) {
        this.insertWithIdentityColumn = insertWithIdentityColumn;
    }

    public SqlDescriptor getUpdate() {
        return update;
    }

    public void setUpdate(SqlDescriptor update) {
        this.update = update;
    }

    public SqlDescriptor getDelete() {
        return delete;
    }

    public void setDelete(SqlDescriptor delete) {
        this.delete = delete;
    }

    public SqlDescriptor getCount() {
        return count;
    }

    public void setCount(SqlDescriptor count) {
        this.count = count;
    }

    public List<ConstraintDescriptor> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintDescriptor> constraints) {
        this.constraints = constraints;
    }
}
