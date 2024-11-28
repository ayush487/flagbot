package com.ayushtech.flagbot.guessGame.logo;

public class LogoOptions {
  private String correctOption;
  private String[] codeOptions;
  private String[] nameOptions;
  public LogoOptions(String correctOption, String[] codeOptions, String[] nameOptions) {
    this.correctOption = correctOption;
    this.codeOptions = codeOptions;
    this.nameOptions = nameOptions;
  }
  public String getCorrectOption() {
    return correctOption;
  }
  public String[] getCodeOptions() {
    return codeOptions;
  }
  public String[] getNameOptions() {
    return nameOptions;
  }

  public boolean isCorrectOption(int index) {
    return codeOptions[index].equals(correctOption);
  }
  
}
