package com.ayushtech.flagbot.listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.StocksDao;
import com.ayushtech.flagbot.dbconnectivity.StocksTransactionsDao;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.fight.Damage;
import com.ayushtech.flagbot.game.fight.FightHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.logo.LogoGameEndRunnable;
import com.ayushtech.flagbot.game.logo.LogoGameHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.CoinTransferService;
import com.ayushtech.flagbot.stocks.Company;
import com.ayushtech.flagbot.stocks.StocksHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

public class InteractionsListener extends ListenerAdapter {

	private ScheduledExecutorService gameEndService;
	private ChannelService channelService;
	private Random random;
	private final int BOUND = 200;

	public InteractionsListener() {
		super();
		gameEndService = new ScheduledThreadPoolExecutor(50);
		channelService = ChannelService.getInstance();
		random = new Random();
	}

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

		if (CaptchaService.getInstance().isUserBanned(event.getUser().getIdLong())) {
			event.reply("You are banned from using bot").setEphemeral(true).queue();
			return;
		}

		if (CaptchaService.getInstance().userHasCaptched(event.getUser().getIdLong())) {
			event.reply("Solve the captcha first").setEphemeral(true).queue();
			return;
		}

		// Disable Command
		if (event.getName().equals("disable")) {
			event.deferReply().setEphemeral(true).queue();
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				OptionMapping option = event.getOption("channel");
				if (option == null) {
					channelService.disableChannel(event.getChannel().getIdLong());
					event.getHook().sendMessage("Commands are disabled for this channel now!").setEphemeral(true).queue();
				} else {
					GuildMessageChannel channelOption = option.getAsMessageChannel();
					if (channelOption == null) {
						event.getHook().sendMessage("Mentioned channel is not a Message Channel").setEphemeral(true).queue();
					} else {
						channelService.disableChannel(channelOption.getIdLong());
						event.getHook().sendMessage("Commands are disabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();
					}
				}
			} else {
				event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
						.queue();
			}
			return;
		}

