package com.ayushtech.flagbot.listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;
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

	public InteractionsListener() {
		super();
		gameEndService = new ScheduledThreadPoolExecutor(4);
		channelService = ChannelService.getInstance();
	}

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
		event.deferReply().queue();

		// Disable Command
		if (event.getName().equals("disable")) {
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				OptionMapping option = event.getOption("channel");
				if (option == null) {
					// channelDao.addDisableChannel(event.getChannel().getIdLong());
					channelService.disableChannel(event.getChannel().getIdLong());
					event.getHook().sendMessage("Commands are disabled for this channel now!").setEphemeral(true).queue();
				} else {
					GuildMessageChannel channelOption = option.getAsMessageChannel();
					if (channelOption == null) {
						event.getHook().sendMessage("Mentioned channel is not a Message Channel").queue();
					} else {
						// channelDao.addDisableChannel(channelOption.getIdLong());
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
		}

		if (channelService.isChannelDisabled(event.getChannel().getIdLong())) {
			event.getHook().sendMessage("Commands are disabled in this channel").setEphemeral(true).queue();
			return;
		}


		// guess command
		if (event.getName().equals("guess")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
		} 
		
		// leaderboards command
		else if (event.getName().equals("leaderboards")) {
			JDA jda = event.getJDA();
			String temp = LeaderboardHandler.getInstance().getLeaderboard(jda);
			String leaderboard = temp != null ? temp : "Something went wrong!";
			event.getHook().sendMessage(leaderboard).queue();
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
			event.getHook().sendMessageEmbeds(eb.build()).queue();
		}
		
		// show_server_count command (private access)
		else if (event.getName().equals("show_server_count")) {
			event.getHook().sendMessage("Total Servers in : " + event.getJDA().getGuilds().size()).queue();
		} 
		
		// help command
		else if (event.getName().equals("help")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setTitle("Commands");
			eb.setColor(new Color(223,32,32));
			eb.setDescription(
					"`/guess` : Start a flag guessing game in the channel\n`/guessmap` : Start a map guessing game in the channel\n`/leaderboards` : Check the global leaderboard (Top 5)\n`/invite` : Invite the bot to your server\n`/disable` : Disable the commands in the given channel\n`/enable` : Enable the commands in the given channel\n`/disable_all_channels` : Disable the commands for all the channels of the server\n`/delete_my_data` : Will Delete your data from the bot");
			eb.addField("Other Information", "[Privacy Policy](https://github.com/ayush487/flagbot/blob/main/PRIVACY.md)", false);
			event.getHook().sendMessageEmbeds(eb.build())
			.addActionRow(Button.link("https://discord.gg/RqvTRMmVgR", "Support Server"))
			.queue();
		} 

		// delete_my_data command
		else if(event.getName().equals("delete_my_data")) {
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
			eb2.setDescription("Click on the **Delete My Data** button to delete your data permanetly.\nNote : It will wipe all your coins permanently.");
			;
			user.openPrivateChannel().flatMap(channel -> 
				channel.sendMessageEmbeds(eb2.build()).setActionRows(ActionRow.of(Button.primary("delete_data", "Delete My Data")))
			)
			.queue();
		}

		// balance command
		else if(event.getName().equals("balance")) {
			User user = event.getUser();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle(user.getName());
			long[] coins_rank = CoinDao.getInstance().getBalanceAndRank(user.getIdLong());
			eb.setDescription("**Balance** : " + coins_rank[0] + " :coin:\n**Rank** : " + coins_rank[1]);
			eb.setColor(Color.YELLOW);
			eb.setThumbnail(user.getAvatarUrl());
			// eb.setFooter("Your rank : " + coins_rank[1]);
			event.getHook().sendMessageEmbeds(eb.build()).queue();
		}
	}

	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		super.onButtonInteraction(event);
		if (event.getComponentId().equals("playAgainButton")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
		} else if (event.getComponentId().equals("skipButton")) {
			event.deferReply().queue();
			if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
		} else if (event.getComponentId().equals("checkRegionButton")) {
			event.deferReply().setEphemeral(true).queue();
			String response = RegionHandler.getInstance().requestForHint(event);
			response = (response != null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();
			;
		} else if (event.getComponentId().equals("playAgainMap")) {
			boolean isAdded = MapGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(
						new MapGameEndRunnable(
								MapGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
		} else if (event.getComponentId().equals("skipMap")) {
			event.deferReply().queue();
			if (MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				MapGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
		} else if (event.getComponentId().equals("delete_data")) {
			
			LocalDateTime messageCreationTime = event.getMessage().getTimeCreated().toLocalDateTime();
			LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("GMT"));
			long timeDifference = Duration.between(messageCreationTime, currentTime).toMillis();
		
			if(timeDifference>=60000l) {
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
		}
		

	}

}
