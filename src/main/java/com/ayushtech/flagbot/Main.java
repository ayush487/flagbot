package com.ayushtech.flagbot;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayushtech.flagbot.dbconnectivity.DBInfo;
import com.ayushtech.flagbot.listeners.InteractionsListener;
import com.ayushtech.flagbot.listeners.MessageListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main 
{

    // private final static String BOT_TOKEN = "";
    // Invite Link -> https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=139586824256&scope=applications.commands%20bot
    
    public static void main( String[] args ) throws LoginException, InterruptedException
    {
        Properties properties = new Properties(1);
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
        
//        Guild swambot = jda.getGuildById(1127236362530209932l);
//        jda.upsertCommand("guessmap", "Guess which country map is it!").queue();
    }
}