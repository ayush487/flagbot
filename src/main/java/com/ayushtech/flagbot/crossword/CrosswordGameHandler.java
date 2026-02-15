package com.ayushtech.flagbot.crossword;

import java.awt.Color;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.LevelsDao;
import com.ayushtech.flagbot.dbconnectivity.UserDao;
import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CrosswordGameHandler {

	private static CrosswordGameHandler instance = null;

	private Map<Long, CrosswordGame> gameMap = new HashMap<>();
	private Set<String> allWordList;
	private final int CROSSWORD_DURATION = 20;

	private CrosswordGameHandler() {
		allWordList = LevelsDao.getInstance().getAllWords();
	}

	public static CrosswordGameHandler getInstance() {
		if (instance == null) {
			instance = new CrosswordGameHandler();
		}
		return instance;
	}

	public void handleCrosswordSlashCommand(SlashCommandInteractionEvent event) {
		long userId = event.getUser().getIdLong();
		if (gameMap.containsKey(userId)) {
			event.reply("You already have a active game!\nDo you want to start a new one ?").setEphemeral(true)
					.setActionRow(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game"))
					.queue();
			return;
		}
		event.reply("Starting game!").queue();
		try {
			Level level = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, level, event.getChannel(), true);
			gameMap.put(userId, game);
			final int gameHashCode = game.hashCode();
			CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(userId))
					return;
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}

	}

	// public void handleCrosswordTextCommand(MessageReceivedEvent event) {
	// 	long authorId = event.getAuthor().getIdLong();
	// 	if (gameMap.containsKey(authorId)) {
	// 		event.getChannel().sendMessage("You already have a active game!\nDo you want to start a new one ?")
	// 				.setActionRow(Button.primary("cancelThenNewCrossword_" + authorId, "Start a new game"),
	// 						Button.primary("cancelCrossword_" + authorId, "Cancel Older Game"))
	// 				.queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
	// 		return;
	// 	}
	// 	try {
	// 		Level level = LevelsDao.getInstance().getUserCurrentLevel(authorId);
	// 		CrosswordGame game = new CrosswordGame(authorId, level, event.getChannel(), true);
	// 		gameMap.put(authorId, game);
	// 		final int gameHashCode = game.hashCode();
	// 		CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
	// 			if (!gameMap.containsKey(authorId))
	// 				return;
	// 			int currentRunningGameHashCode = gameMap.get(authorId).hashCode();
	// 			if (gameHashCode == currentRunningGameHashCode) {
	// 				game.cancelGame();
	// 				gameMap.remove(authorId);
	// 			}
	// 		});
	// 	} catch (SQLException e) {
	// 		event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
	// 	}
	// }

	public void handleCrosswordButton(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		long userId = event.getUser().getIdLong();
		if (gameMap.containsKey(userId)) {
			event.getHook().sendMessage("You already have a active game!\\nDo you want to start a new one ?")
					.setActionRow(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game"))
					.queue();
			return;
		}
		event.getHook().sendMessage("Starting game!").queue();
		try {
			Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, userLevel, event.getChannel(), true);
			gameMap.put(userId, game);
			final int gameHashCode = game.hashCode();
			CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(userId))
					return;
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}
	}

	// public void handleDailyCrosswordButton(ButtonInteractionEvent event) {
	// 	event.deferReply(true).queue();
	// 	long userId = event.getUser().getIdLong();
	// 	if (gameMap.containsKey(userId)) {
	// 		event.getHook().sendMessage("You already have a active game!")
	// 				.setActionRow(Button.primary("cancelCrossword_" + userId, "Cancel Older Game"))
	// 				.queue();
	// 		return;
	// 	}
	// 	try {
	// 		String todayDate = UtilService.getInstance().getDate();
	// 		Optional<Level> dLOpt = LevelsDao.getInstance().getDailyLevel(userId, todayDate);
	// 		if (dLOpt.isEmpty()) {
	// 			event.getHook().sendMessage("You already played daily puzzle today").setEphemeral(true).queue();
	// 			return;
	// 		} else {
	// 			var game = new DailyCrossword(userId, dLOpt.get(), event.getChannel(), todayDate);
	// 			gameMap.put(userId, game);
	// 			final int gameHashCode = game.hashCode();
	// 			CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
	// 				if (!gameMap.containsKey(userId))
	// 					return;
	// 				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
	// 				if (gameHashCode == currentRunningGameHashCode) {
	// 					game.cancelGame();
	// 					gameMap.remove(userId);
	// 				} 
	// 			});
	// 		}
	// 	} catch (SQLException e) {
	// 		e.printStackTrace();
	// 		event.getHook().sendMessage("Something went wrong, please try again").queue();
	// 	}
	// }

	public void handleCrosswordQuitButton(ButtonInteractionEvent event) {
		var buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		if (gameMap.containsKey(event.getUser().getIdLong())) {
			gameMap.get(event.getUser().getIdLong()).quitGame(event);
			gameMap.remove(event.getUser().getIdLong());
		} else {
			event.reply("No game found!").setEphemeral(true).queue();
		}
	}

	public void handleCancelThenNewCrosswordButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		long userId = event.getUser().getIdLong();
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferReply(true).setEphemeral(true).queue();
		if (gameMap.containsKey(userId)) {
			gameMap.get(userId).cancelGame();
			gameMap.remove(userId);
		}
		event.getHook().sendMessage("Starting game!").queue();
		try {
			Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
			var game = new CrosswordGame(userId, userLevel, event.getChannel(), true);
			gameMap.put(userId, game);
			CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
				if (!gameMap.containsKey(userId))
					return;
				int gameHashCode = game != null ? game.hashCode() : 0;
				int currentRunningGameHashCode = gameMap.get(userId).hashCode();
				if (gameHashCode == currentRunningGameHashCode) {
					game.cancelGame();
					gameMap.remove(userId);
				}
			});
		} catch (SQLException e) {
			event.getChannel().sendMessage("Something went wrong!\nPlease try again").queue();
			e.printStackTrace();
		}
	}

	public void handleCrosswordCancelButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		long userId = event.getUser().getIdLong();
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferReply(true).queue();
		CompletableFuture.runAsync(() -> {
			if (gameMap.containsKey(userId)) {
				gameMap.get(userId).cancelGame();
				event.getHook().sendMessage("Older game is cancelled").queue();
				gameMap.remove(userId);
			} else {
				event.getHook().sendMessage("No old game found!").queue();
			}
		});
	}

	public void handleHintButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This Button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.deferEdit().queue();
		var game = gameMap.get(event.getUser().getIdLong());
		if (game.hasUsedHint()) {
			int userBalance = UserDao.getInstance().getUserBalance(event.getUser().getIdLong());
			if (userBalance < 100) {
				event.getHook().sendMessage("You dont have enough balance to use hint!").setEphemeral(true).queue();
				return;
			}
			if (game.activateHint()) {
				CompletableFuture.runAsync(() -> {
					UserDao.getInstance().deductUserBalance(event.getUser().getIdLong(), 100);
				});
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		} else {
			if (game.activateHint()) {
				event.editButton(Button.primary(event.getComponentId(), "💡 (100 🪙)")).queue();
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		}

	}

	public void handleShuffleButton(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!buttonOwnerId.equals(event.getUser().getId())) {
			event.reply("This Button is not for you!").setEphemeral(true).queue();
			return;
		}
		var game = gameMap.get(event.getUser().getIdLong());
		game.shuffleAllowedLetters(event);
	}

	public void handleExtraWordCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		long userId = event.getUser().getIdLong();
		int extraWordCount = UserDao.getInstance().getExtraWordsNumber(userId);
		extraWordCount = extraWordCount > 25 ? 25 : extraWordCount;
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Extra Words");
		StringBuilder sb = new StringBuilder();
		sb.append("Words : ").append(extraWordCount).append("/25\n");
		sb.append(UtilService.getInstance().getProgressBar(extraWordCount * 4));
		eb.setDescription(sb.toString());
		eb.setColor(Color.green);
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			StringBuilder wordlist = new StringBuilder("```\n");
			game.getExtraWords().forEach(w -> wordlist.append(w).append("\n"));
			eb.addField("Current Level Extra Words", wordlist.append("```").toString(), false);
		}
		event.getHook().sendMessageEmbeds(eb.build())
				.addActionRow(extraWordCount >= 25 ? Button.success("claimExtraWords_" + userId, "Claim")
						: Button.success("claimExtraWords", "Claim").asDisabled())
				.queue();
	}

	public void handleExtraWordButton(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		long userId = event.getUser().getIdLong();
		int extraWordCount = UserDao.getInstance().getExtraWordsNumber(userId);
		extraWordCount = extraWordCount > 25 ? 25 : extraWordCount;
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Extra Words");
		StringBuilder sb = new StringBuilder();
		sb.append("Words : ").append(extraWordCount).append("/25\n");
		sb.append(UtilService.getInstance().getProgressBar(extraWordCount * 4));
		eb.setDescription(sb.toString());
		eb.setColor(Color.green);
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			StringBuilder wordlist = new StringBuilder("```\n");
			game.getExtraWords().forEach(w -> wordlist.append(w + "\n"));
			eb.addField("Current Level Extra Words", wordlist.append("```").toString(), false);
		}
		event.getHook().sendMessageEmbeds(eb.build())
				.addActionRow(extraWordCount >= 25 ? Button.success("claimExtraWords_" + userId, "Claim")
						: Button.success("claimExtraWords", "Claim").asDisabled())
				.queue();
	}

	public void handleViewLevelCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		String levelData = event.getOption("level_data").getAsString();
		boolean blank = event.getOption("blank") == null ? false : event.getOption("blank").getAsBoolean();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Level View");
		char[][] grid = getAsGrid(levelData);
		eb.setDescription(getLevelDisplayed(grid, blank));
		eb.setColor(blank ? Color.WHITE : Color.blue);
		event.getHook().sendMessageEmbeds(eb.build()).queue();

	}

	private String getLevelDisplayed(char[][] grid, boolean blank) {
		StringBuilder sb = new StringBuilder();
		if (blank) {
			for (char[] cArr : grid) {
				for (char c : cArr) {
					if (c == '-')
						sb.append(":black_large_square:");
					else
						sb.append(":white_medium_square:");
				}
				sb.append("\n");
			}
		} else {
			for (char[] cArr : grid) {
				for (char c : cArr) {
					sb.append(UtilService.getInstance().getEmoji(c));
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	private char[][] getAsGrid(String levelData) {
		String acrossStrings[] = levelData.split(":");
		int height = acrossStrings.length;
		int width = acrossStrings[0].length();
		char[][] grid = new char[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				grid[i][j] = acrossStrings[i].charAt(j);
			}
		}
		return grid;
	}

	public void inspectAnswer(MessageReceivedEvent event) {
		long authorId = event.getAuthor().getIdLong();
		if (gameMap.containsKey(authorId)) {
			var game = gameMap.get(authorId);
			String message = event.getMessage().getContentRaw().toLowerCase();
			var response = game.checkWord(message);
			// If the word is correct for the crossword and first time answerred
			if (response.isCorrect()) {
				event.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
				if (event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(),
						Permission.MESSAGE_MANAGE)) {
					event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				game.updateGame(response);
				if (response.levelCompleted()) {
					gameMap.remove(authorId);
				}
			}
			// If the word is not in the crossword
			else {
				// If the answer is an actual word
				if (allWordList.contains(message)) {
					CompletableFuture.runAsync(() -> {
						// If the word is already answerred
						if (game.isWordAnswerred(message)) {
							event.getMessage().addReaction(Emoji.fromUnicode("U+1F501")).queue();
							if (event.isFromGuild() && event.getGuild().getSelfMember()
									.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
								event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
							}
						}
						// If the word is not answerred yet
						else {
							// If the word can be formed using provided letters
							if (game.isWordSuitable(message)) {
								game.addAnswerredWords(message);
								event.getMessage().addReaction(Emoji.fromUnicode("U+1F4DD")).queue();
								if (event.isFromGuild() && event.getGuild().getSelfMember()
										.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
									event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
								}
								UserDao.getInstance().updateExtraWordCount(authorId, 1, true);
							}
						}
					});
				}
			}
		}
	}

	public void removeGame(long userId) {
		if (gameMap.containsKey(userId))
			gameMap.remove(userId);
	}

	public boolean isActiveGame(long userId, long channelId) {
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			return game.getChannelId() == channelId;
		}
		return false;
	}

	public void removeWordFromWordSet(String word) {
		allWordList.remove(word);
	}

	public void addWordIntoWordSet(String word) {
		allWordList.add(word);
	}

}