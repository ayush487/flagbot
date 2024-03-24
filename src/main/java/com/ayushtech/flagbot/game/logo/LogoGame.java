package com.ayushtech.flagbot.game.logo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ayushtech.flagbot.game.Game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LogoGame {

  private static Random random = new Random();
  private static Map<String, String> brandMap = new HashMap<>();
  private static List<String> brandList;
  private MessageChannel channel;
  private String brandCode;
  private MessageEmbed messageEmbed;
  private long messageId;

  static {
    LogoGame.loadBrands();
    LogoGame.loadBrandList();
  }

  public LogoGame(MessageChannel channel) {
    this.channel = channel;
    this.brandCode = getRandomBrand();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the Brand");
    eb.setImage(String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(235, 206, 129));
    messageEmbed = eb.build();
    this.channel.sendMessageEmbeds(messageEmbed)
        .setActionRow(Button.primary("skipLogo", "Skip"))
        .queue(msg -> setMessageId(msg.getIdLong()));
  }

  public void endGameAsWin(MessageReceivedEvent msgEvent) {
    LogoGameHandler.getInstance().getGameMap().remove(channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    eb.setDescription(msgEvent.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
        + Game.getAmount(msgEvent.getAuthor().getIdLong()) + "(+100)` " + ":coin:"
        + "  \n **Correct Answer :** " + brandMap.get(brandCode));
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(13, 240, 52));
    msgEvent.getChannel().sendMessageEmbeds(eb.build())
        .setActionRow(Button.primary("playAgainLogo", "Play Again")).queue();
    Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
    disableButton();
  }

  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the logo!");
    eb.setDescription("**Correct Answer :** \n" + brandMap.get(brandCode));
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(240, 13, 52));
    this.channel.sendMessageEmbeds(eb.build())
        .setActionRow(Button.primary("playAgainLogo", "Play Again"))
        .queue();
    LogoGameHandler.getInstance().endGame(channel.getIdLong());
    disableButton();
  }

  private String getRandomBrand() {
    return brandList.get(random.nextInt(brandList.size()));
  }

  private static void loadBrandList() {
    brandList = new ArrayList<>(brandMap.keySet());
  }

  public boolean guess(String guessWord) {
    return brandMap.get(brandCode).equalsIgnoreCase(guessWord);
  }

  private void disableButton() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(this.messageEmbed)
        .setActionRow(Button.primary("skipLogo", "Skip").asDisabled())
        .queue();
  }

  private void setMessageId(long id) {
    this.messageId = id;
  }

  private static void loadBrands() {
    brandMap.put("fkt", "Flipkart");
    brandMap.put("tgt", "Target");
    brandMap.put("azn", "Amazon");
    brandMap.put("shl", "Shell");
    brandMap.put("ggl", "Google");
    brandMap.put("apl", "Apple");
    brandMap.put("nik", "Nike");
    brandMap.put("rbk", "Reebok");
    brandMap.put("ppl", "Paypal");
    brandMap.put("mcd", "Mercedes-Benz");
    brandMap.put("fbk", "Facebook");
    brandMap.put("met", "Meta");
    brandMap.put("igm", "Instagram");
    brandMap.put("tla", "Tesla");
    brandMap.put("wsp", "Whatsapp");
    brandMap.put("sna", "Snapchat");
    brandMap.put("twt", "Twitter");
    brandMap.put("ads", "Adidas");
    brandMap.put("nfx", "Netflix");
    brandMap.put("cnl", "Chanel");
    brandMap.put("msf", "Microsoft");
    brandMap.put("dcd", "Discord");
    brandMap.put("dny", "Disney");
    brandMap.put("ssg", "Samsung");
    brandMap.put("lvt", "Louis Vuitton");
    brandMap.put("lg", "LG");
    brandMap.put("x", "X");
    brandMap.put("mdn", "McDonald's");
    brandMap.put("tyt", "Toyota");
    brandMap.put("itl", "Intel");
    brandMap.put("vis", "VISA");
    brandMap.put("msc", "MasterCard");
    brandMap.put("php", "PhonePe");
    brandMap.put("jio", "Jio");
    brandMap.put("atl", "Airtel");
    brandMap.put("vdf", "Vodafone");
    brandMap.put("tlr", "Telenor");
    brandMap.put("ibm", "IBM");
    brandMap.put("bmw", "BMW");
    brandMap.put("frr", "Ferrari");
    brandMap.put("hnd", "Honda");
    brandMap.put("psp", "Pepsi");
    brandMap.put("coc", "Coca Cola");
    brandMap.put("str", "Starbucks");
    brandMap.put("tat", "Tata");
    brandMap.put("adi", "Audi");
    brandMap.put("oly", "Olympics");
    brandMap.put("rbx", "Roblox");
    brandMap.put("mcf", "Minecraft");
    brandMap.put("tac", "Tacobell");
    brandMap.put("qkr", "Quaker");
    brandMap.put("scd", "Sound Cloud");
    brandMap.put("szm", "Shazam");
    brandMap.put("cnk", "Cartoon Network");
    brandMap.put("brn", "Baskin Robbin");
    brandMap.put("new", "New Era");
    brandMap.put("fbm", "Facebook Messenger");
    brandMap.put("brb", "Barbie");
    brandMap.put("ggd", "Google Drive");
    brandMap.put("swy", "Swiggy");
    brandMap.put("pok", "Pokemon");
    brandMap.put("rnt", "Renault");
    brandMap.put("opr", "Opera");
    brandMap.put("wik", "Wikipedia");
    brandMap.put("bth", "Bluetooth");
    brandMap.put("wif", "Wifi");
    brandMap.put("anb", "Angry Birds");
    brandMap.put("jag", "Jaguar");
    brandMap.put("bat", "Batman");
    brandMap.put("stm", "Steam");
    brandMap.put("rsr", "Rockstar");
    brandMap.put("ntl", "Nestle");
    brandMap.put("prs", "Porsche");
    brandMap.put("esp", "ESPN");
    brandMap.put("rbl", "Red Bull");
    brandMap.put("rlx", "Rolex");
    brandMap.put("del", "Dell");
    brandMap.put("hyu", "Hyundai");
    brandMap.put("nsh", "Nintendo Switch");
    brandMap.put("leg", "LEGO");
    brandMap.put("hua", "Huawei");
    brandMap.put("kfc", "KFC");
    brandMap.put("bkg", "Burger King");
    brandMap.put("vok", "Volkswagen");
    brandMap.put("tok", "Tiktok");
    brandMap.put("att", "AT&T");
    brandMap.put("nvd", "Nvidia");
    brandMap.put("wec", "WeChat");
    brandMap.put("sar", "Saudi Aramco");
    brandMap.put("msz", "Microsoft Azure");
    brandMap.put("frb", "Firebase");
    brandMap.put("mit", "Mitsui");
    brandMap.put("tch", "Twitch");
    brandMap.put("ytb", "YouTube");
    brandMap.put("for", "Ford");
    brandMap.put("lin", "LinkedIn");
    brandMap.put("bp", "BP");
    brandMap.put("stf", "Spotify");
    brandMap.put("aus", "Among US");
    brandMap.put("wwe", "WWE");
    brandMap.put("bch", "Bosch");
    brandMap.put("net", "Netease");
    brandMap.put("trd", "Threads");
    brandMap.put("uni", "Unilever");
    brandMap.put("lay", "Lay's");
    brandMap.put("vol", "Volvo");
    brandMap.put("kia", "Kia");
    brandMap.put("amd", "AMD");
    brandMap.put("psn", "PlayStation");
    brandMap.put("xbx", "Xbox");
    brandMap.put("nsa", "NASA");
    brandMap.put("lac", "Lacoste");
    brandMap.put("rel", "Reliance");
    brandMap.put("nbc", "NBC");
    brandMap.put("dom", "Dominos");
    brandMap.put("hcl", "HCL");
    brandMap.put("pin", "Pinterest");
    brandMap.put("dov", "Dove");
    brandMap.put("avg", "Avengers");
    brandMap.put("che", "Chevrolet");
    brandMap.put("mar", "Marvel Comics");
    brandMap.put("dcc", "DC Comics");
    brandMap.put("baj", "Bajaj");
    brandMap.put("pum", "Puma");
    brandMap.put("pzz", "Pizza Hut");
    brandMap.put("sky", "Skype");
    brandMap.put("red", "Red Hat");
    brandMap.put("and", "Android");
    brandMap.put("xmi", "Xiaomi");
    brandMap.put("fir", "Firefox");
    brandMap.put("edg", "Microsoft Edge");
    brandMap.put("mah", "Mahindra");
    brandMap.put("sbi", "State Bank of India");
    brandMap.put("sun", "Sunfeast");
    brandMap.put("gml", "Gmail");
    brandMap.put("ckn", "Calvin Klein");
    brandMap.put("bic", "Bic");
    brandMap.put("lam", "Lamborghini");
    brandMap.put("rdt", "Reddit");
    brandMap.put("sup", "Superman");
    brandMap.put("ggc", "Google Chrome");
    brandMap.put("vst", "Visual Studio");
    brandMap.put("cro", "Crocs");
    brandMap.put("gat", "Gatorade");
    brandMap.put("pri", "Pringles");
    brandMap.put("rap", "Rapid7");
    brandMap.put("asa", "Asana");
    brandMap.put("ana", "Anaplan");
    brandMap.put("nrc", "New Relic");
    brandMap.put("jam", "Jamf");
    brandMap.put("sps", "SPS Commerce");
    brandMap.put("sam", "Samsara");
    brandMap.put("app", "Appian");
    brandMap.put("dil", "Diligent");
    brandMap.put("sai", "Sailpoint");
    brandMap.put("sny", "Snyk");
    brandMap.put("pod", "Podium");
    brandMap.put("tri", "Tricentis");
    brandMap.put("fin", "Financial Force");
    brandMap.put("frk", "ForgeRock");
    brandMap.put("ris", "Redis");
    brandMap.put("amp", "Amplitude");
    brandMap.put("cla", "Clari");
    brandMap.put("cou", "Couchbase");
    brandMap.put("mca", "McAfee");
    brandMap.put("wor", "Workwave");
    brandMap.put("bby", "BlackBerry");
    brandMap.put("htc", "HTC");
    brandMap.put("zte", "ZTE");
    brandMap.put("son", "Sony");
    brandMap.put("pps", "Philips");
    brandMap.put("not", "Nothing");
    brandMap.put("lav", "LAVA");
    brandMap.put("int", "Intex");
    brandMap.put("whi", "Whirlpool");
    brandMap.put("hai", "Haier");
    brandMap.put("svr", "Sandbox VR");
    brandMap.put("2k", "2k");
    brandMap.put("pzp", "PrizePicks");
    brandMap.put("epi", "Epic Games");
    brandMap.put("poi", "PointsBet");
    brandMap.put("act", "Activision");
    brandMap.put("ubi", "Ubisoft");
    brandMap.put("gam", "Gameloft");
    brandMap.put("war", "Warner Bros");
    brandMap.put("eas", "Electronic Arts");
    brandMap.put("spi", "Spiderman");
    brandMap.put("bon", "Bonfire Studios");
    brandMap.put("pla", "Play Store");
    brandMap.put("can", "Canon");
    brandMap.put("tse", "T-series");
    brandMap.put("tin", "Tinder");
    brandMap.put("bum", "Bumble");
    brandMap.put("val", "Valve");
    brandMap.put("dco", "Dallas Cowboys");
    brandMap.put("pat", "New England Patriots");
    brandMap.put("ram", "Los Angeles Rams");
    brandMap.put("yan", "New York Yankees");
    brandMap.put("gia", "New Year Giants");
    brandMap.put("kni", "New Year Knicks");
    brandMap.put("bea", "Chicago Bears");
    brandMap.put("gsw", "Golden State Warriors");
    brandMap.put("rea", "Real Madrid");
    brandMap.put("fcb", "FC Barcelona");
    brandMap.put("mud", "Manchester United");
    brandMap.put("liv", "Liverpool");
    brandMap.put("bay", "Bayern Munich");
    brandMap.put("mcy", "Manchester City");
    brandMap.put("psg", "Paris Saint Germain");
    brandMap.put("cea", "Chelsea");
    brandMap.put("nud", "Newcastle United");
    brandMap.put("eve", "Everton");
    brandMap.put("acm", "AC Milan");
    brandMap.put("juv", "Juventus");
    brandMap.put("ars", "Arsenal");
    brandMap.put("rcb", "Royal Challengers Bangaluru");
    brandMap.put("csk", "Channai Super Kings");
    brandMap.put("mis", "Mumbai Indians");
    brandMap.put("wps", "Wordpress");
    brandMap.put("sla", "Slack");
    brandMap.put("unr", "Unreal");
    brandMap.put("uty", "Unity");
    brandMap.put("lux", "Linux");
    brandMap.put("tgm", "Telegram");
    brandMap.put("fig", "Figma");
    brandMap.put("doc", "Docker");
    brandMap.put("sfy", "Shopify");
    brandMap.put("med", "Medium");
    brandMap.put("quo", "Quora");
    brandMap.put("air", "Airbnb");
    brandMap.put("lne", "Line");
    brandMap.put("lba", "Gitlab");
    brandMap.put("sti", "Stripe");
    brandMap.put("spx", "SpaceX");
    brandMap.put("ude", "Udemy");
    brandMap.put("sub", "Subaru");
    brandMap.put("jee", "Jeep");
    brandMap.put("dod", "Dodge");
    brandMap.put("mas", "Maserati");
    brandMap.put("ben", "Bentley");
    brandMap.put("chr", "Chrysler");
    brandMap.put("cor", "Corvette");
    brandMap.put("cad", "Cadillac");
    brandMap.put("mda", "Mazda");
    brandMap.put("mus", "Mustang");
    brandMap.put("nis", "Nissan");
    brandMap.put("bug", "Bugati");
    brandMap.put("alf", "Alfa Romeo");
    brandMap.put("bui", "Buick");
    brandMap.put("lex", "Lexus");
    brandMap.put("rrc", "Rolls-Royce");
    brandMap.put("acu", "Acura");
    brandMap.put("ast", "Aston Martin");
    brandMap.put("mcl", "McLaren");
    brandMap.put("msu", "Mitsubishi");
    brandMap.put("gmc", "GMC");
    brandMap.put("inf", "Infiniti");
    brandMap.put("lco", "Lincoln");
    brandMap.put("peu", "Peugeot");
    brandMap.put("pon", "Pontiac");
    brandMap.put("saa", "Saab");
    brandMap.put("gen", "Genesis");
    brandMap.put("suz", "Suzuki");
    brandMap.put("cit", "Citroen");
    brandMap.put("fia", "Fiat");
    brandMap.put("lot", "Lotus");
    brandMap.put("ktm", "KTM");
    brandMap.put("lrr", "Land Rover");
    brandMap.put("may", "Maybach");
    brandMap.put("mer", "Mercury");
    brandMap.put("vip", "Dodge Viper");
    brandMap.put("mrf", "MRF");
    brandMap.put("koe", "Koenigsegg");
    brandMap.put("mac", "Mack");
    brandMap.put("sic", "Scion");
    brandMap.put("sko", "Skoda");
    brandMap.put("opl", "Opel");
    brandMap.put("dat", "Datsun");
    brandMap.put("hol", "Holden");
    brandMap.put("sma", "Smart");
    brandMap.put("alp", "Alpine");
    brandMap.put("ds", "DS");
    brandMap.put("rov", "Rover");
    brandMap.put("vau", "Vauxhall");
    brandMap.put("ari", "Ariel");
    brandMap.put("isu", "Isuzu");
    brandMap.put("sea", "Seat");
    brandMap.put("kar", "Karma");
    brandMap.put("luc", "Lucid");
    brandMap.put("sal", "Saleen");
    brandMap.put("wes", "Western Star");
    brandMap.put("hen", "Hennessey");
    brandMap.put("dac", "Dacia");
    brandMap.put("dai", "Daihatsu");
    brandMap.put("hin", "Hino");
    brandMap.put("sca", "Scania");
    brandMap.put("ste", "Sterling");
    brandMap.put("gwl", "Great Wall");
    brandMap.put("eic", "Eicher");
    brandMap.put("ash", "Ashok Leyland");
    brandMap.put("ih", "IH");
    brandMap.put("jcb", "JCB");
    brandMap.put("gee", "Geely");
    brandMap.put("lad", "Lada");
    brandMap.put("nob", "Noble");
    brandMap.put("pol", "Polestar");
    brandMap.put("mor", "Morris Garages");
    brandMap.put("vin", "Vinfast");
    brandMap.put("ssa", "SsangYong");
    brandMap.put("bra", "Brabus");
    brandMap.put("jet", "Jetta");
    brandMap.put("maz", "Maz");
    brandMap.put("bit", "Bitcoin");
    brandMap.put("smr", "SAIC Motor");
    brandMap.put("vlf", "VLF");
    brandMap.put("guc", "Gucci");
    brandMap.put("frc", "Force Motors");
    brandMap.put("adb", "Adobe");
    brandMap.put("twi", "Twilio");
    brandMap.put("her", "Hermes");
    brandMap.put("ola", "Ola");
    brandMap.put("kil", "Killer Jeans");
    brandMap.put("lul", "Lululemon");
    brandMap.put("lev", "Levi's");
    brandMap.put("tom", "Tommy Hilfiger");
    brandMap.put("git", "Github");
    brandMap.put("mon", "MongoDB");
    brandMap.put("ray", "Ray Ban");
    brandMap.put("dro", "Dropbox");
    brandMap.put("zoh", "Zoho");
    brandMap.put("hrk", "Heroku");
    brandMap.put("byj", "Byju's");
    brandMap.put("zsc", "Zscaler");
    brandMap.put("bur", "Burberry");
    brandMap.put("rei", "Reid & Taylor");
    brandMap.put("one", "Oneplus");
  }

  
}
