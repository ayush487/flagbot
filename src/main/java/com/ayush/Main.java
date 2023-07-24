package com.ayush;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayush.listeners.InteractionsListener;
import com.ayush.listeners.MessageListener;

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
        
        JDABuilder.createDefault(bot_token).addEventListeners(new MessageListener(), new InteractionsListener())
        .setActivity(Activity.playing("/guess"))
        .build().awaitReady();
        
        
    }
}