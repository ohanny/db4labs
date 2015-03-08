package fr.icodem.db4labs.database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

//@Singleton
public class SqlExecutor {

    //@Inject private ConnectionProvider cnProvider;
    //@Inject private DbDescriptorProvider dbDescriptorProvider;

    private DbDescriptor dbDescriptor;
    private Connection connection;

    public SqlExecutor() {}

    public SqlExecutor(DbDescriptor dbDescriptor, Connection connection) {
        this.dbDescriptor = dbDescriptor;
        this.connection = connection;
    }

    /**
     * Allow to reuse this object with different databases
     * @param dbDescriptor
     * @param connection
     */
    public void setDb(DbDescriptor dbDescriptor, Connection connection) {
        this.dbDescriptor = dbDescriptor;
        this.connection = connection;
    }

    public void commit() throws SQLException {
        //getConnection().commit();
        connection.commit();
    }

    public void rollback() throws SQLException {
        //getConnection().rollback();
        connection.rollback();
    }

    public ObservableList<PersistentObject> select(String tableName) throws SQLException, IOException {
        return select(tableName, null);
    }

    public PersistentObject selectUnique(String tableName, WhereDescriptor where) throws SQLException, IOException {
        List<PersistentObject> list = select(tableName, where);

        if (list.size() == 0) return null;

        if (list.size() > 1) {
            throw new IllegalStateException("select unique returned more than one result : " + tableName + " = " + where);
        }

        return list.get(0);
    }

    public ObservableList<PersistentObject> select(String tableName, WhereDescriptor where) throws SQLException, IOException {
        ObservableList<PersistentObject> result = null;
        TableDescriptor table = getTableDescriptor(tableName);
        SqlDescriptor sql = table.getSelect();
        //Connection cn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sqlString = sql.getSql();
            if (where != null) {
                sqlString += " where " + where.getSql();
            }
            ps = connection.prepareStatement(sqlString);
            ps.setFetchSize(100);

            // inject parameters
            if (where != null) {
                for (int index = 0; index < where.getParameterCount(); index++) {
                    injectParam(ps, index + 1, where.getParameterValue(index),
                                               where.getParameterType(index));
                }
            }

            rs = ps.executeQuery();
            result = FXCollections.observableArrayList();
            while (rs.next()) {
                PersistentObject data = new PersistentObject(tableName);
                for (ColumnDescriptor column : table.getColumns()) {
                    Object value = getValue(rs, column.getName(), column.getType());
                    data.setProperty(column.getName(), value);
                }
                //System.out.println("data => " + data.getProperties());
                result.add(data);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            try { if (rs != null) rs.close();} catch(Exception e) {}
            try { if (ps != null) ps.close();} catch(Exception e) {}
        }

        return result;
    }

    public PersistentObject selectByPK(String tableName, Object... params) throws SQLException, IOException {
        TableDescriptor table = getTableDescriptor(tableName);
        SqlDescriptor sql = table.getSelectByPK();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentObject po = null;
        try {
            ps = connection.prepareStatement(sql.getSql());
            ps.setFetchSize(1);
            for (int index = 0; index < sql.getParameterNames().size(); index++) {
                String columnName = sql.getParameterNames().get(index);
                DataType type = table.getType(columnName);
                injectParam(ps, index + 1, params[index], type);
            }

            rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                if (found) throw new IllegalStateException("select by primary key returned more than one result : " + tableName + " => " + params);

                po = new PersistentObject(tableName);
                for (ColumnDescriptor column : table.getColumns()) {
                    Object value = getValue(rs, column.getName(), column.getType());
                    po.setProperty(column.getName(), value);
                }
                found = true;
            }
            if (!found) return null;
        } catch (SQLException e) {
            throw e;
        } finally {
            try { if (rs != null) rs.close();} catch(Exception e) {}
            try { if (ps != null) ps.close();} catch(Exception e) {}
        }

