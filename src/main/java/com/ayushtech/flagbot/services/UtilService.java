package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.atlas.AtlasGameHandler;
import com.ayushtech.flagbot.crossword.CrosswordGameHandler;
import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.UserDao;
import com.ayushtech.flagbot.distanceGuess.GuessDistanceHandler;
import com.ayushtech.flagbot.game.continent.ContinentGameHandler;
import com.ayushtech.flagbot.game.location.LocationGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;

import net.dv8tion.jda.api.EmbedBuilder;
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
  private Map<Character, String> emojiMap;
	private final String bar1empty = Emoji.fromCustom("bar1empty", 1195296826132287541l, false).getAsMention();
	private final String bar1half = Emoji.fromCustom("bar1half", 1195297050993115246l, true).getAsMention();
	private final String bar1full = Emoji.fromCustom("bar1full", 1195297246464442368l, true).getAsMention();
	private final String bar1max = Emoji.fromCustom("bar1max", 1195297353591173201l, true).getAsMention();
	private final String bar2empty = Emoji.fromCustom("bar2empty", 1195297658567413790l, false).getAsMention();
	private final String bar2half = Emoji.fromCustom("bar2half", 1195297926734426162l, true).getAsMention();
	private final String bar2full = Emoji.fromCustom("bar2full", 1195298061522587659l, true).getAsMention();
	private final String bar2max = Emoji.fromCustom("bar2max", 1195298660800528434l, true).getAsMention();
	private final String bar3empty = Emoji.fromCustom("bar3empty", 1195298974429618207l, false).getAsMention();
	private final String bar3half = Emoji.fromCustom("bar3half", 1195299147499192362l, true).getAsMention();
	private final String bar3full = Emoji.fromCustom("bar3full", 1195299364759941131l, true).getAsMention();
	private String wordAdderWebhookUrl = "";
	private String wordRemovedWebhookUrl = "";

  private UtilService() {
    channelService = ChannelService.getInstance();
    this.emojiMap = new HashMap<Character, String>();
		setEmojis();
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
    long[] data = CoinDao.getInstance().getBalanceAndRankWordCoin(user.getIdLong());
    eb.setDescription("**Balance** : " + data[0] + " <:flag_coin:1472232340523843767>\n**Rank** : " + data[2] + "\n\n**Word Coins** : " + data[1] + "<:word_coin:1472270316007981301>");
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
      // StocksDao.getInstance().deleteStocksData(event.getUser().getIdLong());
      // StocksTransactionsDao.getInstance().deleteTransactionData(event.getUser().getIdLong());
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
    eb.addField("Rewards", "> Each vote gets you 1000 <:flag_coin:1472232340523843767> & 100 <:word_coin:1472270316007981301>\n> You will get double rewards during weekends", false);
    eb.setFooter("You can vote every 12 hours");
    eb.setColor(Color.GREEN);
    hook.sendMessageEmbeds(eb.build())
        .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Top.gg"))
        .queue();

  }

  // public void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
  //   JDA jda = event.getJDA();
  //   int optInt = 5;
  //   OptionMapping optSize = event.getOption("size");
  //   if (optSize != null) {
  //     optInt = optSize.getAsInt();
  //   }
  //   int lbSize = optInt >= 25 ? 25 : (optInt <= 5) ? 5 : optInt;
  //   CompletableFuture.runAsync(() -> {
  //     String temp = LeaderboardHandler.getInstance().getLeaderboard(jda, lbSize);
  //     String leaderboard = temp != null ? temp : "Something went wrong!";
  //     event.getHook().sendMessage(leaderboard).queue();
  //   });
  // }

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

  public void handleHelpCommand(SlashCommandInteractionEvent event) {
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
      case "crossword":
        embed = crosswordHelpEmbed(userPfp);
        page = 3;
        break;
      case "atlas":
        embed = atlasHelpEmbed(userPfp);
        page = 4;
        break;
      case "memoflip":
        embed = memoflipHelpEmbed(userPfp);
        page = 5;
        break;
      case "race":
        embed = raceHelpEmbed(userPfp);
        page = 6;
        break;
      case "config":
        embed = configHelpEmbed(userPfp);
        page = 7;
        break;
      default:
        embed = generalHelpEmbed(userPfp);
        break;
    }
    int prevPage = page == 1 ? 7 : page - 1;
    int nextPage = page == 7 ? 1 : page + 1;
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
        embed = crosswordHelpEmbed(userPfp);
        break;
      case 4:
        embed = atlasHelpEmbed(userPfp);
        break;
      case 5:
        embed = memoflipHelpEmbed(userPfp);
        break;
      case 6:
        embed = raceHelpEmbed(userPfp);
        break;
      case 7:
        embed = configHelpEmbed(userPfp);
        break;
      default:
        embed = generalHelpEmbed(userPfp);
        break;
    }
    int prevPage = helpPage == 1 ? 7 : helpPage - 1;
    int nextPage = helpPage == 7 ? 1 : helpPage + 1;
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
    description.append("**/help crossword :** `info about crossword commands`\n");
    description.append("**/help atlas :** `info about atlas commands`\n");
    description.append("**/help race  :** `info about race commands`\n");
    description.append("**/help memoflip :** `info about memoflip command`\n");
    description.append("**/help language :** `info about setting up languages in the bot`\n");
    description.append("**/help config :** `info about bot configuration into the server`");
    eb.setDescription(description.toString());
    StringBuilder sb = new StringBuilder();
    sb.append("`/leaderboards` : Check the global leaderboard\n");
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
    eb.setFooter("Page 1/7", userPfp);
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
    eb.setFooter("Page 2/7", userPfp);
    return eb.build();
  }

  private MessageEmbed crosswordHelpEmbed(String userPfp){
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Crossword");
    eb.setColor(new Color(255, 153, 51));
    StringBuilder descBuilder = new StringBuilder();
    descBuilder.append("**__Commands__**\n");
    descBuilder.append("`/crossword` : Start a crossword game in the current channel\n");
    descBuilder.append("`/extra_words` : View extra words collected by you and claim rewards\n");
    eb.setDescription(descBuilder.toString());
    eb.addField("__How to play?__", "Create words from given letters and solve the crossword puzzle", false);
    StringBuilder buttonsInfo = new StringBuilder();
    buttonsInfo.append("<:refresh:1209076086185656340> : Shuffle the letters\n");
    buttonsInfo.append(":bulb: : Unhide a random letter from the puzzle. One hint is free in each game.\n");
    buttonsInfo.append("**Quit** : Quit the game\n");
    buttonsInfo.append("**Extra Words** : Display extra words and its count");
    eb.addField("__Buttons__", buttonsInfo.toString(), false);
    eb.setFooter("Page 3/7", userPfp);
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
    eb.setFooter("Page 4/7", userPfp);
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
    eb.setFooter("Page 5/7", userPfp);
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
    eb.setFooter("Page 6/7", userPfp);
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
    eb.setFooter("Page 7/7", userPfp);
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

  public void handleAddWordCommand(SlashCommandInteractionEvent event) {
		String word = event.getOption("word").getAsString();
		try {
			boolean isAdded = UserDao.getInstance().addWord(word);
			if (isAdded) {
				CrosswordGameHandler.getInstance().addWordIntoWordSet(word);
				event.getHook().sendMessage(String.format("**%s** added into database", word)).queue();
				String webhookMessage = String.format("Word Added : **%s**     By : <@%s>", word,
						event.getUser().getId());
				sendMessageToWebhook(wordAdderWebhookUrl, webhookMessage);
			} else {
				event.getHook().sendMessage(String.format("**%s** already exist in database!", word)).queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getHook().sendMessage("Something went wrong!").queue();
			return;
		}
	}

	public void handleRemoveWordCommand(SlashCommandInteractionEvent event) {
		String word = event.getOption("word").getAsString();
		try {
			boolean isRemoved = UserDao.getInstance().removeWord(word);
			if (isRemoved) {
				CrosswordGameHandler.getInstance().removeWordFromWordSet(word);
				event.getHook().sendMessage(String.format("**%s** removed from database", word)).queue();
				String webhookMessage = String.format("Word Removed : **%s**    By : <@%s>", word,
						event.getUser().getId());
				sendMessageToWebhook(wordRemovedWebhookUrl, webhookMessage);
			} else {
				event.getHook().sendMessage(String.format("**%s** is not in database", word)).queue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			event.getHook().sendMessage("Something went wrong!").queue();
			return;
		}
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

  public String getEmoji(char c) {
		return this.emojiMap.get(c);
	}

	public String getDate() {
		return String.format("%d-%d-%d", Calendar.getInstance().get(Calendar.DATE),
				Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR));
	}

  public String getProgressBar(int fill) {
		int progressBarFillAmount = Math.round(fill / 10.0f) * 10;
		String progressBar;
		switch (progressBarFillAmount) {
			case 0:
				progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 10:
				progressBar = bar1half + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 20:
				progressBar = bar1full + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
			case 30:
				progressBar = bar1max + bar2half + bar2empty + bar2empty + bar3empty;
				break;
			case 40:
				progressBar = bar1max + bar2full + bar2empty + bar2empty + bar3empty;
				break;
			case 50:
				progressBar = bar1max + bar2max + bar2half + bar2empty + bar3empty;
				break;
			case 60:
				progressBar = bar1max + bar2max + bar2full + bar2empty + bar3empty;
				break;
			case 70:
				progressBar = bar1max + bar2max + bar2max + bar2half + bar3empty;
				break;
			case 80:
				progressBar = bar1max + bar2max + bar2max + bar2full + bar3empty;
				break;
			case 90:
				progressBar = bar1max + bar2max + bar2max + bar2max + bar3half;
				break;
			case 100:
				progressBar = bar1max + bar2max + bar2max + bar2max + bar3full;
				break;
			default:
				progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
				break;
		}
		return progressBar;
	}

  private void setEmojis() {
		this.emojiMap.put('a', ":regional_indicator_a:");
		this.emojiMap.put('b', ":regional_indicator_b:");
		this.emojiMap.put('c', ":regional_indicator_c:");
		this.emojiMap.put('d', ":regional_indicator_d:");
		this.emojiMap.put('e', ":regional_indicator_e:");
		this.emojiMap.put('f', ":regional_indicator_f:");
		this.emojiMap.put('g', ":regional_indicator_g:");
		this.emojiMap.put('h', ":regional_indicator_h:");
		this.emojiMap.put('i', ":regional_indicator_i:");
		this.emojiMap.put('j', ":regional_indicator_j:");
		this.emojiMap.put('k', ":regional_indicator_k:");
		this.emojiMap.put('l', ":regional_indicator_l:");
		this.emojiMap.put('m', ":regional_indicator_m:");
		this.emojiMap.put('n', ":regional_indicator_n:");
		this.emojiMap.put('o', ":regional_indicator_o:");
		this.emojiMap.put('p', ":regional_indicator_p:");
		this.emojiMap.put('q', ":regional_indicator_q:");
		this.emojiMap.put('r', ":regional_indicator_r:");
		this.emojiMap.put('s', ":regional_indicator_s:");
		this.emojiMap.put('t', ":regional_indicator_t:");
		this.emojiMap.put('u', ":regional_indicator_u:");
		this.emojiMap.put('v', ":regional_indicator_v:");
		this.emojiMap.put('w', ":regional_indicator_w:");
		this.emojiMap.put('x', ":regional_indicator_x:");
		this.emojiMap.put('y', ":regional_indicator_y:");
		this.emojiMap.put('z', ":regional_indicator_z:");
		this.emojiMap.put('-', "‎:black_large_square:");
		this.emojiMap.put('+', ":white_medium_square:");
		this.emojiMap.put('A', "<:a_:1471520693014364272>");
		this.emojiMap.put('B', "<:b_:1471520696088788992>");
		this.emojiMap.put('C', "<:c_:1471520698420822148>");
		this.emojiMap.put('D', "<:d_:1471520700681420882>");
		this.emojiMap.put('E', "<:e_:1471520703374299310>");
		this.emojiMap.put('F', "<:f_:1471520705660321823>");
		this.emojiMap.put('G', "<:g_:1471520707795091577>");
		this.emojiMap.put('H', "<:h_:1471520709715951626>");
		this.emojiMap.put('I', "<:i_:1471520712136069202>");
		this.emojiMap.put('J', "<:j_:1471520714136883364>");
		this.emojiMap.put('K', "<:k_:1471520716993069159>");
		this.emojiMap.put('L', "<:l_:1471520719127969874>");
		this.emojiMap.put('M', "<:m_:1471520721800007792>");
		this.emojiMap.put('N', "<:n_:1471520724257869895>");
		this.emojiMap.put('O', "<:o_:1471520726510211153>");
		this.emojiMap.put('P', "<:p_:1471520728506568807>");
		this.emojiMap.put('Q', "<:q_:1471520730834407610>");
		this.emojiMap.put('R', "<:r_:1471520733350985739>");
		this.emojiMap.put('S', "<:s_:1471520735842275600>");
		this.emojiMap.put('T', "<:t_:1471520737994084536>");
		this.emojiMap.put('U', "<:u_:1471520740418523167>");
		this.emojiMap.put('V', "<:v_:1471520744310571178>");
		this.emojiMap.put('W', "<:w_:1471520746776822020>");
		this.emojiMap.put('X', "<:x_:1471520750942031932>");
		this.emojiMap.put('Y', "<:y_:1471520756751011902>");
		this.emojiMap.put('Z', "<:z_:1471520759061942333>");
	}

	public void setWordAdderWebhookUrl(String url) {
		this.wordAdderWebhookUrl = url;
	}

	public void setWordRemovedWebhookUrl(String url) {
		this.wordRemovedWebhookUrl = url;
	}
}