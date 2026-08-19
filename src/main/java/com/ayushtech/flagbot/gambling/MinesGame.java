package com.ayushtech.flagbot.gambling;

import java.awt.Color;
import java.util.Random;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class MinesGame {

    private static String questionEmoji = "<:question_mark:1231126072909631518>";
    private static String gemEmojiUnicode = "U+1F48E";
    private static String explosionEmojiUnicode = "U+1F4A5";
    private static String bombEmojiUnicode = "U+1F4A3";

    private final long userId;
    private final long amount;
    private long winningAmount = 0;
    private final int mines;
    private int minesExplored = 0;
    private Message message;
    private final boolean[] mineMap; // true = mine, false = safe
    private final double[] multiArray;
    private boolean isGameActive = true;
    private boolean[] exploredMines;

    private static Random random = new Random();

    public MinesGame(long userId, long amount, int mines, InteractionHook hook) {
        this.userId = userId;
        this.amount = amount;
        this.mines = mines;
        this.mineMap = getMineMap(mines);
        this.exploredMines = new boolean[9];
        this.multiArray = getMultipliers(mines);
        hook.sendMessageComponents(getComponent(this, "💣 LET THE HUNT BEGIN!")).useComponentsV2().queue(
                m -> setMessage(m));
        CoinDao.getInstance().addCoins(userId, amount * -1);
    }

    public MinesGame(long userId, long amount, int mines, MessageChannel channel) {
        this.userId = userId;
        this.amount = amount;
        this.mines = mines;
        this.mineMap = getMineMap(mines);
        this.exploredMines = new boolean[9];
        this.multiArray = getMultipliers(mines);
        channel.sendMessageComponents(getComponent(this, "💣 LET THE HUNT BEGIN!")).useComponentsV2().queue(
                m -> setMessage(m));
        CoinDao.getInstance().addCoins(userId, amount * -1);
    }

    public void registerMineButton(int mineIndex) {
        exploredMines[mineIndex] = true;
        minesExplored++;
        String title = "";
        if (mineMap[mineIndex]) {
            winningAmount = 0;
            this.endGameAsLose();
            title = "💥 BOOM! YOU HIT A MINE!";
        } else {
            winningAmount = (long) (amount * multiArray[minesExplored]);
            title = "💎 GEM DISCOVERED!";
            if (minesExplored == 9 - mines)
                endGameAsWin();

        }
        message.editMessageComponents(getComponent(this, title)).useComponentsV2().queue();
    }

    public void registerCashoutButton() {
        this.endGameAsWin();
        message.editMessageComponents(getComponent(this, "💰 YOU ESCAPED WITH THE LOOT!")).useComponentsV2().queue();
    }

    public void resendMessage(InteractionHook hook) {
        this.message.editMessageComponents(
                Container.of(TextDisplay.of("This bet has been resumed somewhere else")).withAccentColor(Color.gray))
                .useComponentsV2()
                .queue();
        hook.sendMessageComponents(getComponent(this, "⛏️ THE HUNT CONTINUES!")).useComponentsV2().queue(
                m -> setMessage(m));
    }

    public void resendMessage(MessageChannel channel) {
        this.message.editMessageComponents(
                Container.of(TextDisplay.of("This bet has been resumed somewhere else")).withAccentColor(Color.gray))
                .useComponentsV2()
                .queue();
        channel.sendMessageComponents(getComponent(this, "⛏️ THE HUNT CONTINUES!")).useComponentsV2().queue(
                m -> setMessage(m));
    }

    private void endGameAsWin() {
        isGameActive = false;
        CoinDao.getInstance().addCoins(userId, winningAmount);
        MinesHandler.getInstance().removeGame(userId);
    }

    private void endGameAsLose() {
        isGameActive = false;
        MinesHandler.getInstance().removeGame(userId);
    }

    private void setMessage(Message msg) {
        this.message = msg;
    }

    private static Container getComponent(MinesGame game, String title) {
        StringBuilder sb = new StringBuilder("### " + title);
        sb.append("\n");
        sb.append(String.format("**Bet**: `%d` **Mines**: `%d`", game.amount, game.mines));
        sb.append("\n");
        sb.append(
                String.format("**Cash Out**: `%d` `(%.2fx)`", game.winningAmount, game.multiArray[game.minesExplored]));
        if (game.minesExplored < 9 - game.mines) {
            sb.append("\n");
            sb.append(String.format("**Next**: `%d` `(%.2fx)`",
                    (long) (game.amount * game.multiArray[game.minesExplored + 1]),
                    game.multiArray[game.minesExplored + 1]));
        }
        TextDisplay textDisplay = TextDisplay.of(sb.toString());
        ActionRow buttonRow1 = ActionRow.of(
                getButton(game.mineMap, game.exploredMines, 0, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 1, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 2, game.userId, game.isGameActive));
        ActionRow buttonRow2 = ActionRow.of(
                getButton(game.mineMap, game.exploredMines, 3, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 4, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 5, game.userId, game.isGameActive));
        ActionRow buttonRow3 = ActionRow.of(
                getButton(game.mineMap, game.exploredMines, 6, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 7, game.userId, game.isGameActive),
                getButton(game.mineMap, game.exploredMines, 8, game.userId, game.isGameActive));
        ActionRow bottomRow = ActionRow.of(getCashoutButton(game.userId, game.isGameActive, game.minesExplored));
        Container container = Container.of(
                textDisplay,
                Separator.createDivider(Spacing.SMALL),
                buttonRow1,
                buttonRow2,
                buttonRow3,
                Separator.createDivider(Spacing.SMALL),
                bottomRow).withAccentColor(Color.decode("#F0B132"));
        return container;
    }

    private static Button getCashoutButton(long userId, boolean isGameActive, int minesExplored) {
        Button cashoutButton = Button.success("cashoutMines_" + userId, "Cash Out")
                .withEmoji(Emoji.fromUnicode("U+1F4B0"));
        if (isGameActive) {
            if (minesExplored == 0)
                return cashoutButton.asDisabled();
            else
                return cashoutButton.asEnabled();
        } else
            return cashoutButton.asDisabled();
    }

    private static Button getButton(boolean[] mineMap, boolean[] exploredMines, int mineIndex, long userId,
            boolean isGameActive) {
        if (isGameActive) {
            if (exploredMines[mineIndex] == false)
                return Button.secondary(String.format("mine_%d_%d", mineIndex, userId),
                        Emoji.fromFormatted(questionEmoji));
            else {
                if (mineMap[mineIndex])
                    return Button.danger(String.format("mine_%d_%d", mineIndex, userId),
                            Emoji.fromUnicode(explosionEmojiUnicode)).asDisabled();
                else
                    return Button
                            .success(String.format("mine_%d_%d", mineIndex, userId), Emoji.fromUnicode(gemEmojiUnicode))
                            .asDisabled();

            }
        } else {
            if (exploredMines[mineIndex] == false)
                if (mineMap[mineIndex]) {
                    return Button.secondary(String.format("mine_%d_%d", mineIndex, userId),
                            Emoji.fromFormatted(bombEmojiUnicode)).asDisabled();
                } else {
                    return Button.secondary(String.format("mine_%d_%d", mineIndex, userId),
                            Emoji.fromFormatted(gemEmojiUnicode)).asDisabled();
                }
            else {
                if (mineMap[mineIndex])
                    return Button.danger(String.format("mine_%d_%d", mineIndex, userId),
                            Emoji.fromUnicode(explosionEmojiUnicode)).asDisabled();
                else
                    return Button
                            .success(String.format("mine_%d_%d", mineIndex, userId), Emoji.fromUnicode(gemEmojiUnicode))
                            .asDisabled();

            }
        }
    }

    private static boolean[] getMineMap(int mineCount) {
        boolean[] map = new boolean[9];
        for (int i = 0; i < mineCount; i++) {
            boolean foundEmpty = false;
            while (!foundEmpty) {
                int pos = random.nextInt(9);
                if (map[pos] == false) {
                    map[pos] = true;
                    foundEmpty = true;
                }
            }
        }
        return map;
    }

    private static double[] getMultipliers(int mineCount) {
        double[][] multiMap = new double[][] {
                { 0, 1.05, 1.24, 1.45, 1.74, 2.2, 2.94, 4.43, 8.5 },
                { 0, 1.24, 1.66, 2.34, 3.52, 5.9, 11.84, 35 },
                { 0, 1.45, 2.34, 4.1, 8.26, 20.7, 83 },
                { 0, 1.65, 3.5, 8.25, 24.9, 124 },
                { 0, 2.2, 5.9, 20, 124 },
                { 0, 2.9, 11.8, 83 },
                { 0, 4.4, 35 },
                { 0, 8.5 },
        };
        return multiMap[mineCount - 1];
    }
}
