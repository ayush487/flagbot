package com.ayushtech.flagbot.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.dbconnectivity.LevelsDao;
import com.ayushtech.flagbot.utils.LevelData;
import com.opencsv.CSVReader;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LevelAppendService {
  private static LevelAppendService instance = null;

  private LevelAppendService() {
  }

  public static LevelAppendService getInstance() {
    if (instance == null) {
      instance = new LevelAppendService();
    }
    return instance;
  }

  public void handleLevelAddCommand(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    OptionMapping optionMapping = event.getOption("levelfile");
    optionMapping.getAsAttachment().getProxy().download().thenAccept(inputStream -> {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      CSVReader csvReader = new CSVReader(reader);
      List<LevelData> levels = new ArrayList<>();
      try {
        String records[];
        while ((records = csvReader.readNext()) != null) {
          int levelNumber = Integer.parseInt(records[0]);
          String mainWord = records[1];
          String words = records[2];
          String levelData = records[3];
          levels.add(new LevelData(levelNumber, mainWord, words, levelData));
        }
        csvReader.close();
        LevelsDao.getInstance().addLevels(levels);
        event.getHook().sendMessage("Levels added successfully!").queue();
      } catch (IOException e) {
        e.printStackTrace();
        event.getHook().sendMessage("Something went wrong while adding levels.\nTry Again!").queue();
        return;
      }
    });

  }
}
