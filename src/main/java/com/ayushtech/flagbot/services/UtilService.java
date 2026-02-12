package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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
        GuildChannelUnion channelOption = option.getAsChannel();
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
        GuildChannelUnion channelOption = option.getAsChannel();
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
            .setComponents(ActionRow.of(Button.primary("delete_data_" + user.getId(), "Delete My Data"))))
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

  // public void handleHelpCommand(SlashCommandInteractionEvent event) {
  // sendGeneralHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // sendGuessHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // sendAtlasHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // sendMemoflipHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // sendRaceHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // sendConfigHelpEmbed(event.getHook(), event.getUser().getAvatarUrl());
  // }

  public void handleHelpCommand(SlashCommandInteractionEvent event) {
    // event.getJDA().upsertCommand("help", "Help command")
    // .addSubcommandGroups(
    //   new SubcommandGroupData("overview", "Overview about all the commands of the bot"),
    //   new SubcommandGroupData("guess", "Information about the guess commands."),
    //   new SubcommandGroupData("atlas", "Information about the atlas commands."),
    //   new SubcommandGroupData("memoflip", "Information about the memoflip commands."),
    //   new SubcommandGroupData("race", "Information about the race commands."),
    //   new SubcommandGroupData("config", "Information about bot configuration and setup.")
    // ).queue();
    String subcommandName = event.getSubcommandName();
    if (subcommandName == null)
      subcommandName = "";
    int page = 1;
    String userPfp = event.getUser().getAvatarUrl();
    MessageEmbed embed;
    switch (subcommandName) {
      case "overview":
        embed = generalHelpEmbed(userPfp);
        break;
      case "guess":
        embed = guessHelpEmbed(userPfp);
        page = 2;
        break;
      case "atlas":
        embed = atlasHelpEmbed(userPfp);
        page = 3;
        break;
      case "memoflip":
        embed = memoflipHelpEmbed(userPfp);
        page = 4;
        break;
      case "race":
        embed = raceHelpEmbed(userPfp);
        page = 5;
        break;
      case "config":
        embed = configHelpEmbed(userPfp);
        page = 6;
        break;
      default:
        embed = generalHelpEmbed(userPfp);
        break;
    }
    int prevPage = page == 1 ? 6 : page - 1;
    int nextPage = page == 6 ? 1 : page + 1;
    event.getHook().sendMessageEmbeds(embed)
        .addActionRow(Button.secondary("help_" + prevPage, Emoji.fromFormatted("<:left_tri:1471426605263097999>")),
            Button.secondary("help_" + nextPage, Emoji.fromFormatted("<:right_tri:1471426673131126954>")))
        .queue();
  }

  public void handleHelpButton(ButtonInteractionEvent event) {
    int helpPage = Integer.parseInt(event.getComponentId().split("_")[1]);
    String userPfp = event.getUser().getAvatarUrl();
    MessageEmbed embed;
    switch (helpPage) {
      case 1:
        embed = generalHelpEmbed(userPfp);
        break;
      case 2:
        embed = guessHelpEmbed(userPfp);
        break;
      case 3:
        embed = atlasHelpEmbed(userPfp);
        break;
      case 4:
        embed = memoflipHelpEmbed(userPfp);
        break;
      case 5:
        embed = raceHelpEmbed(userPfp);
        break;
      case 6:
        embed = configHelpEmbed(userPfp);
        break;
      default:
        embed = generalHelpEmbed(userPfp);
        break;
    }
    int prevPage = helpPage == 1 ? 6 : helpPage - 1;
    int nextPage = helpPage == 6 ? 1 : helpPage + 1;
    event.editMessageEmbeds(embed)
        .setActionRow(Button.link("https://discord.gg/RqvTRMmVgR", "Support Server"),
            Button.link("https://top.gg/bot/1129789320165867662/vote", "❤️Vote"))
        .setActionRow(Button.secondary("help_" + prevPage, Emoji.fromFormatted("<:left_tri:1471426605263097999>")),
            Button.secondary("help_" + nextPage, Emoji.fromFormatted("<:right_tri:1471426673131126954>")))
        .queue();
  }

  private MessageEmbed generalHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Help Command");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder description = new StringBuilder();
    description.append("**/help guess :** `info about guess commands`\n");
    description.append("**/help atlas :** `info about atlas commands`\n");
    description.append("**/help race  :** `info about race commands`\n");
    description.append("**/help memoflip :** `info about memoflip command`\n");
    description.append("**/help language :** `info about setting up languages in the bot`\n");
    description.append("**/help config :** `info about bot configuration into the server`");
    eb.setDescription(description.toString());
    StringBuilder sb = new StringBuilder();
    sb.append("`/leaderboards` : Check the global leaderboard (upto top 25)\n");
    sb.append("`/invite` : Invite the bot to your server\n");
    sb.append("`/balance` : You can see your coins and rank\n");
    sb.append("`/give coins` : Send coins to other users.\n");
    sb.append("`/vote` : Vote for us and get rewards\n");
    sb.append("`/patreon` : Show information about Patreon Membership");
    eb.addField("Commands", sb.toString(), false);
    eb.setDescription(description.toString());
    eb.addField("Other Information",
        "[Terms of Services](https://github.com/ayush487/flagbot/blob/main/TERMSOFSERVICE.md)\n[Privacy Policy](https://github.com/ayush487/flagbot/blob/main/PRIVACY.md)",
        false);
    eb.setFooter("Page 1/6", userPfp);
    return eb.build();
  }

  private MessageEmbed guessHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess Commands");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("`/guess flag` : Start a flag guessing game in the channel\n");
    descBuilder.append("`/guess map` : Start a map guessing game in the channel\n");
    descBuilder.append("`/guess logo` : Start a logo guessing game in the channel\n");
    descBuilder.append("`/guess capital` : Start a capital guessing game in the channel\n");
    descBuilder.append("`/guess state_flag` : Start a flag guessing game for states of a country\n");
    descBuilder.append("`/guess place` : Start a place guessing game in the channel\n");
    descBuilder.append("`/guess continent` : State a continent guessing game in the channel\n");
    descBuilder.append("`/guess location` : Start a location guessing game in the channel (**Only for voters**)\n");
    descBuilder.append(
        "`/guess distance` : A Multiplayer mode in which users can guess distance marked on the map (**Only for voters**)\n");
    eb.setDescription(descBuilder.toString());
    StringBuilder optionsBuilder = new StringBuilder();
    optionsBuilder.append("`mode` : choose mode you want to play (Soverign Only, Non-Soverign Only, All Countries)\n");
    optionsBuilder.append("`continent` : specify the continent for the flag game\n");
    optionsBuilder.append("`rounds` : enter number of rounds you want to play (maximum it would be 15) (optional)\n");
    optionsBuilder.append("`unit` : enter your preffered unit (kilometer or miles)\n");
    optionsBuilder.append("`skippable` : Set whether the games can be skipped or not.\n");
    eb.addField("__Options__", optionsBuilder.toString(), false);
    StringBuilder noteBuilder = new StringBuilder();
    noteBuilder.append(
        "Specifying continent will nullify mode selection and mode will automatically become 'All Countries'.\n");
    noteBuilder.append(
        "Supported countries for state flag mode: `USA, Brazil, Germany, Spain, Switzerland, Canada, Italy, Russia, Netherlands, England, Australia, Japan, Poland, Argentina`");
    eb.addField("__Extra Info__", noteBuilder.toString(), false);
    eb.setFooter("Page 2/6", userPfp);
    return eb.build();
  }

  private MessageEmbed atlasHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Atlas - Discover the World with Flag Bot!");
    eb.setThumbnail(
        "https://cdn.discordapp.com/attachments/1133277774010925206/1319749920852410420/globe_question.jpg?ex=67671864&is=6765c6e4&hm=8f7db66b8f9ba8f6f9962f261c6b55ce1815191068929a940604b07da9160d67&");
    eb.setColor(new Color(255, 153, 51));
    eb.setDescription(
        "Embark on an epic geographical adventure with the **Atlas** command, an exciting multiplayer mode in Flag Bot!\nTest your knowledge of the world and compete with friends in various modes. 🌍🗺️");
    StringBuilder sb = new StringBuilder(
        "\n__Classic Mode__ : *Submit one answer per question. The first correct answer earns 5 points, the second 3 points, and the rest 1 point.*\n");
    sb.append("\n__Quick Mode__ : *Only the first correct answer wins, earning 5 points. Speed is key!*\n");
    sb.append(
        "\n__Rapid Mode__ : *Submit as many answers as you can. Points are awarded based on the number of correct answers given.*\n");
    sb.append(
        "\n__Note__ : `Quick and Rapid mode is only available for Patrons and users who voted for the bot in last 24 hours.`");
    eb.addField("__Modes__", sb.toString(), false);
    StringBuilder sb2 = new StringBuilder(
        "Options can be used to customize the game **(Restricted to Patrons only)**.\n");
    sb2.append(
        "`bet_amount` : *Set an entry fee for the game where the winner takes all. Default is 0 (max bet can be 10,000).*\n");
    sb2.append(
        "`rounds` : *Set the number of rounds the game will run. Default is 10 (customizable between 2 to 25).*\n");
    sb2.append(
        "`time` *Set the time provided each round to answer. Default is 15 seconds (modifiable between 5 and 60 seconds).*\n");
    sb2.append("`max_score` : *Set the score limit for the game. Default is 30 (customizable between 8 to 100).*\n");
    sb2.append("\n**f!exitatlas** to quit the game midway");
    eb.addField("__Options__", sb2.toString(), false);
    eb.setFooter("Page 3/6", userPfp);
    return eb.build();
  }

  private MessageEmbed memoflipHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memoflip Game");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("`/memoflip easy` : Start a memoflip game in easy mode (8 cards)\n");
    descBuilder.append("`/memoflip medium` : Start a memoflip game in medium mode (16 cards)\n");
    descBuilder.append("`/memoflip hard` : Start a memoflip game in hard mode (24 cards)\n");
    descBuilder.append("`/memoflip scores` : Sends your best scores in each mode.");
    eb.setDescription(descBuilder.toString());
    eb.setFooter("Page 4/6", userPfp);
    return eb.build();
  }

  private MessageEmbed raceHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Race Command");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("`/race flags` : Start a race in the following channel of Flag mode\n");
    descBuilder.append("`/race maps` : Start a race in the following channel of Map mode\n");
    descBuilder.append("`/race logo` : Start a race in the following channel of Logo mode\n");
    descBuilder.append("`/race maths` : Start a race in the following channel of Maths mode");
    eb.setDescription(descBuilder.toString());
    eb.setFooter("Page 5/6", userPfp);
    return eb.build();
  }

  private MessageEmbed configHelpEmbed(String userPfp) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Bot Config Commands");
    eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("`/language set` : set language for the server (Only work for flag and map guessers)\n");
    descBuilder.append("`/language info` : see your server language and other supported languages\n");
    descBuilder.append("`/language remove` : remove server language\n");
    descBuilder.append("`/disable` : Disable the commands in the given channel\n");
    descBuilder.append("`/enable` : Enable the commands in the given channel\n");
    descBuilder.append("`/disable_all_channels` : Disable the commands for all the channels of the server\n");
    eb.setDescription(descBuilder.toString());
    eb.setFooter("Page 6/6", userPfp);
    return eb.build();
  }

  public void handleGiveCommands(SlashCommandInteractionEvent event) {
    String subCommandName = event.getSubcommandName();

    if (subCommandName.equals("coins")) {
      CompletableFuture.runAsync(() -> CoinTransferService.getInstance().handleGiveCoinsCommand(event));
      return;
    }

  }

  public void handleAtlasCommands(SlashCommandInteractionEvent event) {
    Member selfMember = event.getGuild().getSelfMember();
    if (event.getGuild() != null) {
      GuildChannel guildChannel = event.getGuild().getGuildChannelById(event.getChannel().getId());
      if (!selfMember.hasPermission(guildChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
          Permission.MESSAGE_EMBED_LINKS)) {
        event.getHook().sendMessage(
            "Missing Permissions : `VIEW_CHANNEL` or `MESSAGE_SEND` or `MESSAGE_EMBED_LINKS`\nPlease ask your server admin to grant me these permissions or try in a different channel.")
            .queue();
        return;
      }
    }
    String commandName = event.getSubcommandName();
    if (commandName.equals("classic")) {
      AtlasGameHandler.getInstance().handleClassicMode(event);
    } else if (commandName.equals("quick")) {
      AtlasGameHandler.getInstance().handleQuickMode(event);
    } else if (commandName.equals("rapid")) {
      AtlasGameHandler.getInstance().handleRapidMode(event);
    } else {
      AtlasGameHandler.getInstance().handleAtlasHelp(event);
    }
  }

  public void handleBotCommands(SlashCommandInteractionEvent event) {
    String commandName = event.getSubcommandName();
    if (commandName.equals("gc")) {
      Runtime.getRuntime().gc();
      event.getHook().sendMessage("Requested for Garbage Collection").queue();
      return;
    } else if (commandName.equals("memory")) {
      long totalMemory = Runtime.getRuntime().totalMemory();
      long freeMemory = Runtime.getRuntime().freeMemory();
      long usedMemory = totalMemory - freeMemory;
      event.getHook()
          .sendMessage(String.format("Memory Used : %d\nAvailable Free Memory: %d MB\nTotal Memory in JVM : %d MB",
              usedMemory / (1024 * 1024), freeMemory / (1024 * 1024), totalMemory / (1024 * 1024)))
          .queue();
      return;
    }
  }

  public void handleGuessComnmands(SlashCommandInteractionEvent event) {
    if (event.getGuild() != null) {
      Member selfMember = event.getGuild().getSelfMember();
      GuildChannel guildChannel = event.getGuild().getGuildChannelById(event.getChannel().getId());
      if (!selfMember.hasPermission(guildChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
          Permission.MESSAGE_EMBED_LINKS)) {
        event.getHook().sendMessage(
            "Missing Permissions : `VIEW_CHANNEL` or `MESSAGE_SEND` or `MESSAGE_EMBED_LINKS`\nPlease ask your server admin to grant me these permissions or try in a different channel.")
            .queue();
        return;
      }
    }
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
    } else if (commandName.equals("state_flag")) {
      GuessGameHandler.getInstance().handlePlayStateFlagCommand(event);
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
          .queue();
      return;
    } else if (subcommandName.equals("owned")) {
      event.getHook().sendMessageEmbeds(StocksHandler.getInstance().getStocksOwned(event.getUser()))
          .addActionRow(Button.secondary("stockTransactions_0", "View Transactions"))
          .queue();
      return;
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

  public void sendMessageToWebhook(String url, String message) {
    OkHttpClient client = new OkHttpClient();
    String jsonInputString = String.format("{\"content\" : \"%s\"}", message);
    RequestBody body = RequestBody.create(jsonInputString, MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder().url(url).post(body).build();
    try {
      client.newCall(request).execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}