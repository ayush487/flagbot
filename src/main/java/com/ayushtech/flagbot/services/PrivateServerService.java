package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.PrivateServerDao;
import com.ayushtech.flagbot.utils.PingRecord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PrivateServerService {

    private static PrivateServerService privateServerService = null;
    private final Map<Long, Long> slowdownMap;
    private final long slowdown = 30000L;
    private final long chatSummonId = 1467494594899087422L;
    private final long vcSummonId = 1467494758946701354L;
    private final long communityHeadRoleId = 1476553851372048465L;
    private final String embedContentUrl = "https://raw.githubusercontent.com/ayush487/flagbot/refs/heads/main/govembed.txt";
    private String embedDescription;
    private final String[] keywords = {"link", "games", "download game"};
    private Map<Long, Event> activeEvent;

    private PrivateServerService() {
        slowdownMap = new HashMap<>();
        embedDescription = getOnlineFileContent(embedContentUrl);
        this.activeEvent = PrivateServerDao.getInstance().retrieveActiveEvents();
    }

    public static PrivateServerService getInstance() {
        if (privateServerService == null) {
            privateServerService = new PrivateServerService();
        }
        return privateServerService;
    }

    public void handleChatPingCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Member member = event.getMember();
        boolean isAllowed = false;
        for (Role role : member.getRoles()) {
            if (role.getIdLong() == communityHeadRoleId) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            if (member.hasPermission(Permission.ADMINISTRATOR))
                isAllowed = true;
            else {
                event.getHook().sendMessage("You don't have permission to use this command").queue();
                return;
            }
        }
        OptionMapping channelMapping = event.getOption("channel");
        GuildChannelUnion channelUnion = channelMapping.getAsChannel();
        if (!channelUnion.getType().equals(ChannelType.TEXT)) {
            event.getHook().sendMessage("Only Text Channel is allowed for Chat Ping Command").queue();
            return;
        }
        if (isAllowed) {
            if (PingRecord.getInstance().isChatPingAllowed()) {
                event.getHook().sendMessage("Pinging the chat...").queue();
                OptionMapping textMapping = event.getOption("message");
                String text = "";
                if (textMapping != null) {
                    text = textMapping.getAsString();
                }

                channelUnion.asTextChannel().sendMessage(String.format("<@&%d> %s", chatSummonId, text)).queue();
            } else
                event.getHook().sendMessage("Chat pinging is in cooldown").queue();
        }
    }

    public void handleVcPingCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Member member = event.getMember();
        boolean isAllowed = false;
        for (Role role : member.getRoles()) {
            if (role.getIdLong() == communityHeadRoleId) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            if (member.hasPermission(Permission.ADMINISTRATOR))
                isAllowed = true;
            else {
                event.getHook().sendMessage("You don't have permission to use this command").queue();
                return;
            }
        }
        OptionMapping channelMapping = event.getOption("channel");
        GuildChannelUnion channelUnion = channelMapping.getAsChannel();
        if (!channelUnion.getType().equals(ChannelType.TEXT) && !channelUnion.getType().equals(ChannelType.VOICE)) {
            event.getHook().sendMessage("Only Text or Voice Channel is allowed for VC Ping Command").queue();
            return;
        }
        if (isAllowed) {
            if (PingRecord.getInstance().isVcPingAllowed()) {
                event.getHook().sendMessage("Pinging the VC...").queue();
                OptionMapping textMapping = event.getOption("message");
                String text = "";
                if (textMapping != null) {
                    text = textMapping.getAsString();
                }
                if (channelUnion.getType().equals(ChannelType.TEXT)) {
                    channelUnion.asTextChannel().sendMessage(String.format("<@&%d> %s", vcSummonId, text)).queue();
                } else {
                    channelUnion.asVoiceChannel().sendMessage(String.format("<@&%d> %s", vcSummonId, text)).queue();
                }
            } else
                event.getHook().sendMessage("VC pinging is in cooldown").queue();
        }
    }

    public void handleMessage(MessageReceivedEvent event) {
        long channelId = event.getChannel().getIdLong();
        if (activeEvent.containsKey(channelId)) {
            // Add Entries
            long messageId = event.getMessageIdLong();
            long userId = event.getAuthor().getIdLong();
            int eventId = activeEvent.get(channelId).eventId;
            PrivateServerDao.getInstance().addEventEntries(userId, messageId, eventId);
            event.getMessage().addReaction(Emoji.fromUnicode("U+2795")).queue();
        } else {
            String messageContent = event.getMessage().getContentDisplay().toLowerCase();
            if (!isContainKeyword(messageContent)) return;
            if (messageContent.contains("@gamesofvaibhav"))
                return;
            if (!slowdownMap.containsKey(channelId)) {
                sendDownloadLinkMessage(event);
                slowdownMap.put(channelId, System.currentTimeMillis());
            } else {
                if (System.currentTimeMillis() - slowdownMap.get(channelId) >= slowdown) {
                    sendDownloadLinkMessage(event);
                    slowdownMap.put(channelId, System.currentTimeMillis());
                }
            }
        }
    }

    private void sendDownloadLinkMessage(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("__Download Our Games__");
        eb.setColor(Color.GREEN);
        eb.setDescription(embedDescription);
        eb.setFooter("Click on the links to download them", "https://cdn.discordapp.com/emojis/1227575632930668555.png");
        eb.addField("__Can't find your favourite game__ ?",
                "Check out these channels :\n<#1465252474398183568>\n<#1465252368902783058>", false);
        eb.addField("__How to download and install games__", "[Click here](https://www.youtube.com/watch?v=sPy45fv_L6A)",
                false);
        event.getMessage().replyEmbeds(eb.build()).queue(
                message -> message.delete().queueAfter(slowdown, TimeUnit.MILLISECONDS));
    }

    public void updateEmbedDescription() {
        this.embedDescription = getOnlineFileContent(embedContentUrl);
    }

    private String getOnlineFileContent(String fileUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = URI.create(fileUrl).toURL();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private boolean isContainKeyword(String message) {
        for (String keyword : keywords) {
            if (message.contains(keyword))
                return true;
        }
        return false;
    }

    //  Event Voting System

    public static class Event {
        int eventId;
        long channelId;
        Set<Long> entries;
        Map<Long, Long> voters;

        public Event(int eventId, long channelId) {
            this.eventId = eventId;
            this.channelId = channelId;
            this.entries = new HashSet<>();
            this.voters = new HashMap<>();
        }

        public void setEntries(Set<Long> entries) {
            this.entries = entries;
        }

        public void setVoters(Map<Long, Long> voters) {
            this.voters = voters;
        }

        public int getEventId() {
            return eventId;
        }
    }


    public void handleEventCommand(SlashCommandInteractionEvent event) {
        boolean hasPermmision = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!hasPermmision) {
            event.reply("You can't use this command").setEphemeral(true).queue();
            return;
        }
        event.deferReply().queue();
        String subcommandName = event.getSubcommandName();
        if (subcommandName.equals("start")) {
            // Start a new event in the specified channel
            Channel channel = event.getOption("channel").getAsChannel();
            Long channelId = channel.getIdLong();
            int response = PrivateServerDao.getInstance().addEvent(channelId);
            if (response == -2)
                event.getHook().sendMessage("Something went wrong!").queue();
            else if (response == -1)
                event.getHook().sendMessage("There is already a active event going on that channel").queue();
            else {
                Event newEvent = new Event(response, channelId);
                activeEvent.put(channelId, newEvent);
                event.getHook().sendMessage(String.format("Event with ID : `%d` is started on %s", response, channel.getAsMention())).queue();
            }
        } else if (subcommandName.equals("end")) {
            // End a running event on the specified channel
            Channel channel = event.getOption("channel").getAsChannel();
            Long channelId = channel.getIdLong();
            if (!activeEvent.containsKey(channelId)) {
                event.getHook().sendMessage("No running event in the specified channel found!").queue();
                return;
            }
            int eventId = activeEvent.get(channelId).eventId;
            PrivateServerDao.getInstance().endEvent(channelId);
            activeEvent.remove(channelId);
            event.getHook().sendMessage("Event with ID : `" + eventId + "` has been ended now").queue();
        } else if (subcommandName.equals("restart")) {
            // Restart a old event
            int eventId = event.getOption("event_id").getAsInt();
            for (Event runningEvent : activeEvent.values()) {
                if (runningEvent.eventId == eventId) {
                    event.getHook().sendMessage("Specified event is already running!").queue();
                    return;
                }
            }
            Event restartedEvent = PrivateServerDao.getInstance().restartEvent(eventId);
            if (restartedEvent == null) {
                event.getHook().sendMessage("Event not found!").queue();
                return;
            }
            activeEvent.put(restartedEvent.channelId, restartedEvent);
            event.getHook().sendMessage(String.format("Event with ID : `%d` is running again in <#%d>", eventId, restartedEvent.channelId)).queue();
        }
    }

    public void handleEventAddReactions(MessageReactionAddEvent event) {
        long channelId = event.getChannel().getIdLong();
        if (!activeEvent.containsKey(channelId)) return;
        String reactionEmojiFormatted = event.getEmoji().getFormatted();
        if (!reactionEmojiFormatted.equals("➕")) return;
        long userId = event.getUser().getIdLong();
        Event voteEvent = activeEvent.get(channelId);
        long messageId = event.getMessageIdLong();
        if (voteEvent.voters.containsKey(userId)) {
//            User Already Voted, Now changing his vote
            long previousVoteMessageId = voteEvent.voters.get(userId);
            if (previousVoteMessageId==messageId) return;
            event.getChannel().retrieveMessageById(previousVoteMessageId).queue(prevMsg ->
                    prevMsg.removeReaction(Emoji.fromUnicode("U+2795"), event.getUser()).queue()
            );
        }
        voteEvent.voters.put(userId, messageId);
        PrivateServerDao.getInstance().vote(userId, messageId, voteEvent.eventId);
    }
}
