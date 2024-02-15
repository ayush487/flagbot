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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

                // DefaultShardManagerBuilder builder =
                // DefaultShardManagerBuilder.createDefault(bot_token);
                // builder.setActivity(Activity.playing("/battle"));
                // ShardManager manager = builder.build();
                // manager.addEventListener(new MessageListener(), new InteractionsListener(),
                // new GuildEventListener());
                JDA jda = JDABuilder.createDefault(bot_token)
                                .addEventListeners(new MessageListener(), new InteractionsListener(),
                                                new GuildEventListener())
                                .setActivity(Activity.playing("/battle"))
                                .build().awaitReady();
                                

                SubcommandData stockListCommand = new SubcommandData("list", "List the stocks with the current prices");
                SubcommandData stockbuyCommand = new SubcommandData("buy", "Buy stocks")
                        .addOption(OptionType.STRING, "company", "Enter company name", true, true)
                        .addOption(OptionType.INTEGER, "amount", "Enter amount of shares you want to buy", true);
                SubcommandData stockSellCommand = new SubcommandData("sell", "Sell stocks")
                        .addOption(OptionType.STRING, "company", "Enter company name", true, true)
                        .addOption(OptionType.INTEGER, "amount", "Enter amount of shares you want to buy", true);
                SubcommandData stocksViewCommand = new SubcommandData("owned", "View your owned stocks");

                jda.upsertCommand("stocks", "Stock Related Commands")
                        .addSubcommands(stockListCommand, stockbuyCommand, stockSellCommand, stocksViewCommand)
                        .queue();
                        
                
                ChannelService.getInstance().loadDisabledChannels();
        }
}