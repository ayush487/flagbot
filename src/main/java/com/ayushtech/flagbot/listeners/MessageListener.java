package com.ayushtech.flagbot.listeners;

import java.util.HashMap;
import java.util.Map;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private final long vote_notifs_channel = 1190982948804100108l;
    private Map<String, String> alternateNamesMap;
    private String[] keywords = { "link", "games", "download game" };
    private long privateServerId = 1465232854681129065l;
    private long webhook_channel = 1118507065439174677l;
    private long newPledgeChannel = 1263027212194414644l;
    private long updatePledgeChannel = 1263027292322529301l;

    public MessageListener() {
        super();
        alternateNamesMap = new HashMap<>();
        loadAlternateNames();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        long channelId = event.getChannel().getIdLong();
        if (channelId == vote_notifs_channel) {
            String voter_id = event.getMessage().getContentDisplay();
            VotingService.getInstance().voteUser(event.getJDA(), voter_id);
            return;
        } else if (channelId == webhook_channel) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F44D")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F937")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("U+1F44E")).queue();
            return;
        } else if (channelId == newPledgeChannel || channelId == updatePledgeChannel) {
            String patreonId = event.getMessage().getContentDisplay();
            PatreonService.getInstance().addNewPatron(event.getJDA(), Long.parseLong(patreonId));
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

        // if
        // (CaptchaService.getInstance().userHasCaptched(event.getAuthor().getIdLong()))
        // {
        // if (event.isFromType(ChannelType.PRIVATE)) {
        // CaptchaService.getInstance().handleCaptchaAnswer(event, messageText);
        // return;
        // } else {
        // return;
        // }
        // }

        if (messageText.startsWith("f!set correct_guess")) {
            PatreonService.getInstance().setReactionsForCorrectGuess(event);
            return;
        } else if (messageText.startsWith("f!set wrong_guess")) {
            PatreonService.getInstance().setReactionsForWrongGuess(event);
            return;
        } else if (messageText.startsWith("f!remove wrong_guess")) {
            PatreonService.getInstance().removeReactionsForWrongGuess(event);
            return;
        } else if (messageText.startsWith("f!exitatlas")) {
            AtlasGameHandler.getInstance().requestCancelGame(event);
            return;
        }

        if (GuessDistanceHandler.getInstance().isActiveGameInChannel(channelId)) {
            GuessDistanceHandler.getInstance().handleGuess(messageText, event);
        }

        if (AtlasGameHandler.getInstance().isGameExist(channelId)) {
            AtlasGameHandler.getInstance().handleAnswer(messageText, event);
        }

        if (alternateNamesMap.containsKey(messageText.toLowerCase())) {
            messageText = alternateNamesMap.get(messageText.toLowerCase());
        }
        if (GuessGameHandler.getInstance().isActiveGame(channelId)) {
            GuessGameHandler.getInstance().handleGuess(messageText, event);
            return;
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
        alternateNamesMap.put("Åland islands", "Aland Islands");
        alternateNamesMap.put("northern cyprus", "Turkish Republic of Northern Cyprus");
        alternateNamesMap.put("usvi", "US Virgin Islands");
        alternateNamesMap.put("artsakh", "Nagorno-Karabakh");
        alternateNamesMap.put("united states virgin islands", "US Virgin Islands");
        alternateNamesMap.put("الإمارات", "الإمارات العربية المتحدة");

    }
}
