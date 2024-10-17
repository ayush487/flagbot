package com.ayushtech.flagbot.listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.StocksDao;
import com.ayushtech.flagbot.dbconnectivity.StocksTransactionsDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.fight.Damage;
import com.ayushtech.flagbot.game.fight.FightHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.game.logo.LogoGameEndRunnable;
import com.ayushtech.flagbot.game.logo.LogoGameHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.game.place.PlaceGameEndRunnable;
import com.ayushtech.flagbot.game.place.PlaceGameHandler;
import com.ayushtech.flagbot.memoflip.MemoflipHandler;
import com.ayushtech.flagbot.race.RaceHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.CoinTransferService;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.VotingService;
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

	private ChannelService channelService;
	// private Random random;
	// private final int BOUND = 500;

	public InteractionsListener() {
		super();
		channelService = ChannelService.getInstance();
		// random = new Random();
	}

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

		MetricService.getInstance().registerCommandData(event);

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
		} else if (event.getName().equals("staff_poll")) {
			PrivateServerService.getInstance().handlePollCommand(event);
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

		else if (event.getName().equals("race")) {
			RaceHandler.getInstance().handleRaceCommand(event);
			return;
		}

		event.deferReply().queue();

		// MemoFlip command
		if (event.getName().equals("memoflip")) {
			MemoflipHandler.getInstance().handleMemoflipCommand(event);
			return;
		}

		else if (event.getName().equals("vote")) {
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

		else if (event.getName().equals("patreon")) {
			PatreonService.getInstance().handlePatreonCommand(event);
			return;
		}

		// help command
		else if (event.getName().equals("help")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setTitle("Commands");
			eb.setColor(new Color(255, 153, 51)); // rgb (255,153,51)
			eb.setDescription(
					"**__Guess Commands__**\n`/guess flag` : Start a flag guessing game in the channel\n`/guess map` : Start a map guessing game in the channel\n`/guess logo` : Start a logo guessing game in the channel\n`/guess place` : Start a place guessing game in the channel\n`/guess continent` : State a continent guessing game in the channel\n`/guess location` : Start a location guessing game in the channel (**Only for voters**)\n`/guess distance` : A Multiplayer mode in which users can guess distance marked on the map (**Only for voters**)\n__Options__ :\n`mode` : Choose the mode you want to play :Soverign Only, Non-Soverign Only, All Countries (Soverign Only if not selected)\n`continent` : Specify the continent for the flag game\n`rounds` : Enter the number of rounds you want to play (maximum it would be 15) (optional)\n`include_non_soverign_countries` : True or False to include non soverign countries (false if not selected)\n`unit` : Enter your preffered unit (kilometer or miles)\n__Note__ : `Specifying continent will nullify mode selection and mode will automatically become 'All Countries'.`");
			eb.addField("__General Commands__",
					"`/leaderboards` : Check the global leaderboard (Upto top 25)\n`/invite` : Invite the bot to your server\n`/language set` : Set language for the server (Only work for flag and map guessers)\n`/language info` : See your server language and other supported languages\n`/language remove` : Remove server language\n`/disable` : Disable the commands in the given channel\n`/enable` : Enable the commands in the given channel\n`/disable_all_channels` : Disable the commands for all the channels of the server\n`/delete_my_data` : Will Delete your data from the bot\n`/balance` : You can see your coins and rank\n`/give coins` : Send coins to other users.\n`/vote` : Vote for us and get rewards\n`/patreon` : Show information about Patreon Membership",
					false);
			eb.addField("__Battle Command__",
					"`/battle` : Start a 1v1 battle between two users.\n**__Options__**\n**opponent** : Mention the user with whom you wanna battle.\n**bet** : Amout to bet in the battle (optional)",
					false);
			eb.addField("__Memoflip Game__",
					"`/memoflip easy` : Start a memoflip game in easy mode (8 cards)\n`/memoflip medium` : Start a memoflip game in medium mode (16 cards)\n`/memoflip hard` : Start a memoflip game in hard mode (24 cards)",
					false);
			eb.addField("__Race Command__",
					"`/race flags` : Start a race in the following channel of Flag mode\n`/race maps` : Start a race in the following channel of Flag mode\n`/race logo` : Start a race in the following channel of Logo mode\n`/race maths` : Start a race in the following channel of maths mode",
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
			event.getHook().sendMessageEmbeds(eb.build())
					.queue();
		}

		else if (event.getName().equals("language")) {
			LanguageService.getInstance().handleLanguageCommand(event);
			return;
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

		// Commenting Captcha for now
		// if (random.nextInt(BOUND) == 1) {
		// 	CaptchaService.getInstance().sendCaptcha(event);
		// 	return;
		// }

		// guess command
		else if (event.getName().equals("guess")) {
			String commandName = event.getSubcommandName();
			if (commandName.equals("map")) {
				boolean isAdded = MapGameHandler.getInstance().addGame(event);
				if (isAdded) {
					GameEndService.getInstance().scheduleEndGame(
							new MapGameEndRunnable(
									MapGameHandler.getInstance().getGameMap()
											.get(event.getChannel().getIdLong()),
									event.getChannel().getIdLong()),
							30, TimeUnit.SECONDS);
				}
				return;
			} else if (commandName.equals("logo")) {
				boolean isAdded = LogoGameHandler.getInstance().addGame(event);
				if (isAdded) {
					GameEndService.getInstance().scheduleEndGame(
							new LogoGameEndRunnable(
									LogoGameHandler.getInstance().getGameMap()
											.get(event.getChannel().getIdLong()),
									event.getChannel().getIdLong()),
							30, TimeUnit.SECONDS);
				}
				return;
			} else if (commandName.equals("flag")) {
				boolean isAdded = FlagGameHandler.getInstance().addGame(event);
				if (isAdded) {
					GameEndService.getInstance().scheduleEndGame(new FlagGameEndRunnable(
							FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
							event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
				}
				return;
			} else if (commandName.equals("place")) {
				boolean isAdded = PlaceGameHandler.getInstance().addGame(event);
				if (isAdded) {
					GameEndService.getInstance().scheduleEndGame(new PlaceGameEndRunnable(
							PlaceGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
							event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
				}
				return;
			} else if (commandName.equals("distance")) {
				GuessDistanceHandler.getInstance().handleNewGameCommand(event);
				return;
			} else if (commandName.equals("location")) {
				LocationGameHandler.getInstance().handleStartGameCommand(event);
				return;
			} else {
				ContinentGameHandler.getInstance().handlePlayCommand(event);
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

		else if (event.getName().equals("metrics")) {
			MetricService.getInstance().handleMetricCommand(event);
			return;
		}

		// voters info
		else if (event.getName().equals("recent_votes")) {
			VotingService.getInstance().handleVoteInfoCommand(event);
			return;
		}
	}

	/*
	 * Button Interactions Listener
	 */
	@Override
	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		super.onButtonInteraction(event);

		String commandId = event.getComponentId();

		MetricService.getInstance().registerCommandData(event);

		if (CaptchaService.getInstance().isUserBanned(event.getUser().getIdLong())) {
			event.reply("You are banned from using bot").setEphemeral(true).queue();
			return;
		}

		if (CaptchaService.getInstance().userHasCaptched(event.getUser().getIdLong())) {
			event.reply("Solve the captcha first").setEphemeral(true).queue();
			return;
		}

		if (commandId.equals("raceCancel")) {
			RaceHandler.getInstance().handleCancelRace(event);
			return;
		} else if (commandId.equals("raceJoin")) {
			RaceHandler.getInstance().handleJoinRace(event);
			return;
		} else if (commandId.equals("raceStart")) {
			RaceHandler.getInstance().handleStartRace(event);
			return;
		} else if (commandId.equals("viewPatreonPerks")) {
			PatreonService.getInstance().showPatreonPerks(event);
			return;
		}

		if (commandId.startsWith("punchSelection")) {
			String selectedOptionIso = commandId.split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.PUNCH, selectedOptionIso);
			return;
		} else if (commandId.startsWith("joinDistance")) {
			GuessDistanceHandler.getInstance().handleJoinCommand(event);
			return;
		} else if (commandId.startsWith("kickSelection")) {
			String selectedOptionIso = commandId.split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.KICK, selectedOptionIso);
			return;
		} else if (commandId.startsWith("refreshMarket")) {
			long lastUpdatedSince = Long.parseLong(commandId.split("_")[1]);
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
		} else if (commandId.startsWith("stockTransactions")) {
			int page = Integer.parseInt(commandId.split("_")[1]);
			MessageEmbed eb = StocksHandler.getInstance().getTransactionsEmbed(event.getUser().getIdLong(), page);
			event.replyEmbeds(eb).setEphemeral(true).queue();
			return;
		} else if (commandId.startsWith("accelerate_")) {
			RaceHandler.getInstance().handleAccelerate(event);
			return;
		} else if (commandId.startsWith("correct")) {
			RaceHandler.getInstance().handleCorrectSelection(event);
			return;
		} else if (commandId.startsWith("wrong")) {
			RaceHandler.getInstance().handleWrongSelection(event);
			return;
		} else if (commandId.startsWith("cardButton")) {
			MemoflipHandler.getInstance().handleCardButton(event);
			return;
		} else if (commandId.startsWith("selectContinent")) {
			ContinentGameHandler.getInstance().handleSelection(event);
			return;
		} else if (commandId.startsWith("changeDistanceUnit")) {
			GuessDistanceHandler.getInstance().handleChangeUnitCommand(event);
			return;
		} else if (commandId.startsWith("cancelDistance")) {
			GuessDistanceHandler.getInstance().handleCancelCommand(event);
			return;
		} else if (commandId.startsWith("startDistance")) {
			GuessDistanceHandler.getInstance().handleStartCommand(event);
			return;
		} else if (commandId.startsWith("viewPlace")) {
			LocationGameHandler.getInstance().handleViewPlaceButton(event);
			return;
		} else if (commandId.startsWith("skipLocation")) {
			LocationGameHandler.getInstance().handleSkipButton(event);
			return;
		} else if (commandId.startsWith("selectLocation")) {
			LocationGameHandler.getInstance().handleSelection(event);
			return;
		} else if (commandId.startsWith("pollUpvote")) {
			PrivateServerService.getInstance().handlePollUpvote(event);
			return;
		} else if (commandId.startsWith("pollDownvote")) {
			PrivateServerService.getInstance().handlePollDownvote(event);
			return;
		} else if (commandId.startsWith("pollViewVotes")) {
			PrivateServerService.getInstance().handlePollViewVotes(event);
			return;
		} else if (commandId.startsWith("pollRemovevote")) {
			PrivateServerService.getInstance().handleRemoveVote(event);
			return;
		}

		if (commandId.equals("delete_data")) {

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

		else if (commandId.equals("skipButton")) {
			event.deferReply().queue();
			if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (commandId.equals("checkRegionButton")) {
			event.deferReply().setEphemeral(true).queue();
			String response = RegionHandler.getInstance().requestForHint(event);
			response = (response != null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();
			return;
		}

		else if (commandId.equals("skipMap")) {
			event.deferReply().queue();
			if (MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				MapGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (commandId.equals("skipLogo")) {
			event.deferReply().queue();
			if (LogoGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				LogoGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (commandId.equals("skipPlace")) {
			event.deferReply().queue();
			if (PlaceGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				PlaceGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
			return;
		}

		else if (commandId.equals("rejectBattle")) {
			FightHandler.getInstance().handleCancelButton(event);
			return;
		}

		else if (commandId.equals("acceptBattle")) {
			FightHandler.getInstance().handleAcceptButton(event);
			return;
		}

		else if (commandId.equals("punchInBattle")) {
			FightHandler.getInstance().handlePunchButton(event);
			return;
		}

		else if (commandId.equals("kickInBattle")) {
			FightHandler.getInstance().handleKickButton(event);
			return;
		}

		else if (commandId.equals("runInBattle")) {
			FightHandler.getInstance().handleRunButton(event);
			return;
		}

		// Commenting Captcha for now
		// if (random.nextInt(BOUND) == 1) {
		// 	event.deferReply().queue();
		// 	CaptchaService.getInstance().sendCaptcha(event);
		// 	return;
		// }

		else if (commandId.startsWith("playAgainFlag")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				GameEndService.getInstance().scheduleEndGame(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (commandId.startsWith("playAgainMap")) {
			boolean isAdded = MapGameHandler.getInstance().addGame(event);
			if (isAdded) {
				GameEndService.getInstance().scheduleEndGame(
						new MapGameEndRunnable(
								MapGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (commandId.startsWith("playAgainLogo")) {
			boolean isAdded = LogoGameHandler.getInstance().addGame(event);
			if (isAdded) {
				GameEndService.getInstance().scheduleEndGame(
						new LogoGameEndRunnable(
								LogoGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (commandId.startsWith("playAgainPlace")) {
			boolean isAdded = PlaceGameHandler.getInstance().addGame(event);
			if (isAdded) {
				GameEndService.getInstance().scheduleEndGame(
						new PlaceGameEndRunnable(
								PlaceGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
		}

		else if (commandId.startsWith("playAgainContinent")) {
			ContinentGameHandler.getInstance().handlePlayCommand(event);
			return;
		}

		else if (commandId.startsWith("playAgainLocation")) {
			LocationGameHandler.getInstance().handleStartGameCommand(event);
			return;
		}

	}
}