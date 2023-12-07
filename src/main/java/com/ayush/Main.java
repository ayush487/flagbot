package com.ayush;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayush.dbconnectivity.DBInfo;
import com.ayush.listeners.InteractionsListener;
import com.ayush.listeners.MessageListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main 
{

    // Invite Link -> https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=139586824256&scope=applications.commands%20bot
    
    public static void main( String[] args ) throws LoginException, InterruptedException
    {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("credential.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        final String bot_token = properties.getProperty("BOT_TOKEN");
        final String db_host = properties.getProperty("database_url");
        final String db_username = properties.getProperty("database_username");
        final String db_password = properties.getProperty("database_password");
        DBInfo.setData(db_host, db_username, db_password);
        
        JDA jda = JDABuilder.createDefault(bot_token).addEventListeners(new MessageListener(), new InteractionsListener())
        .setActivity(Activity.playing("/guess"))
        .build().awaitReady();
        
//        jda.upsertCommand("disable", "Disable the bot commands in the following channel")
//        .addOption(OptionType.CHANNEL, "channel", "Enter channel", false).queue();
//        jda.upsertCommand("enable", "Enable the bot commands in the following channel")
//        .addOption(OptionType.CHANNEL, "channel", "Enter channel", false).queue();
        
//        Guild swambot = jda.getGuildById(1127236362530209932l);
        jda.upsertCommand("invite", "Invite Flagbot to your server")
        .queue();
        
//        jda.upsertCommand("guessmap", "Guess which country map is it!").queue();
    }
}