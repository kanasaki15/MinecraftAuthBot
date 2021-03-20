package xyz.n7mn.dev;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.lib.NanamiPerm;
import xyz.n7mn.dev.lib.PermData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DiscordEventListener extends ListenerAdapter {

    private final SettingData data;
    public DiscordEventListener(SettingData d){
        this.data = d;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getMessage().getContentRaw().toLowerCase().startsWith("#.")){
            command(event.getMessage(), event.getAuthor());
            return;
        }

        if (event.getAuthor().isBot()){
            return;
        }

        new Thread(()->{

            if (!event.getMessage().getTextChannel().getId().equals(data.getDiscordReceivedTextChannel())){
                return;
            }

            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url("https://mc-oauth.net/api/api?token?token").addHeader("token", event.getMessage().getContentRaw()).build();
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                Gson gson = new Gson();
                AuthResult result = gson.fromJson(json, AuthResult.class);


                if (result.getStatus().equals("success")){
                    event.getMessage().delete().queue();
                } else {
                    event.getMessage().reply("6桁の数字が無効か正しく入力されてないです。。。\nエラー内容：\n```\n"+result.getMessage()+"\n```").queue();
                    return;
                }

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

                List<Role> roles = event.getMember().getRoles();
                List<PermData> list = new NanamiPerm(data).getList();

                UUID permID = null;

                for (Role role : roles){
                    for (PermData perm : list){
                        if (perm.getDiscordRoleID().equals(role.getId())){
                            permID = perm.getUUID();
                        }
                    }
                }

                if (!updateMode){
                    statement = con.prepareStatement("INSERT INTO `MinecraftUserList` (`ID`, `MinecraftUUID`, `DiscordUserID`, `RoleUUID`, `JoinDate`) VALUES (?, ?, ?, ?, ?) ");
                    statement.setString(1, UUID.randomUUID().toString());
                    statement.setString(2, result.getUuid().toString());
                    statement.setString(3, event.getAuthor().getId());
                    if (permID != null){
                        statement.setString(4, permID.toString());
                    } else {
                        statement.setString(4, new NanamiPerm(data).getData(data.getDiscordAddRoleID()).getDiscordRoleID());
                    }
                    statement.setDate(5, new java.sql.Date(0L));

                } else {
                    statement = con.prepareStatement("UPDATE MinecraftUserList SET DiscordUserID = ? WHERE MinecraftUUID = ?");
                    statement.setString(1, event.getAuthor().getId());
                    statement.setString(2, result.getUuid().toString());
                }

                statement.execute();
                statement.close();
                con.close();

                if (permID == null){
                    Member member = event.getMessage().getMember();
                    JDA jda = member.getJDA();
                    Guild guiid = jda.getGuildById(member.getGuild().getId());
                    List<Role> roleList = guiid.getRoles();
                    for (Role role : roleList){
                        if (role.getId().equals(data.getDiscordAddRoleID())){
                            guiid.addRoleToMember(event.getAuthor().getId(), role).queue();
                            return;
                        }
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            }

        }).start();

    }

    private void command(Message message, User author){

        if (author.isBot()){
            return;
        }

        if (message.isWebhookMessage()){
            return;
        }

        new Thread(()->{

        }).start();

    }
}
