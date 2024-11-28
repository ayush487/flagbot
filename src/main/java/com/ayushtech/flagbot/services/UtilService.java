package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.StocksDao;
import com.ayushtech.flagbot.dbconnectivity.StocksTransactionsDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.stocks.Company;
import com.ayushtech.flagbot.stocks.StocksHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class UtilService {
  private static UtilService utilService = null;

  private ChannelService channelService;

  private UtilService() {
    channelService = ChannelService.getInstance();
  }

  public static synchronized UtilService getInstance() {
    if (utilService == null) {
      utilService = new UtilService();
    }
    return utilService;
  }

  public void handleBalanceCommand(User user, InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(user.getName());
    long[] coins_rank = CoinDao.getInstance().getBalanceAndRank(user.getIdLong());
    eb.setDescription("**Balance** : " + coins_rank[0] + " :coin:\n**Rank** : " + coins_rank[1]);
    eb.setColor(Color.YELLOW);
    eb.setThumbnail(user.getAvatarUrl());
    hook.sendMessageEmbeds(eb.build()).setEphemeral(false).queue();
  }

  public void handleEnableCommand(SlashCommandInteractionEvent event) {
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
  }

  public void handleDisableAllCommand(SlashCommandInteractionEvent event) {
    event.deferReply().setEphemeral(true).queue();
    Member member = event.getMember();
    if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
      channelService.disableMultipleChannels(event.getGuild());
      event.getHook().sendMessage("Commands are disabled in all channels.").setEphemeral(true).queue();
    } else {
      event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
          .queue();
    }
  }

  public void handleDisableCommand(SlashCommandInteractionEvent event) {
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
  }

  public void handleDataDeletionRequest(User user, InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.RED);
    eb.setTitle("Delete data");
    eb.setDescription("Sending a dm for confirmation.");
    eb.setFooter("If not received message, consider turning on DMs.");
    hook.sendMessageEmbeds(eb.build()).queue();
    EmbedBuilder eb2 = new EmbedBuilder();
    eb2.setColor(Color.RED);
    eb2.setTitle("Confirm Data deletion");
    eb2.setDescription(
        "Click on the **Delete My Data** button to delete your data permanetly.\nNote : It will wipe all your coins, stocks permanently.");
    user.openPrivateChannel()
        .flatMap(channel -> channel.sendMessageEmbeds(eb2.build())
            .setActionRows(ActionRow.of(Button.primary("delete_data_" + user.getId(), "Delete My Data"))))
        .queue();
  }

  public void handleConfirmDeleteButton(ButtonInteractionEvent event) {
    String userId = event.getComponentId().split("_")[1];
    if (!event.getUser().getId().equals(userId)) {
      event.reply("This message is not for you!").setEphemeral(true).queue();
      return;
    }
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
  }

  public void handleVoteCommand(InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Vote for Flag Bot");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setDescription("Vote for Flag bot on top.gg\n[here](https://top.gg/bot/1129789320165867662/vote)");
    eb.addField("Rewards", "> Each vote gets you 1000 :coin:\n> You will get double rewards during weekends", false);
    eb.setFooter("You can vote every 12 hours");
    eb.setColor(Color.GREEN);
    hook.sendMessageEmbeds(eb.build())
        .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Top.gg"))
        .queue();

  }

  public void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
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
  }

  public void handleInviteCommand(InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(Color.GREEN);
    eb.setTitle("Invite Flagbot");
    eb.addField("Add Flagbot",
        "[here](https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=85056&scope=bot+applications.commands)",
        true);
    eb.addBlankField(true);
    eb.addField("Support Server", "[here](https://discord.gg/MASMYsNCT9)", true);
    hook.sendMessageEmbeds(eb.build())
        .addActionRow(Button.link(
            "https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=85056&scope=bot+applications.commands",
            "Add Flag bot to your server"), Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote"))
        .queue();
  }

  public void handleSupportCommand(InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.YELLOW);
    eb.addField("Support Server", "[Flag Bot Support Server](https://discord.gg/RqvTRMmVgR)", false);
    hook.sendMessageEmbeds(eb.build())
        .queue();
  }

  public void handleHelpCommand(InteractionHook hook) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setTitle("Commands");
    eb.setColor(new Color(255, 153, 51)); // rgb (255,153,51)
    eb.setDescription(
        "**__Guess Commands__**\n`/guess flag` : Start a flag guessing game in the channel\n`/guess map` : Start a map guessing game in the channel\n`/guess logo` : Start a logo guessing game in the channel\n`/guess capital` : Start a capital guessing game in the channel\n`/guess place` : Start a place guessing game in the channel\n`/guess continent` : State a continent guessing game in the channel\n`/guess location` : Start a location guessing game in the channel (**Only for voters**)\n`/guess distance` : A Multiplayer mode in which users can guess distance marked on the map (**Only for voters**)\n__Options__ :\n`mode` : Choose the mode you want to play :Soverign Only, Non-Soverign Only, All Countries (Soverign Only if not selected)\n`continent` : Specify the continent for the flag game\n`rounds` : Enter the number of rounds you want to play (maximum it would be 15) (optional)\n`include_non_soverign_countries` : True or False to include non soverign countries (false if not selected)\n`unit` : Enter your preffered unit (kilometer or miles)\n__Note__ : `Specifying continent will nullify mode selection and mode will automatically become 'All Countries'.`");
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
    hook.sendMessageEmbeds(eb.build())
        .addActionRow(Button.link("https://discord.gg/RqvTRMmVgR", "Support Server"),
            Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote"))
        .queue();
  }

  public void handleGiveCommands(SlashCommandInteractionEvent event) {
    String subCommandName = event.getSubcommandName();

    if (subCommandName.equals("coins")) {
      CompletableFuture.runAsync(() -> CoinTransferService.getInstance().handleGiveCoinsCommand(event));
      return;
    }

  }

  public void handleGuessComnmands(SlashCommandInteractionEvent event) {
    String commandName = event.getSubcommandName();
    if (commandName.equals("map")) {
      GuessGameHandler.getInstance().handlePlayMapCommand(event);
      return;
    } else if (commandName.equals("logo")) {
      GuessGameHandler.getInstance().handlePlayLogoCommand(event);
      return;
    } else if (commandName.equals("flag")) {
      GuessGameHandler.getInstance().handlePlayFlagCommand(event);
      return;
    } else if (commandName.equals("place")) {
      GuessGameHandler.getInstance().handlePlayPlaceCommand(event);
      return;
    } else if (commandName.equals("distance")) {
      GuessDistanceHandler.getInstance().handleNewGameCommand(event);
      return;
    } else if (commandName.equals("location")) {
      LocationGameHandler.getInstance().handleStartGameCommand(event);
      return;
    } else if (commandName.equals("capital")) {
      GuessGameHandler.getInstance().handlePlayCapitalCommand(event);
      return;
    } else {
      ContinentGameHandler.getInstance().handlePlayCommand(event);
      return;
    }
  }

  public void handleStockCommands(SlashCommandInteractionEvent event) {
    String subcommandName = event.getSubcommandName();
    if (subcommandName.equals("list")) {
      event.getHook().sendMessageEmbeds(StocksHandler.getInstance().getStockList())
          .addActionRow(Button.primary("refreshMarket_" + System.currentTimeMillis(),
              Emoji.fromEmote("refresh", 1209076086185656340l, false)))
          .queue();
      return;
    } else if (subcommandName.equals("owned")) {
      event.getHook().sendMessageEmbeds(StocksHandler.getInstance().getStocksOwned(event.getUser()))
          .addActionRow(Button.secondary("stockTransactions_0", "View Transactions"))
          .queue();
      return;
    } else if (subcommandName.equals("buy")) {
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
    } else if (subcommandName.equals("sell")) {
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
}