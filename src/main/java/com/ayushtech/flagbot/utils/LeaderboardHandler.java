package com.ayushtech.flagbot.utils;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.dbconnectivity.LeaderboardDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LeaderboardHandler {

	private static LeaderboardHandler leaderboardHandler = null;
	private static LeaderboardDao dao = new LeaderboardDao();

	private LeaderboardHandler() {
	}

	public static synchronized LeaderboardHandler getInstance() {
		if (leaderboardHandler == null) {
			leaderboardHandler = new LeaderboardHandler();
		}
		return leaderboardHandler;
	}

	public void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
		String subcommandName = event.getSubcommandName();
		subcommandName = subcommandName == null ? "coins" : subcommandName;
		LbData leaderboardData = null;
		if (subcommandName.equals("coins")) {
			leaderboardData = getLeaderboardDataCoins(event.getJDA(), 10, 0, event.getUser().getIdLong());
		} else {
			leaderboardData = getLeaderboardDataLevels(event.getJDA(), 10, 0, event.getUser().getIdLong());
		}
		event.getHook().sendMessageEmbeds(createLeaderboardEmbed(leaderboardData, subcommandName, event.getUser()))
				.addActionRow(createButtonPrev5(0, leaderboardData.getTotalCount(), subcommandName),
						createButtonPrev1(0, leaderboardData.getTotalCount(), subcommandName),
						createButtonMyRank(subcommandName, leaderboardData.getUserRank()),
						createButtonNext1(0, leaderboardData.getTotalCount(), subcommandName),
						createButtonNext5(0, leaderboardData.getTotalCount(), subcommandName))
				.queue();
	}

	public void handleLeaderboardButton(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		String[] commandData = event.getComponentId().split("_");
		String subcommandName = commandData[1];
		long offset = Long.parseLong(commandData[3]);
		LbData leaderboardData = null;
		if (subcommandName.equals("coins")) {
			leaderboardData = getLeaderboardDataCoins(event.getJDA(), 10, offset, event.getUser().getIdLong());
		} else {
			leaderboardData = getLeaderboardDataLevels(event.getJDA(), 10, offset, event.getUser().getIdLong());
		}
		event.getHook().editOriginalEmbeds(createLeaderboardEmbed(leaderboardData, subcommandName, event.getUser()))
				.setActionRow(createButtonPrev5(offset, leaderboardData.getTotalCount(), subcommandName),
						createButtonPrev1(offset, leaderboardData.getTotalCount(), subcommandName),
						createButtonMyRank(subcommandName, leaderboardData.getUserRank()),
						createButtonNext1(offset, leaderboardData.getTotalCount(), subcommandName),
						createButtonNext5(offset, leaderboardData.getTotalCount(), subcommandName))
				.queue();
	}

	private LbData getLeaderboardDataCoins(JDA jda, int lbSize, long offset, long userId) {
		List<LbEntry> names = dao.getPlayersBasedOnCoins(lbSize, offset);
		LbData data = new LbData(offset, "coins");
		getUsernames(jda, names, data, userId);
		data.setEntries(names);
		return data;
	}

	private LbData getLeaderboardDataLevels(JDA jda, int lbSize, long offset, long userId) {
		List<LbEntry> names = dao.getPlayersBasedOnLevels(lbSize, offset);
		LbData data = new LbData(offset, "levels");
		getUsernames(jda, names, data, userId);
		data.setEntries(names);
		return data;
	}

	private void getUsernames(JDA jda, List<LbEntry> userEntries, LbData lbData, long userId) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (LbEntry entry : userEntries) {
			var future = CompletableFuture.runAsync(() -> {
				User user = jda.getUserById(entry.getUserId());
				if (user == null) {
					user = jda.retrieveUserById(entry.getUserId()).complete();
				}
				entry.setName(user != null ? user.getName() : "UnknownUser");
			});
			futures.add(future);
		}
		if (lbData.getType().equals("coins")) {
			var cf = CompletableFuture.runAsync(() -> {
				long playerCount = dao.getTotalPlayerCount();
				long userRank = dao.getPlayerCoinRank(userId);
				lbData.setTotalCount(playerCount);
				lbData.setUserRank(userRank);
			});

			futures.add(cf);
		} else {
			var cf = CompletableFuture.runAsync(() -> {
				long playerCount = dao.getTotalPlayerCount();
				long userRank = dao.getPlayerLevelRank(userId);
				lbData.setTotalCount(playerCount);
				lbData.setUserRank(userRank);
			});
			futures.add(cf);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	}

	private MessageEmbed createLeaderboardEmbed(LbData leaderboardData, String type, User user) {
		EmbedBuilder eb = new EmbedBuilder();
		StringBuilder sb = new StringBuilder();
		sb.append(type.equals("coins") ? "# 📊 Coins Leaderboard" : "# 📊 Levels Leaderboard");
		sb.append(type.equals("coins") ? "\n\nLeaderboard for coins you have.\n"
				: "\n\nLeaderboard for crossword levels you solved.\n");
		sb.append("## 🏆 Rankings\n");
		for (LbEntry entry : leaderboardData.getEntries()) {
			sb.append(entry.getRankingString() + " @").append(entry.getName());
			if (type.equals("coins")) {
				sb.append("\n\t`coins: ").append(NumberFormat.getInstance().format(entry.getScore())).append("`\n");
			} else {
				sb.append("\n\t`level:` ").append(entry.getScore()).append("\n");
			}
		}
		eb.setDescription(sb.toString());
		eb.setColor(Color.CYAN);
		eb.setFooter(
				"Page " + (leaderboardData.getOffset() / 10 + 1) + " of " + (leaderboardData.getTotalCount() / 10 + 1),
				user.getEffectiveAvatarUrl());
		return eb.build();
	}

	private Button createButtonMyRank(String type, long userRank) {
		long offset = (userRank / 10) * 10;
		Button button = Button.secondary("lb_" + type + "_myrank_" + offset, "My Rank")
				.withEmoji(Emoji.fromFormatted("<:search_profile:1475500256387924140>"));
		return button;
	}

	private Button createButtonPrev5(long currentOffset, long totalCount, String type) {
		long newOffset = 0;
		if (currentOffset < 50) {
			newOffset = totalCount + currentOffset - 50;
			newOffset = (newOffset / 10) * 10;
		} else {
			newOffset = currentOffset - 50;
		}
		Button button = Button.secondary("lb_" + type + "_prev5_" + newOffset,
				Emoji.fromFormatted("<:previous:1209524266131914812>"));
		return button;
	}

	private Button createButtonPrev1(long currentOffset, long totalCount, String type) {
		long newOffset = 0;
		if (currentOffset == 0) {
			if (totalCount % 10 == 0)
				newOffset = totalCount - 10;
			else
				newOffset = (totalCount / 10) * 10;
		} else
			newOffset = currentOffset - 10;
		Button button = Button.secondary("lb_" + type + "_prev1_" + newOffset,
				Emoji.fromFormatted("<:left_tri:1471426605263097999>"));
		return button;
	}

	private Button createButtonNext1(long currentOffset, long totalCount, String type) {
		long newOffset = totalCount - currentOffset <= 10 ? 0 : currentOffset + 10;
		Button button = Button.secondary("lb_" + type + "_next1_" + newOffset,
				Emoji.fromFormatted("<:right_tri:1471426673131126954>"));
		return button;
	}

	private Button createButtonNext5(long currentOffset, long totalCount, String type) {
		long newOffset = totalCount - currentOffset <= 50 ? 0 : currentOffset + 50;
		Button button = Button.secondary("lb_" + type + "_next5_" + newOffset,
				Emoji.fromFormatted("<:next:1209524214302908457>"));
		return button;
	}

}
