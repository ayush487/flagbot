package com.ayushtech.flagbot.game.fight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FightUtils {

  private String bar1empty = Emoji.fromCustom("bar1empty", 1195296826132287541l, false).getAsMention();
  private String bar1half = Emoji.fromCustom("bar1half", 1195297050993115246l, true).getAsMention();
  private String bar1full = Emoji.fromCustom("bar1full", 1195297246464442368l, true).getAsMention();
  private String bar1max = Emoji.fromCustom("bar1max", 1195297353591173201l, true).getAsMention();
  private String bar2empty = Emoji.fromCustom("bar2empty", 1195297658567413790l, false).getAsMention();
  private String bar2half = Emoji.fromCustom("bar2half", 1195297926734426162l, true).getAsMention();
  private String bar2full = Emoji.fromCustom("bar2full", 1195298061522587659l, true).getAsMention();
  private String bar2max = Emoji.fromCustom("bar2max", 1195298660800528434l, true).getAsMention();
  private String bar3empty = Emoji.fromCustom("bar3empty", 1195298974429618207l, false).getAsMention();
  private String bar3half = Emoji.fromCustom("bar3half", 1195299147499192362l, true).getAsMention();
  private String bar3full = Emoji.fromCustom("bar3full", 1195299364759941131l, true).getAsMention();

  private static FightUtils fightUtils = null;
  private List<String> isoList;
  private Random random;

  private FightUtils() {
    random = new Random();
    isoList = new ArrayList<>(200);
    loadIsoList();
  }

  public static synchronized FightUtils getInstance() {
    if (fightUtils == null) {
      fightUtils = new FightUtils();
    }
    return fightUtils;
  }

  public void sendEmbedForPunch(ButtonInteractionEvent event) {
    CountryOptions o = getOptions();
    FightHandler.getInstance().fightGameMap.get(event.getChannel().getIdLong()).setCountryOptions(o);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select correct country");
    eb.setImage("https://flagcdn.com/256x192/" + o.getCorrectOption().getIsoCode() + ".png");
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary("punchSelection-" + o.getOptions()[0].getIsoCode(), o.getOptions()[0].getName()),
            Button.primary("punchSelection-" + o.getOptions()[1].getIsoCode(), o.getOptions()[1].getName()),
            Button.primary("punchSelection-" + o.getOptions()[2].getIsoCode(), o.getOptions()[2].getName()),
            Button.primary("punchSelection-" + o.getOptions()[3].getIsoCode(), o.getOptions()[3].getName()))
        .setEphemeral(true)
        .queue();
  }

  public void sendEmbedForKick(ButtonInteractionEvent event) {
    CountryOptions o = getOptions();
    FightHandler.getInstance().fightGameMap.get(event.getChannel().getIdLong()).setCountryOptions(o);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select correct country");
    eb.setImage(GuessGameUtil.getInstance().getMapImage(o.getCorrectOption().getName()));
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary("kickSelection-" + o.getOptions()[0].getIsoCode(), o.getOptions()[0].getName()),
            Button.primary("kickSelection-" + o.getOptions()[1].getIsoCode(), o.getOptions()[1].getName()),
            Button.primary("kickSelection-" + o.getOptions()[2].getIsoCode(), o.getOptions()[2].getName()),
            Button.primary("kickSelection-" + o.getOptions()[3].getIsoCode(), o.getOptions()[3].getName()))
        .setEphemeral(true)
        .queue();
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

  public boolean hasMoney(long user1, long user2, long amount) {
    long user1Balance = CoinDao.getInstance().getBalance(user1);
    long user2Balance = CoinDao.getInstance().getBalance(user2);
    return user1Balance >= amount && user2Balance >= amount;
  }

  public void transferMoney(long winner, long loser, long amount) {
    CoinDao.getInstance().addCoins(winner, amount);
    CoinDao.getInstance().addCoins(loser, -1 * amount);
  }

  public String getProgressBar(int fill) {
    int progressBarFillAmount = Math.round(fill / 10.0f) * 10;
    String progressBar;
    switch (progressBarFillAmount) {
      case 0:
        progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
        break;
      case 10:
        progressBar = bar1half + bar2empty + bar2empty + bar2empty + bar3empty;
        break;
      case 20:
        progressBar = bar1full + bar2empty + bar2empty + bar2empty + bar3empty;
        break;
      case 30:
        progressBar = bar1max + bar2half + bar2empty + bar2empty + bar3empty;
        break;
      case 40:
        progressBar = bar1max + bar2full + bar2empty + bar2empty + bar3empty;
        break;
      case 50:
        progressBar = bar1max + bar2max + bar2half + bar2empty + bar3empty;
        break;
      case 60:
        progressBar = bar1max + bar2max + bar2full + bar2empty + bar3empty;
        break;
      case 70:
        progressBar = bar1max + bar2max + bar2max + bar2half + bar3empty;
        break;
      case 80:
        progressBar = bar1max + bar2max + bar2max + bar2full + bar3empty;
        break;
      case 90:
        progressBar = bar1max + bar2max + bar2max + bar2max + bar3half;
        break;
      case 100:
        progressBar = bar1max + bar2max + bar2max + bar2max + bar3full;
        break;
      default:
        progressBar = bar1empty + bar2empty + bar2empty + bar2empty + bar3empty;
        break;
    }
    return progressBar;
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
