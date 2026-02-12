package com.ayushtech.flagbot.race;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ayushtech.flagbot.guessGame.GuessGameUtil;

public class MathUtils {
  private static MathUtils mathUtils = null;
  private Random random;
  private List<String> isoList;

  private MathUtils() {
    random = new Random();
    isoList = new ArrayList<>(200);
    loadIsoList();
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

  public CountryOptions getOptions() {
    int[] options = get4randomNumbers(isoList.size());
    int winnerIndex = random.nextInt(4);
    String[] optionsAsCode = new String[4];
    optionsAsCode[0] = isoList.get(options[0]);
    optionsAsCode[1] = isoList.get(options[1]);
    optionsAsCode[2] = isoList.get(options[2]);
    optionsAsCode[3] = isoList.get(options[3]);
    String winnerIsoCode = optionsAsCode[winnerIndex];
    CountryPair winner = new CountryPair(winnerIsoCode, GuessGameUtil.getInstance().getCountryName(winnerIsoCode));
    CountryPair[] countryOptions = new CountryPair[4];
    countryOptions[0] = new CountryPair(optionsAsCode[0], GuessGameUtil.getInstance().getCountryName(optionsAsCode[0]));
    countryOptions[1] = new CountryPair(optionsAsCode[1], GuessGameUtil.getInstance().getCountryName(optionsAsCode[1]));
    countryOptions[2] = new CountryPair(optionsAsCode[2], GuessGameUtil.getInstance().getCountryName(optionsAsCode[2]));
    countryOptions[3] = new CountryPair(optionsAsCode[3], GuessGameUtil.getInstance().getCountryName(optionsAsCode[3]));
    return new CountryOptions(winner, countryOptions);
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

  private void loadIsoList() {
    isoList.add("af");
    isoList.add("al");
    isoList.add("dz");
    isoList.add("ao");
    isoList.add("ar");
    isoList.add("am");
    isoList.add("au");
    isoList.add("at");
    isoList.add("az");
    isoList.add("bs");
    isoList.add("bh");
    isoList.add("bd");
    isoList.add("bb");
    isoList.add("by");
    isoList.add("be");
    isoList.add("bz");
    isoList.add("bj");
    isoList.add("bt");
    isoList.add("bo");
    isoList.add("ba");
    isoList.add("bw");
    isoList.add("br");
    isoList.add("bn");
    isoList.add("bg");
    isoList.add("bf");
    isoList.add("bi");
    isoList.add("kh");
    isoList.add("cm");
    isoList.add("ca");
    isoList.add("cv");
    isoList.add("cf");
    isoList.add("td");
    isoList.add("cl");
    isoList.add("cn");
    isoList.add("co");
    isoList.add("km");
    isoList.add("cg");
    isoList.add("cr");
    isoList.add("ci");
    isoList.add("hr");
    isoList.add("cu");
    isoList.add("cy");
    isoList.add("cz");
    isoList.add("dk");
    isoList.add("dj");
    isoList.add("dm");
    isoList.add("do");
    isoList.add("ec");
    isoList.add("eg");
    isoList.add("sv");
    isoList.add("gq");
    isoList.add("er");
    isoList.add("ee");
    isoList.add("et");
    isoList.add("fj");
    isoList.add("fi");
    isoList.add("fr");
    isoList.add("ga");
    isoList.add("ge");
    isoList.add("de");
    isoList.add("gh");
    isoList.add("gr");
    isoList.add("gd");
    isoList.add("gt");
    isoList.add("gn");
    isoList.add("gw");
    isoList.add("gy");
    isoList.add("ht");
    isoList.add("hn");
    isoList.add("hu");
    isoList.add("is");
    isoList.add("in");
    isoList.add("id");
    isoList.add("ir");
    isoList.add("iq");
    isoList.add("ie");
    isoList.add("il");
    isoList.add("it");
    isoList.add("jm");
    isoList.add("jp");
    isoList.add("jo");
    isoList.add("kz");
    isoList.add("ke");
    isoList.add("ki");
    isoList.add("kp");
    isoList.add("kr");
    isoList.add("kw");
    isoList.add("kg");
    isoList.add("la");
    isoList.add("lv");
    isoList.add("lb");
    isoList.add("ls");
    isoList.add("lr");
    isoList.add("ly");
    isoList.add("li");
    isoList.add("lt");
    isoList.add("lu");
    isoList.add("mk");
    isoList.add("mg");
    isoList.add("mw");
    isoList.add("my");
    isoList.add("mv");
    isoList.add("ml");
    isoList.add("mt");
    isoList.add("mh");
    isoList.add("mr");
    isoList.add("mu");
    isoList.add("mx");
    isoList.add("fm");
    isoList.add("md");
    isoList.add("mc");
    isoList.add("mn");
    isoList.add("me");
    isoList.add("ma");
    isoList.add("mz");
    isoList.add("mm");
    isoList.add("na");
    isoList.add("nr");
    isoList.add("np");
    isoList.add("nl");
    isoList.add("nz");
    isoList.add("ni");
    isoList.add("ne");
    isoList.add("no");
    isoList.add("om");
    isoList.add("pk");
    isoList.add("pw");
    isoList.add("pa");
    isoList.add("pg");
    isoList.add("py");
    isoList.add("pe");
    isoList.add("ph");
    isoList.add("pl");
    isoList.add("pt");
    isoList.add("qa");
    isoList.add("tw");
    isoList.add("ro");
    isoList.add("ru");
    isoList.add("rw");
    isoList.add("kn");
    isoList.add("lc");
    isoList.add("vc");
    isoList.add("ws");
    isoList.add("sm");
    isoList.add("st");
    isoList.add("sa");
    isoList.add("sn");
    isoList.add("rs");
    isoList.add("sc");
    isoList.add("sl");
    isoList.add("sg");
    isoList.add("sk");
    isoList.add("si");
    isoList.add("sb");
    isoList.add("so");
    isoList.add("za");
    isoList.add("ss");
    isoList.add("es");
    isoList.add("lk");
    isoList.add("sd");
    isoList.add("sr");
    isoList.add("se");
    isoList.add("ch");
    isoList.add("sy");
    isoList.add("tj");
    isoList.add("tz");
    isoList.add("th");
    isoList.add("tl");
    isoList.add("tg");
    isoList.add("to");
    isoList.add("tt");
    isoList.add("tn");
    isoList.add("tr");
    isoList.add("tm");
    isoList.add("tv");
    isoList.add("ug");
    isoList.add("ua");
    isoList.add("ae");
    isoList.add("gb");
    isoList.add("us");
    isoList.add("uy");
    isoList.add("uz");
    isoList.add("vu");
    isoList.add("ve");
    isoList.add("vn");
    isoList.add("ye");
    isoList.add("zm");
    isoList.add("zw");
  }

}
