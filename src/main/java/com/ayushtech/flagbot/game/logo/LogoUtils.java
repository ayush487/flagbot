package com.ayushtech.flagbot.game.logo;

import java.util.Random;

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
    int[] opt = get4randomNumbers(LogoGame.brandList.size());
    String[] codeOptions = new String[4];
    codeOptions[0] = LogoGame.brandList.get(opt[0]);
    codeOptions[1] = LogoGame.brandList.get(opt[1]);
    codeOptions[2] = LogoGame.brandList.get(opt[2]);
    codeOptions[3] = LogoGame.brandList.get(opt[3]);
    String[] nameOptions = new String[4];
    nameOptions[0] = LogoGame.getLogoMap().get(codeOptions[0]);
    nameOptions[1] = LogoGame.getLogoMap().get(codeOptions[1]);
    nameOptions[2] = LogoGame.getLogoMap().get(codeOptions[2]);
    nameOptions[3] = LogoGame.getLogoMap().get(codeOptions[3]);
    int randomCorrect = random.nextInt(4);
    String correctOption = codeOptions[randomCorrect];
    return new LogoOptions(correctOption, codeOptions, nameOptions);
  }

  private int[] get4randomNumbers(int range) {
    int[] numbers = new int[4];
    numbers[0] = random.nextInt(range);
    do {
      numbers[1] = random.nextInt(range);
    } while (numbers[1] == numbers[0]);
    do {
      numbers[2] = random.nextInt(range);
    } while (numbers[2] == numbers[1] || numbers[2] == numbers[0]);
    do {
      numbers[3] = random.nextInt(range);
    } while (numbers[3] == numbers[2] || numbers[3] == numbers[1] || numbers[3] == numbers[0]);
    return numbers;
  }

}