        return po;
    }

    public int count(String tableName) throws SQLException {
        return count(tableName, null);
    }

    public int count(String tableName, WhereDescriptor where) throws SQLException {
        int result = 0;
        TableDescriptor table = getTableDescriptor(tableName);
        SqlDescriptor sql = table.getCount();
        //Connection cn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sqlString = sql.getSql();
            if (where != null) {
                sqlString += " where " + where.getSql();
            }
            ps = connection.prepareStatement(sqlString);
            ps.setFetchSize(1);

            // inject parameters
            if (where != null) {
                for (int index = 0; index < where.getParameterCount(); index++) {
                    injectParam(ps, index + 1, where.getParameterValue(index),
                            where.getParameterType(index));
                }
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            try { if (rs != null) rs.close();} catch(Exception e) {}
            try { if (ps != null) ps.close();} catch(Exception e) {}
        }

        return result;
    }

    public void insert(PersistentObject data) throws SQLException {
        String tableName = data.getTable();
        Map<String, Object> params = data.getProperties();
        TableDescriptor table = getTableDescriptor(tableName);

        SqlDescriptor sql = table.getInsert();
        ColumnDescriptor identityColumn = table.getIdentityColumn();
        if (identityColumn != null && params.containsKey(identityColumn.getName())) {
            sql = table.getInsertWithIdentityColumn();
        }

        //Connection connection = getConnection();

        try (PreparedStatement st = connection.prepareStatement(sql.getSql());) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                String columnName = param.getKey();
                int index = sql.getPosition(columnName);
                DataType type = table.getType(columnName);
                injectParam(st, index, param.getValue(), type);
            }

            // param not present, inject null
            injectNullParams(table, sql, st, params);

            st.executeUpdate();

            // getProperty identity value
            if (identityColumn != null && !params.containsKey(identityColumn.getName())) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    data.setProperty(identityColumn.getName(), rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void update(PersistentObject data) throws SQLException {
        String tableName = data.getTable();
        Map<String, Object> params = data.getProperties();
        TableDescriptor table = getTableDescriptor(tableName);

        SqlDescriptor sql = table.getUpdate();

        //Connection cn = getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql.getSql());) {
            for (int index = 0; index < sql.getParameterNames().size(); index++) {
                String columnName = sql.getParameterNames().get(index);
                DataType type = table.getType(columnName);
                System.out.println((index+1) + " => " + data.getProperty(columnName));
                injectParam(ps, index + 1, data.getProperty(columnName), type);
            }

            // param not present, inject null
            injectNullParams(table, sql, ps, params);

            ps.executeUpdate();
        } catch (Exception e) {
            throw e;
        }
    }

    public void delete(String table, WhereDescriptor where) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ")
           .append(table).append(" where ")
           .append(where.getSql());

        //Connection cn = getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql.toString());) {
            for (int index = 0; index < where.getParameterCount(); index++) {
                injectParam(ps, index + 1, where.getParameterValue(index),
                        where.getParameterType(index));
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public void delete(PersistentObject data) throws SQLException {
        String tableName = data.getTable();
        Map<String, Object> params = data.getProperties();
        TableDescriptor table = getTableDescriptor(tableName);

        SqlDescriptor sql = table.getDelete();

        //Connection cn = getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql.getSql());) {
            for (int index = 0; index < sql.getParameterNames().size(); index++) {
                String columnName = sql.getParameterNames().get(index);
                DataType type = table.getType(columnName);
                injectParam(ps, index + 1, data.getProperty(columnName), type);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

/*
    private Connection getConnection() {
        return cnProvider.getConnection();
    }
*/

    private TableDescriptor getTableDescriptor(String table) {
        //return dbDescriptorProvider.getDescriptor().getTable(table);
        return dbDescriptor.getTable(table);
    }

    private void injectNullParams(TableDescriptor table, SqlDescriptor sqlDescriptor,
                                  PreparedStatement st, Map<String, ?> params) throws SQLException {
        for (String columnName : sqlDescriptor.getParameterNames()) {
            if (!params.containsKey(columnName)) {
                int index = sqlDescriptor.getPosition(columnName);
                DataType type = table.getType(columnName);
                injectParam(st, index, null, type);
            }
        }
    }

    private void injectParam(PreparedStatement st, int index, Object value, DataType type) throws SQLException {
        switch (type) {
            case VARCHAR:
            case CHARACTER:
                st.setString(index, (String)value);
                //if (value == null) st.setString(index, (String)value);
                //else st.setString(index, ((String)value).trim());
                break;
            case INTEGER:
                if (value == null) st.setNull(index, Types.INTEGER);
                else st.setInt(index, (Integer)value);
                break;
            case NUMERIC:
                if (value == null) st.setNull(index, Types.NUMERIC);
                else st.setDouble(index, (Double)value);
                break;
            case DATE:
                java.sql.Date date = new java.sql.Date(((Date)value).getTime());
                st.setDate(index, date);
                break;
            case TIME:
                Time time = new Time(((Date)value).getTime());
                st.setTime(index, time);
                break;
            case TIMESTAMP:
                Timestamp timestamp = new Timestamp(((Date)value).getTime());
                st.setTimestamp(index, timestamp);
                break;
            case BOOLEAN:
                st.setBoolean(index, (Boolean)value);
                break;
            case BLOB:
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[])value);
                st.setBinaryStream(index, bais);
                break;
        }
    }

    private Object getValue(ResultSet rs, String columnName, DataType type) throws SQLException, IOException {
        Object value;
        switch (type) {
            case VARCHAR:
            case CHARACTER:
                value = rs.getString(columnName);
                break;
            case INTEGER:
                value = rs.getInt(columnName);
                if (rs.wasNull()) value = null;
                break;
            case NUMERIC:
                value = rs.getDouble(columnName);
                if (rs.wasNull()) value = null;
                break;
            case DATE:
                value = rs.getDate(columnName);
                break;
            case TIME:
                value = rs.getTime(columnName);
                break;
            case TIMESTAMP:
                value = rs.getTimestamp(columnName);
                break;
            case BOOLEAN:
                value = rs.getBoolean(columnName);
                if (rs.wasNull()) value = null;
                break;
            case BLOB:
                Blob blob = rs.getBlob(columnName);
                int blobLength = (int) blob.length();
                value = blob.getBytes(1, blobLength);
                blob.free();

                break;
            default:
                value = null;
        }
        return value;
    }


}
