package xyz.n7mn.dev;

public class SettingData {

    private String DiscordToken = "";
    private String DiscordReceivedTextChannel = "";
    private String DiscordAddRoleID = "";

    private String MySQLServer = "";
    private int MySQlPort = 3306;
    private String MySQLDatabase = "";
    private String MySQLOption = "?allowPublicKeyRetrieval=true&useSSL=false";
    private String MySQLUsername = "";
    private String MySQLPassword = "";

    public SettingData(){

    }

    public SettingData(String discordToken, String discordReceivedTextChannel, String discordAddRoleID, String mySQLServer, int mySQlPort, String mySQLDatabase, String mySQLOption, String mySQLUsername, String mySQLPassword){

        this.DiscordToken = discordToken;
        this.DiscordReceivedTextChannel = discordReceivedTextChannel;
        this.DiscordAddRoleID = discordAddRoleID;

        this.MySQLServer = mySQLServer;
        this.MySQlPort = mySQlPort;
        this.MySQLDatabase = mySQLDatabase;
        this.MySQLOption = mySQLOption;
        this.MySQLUsername = mySQLUsername;
        this.MySQLPassword = mySQLPassword;

    }

    public String getDiscordToken() {
        return DiscordToken;
    }

    public String getDiscordReceivedTextChannel() {
        return DiscordReceivedTextChannel;
    }

    public String getDiscordAddRoleID(){
        return DiscordAddRoleID;
    }

    public String getMySQLServer() {
        return MySQLServer;
    }

    public int getMySQlPort() {
        return MySQlPort;
    }

    public String getMySQLDatabase() {
        return MySQLDatabase;
    }

    public String getMySQLOption() {
        return MySQLOption;
    }

    public String getMySQLUsername() {
        return MySQLUsername;
    }

    public String getMySQLPassword() {
        return MySQLPassword;
    }
}
