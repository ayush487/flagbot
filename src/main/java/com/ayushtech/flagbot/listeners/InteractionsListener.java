package com.ayushtech.flagbot.listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.fight.Damage;
import com.ayushtech.flagbot.game.fight.FightHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.services.CaptchaService;
import com.ayushtech.flagbot.services.ChannelService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class InteractionsListener extends ListenerAdapter {

	private ScheduledExecutorService gameEndService;
	private ChannelService channelService;
	private Random random;
	private final int BOUND = 75;

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
			eb.addField("Rewards", "**•** Each vote gets you 1000 :coin: ", false);
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
			String temp = LeaderboardHandler.getInstance().getLeaderboard(jda, lbSize);
			String leaderboard = temp != null ? temp : "Something went wrong!";
			event.getHook().sendMessage(leaderboard).queue();
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
					.addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote")).queue();
			return;
		}

		// help command
		else if (event.getName().equals("help")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setTitle("Commands");
			eb.setColor(new Color(255, 153, 51)); // rgb (255,153,51)
			eb.setDescription(
					"`/guess` : Start a flag guessing game in the channel\n`/guessmap` : Start a map guessing game in the channel\n`/leaderboards` : Check the global leaderboard (Top 5)\n`/invite` : Invite the bot to your server\n`/disable` : Disable the commands in the given channel\n`/enable` : Enable the commands in the given channel\n`/disable_all_channels` : Disable the commands for all the channels of the server\n`/delete_my_data` : Will Delete your data from the bot\n`/balance` : You can see your coins and rank\n`/vote` : Vote for us and get rewards");
			eb.addField("__Battle Command__", "`/battle` : Start a 1v1 battle between two users.\n**__Options__**\n**opponent** : Mention the user with whom you wanna battle.\n**bet** : Amout to bet in the battle (optional)", false);
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
					"Click on the **Delete My Data** button to delete your data permanetly.\nNote : It will wipe all your coins permanently.");
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

		if (random.nextInt(BOUND) == 1) {
			CaptchaService.getInstance().sendCaptcha(event);
			return;
		}

		// guess command
		else if (event.getName().equals("guess")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
			return;
		}

		// guessmap command
		else if (event.getName().equals("guessmap")) {
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
		else if(event.getName().equals("unblock")) {
			String user_id = event.getOption("user_id").getAsString();
			CaptchaService.getInstance().removeBlock(Long.parseLong(user_id));
			event.reply("Unblocked User").queue();
			return;
		}
	}

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

		if(event.getComponentId().startsWith("punchSelection")) {
			String selectedOptionIso = event.getComponentId().split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.PUNCH, selectedOptionIso);
			return;
		}
		else if (event.getComponentId().startsWith("kickSelection")) {
			String selectedOptionIso = event.getComponentId().split("-")[1];
			FightHandler.getInstance().handleSelection(event, Damage.KICK, selectedOptionIso);
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

		else if(event.getComponentId().equals("kickInBattle")) {
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

		else if (event.getComponentId().equals("playAgainButton")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
			return;
		}

		else if (event.getComponentId().equals("playAgainMap")) {
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
	}

}
