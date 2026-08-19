package com.ayushtech.flagbot.utils;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.gambling.CoinflipHandler;
import com.ayushtech.flagbot.gambling.MinesHandler;
import com.ayushtech.flagbot.gambling.SlotsHandler;
import com.ayushtech.flagbot.services.PatreonService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PrefixCommandHandler {
    private static final PrefixCommandHandler INSTANCE = new PrefixCommandHandler();

    private PrefixCommandHandler() {
    }

    public static PrefixCommandHandler getInstance() {
        return INSTANCE;
    }

    public void handlePrefixCommand(MessageReceivedEvent event) {
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
            default:
                break;
        }
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
