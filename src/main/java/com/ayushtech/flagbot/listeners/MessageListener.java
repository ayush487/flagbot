package com.ayushtech.flagbot.listeners;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.logo.LogoGameHandler;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.game.place.PlaceGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private final long vote_notifs_channel = 1190982948804100108l;
    private Map<String, String> alternateNamesMap;
    private String[] keywords = { "link", "games", "download game" };
    private long privateServerId = 835384407368007721l;
    private long webhook_channel = 1118507065439174677l;

    public MessageListener() {
        super();
        alternateNamesMap = new HashMap<>();
        loadAlternateNames();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        
        long channelId = event.getChannel().getIdLong();
        if (channelId == vote_notifs_channel) {
            String voter_id = event.getMessage().getContentDisplay();
            VotingService.getInstance().voteUser(event.getJDA(), voter_id);
            return;
        } else if (channelId == webhook_channel) {
            event.getMessage().addReaction("U+1F44D").queue();
            event.getMessage().addReaction("U+1F937").queue();
            event.getMessage().addReaction("U+1F44E").queue();
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
        String messageText = event.getMessage().getContentDisplay();

        if (CaptchaService.getInstance().userHasCaptched(event.getAuthor().getIdLong())) {
            if (event.isFromType(ChannelType.PRIVATE)) {
                CaptchaService.getInstance().handleCaptchaAnswer(event, messageText);
                return;
            } else {
                return;
            }
        }

        if (GuessDistanceHandler.getInstance().isActiveGameInChannel(channelId)) {
            GuessDistanceHandler.getInstance().handleGuess(messageText, event);
        }

        if (alternateNamesMap.containsKey(messageText.toLowerCase())) {
            messageText = alternateNamesMap.get(messageText.toLowerCase());
        }
        if (FlagGameHandler.getInstance().getGameMap().containsKey(channelId)) {
            FlagGameHandler.getInstance().handleGuess(messageText, event);
        }
        if (MapGameHandler.getInstance().getGameMap().containsKey(channelId)) {
            MapGameHandler.getInstance().handleGuess(messageText, event);
        }
        if (LogoGameHandler.getInstance().getGameMap().containsKey(channelId)) {
            LogoGameHandler.getInstance().handleGuess(messageText, event);
        }
        if (PlaceGameHandler.getInstance().getGameMap().containsKey(channelId)) {
            PlaceGameHandler.getInstance().handleGuess(messageText, event);
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
        alternateNamesMap.put("drc", "Democratic Republic of the Congo");
        alternateNamesMap.put("côte d'ivoire", "Ivory Coast");
        alternateNamesMap.put("cabo verde", "Cape Verde");
        alternateNamesMap.put("czech republic", "Czechia");
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
        alternateNamesMap.put("eu", "European Union");
        alternateNamesMap.put("car", "Central African Republic");
        alternateNamesMap.put("south georgia", "South Georgia and the South Sandwich Islands");
        alternateNamesMap.put("sealand", "Principality of Sealand");
        alternateNamesMap.put("aland islands", "Åland Islands");
        alternateNamesMap.put("us virgin islands", "U.S. Virgin Islands");
        alternateNamesMap.put("united states virgin islands", "U.S. Virgin Islands");
        alternateNamesMap.put("الإمارات", "الإمارات العربية المتحدة");
    }
}
