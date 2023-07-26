package com.ayush.listeners;

import javax.annotation.Nonnull;

import com.ayush.game.flag.FlagGameHandler;
import com.ayush.game.map.MapGameHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        
        String messageText = message.getContentDisplay();
        // System.out.println(messageText);
        if (!event.getAuthor().isBot()) {
            if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
                // System.out.println(messageText);
                FlagGameHandler.getInstance().handleGuess(messageText, event);
            }
            if(MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
            	MapGameHandler.getInstance().handleGuess(messageText, event);
            }
            
        }
        
    }
}
