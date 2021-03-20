package xyz.n7mn.dev.lib;

import xyz.n7mn.dev.SettingData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NanamiPerm {

    private final SettingData data;

    public NanamiPerm(SettingData d){
        this.data = d;
    }

    public List<PermData> getList(){
        List<PermData> list = new ArrayList<>();

        try {

            Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT * FROM `RoleRankList` ORDER BY `Rank` DESC ");
            ResultSet set = statement.executeQuery();

            while (set.next()){
                list.add(new PermData(UUID.fromString(set.getString("UUID")), set.getString("DiscordRoleID"), set.getString("Name"), set.getInt("Rank")));
            }

            set.close();
            statement.close();
            con.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public PermData getData(UUID uuid){

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT * FROM `RoleRankList` WHERE UUID = ?");
            statement.setString(1, uuid.toString());
            ResultSet set = statement.executeQuery();

            PermData data = new PermData(UUID.fromString(set.getString("UUID")), set.getString("DiscordRoleID"), set.getString("Name"), set.getInt("Rank"));
            set.close();
            statement.close();
            con.close();

            return data;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public PermData getData(String discordRoleID){

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT * FROM `RoleRankList` WHERE DiscordRoleID = ?");
            statement.setString(1, discordRoleID);
            ResultSet set = statement.executeQuery();

            PermData data = null;
            if (set.next()){
                data = new PermData(UUID.fromString(set.getString("UUID")), set.getString("DiscordRoleID"), set.getString("Name"), set.getInt("Rank"));
            }


            set.close();
            statement.close();
            con.close();

            return data;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
