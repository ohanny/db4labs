package fr.icodem.db4labs.database;

public enum DataType {

    VARCHAR("varchar"), CHARACTER("character"),
    INTEGER("integer"), NUMERIC("numeric"),
    DATE("date"), TIME("time"), TIMESTAMP("timestamp"),
    BOOLEAN("boolean"),
    BLOB("blob");

    private final String simpleName;

    private DataType(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public static DataType getValue(String simpleName) {
        for (DataType t : DataType.values()) {
            if (simpleName.equals(t.getSimpleName())) return t;
        }
        return null;
    }
}
