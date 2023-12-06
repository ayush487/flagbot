package com.ayushtech.flagbot.listeners;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionsListener extends ListenerAdapter {

    ScheduledExecutorService gameEndService;

    public InteractionsListener() {
        super();
        gameEndService = new ScheduledThreadPoolExecutor(4);
    }

    
	@Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("guess")) {
        	
            boolean isAdded = FlagGameHandler.getInstance().addGame(event);
            if (isAdded) {
                gameEndService.schedule(new FlagGameEndRunnable(
                        FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
                        event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
            }
        }
        else if(event.getName().equals("leaderboards")) {
        	event.deferReply().queue();
        	JDA jda = event.getJDA();
        	String temp = LeaderboardHandler.getInstance().getLeaderboard(jda);
        	String leaderboard = temp!=null ? temp : "Something went wrong!";
			event.getHook().sendMessage(leaderboard).queue();
        }
        else if(event.getName().equals("guessmap")) {
        
        	boolean isAdded = MapGameHandler.getInstance().addGame(event);
        	if(isAdded) {
        		gameEndService.schedule(
        				new MapGameEndRunnable(
        				MapGameHandler.getInstance().getGameMap()
        				.get(event.getChannel().getIdLong()), event.getChannel().getIdLong()),
        				30, TimeUnit.SECONDS
        				);
        	}
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        if(event.getComponentId().equals("playAgainButton")) {
            boolean isAdded = FlagGameHandler.getInstance().addGame(event);
            if(isAdded) {
                gameEndService.schedule(new FlagGameEndRunnable(
                        FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
                        event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
            }
        }
        else if (event.getComponentId().equals("skipButton")) {
        	event.deferReply().queue();
            if(FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())){
                event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
                FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
            }
        }
        else if (event.getComponentId().equals("checkRegionButton")) {
        	event.deferReply().setEphemeral(true).queue();
        	String response = RegionHandler.getInstance().requestForHint(event);
        	response = (response!=null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();;
        }
        else if(event.getComponentId().equals("playAgainMap")) {
        	boolean isAdded = MapGameHandler.getInstance().addGame(event);
        	if(isAdded) {
        		gameEndService.schedule(
        				new MapGameEndRunnable(
        				MapGameHandler.getInstance().getGameMap()
        				.get(event.getChannel().getIdLong()), event.getChannel().getIdLong()),
        				30, TimeUnit.SECONDS
        				);
        	}
        }
        else if (event.getComponentId().equals("skipMap")) {
        	event.deferReply().queue();
            if(MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())){
                event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
                MapGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
            }
        }
    }

}
