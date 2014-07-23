package fr.icodem.db4labs.database;

import java.util.ArrayList;
import java.util.List;

public class MySqlSqlGenerator extends SqlGenerator {

    private DataTypeMapper typeMapper = DataTypeMapper.getInstance(DbType.MySql);

    @Override
    public SqlDescriptor generateCreateTable(TableDescriptor table) {
        StringBuilder sql = new StringBuilder();
        List<ColumnDescriptor> pks = new ArrayList<>();

        sql.append("create table \u0060")
                .append(table.getName())
                .append("\u0060 (");
        for (ColumnDescriptor column : table.getColumns()) {
            sql.append("\r\n\t")
                    .append(column.getName())
                    .append(" ")
                    .append(typeMapper.getSqlStringType(column));
            if (!column.isNullable()) sql.append(" not null");
            if (column.isIdentity()) sql.append(" auto_increment");
            sql.append(",");

            if (column.isPrimaryKey()) pks.add(column);
        }

        // add primary key
        if (pks.size() > 0) {
            sql.append("\r\n\tprimary key (");
            for (ColumnDescriptor pk : pks) {
                sql.append(pk.getName())
                        .append(",");
            }
            deleteLastComma(sql);
            sql.append(")");
        }
        deleteLastComma(sql);
        sql.append("\r\n) engine=InnoDB");


        System.out.println(sql);

        SqlDescriptor sqlDescriptor = new SqlDescriptor();
        sqlDescriptor.setSql(sql.toString());

        return sqlDescriptor;
    }

}
