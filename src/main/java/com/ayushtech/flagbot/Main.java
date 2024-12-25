package com.ayushtech.flagbot;

import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import com.ayushtech.flagbot.dbconnectivity.DBInfo;
import com.ayushtech.flagbot.fileConnectivity.CountryNameFileReader;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.listeners.GuildEventListener;
import com.ayushtech.flagbot.listeners.InteractionsListener;
import com.ayushtech.flagbot.listeners.MessageListener;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.stocks.StocksHandler;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

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

                final int adminThreshold = Integer.parseInt(properties.getProperty("adminThreshold"));
                final int modThreshold = Integer.parseInt(properties.getProperty("modThreshold"));
                final int staffThreshold = Integer.parseInt(properties.getProperty("staffThreshold"));
                final int totalVotes = Integer.parseInt(properties.getProperty("totalVotes"));

                CountryNameFileReader.getInstance();

                DBInfo.setData(db_host, db_username, db_password);
                PrivateServerService.getInstance().setThreshold(adminThreshold, modThreshold, staffThreshold,
                                totalVotes);
                StocksHandler.loadInitialPriceMap();
                PatreonService.getInstance();
                LanguageService.getInstance();
                GuessGameUtil.getInstance();

                DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(bot_token,
                                GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.DIRECT_MESSAGES);
                builder.setActivity(Activity.playing("/atlas"));
                builder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
                                CacheFlag.SCHEDULED_EVENTS);
                builder.addEventListeners(new MessageListener(), new InteractionsListener(),
                                new GuildEventListener());
                builder.build();

                ChannelService.getInstance().loadDisabledChannels();
        }
}