package xyz.n7mn.dev;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.sql.Connection;
import java.util.UUID;

public class DiscordEventListener extends ListenerAdapter {

    private final SettingData data;
    public DiscordEventListener(SettingData d){
        this.data = d;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (!event.getTextChannel().getId().equals(data.getDiscordReceivedTextChannel())){
            return;
        }

        if (event.getAuthor().isBot()){
            return;
        }

        Message message = event.getMessage();
        String text = event.getMessage().getContentRaw();
        String DiscordUserID = event.getAuthor().getId();


        if (text.length() != 6){
            return;
        }

        Thread thread = new Thread(() -> {

            String json = null;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("https://mc-oauth.net/api/api?token?token").addHeader("token", text).build();
            try {
                Response response = client.newCall(request).execute();
                json = response.body().string();
            } catch (Exception e){
                e.printStackTrace();
            }

            Gson gson = new Gson();
            AuthResult result = gson.fromJson(json, AuthResult.class);

            // {"status":"success","message":"Token has been invalidated","uuid":"c6f2f6d35a7d45cda16a52e4d4e2ceb2","username":"7mi_chan"}
            if (result.getStatus().equals("success")){
                message.delete().queue();
            } else {
                message.reply("6桁の数字が無効か正しく入力されてないです。。。\nエラー内容：\n```\n"+result.getMessage()+"\n```").queue();
                return;
            }

            try {
                boolean updateMode = false;
                Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
                PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList WHERE MinecraftUUID = ?");
                statement.setString(1, result.getUuid().toString());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    updateMode = true;
                }
                set.close();
                statement.close();

                if (!updateMode){
                    statement = con.prepareStatement("INSERT INTO `MinecraftUserList` (`ID`, `MinecraftUUID`, `DiscordUserID`, `Role`, `JoinDate`) VALUES (?, ?, ?, ?, ?) ");
                    statement.setString(1, UUID.randomUUID().toString());
                    statement.setString(2, result.getUuid().toString());
                    statement.setString(3, DiscordUserID);
                    statement.setString(4, "Authenticated");
                    statement.setDate(5, new Date(0L));
                } else {
                    statement = con.prepareStatement("UPDATE MinecraftUserList SET DiscordUserID = ? WHERE MinecraftUUID = ?");
                    statement.setString(1, DiscordUserID);
                    statement.setString(2, result.getUuid().toString());
                }

                statement.execute();
                statement.close();

                Member member = message.getMember();
                JDA jda = member.getJDA();
                Guild guiid = jda.getGuildById(member.getGuild().getId());
                guiid.addRoleToMember(DiscordUserID, guiid.getRoleById(data.getDiscordAddRoleID())).queue();

                con.close();
            } catch (Exception e){
                e.printStackTrace();
            }

        });

        thread.start();
    }
}
