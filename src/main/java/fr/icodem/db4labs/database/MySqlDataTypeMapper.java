package fr.icodem.db4labs.database;

public class MySqlDataTypeMapper extends DataTypeMapper {
    public String getSqlStringType(ColumnDescriptor column) {
        StringBuilder sb = new StringBuilder();

        switch (column.getType()) {
            case VARCHAR:
                sb.append("varchar");
                if (column.getLength() > 0) {
                    sb.append("(").append(column.getLength()).append(")");
                } else {
                    sb.append("(1000)");
                }
                break;
            case INTEGER:
                sb.append("integer");
                break;
            case BLOB:
                sb.append("mediumblob");
                break;
            default:
                return super.getSqlStringType(column);
        }

        return sb.toString();
    }

}
