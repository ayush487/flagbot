package com.ayushtech.flagbot.race;

public class MathOption {
  private String expression;
  private int correctAnswer;
  private int[] options;
  public MathOption(String expression, int correctAnswer, int[] options) {
    this.expression = expression;
    this.correctAnswer = correctAnswer;
    this.options = options;
  }
  public String getExpression() {
    return this.expression;
  }
  public int getCorrectOption() {
    return correctAnswer;
  }
  public int getOption(int index) {
    return options[index];
  }
  public boolean isCorrectOption(int index) {
    return correctAnswer==options[index];
  }
}
