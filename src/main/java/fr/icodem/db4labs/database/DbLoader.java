package fr.icodem.db4labs.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public abstract class DbLoader {

    protected Connection connection;
    protected DbType dbType;
    protected SqlGenerator sqlGenerator;
    protected DbDescriptor descriptor;
    protected SqlExecutor sqlExecutor;

    public static DbLoader getInstance(Connection connection, DbType dbType) {
        DbLoader loader;
        switch (dbType) {
            case H2:
                loader = new H2DbLoader();
                break;
            case MySql:
                loader = new MySqlDbLoader();
                break;
            default:
                return null;
        }

        loader.connection = connection;
        loader.dbType = dbType;
        loader.sqlGenerator = SqlGenerator.getInstance(dbType);

        return loader;
    }

    public DbDescriptor load(Path dataPath, String schema) throws Exception {
        // build descriptor
        buildDescriptor(schema);
        sqlExecutor = new SqlExecutor(descriptor, connection);

        // create database and tables
        createDatabase();
        createTables();

        // import data into database
        if (Files.exists(dataPath)) {
            insertData(dataPath);
        }

        // create constraints
        createConstraints();

        return descriptor;
    }

    private void buildDescriptor(String schema) throws Exception {
        SchemaParser schemaParser = new SchemaParser();

        // main descriptor
        descriptor = schemaParser.parse(schema);

        for (TableDescriptor table : descriptor.getTables()) {
            sqlGenerator.generate(table);
        }
    }

    /**
     * Specifoc to each database type
     * @throws Exception
     */
    protected abstract void createDatabase() throws Exception;

    private DbDescriptor createTables() throws Exception {
        for (TableDescriptor table : descriptor.getTables()) {

            // create table in schema
            SqlDescriptor createTable = sqlGenerator.generateCreateTable(table);
            try (Statement st = connection.createStatement();) {
                st.execute(createTable.getSql());
            }
        }

        return descriptor;
    }

    private void createConstraints() throws Exception {
        for (TableDescriptor table : descriptor.getTables()) {
            if (table.getConstraints() != null) {
                for (ConstraintDescriptor constraint : table.getConstraints()) {
                    SqlDescriptor createConstraint = sqlGenerator.generateCreateConstraint(table, constraint);
                    try (Statement st = connection.createStatement();) {
                        st.execute(createConstraint.getSql());
                    }
                }
            }
        }
    }

    private void insertData(Path dataPath) throws Exception {
        DataParser dataParser = new DataParser(sqlExecutor);
        dataParser.parse(dataPath, descriptor);
    }

}
