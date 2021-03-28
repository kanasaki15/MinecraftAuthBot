package xyz.n7mn.dev;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.lib.NanamiPerm;
import xyz.n7mn.dev.lib.PermData;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

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

            } catch (Exception e){
                e.printStackTrace();
            }

        }).start();

    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<PermData> roleList = new NanamiPerm(data).getList();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                new Thread(()->{
                    try {
                        Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
                        con.setAutoCommit(true);

                        JDA jda = event.getJDA();
                        Guild guild = jda.getGuildById("810725404545515561");
                        List<Member> members = guild.getMembers();

                        for (Member member: members){
                            UUID roleId = null;
                            List<Role> memberRoleList = member.getRoles();
                            for (PermData data : roleList){
                                for (Role memberRole : memberRoleList){
                                    if (data.getDiscordRoleID().equals(memberRole.getId())){
                                        roleId = data.getUUID();

                                        break;
                                    }
                                }

                                if (roleId != null){
                                    break;
                                }
                            }

                            if (roleId != null){
                                try {
                                    PreparedStatement statement = con.prepareStatement("UPDATE MinecraftUserList SET RoleUUID = ? WHERE DiscordUserID = ?");
                                    statement.setString(1, roleId.toString());
                                    statement.setString(2, member.getId());
                                    statement.execute();
                                    statement.close();
                                } catch (Exception e){
                                    e.printStackTrace();
                                    con.close();
                                    return;
                                }
                            }
                        }

                        con.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        };

        timer.scheduleAtFixedRate(task, 0L, 60000L);

    }

    private void command(Message message, User author){

        if (author.isBot()){
            return;
        }

        if (message.isWebhookMessage()){
            return;
        }

        new Thread(()->{

            String msg = message.getContentRaw();

            if (msg.toLowerCase().startsWith("#.check")){

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("問い合わせ結果");

                if (!msg.toLowerCase().equals("#.check")){
                    builder.setColor(Color.RED);

                    builder.setTitle("えらー");
                    builder.setDescription("`#.check`でお願いします。");

                    message.reply(builder.build()).queue();
                    return;
                }

                List<String> mineUUID = new ArrayList<>();
                List<String> mineUser = new ArrayList<>();
                String checkUser = "";

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + data.getMySQLServer() + ":" + data.getMySQlPort() + "/" + data.getMySQLDatabase() + data.getMySQLOption(), data.getMySQLUsername(), data.getMySQLPassword());
                    con.setAutoCommit(true);

                    PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList WHERE DiscordUserID = ?");
                    statement.setString(1, message.getAuthor().getId());
                    ResultSet set = statement.executeQuery();

                    while (set.next()){
                        mineUUID.add(set.getString("MinecraftUUID"));
                    }

                    set.close();
                    statement.close();
                    con.close();

                } catch (Exception e){
                    e.printStackTrace();
                }

                if (mineUUID.size() == 0){
                    builder.setDescription(author.getAsTag() + "さんは連携をしていないようです。");
                    message.reply(builder.build()).queue();
                    return;
                }

                checkUser = author.getAsTag();

                OkHttpClient client = new OkHttpClient();
                for (String str : mineUUID){
                    String url = "https://api.mojang.com/user/profiles/" + str.replaceAll("-","") + "/names";
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        String json = response.body().string();
                        UUID2MCIDResult[] result = new Gson().fromJson(json, UUID2MCIDResult[].class);
                        mineUser.add(result[result.length - 1].getName());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                StringBuffer sb = new StringBuffer();

                for (String name : mineUser){
                    sb.append("`");
                    sb.append(name);
                    sb.append("`");
                    sb.append("\n");
                }

                builder.setDescription(checkUser + "さんは以下のMinecraftIDと連携しています。\n\n" + sb.toString());
                message.reply(builder.build()).queue();
            }

        }).start();

    }
}
