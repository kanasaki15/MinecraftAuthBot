package xyz.n7mn.dev.lib;

import xyz.n7mn.dev.SettingData;

import java.util.UUID;

public class PermData {

    private UUID UUID;
    private String DiscordRoleID;
    private String Name;
    private int Rank;

    public PermData(UUID uuid, String discordRoleID, String name, int rank){
        this.UUID = uuid;
        this.DiscordRoleID = discordRoleID;
        this.Name = name;
        this.Rank = rank;
    }

    public UUID getUUID() {
        return UUID;
    }

    public String getDiscordRoleID() {
        return DiscordRoleID;
    }

    public String getName() {
        return Name;
    }

    public int getRank() {
        return Rank;
    }
}
