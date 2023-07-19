package com.ayush.listeners;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayush.game.GameEndRunnable;
import com.ayush.game.GameHandler;

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
            if(GameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())){
                GameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
            }
        }
    }

}
