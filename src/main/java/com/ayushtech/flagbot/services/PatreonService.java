package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.PatronDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

public class PatreonService {
  private static PatreonService patreonService = null;

  private Set<Long> patronUsers;
  private final ScheduledThreadPoolExecutor executor;
  private final Map<Long, String> correctGuessReactions;
  private final Map<Long, String> wrongGuessReactions;
  private String WEBHOOK_URL = "";

  private PatreonService() {
    this.patronUsers = PatronDao.getInstance().getValidPatronMembers();
    executor = new ScheduledThreadPoolExecutor(1);
    executor.scheduleAtFixedRate(() -> {
      this.patronUsers = PatronDao.getInstance().getValidPatronMembers();
    }, 60, 60, TimeUnit.MINUTES);
    this.wrongGuessReactions = PatronDao.getInstance().getWrongReactions();
    this.correctGuessReactions = PatronDao.getInstance().getCorrectReactions();
  }

  public static PatreonService getInstance() {
    if (patreonService == null) {
      patreonService = new PatreonService();
    }
    return patreonService;
  }

  public void addNewPatron(JDA jda, long userId) {
    patronUsers.add(userId);
    long validTill = PatronDao.getInstance().addNewPatron(userId);
    jda.retrieveUserById(userId).queue(user -> {
      user.openPrivateChannel().flatMap(channel -> channel.sendMessage(
          "# Thanks for joining Flag Bot Patreon Membership\nYou got the following perks :\n> Access to Voters Only Commands (Guess distance, Guess location, Atlas Quick, Atlas Rapid)\n> Custom Reactions for correct and wrong guesses\n> Can customize Atlas games\n\n__Set Custom Reaction with These commands__ :\n"
              +
              "`f!set correct_guess :emote:`\n" +
              "`f!set wrong_guess :emote:`\n" +
              "`f!remove wrong_guess`\n\nThese perks will be valid till "
              + TimeFormat.DATE_TIME_SHORT.atTimestamp(validTill)))
          .queue();
      UtilService.getInstance().sendMessageToWebhook(WEBHOOK_URL,
          String.format("%s (`%s`) joined patreon Membership", user.getName(), user.getId()));
    });

  }

  public void setReactionsForCorrectGuess(MessageReceivedEvent event) {
    long authorId = event.getAuthor().getIdLong();
    if (isUserPatron(authorId)) {
      List<CustomEmoji> emotes = event.getMessage().getMentions().getCustomEmojis();
      if (emotes.isEmpty()) {
        event.getMessage().reply("You must include a custom emoji")
            .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
        return;
      }
      CustomEmoji e = emotes.get(0);
      correctGuessReactions.put(authorId, e.getName() + ":" + e.getIdLong());
      PatronDao.getInstance().setCorrectReaction(authorId, e.getIdLong(), e.getName());
      event.getMessage().reply(e.getAsMention() + " reaction will be added to your correct guesses!").queue();
      return;
    } else {
      event.getMessage().reply("You must be a patreon supporter to use this command!")
          .queue(message -> message.delete().queueAfter(8, TimeUnit.SECONDS));
      return;
    }
  }

  public void setReactionsForWrongGuess(MessageReceivedEvent event) {
    long authorId = event.getAuthor().getIdLong();
    if (isUserPatron(authorId)) {
      List<CustomEmoji> emotes = event.getMessage().getMentions().getCustomEmojis();
      if (emotes.isEmpty()) {
        event.getMessage().reply("You must include a custom emoji")
            .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
        return;
      }
      CustomEmoji e = emotes.get(0);
      wrongGuessReactions.put(authorId, e.getName() + ":" + e.getIdLong());
      PatronDao.getInstance().setWrongReaction(authorId, e.getIdLong(), e.getName());
      event.getMessage().reply(e.getAsMention() + " reaction will be added to your wrong guesses!").queue();
      return;
    } else {
      event.getMessage().reply("You must be a patreon supporter to use this command!")
          .queue(message -> message.delete().queueAfter(8, TimeUnit.SECONDS));
      return;
    }
  }

  public void removeReactionsForWrongGuess(MessageReceivedEvent event) {
    long authorId = event.getAuthor().getIdLong();
    if (isUserPatron(authorId)) {
      if (wrongGuessReactions.containsKey(authorId)) {
        wrongGuessReactions.remove(authorId);
        PatronDao.getInstance().removeWrongReaction(authorId);
        event.getMessage().reply("Reactions for wrong guesses has been removed").queue();
        return;
      } else {
        event.getMessage().reply("No reaction set previously")
            .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
      }
    } else {
      event.getMessage().reply("You must be a patreon supporter to use this command!")
          .queue(message -> message.delete().queueAfter(8, TimeUnit.SECONDS));
      return;
    }
  }

  public void handlePatreonCommand(SlashCommandInteractionEvent event) {
    long validity = PatronDao.getInstance().getUserPatreonValidity(event.getUser().getIdLong());
    if (validity >= System.currentTimeMillis()) {
      event.getHook().sendMessage("<:flagbot:1230836096548601907> **|** You are a active patreon member\n" +
          "Your Membership will expire on " + TimeFormat.DATE_TIME_SHORT.atTimestamp(validity))
          .addActionRow(Button.primary("viewPatreonPerks", "View Active Perks"))
          .queue();
    } else {
      event.getHook().sendMessage("<:flagbot:1230836096548601907> **|** Become a Flag Bot Patron Today\n" +
          "<:blank:1223533175960109106> **|** Support your favourite bot financially and enjoy special perks")
          .addActionRow(Button.link("https://www.patreon.com/FlagBot", "Patreon"),
              Button.primary("viewPatreonPerks", "View Perks"))
          .queue();
    }
  }

  public void showPatreonPerks(ButtonInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Patreon Perks");
    eb.setColor(Color.pink);
    eb.setDescription(
        "Access to following commands :\n> `/guess distance`\n> `/guess location`\n> `/atlas quick`\n> `/atlas rapid`\nCustom Reactions for correct and wrong guesses\nCustomize Atlas Game");
    eb.addField("__How to set custom reactions__ ?",
        "for correct guesses : `f!set correct_guess :emote:`\nfor wrong guesses : `f!set wrong_guess :emoji:`\nto remove wrong guess reaction : `f!remove wrong_guess`",
        false);
    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
  }

  public boolean isUserPatron(long userId) {
    return patronUsers.contains(userId);
  }

  public boolean hasUserCustomCorrectReactions(long userId) {
    return this.correctGuessReactions.containsKey(userId);
  }

  public boolean hasUserCustomWrongReactions(long userId) {
    return this.wrongGuessReactions.containsKey(userId);
  }

  public Emoji getCorrectReaction(long userId) {
    return Emoji.fromFormatted("<:" + this.correctGuessReactions.get(userId) + ">");
  }

  public Emoji getWrongReaction(long userId) {
    return Emoji.fromFormatted("<:" + this.wrongGuessReactions.get(userId) + ">");
  }

  public void setPatreonWebhookUrl(String webhookUrl) {
    this.WEBHOOK_URL = webhookUrl;
  }

}
