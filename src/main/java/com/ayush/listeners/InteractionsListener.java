package com.ayush.listeners;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayush.game.GameEndRunnable;
import com.ayush.game.GameHandler;
import com.ayush.game.LeaderboardHandler;
import com.ayush.game.RegionHandler;

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
        	
            boolean isAdded = GameHandler.getInstance().addGame(event);
            if (isAdded) {
                gameEndService.schedule(new GameEndRunnable(
                        GameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
                        event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
            }
        } else if(event.getName().equals("leaderboards")) {
        	event.deferReply().queue();
        	JDA jda = event.getJDA();
        	String temp = LeaderboardHandler.getInstance().getLeaderboard(jda);
        	String leaderboard = temp!=null ? temp : "Something went wrong!";
			event.getHook().sendMessage(leaderboard).queue();
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        if(event.getComponentId().equals("playAgainButton")) {
            boolean isAdded = GameHandler.getInstance().addGame(event);
            if(isAdded) {
                gameEndService.schedule(new GameEndRunnable(
                        GameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
                        event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
            }
        }
        else if (event.getComponentId().equals("skipButton")) {
        	event.deferReply().queue();
            if(GameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())){
                event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
                GameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
            }
        }
        else if (event.getComponentId().equals("checkRegionButton")) {
        	event.deferReply().setEphemeral(true).queue();
        	String response = RegionHandler.getInstance().requestForHint(event);
        	response = (response!=null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();;
        }
    }

}
