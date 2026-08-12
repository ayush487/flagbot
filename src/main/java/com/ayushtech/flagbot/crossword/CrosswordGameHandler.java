package com.ayushtech.flagbot.crossword;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.LevelsDao;
import com.ayushtech.flagbot.dbconnectivity.UserDao;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CrosswordGameHandler {

	private static CrosswordGameHandler instance = null;

	private Map<Long, CrosswordGame> gameMap = new HashMap<>();
	private Map<Long, CrosswordDuel> duelGameMap = new HashMap<>();
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
		if (duelGameMap.containsKey(userId)) {
			event.reply("You have an active crossduel game!\nFinish them to start a new game.").queue();
			return;
		}
		if (gameMap.containsKey(userId)) {
			event.reply("You already have a active game!\nDo you want to start a new one ?").setEphemeral(true)
					.setComponents(ActionRow.of(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game")))
					.queue();
			return;
		}
		event.reply("Starting game!").queue();
		Level level = LevelsDao.getInstance().getUserCurrentLevel(userId);
		boolean isUserPatron = PatreonService.getInstance().isUserPatron(userId);
		var game = new CrosswordGame(userId, level, event.getChannel(), !isUserPatron, true);
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

	}

	// public void handleCrosswordTextCommand(MessageReceivedEvent event) {
	// long authorId = event.getAuthor().getIdLong();
	// if (gameMap.containsKey(authorId)) {
	// event.getChannel().sendMessage("You already have a active game!\nDo you want
	// to start a new one ?")
	// .setActionRow(Button.primary("cancelThenNewCrossword_" + authorId, "Start a
	// new game"),
	// Button.primary("cancelCrossword_" + authorId, "Cancel Older Game"))
	// .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
	// return;
	// }
	// try {
	// Level level = LevelsDao.getInstance().getUserCurrentLevel(authorId);
	// CrosswordGame game = new CrosswordGame(authorId, level, event.getChannel(),
	// true);
	// gameMap.put(authorId, game);
	// final int gameHashCode = game.hashCode();
	// CompletableFuture.delayedExecutor(CROSSWORD_DURATION,
	// TimeUnit.MINUTES).execute(() -> {
	// if (!gameMap.containsKey(authorId))
	// return;
	// int currentRunningGameHashCode = gameMap.get(authorId).hashCode();
	// if (gameHashCode == currentRunningGameHashCode) {
	// game.cancelGame();
	// gameMap.remove(authorId);
	// }
	// });
	// } catch (SQLException e) {
	// event.getChannel().sendMessage("Something went wrong!\nPlease try
	// again").queue();
	// }
	// }

	public void handleCrosswordButton(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		long userId = event.getUser().getIdLong();
		if (duelGameMap.containsKey(userId)) {
			event.getHook().sendMessage("You have an active crossduel game!\nFinish them to start a new game.").queue();
			return;
		}
		if (gameMap.containsKey(userId)) {
			event.getHook().sendMessage("You already have a active game!\\nDo you want to start a new one ?")
					.setComponents(ActionRow.of(Button.primary("cancelThenNewCrossword_" + userId, "Start a new game"),
							Button.primary("cancelCrossword_" + userId, "Cancel Older Game")))
					.queue();
			return;
		}
		event.getHook().sendMessage("Starting game!").queue();
		Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
		boolean isUserPatron = PatreonService.getInstance().isUserPatron(userId);
		var game = new CrosswordGame(userId, userLevel, event.getChannel(), !isUserPatron, true);
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

	}

	// public void handleDailyCrosswordButton(ButtonInteractionEvent event) {
	// event.deferReply(true).queue();
	// long userId = event.getUser().getIdLong();
	// if (gameMap.containsKey(userId)) {
	// event.getHook().sendMessage("You already have a active game!")
	// .setActionRow(Button.primary("cancelCrossword_" + userId, "Cancel Older
	// Game"))
	// .queue();
	// return;
	// }
	// try {
	// String todayDate = UtilService.getInstance().getDate();
	// Optional<Level> dLOpt = LevelsDao.getInstance().getDailyLevel(userId,
	// todayDate);
	// if (dLOpt.isEmpty()) {
	// event.getHook().sendMessage("You already played daily puzzle
	// today").setEphemeral(true).queue();
	// return;
	// } else {
	// var game = new DailyCrossword(userId, dLOpt.get(), event.getChannel(),
	// todayDate);
	// gameMap.put(userId, game);
	// final int gameHashCode = game.hashCode();
	// CompletableFuture.delayedExecutor(CROSSWORD_DURATION,
	// TimeUnit.MINUTES).execute(() -> {
	// if (!gameMap.containsKey(userId))
	// return;
	// int currentRunningGameHashCode = gameMap.get(userId).hashCode();
	// if (gameHashCode == currentRunningGameHashCode) {
	// game.cancelGame();
	// gameMap.remove(userId);
	// }
	// });
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// event.getHook().sendMessage("Something went wrong, please try
	// again").queue();
	// }
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
		Level userLevel = LevelsDao.getInstance().getUserCurrentLevel(userId);
		boolean isUserPatron = PatreonService.getInstance().isUserPatron(userId);
		var game = new CrosswordGame(userId, userLevel, event.getChannel(), !isUserPatron, true);
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
		if (game.isPatreonHintAvailable()) {
			if (game.activateHint(true)) {
				event.editButton(
						Button.primary(event.getComponentId(), "(Free)").withEmoji(Emoji.fromUnicode("U+1F4A1")))
						.queue();
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		} else if (game.isFreeHintAvailable()) {
			if (game.activateHint(false)) {
				event.editButton(Button.primary(event.getComponentId(), "💡 (100 🪙)")).queue();
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		} else {
			int userBalance = UserDao.getInstance().getUserBalance(event.getUser().getIdLong());
			if (userBalance < 100) {
				event.getHook().sendMessage("You dont have enough balance to use hint!").setEphemeral(true).queue();
				return;
			}
			if (game.activateHint(false)) {
				CompletableFuture.runAsync(() -> {
					UserDao.getInstance().deductUserBalance(event.getUser().getIdLong(), 100);
				});
			} else {
				event.getHook().sendMessage("No empty space left for hint").setEphemeral(true).queue();
			}
		}

		// if (game.hasUsedHint()) {

		// } else {

		// }

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
				.addComponents(ActionRow.of(extraWordCount >= 25 ? Button.success("claimExtraWords_" + userId, "Claim")
						: Button.success("claimExtraWords", "Claim").asDisabled()))
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
				.addComponents(ActionRow.of(extraWordCount >= 25 ? Button.success("claimExtraWords_" + userId, "Claim")
						: Button.success("claimExtraWords", "Claim").asDisabled()))
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
		} else if (duelGameMap.containsKey(authorId)) {
			var duel = duelGameMap.get(authorId);
			String message = event.getMessage().getContentRaw().toLowerCase();
			var response = duel.checkWord(message, authorId);
			// If the word is correct for the crossword and first time answerred
			if (response.isCorrect()) {
				event.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue();
				if (event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(),
						Permission.MESSAGE_MANAGE)) {
					event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
				}
				duel.updateGame(response);
				if (response.levelCompleted()) {
					Arrays.stream(duel.getPlayersID()).forEach(id -> duelGameMap.remove(id));
				}
			}
			// If the word is not in the crossword
			else {
				// If the answer is an actual word
				if (allWordList.contains(message)) {
					CompletableFuture.runAsync(() -> {
						// If the word is already answerred
						if (duel.isWordAnswerred(message)) {
							event.getMessage().addReaction(Emoji.fromUnicode("U+1F501")).queue();
							if (event.isFromGuild() && event.getGuild().getSelfMember()
									.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
								event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
							}
						}
						// If the word is not answerred yet
						else {
							// If the word can be formed using provided letters
							if (duel.isWordSuitable(message)) {
								duel.addAnswerredWords(message, authorId);
								event.getMessage().addReaction(Emoji.fromUnicode("U+1F4DD")).queue();
								if (event.isFromGuild() && event.getGuild().getSelfMember()
										.hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
									event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
								}

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

	public void removeDuel(long p1, long p2) {
		duelGameMap.remove(p1);
		duelGameMap.remove(p2);
	}

	public boolean isActiveGame(long userId, long channelId) {
		if (gameMap.containsKey(userId)) {
			var game = gameMap.get(userId);
			return game.getChannelId() == channelId;
		} else if (duelGameMap.containsKey(userId)) {
			var duel = duelGameMap.get(userId);
			return duel.getChannelId() == channelId;
		}
		return false;
	}

	public void removeWordFromWordSet(String word) {
		allWordList.remove(word);
	}

	public void addWordIntoWordSet(String word) {
		allWordList.add(word);
	}

	private void startCrossDuelGame(long player1Id, long player2Id, MessageChannel channel) {
		Level level = LevelsDao.getInstance().getRandomLevel();
		CrosswordDuel duel = new CrosswordDuel(player1Id, player2Id, level, channel, true);
		duelGameMap.put(player1Id, duel);
		duelGameMap.put(player2Id, duel);
		int gameHashCode = duel.hashCode();
		CompletableFuture.delayedExecutor(CROSSWORD_DURATION, TimeUnit.MINUTES).execute(() -> {
			if (!duelGameMap.containsKey(player1Id))
				return;
			int currentRunningGameHashCode = duelGameMap.get(player1Id).hashCode();
			if (gameHashCode == currentRunningGameHashCode) {
				duel.timeRunout();
				duelGameMap.remove(player1Id);
				duelGameMap.remove(player2Id);
			}
		});
	}

	public void handleCrossduelSlashCommand(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		long challengerID = event.getMember().getIdLong();
		User opponentUser = event.getOption("opponent").getAsUser();
		if (opponentUser.isBot()) {
			event.getHook().sendMessage("You cannot challenge a bot!")
					.queue(m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
			return;
		}
		long opponentID = opponentUser.getIdLong();
		if (challengerID == opponentID) {
			event.getHook().sendMessage("You cannot challenge yourself!")
					.queue(m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
			return;
		}
		if (gameMap.containsKey(challengerID) || gameMap.containsKey(opponentID)) {
			event.getHook().sendMessage(
					"You or your opponent may have an active crossword game. Please complete or quit it to start a Crossduel.")
					.queue();
			return;
		}
		if (duelGameMap.containsKey(challengerID) || duelGameMap.containsKey(opponentID)) {
			event.getHook().sendMessage(
					"You or your opponent may have an active crossduel game. Please complete or quit it to start a new one.")
					.queue();
			return;
		}

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Crossduel Challenge!");
		eb.setDescription(String.format("<@%d> has challenged <@%d> to a Crossduel!", challengerID, opponentID));
		eb.setColor(Color.BLUE);
		eb.setFooter("The game will start once the opponent accepts.");

		event.getHook()
				.sendMessage("<@" + opponentID + ">")
				.addEmbeds(eb.build())
				.addComponents(ActionRow.of(
						Button.success("acceptCrossduel_" + challengerID + "_" + opponentID, "Accept"),
						Button.danger("denyCrossduel_" + challengerID + "_" + opponentID, "Deny")))
				.queue();
	}

	public void handleAcceptCrossduel(ButtonInteractionEvent event) {
		String[] buttonInfo = event.getComponentId().split("_");
		String challengerId = buttonInfo[1];
		String opponentId = buttonInfo[2];
		String interactionUserId = event.getMember().getId();
		if (interactionUserId.equals(opponentId)) {
			event.deferEdit().queue();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Crossduel Accepted").setColor(Color.GREEN).setDescription(
					String.format("<@%s> accepted dueling challenge from <@%s>", opponentId, challengerId))
					.setFooter("Game will begin shortly");
			event.getHook().editOriginalEmbeds(eb.build())
					.setComponents(ActionRow.of(Button.success("1234", "Duel accepted").asDisabled())).queue();
			startCrossDuelGame(Long.parseLong(challengerId), Long.parseLong(opponentId), event.getChannel());

		} else {
			event.reply("You can't use this button!").setEphemeral(true).queue();
		}
	}

	public void handleDenyCrossduel(ButtonInteractionEvent event) {
		String[] buttonInfo = event.getComponentId().split("_");
		String challengerId = buttonInfo[1];
		String opponentId = buttonInfo[2];
		String interactionUserId = event.getMember().getId();
		if (interactionUserId.equals(opponentId)) {
			event.deferEdit().queue();
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("Crossduel denied")
					.setDescription(String.format("<@%s> denied the duel challenge from <@%s>", opponentId,
							challengerId))
					.setColor(Color.RED);
			event.getHook().editOriginalEmbeds(eb.build())
					.setComponents(ActionRow.of(Button.secondary("1234", "Duel denied").asDisabled()))
					.queue();
		} else
			event.reply("You can't use this button!").setEphemeral(true).queue();

	}

	public void handleCrossduelQuitButton(ButtonInteractionEvent event) {
		String[] buttonInfo = event.getComponentId().split("_");
		String interactedUser = event.getMember().getId();
		String player1 = buttonInfo[1];
		String player2 = buttonInfo[2];
		if (interactedUser.equals(player1) || interactedUser.equals(player2)) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Quit the duel?");
			eb.setDescription("Quitting now will give your opponent an instant win. Are you sure you want to quit?");
			eb.setColor(Color.RED);
			event.replyEmbeds(eb.build()).setEphemeral(true)
					.addComponents(ActionRow.of(
							Button.danger(String.format("confirmQuitCrossduel_%s", interactedUser), "Yes, Quit"),
							Button.secondary(String.format("cancelQuitCrossduel_%s", interactedUser), "No, Cancel")))
					.setEphemeral(true)
					.queue();
		} else
			event.reply("You cannot use this button").setEphemeral(true).queue();
	}

	public void handleConfirmQuitCrossduel(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		event.getHook().deleteOriginal().queue();
		long interactedUser = event.getUser().getIdLong();
		CrosswordDuel duelGame = duelGameMap.get(interactedUser);
		if (duelGame == null)
			return;
		duelGame.quitGame(event.getUser().getIdLong());
		Arrays.stream(duelGame.getPlayersID()).forEach(id -> duelGameMap.remove(id));
	}

	public void handleCancelQuitCrossduel(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		event.getHook().deleteOriginal().queue();
	}

	public void handleShuffleDuelButton(ButtonInteractionEvent event) {
		String[] buttonInfo = event.getComponentId().split("_");
		String interactedUser = event.getUser().getId();
		if (interactedUser.equals(buttonInfo[1]) || interactedUser.equals(buttonInfo[2])) {
			CrosswordDuel duel = duelGameMap.get(event.getUser().getIdLong());
			duel.shuffleAllowedLetters(event);
		} else
			event.reply("You cannot use this button").setEphemeral(true).queue();
	}

	public void handleCrosswordAppearenceCommand(SlashCommandInteractionEvent event) {
		long userId = event.getUser().getIdLong();
		if (PatreonService.getInstance().isUserPatron(userId)) {
			CrosswordBgTile userSelectedBgTile = PatreonService.getInstance().getUserBgTile(userId);
			CrosswordFgTile userSelectedEmptyTile = PatreonService.getInstance().getUserEmptyTile(userId);
			Container c = getCWAppearanceContainer(userId, userSelectedBgTile, userSelectedEmptyTile);
			event.getHook().sendMessageComponents(c).useComponentsV2().queue();
		} else {
			event.getHook().sendMessage("This command is only for patreon users.")
					.queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
		}
	}

	public void handleBgSelection(StringSelectInteractionEvent event) {
		String userId = event.getComponentId().split("_")[1];
		String interactionUser = event.getUser().getId();
		if (userId.equals(interactionUser)) {
			event.deferEdit().queue();
			long userIdLong = event.getUser().getIdLong();
			SelectOption selectedOption = event.getSelectedOptions().get(0);
			CrosswordBgTile bgTile = CrosswordBgTile.valueOf(selectedOption.getValue().toUpperCase());
			PatreonService.getInstance().setUserBgTile(userIdLong, bgTile);
			CrosswordFgTile emptyTile = PatreonService.getInstance().getUserEmptyTile(userIdLong);
			Container c = getCWAppearanceContainer(userIdLong, bgTile, emptyTile);
			event.getHook().editMessageComponentsById(event.getMessageId(), c).useComponentsV2().queue();
		} else {
			event.reply("This is not for you!").setEphemeral(true).queue();
		}

	}

	public void handleEmptySelection(StringSelectInteractionEvent event) {
		String userId = event.getComponentId().split("_")[1];
		String interactionUser = event.getUser().getId();
		if (userId.equals(interactionUser)) {
			event.deferEdit().queue();
			long userIdLong = event.getUser().getIdLong();
			SelectOption selectedOption = event.getSelectedOptions().get(0);
			CrosswordFgTile emptyTile = CrosswordFgTile.valueOf(selectedOption.getValue().toUpperCase());
			PatreonService.getInstance().setUserEmptyTile(userIdLong, emptyTile);
			CrosswordBgTile bgTile = PatreonService.getInstance().getUserBgTile(userIdLong);
			Container c = getCWAppearanceContainer(userIdLong, bgTile, emptyTile);
			event.getHook().editMessageComponentsById(event.getMessageId(), c).useComponentsV2().queue();
		} else {
			event.reply("This is not for you!").setEphemeral(true).queue();
		}
	}

	private Container getCWAppearanceContainer(long userId, CrosswordBgTile userSelectedBgTile,
			CrosswordFgTile userSelectedEmptyTile) {
		Builder bgSelectMenuBuilder = StringSelectMenu.create("bgSelectMenu_" + userId);
		Builder emptySelectMenuBuilder = StringSelectMenu.create("emptySelectMenu_" + userId);
		for (CrosswordBgTile tile : CrosswordBgTile.values())
			bgSelectMenuBuilder.addOption(tile.getName(), tile.getName(), tile.getEmoji());

		for (CrosswordFgTile tile : CrosswordFgTile.values()) {
			emptySelectMenuBuilder.addOption(tile.getName(), tile.getName(), tile.getEmoji());
		}
		bgSelectMenuBuilder
				.setDefaultOptions(SelectOption.of(userSelectedBgTile.getName(), userSelectedBgTile.getName()));
		emptySelectMenuBuilder
				.setDefaultOptions(SelectOption.of(userSelectedEmptyTile.getName(), userSelectedEmptyTile.getName()));
		Separator separatorSmall = Separator.create(true, Spacing.SMALL);
		Separator separatorLarge = Separator.create(true, Spacing.LARGE);

		StringBuilder sb = new StringBuilder("__Current Appearance__\n");
		sb.append(
				getSampleCrosswordDisplay("--++++:--+--+:club-+:+---+-:++++++:----P-", userSelectedBgTile,
						userSelectedEmptyTile));
		TextDisplay crosswordDisplay = TextDisplay.of(sb.toString());
		Container c = Container.of(
				TextDisplay.of("### Customize your crossword appearance"),
				separatorSmall,
				crosswordDisplay,
				separatorLarge,
				TextDisplay.of("**Background Tile**"),
				ActionRow.of(bgSelectMenuBuilder.build()),
				separatorSmall,
				TextDisplay.of("**Blank Tile**"),
				ActionRow.of(emptySelectMenuBuilder.build()))
				.withAccentColor(Color.PINK);
		return c;

	}

	private String getSampleCrosswordDisplay(String cwSample, CrosswordBgTile userSelectedBgTile,
			CrosswordFgTile userSelectedEmptyTile) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cwSample.length(); i++) {
			char c = cwSample.charAt(i);
			if (c == ':') {
				sb.append("\n");
			} else if (c == '-') {
				sb.append(userSelectedBgTile.getEmoji().getFormatted());
			} else if (c == '+') {
				sb.append(userSelectedEmptyTile.getEmoji().getFormatted());
			} else {
				sb.append(UtilService.getInstance().getEmoji(c));
			}
		}
		return sb.toString();
	}

}