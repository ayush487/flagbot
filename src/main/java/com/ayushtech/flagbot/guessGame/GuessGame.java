package com.ayushtech.flagbot.guessGame;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface GuessGame {
  
  void endGameAsWin(MessageReceivedEvent event);
  void endGameAsLose();
  void disableButtons();
  boolean guess(String guessString);
}
