package com.ayushtech.flagbot.gambling;

import java.util.HashMap;
import java.util.Map;
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

public class SlotsHandler {

    private static SlotsHandler instance = null;

    private LRUCache<Long, Long> rateLimitCache;

    private final String[] cards = { ":bell:", ":cherries:", ":lemon:", ":four_leaf_clover:", ":gem:", ":seven:" };
    private static final int[] RATES = { 35, 25, 16, 10, 8, 6 };
    private final double[] pairPayouts = { 1.25, 1.55, 2.1, 2.6, 3.1, 4.0 };
    private final double[] trioPayouts = { 2.1, 3.1, 5.2, 7.2, 10.5, 15.0 };
    private final Map<String, String> slotEmojis;
    private Random random;

    private SlotsHandler() {
        rateLimitCache = new LRUCache<>(100);
        random = new Random();
        slotEmojis = new HashMap<>(13);
        populateSlotEmojis();
    }

    public static SlotsHandler getInstance() {
        if (instance == null)
            instance = new SlotsHandler();
        return instance;
    }

    // /slots amount:10000 (max) (amount is required)
    public void handleSlotsSlashCommand(SlashCommandInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        long prevTimestamp = rateLimitCache.getOrDefault(userId, 0l);
        if (System.currentTimeMillis() - prevTimestamp < Constants.GAMBLE_COMMAND_COOLDOWN
                && !PatreonService.getInstance().isUserPatron(userId)) {
            long cooldownEndTime = prevTimestamp + Constants.GAMBLE_COMMAND_COOLDOWN;
            event.getHook()
                    .sendMessage("You're on cooldown! Try again " + TimeFormat.RELATIVE.format(cooldownEndTime))
                    .queue(m -> m.delete().queueAfter(
                            Constants.GAMBLE_COMMAND_COOLDOWN + prevTimestamp - System.currentTimeMillis(),
                            TimeUnit.MILLISECONDS));
            return;
        }
        OptionMapping amountOption = event.getOption("amount");
        long amount = checkAmount(amountOption.getAsLong());
        long userBalance = CoinDao.getInstance().getBalance(userId);
        if (userBalance < amount) {
            event.getHook().sendMessage("🪙 **You don't have enough coins to gamble!** Your balance is too low.").queue();
            return;
        }
        rateLimitCache.put(userId, System.currentTimeMillis());
        String initText = String.format("You bet %,d <:flag_coin:1472232340523843767>", amount);
        String initialMessage = getSlotImage(7, 7, 7, initText, "");
        int card1 = rollSymbol();
        int card2 = rollSymbol();
        int card3 = rollSymbol();
        double payout = payoutRate(card1, card2, card3);
        int finalAmount = (int) (amount * payout);
        String finalText;
        if (payout == 0.0)
            finalText = "and won nothing... :c";
        else
            finalText = "and won " + String.format("%,d <:flag_coin:1472232340523843767>", finalAmount);
        event.getHook().sendMessage(initialMessage).queue(m -> {
            CoinDao.getInstance().addCoins(userId, payout == 0 ? (long) (amount * -1) : (long) (finalAmount - amount));
            m.editMessage(getSlotImage(card1, 7, 7, initText, "")).queueAfter(1, TimeUnit.SECONDS);
            m.editMessage(getSlotImage(card1, 7, card3, initText, "")).queueAfter(2, TimeUnit.SECONDS);
            m.editMessage(getSlotImage(card1, card2, card3, initText, finalText)).queueAfter(3, TimeUnit.SECONDS);
        });
    }

