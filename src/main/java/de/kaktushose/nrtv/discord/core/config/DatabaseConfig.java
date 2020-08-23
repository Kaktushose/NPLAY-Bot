package de.kaktushose.nrtv.discord.core.config;

public class DatabaseConfig {

    private String jdbcurl, dbpassword, dbuser;
    private ConfigFile configFile;

    void setConfigFile(ConfigFile configFile) {
        this.configFile = configFile;
    }

    public void saveConfig() {
        configFile.saveConfig();
    }

    public String getJdbcurl() {
        return jdbcurl;
    }

    public void setJdbcurl(String jdbcurl) {
        this.jdbcurl = jdbcurl;
    }

    public String getDbpassword() {
        return dbpassword;
    }

    public void setDbpassword(String dbpassword) {
        this.dbpassword = dbpassword;
    }

    public String getDbuser() {
        return dbuser;
    }

    public void setDbuser(String dbuser) {
        this.dbuser = dbuser;
    }

}
