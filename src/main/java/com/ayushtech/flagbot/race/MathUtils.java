package com.ayushtech.flagbot.race;

import java.util.Random;

public class MathUtils {
  private static MathUtils mathUtils = null;
  private Random random;

  private MathUtils() {
    random = new Random();
  }

  public static MathUtils getInstance() {
    if (mathUtils == null) {
      mathUtils = new MathUtils();
    }
    return mathUtils;
  }

  public MathOption getMathOption() {
    int type = random.nextInt(4);
    switch (type) {
      case 0:
        return getMathOptionAddition();
      case 1:
        return getMathOptionSubstraction();
      case 2:
        return getMathOptionMultiplication();
      default:
        return getMathOptionDivision();
    }
  }

  private MathOption getMathOptionMultiplication() {
    int num1 = random.nextInt(100);
    int num2 = random.nextInt(100);
    int product = num1 * num2;
    int[] options = new int[4];
    int position = random.nextInt(4);
    for (int i = 0; i < 4; i++) {
      if (i == position) {
        options[i] = product;
      } else {
        do {
          options[i] = product - 100 + random.nextInt(200);
        } while (options[i] == product);
      }
    }
    return new MathOption(String.format("%d * %d", num1, num2), product, options);
  }

  private MathOption getMathOptionSubstraction() {
    int num1 = random.nextInt(10000);
    int num2 = random.nextInt(num1);
    int difference = num1 - num2;
    int[] options = new int[4];
    int position = random.nextInt(4);
    for (int i = 0; i < 4; i++) {
      if (i == position) {
        options[i] = difference;
      } else {
        do {
          options[i] = difference - 100 + random.nextInt(200);
        } while (options[i] == difference);
      }
    }
    return new MathOption(String.format("%d - %d", num1, num2), difference, options);
  }

  private MathOption getMathOptionAddition() {
    int num1 = random.nextInt(10000);
    int num2 = random.nextInt(10000);
    int sum = num1 + num2;
    int[] options = new int[4];
    int position = random.nextInt(4);
    for (int i = 0; i < 4; i++) {
      if (i == position) {
        options[i] = sum;
      } else {
        do {
          options[i] = sum - 100 + random.nextInt(200);
        } while (options[i] == sum);
      }
    }
    return new MathOption(String.format("%d + %d", num1, num2), sum, options);
  }

  private MathOption getMathOptionDivision() {
    int num2 = 1 + random.nextInt(30);
    int num1 = random.nextInt(1000) + num2;
    num1 = num1 - (num1 % num2);
    int quotient = num1 / num2;
    int[] options = new int[4];
    int position = random.nextInt(4);
    for (int i = 0; i < 4; i++) {
      if (i == position) {
        options[i] = quotient;
      } else {
        do {
          options[i] = quotient - 20 + random.nextInt(40);
        } while (options[i] == quotient);
      }
    }
    return new MathOption(String.format("%d ÷ %d", num1, num2), quotient, options);
  }
}
