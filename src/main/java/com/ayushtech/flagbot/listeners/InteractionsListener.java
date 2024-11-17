package com.ayushtech.flagbot.listeners;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.capital.CapitalGameHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.fight.Damage;
import com.ayushtech.flagbot.game.fight.FightHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.memoflip.MemoflipHandler;
import com.ayushtech.flagbot.race.RaceHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.services.VotingService;
import com.ayushtech.flagbot.stocks.StocksHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

		if (event.getName().equals("disable")) {
			UtilService.getInstance().handleDisableCommand(event);
			return;
		}

		else if (event.getName().equals("disable_all_channels")) {
			UtilService.getInstance().handleDisableAllCommand(event);
			return;
		}

		else if (event.getName().equals("enable")) {
			UtilService.getInstance().handleEnableCommand(event);
			return;
		}

		else if (event.getName().equals("staff_poll")) {
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

		if (event.getName().equals("memoflip")) {
			MemoflipHandler.getInstance().handleMemoflipCommand(event);
			return;
		}

		else if (event.getName().equals("vote")) {
			UtilService.getInstance().handleVoteCommand(event.getHook());
			return;
		}

		else if (event.getName().equals("leaderboards")) {
			UtilService.getInstance().handleLeaderboardCommand(event);
			return;
		}

		else if (event.getName().equals("invite")) {
			UtilService.getInstance().handleInviteCommand(event.getHook());
			return;
		}

		else if (event.getName().equals("patreon")) {
			PatreonService.getInstance().handlePatreonCommand(event);
			return;
		}

		else if (event.getName().equals("help")) {
			UtilService.getInstance().handleHelpCommand(event.getHook());
			return;
		}

		else if (event.getName().equals("support")) {
			UtilService.getInstance().handleSupportCommand(event.getHook());
			return;
		}

		else if (event.getName().equals("language")) {
			LanguageService.getInstance().handleLanguageCommand(event);
			return;
		}

		else if (event.getName().equals("delete_my_data")) {
			UtilService.getInstance().handleDataDeletionRequest(event.getUser(), event.getHook());
			return;
		}

		else if (event.getName().equals("balance")) {
			UtilService.getInstance().handleBalanceCommand(event.getUser(), event.getHook());
			return;
		}

		else if (event.getName().equals("give")) {
			UtilService.getInstance().handleGiveCommands(event);
			return;
		}

		else if (event.getName().equals("stocks")) {
			UtilService.getInstance().handleStockCommands(event);
			return;
		}

		// Commenting Captcha for now
		// if (random.nextInt(BOUND) == 1) {
		// CaptchaService.getInstance().sendCaptcha(event);
		// return;
		// }

		else if (event.getName().equals("guess")) {
			UtilService.getInstance().handleGuessComnmands(event);
			return;
		}

		// Admin commands
		else if (event.getName().equals("show_server_count")) {
			event.getHook().sendMessage("Total Servers in : " + event.getJDA().getGuilds().size()).queue();
		}

		else if (event.getName().equals("reset_coins")) {
			String user_id = event.getOption("user_id").getAsString();
			long coinsDedecuted = CoinDao.getInstance().resetUserCoins(Long.parseLong(user_id));
			event.getHook().sendMessage("Deducted " + coinsDedecuted + " coins from the User").queue();
		}

		else if (event.getName().equals("send_dm")) {
			String user_id = event.getOption("user_id").getAsString();
			String message = event.getOption("message").getAsString();
			event.getJDA().retrieveUserById(Long.parseLong(user_id)).queue(user -> {
				user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
			});
			event.getHook().sendMessage("Message Sent").queue();
		}

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
			FightHandler.getInstance().handleSelection(event, Damage.PUNCH, commandId.split("-")[1]);
			return;
		} else if (commandId.startsWith("joinDistance")) {
			GuessDistanceHandler.getInstance().handleJoinCommand(event);
			return;
		} else if (commandId.startsWith("kickSelection")) {
			FightHandler.getInstance().handleSelection(event, Damage.KICK, commandId.split("-")[1]);
			return;
		} else if (commandId.startsWith("refreshMarket")) {
			StocksHandler.getInstance().handleRefreshMarketButton(event);
			return;
		} else if (commandId.startsWith("stockTransactions")) {
			StocksHandler.getInstance().handleStockTransactionButton(event);
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
		} else if (commandId.startsWith("delete_data")) {
			UtilService.getInstance().handleConfirmDeleteButton(event);
			return;
		}

		else if (commandId.equals("skipButton")) {
			UtilService.getInstance().handleSkipFlagButton(event);
			return;
		}

		else if (commandId.equals("checkRegionButton")) {
			RegionHandler.getInstance().handleRegionButton(event);
			return;
		}

		else if (commandId.equals("skipMap")) {
			UtilService.getInstance().handleSkipMapButton(event);
			return;
		}

		else if (commandId.equals("skipLogo")) {
			UtilService.getInstance().handleSkipLogoButton(event);
			return;
		}

		else if (commandId.equals("skipCapital")) {
			event.deferReply().queue();
			CapitalGameHandler.getInstance().handleSkipRequest(event);
			return;
		}

		else if (commandId.equals("skipPlace")) {
			UtilService.getInstance().handleSkipPlaceButton(event);
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
		// event.deferReply().queue();
		// CaptchaService.getInstance().sendCaptcha(event);
		// return;
		// }

		else if (commandId.startsWith("playAgainFlag")) {
			UtilService.getInstance().handlePlayFlagButton(event);
			return;
		}

		else if (commandId.startsWith("playAgainMap")) {
			UtilService.getInstance().handlePlayMapButton(event);
			return;
		}

		else if (commandId.startsWith("playAgainLogo")) {
			UtilService.getInstance().handlePlayLogoButton(event);
			return;
		}

		else if (commandId.startsWith("playAgainPlace")) {
			UtilService.getInstance().handlePlayPlaceButton(event);
			return;
		}

		else if (commandId.startsWith("playAgainContinent")) {
			ContinentGameHandler.getInstance().handlePlayCommand(event);
			return;
		}

		else if (commandId.startsWith("playAgainCapital")) {
			CapitalGameHandler.getInstance().handlePlayCommand(event);
			return;
		}

		else if (commandId.startsWith("playAgainLocation")) {
			LocationGameHandler.getInstance().handleStartGameCommand(event);
			return;
		}

	}
}