		// disable_all_channels command
		else if (event.getName().equals("disable_all_channels")) {
			event.deferReply().setEphemeral(true).queue();
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				channelService.disableMultipleChannels(event.getGuild());
				event.getHook().sendMessage("Commands are disabled in all channels.").setEphemeral(true).queue();
			} else {
				event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
						.queue();
			}
			return;
		}

		// Enable Command
		else if (event.getName().equals("enable")) {
			event.deferReply().setEphemeral(true).queue();
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				OptionMapping option = event.getOption("channel");
				if (option == null) {
					channelService.enableChannel(event.getChannel().getIdLong());
					event.getHook().sendMessage("Commands are enabled for this channel now!").setEphemeral(true).queue();
				} else {
					GuildMessageChannel channelOption = option.getAsMessageChannel();
					if (channelOption == null) {
						event.getHook().sendMessage("Mentioned channel is not a Message Channel").queue();
					} else {
						channelService.enableChannel(channelOption.getIdLong());
						event.getHook().sendMessage("Commands are enabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();
					}
				}
			} else {
				event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
						.queue();
			}
			return;
		}

		boolean isCommandsDisabled = channelService.isChannelDisabled(event.getChannel().getIdLong());

		if (isCommandsDisabled) {
			event.deferReply().setEphemeral(true).queue();
			event.getHook().sendMessage("Commands are disabled in this channel").setEphemeral(true).queue();
			return;
		}

		if (event.getName().equals("battle")) {
			FightHandler.getInstance().handleFightCommand(event);
			return;
		}

		event.deferReply().queue();

		if (event.getName().equals("vote")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Vote for Flag Bot");
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setDescription("Vote for Flag bot on top.gg\n[here](https://top.gg/bot/1129789320165867662/vote)");
			eb.addField("Rewards", "> Each vote gets you 1000 :coin:\n> You will get double rewards during weekends", false);
			eb.setFooter("You can vote every 12 hours");
			eb.setColor(Color.GREEN);
			event.getHook().sendMessageEmbeds(eb.build())
					.addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Top.gg"))
					.queue();
			return;
		}

		// leaderboards command
		else if (event.getName().equals("leaderboards")) {
			JDA jda = event.getJDA();
			int optInt = 5;
			OptionMapping optSize = event.getOption("size");
			if (optSize != null) {
				optInt = optSize.getAsInt();
			}
			int lbSize = optInt >= 25 ? 25 : (optInt <= 5) ? 5 : optInt;
			CompletableFuture.runAsync(() -> {
				String temp = LeaderboardHandler.getInstance().getLeaderboard(jda, lbSize);
				String leaderboard = temp != null ? temp : "Something went wrong!";
				event.getHook().sendMessage(leaderboard).queue();
			});
			return;
		}

		// invite command
		else if (event.getName().equals("invite")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setColor(Color.GREEN);
			eb.setTitle("Invite Flagbot");
			eb.addField("Add Flagbot",
					"[here](https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=85056&scope=bot+applications.commands)",
					true);
			eb.addBlankField(true);
			eb.addField("Support Server", "[here](https://discord.gg/MASMYsNCT9)", true);
			event.getHook().sendMessageEmbeds(eb.build())
					.addActionRow(Button.link(
							"https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=85056&scope=bot+applications.commands",
							"Add Flag bot to your server"), Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote"))
					.queue();
			return;
		}

		// help command
		else if (event.getName().equals("help")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setTitle("Commands");
			eb.setColor(new Color(255, 153, 51)); // rgb (255,153,51)
			eb.setDescription(
					"`/guess flag` : Start a flag guessing game in the channel\n`/guess map` : Start a map guessing game in the channel\n`/guess logo` : Start a logo guessing game in the channel\n`/leaderboards` : Check the global leaderboard (Top 5)\n`/invite` : Invite the bot to your server\n`/disable` : Disable the commands in the given channel\n`/enable` : Enable the commands in the given channel\n`/disable_all_channels` : Disable the commands for all the channels of the server\n`/delete_my_data` : Will Delete your data from the bot\n`/balance` : You can see your coins and rank\n`/give coins` : Send coins to other users.\n`/vote` : Vote for us and get rewards");
			eb.addField("__Battle Command__",
					"`/battle` : Start a 1v1 battle between two users.\n**__Options__**\n**opponent** : Mention the user with whom you wanna battle.\n**bet** : Amout to bet in the battle (optional)",
					false);
			eb.addField("__Stocks__",
					"`/stocks list` : View Available Stocks with current market prices\n`/stocks owned` : View your portfolio\n`/stocks buy` : Buy Shares of different companies\n`/stocks sell` : Sell Shares which you own for coins",
					false);
			eb.addField("Other Information",
					"[Terms of Services](https://github.com/ayush487/flagbot/blob/main/TERMSOFSERVICE.md)\n[Privacy Policy](https://github.com/ayush487/flagbot/blob/main/PRIVACY.md)",
					false);
			eb.setFooter("You can earn 1000 coins by voting for us");
			event.getHook().sendMessageEmbeds(eb.build())
					.addActionRow(Button.link("https://discord.gg/RqvTRMmVgR", "Support Server"),
							Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote"))
					.queue();
			return;
		}

		else if (event.getName().equals("support")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.YELLOW);
			eb.addField("Support Server", "[Flag Bot Support Server](https://discord.gg/RqvTRMmVgR)", false);
			eb.addField("Support Developer",
					"[<:buymeacoffee:1202183996021415946> Buy me a coffee](https://www.buymeacoffee.com/ayush487)", false);
			event.getHook().sendMessageEmbeds(eb.build())
					.addActionRow(Button.link("https://www.buymeacoffee.com/ayush487",
							Emoji.fromEmote("buymeacoffee", 1202183996021415946l, false)))
					.queue();
		}

		// delete_my_data command
		else if (event.getName().equals("delete_my_data")) {
			User user = event.getUser();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.RED);
			eb.setTitle("Delete data");
			eb.setDescription("Sending a dm for confirmation.");
			eb.setFooter("If not received message, consider turning on DMs.");
			event.getHook().sendMessageEmbeds(eb.build()).queue();
			EmbedBuilder eb2 = new EmbedBuilder();
			eb2.setColor(Color.RED);
			eb2.setTitle("Confirm Data deletion");
			eb2.setDescription(
					"Click on the **Delete My Data** button to delete your data permanetly.\nNote : It will wipe all your coins, stocks permanently.");
			;
			user.openPrivateChannel()
					.flatMap(channel -> channel.sendMessageEmbeds(eb2.build())
							.setActionRows(ActionRow.of(Button.primary("delete_data", "Delete My Data"))))
					.queue();
			return;
		}

		// balance command
		else if (event.getName().equals("balance")) {
			User user = event.getUser();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle(user.getName());
			long[] coins_rank = CoinDao.getInstance().getBalanceAndRank(user.getIdLong());
			eb.setDescription("**Balance** : " + coins_rank[0] + " :coin:\n**Rank** : " + coins_rank[1]);
			eb.setColor(Color.YELLOW);
			eb.setThumbnail(user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build()).setEphemeral(false).queue();
			return;
		}

		// Give Command
		else if (event.getName().equals("give")) {
			String subCommandName = event.getSubcommandName();

			// Give Coins Command
			if (subCommandName.equals("coins")) {
				CompletableFuture.runAsync(() -> CoinTransferService.getInstance().handleGiveCoinsCommand(event));
				return;
			}
		}

		// stock command
		else if (event.getName().equals("stocks")) {
			String subcommandName = event.getSubcommandName();

			// List Stocks Command
			if (subcommandName.equals("list")) {
				event.getHook().sendMessageEmbeds(StocksHandler.getInstance().getStockList())
						.addActionRow(Button.primary("refreshMarket_" + System.currentTimeMillis(),
								Emoji.fromEmote("refresh", 1209076086185656340l, false)))
						.queue();
				return;
			}

			// View Owned Stocks Command
			else if (subcommandName.equals("owned")) {
				event.getHook().sendMessageEmbeds(StocksHandler.getInstance().getStocksOwned(event.getUser()))
						.addActionRow(Button.secondary("stockTransactions_0", "View Transactions"))
						.queue();
				return;
			}

			// Buy Stocks Command
			else if (subcommandName.equals("buy")) {
				String companyName = event.getOption("company").getAsString().toUpperCase();
				if (StocksHandler.getInstance().isCompanyValid(companyName)) {
					Company selectedCompany = Company.valueOf(companyName);
					int amountOfStocks = 0;
					try {
						amountOfStocks = event.getOption("amount").getAsInt();
					} catch (Exception e) {
						event.getHook().sendMessage("Something went wrong!").queue();
						return;
					}
					if (amountOfStocks <= 0) {
						event.getHook().sendMessage("You can't buy negative numbers of stocks :face_with_raised_eyebrow:").queue();
						return;
					}
					int[] returnArray = StocksHandler.getInstance().buyStocks(selectedCompany, amountOfStocks,
							event.getUser().getIdLong());
					if (returnArray[0] == 1) {
						event.getHook()
								.sendMessage("You bought `" + amountOfStocks + "` shares of **" + selectedCompany.toString()
										+ "** spending `" + (returnArray[1] * amountOfStocks) + "` :coin:")
								.queue();
					} else {
						event.getHook().sendMessage("Something went wrong!\nCheck your balance or Try Again!").queue();
					}
				} else {
					event.getHook().sendMessage("Company not valid!").queue();
					return;
				}
			}

			// Sell Stocks Command
			else if (subcommandName.equals("sell")) {
				String companyName = event.getOption("company").getAsString().toUpperCase();
				if (StocksHandler.getInstance().isCompanyValid(companyName)) {
					Company selectedCompany = Company.valueOf(companyName);
					int amountOfStocks = 0;
					try {
						amountOfStocks = event.getOption("amount").getAsInt();
					} catch (Exception e) {
						event.getHook().sendMessage("You can't sell that much number of stocks").queue();
						return;
					}
					if (amountOfStocks <= 0) {
						event.getHook().sendMessage("You can't sell negative numbers of stocks :face_with_raised_eyebrow:").queue();
						return;
					}
					int[] returnArray = StocksHandler.getInstance().sellStock(selectedCompany, amountOfStocks,
							event.getUser().getIdLong());
					if (returnArray[0] == 1) {
						event.getHook()
								.sendMessage("You sold `" + amountOfStocks + "` shares of **" + selectedCompany.toString()
										+ "** getting `" + (returnArray[1] * amountOfStocks) + "` :coin:")
								.queue();

					} else {
						event.getHook().sendMessage("Something went wrong!\nCheck your portfolio and Try again!").queue();
					}
				} else {
					event.getHook().sendMessage("Company not valid!").queue();
					return;
				}
			}
			return;
		}

		if (random.nextInt(BOUND) == 1) {
			CaptchaService.getInstance().sendCaptcha(event);
			return;
		}

		// guess command
		else if (event.getName().equals("guess")) {
			String commandName = event.getSubcommandName();
			if (commandName != null && commandName.equals("map")) {
				boolean isAdded = MapGameHandler.getInstance().addGame(event);
				if (isAdded) {
					gameEndService.schedule(
							new MapGameEndRunnable(
									MapGameHandler.getInstance().getGameMap()
											.get(event.getChannel().getIdLong()),
									event.getChannel().getIdLong()),
							30, TimeUnit.SECONDS);
				}
				return;
			} else if (commandName != null && commandName.equals("logo")) {
				boolean isAdded = LogoGameHandler.getInstance().addGame(event);
				if (isAdded) {
					gameEndService.schedule(
							new LogoGameEndRunnable(
									LogoGameHandler.getInstance().getGameMap()
											.get(event.getChannel().getIdLong()),
									event.getChannel().getIdLong()),
							30, TimeUnit.SECONDS);
				}
				return;
			} else {
				boolean isAdded = FlagGameHandler.getInstance().addGame(event);
				if (isAdded) {
					gameEndService.schedule(new FlagGameEndRunnable(
							FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
							event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
				}
				return;
			}
		}

		// Admin commands
		// show_server_count command (private access)
		else if (event.getName().equals("show_server_count")) {
			event.getHook().sendMessage("Total Servers in : " + event.getJDA().getGuilds().size()).queue();
		}

		else if (event.getName().equals("reset_coins")) {
			String user_id = event.getOption("user_id").getAsString();
			long coinsDedecuted = CoinDao.getInstance().resetUserCoins(Long.parseLong(user_id));
			event.getHook().sendMessage("Deducted " + coinsDedecuted + " coins from the User").queue();
		}

		// send_dm (private access)
		else if (event.getName().equals("send_dm")) {
			String user_id = event.getOption("user_id").getAsString();
			String message = event.getOption("message").getAsString();
			event.getJDA().retrieveUserById(Long.parseLong(user_id)).queue(user -> {
				user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
			});
			event.getHook().sendMessage("Message Sent").queue();
		}

		// unblock users
		else if (event.getName().equals("unblock")) {
			String user_id = event.getOption("user_id").getAsString();
			CaptchaService.getInstance().removeBlock(Long.parseLong(user_id));
			event.getHook().sendMessage("Unblocked User").queue();
			return;
		}
	}

	/*
	 * Button Interactions Listener
	 */
	@Override
	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		super.onButtonInteraction(event);

		if (CaptchaService.getInstance().isUserBanned(event.getUser().getIdLong())) {
			event.reply("You are banned from using bot").setEphemeral(true).queue();
			return;
		}

		if (CaptchaService.getInstance().userHasCaptched(event.getUser().getIdLong())) {
			event.reply("Solve the captcha first").setEphemeral(true).queue();
			return;
		}

		if (event.getComponentId().startsWith("punchSelection")) {
			String selectedOptionIso = event.getComponentId().split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.PUNCH, selectedOptionIso);
			return;
		} else if (event.getComponentId().startsWith("kickSelection")) {
			String selectedOptionIso = event.getComponentId().split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.KICK, selectedOptionIso);
			return;
		} else if (event.getComponentId().startsWith("refreshMarket")) {
			long lastUpdatedSince = Long.parseLong(event.getComponentId().split("_")[1]);
			long curentTime = System.currentTimeMillis();
			if (curentTime - lastUpdatedSince < 30_000) {
				event.reply("Try again in " + TimeFormat.RELATIVE.atTimestamp(lastUpdatedSince + 30_000)).setEphemeral(true)
						.queue();
			} else {
				event.editMessageEmbeds(StocksHandler.getInstance().getStockList())
						.setActionRow(Button.primary("refreshMarket_" + System.currentTimeMillis(),
								Emoji.fromEmote("refresh", 1209076086185656340l, false)))
						.queue();
			}
			return;
		} else if (event.getComponentId().startsWith("stockTransactions")) {
			int page = Integer.parseInt(event.getComponentId().split("_")[1]);
			MessageEmbed eb = StocksHandler.getInstance().getTransactionsEmbed(event.getUser().getIdLong(), page);
			event.replyEmbeds(eb).setEphemeral(true).queue();
			return;
		}

		if (event.getComponentId().equals("delete_data")) {

			LocalDateTime messageCreationTime = event.getMessage().getTimeCreated().toLocalDateTime();
			LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("GMT"));
			long timeDifference = Duration.between(messageCreationTime, currentTime).toMillis();

			if (timeDifference >= 60000l) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setDescription("Too late");
				eb.setColor(new Color(252, 209, 42));
				event.replyEmbeds(eb.build()).queue();
			} else {
				CoinDao.getInstance().deleteData(event.getUser().getIdLong());
				StocksDao.getInstance().deleteStocksData(event.getUser().getIdLong());
				StocksTransactionsDao.getInstance().deleteTransactionData(event.getUser().getIdLong());
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("Delete Confirmed");
				eb.setDescription("Data deleted");
				eb.setColor(Color.GREEN);
				event.replyEmbeds(eb.build()).queue();
			}
			return;
		}

		else if (event.getComponentId().equals("skipButton")) {
			event.deferReply().queue();
			if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (event.getComponentId().equals("checkRegionButton")) {
			event.deferReply().setEphemeral(true).queue();
			String response = RegionHandler.getInstance().requestForHint(event);
			response = (response != null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();
			return;
		}

		else if (event.getComponentId().equals("skipMap")) {
			event.deferReply().queue();
			if (MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				MapGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (event.getComponentId().equals("skipLogo")) {
			event.deferReply().queue();
			if (LogoGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				LogoGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (event.getComponentId().equals("rejectBattle")) {
			FightHandler.getInstance().handleCancelButton(event);
			return;
		}

		else if (event.getComponentId().equals("acceptBattle")) {
			FightHandler.getInstance().handleAcceptButton(event);
			return;
		}

		else if (event.getComponentId().equals("punchInBattle")) {
			FightHandler.getInstance().handlePunchButton(event);
			return;
		}

		else if (event.getComponentId().equals("kickInBattle")) {
			FightHandler.getInstance().handleKickButton(event);
			return;
		}

		else if (event.getComponentId().equals("runInBattle")) {
			FightHandler.getInstance().handleRunButton(event);
			return;
		}

		if (random.nextInt(BOUND) == 1) {
			event.deferReply().queue();
			CaptchaService.getInstance().sendCaptcha(event);
			return;
		}

		else if (event.getComponentId().startsWith("playAgainFlag")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (event.getComponentId().startsWith("playAgainMap")) {
			boolean isAdded = MapGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(
						new MapGameEndRunnable(
								MapGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (event.getComponentId().equals("playAgainLogo")) {
			boolean isAdded = LogoGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(
						new LogoGameEndRunnable(
								LogoGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
			return;

		}
	}
}