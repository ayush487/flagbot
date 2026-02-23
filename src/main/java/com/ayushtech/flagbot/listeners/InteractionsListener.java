package com.ayushtech.flagbot.listeners;

import java.util.Random;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.crossword.CrosswordGameHandler;
import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.guessGame.flag.RegionHandler;
import com.ayushtech.flagbot.memoflip.MemoflipHandler;
import com.ayushtech.flagbot.race.RaceHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.LevelAppendService;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.UserService;
import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.services.VotingService;
import com.ayushtech.flagbot.utils.LeaderboardHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionsListener extends ListenerAdapter {

	private ChannelService channelService;
	private Random random;
	private final int BOUND = 250;

	public InteractionsListener() {
		super();
		channelService = ChannelService.getInstance();
		random = new Random();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

		MetricService.getInstance().registerCommandData(event);

		if (CaptchaService.getInstance().isUserBanned(event.getUser().getIdLong())) {
			event.reply("You are banned from using bot").setEphemeral(true).queue();
			return;
		}

		if (CaptchaService.getInstance().userHasCaptched(event.getUser().getIdLong())) {
			event.reply("Solve the captcha first").setEphemeral(true).queue();
			return;
		}

		String slashCommandName = event.getName();

		if (slashCommandName.equals("disable")) {
			UtilService.getInstance().handleDisableCommand(event);
			return;
		}

		else if (slashCommandName.equals("disable_all_channels")) {
			UtilService.getInstance().handleDisableAllCommand(event);
			return;
		}

		else if (slashCommandName.equals("enable")) {
			UtilService.getInstance().handleEnableCommand(event);
			return;
		}

		else if (slashCommandName.equals("staff_poll")) {
			PrivateServerService.getInstance().handlePollCommand(event);
			return;
		}

		boolean isCommandsDisabled = channelService.isChannelDisabled(event.getChannel().getIdLong());

		if (isCommandsDisabled) {
			event.deferReply().setEphemeral(true).queue();
			event.getHook().sendMessage("Commands are disabled in this channel").setEphemeral(true).queue();
			return;
		}

		if (slashCommandName.equals("battle")) {
			event.reply("This command has been removed.").queue();
			return;
		}

		else if (slashCommandName.equals("race")) {
			RaceHandler.getInstance().handleRaceCommand(event);
			return;
		}

		else if (slashCommandName.equals("crossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordSlashCommand(event);
			return;
		}

		else if (slashCommandName.equals("extra_words")) {
			CrosswordGameHandler.getInstance().handleExtraWordCommand(event);
			return;
		}

		event.deferReply().queue();

		if (slashCommandName.equals("memoflip")) {
			MemoflipHandler.getInstance().handleMemoflipCommand(event);
			return;
		}

		else if (slashCommandName.equals("vote")) {
			UtilService.getInstance().handleVoteCommand(event.getHook());
			return;
		}

		else if (slashCommandName.equals("daily")) {
			UserService.getInstance().handleDailyCommand(event);
			return;
		}

		else if (slashCommandName.equals("leaderboards")) {
			LeaderboardHandler.getInstance().handleLeaderboardCommand(event);
			return;
		}

		else if (slashCommandName.equals("invite")) {
			UtilService.getInstance().handleInviteCommand(event.getHook());
			return;
		}

		else if (slashCommandName.equals("patreon")) {
			PatreonService.getInstance().handlePatreonCommand(event);
			return;
		}

		else if (slashCommandName.equals("help")) {
			UtilService.getInstance().handleHelpCommand(event);
			return;
		}

		else if (slashCommandName.equals("support")) {
			UtilService.getInstance().handleSupportCommand(event.getHook());
			return;
		}

		else if (slashCommandName.equals("language")) {
			LanguageService.getInstance().handleLanguageCommand(event);
			return;
		}

		else if (slashCommandName.equals("delete_my_data")) {
			UtilService.getInstance().handleDataDeletionRequest(event.getUser(), event.getHook());
			return;
		}

		else if (slashCommandName.equals("balance")) {
			UtilService.getInstance().handleBalanceCommand(event.getUser(), event.getHook());
			return;
		}

		else if (slashCommandName.equals("give")) {
			UtilService.getInstance().handleGiveCommands(event);
			return;
		}


		// Commenting Captcha for now
		if (random.nextInt(BOUND) == 1) {
			PatreonService.getInstance().sendPatreonRequestMessage(event.getChannel());
			// CaptchaService.getInstance().sendCaptcha(event);
			// return;
		}

		if (slashCommandName.equals("guess")) {
			UtilService.getInstance().handleGuessComnmands(event);
			return;
		}

		else if (slashCommandName.equals("atlas")) {
			UtilService.getInstance().handleAtlasCommands(event);
			return;
		}

		// Admin commands
		else if (slashCommandName.equals("show_server_count")) {
			PrivateServerService.getInstance().updateEmbedDescription();
			event.getHook().sendMessage("Total Servers in : " + event.getJDA().getGuilds().size()).queue();
		}

		else if (slashCommandName.equals("reset_coins")) {
			String user_id = event.getOption("user_id").getAsString();
			long coinsDedecuted = CoinDao.getInstance().resetUserCoins(Long.parseLong(user_id));
			event.getHook().sendMessage("Deducted " + coinsDedecuted + " coins from the User").queue();
		}

		else if (slashCommandName.equals("send_dm")) {
			String user_id = event.getOption("user_id").getAsString();
			String message = event.getOption("message").getAsString();
			event.getJDA().retrieveUserById(Long.parseLong(user_id)).queue(user -> {
				user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
			});
			event.getHook().sendMessage("Message Sent").queue();
		}

		else if (slashCommandName.equals("unblock")) {
			String user_id = event.getOption("user_id").getAsString();
			CaptchaService.getInstance().removeBlock(Long.parseLong(user_id));
			event.getHook().sendMessage("Unblocked User").queue();
			return;
		}

		else if (slashCommandName.equals("metrics")) {
			MetricService.getInstance().handleMetricCommand(event);
			return;
		}

		else if (slashCommandName.equals("recent_votes")) {
			VotingService.getInstance().handleVoteInfoCommand(event);
			return;
		}

		else if (slashCommandName.equals("botinfo")) {
			UtilService.getInstance().handleBotCommands(event);
			return;
		}

		else if (slashCommandName.equals("add_words")) {
			UtilService.getInstance().handleAddWordCommand(event);
			return;
		}

		else if (slashCommandName.equals("remove_words")) {
			UtilService.getInstance().handleRemoveWordCommand(event);
			return;
		}

		else if (slashCommandName.equals("view_level")) {
			CrosswordGameHandler.getInstance().handleViewLevelCommand(event);
			return;
		}

		else if (slashCommandName.equals("add_level")) {
			LevelAppendService.getInstance().handleLevelAddCommand(event);
			return;
		}
	}

	/*
	 * Button Interactions Listener
	 */
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		super.onButtonInteraction(event);

		String buttonCommandId = event.getComponentId();

		MetricService.getInstance().registerCommandData(event);

		if (CaptchaService.getInstance().isUserBanned(event.getUser().getIdLong())) {
			event.reply("You are banned from using bot").setEphemeral(true).queue();
			return;
		}

		if (CaptchaService.getInstance().userHasCaptched(event.getUser().getIdLong())) {
			event.reply("Solve the captcha first").setEphemeral(true).queue();
			return;
		}

		if (buttonCommandId.equals("raceCancel")) {
			RaceHandler.getInstance().handleCancelRace(event);
			return;
		} else if (buttonCommandId.equals("raceJoin")) {
			RaceHandler.getInstance().handleJoinRace(event);
			return;
		} else if (buttonCommandId.equals("raceStart")) {
			RaceHandler.getInstance().handleStartRace(event);
			return;
		} else if (buttonCommandId.equals("viewPatreonPerks")) {
			PatreonService.getInstance().showPatreonPerks(event);
			return;
		}

		if (buttonCommandId.startsWith("joinDistance")) {
			GuessDistanceHandler.getInstance().handleJoinCommand(event);
			return;
		}

		else if (buttonCommandId.startsWith("newCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordButton(event);
			return;
		} else if (buttonCommandId.startsWith("quitCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordQuitButton(event);
			return;
		} else if (buttonCommandId.startsWith("cancelCrossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordCancelButton(event);
			return;
		} else if (buttonCommandId.startsWith("hintCrossword")) {
			CrosswordGameHandler.getInstance().handleHintButton(event);
			return;
		} else if (buttonCommandId.startsWith("shuffleCrossword")) {
			CrosswordGameHandler.getInstance().handleShuffleButton(event);
			return;
		} else if (buttonCommandId.startsWith("extraWords")) {
			CrosswordGameHandler.getInstance().handleExtraWordButton(event);
			return;
		} else if (buttonCommandId.startsWith("claimExtraWords")) {
			UserService.getInstance().claimExtraWordCoins(event);
			return;
		} else if (buttonCommandId.startsWith("cancelThenNewCrossword")) {
			CrosswordGameHandler.getInstance().handleCancelThenNewCrosswordButton(event);
			return;
		}

		// else if (buttonCommandId.startsWith("stockTransactions")) {
		// 	StocksHandler.getInstance().handleStockTransactionButton(event);
		// 	return;
		// } 
		else if (buttonCommandId.startsWith("accelerate_")) {
			RaceHandler.getInstance().handleAccelerate(event);
			return;
		} else if (buttonCommandId.startsWith("correct")) {
			RaceHandler.getInstance().handleCorrectSelection(event);
			return;
		} else if (buttonCommandId.startsWith("wrong")) {
			RaceHandler.getInstance().handleWrongSelection(event);
			return;
		} else if (buttonCommandId.startsWith("cardButton")) {
			MemoflipHandler.getInstance().handleCardButton(event);
			return;
		} else if (buttonCommandId.startsWith("selectContinent")) {
			ContinentGameHandler.getInstance().handleSelection(event);
			return;
		} else if (buttonCommandId.startsWith("changeDistanceUnit")) {
			GuessDistanceHandler.getInstance().handleChangeUnitCommand(event);
			return;
		} else if (buttonCommandId.startsWith("cancelDistance")) {
			GuessDistanceHandler.getInstance().handleCancelCommand(event);
			return;
		} else if (buttonCommandId.startsWith("startDistance")) {
			GuessDistanceHandler.getInstance().handleStartCommand(event);
			return;
		} else if (buttonCommandId.startsWith("viewPlace")) {
			LocationGameHandler.getInstance().handleViewPlaceButton(event);
			return;
		} else if (buttonCommandId.startsWith("skipLocation")) {
			LocationGameHandler.getInstance().handleSkipButton(event);
			return;
		} else if (buttonCommandId.startsWith("selectLocation")) {
			LocationGameHandler.getInstance().handleSelection(event);
			return;
		} else if (buttonCommandId.startsWith("pollUpvote")) {
			PrivateServerService.getInstance().handlePollUpvote(event);
			return;
		} else if (buttonCommandId.startsWith("pollDownvote")) {
			PrivateServerService.getInstance().handlePollDownvote(event);
			return;
		} else if (buttonCommandId.startsWith("pollViewVotes")) {
			PrivateServerService.getInstance().handlePollViewVotes(event);
			return;
		} else if (buttonCommandId.startsWith("pollRemovevote")) {
			PrivateServerService.getInstance().handleRemoveVote(event);
			return;
		} else if (buttonCommandId.startsWith("delete_data")) {
			UtilService.getInstance().handleConfirmDeleteButton(event);
			return;
		} else if (buttonCommandId.startsWith("checkRegionButton")) {
			RegionHandler.getInstance().handleRegionButton(event);
			return;
		} else if (buttonCommandId.startsWith("help")) {
			UtilService.getInstance().handleHelpButton(event);
			return;
		} else if(buttonCommandId.startsWith("lb_")) {
			LeaderboardHandler.getInstance().handleLeaderboardButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("skipGuess")) {
			GuessGameHandler.getInstance().handleSkipRequest(event);
			return;
		}

		else if (buttonCommandId.equals("cancelAtlas")) {
			AtlasGameHandler.getInstance().handleCancelStartButton(event);
			return;
		}
		// Commenting Captcha for now
		if (random.nextInt(BOUND) == 1) {
			PatreonService.getInstance().sendPatreonRequestMessage(event.getChannel());
			// event.deferReply().queue();
			// CaptchaService.getInstance().sendCaptcha(event);
			// return;
		}

		if (buttonCommandId.startsWith("playAgainFlag")) {
			GuessGameHandler.getInstance().handlePlayFlagButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainMap")) {
			GuessGameHandler.getInstance().handlePlayMapButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainLogo")) {
			GuessGameHandler.getInstance().handlePlayLogoButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainPlace")) {
			GuessGameHandler.getInstance().handlePlayPlaceButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainCapital")) {
			GuessGameHandler.getInstance().handlePlayCapitalButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainStateFlag_")) {
			GuessGameHandler.getInstance().handlePlayStateFlagButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainContinent")) {
			ContinentGameHandler.getInstance().handlePlayCommand(event);
			return;
		}

		else if (buttonCommandId.startsWith("playAgainLocation")) {
			LocationGameHandler.getInstance().handleStartGameCommand(event);
			return;
		}

		else if (buttonCommandId.startsWith("joinAtlas")) {
			AtlasGameHandler.getInstance().handleJoinButton(event);
			return;
		}

	}
}