    public void handleSlotsPrefixCommand(MessageReceivedEvent event, String[] commandData) {
        MetricService.getInstance().registerCommandData("slots");
        long userId = event.getAuthor().getIdLong();
        long prevTimestamp = rateLimitCache.getOrDefault(userId, 0l);
        MessageChannel channel = event.getChannel();
        Message msg = event.getMessage();
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
            msg.reply("⚠️ **Specify the amount!** — try `f!slots 100`").queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        Optional<Long> amountOptional = parse(commandData[1]);
        if (amountOptional.isEmpty()) {
            msg.reply("❌ **Invalid arguments!** — usage: `f!slots <amount>`").queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        long amount = checkAmount(amountOptional.get());
        long userBalance = CoinDao.getInstance().getBalance(userId);
        if (userBalance < amount) {
            channel.sendMessage("🪙 **You don't have enough coins to gamble!** Your balance is too low.")
            .queue(m -> m.delete().queueAfter(3000, TimeUnit.MILLISECONDS));
            return;
        }
        rateLimitCache.put(userId, System.currentTimeMillis());
        String initText = String.format("You bet %,d <:flag_coin:1472232340523843767>", amount);
        String initialMessage = getSlotImage(7, 7, 7, initText, "");
        int card1 = rollSymbol();
        int card2 = rollSymbol();
        int card3 = rollSymbol();
        double payout = payoutRate(card1, card2, card3);
        int finalAmount = (int) (amount * payout);
        String finalText;
        if (payout == 0.0)
            finalText = "and won nothing... :c";
        else
            finalText = "and won " + String.format("%,d <:flag_coin:1472232340523843767>", finalAmount);
        channel.sendMessage(initialMessage).queue(m -> {
            CoinDao.getInstance().addCoins(userId, payout == 0 ? (long) (amount * -1) : (long) (finalAmount - amount));
            m.editMessage(getSlotImage(card1, 7, 7, initText, "")).queueAfter(1, TimeUnit.SECONDS);
            m.editMessage(getSlotImage(card1, 7, card3, initText, "")).queueAfter(2, TimeUnit.SECONDS);
            m.editMessage(getSlotImage(card1, card2, card3, initText, finalText)).queueAfter(3, TimeUnit.SECONDS);
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

    private int rollSymbol() {
        int roll = random.nextInt(100);
        int cumulative = 0;

        for (int i = 0; i < cards.length; i++) {
            cumulative += RATES[i];
            if (roll < cumulative) {
                return i;
            }
        }
        return cards.length - 1;
    }

    private double payoutRate(int a, int b, int c) {
        if (a == b && b == c) {
            return trioPayouts[a];
        }
        if (a == b || a == c) {
            return pairPayouts[a];
        } else if (b == c) {
            return pairPayouts[b];
        }
        return 0.0;
    }

    private String getSlotImage(int num1, int num2, int num3, String text1, String text2) {
        StringBuilder sb = new StringBuilder(
                "<:slot11:1538212518156181624><:slot12:1538212605263618139><:slot13:1538212630391689276><:slot14:1538212656664813608><:slot15:1538212680706555954>\n<:slot21:1538212704521814098>");
        sb.append(num1 > 5 ? "<a:slots:1538150860750725150>" : cards[num1]);
        sb.append(num2 > 5 ? "<a:slots:1538150860750725150>" : cards[num2]);
        sb.append(num3 > 5 ? "<a:slots:1538150860750725150>" : cards[num3]);
        sb.append("<:slot25:1538212813514743878> ");
        sb.append(text1);
        sb.append(
                "\n<:slot31:1538212835228913684><:slot32:1538212853889376386><:slot33:1538212878790824126><:slot34:1538212899724726362><:slot35:1538212921417400400> ");
        sb.append(text2);
        return sb.toString();

    }

    private long checkAmount(long amount) {
        if (amount > Constants.MAX_GAMBLE_AMOUNT)
            return Constants.MAX_GAMBLE_AMOUNT;
        else if (amount < 1)
            return 1;
        else
            return amount;
    }

    private void populateSlotEmojis() {
        slotEmojis.put("slot11", "<:slot11:1538212518156181624>");
        slotEmojis.put("slot12", "<:slot12:1538212605263618139>");
        slotEmojis.put("slot13", "<:slot13:1538212630391689276>");
        slotEmojis.put("slot14", "<:slot14:1538212656664813608>");
        slotEmojis.put("slot15", "<:slot15:1538212680706555954>");
        slotEmojis.put("slot21", "<:slot21:1538212704521814098>");
        slotEmojis.put("slot25", "<:slot25:1538212813514743878>");
        slotEmojis.put("slot31", "<:slot31:1538212835228913684>");
        slotEmojis.put("slot31", "<:slot32:1538212853889376386>");
        slotEmojis.put("slot31", "<:slot33:1538212878790824126>");
        slotEmojis.put("slot31", "<:slot34:1538212899724726362>");
        slotEmojis.put("slot31", "<:slot35:1538212921417400400>");
        slotEmojis.put("slotGif", "<a:slots:1538150860750725150>");
    }

}
