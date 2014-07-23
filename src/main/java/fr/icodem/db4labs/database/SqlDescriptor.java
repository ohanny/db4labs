package fr.icodem.db4labs.database;

import java.util.ArrayList;
import java.util.List;

public class SqlDescriptor {
    private String sql;
    private List<String> parameterNames;

    public void addParameterName(String name) {
        if (parameterNames == null) parameterNames = new ArrayList<>();
        parameterNames.add(name);
    }

    public int getPosition(String parameterName) {
        return parameterNames.indexOf(parameterName) + 1;
    }

    public int getParamCount() {
        if (parameterNames == null) return 0;
        return parameterNames.size();
    }

    // getters and setters
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }
}
