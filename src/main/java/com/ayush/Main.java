package com.ayush;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayush.listeners.MessageListener;
import com.ayush.listeners.SlashCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main 
{

    // private final static String BOT_TOKEN = "";
    
    public static void main( String[] args ) throws LoginException, InterruptedException
    {
        Properties properties = new Properties(1);
        try {
            properties.load(new FileInputStream("credential.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String bot_token = properties.getProperty("BOT_TOKEN");
        JDA jda = JDABuilder.createDefault(bot_token).addEventListeners(new MessageListener(), new SlashCommandListener()).build().awaitReady();
        
        jda.upsertCommand("guess", "Guess the country name by its flag").queue();;
    }
}