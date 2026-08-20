package com.ayushtech.flagbot.gambling;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.utils.Constants;
import com.ayushtech.flagbot.utils.LRUCache;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;

public class MinesHandler {

    private static MinesHandler instance = null;

    private LRUCache<Long, Long> rateLimitCache;
    private Map<Long, MinesGame> mineGameMap;

    private MinesHandler() {
        rateLimitCache = new LRUCache<>(100);
        mineGameMap = new HashMap<>();
    }

    public static MinesHandler getInstance() {
        if (instance == null)
            instance = new MinesHandler();
        return instance;
    }

    public void handleMinesSlashCommand(SlashCommandInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        long prevTimestamp = rateLimitCache.getOrDefault(userId, 0l);
        if (System.currentTimeMillis() - prevTimestamp < Constants.GAMBLE_COMMAND_COOLDOWN
                && !PatreonService.getInstance().isUserPatron(userId)) {
            long cooldownEndTime = prevTimestamp + Constants.GAMBLE_COMMAND_COOLDOWN;
            event.getHook().sendMessage("You're on cooldown! Try again " + TimeFormat.RELATIVE.format(cooldownEndTime))
                    .queue(m -> m.delete().queueAfter(
                            Constants.GAMBLE_COMMAND_COOLDOWN + prevTimestamp - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS));
            return;
        }
        if (mineGameMap.containsKey(userId)) {
            mineGameMap.get(userId).resendMessage(event.getHook());
            return;
        }
        OptionMapping amountOption = event.getOption("amount");
        long amount = checkAmount(amountOption.getAsLong());
        long userBalance = CoinDao.getInstance().getBalance(userId);
        if (userBalance < amount) {
            event.getHook().sendMessage("🪙 **You don't have enough coins to gamble!** Your balance is too low.")
                    .queue();
            return;
        }
        rateLimitCache.put(userId, System.currentTimeMillis());
        int mines = 3;
        OptionMapping minesOption = event.getOption("mine");
        if (minesOption != null) {
            int mineInput = minesOption.getAsInt();
            if (mineInput > 0 && mineInput < 9)
                mines = mineInput;
        }
        MinesGame game = new MinesGame(userId, amount, mines, event.getHook());
        mineGameMap.put(userId, game);
    }

    public void handleMinesPrefixCommand(MessageReceivedEvent event, String[] commandData) {
        MetricService.getInstance().registerCommandData("mines");
        long userId = event.getAuthor().getIdLong();
        long prevTimestamp = rateLimitCache.getOrDefault(userId, 0l);
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        if (System.currentTimeMillis() - prevTimestamp < Constants.GAMBLE_COMMAND_COOLDOWN
                && !PatreonService.getInstance().isUserPatron(userId)) {
            long cooldownEndTime = prevTimestamp + Constants.GAMBLE_COMMAND_COOLDOWN;
            msg.reply("You're on cooldown! Try again " + TimeFormat.RELATIVE.format(cooldownEndTime))
                    .queue(m -> m.delete().queueAfter(
                            Constants.GAMBLE_COMMAND_COOLDOWN + prevTimestamp - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS));
            return;
        }
        if (mineGameMap.containsKey(userId)) {
            mineGameMap.get(userId).resendMessage(channel);
            return;
        }
        if (commandData.length < 2) {
            msg.reply("⚠️ **Specify the amount!** — try `f!mines 100`")
                    .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        Optional<Integer> amountOptional = parse(commandData[1]);
        if (amountOptional.isEmpty()) {
            msg.reply("❌ **Invalid arguments!** — usage: `f!mines <amount>`")
                    .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        long amount = checkAmount(amountOptional.get());
        long userBalance = CoinDao.getInstance().getBalance(userId);
        if (userBalance < amount) {
            msg.reply("🪙 **You don't have enough coins to gamble!** Your balance is too low.")
                    .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        rateLimitCache.put(userId, System.currentTimeMillis());
        int mines = 3;
        if (commandData.length>2) {
            Optional<Integer> minesOptional = parse(commandData[2]);
            if (minesOptional.isPresent()) {
                int minesProvided = minesOptional.get();
                mines = minesProvided < 1 ? 3 : minesProvided;
                mines = mines > 8 ? 3 : mines;
            }
        }
        MinesGame game = new MinesGame(userId, amount, mines, channel);
        mineGameMap.put(userId, game);
    }

    private Optional<Integer> parse(String num) {
        try {
            int numInt = Integer.parseInt(num);
            return Optional.of(numInt);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void handleMineButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String[] buttonData = event.getComponentId().split("_");
        if (event.getUser().getId().equals(buttonData[2])) {
            long userId = event.getUser().getIdLong();
            mineGameMap.get(userId).registerMineButton(Integer.parseInt(buttonData[1]));
        }
    }

    public void handleCashoutButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String buttonData[] = event.getComponentId().split("_");
        if (event.getUser().getId().equals(buttonData[1])) {
            mineGameMap.get(event.getUser().getIdLong()).registerCashoutButton();
        }
    }

    public void removeGame(long userId) {
        this.mineGameMap.remove(userId);
    }

    private long checkAmount(long amount) {
        if (amount > Constants.MAX_GAMBLE_AMOUNT)
            return Constants.MAX_GAMBLE_AMOUNT;
        else if (amount < 1)
            return 1;
        else
            return amount;
    }

}
