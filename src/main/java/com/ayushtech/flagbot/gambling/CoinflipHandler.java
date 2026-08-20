package com.ayushtech.flagbot.gambling;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.utils.Constants;
import com.ayushtech.flagbot.utils.LRUCache;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;

public class CoinflipHandler {

    private static CoinflipHandler instance = null;

    private LRUCache<Long, Long> rateLimitCache;
    private Random random;

    private CoinflipHandler() {
        rateLimitCache = new LRUCache<Long, Long>(100);
        random = new Random();
    }

    public static CoinflipHandler getInstance() {
        if (instance == null)
            instance = new CoinflipHandler();
        return instance;
    }

    public void handleCoinflipSlashCommand(SlashCommandInteractionEvent event) {
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
        OptionMapping amountOption = event.getOption("amount");
        long amount = checkAmount(amountOption.getAsLong());
        long userBalance = CoinDao.getInstance().getBalance(userId);
        if (userBalance < amount) {
            event.getHook().sendMessage("You don't have enough coins to gamble!").queue();
            return;
        }
        rateLimitCache.put(userId, System.currentTimeMillis());
        String choosen = "heads";
        OptionMapping sideOption = event.getOption("side");
        if (sideOption != null) {
            String side = sideOption.getAsString().toLowerCase();
            if (side.startsWith("t")) {
                choosen = "tails";
            }
        }
        final String choosenFinal = choosen;
        int outcome = random.nextInt(2);
        String outcomeEmoji = outcome == 0 ? "<:flag_coin:1472232340523843767>"
                : "<:flagbot_coin_blank:1538134980197355584>";
        String outcomeSide = outcome == 0 ? "heads" : "tails";
        String name = event.getUser().getEffectiveName();
        String initialMessage = generateInitialMessage(name, amount, choosen);
        event.getHook().sendMessage(initialMessage).queue(m -> {
            String finalMsg = "";
            if (choosenFinal.equals(outcomeSide)) {
                finalMsg = generateFinalWinMessage(name, amount, choosenFinal, outcomeEmoji);
                CoinDao.getInstance().addCoins(userId, amount);
            } else {
                finalMsg = generateFinalLoseMessage(name, amount, choosenFinal, outcomeEmoji);
                CoinDao.getInstance().addCoins(userId, amount * -1);
            }
            m.editMessage(finalMsg).queueAfter(2, TimeUnit.SECONDS);

        });
    }

    public void handleCoinflipPrefixCommand(MessageReceivedEvent event, String[] commandData) {
        long userId = event.getAuthor().getIdLong();
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        long prevTimestamp = rateLimitCache.getOrDefault(userId, 0l);
        if (System.currentTimeMillis() - prevTimestamp < Constants.GAMBLE_COMMAND_COOLDOWN
                && !PatreonService.getInstance().isUserPatron(userId)) {
            long cooldownEndTime = prevTimestamp + Constants.GAMBLE_COMMAND_COOLDOWN;
            msg.reply("You're on cooldown! Try again " + TimeFormat.RELATIVE.format(cooldownEndTime))
                    .queue(m -> m.delete().queueAfter(
                            Constants.GAMBLE_COMMAND_COOLDOWN + prevTimestamp - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS));
            return;
        }
        if (commandData.length < 2) {
            msg.reply("⚠️ **Specify the amount!** — try `f!coinflip 100`")
                    .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        Optional<Long> amountOptional = parse(commandData[1]);
        if (amountOptional.isEmpty()) {
            msg.reply("❌ **Invalid arguments!** — usage: `f!coinflip <amount>`")
                    .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        long amount = checkAmount(amountOptional.get());
        rateLimitCache.put(userId, System.currentTimeMillis());
        String choosen = "heads";
        if (commandData.length > 2)
            choosen = commandData[2].toLowerCase().startsWith("t") ? "tails" : "heads";
        final String choosenFinal = choosen;
        int outcome = random.nextInt(2);
        String outcomeEmoji = outcome == 0 ? "<:flag_coin:1472232340523843767>"
                : "<:flagbot_coin_blank:1538134980197355584>";
        String outcomeSide = outcome == 0 ? "heads" : "tails";
        String name = event.getAuthor().getEffectiveName();
        String initialMessage = generateInitialMessage(name, amount, choosen);
        channel.sendMessage(initialMessage).queue(m -> {
            String finalMsg = "";
            if (choosenFinal.equals(outcomeSide)) {
                finalMsg = generateFinalWinMessage(name, amount, choosenFinal, outcomeEmoji);
                CoinDao.getInstance().addCoins(userId, amount);
            } else {
                finalMsg = generateFinalLoseMessage(name, amount, choosenFinal, outcomeEmoji);
                CoinDao.getInstance().addCoins(userId, amount * -1);
            }
            m.editMessage(finalMsg).queueAfter(2, TimeUnit.SECONDS);
        });
    }

    private Optional<Long> parse(String num) {
        try {
            long numLong = Long.parseLong(num);
            return Optional.of(numLong);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String generateInitialMessage(String name, long amount, String choosen) {
        return String.format("**%s** bets **%,d coins** on **%s**\n*coin spins* <a:coin_flipping:1538128565860569172>",
                name, amount,
                choosen);
    }

    private String generateFinalWinMessage(String name, long amount, String choosen, String outcomeEmoji) {
        return String.format("**%s** bets **%,d coins** on **%s**\n*coin spins* %s - won %,d coins :tada:",
                name, amount, choosen, outcomeEmoji, amount * 2);
    }

    private String generateFinalLoseMessage(String name, long amount, String choosen, String outcomeEmoji) {
        return String.format(
                "**%s** bets **%,d coins** on **%s**\n*coin spins* %s - lost all of them... :c ",
                name, amount, choosen, outcomeEmoji);
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
