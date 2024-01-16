package com.ayushtech.flagbot.game.fight;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class FightHandler {

  private static FightHandler fightHandler = null;
  public HashMap<Long, FightGame> fightGameMap;
  private ScheduledThreadPoolExecutor endIgnoredBattleService;
  private ScheduledThreadPoolExecutor endDrawnBattlesService;

  private FightHandler() {
    fightGameMap = new HashMap<>();
    endIgnoredBattleService = new ScheduledThreadPoolExecutor(10);
    endDrawnBattlesService = new ScheduledThreadPoolExecutor(10);
  }

  public static synchronized FightHandler getInstance() {
    if (fightHandler == null) {
      fightHandler = new FightHandler();
      return fightHandler;
    }
    return fightHandler;
  }

  public void handleFightCommand(SlashCommandInteractionEvent event) {
    User player1 = event.getUser();
    User player2 = event.getOption("opponent").getAsUser();
    if (player2.isBot() || player2.isSystem()) {
      event.getHook().sendMessage("You can't fight bots! haha").setEphemeral(true).queue();
      return;
    }
    if (player1.getName().equals(player2.getName())) {
      event.getHook().sendMessage("You cannot fight yourself on internet").setEphemeral(true).queue();
      return;
    }

    if (fightGameMap.containsKey(event.getChannel().getIdLong())) {
      event.reply("There already a game running in the channel!\nPlease use a different channel")
          .setEphemeral(true).queue();
      return;
    }

    event.deferReply().queue();

    OptionMapping betOption = event.getOption("bet");
    int betAmount = betOption == null ? 0 : betOption.getAsInt();
    betAmount = betAmount <= 0 ? 0 : betAmount;
    betAmount = betAmount >= 10000 ? 10000 : betAmount;
    if(!FightUtils.getInstance().hasMoney(player1.getIdLong(), player2.getIdLong(), betAmount)) {
      event.getHook().sendMessage("Can't start the battle (You or your opponent don't have enough coins to bet)").queue();
      return;
    }
    event.getHook().sendMessage("Starting battle!").queue();
    FightGame fightGame = new FightGame(event.getChannel(), player1, player2, betAmount);
    fightGameMap.put(event.getChannel().getIdLong(), fightGame);
    endIgnoredBattleService.schedule(() -> {
      if (!fightGame.isInteracted()) {
        fightGame.endGameAsTimeout();
        fightGameMap.remove(event.getChannel().getIdLong());
      }
    }, 60, TimeUnit.SECONDS);
  }

  public void handleCancelButton(ButtonInteractionEvent event) {
    if (fightGameMap.get(event.getChannel().getIdLong()).hasAuthorities(event.getUser())) {
      fightGameMap.get(event.getChannel().getIdLong()).endGameAsCancelled(event);
      fightGameMap.remove(event.getChannel().getIdLong());
    } else {
      event.reply("You can't cancel someone's else battle!").setEphemeral(true).queue();
    }
  }

  public void handleAcceptButton(ButtonInteractionEvent event) {
    if (fightGameMap.get(event.getChannel().getIdLong()).canAccept(event.getUser())) {
      fightGameMap.get(event.getChannel().getIdLong()).startBattle(event);
      endDrawnBattlesService.schedule(() -> {
        if (fightGameMap.containsKey(event.getChannel().getIdLong())
            && fightGameMap.get(event.getChannel().getIdLong()).getMessage().getIdLong() == event.getMessageIdLong()) {
          fightGameMap.get(event.getChannel().getIdLong()).endGameAsDraw();
          fightGameMap.remove(event.getChannel().getIdLong());
        }
      }, 5, TimeUnit.MINUTES);
    } else {
      event.reply("You can't use that button").setEphemeral(true).queue();
    }
  }

  public void handlePunchButton(ButtonInteractionEvent event) {
    if (fightGameMap.get(event.getChannel().getIdLong()).isUserTurn(event.getUser())
        && fightGameMap.get(event.getChannel().getIdLong()).isOptionsNull()) {
      FightUtils.getInstance().sendEmbedForPunch(event);
    } else {
      event.reply("You can't use this button").setEphemeral(true).queue();
    }
  }

  public void handleKickButton(ButtonInteractionEvent event) {
    if (fightGameMap.get(event.getChannel().getIdLong()).isUserTurn(event.getUser())
        && fightGameMap.get(event.getChannel().getIdLong()).isOptionsNull()) {
      FightUtils.getInstance().sendEmbedForKick(event);
    } else {
      event.reply("You can't use this button").setEphemeral(true).queue();
    }
  }

  public void handleRunButton(ButtonInteractionEvent event) {
    if (fightGameMap.get(event.getChannel().getIdLong()).canAccept(event.getUser())) {
      event.deferEdit().queue();
      fightGameMap.get(event.getChannel().getIdLong()).endGameAsRun(event.getUser().getIdLong());
      fightGameMap.remove(event.getChannel().getIdLong());
    } else {
      event.reply("You can't use that button!").setEphemeral(true).queue();
    }
  }

  public void handleSelection(ButtonInteractionEvent event, Damage type, String countryIso) {
    if (fightGameMap.containsKey(event.getChannel().getIdLong())) {
      event.deferEdit().queue();
      fightGameMap.get(event.getChannel().getIdLong()).handleDamage(type, countryIso);
      event.getHook().deleteOriginal().queue();
    } else {
      event.reply("Something went wrong!").setEphemeral(true).queue();
    }
  }
}