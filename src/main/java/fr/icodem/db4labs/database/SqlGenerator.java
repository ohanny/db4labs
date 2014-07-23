package fr.icodem.db4labs.database;

//@Singleton
public abstract class SqlGenerator {

    public static SqlGenerator getInstance(DbType type) {
        switch (type) {
            case H2:
                return new H2SqlGenerator();
            case MySql:
                return new MySqlSqlGenerator();
            default:
                return null;
        }
    }

    protected void deleteLastComma(StringBuilder sb) {
        int end = sb.length() - 1;
        if (sb.charAt(end) == ',') {
            sb.deleteCharAt(end);
        }
    }

    public abstract SqlDescriptor generateCreateTable(TableDescriptor table);

    public SqlDescriptor generateCreateConstraint(TableDescriptor table, ConstraintDescriptor constraint) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("alter table \u0060")
           .append(table.getName())
           .append("\u0060 add constraint ")
           .append(constraint.getName());

        switch (constraint.getType()) {
            case "foreign":
                sql.append(" foreign key (");
                for (String column : constraint.getColumns()) {
                    sql.append(column).append(",");
                }
                deleteLastComma(sql);
                sql.append(") references \u0060")
                   .append(constraint.getTableRef())
                   .append("\u0060 (");
                for (String columnRef : constraint.getColumnsRef()) {
                    sql.append(columnRef).append(",");
                }
                deleteLastComma(sql);
                sql.append(")");
                break;

            case "unique":
                sql.append(" unique (");
                for (String column : constraint.getColumns()) {
                    sql.append(column).append(",");
                }
                deleteLastComma(sql);
                sql.append(")");
                break;
            default:
                throw new IllegalArgumentException("Constraint type is invalid : " + constraint);
        }

        descriptor.setSql(sql.toString());

        System.out.println("SqlGenerator.generateCreateConstraint() => " + sql.toString());

        return descriptor;
    }

    public void generate(TableDescriptor table) {
        generateSelect(table);
        generateSelectByPK(table);
        generateInsert(table, false);
        if (table.isPrimaryKeyIdentity()) {
            generateInsert(table, true);// used for data import in H2 db
        }
        generateUpdate(table);
        generateDelete(table);
        generateCount(table);
    }

    private void generateSelect(TableDescriptor table) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("select ");

        for (ColumnDescriptor column : table.getColumns()) {
            sql.append(column.getName())
                    .append(",");
        }
        deleteLastComma(sql);

        sql.append(" from \u0060")
           .append(table.getName())
           .append("\u0060");

        descriptor.setSql(sql.toString());

        table.setSelect(descriptor);

    }

    @Deprecated
    private void generateSelectByPK(TableDescriptor table) {
        SqlDescriptor descriptor = new SqlDescriptor();
        StringBuilder sql = new StringBuilder();
        sql.append(table.getSelect().getSql());
        addWhereByPK(descriptor, sql, table);

        descriptor.setSql(sql.toString());
        System.out.println(sql);

        table.setSelectByPK(descriptor);

    }

    private void generateInsert(TableDescriptor table, boolean includeIdentityColumn) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("insert into \u0060")
           .append(table.getName())
           .append("\u0060 (");

        for (ColumnDescriptor column : table.getColumns()) {
            if (!includeIdentityColumn && column.isIdentity()) continue;
            sql.append(column.getName())
               .append(",");

            descriptor.addParameterName(column.getName());
        }
        deleteLastComma(sql);

        sql.append(") values (");
        for (ColumnDescriptor column : table.getColumns()) {
            if (!includeIdentityColumn && column.isIdentity()) continue;
            sql.append("?,");
        }
        deleteLastComma(sql);
        sql.append(")");

        descriptor.setSql(sql.toString());

        System.out.println("SqlGenerator => " + sql);

        if (includeIdentityColumn) table.setInsertWithIdentityColumn(descriptor);
        else table.setInsert(descriptor);
    }

    private void generateUpdate(TableDescriptor table) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("update \u0060")
                .append(table.getName())
                .append("\u0060 set ");

        for (ColumnDescriptor column : table.getColumns()) {
            if (column.isIdentity()) continue;
            sql.append(column.getName())
                    .append("=?,");

            descriptor.addParameterName(column.getName());
        }
        deleteLastComma(sql);

        addWhereByPK(descriptor, sql, table);

        descriptor.setSql(sql.toString());

        System.out.println("SqlGenerator => " + sql);

        table.setUpdate(descriptor);
    }

    private void generateDelete(TableDescriptor table) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("delete from \u0060")
           .append(table.getName())
           .append("\u0060");

        addWhereByPK(descriptor, sql, table);

        descriptor.setSql(sql.toString());

        System.out.println("SqlGenerator => " + sql);

        table.setDelete(descriptor);
    }

    private void generateCount(TableDescriptor table) {
        SqlDescriptor descriptor = new SqlDescriptor();

        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from \u0060")
           .append(table.getName())
           .append("\u0060");

        descriptor.setSql(sql.toString());

        System.out.println("SqlGenerator => " + sql);

        table.setCount(descriptor);
    }

    private void addWhereByPK(SqlDescriptor descriptor, StringBuilder sql, TableDescriptor table) {
        sql.append(" where ");
        for (ColumnDescriptor column : table.getColumns()) {
            if (!column.isPrimaryKey()) continue;
            if (sql.charAt(sql.length() - 1) == '?') sql.append(" and ");
            sql.append(column.getName()).append("=?");
            descriptor.addParameterName(column.getName());
        }
        deleteLastComma(sql);
    }

}
