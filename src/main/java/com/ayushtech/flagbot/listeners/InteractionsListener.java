package com.ayushtech.flagbot.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.crossword.CrosswordGameHandler;
import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.gambling.CoinflipHandler;
import com.ayushtech.flagbot.gambling.MinesHandler;
import com.ayushtech.flagbot.gambling.SlotsHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.guessGame.flag.RegionHandler;
import com.ayushtech.flagbot.race.RaceHandler;
import com.ayushtech.flagbot.services.ChannelService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.LevelAppendService;
import com.ayushtech.flagbot.services.MetricService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.PrivateServerService;
import com.ayushtech.flagbot.services.UpdateReminder;
import com.ayushtech.flagbot.services.UserService;
import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.services.VotingService;
import com.ayushtech.flagbot.utils.Constants;
import com.ayushtech.flagbot.utils.LeaderboardHandler;
import com.ayushtech.flagbot.utils.PrefixCommandHandler;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionsListener extends ListenerAdapter {

	private ChannelService channelService;
	private Random random;
	private static String WEBHOOK_URL = "";
	private final long vote_notifs_channel = 1190982948804100108l;
	private Map<String, String> alternateNamesMap;
	private String[] keywords = { "link", "games", "download game" };
	private long privateServerId = 1465232854681129065l;
	private long newPledgeChannel = 1263027212194414644l;
	private long updatePledgeChannel = 1263027292322529301l;

	public InteractionsListener() {
		super();
		channelService = ChannelService.getInstance();
		random = new Random();
		alternateNamesMap = new HashMap<>();
		loadAlternateNames();
	}

	public static void setJoinUpdateWebhookUrl(String url) {
		WEBHOOK_URL = url;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		long channelId = event.getChannel().getIdLong();

		if (channelId == vote_notifs_channel) {
			VotingService.getInstance().handleVote(event);
			return;
		}

		else if (channelId == newPledgeChannel || channelId == updatePledgeChannel) {
			String patreonId = event.getMessage().getContentDisplay();
			PatreonService.getInstance().addNewPatron(event.getJDA(), Long.parseLong(patreonId));
		}

		if (event.getAuthor().isBot())
			return;

		if (event.isFromGuild() && event.getGuild().getIdLong() == privateServerId
				&& isContainKeyword(event.getMessage().getContentDisplay().toLowerCase())) {
			PrivateServerService.getInstance().handleMessage(event);
		}

		boolean isCommandsDisabled = channelService.isChannelDisabled(event.getChannel().getIdLong());

		if (isCommandsDisabled)
			return;

		String messageText = event.getMessage().getContentDisplay();

		String prefix = PrefixCommandHandler.getInstance().getPrefix(event.getGuild().getIdLong());

		if (messageText.startsWith(prefix)) {
			PrefixCommandHandler.getInstance().handlePrefixCommand(event, prefix);
		} else if (messageText.startsWith(Constants.DEFAULT_PREFIX)) {
			PrefixCommandHandler.getInstance().handlePrefixCommand(event, Constants.DEFAULT_PREFIX);
		}

		if (CrosswordGameHandler.getInstance().isActiveGame(event.getAuthor().getIdLong(),
				event.getChannel().getIdLong())) {
			CrosswordGameHandler.getInstance().inspectAnswer(event);
		}

		if (GuessDistanceHandler.getInstance().isActiveGameInChannel(channelId)) {
			GuessDistanceHandler.getInstance().handleGuess(messageText, event);
		}

		if (AtlasGameHandler.getInstance().isGameExist(channelId)) {
			AtlasGameHandler.getInstance().handleAnswer(messageText, event);
		}

		if (alternateNamesMap.containsKey(messageText.toLowerCase())) {
			messageText = alternateNamesMap.get(messageText.toLowerCase());
		}
		if (GuessGameHandler.getInstance().isActiveGame(channelId)) {
			GuessGameHandler.getInstance().handleGuess(messageText, event);
			return;
		}
		return;
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

		MetricService.getInstance().registerCommandData(event);

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

		else if (slashCommandName.equals("chatping")) {
			PrivateServerService.getInstance().handleChatPingCommand(event);
			return;
		}

		else if (slashCommandName.equals("vcping")) {
			PrivateServerService.getInstance().handleVcPingCommand(event);
			return;
		}

		else if (slashCommandName.equals("vcping")) {
			PrivateServerService.getInstance().handleVcPingCommand(event);
			return;
		}

		boolean isCommandsDisabled = channelService.isChannelDisabled(event.getChannel().getIdLong());

		if (isCommandsDisabled) {
			event.deferReply().setEphemeral(true).queue();
			event.getHook().sendMessage("Commands are disabled in this channel").setEphemeral(true).queue();
			return;
		}

		CompletableFuture.runAsync(() -> UpdateReminder.getInstance().handleUser(event.getUser(), event.getChannel()));

		if (slashCommandName.equals("race")) {
			RaceHandler.getInstance().handleRaceCommand(event);
			return;
		}

		else if (slashCommandName.equals("crossword")) {
			CrosswordGameHandler.getInstance().handleCrosswordSlashCommand(event);
			return;
		}

		else if (slashCommandName.equals("crossduel")) {
			CrosswordGameHandler.getInstance().handleCrossduelSlashCommand(event);
			return;
		}

		else if (slashCommandName.equals("extra_words")) {
			CrosswordGameHandler.getInstance().handleExtraWordCommand(event);
			return;
		}

		event.deferReply().queue();

		if (slashCommandName.equals("vote")) {
			UtilService.getInstance().handleVoteCommand(event.getHook());
			return;
		}

		else if (slashCommandName.equals("prefix")) {
			PrefixCommandHandler.getInstance().handlePrefixCommand(event);
			return;
		}

		else if (slashCommandName.equals("crossword_appearance")) {
			CrosswordGameHandler.getInstance().handleCrosswordAppearenceCommand(event);
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
		if (random.nextInt(Constants.BOUND) == 1) {
			PatreonService.getInstance().sendPatreonRequestMessage(event.getChannel());
		}

		if (slashCommandName.equals("guess")) {
			UtilService.getInstance().handleGuessComnmands(event);
			return;
		}

		else if (slashCommandName.equals("slots")) {
			SlotsHandler.getInstance().handleSlotsSlashCommand(event);
			return;
		}

		else if (slashCommandName.equals("coinflip")) {
			CoinflipHandler.getInstance().handleCoinflipSlashCommand(event);
			return;
		}

		else if (slashCommandName.equals("mines")) {
			MinesHandler.getInstance().handleMinesSlashCommand(event);
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

		// else if (slashCommandName.equals("unblock")) {
		// String user_id = event.getOption("user_id").getAsString();
		// CaptchaService.getInstance().removeBlock(Long.parseLong(user_id));
		// event.getHook().sendMessage("Unblocked User").queue();
		// return;
		// }

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

		else if (slashCommandName.equals("activepatrons")) {
			PatreonService.getInstance().handleActivePatronCommand(event);
			return;
		}

	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		String componentId = event.getComponentId();
		if (componentId.startsWith("bgSelectMenu")) {
			CrosswordGameHandler.getInstance().handleBgSelection(event);
			return;
		} else if (componentId.startsWith("emptySelectMenu")) {
			CrosswordGameHandler.getInstance().handleEmptySelection(event);
			return;
		} else if (componentId.startsWith("help")) {
			UtilService.getInstance().handleHelpSelection(event);
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

		CompletableFuture.runAsync(() -> UpdateReminder.getInstance().handleUser(event.getUser(), event.getChannel()));

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
		} else if (buttonCommandId.startsWith("denyCrossduel")) {
			CrosswordGameHandler.getInstance().handleDenyCrossduel(event);
			return;
		} else if (buttonCommandId.startsWith("acceptCrossduel")) {
			CrosswordGameHandler.getInstance().handleAcceptCrossduel(event);
			return;
		} else if (buttonCommandId.startsWith("quitCrossduel")) {
			CrosswordGameHandler.getInstance().handleCrossduelQuitButton(event);
			return;
		} else if (buttonCommandId.startsWith("confirmQuitCrossduel")) {
			CrosswordGameHandler.getInstance().handleConfirmQuitCrossduel(event);
			return;
		} else if (buttonCommandId.startsWith("cancelQuitCrossduel")) {
			CrosswordGameHandler.getInstance().handleCancelQuitCrossduel(event);
			return;
		} else if (buttonCommandId.startsWith("shuffleCrossduel")) {
			CrosswordGameHandler.getInstance().handleShuffleDuelButton(event);
			return;
		}

		// else if (buttonCommandId.startsWith("stockTransactions")) {
		// StocksHandler.getInstance().handleStockTransactionButton(event);
		// return;
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
		} else if (buttonCommandId.startsWith("delete_data")) {
			UtilService.getInstance().handleConfirmDeleteButton(event);
			return;
		} else if (buttonCommandId.startsWith("checkRegionButton")) {
			RegionHandler.getInstance().handleRegionButton(event);
			return;
		} else if (buttonCommandId.startsWith("lb_")) {
			LeaderboardHandler.getInstance().handleLeaderboardButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("skipGuess")) {
			GuessGameHandler.getInstance().handleSkipRequest(event);
			return;
		}

		else if (buttonCommandId.startsWith("cancelAtlas")) {
			AtlasGameHandler.getInstance().handleCancelStartButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("mine")) {
			MinesHandler.getInstance().handleMineButton(event);
			return;
		}

		else if (buttonCommandId.startsWith("cashoutMines")) {
			MinesHandler.getInstance().handleCashoutButton(event);
			return;
		}

		// Commenting Captcha for now
		if (random.nextInt(Constants.BOUND) == 1) {
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

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if (event.getSubcommandName().equals("flag")) {
			if (event.getFocusedOption().getName().equals("mode")) {
				event.replyChoiceStrings("Sovereign Countries Only", "Non-Sovereign Countries Only", "All Countries")
						.queue();
			} else {
				event.replyChoiceStrings("Asia", "Africa", "Europe", "North America", "South America", "Oceania",
						"Antarctica")
						.queue();
			}
		} else if (event.getSubcommandName().equals("state_flag")) {
			event
					.replyChoiceStrings("United States", "Brazil", "Germany", "Spain", "Switzerland", "Canada", "Italy",
							"Russia",
							"Netherlands", "England", "Australia", "Japan", "Poland", "Argentina")
					.queue();
		} else if (event.getSubcommandName().equals("distance")) {
			event.replyChoiceStrings("Kilometers", "Miles").queue();
		} else if (event.getSubcommandName().equals("set")) {
			event.replyChoiceStrings("Arabic", "Dutch", "French", "German", "Japanese", "Korean", "Portuguese",
					"Russian",
					"Spanish", "Swedish", "Turkish", "Croatian", "Thai").queue();

		}
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		UtilService.getInstance().sendMessageToWebhook(WEBHOOK_URL,
				"Flagbot joined server : **" + event.getGuild().getName() + "**");
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		UtilService.getInstance().sendMessageToWebhook(WEBHOOK_URL,
				"Flagbot Leaved server : **" + event.getGuild().getName() + "**");
	}

	private boolean isContainKeyword(String message) {
		for (String keyword : keywords) {
			if (message.contains(keyword))
				return true;
		}
		return false;
	}

	private void loadAlternateNames() {
		alternateNamesMap.put("uae", "United Arab Emirates");
		alternateNamesMap.put("dr congo", "Democratic Republic of the Congo");
		alternateNamesMap.put("drc", "Democratic Republic of the Congo");
		alternateNamesMap.put("côte d'ivoire", "Ivory Coast");
		alternateNamesMap.put("cabo verde", "Cape Verde");
		alternateNamesMap.put("czech republic", "Czechia");
		alternateNamesMap.put("turkey", "Turkiye");
		alternateNamesMap.put("usa", "United States of America");
		alternateNamesMap.put("united states", "United States of America");
		alternateNamesMap.put("uk", "United Kingdom");
		alternateNamesMap.put("east timor", "Timor-Leste");
		alternateNamesMap.put("bharat", "India");
		alternateNamesMap.put("bosnia", "Bosnia and Herzegovina");
		alternateNamesMap.put("burma", "Myanmar");
		alternateNamesMap.put("c sharp", "C#");
		alternateNamesMap.put("cpp", "C++");
		alternateNamesMap.put("ea", "Electronic Arts");
		alternateNamesMap.put("eu", "European Union");
		alternateNamesMap.put("car", "Central African Republic");
		alternateNamesMap.put("south georgia", "South Georgia and the South Sandwich Islands");
		alternateNamesMap.put("sealand", "Principality of Sealand");
		alternateNamesMap.put("Åland islands", "Aland Islands");
		alternateNamesMap.put("northern cyprus", "Turkish Republic of Northern Cyprus");
		alternateNamesMap.put("usvi", "US Virgin Islands");
		alternateNamesMap.put("artsakh", "Nagorno-Karabakh");
		alternateNamesMap.put("united states virgin islands", "US Virgin Islands");
		alternateNamesMap.put("الإمارات", "الإمارات العربية المتحدة");
	}
}