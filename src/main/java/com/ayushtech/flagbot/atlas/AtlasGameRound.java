package com.ayushtech.flagbot.atlas;

public interface AtlasGameRound {

  String thumbnailUrl = "https://cdn.discordapp.com/attachments/1133277774010925206/1319633697279840287/robot_thinking.jpg?ex=6766ac27&is=67655aa7&hm=37cb0c0496cb40b04a6adca76072feef4b175bf3b78250ffd46e82c50207bdf9&";
  String flagbotURL = "https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png";

  int handleAnswer(long userId, String answer);
  
}
