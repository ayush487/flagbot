package com.ayushtech.flagbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import com.ayushtech.flagbot.dbconnectivity.DBInfo;
import com.ayushtech.flagbot.fileConnectivity.CSVFileReader;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.listeners.InteractionsListener;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {

        public static void main(String[] args) throws LoginException, InterruptedException {
                Properties properties = new Properties();
                try (FileInputStream inputStream = new FileInputStream("credential.properties")) {
                        properties.load(inputStream);
                } catch (IOException e) {
                        e.printStackTrace();
                }

                final String bot_token = properties.getProperty("BOT_TOKEN");
                final String db_host = properties.getProperty("database_url");
                final String db_username = properties.getProperty("database_username");
                final String db_password = properties.getProperty("database_password");
                final String voterWebhookUrl = properties.getProperty("vote_reward_logs");
                final String joinUpdateWebhookUrl = properties.getProperty("private_updates");
                final String patreonWebhookUrl = properties.getProperty("patreon_logs");
                final String wordAdderWebhook = properties.getProperty("WORD_ADDER_WEBHOOK");
                final String wordRemoverWebhook = properties.getProperty("WORD_REMOVER_WEBHOOK");

                CSVFileReader.getInstance();

                DBInfo.setData(db_host, db_username, db_password);
                InteractionsListener.setJoinUpdateWebhookUrl(joinUpdateWebhookUrl);
                PatreonService.getInstance().setPatreonWebhookUrl(patreonWebhookUrl);
                VotingService.setVotingWebhookUrl(voterWebhookUrl);
                UtilService.getInstance().setWordAdderWebhookUrl(wordAdderWebhook);
                UtilService.getInstance().setWordRemovedWebhookUrl(wordRemoverWebhook);
                LanguageService.getInstance();
                GuessGameUtil.getInstance();
                VotingService.getInstance();

                DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(bot_token,
                                EnumSet.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                                                GatewayIntent.DIRECT_MESSAGES));
                builder.setActivity(Activity.playing("/crossduel"));
                builder.disableCache(EnumSet.of(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
                                CacheFlag.SCHEDULED_EVENTS));
                builder.addEventListeners(new InteractionsListener());
                builder.build();

                ChannelService.getInstance().loadDisabledChannels();

                @SuppressWarnings("resource")
                ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
                scheduler.scheduleAtFixedRate(() -> {
                        PatreonService.getInstance().updatePatronUsers();
                        VotingService.getInstance().renewVoterData();
                        System.gc();
                }, 10, 10, TimeUnit.MINUTES);

        }
}