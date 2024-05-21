package com.ayushtech.flagbot;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayushtech.flagbot.dbconnectivity.DBInfo;
import com.ayushtech.flagbot.listeners.GuildEventListener;
import com.ayushtech.flagbot.listeners.InteractionsListener;
import com.ayushtech.flagbot.listeners.MessageListener;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.stocks.StocksHandler;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

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
                StocksHandler.loadInitialPriceMap();

                DefaultShardManagerBuilder builder =
                DefaultShardManagerBuilder.createDefault(bot_token);
                builder.setShardsTotal(2);
                builder.setActivity(Activity.playing("/guess"));
                builder.addEventListeners(new MessageListener(), new InteractionsListener(),
                new GuildEventListener());
                builder.build();

                ChannelService.getInstance().loadDisabledChannels();
        }
}