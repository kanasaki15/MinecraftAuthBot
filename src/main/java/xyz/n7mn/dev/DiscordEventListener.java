package xyz.n7mn.dev;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;

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

        new Thread(()->{

            try {

                Connection con = DriverManager.getConnection("");
                con.setAutoCommit(true);




                con.close();

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



    }
}
