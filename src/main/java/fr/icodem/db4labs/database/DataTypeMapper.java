package fr.icodem.db4labs.database;

public abstract class DataTypeMapper {

    // factory
    public static DataTypeMapper getInstance(DbType type) {
        switch (type) {
            case H2:
                return new H2DataTypeMapper();

            case MySql:
                return new MySqlDataTypeMapper();

            default:
                return null;
        }
    }

    //
    public String getSqlStringType(ColumnDescriptor column) {
        StringBuilder sb = new StringBuilder();

        if (column.getType() == null) {
            throw new IllegalArgumentException("Type cannot be null : "  + column);
        }

        switch (column.getType()) {
            case VARCHAR:
                sb.append("varchar");
                if (column.getLength() > 0) {
                    sb.append("(").append(column.getLength()).append(")");
                }
                break;
            case CHARACTER:
                if (column.getLength() < 1) {
                    throw new IllegalArgumentException("Length with 'character' type is required : " + column.getName());
                }
                sb.append("char(")
                  .append(column.getLength())
                  .append(")");
                break;
            case INTEGER:
                sb.append("int");
                break;
            case NUMERIC:
                if (column.getPrecision() < 1 || column.getScale() < 1) {
                    throw new IllegalArgumentException("Precision and scale are required for 'numeric' type : " + column.getName());
                }
                sb.append("numeric(")
                  .append(column.getPrecision())
                  .append(",").append(column.getScale())
                  .append(")");
                break;
            case DATE:
                sb.append("date");
                break;
            case TIME:
                sb.append("time");
                break;
            case TIMESTAMP:
                sb.append("timestamp");
                break;
            case BOOLEAN:
                sb.append("boolean");
                break;
            case BLOB:
                sb.append("blob");
                break;
            default:
                throw new IllegalArgumentException("Type '" + column.getType() + "' is not supported : " + column.getName());
        }

        return sb.toString();
    }
}
