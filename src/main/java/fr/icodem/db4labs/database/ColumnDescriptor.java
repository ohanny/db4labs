package fr.icodem.db4labs.database;

public class ColumnDescriptor {
    private String name;
    private DataType type;
    private boolean nullable;
    private boolean primaryKey;
    private boolean identity;
    private short length;
    private short precision;
    private short scale;
    private boolean storeAsFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public short getPrecision() {
        return precision;
    }

    public void setPrecision(short precision) {
        this.precision = precision;
    }

    public short getScale() {
        return scale;
    }

    public void setScale(short scale) {
        this.scale = scale;
    }

    public boolean isStoreAsFile() {
        return storeAsFile;
    }

    public void setStoreAsFile(boolean storeAsFile) {
        this.storeAsFile = storeAsFile;
    }
}
