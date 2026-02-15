package com.ayushtech.flagbot.memoflip;

import java.awt.Color;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.MemoflipDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class Memoflip {
  private long userId;
  private Message message;
  private Card[][] cards;
  private Card selectedCard;
  private int points;
  private Difficulty difficulty;
  private boolean end;
  private int turns;
  private int rewards;

  public Memoflip(long userId, Card[][] cards, Message message, Difficulty difficulty, int rewards) {
    this.userId = userId;
    this.cards = cards;
    this.message = message;
    this.selectedCard = null;
    points = 0;
    this.difficulty = difficulty;
    this.end = false;
    this.turns = 0;
    this.rewards = rewards;
  }

  public void endGame(boolean isTimeout) {
    end();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription(String.format("**Difficulty** : `%s`\n**Turns** : `%d`\n__Reward__ : `%d` <:flag_coin:1472232340523843767>", difficulty, turns, rewards));
    if (isTimeout) {
      eb.setFooter("Game end due to time out!");
      eb.setColor(Color.red);
    }
    ActionRow[] rows = MemoflipHandler.getInstance().getButtonsAsDisabled(cards);
    message.editMessageEmbeds(eb.build()).setComponents(rows).queue();

  }

  public void endGameAsWin(ButtonInteractionEvent event) {
    end();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription(String.format("**Difficulty** : `%s`\n**Turns** : `%d`\n__Reward__ : `%d` <:flag_coin:1472232340523843767>", difficulty, turns, rewards));
    eb.setFooter("You win!");
    eb.setColor(Color.green);
    ActionRow[] rows = MemoflipHandler.getInstance().getButtonsAsDisabled(cards);
    event.editMessageEmbeds(eb.build())
        .setComponents(rows).queue();
    CoinDao.getInstance().addCoins(userId, (long)rewards);
    MemoflipDao.getInstance().setHighScore(userId, turns, difficulty);
  }

  public boolean isWin() {
    switch (difficulty) {
      case EASY:
        return points >= 4;
      case MEDIUM:
        return points >= 8;
      case HARD:
        return points >= 12;
      default:
        return false;
    }
  }

  public Card getCard(int i, int j) {
    return cards[i][j];
  }

  public Message getMessage() {
    return this.message;
  }

  public long getMessageId() {
    return this.message.getIdLong();
  }

  public Card getSelectedCard() {
    return selectedCard;
  }

  public boolean isCardSelected() {
    return selectedCard != null;
  }

  public void setSelectedCard(int i, int j) {
    this.selectedCard = cards[i][j];
  }

  public void removeSelection() {
    this.selectedCard = null;
  }

  public Card[][] getCards() {
    return this.cards;
  }

  public void increasePoints() {
    points = points >= 11 ? 12 : points + 1;
  }

  public int getPoints() {
    return this.points;
  }

  public long getUserId() {
    return this.userId;
  }

  public Difficulty getDifficulty() {
    return this.difficulty;
  }

  public boolean isNotEnd() {
    return !this.end;
  }

  public void end() {
    this.end = true;
  }

  public int getTurns() {
    return this.turns;
  }
  public void incrementTurns() {
    turns += 1;
  }
  public int getRewards() {
    return this.rewards;
  }
}
