package com.ayushtech.flagbot.utils;

import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.crossword.CrosswordGameHandler;
import com.ayushtech.flagbot.gambling.CoinflipHandler;
import com.ayushtech.flagbot.gambling.MinesHandler;
import com.ayushtech.flagbot.gambling.SlotsHandler;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.UpdateReminder;
import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PrefixCommandHandler {
    private static final PrefixCommandHandler INSTANCE = new PrefixCommandHandler();

    private PrefixCommandHandler() {
    }

    public static PrefixCommandHandler getInstance() {
        return INSTANCE;
    }

    public void handlePrefixCommand(MessageReceivedEvent event) {
        CompletableFuture.runAsync(() -> UpdateReminder.getInstance().handleUser(event.getAuthor(), event.getChannel()));
        String msgContent = event.getMessage().getContentDisplay();
        String[] commandData = msgContent.substring(2).strip().toLowerCase().split(" ");
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
            case "mines":
                MinesHandler.getInstance().handleMinesPrefixCommand(event, commandData);
                break;
            case "coinflip":
                CoinflipHandler.getInstance().handleCoinflipPrefixCommand(event, commandData);
                break;
            case "crossword":
                CrosswordGameHandler.getInstance().handleCrosswordPrefixCommand(event);
                break;
            case "balance":
                UtilService.getInstance().handleBalanceCommand(event.getAuthor(), event.getChannel());
                break;
            case "leaderboard":
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
