package com.ayushtech.flagbot.listeners;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.logo.LogoGameHandler;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private final long vote_notifs_channel = 1190982948804100108l;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        if (event.getChannel().getIdLong() == vote_notifs_channel) {
            String voter_id = event.getMessage().getContentDisplay();
            VotingService.getInstance().voteUser(event.getJDA(), voter_id);
            return;
        }

        if (CaptchaService.getInstance().isUserBanned(event.getAuthor().getIdLong())) {
			return;
		}
        Message message = event.getMessage();

        if (CaptchaService.getInstance().userHasCaptched(event.getAuthor().getIdLong())) {
            if (event.isFromType(ChannelType.PRIVATE)) {
                CaptchaService.getInstance().handleCaptchaAnswer(event, message.getContentDisplay());
                return;
            } else {
                return;
            }
        }
        String messageText = message.getContentDisplay();
        if (!event.getAuthor().isBot()) {
            if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
                FlagGameHandler.getInstance().handleGuess(messageText, event);
            }
            if (MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
                MapGameHandler.getInstance().handleGuess(messageText, event);
            }
            if (LogoGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
                LogoGameHandler.getInstance().handleGuess(messageText, event);
            }
            return;
        }

    }
}
