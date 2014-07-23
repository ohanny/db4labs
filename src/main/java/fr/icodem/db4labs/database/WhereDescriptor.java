package fr.icodem.db4labs.database;

import java.util.ArrayList;
import java.util.List;

public class WhereDescriptor {

    private class Parameter {
        private Object value;
        private DataType type;

        private Parameter(Object value, DataType type) {
            this.value = value;
            this.type = type;
        }

        private Object getValue() {
            return value;
        }

        private DataType getType() {
            return type;
        }
    }

    private String sql;

    private List<Parameter> parameters;

    // constructor
    public WhereDescriptor(String sql) {
        this.sql = sql;
    }

    // builder method
    public static WhereDescriptor build(String where) {
        WhereDescriptor descriptor = new WhereDescriptor(where);
        return descriptor;
    }

    // instance method
    public WhereDescriptor addParameter(Object value, DataType type) {
        if (parameters == null) parameters = new ArrayList<>();
        parameters.add(new Parameter(value, type));
        return this;
    }

    public int getParameterCount() {
        if (parameters == null) return 0;
        return parameters.size();
    }

    public Object getParameterValue(int index) {
        if (parameters != null) {
            return parameters.get(index).getValue();
        }
        return null;
    }

    public DataType getParameterType(int index) {
        if (parameters != null) {
            return parameters.get(index).getType();
        }
        return null;
    }

    // getters and setters
    public String getSql() {
        return sql;
    }

}
