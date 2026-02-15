package com.ayushtech.flagbot.crossword;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.dbconnectivity.LevelsDao;
import com.ayushtech.flagbot.dbconnectivity.UserDao;
import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CrosswordGame {

	protected static Random random = new Random();

	protected long userId;
	private final int asciA = 97;
	protected final int levelNumber;
	protected boolean usedHint;
	protected final Level currentLevel;
	protected long messageId;
	protected final MessageChannel channel;
	protected Set<String> enterredWords;
	private final int[] letterCounts = new int[26];
	protected List<String> extraWords;

	public CrosswordGame(long userId, Level level, MessageChannel channel, boolean startInstantly) {
		this.userId = userId;
		this.levelNumber = level.getLevel();
		this.channel = channel;
		this.currentLevel = level;
		this.usedHint = false;
		this.enterredWords = new HashSet<String>();
		this.extraWords = new ArrayList<String>();
		for (char c : level.getAllowedLetterList()) {
			letterCounts[(int) c - asciA]++;
		}
		if (startInstantly)
			sendGameEmbed();
	}

	protected void sendGameEmbed() {
		this.channel.sendMessageEmbeds(getBeginningEmbed(currentLevel))
				.addActionRow(
						Button.primary("shuffleCrossword_" + userId,
								Emoji.fromFormatted("<:refresh:1209076086185656340>")),
						Button.primary("hintCrossword_" + userId, usedHint ? "💡 (100 🪙)" : "💡 (Free)"))
				.addActionRow(Button.danger("quitCrossword_" + userId, "Quit"),
						Button.primary("extraWords", "Extra Words"))
				.queue(message -> this.messageId = message.getIdLong());
	}

	protected MessageEmbed getBeginningEmbed(Level level) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		eb.setColor(Color.yellow);
		StringBuilder sb = new StringBuilder(level.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", level.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", level.getMaxWordSize()));
		eb.addField("__Allowed Letters__", sb.toString(), false);
		return eb.build();
	}

	public void updateGame(CorrectWordResponse response) {
		this.currentLevel.updateUnsolvedGrid(response);
		if (response.levelCompleted()) {
			completeThisLevel();
			String messageToSend = levelNumber == 0 ? "Daily Level Completed! :tada:"
					: "You completed Level " + levelNumber + " :tada:";
			this.channel.sendMessage(messageToSend)
					.addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
		} else {
			updateEmbed();
			checkIfWordCompleted();
		}
	}

	public void quitGame(ButtonInteractionEvent event) {
		var embed = getEmbed((byte) 1, "Game quit!");
		event.editMessageEmbeds(embed).setActionRow(
				levelNumber == 0 ? Button.primary("dailyCrossword", "Start again")
						: Button.primary("newCrossword_" + userId, "Start New Game"))
				.queue();
		CompletableFuture.runAsync(() -> {
			UserDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	public void cancelGame() {
		var embed = getEmbed((byte) 1, "Game cancelled!");
		this.channel.editMessageEmbedsById(messageId, embed)
				.setActionRow(Button.danger("cancelled", "Cancelled").asDisabled()).queue();
		CompletableFuture.runAsync(() -> {
			UserDao.getInstance().updateExtraWordCount(userId, extraWords.size(), false);
		});
	}

	protected void completeThisLevel() {
		var embed = getEmbed((byte) 0, "Level Completed!");
		this.channel.editMessageEmbedsById(messageId, embed)
				.setActionRow(Button.success("complete", "Level Completed").asDisabled()).queue();
		try {
			LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void updateEmbed() {
		var embed = getEmbed((byte) 2, null);
		this.channel.editMessageEmbedsById(messageId, embed).queue();
	}

	protected String getGridFormated() {
		char[][] gridUnsolved = currentLevel.getGridUnsolved();
		StringBuilder gridFormatted = new StringBuilder();
		for (char[] column : gridUnsolved) {
			for (char cell : column) {
				gridFormatted.append(UtilService.getInstance().getEmoji(cell));
			}
			gridFormatted.append("\n");
		}
		return gridFormatted.toString();
	}

	protected MessageEmbed getEmbed(byte status, String footerText) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(String.format("Level %d", levelNumber));
		eb.setDescription(getGridFormated());
		Color color = status == 0 ? Color.green : status == 1 ? Color.red : Color.yellow;
		eb.setColor(color);
		StringBuilder sb = new StringBuilder(currentLevel.getAllowedLetters());
		sb.append(String.format("\nMinimum Word Size : `%d`", currentLevel.getMinWordSize()));
		sb.append(String.format("\nMaximum Word Size : `%d`", currentLevel.getMaxWordSize()));
		eb.addField("__Allowed Letters__", sb.toString(), false);
		if (footerText != null)
			eb.setFooter(footerText);
		return eb.build();
	}

	public boolean activateHint() {
		List<CrosswordPointer> emptyPositionList = new ArrayList<CrosswordPointer>();
		for (int i = 0; i < currentLevel.getColumns(); i++) {
			for (int j = 0; j < currentLevel.getRows(); j++) {
				if (currentLevel.getGridUnsolved()[i][j] == '+') {
					emptyPositionList.add(new CrosswordPointer(i, j));
				}
			}
		}
		if (emptyPositionList.size() == 0) {
			return false;
		} else {
			var pointer = emptyPositionList.get(random.nextInt(emptyPositionList.size()));
			currentLevel.unlockLetter(pointer.i(), pointer.j());
			updateEmbed();
			usedHint = true;
			checkIfWordCompleted();
			return true;
		}
	}

	public CorrectWordResponse checkWord(String word) {
		var res = currentLevel.checkWord(word);
		if (res.isCorrect()) {
			enterredWords.add(word);
		}
		return res;
	}

	public long getChannelId() {
		return this.channel.getIdLong();
	}

	public int getLevel() {
		return this.levelNumber;
	}

	public void shuffleAllowedLetters(ButtonInteractionEvent event) {
		currentLevel.shuffleAllowedLetters();
		var emb = getEmbed((byte) 2, null);
		event.editMessageEmbeds(emb).queue();
	}

	public void addAnswerredWords(String word) {
		enterredWords.add(word);
	}

	public boolean isWordAnswerred(String word) {
		return enterredWords.contains(word);
	}

	public boolean isWordSuitable(String word) {
		if (word.length() < currentLevel.getMinWordSize() || word.length() > currentLevel.getMaxWordSize()) {
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

	protected void checkIfWordCompleted() {
		CompletableFuture.runAsync(() -> {
			boolean isLevelCompleted = currentLevel.checkExtraWordCompletion();
			if (isLevelCompleted) {
				CrosswordGameHandler.getInstance().removeGame(userId);
				completeThisLevel();
				this.channel.sendMessage("You completed Level " + levelNumber + " :tada:")
						.addActionRow(Button.primary("newCrossword_" + userId, "Play Next Level")).queue();
				try {
					LevelsDao.getInstance().promoteUserLevel(userId, levelNumber);
				} catch (SQLException e) {
				}
			}
		});
	}

	public List<String> getExtraWords() {
		return this.extraWords;
	}

	public boolean hasUsedHint() {
		return this.usedHint;
	}
}

