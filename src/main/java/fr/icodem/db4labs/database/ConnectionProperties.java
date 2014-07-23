package fr.icodem.db4labs.database;

public class ConnectionProperties {
    private DbType dbType;
    private String url;
    private String driver;
    private String username;
    private String password;

    public ConnectionProperties(DbType dbType, String url, String driver, String username, String password) {
        this.dbType = dbType;
        this.url = url;
        this.driver = driver;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "ConnectionProperties{" +
                "dbType='" + dbType + '\'' +
                ", url='" + url + '\'' +
                ", driver='" + driver + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    // getters and setters
    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public DbType getDbType() {
        return dbType;
    }

}
