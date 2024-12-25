package com.ayushtech.flagbot.memoflip;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Card {
  private int id;
  private Emoji emoji;
  private CardStatus status;

  public Card(int id, Emoji emoji) {
    this.id = id;
    this.emoji = emoji;
    this.status = CardStatus.HIDDEN;
  }

  public int getId() {
    return id;
  }

  public Emoji getEmoji() {
    return emoji;
  }

  public CardStatus getStatus() {
    return this.status;
  }

  public void setStatus(CardStatus status) {
    this.status = status;
  }

}