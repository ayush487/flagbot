package com.ayushtech.flagbot.listeners;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.logo.LogoGameHandler;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private final long vote_notifs_channel = 1190982948804100108l;
    private Map<String, String> alternateNamesMap;
    private String[] keywords = { "link", "games", "download game" };
    private long privateServerId = 835384407368007721l;

    public MessageListener() {
        super();
        alternateNamesMap = new HashMap<>();
        loadAlternateNames();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        if (event.getChannel().getIdLong() == vote_notifs_channel) {
            String voter_id = event.getMessage().getContentDisplay();
            VotingService.getInstance().voteUser(event.getJDA(), voter_id);
            return;
        }

        if (event.getAuthor().isBot())
            return;

        if (event.isFromGuild() && event.getGuild().getIdLong() == privateServerId
                && isContainKeyword(event.getMessage().getContentDisplay().toLowerCase())) {
            PrivateServerService.getInstance().handleMessage(event);
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

        if (alternateNamesMap.containsKey(messageText.toLowerCase())) {
            messageText = alternateNamesMap.get(messageText.toLowerCase());
        }
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

    private boolean isContainKeyword(String message) {
        for (String keyword : keywords) {
            if (message.contains(keyword))
                return true;
        }
        return false;
    }

    private void loadAlternateNames() {
        alternateNamesMap.put("uae", "United Arab Emirates");
        alternateNamesMap.put("dr congo", "Democratic Republic of the Congo");
        alternateNamesMap.put("côte d'ivoire", "Ivory Coast");
        alternateNamesMap.put("cabo verde", "Cape Verde");
        alternateNamesMap.put("czechia", "Czech Republic");
        alternateNamesMap.put("turkey", "Turkiye");
        alternateNamesMap.put("usa", "United States of America");
        alternateNamesMap.put("united states", "United States of America");
        alternateNamesMap.put("uk", "United Kingdom");
        alternateNamesMap.put("east timor", "Timor-Leste");
        alternateNamesMap.put("bharat", "India");
        alternateNamesMap.put("bosnia", "Bosnia and Herzegovina");
        alternateNamesMap.put("burma", "Myanmar");
        alternateNamesMap.put("c sharp", "C#");
        alternateNamesMap.put("cpp", "C++");
        alternateNamesMap.put("ea", "Electronic Arts");
        alternateNamesMap.put("car", "Central African Republic");
        alternateNamesMap.put("south georgia", "South Georgia and the South Sandwich Islands");
    }

}
