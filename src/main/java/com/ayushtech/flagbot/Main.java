package com.ayushtech.flagbot;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayushtech.flagbot.dbconnectivity.DBInfo;
import com.ayushtech.flagbot.listeners.GuildEventListener;
import com.ayushtech.flagbot.listeners.InteractionsListener;
import com.ayushtech.flagbot.listeners.MessageListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

public class Main {

        public static void main(String[] args) throws LoginException, InterruptedException {
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

                JDA jda = JDABuilder.createDefault(bot_token)
                                .addEventListeners(new MessageListener(), new InteractionsListener(),
                                                new GuildEventListener())
                                .setActivity(Activity.playing("/guess"))
                                .build().awaitReady();

                Guild testServer = jda.getGuildById(834364315879342130l);
                testServer.upsertCommand("delete_my_data", "Will delete your user data including your coins").queue();
                // jda.upsertCommand("disable_all_channels", "Disable the bot commands in all channels of the server").queue();
                // jda.upsertCommand("help", "Shows command list").queue();
                // jda.upsertCommand("delete_my_data", "Will delete your user data including your coins").queue();
        }
}