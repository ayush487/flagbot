package com.ayushtech.flagbot.utils;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.crossword.CrosswordGameHandler;
import com.ayushtech.flagbot.dbconnectivity.ChannelDao;
import com.ayushtech.flagbot.gambling.CoinflipHandler;
import com.ayushtech.flagbot.gambling.MinesHandler;
import com.ayushtech.flagbot.gambling.SlotsHandler;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.UpdateReminder;
import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PrefixCommandHandler {
    private static final PrefixCommandHandler INSTANCE = new PrefixCommandHandler();

    private Map<Long, String> serverPrefixMap;

    private PrefixCommandHandler() {
        this.serverPrefixMap = ChannelDao.getInstance().getServerPrefixData();
    }

    public static PrefixCommandHandler getInstance() {
        return INSTANCE;
    }

    public void handlePrefixCommand(MessageReceivedEvent event, String prefix) {
        CompletableFuture
                .runAsync(() -> UpdateReminder.getInstance().handleUser(event.getAuthor(), event.getChannel()));
        String msgContent = event.getMessage().getContentDisplay();
        String[] commandData = msgContent.substring(prefix.length()).strip().toLowerCase().split(" ");
        String commandName = commandData[0];
        switch (commandName) {
            case "set":
                handleReactionSetCommand(event, commandData);
                break;
            case "remove":
                handleReactionRemoveCommand(event, commandData);
                break;
            case "exitatlas":
                handleExitAtlasCommand(event);
                break;
            case "slots":
                SlotsHandler.getInstance().handleSlotsPrefixCommand(event, commandData);
                break;
            case "mines", "mine":
                MinesHandler.getInstance().handleMinesPrefixCommand(event, commandData);
                break;
            case "coinflip", "cf":
                CoinflipHandler.getInstance().handleCoinflipPrefixCommand(event, commandData);
                break;
            case "crossword":
                CrosswordGameHandler.getInstance().handleCrosswordPrefixCommand(event);
                break;
            case "balance":
                UtilService.getInstance().handleBalanceCommand(event.getAuthor(), event.getChannel());
                break;
            case "leaderboard", "lb":
                LeaderboardHandler.getInstance().handleLeaderboardCommand(event, commandData);
                break;
            case "invite":
                UtilService.getInstance().handleInviteCommand(event.getMessage());
                break;
            case "help":
                UtilService.getInstance().handleHelpCommand(event);
                break;
            case "crossduel":
                CrosswordGameHandler.getInstance().handleCrossduelCommand(event);
                break;
            case "vote":
                UtilService.getInstance().handleVoteCommand(event.getMessage());
                break;
            default:
                break;
        }
        MetricService.getInstance().registerCommandData(commandName);
    }

    public String getPrefix(long serverId) {
        return serverPrefixMap.getOrDefault(serverId, Constants.DEFAULT_PREFIX);
    }

    public boolean setPrefix(long serverId, String prefix) {
        boolean isPrefixSet = ChannelDao.getInstance().setServerPrefix(serverId, prefix);
        if (isPrefixSet) {
            this.serverPrefixMap.put(serverId, prefix);
            return true;
        } else
            return false;
    }

    public void handlePrefixCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        long serverId = event.getGuild().getIdLong();
        EmbedBuilder eb = new EmbedBuilder();
        OptionMapping setOption = event.getOption("set");
        eb.setTitle(":gear: Prefix Settings");
        eb.setColor(Color.GREEN);
        eb.setFooter("Flag Bot", event.getJDA().getSelfUser().getAvatarUrl());
        if (setOption != null) {
            if (member.hasPermission(Permission.MANAGE_SERVER)) {
                String newPrefix = setOption.getAsString().trim().toLowerCase();
                if (newPrefix.length() > Constants.MAX_ALLOWED_PREFIX_LENGTH) {
                    eb.setDescription("Failed to update server prefix\nMaximum allowed prefix length is `"
                            + Constants.MAX_ALLOWED_PREFIX_LENGTH + "`");
                    eb.setColor(Color.RED);
                } else {
                    if (setPrefix(serverId, newPrefix)) {
                        eb.setDescription("Server prefix has been changed to `" + newPrefix + "`");
                        eb.setColor(Color.GREEN);
                    } else {
                        eb.setDescription("Something went wrong while updating the prefix.Try Again!");
                        eb.setColor(Color.RED);
                    }
                }
            } else {
                eb.setColor(Color.RED);
                eb.setDescription(
                        "You don't have permission to change the prefix — you'll need the `MANAGE SERVER` permission first.");
            }
        }
        StringBuilder sb = new StringBuilder("1. `").append(Constants.DEFAULT_PREFIX).append("`");
        if (serverPrefixMap.containsKey(serverId))
            sb.append("\n2. `").append(serverPrefixMap.get(serverId)).append("`");
        eb.addField("Server Prefix", sb.toString(), false);
        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleExitAtlasCommand(MessageReceivedEvent event) {
        AtlasGameHandler.getInstance().requestCancelGame(event);
    }

    private void handleReactionRemoveCommand(MessageReceivedEvent event, String[] commandData) {
        if (commandData.length < 2)
            return;
        if (commandData[1].equals("wrong_guess"))
            PatreonService.getInstance().setReactionsForWrongGuess(event);
    }

    private void handleReactionSetCommand(MessageReceivedEvent event, String[] commandData) {
        if (commandData.length < 2)
            return;
        if (commandData[1].equals("correct_guess"))
            PatreonService.getInstance().setReactionsForCorrectGuess(event);
        else if (commandData[1].equals("wrong_guess"))
            PatreonService.getInstance().setReactionsForWrongGuess(event);
    }
}
