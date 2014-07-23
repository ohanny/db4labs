package fr.icodem.db4labs.database;

import java.sql.Statement;

public class MySqlDbLoader extends DbLoader {

    @Override
    protected void createDatabase() throws Exception {
        // drop database
        String drop = "drop database if exists " + descriptor.getDbName();
        try (Statement st = connection.createStatement();) {
            st.execute(drop);
        }

        // create database
        String create = "create database " + descriptor.getDbName() +
                " default character set utf8 collate utf8_general_ci";
        try (Statement st = connection.createStatement();) {
            st.execute(create);
        }

        // use new database
        String use = "use " + descriptor.getDbName();
        try (Statement st = connection.createStatement();) {
            st.execute(use);
        }

        // grant priviliges // todo use user descriptor
        String grant = "grant all privileges on " + descriptor.getDbName() +
                ".* to scott@127.0.0.1 identified by 'tiger'";
        try (Statement st = connection.createStatement();) {
            st.execute(grant);
        }

    }


}
