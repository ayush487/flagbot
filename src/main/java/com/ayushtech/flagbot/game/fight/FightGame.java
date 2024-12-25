package com.ayushtech.flagbot.game.fight;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FightGame {

  private User player1;
  private User player2;
  private MessageChannelUnion channel;
  private int betAmount;
  private boolean isInteracted;
  private int player1Hp;
  private int player2Hp;
  private String whoseTurn;
  private CountryOptions options;
  private Message message;

  public FightGame(MessageChannelUnion channel, User player1, User player2, int betAmount) {
    options = null;
    this.channel = channel;
    this.player1 = player1;
    this.player2 = player2;
    this.betAmount = betAmount;
    this.player1Hp = 100;
    this.player2Hp = 100;
    isInteracted = false;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Battle");
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setDescription(player2.getAsMention() + ", you are challenged by **" + player1.getName() + "** into battle!");
    eb.setColor(Color.YELLOW);
    String betText = betAmount > 0 ? String.valueOf(betAmount) : "Friendly Battle";
    eb.addField("Bet Amount", betText, false);
    this.channel.sendMessageEmbeds(eb.build())
        .setActionRow(Button.danger("rejectBattle", "Reject"), Button.success("acceptBattle", "Accept"))
        .queue(message -> setMessage(message));
  }

  public void handleDamage(Damage type, String selectedIsoCode) {
    int dmg = type.equals(Damage.PUNCH) ? 10 + (int) Math.round(Math.random() * 10)
        : 20 + (int) Math.round(Math.random() * 10);
    String lastRoundText = null;
    if (options.getCorrectOption().getIsoCode().equals(selectedIsoCode)) {
      if (whoseTurn.equals(player1.getName())) {
        player2Hp -= dmg;
        lastRoundText = type.equals(Damage.PUNCH)
            ? whoseTurn + " hit a punch on " + player2.getName() + " damaging him for " + dmg
            : whoseTurn + " land a kick on " + player2.getName() + " damaging him for " + dmg;
        whoseTurn = player2.getName();
      } else if (whoseTurn.equals(player2.getName())) {
        player1Hp -= dmg;
        lastRoundText = type.equals(Damage.PUNCH)
            ? whoseTurn + " hit a punch on " + player1.getName() + " damaging him for " + dmg
            : whoseTurn + " land a kick on " + player1.getName() + " damaging him for " + dmg;
        whoseTurn = player1.getName();
      }
    } else {
      lastRoundText = type.equals(Damage.PUNCH)
            ? whoseTurn + " trying to punch but failed damaging himself for " + dmg
            : whoseTurn + " trying to kick but failed damaging himself for " + dmg;
      if (whoseTurn.equals(player1.getName())) {
        player1Hp -= dmg;
        whoseTurn = player2.getName();
      } else if (whoseTurn.equals(player2.getName())) {
        player2Hp -= dmg;
        whoseTurn = player1.getName();
      }
    }
    if (player1Hp <= 0 || player2Hp <= 0) {
      endBattle(lastRoundText);
      return;
    }
    setCountryOptions(null);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(player1.getName() + " vs " + player2.getName());
    eb.setDescription(betAmount == 0 ? "**Friendly battle**" : "**Bet** : " + betAmount + " :coin:");
    eb.addField(player1.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player1Hp) + " **" + player1Hp + "%**", false);
    eb.addField(player2.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player2Hp) + " **" + player2Hp + "%**", false);
    eb.addField("Last Round", lastRoundText, false);
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setFooter(whoseTurn + "'s turn");
    eb.setColor(Color.green);
    message.editMessageEmbeds(eb.build())
        .setActionRow(Button.primary("punchInBattle", "Punch"),
            Button.primary("kickInBattle", "Kick"), Button.primary("runInBattle", "Run"))
        .queue();
  }

  public void startBattle(ButtonInteractionEvent event) {
    isInteracted = true;
    whoseTurn = player2.getName();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(player1.getName() + " vs " + player2.getName());
    eb.setDescription(betAmount == 0 ? "**Friendly battle**" : "**Bet** : " + betAmount + " :coin:");
    eb.addField(player1.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player1Hp) + " **" + player1Hp + "%**", false);
    eb.addField(player2.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player2Hp) + " **" + player2Hp + "%**", false);
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setFooter(whoseTurn + "'s turn");
    eb.setColor(Color.green);
    event.editMessageEmbeds(eb.build()).setActionRow(Button.primary("punchInBattle", "Punch"),
        Button.primary("kickInBattle", "Kick"), Button.primary("runInBattle", "Run")).queue();
  }

  public void endBattle(String lastRoundText) {
    String footerText = null;
    if (player1Hp <= 0) {
      footerText = player2.getName() + " won!";
      if (betAmount>0) {
        FightUtils.getInstance().transferMoney(player2.getIdLong(), player1.getIdLong(), betAmount);
      }
    } else if (player2Hp <= 0) {
      footerText = player1.getName() + " won!";
      if (betAmount>0) {
        FightUtils.getInstance().transferMoney(player1.getIdLong(), player2.getIdLong(), betAmount);
      }
    }
    FightHandler.getInstance().fightGameMap.remove(channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(player1.getName() + " vs " + player2.getName());
    eb.setDescription(betAmount == 0 ? "**Friendly battle**" : "**Bet** : " + betAmount + " :coin:");
    eb.addField(player1.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player1Hp) + " **" + player1Hp + "%**", false);
    eb.addField(player2.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player2Hp) + " **" + player2Hp + "%**", false);
    eb.addField("Last Round", lastRoundText, false);
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setFooter(footerText);
    eb.setColor(Color.green);
    message.editMessageEmbeds(eb.build())
        .setActionRow(Button.success("winLabel", "Batte end").asDisabled())
        .queue();
  }

  public void endGameAsCancelled(ButtonInteractionEvent event) {
    isInteracted = true;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Battle");
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setDescription(player2.getAsMention() + ", you are challenged by **" + player1.getName() + "** into battle!");
    eb.setFooter("Battle cancelled!");
    eb.setColor(Color.red);
    event.editMessageEmbeds(eb.build()).setActionRow(Button.danger("fightCancelled", "Cancelled").asDisabled()).queue();
  }

  public void endGameAsTimeout() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Battle");
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setDescription(player2.getAsMention() + ", you are challenged by **" + player1.getName() + "** into battle!");
    eb.setFooter("Battle cancelled due to no response from opponent!");
    eb.setColor(Color.gray);
    message.editMessageEmbeds(eb.build())
        .setActionRow(Button.danger("fightTimeout", "Timed Out").asDisabled()).queue();
  }

  public void endGameAsDraw() {
    isInteracted = true;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Battle Drawn");
    eb.setDescription(betAmount == 0 ? "**Friendly battle**" : "**Bet** : " + betAmount + " :coin:");
    eb.addField(player1.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player1Hp) + " **" + player1Hp + "%**", false);
    eb.addField(player2.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player2Hp) + " **" + player2Hp + "%**", false);
    eb.setFooter("Battle was too long!");
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setColor(Color.gray);
    message.editMessageEmbeds(eb.build())
        .setActionRow(Button.secondary("battleDrawn", "Battle Draw").asDisabled()).queue();

  }

  public void endGameAsRun(long userId) {
    String lastRoundText = null;
    String footerText = null;
    if(player1.getIdLong()==userId) {
      lastRoundText = "**" + player1.getName() + "** ran away from the battle.";
      footerText = player2.getName() + " is winner!";
      if (betAmount>0) {
        FightUtils.getInstance().transferMoney(player2.getIdLong(), player1.getIdLong(), betAmount);
      }
    } else if(player2.getIdLong()==userId) {
      lastRoundText = "**" + player2.getName() + "** ran away from the battle.";
      footerText = player1.getName() + " is winner!";
      if (betAmount>0) {
        FightUtils.getInstance().transferMoney(player1.getIdLong(), player2.getIdLong(), betAmount);
      }
    }
    isInteracted = true;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(player1.getName() + " vs " + player2.getName());
    eb.setDescription(betAmount == 0 ? "**Friendly battle**" : "**Bet** : " + betAmount + " :coin:");
    eb.addField(player1.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player1Hp) + " **" + player1Hp + "%**", false);
    eb.addField(player2.getName() + "'s hp",
        FightUtils.getInstance().getProgressBar(player2Hp) + " **" + player2Hp + "%**", false);
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1194298423139127487/c2b78208-0e86-4778-8317-41b128c3dcfd-removebg-preview.png?ex=65afd815&is=659d6315&hm=eeb00fdbfd13aa4c6075658a75c177e6f976b7567eeef1f570c79b27b93d6c34&=&format=webp&quality=lossless&width=200&height=200");
    eb.setFooter(footerText);
    eb.setColor(Color.red);
    eb.addField("Last Round", lastRoundText, false);
    message.editMessageEmbeds(eb.build()).setActionRow(Button.danger("battleEnd", "Battle end").asDisabled()).queue();
  }

  public void setCountryOptions(CountryOptions opt) {
    this.options = opt;
  }

  public CountryOptions geCountryOptions() {
    return this.options;
  }

  public boolean isOptionsNull() {
    return options == null;
  }

  public boolean hasAuthorities(User user) {
    if (user.getName().equals(player1.getName()) || user.getName().equals(player2.getName())) {
      return true;
    }
    return false;
  }

  public boolean isUserTurn(User user) {
    return user.getName().equals(whoseTurn);
  }

  public boolean canAccept(User user) {
    return user.getName().equals(player2.getName());
  }
  public Message getMessage() {
    return this.message;
  }
  private void setMessage(Message message){
    this.message = message;
  }

  public int getBetAmount() {
    return this.betAmount;
  }

  public boolean isInteracted() {
    return this.isInteracted;
  }
}