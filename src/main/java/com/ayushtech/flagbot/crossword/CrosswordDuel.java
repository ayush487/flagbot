package com.ayushtech.flagbot.crossword;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CrosswordDuel {

    private final long player1;
    private final long player2;
    private int player1Points;
    private int player2Points;
    private final int asciA = 97;
    private final MessageChannel channel;
    private final Level level;
    private long messageId;
    private Set<String> enterredWords;
    private List<String> extraWords;
    private final int[] letterCounts = new int[26];

    public CrosswordDuel(long player1, long player2, Level level, MessageChannel channel, boolean startInstantly) {
        this.player1 = player1;
        this.player2 = player2;
        this.channel = channel;
        this.level = level;
        this.player1Points = 0;
        this.player2Points = 0;
        this.enterredWords = new HashSet<String>();
        this.extraWords = new ArrayList<String>();
        for (char c : level.getAllowedLetterList()) {
            letterCounts[(int) c - asciA]++;
        }
        sendGameEmbed();
    }

    private void sendGameEmbed() {
        this.channel.sendMessageEmbeds(getBeginningEmbed(level))
                .addComponents(
                        ActionRow.of(
                                Button.primary("shuffleCrossduel_" + player1 + "_" + player2,
                                        Emoji.fromFormatted("<:refresh:1209076086185656340>")),
                                Button.danger(String.format("quitCrossduel_%d_%d", player1, player2), "Quit")))
                .queue(message -> this.messageId = message.getIdLong());
    }

    private MessageEmbed getBeginningEmbed(Level level) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Crossduel"));
        eb.setDescription(getGridFormated());
        eb.setColor(Color.yellow);
        StringBuilder scSb = new StringBuilder();
        scSb.append("<@" + player1 + "> : `" + player1Points + "`\n");
        scSb.append("<@" + player2 + "> : `" + player2Points + "`");
        eb.addField("__Scores__", scSb.toString(), false);
        StringBuilder sb = new StringBuilder(level.getAllowedLetters());
        sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
        sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
        eb.addField("__Allowed Letters__", sb.toString(), false);
        return eb.build();
    }

    public void updateGame(CorrectWordResponse response) {
        this.level.updateUnsolvedGrid(response);
        if (response.levelCompleted()) {
            long winner = player1Points == player2Points ? 0 : player1Points > player2Points ? player1 : player2;
            completeThisLevel(winner);
            String messageToSend = winner == 0 ? "It's a tie! Both players scored equally. 🤝"
                    : String.format("<@%d> won the Crossduel! Congratulations! 🏆", winner);
            this.channel.sendMessage(messageToSend)
                    .queue();
        } else {
            updateEmbed();
            checkIfWordCompleted();
        }
    }

    public void quitGame(long quittingPlayerId) {
        long winningPlayerId = (quittingPlayerId == player1) ? player2 : player1;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Crossduel Concluded! ⚔️");
        eb.setDescription(String.format(
                "A Crossduel has ended!\n\n" +
                        "<@%d> quit the game, so <@%d> wins! 🏆",
                quittingPlayerId, winningPlayerId));
        eb.setColor(Color.RED);
        StringBuilder scSb = new StringBuilder();
        scSb.append(String.format("<@%d> : `%d`\n", player1, player1Points));
        scSb.append(String.format("<@%d> : `%d`", player2, player2Points));
        eb.addField("__Final Scores__", scSb.toString(), false);
        eb.setFooter("The battlefield awaits its next champions!");
        this.channel.editMessageEmbedsById(messageId, eb.build())
                .setComponents(ActionRow.of(Button.danger("duelEnded", "Duel Ended").asDisabled()))
                .queue();
    }

    public void timeRunout() {
        long winner = 0;
        String description;
        if (player1Points > player2Points) {
            winner = player1;
            description = String.format("The Crossduel has ended due to time running out! <@%d> wins with more points! 🏆", winner);
        } else if (player2Points > player1Points) {
            winner = player2;
            description = String.format("The Crossduel has ended due to time running out! <@%d> wins with more points! 🏆", winner);
        } else {
            description = "The Crossduel has ended due to time running out! It's a tie! 🤝";
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Crossduel Timed Out! ⏳");
        eb.setDescription(description);
        eb.setColor(winner == 0 ? Color.GRAY : Color.GREEN);

        StringBuilder scSb = new StringBuilder();
        scSb.append(String.format("<@%d> : `%d`\n", player1, player1Points));
        scSb.append(String.format("<@%d> : `%d`", player2, player2Points));
        eb.addField("__Final Scores__", scSb.toString(), false);
        this.channel.editMessageEmbedsById(messageId, eb.build())
                .setComponents(ActionRow.of(Button.secondary("duelTimedOut", "Timed Out").asDisabled()))
                .queue();

    }

    private void completeThisLevel(long winningPlayerId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Crossduel Concluded!"));
        eb.setDescription(getGridFormated());
        eb.setColor(winningPlayerId == 0 ? Color.gray : Color.green);
        StringBuilder scSb = new StringBuilder();
        scSb.append("<@" + player1 + "> : `" + player1Points + "`\n");
        scSb.append("<@" + player2 + "> : `" + player2Points + "`");
        if (winningPlayerId == 0)
            eb.appendDescription("\n\nCrossduel tied!");
        else
            eb.appendDescription("\n\n<@" + winningPlayerId + "> won the duel!");
        eb.addField("__Final Scores__", scSb.toString(), false);
        eb.setFooter("The battlefield awaits its next champions!");
        var embed = eb.build();
        this.channel.editMessageEmbedsById(messageId, embed)
                .setComponents(ActionRow.of(winningPlayerId == 0 ? Button.secondary("compl", "Duel Tied").asDisabled()
                        : Button.success("complete", "Duel Completed").asDisabled()))
                .queue();
        // try {
        // LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
    }

    private void updateEmbed() {
        var embed = getEmbed(Color.GREEN, null);
        this.channel.editMessageEmbedsById(messageId, embed).queue();
    }

    public void shuffleAllowedLetters(ButtonInteractionEvent event) {
        level.shuffleAllowedLetters();
        var emb = getEmbed(Color.yellow, null);
        event.editMessageEmbeds(emb).queue();
    }

    public void addAnswerredWords(String word, long playerId) {
        enterredWords.add(word);
        if (playerId == player1)
            player1Points += 1;
        else if (playerId == player2)
            player2Points += 1;
    }

    public boolean isWordAnswerred(String word) {
        return enterredWords.contains(word);
    }

    public boolean isWordSuitable(String word) {
        if (word.length() < level.getMinWordSize() || word.length() > level.getMaxWordSize()) {
            return false;
        }
        int[] tempAllowedLetters = letterCounts.clone();
        for (int i = 0; i < word.length(); i++) {
            int c = (int) word.charAt(i) - asciA;
            if (tempAllowedLetters[c] <= 0) {
                return false;
            } else {
                tempAllowedLetters[c]--;
            }
        }
        extraWords.add(word);
        return true;
    }

    private MessageEmbed getEmbed(Color color, String footerText) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Crossduel"));
        eb.setDescription(getGridFormated());
        eb.setColor(color);
        StringBuilder scSb = new StringBuilder();
        scSb.append("<@" + player1 + "> : `" + player1Points + "`\n");
        scSb.append("<@" + player2 + "> : `" + player2Points + "`");
        eb.addField("__Scores__", scSb.toString(), false);
        StringBuilder sb = new StringBuilder(level.getAllowedLetters());
        sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
        sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
        eb.addField("__Allowed Letters__", sb.toString(), false);
        if (footerText != null)
            eb.setFooter(footerText);
        return eb.build();
    }

    public CorrectWordResponse checkWord(String word, long playerId) {
        var res = level.checkWord(word);
        if (res.isCorrect()) {
            enterredWords.add(word);
            if (playerId == player1)
                player1Points += word.length();
            else if (playerId == player2)
                player2Points += word.length();
        }
        return res;
    }

    private String getGridFormated() {
        char[][] gridUnsolved = level.getGridUnsolved();
        StringBuilder gridFormatted = new StringBuilder();
        for (char[] column : gridUnsolved) {
            for (char cell : column) {
                gridFormatted.append(UtilService.getInstance().getEmoji(cell));
            }
            gridFormatted.append("\n");
        }
        return gridFormatted.toString();
    }

    public long[] getPlayersID() {
        return new long[] { player1, player2 };
    }

    public long getChannelId() {
        return this.channel.getIdLong();
    }

    private void checkIfWordCompleted() {
        CompletableFuture.runAsync(() -> {
            boolean isLevelCompleted = level.checkExtraWordCompletion();
            if (isLevelCompleted) {
                CrosswordGameHandler.getInstance().removeDuel(player1, player2);
                long winner = player1Points == player2Points ? 0 : player1Points > player2Points ? player1 : player2;
                completeThisLevel(winner);
                String message = winner == 0 ? "It's a tie! Both players scored equally. 🤝"
                        : String.format("<@%d> won the Crossduel! Congratulations! 🏆", winner);
                this.channel.sendMessage(message).queue();
                // try {
                // LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
                // } catch (SQLException e) {
                // }
            }
        });
    }

}