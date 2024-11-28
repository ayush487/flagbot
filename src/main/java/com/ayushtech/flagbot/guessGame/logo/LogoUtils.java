package com.ayushtech.flagbot.guessGame.logo;

import java.util.Random;

import com.ayushtech.flagbot.guessGame.GuessGameUtil;

public class LogoUtils {

  private static LogoUtils logoUtils = null;

  private Random random;
  private LogoUtils() {
    random = new Random();
  };
  public static LogoUtils getInstance() {
    if (logoUtils==null) {
      logoUtils = new LogoUtils();
    }
    return logoUtils;
  }
  public LogoOptions getOptions() {
    String[] codeOptions = GuessGameUtil.getInstance().getCodeOptions();
    String[] nameOptions = GuessGameUtil.getInstance().getNameOptions(codeOptions);
    int randomCorrect = random.nextInt(4);
    String correctOption = codeOptions[randomCorrect];
    return new LogoOptions(correctOption, codeOptions, nameOptions);
  }

}
