package com.ayush;

import java.util.HashSet;

import com.ayush.game.FlagGame;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    HashSet<String> countries = new HashSet<>(200);
    FlagGame flagGame;
    
    public MessageListener() {
        super();
        
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String messageText = message.getContentDisplay();
        checkGameStatus();
        if(flagGame!=null) {
            if(guessCountry(messageText)) {
                event.getChannel().sendMessage("You Guessed it right! " + event.getAuthor().getAsMention()).queue();
                flagGame = null;
                FlagGame.endGame(true);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        checkGameStatus();
        String cmdName = event.getName();
        if(cmdName.equalsIgnoreCase("guess")) {
           try {
            event.reply("Starting a new game now!").queue();
            flagGame = FlagGame.newGame(event.getChannel(), System.currentTimeMillis());
           } catch (RuntimeException e) {
            System.out.println(e);
            event.reply("These already a game running!").queue();
           }
        }
    }

    private boolean guessCountry(String guessWord) {
        return flagGame.guess(guessWord);
    }

    private void checkGameStatus() {
        if(flagGame!=null) {
            if(System.currentTimeMillis()-flagGame.getStartingTime()>=30000l) {
                FlagGame.endGame(false);
                flagGame=null;
            }
        }
    }
	
}
