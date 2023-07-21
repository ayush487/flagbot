package com.ayush.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FlagGame {

    private static HashMap<String, String> countryMap = new HashMap<>(194);
    private static HashMap<Long, Integer> coinMap = new HashMap<>();
    private static HashMap<String, String> hintMap = new HashMap<>(194);
    private static ArrayList<String> isoList;
    private static Random random;
    private static String flagLink = "https://flagcdn.com/256x192/";
    private static String suffix = ".png";

    private String countryCode;
    private MessageChannel channel;


    static {
        random = new Random();
        loadCountries();
        loadHints();
        isoList = new ArrayList<String>(countryMap.keySet());
    }

    {
        countryCode = isoList.get(random.nextInt(isoList.size()));
    }

    public FlagGame (MessageChannel channel) {
        super();
        this.channel = channel;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Guess the Country Flag");
        eb.setImage(flagLink + countryCode + suffix);
        eb.setColor(new Color(38, 187, 237));
        channel.sendMessageEmbeds(eb.build())
            .setActionRow(Button.primary("skipButton", "Skip"))
            .queue();
    }
    
    void endGameAsWin(MessageReceivedEvent msgEvent) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Correct!");
        eb.setDescription(
            msgEvent.getAuthor().getAsMention() + 
            " is correct!\n**Coins :** `" + 
            getAmount(msgEvent.getAuthor().getIdLong()) + 
            "(+100)` " + ":coin:" + 
            "  \n **Correct Answer :** "+ 
            countryMap.get(countryCode)
        );
        eb.setThumbnail(flagLink+countryCode+suffix);
        eb.setColor(new Color(13, 240, 52));
        msgEvent.getChannel().sendMessageEmbeds(eb.build())
        .setActionRow(Button.primary("playAgainButton", "Play Again"))
        .queue();
        GameHandler.getInstance().getGameMap().remove(channel.getIdLong());
        increaseCoins(msgEvent.getAuthor().getIdLong(), 100);
    }

    public void endGameAsLose() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("No one guessed the country!");
        eb.setDescription("**Correct Answer :** \n"+ countryMap.get(countryCode));
        eb.setThumbnail(flagLink+countryCode+suffix);
        eb.setColor(new Color(240, 13, 52));
        this.channel.sendMessageEmbeds(eb.build())
        .setActionRow(Button.primary("playAgainButton", "Play Again"))
        .queue();
        GameHandler.getInstance().endGame(channel.getIdLong());
    }

    public boolean guess(String guessCountry) {
        if(countryMap.get(countryCode).equalsIgnoreCase(guessCountry)) {
            return true;
        }else {
            return false;
        }
    }

    private static synchronized void increaseCoins(long userId, int amount) {
        if(coinMap.containsKey(userId)) {
            int prevAmount = coinMap.get(userId);
            coinMap.put(userId, prevAmount+amount);
        } else {
            coinMap.put(userId, amount);
        }
    }

    private static void loadHints() {
        hintMap.put("af", "A landlocked country located at the crossroads of Central Asia and South Asia");
        hintMap.put("al", "Birthplace of Mother Teresa");
        hintMap.put("dz", "Home to the largest swath of the Sahara Desert");
        hintMap.put("ao", "Country in Central Africa");
        hintMap.put("ar", "Lionel Messi");
        hintMap.put("am", "First country to officially adopt Christianity");
        hintMap.put("au", "Kangaroos :kangaroo:");
        hintMap.put("at", "Hitler born here :skull:");
        hintMap.put("az", "Transcontinental country located at the boundary of Eastern Europe and West Asia");
        hintMap.put("bs", "Country in the Caribbean, you would probably visit if you love beaches");
        hintMap.put("bh", "Country in the Middle East");
        hintMap.put("bd", "Country in South Asia");
        hintMap.put("bb", "Country in the Caribbean");
        hintMap.put("by", "Country in Europe, was part of USSR");
        hintMap.put("be", "Headquarters of the European Union and NATO located here");
        hintMap.put("bz", "Country in Central America, Great Blue Hole located here");
        hintMap.put("bj", "Country in West Africa");
        hintMap.put("bt", "Country in South Asia on the Himalayas");
        hintMap.put("bo", "Country in South America");
        hintMap.put("ba", "Southeastern European country");
        hintMap.put("bw", "Country in Southern Africa");
        hintMap.put("br", "Known as country of footbal");
        hintMap.put("bn", "Islamic sultanate on the northern coast of the island of Borneo in Southeast Asia.");
        hintMap.put("bg", "Country in Southeastern Europe/A Balkans Country");
        hintMap.put("bf", "Country in West Africa");
        hintMap.put("bi", "Country in East Africa");
        hintMap.put("kh", "Southeast Asian nation");
        hintMap.put("cm", "Country in Central Africa");
        hintMap.put("ca", "Country in North America");
        hintMap.put("cv", "Country in Africa, also known as Cabo Verde");
        hintMap.put("cf", "Country in Central Africa");
        hintMap.put("td", "Country in Northern Central Africa");
        hintMap.put("cl", "Country in South America, spanish widely used here");
        hintMap.put("cn", "Was most populous country (now second)");
        hintMap.put("co", "Home to the world's second largest population of Spanish-speaking people");
        hintMap.put("km", "Island Country in East Africa");
        hintMap.put("cg", "Got divided into two nations in 1960");
        hintMap.put("cr", "Country in Central America");
        hintMap.put("ci", "World largest producer of cocoa beans");
        hintMap.put("hr", "Was on of the finalist in 2018 FIFA cup");
        hintMap.put("cu", "Country in the Caribbean");
        hintMap.put("cy", "Country in the Middle East");
        hintMap.put("cz", "Landlocked country in Central Europe");
        hintMap.put("cd", "Got split from a country into two in 1960");
        hintMap.put("dk", "Country in Europe, southernmost of the Scandinavian countries");
        hintMap.put("dj", "Country in East Africa");
        hintMap.put("dm", "Country in the Caribbean");
        hintMap.put("do", "Country in the Caribbean");
        hintMap.put("ec", "Country in South America");
        hintMap.put("eg", "Pyramids of Giza");
        hintMap.put("sv", "Country in Central America");
        hintMap.put("gq", "Country in Central Africa");
        // TODO : DO LATER
        hintMap.put("er", "Eritrea");
        hintMap.put("ee", "Estonia");
        hintMap.put("sz", "Eswatini");
        hintMap.put("et", "Ethiopia");
        hintMap.put("fj", "Fiji");
        hintMap.put("fi", "Finland");
        hintMap.put("fr", "France");
        hintMap.put("ga", "Gabon");
        hintMap.put("gm", "Gambia");
        hintMap.put("ge", "Georgia");
        hintMap.put("de", "Germany");
        hintMap.put("gh", "Ghana");
        hintMap.put("gr", "Greece");
        hintMap.put("gd", "Grenada");
        hintMap.put("gt", "Guatemala");
        hintMap.put("gn", "Guinea");
        hintMap.put("gw", "Guinea-Bissau");
        hintMap.put("gy", "Guyana");
        hintMap.put("ht", "Haiti");
        hintMap.put("hn", "Honduras");
        hintMap.put("hu", "Hungary");
        hintMap.put("is", "Iceland");
        hintMap.put("in", "India");
        hintMap.put("id", "Indonesia");
        hintMap.put("ir", "Iran");
        hintMap.put("iq", "Iraq");
        hintMap.put("ie", "Ireland");
        hintMap.put("il", "Israel");
        hintMap.put("it", "Italy");
        hintMap.put("jm", "Jamaica");
        hintMap.put("jp", "Japan");
        hintMap.put("jo", "Jordan");
        hintMap.put("kz", "Kazakhstan");
        hintMap.put("ke", "Kenya");
        hintMap.put("ki", "Kiribati");
        hintMap.put("kp", "North Korea");
        hintMap.put("kr", "South Korea");
        hintMap.put("kw", "Kuwait");
        hintMap.put("kg", "Kyrgyzstan");
        hintMap.put("la", "Laos");
        hintMap.put("lv", "Latvia");
        hintMap.put("lb", "Lebanon");
        hintMap.put("ls", "Lesotho");
        hintMap.put("lr", "Liberia");
        hintMap.put("ly", "Libya");
        hintMap.put("li", "Liechtenstein");
        hintMap.put("lt", "Lithuania");
        hintMap.put("lu", "Luxembery");
        hintMap.put("mk", "North Macedonia");
        hintMap.put("mg", "Madagascar");
        hintMap.put("mw", "Malawi");
        hintMap.put("my", "Malaysia");
        hintMap.put("mv", "Maldives");
        hintMap.put("ml", "Mali");
        hintMap.put("mt", "Malta");
        hintMap.put("mh", "Marshall Islands");
        hintMap.put("mr", "Mauritania");
        hintMap.put("mu", "Mauritius");
        hintMap.put("mx", "Mexico");
        hintMap.put("fm", "Micronesia");
        hintMap.put("md", "Moldova");
        hintMap.put("mc", "Monaco");
        hintMap.put("mn", "Mongolia");
        hintMap.put("me", "Montenegro");
        hintMap.put("ma", "Morocco");
        hintMap.put("mz", "Mozambique");
        hintMap.put("mm", "Myanmar");
        hintMap.put("na", "Namibia");
        hintMap.put("nr", "Nauru");
        hintMap.put("np", "Nepal");
        hintMap.put("nl", "Netherlands");
        hintMap.put("nz", "NewZealand");
        hintMap.put("ni", "Nicaragua");
        hintMap.put("ne", "Niger");
        hintMap.put("no", "Norway");
        hintMap.put("om", "Oman");
        hintMap.put("pk", "Pakistan");
        hintMap.put("pw", "Palau");
        hintMap.put("ps", "Palestine");
        hintMap.put("pa", "Panama");
        hintMap.put("pg", "Papua New Guinea");
        hintMap.put("py", "Paraguay");
        hintMap.put("pe", "Peru");
        hintMap.put("ph", "Philippines");
        hintMap.put("pl", "Poland");
        hintMap.put("pt", "Portugal");
        hintMap.put("qa", "Qatar");
        hintMap.put("tw", "Taiwan");
        hintMap.put("ro", "Romania");
        hintMap.put("ru", "Russia");
        hintMap.put("rw", "Rwanda");
        hintMap.put("kn", "Saint Kitts and Nevis");
        hintMap.put("lc", "Saint Lucia");
        hintMap.put("vc", "Saint Vincent and the Grenadines");
        hintMap.put("ws", "Samoa");
        hintMap.put("sm", "San Marino");
        hintMap.put("st", "Sao Tome and Principe");
        hintMap.put("sa", "Saudi Arabia");
        hintMap.put("sn", "Senegal");
        hintMap.put("rs", "Serbia");
        hintMap.put("sc", "Seychelles");
        hintMap.put("sl", "Sierra Leone");
        hintMap.put("sg", "Singapore");
        hintMap.put("sk", "Slovakia");
        hintMap.put("si", "Slovenia");
        hintMap.put("sb", "Solomon Islands");
        hintMap.put("so", "Somalia");
        hintMap.put("za", "South Africa");
        hintMap.put("ss", "South Sudan");
        hintMap.put("es", "Spain");
        hintMap.put("lk", "Sri Lanka");
        hintMap.put("sd", "Sudan");
        hintMap.put("sr", "Suriname");
        hintMap.put("se", "Sweden");
        hintMap.put("ch", "Switzerland");
        hintMap.put("sy", "Syria");
        hintMap.put("tj", "Tajikistan");
        hintMap.put("tz", "Tanzania");
        hintMap.put("th", "Thailand");
        hintMap.put("tl", "Timor-Leste");
        hintMap.put("tg", "Togo");
        hintMap.put("to", "Tonga");
        hintMap.put("tt", "Trinidad and Tobago");
        hintMap.put("tn", "Tunisia");
        hintMap.put("tr", "Turkiye");
        hintMap.put("tm", "Turkmenistan");
        hintMap.put("tv", "Tuvalu");
        hintMap.put("ug", "Uganda");
        hintMap.put("ua", "Ukraine");
        hintMap.put("ae", "United Arab Emirates");
        hintMap.put("gb", "United Kingdom");
        hintMap.put("us", "United States of America");
        hintMap.put("uy", "Uruguay");
        hintMap.put("uz", "Uzbekistan");
        hintMap.put("vu", "Vanuatu");
        hintMap.put("va", "Vatican City");
        hintMap.put("ve", "Venezuela");
        hintMap.put("vn", "Vietnam");
        hintMap.put("ye", "Yemen");
        hintMap.put("zm", "Zambia");
        hintMap.put("zw", "Zimbabwe");     
    }

    private static synchronized int getAmount(long userId) {
        if(coinMap.containsKey(userId)) {
            return coinMap.get(userId);
        } else {
            return 0;
        }
    }

    private static void loadCountries() {
        countryMap.put("af", "Afghanistan");
        countryMap.put("al", "Albania");
        countryMap.put("dz", "Algeria");
        countryMap.put("ao", "Angola");
        countryMap.put("ar", "Argentina");
        countryMap.put("am", "Armenia");
        countryMap.put("au", "Australia");
        countryMap.put("at", "Austria");
        countryMap.put("az", "Azerbaijan");
        countryMap.put("bs", "Bahamas");
        countryMap.put("bh", "Bahrain");
        countryMap.put("bd", "Bangladesh");
        countryMap.put("bb", "Barbados");
        countryMap.put("by", "Belarus");
        countryMap.put("be", "Belgium");
        countryMap.put("bz", "Belize");
        countryMap.put("bj", "Benin");
        countryMap.put("bt", "Bhutan");
        countryMap.put("bo", "Bolivia");
        countryMap.put("ba", "Bosnia and Herzegovina");
        countryMap.put("bw", "Botswana");
        countryMap.put("br", "Brazil");
        countryMap.put("bn", "Brunei");
        countryMap.put("bg", "Bulgaria");
        countryMap.put("bf", "Burkina Faso");
        countryMap.put("bi", "Burundi");
        countryMap.put("kh", "Cambodia");
        countryMap.put("cm", "Cameroon");
        countryMap.put("ca", "Canada");
        countryMap.put("cv", "Cape Verde");
        countryMap.put("cf", "Central African Republic");
        countryMap.put("td", "Chad");
        countryMap.put("cl", "Chile");
        countryMap.put("cn", "China");
        countryMap.put("co", "Colombia");
        countryMap.put("km", "Comoros");
        countryMap.put("cg", "Congo");
        countryMap.put("cr", "Costa Rica");
        countryMap.put("ci", "Ivory Coast");
        countryMap.put("hr", "Croatia");
        countryMap.put("cu", "Cuba");
        countryMap.put("cy", "Cyprus");
        countryMap.put("cz", "Czech Republic");
        countryMap.put("cd", "Democratic Republic of the Congo");
        countryMap.put("dk", "Denmark");
        countryMap.put("dj", "Djibouti");
        countryMap.put("dm", "Dominica");
        countryMap.put("do", "Dominican Republic");
        countryMap.put("ec", "Ecuador");
        countryMap.put("eg", "Egypt");
        countryMap.put("sv", "El Salvador");
        countryMap.put("gq", "Equatorial Guinea");
        countryMap.put("er", "Eritrea");
        countryMap.put("ee", "Estonia");
        countryMap.put("sz", "Eswatini");
        countryMap.put("et", "Ethiopia");
        countryMap.put("fj", "Fiji");
        countryMap.put("fi", "Finland");
        countryMap.put("fr", "France");
        countryMap.put("ga", "Gabon");
        countryMap.put("gm", "Gambia");
        countryMap.put("ge", "Georgia");
        countryMap.put("de", "Germany");
        countryMap.put("gh", "Ghana");
        countryMap.put("gr", "Greece");
        countryMap.put("gd", "Grenada");
        countryMap.put("gt", "Guatemala");
        countryMap.put("gn", "Guinea");
        countryMap.put("gw", "Guinea-Bissau");
        countryMap.put("gy", "Guyana");
        countryMap.put("ht", "Haiti");
        countryMap.put("hn", "Honduras");
        countryMap.put("hu", "Hungary");
        countryMap.put("is", "Iceland");
        countryMap.put("in", "India");
        countryMap.put("id", "Indonesia");
        countryMap.put("ir", "Iran");
        countryMap.put("iq", "Iraq");
        countryMap.put("ie", "Ireland");
        countryMap.put("il", "Israel");
        countryMap.put("it", "Italy");
        countryMap.put("jm", "Jamaica");
        countryMap.put("jp", "Japan");
        countryMap.put("jo", "Jordan");
        countryMap.put("kz", "Kazakhstan");
        countryMap.put("ke", "Kenya");
        countryMap.put("ki", "Kiribati");
        countryMap.put("kp", "North Korea");
        countryMap.put("kr", "South Korea");
        countryMap.put("kw", "Kuwait");
        countryMap.put("kg", "Kyrgyzstan");
        countryMap.put("la", "Laos");
        countryMap.put("lv", "Latvia");
        countryMap.put("lb", "Lebanon");
        countryMap.put("ls", "Lesotho");
        countryMap.put("lr", "Liberia");
        countryMap.put("ly", "Libya");
        countryMap.put("li", "Liechtenstein");
        countryMap.put("lt", "Lithuania");
        countryMap.put("lu", "Luxembourg");
        countryMap.put("mk", "North Macedonia");
        countryMap.put("mg", "Madagascar");
        countryMap.put("mw", "Malawi");
        countryMap.put("my", "Malaysia");
        countryMap.put("mv", "Maldives");
        countryMap.put("ml", "Mali");
        countryMap.put("mt", "Malta");
        countryMap.put("mh", "Marshall Islands");
        countryMap.put("mr", "Mauritania");
        countryMap.put("mu", "Mauritius");
        countryMap.put("mx", "Mexico");
        countryMap.put("fm", "Micronesia");
        countryMap.put("md", "Moldova");
        countryMap.put("mc", "Monaco");
        countryMap.put("mn", "Mongolia");
        countryMap.put("me", "Montenegro");
        countryMap.put("ma", "Morocco");
        countryMap.put("mz", "Mozambique");
        countryMap.put("mm", "Myanmar");
        countryMap.put("na", "Namibia");
        countryMap.put("nr", "Nauru");
        countryMap.put("np", "Nepal");
        countryMap.put("nl", "Netherlands");
        countryMap.put("nz", "NewZealand");
        countryMap.put("ni", "Nicaragua");
        countryMap.put("ne", "Niger");
        countryMap.put("no", "Norway");
        countryMap.put("om", "Oman");
        countryMap.put("pk", "Pakistan");
        countryMap.put("pw", "Palau");
        countryMap.put("ps", "Palestine");
        countryMap.put("pa", "Panama");
        countryMap.put("pg", "Papua New Guinea");
        countryMap.put("py", "Paraguay");
        countryMap.put("pe", "Peru");
        countryMap.put("ph", "Philippines");
        countryMap.put("pl", "Poland");
        countryMap.put("pt", "Portugal");
        countryMap.put("qa", "Qatar");
        countryMap.put("tw", "Taiwan");
        countryMap.put("ro", "Romania");
        countryMap.put("ru", "Russia");
        countryMap.put("rw", "Rwanda");
        countryMap.put("kn", "Saint Kitts and Nevis");
        countryMap.put("lc", "Saint Lucia");
        countryMap.put("vc", "Saint Vincent and the Grenadines");
        countryMap.put("ws", "Samoa");
        countryMap.put("sm", "San Marino");
        countryMap.put("st", "Sao Tome and Principe");
        countryMap.put("sa", "Saudi Arabia");
        countryMap.put("sn", "Senegal");
        countryMap.put("rs", "Serbia");
        countryMap.put("sc", "Seychelles");
        countryMap.put("sl", "Sierra Leone");
        countryMap.put("sg", "Singapore");
        countryMap.put("sk", "Slovakia");
        countryMap.put("si", "Slovenia");
        countryMap.put("sb", "Solomon Islands");
        countryMap.put("so", "Somalia");
        countryMap.put("za", "South Africa");
        countryMap.put("ss", "South Sudan");
        countryMap.put("es", "Spain");
        countryMap.put("lk", "Sri Lanka");
        countryMap.put("sd", "Sudan");
        countryMap.put("sr", "Suriname");
        countryMap.put("se", "Sweden");
        countryMap.put("ch", "Switzerland");
        countryMap.put("sy", "Syria");
        countryMap.put("tj", "Tajikistan");
        countryMap.put("tz", "Tanzania");
        countryMap.put("th", "Thailand");
        countryMap.put("tl", "Timor-Leste");
        countryMap.put("tg", "Togo");
        countryMap.put("to", "Tonga");
        countryMap.put("tt", "Trinidad and Tobago");
        countryMap.put("tn", "Tunisia");
        countryMap.put("tr", "Turkiye");
        countryMap.put("tm", "Turkmenistan");
        countryMap.put("tv", "Tuvalu");
        countryMap.put("ug", "Uganda");
        countryMap.put("ua", "Ukraine");
        countryMap.put("ae", "United Arab Emirates");
        countryMap.put("gb", "United Kingdom");
        countryMap.put("us", "United States of America");
        countryMap.put("uy", "Uruguay");
        countryMap.put("uz", "Uzbekistan");
        countryMap.put("vu", "Vanuatu");
        countryMap.put("va", "Vatican City");
        countryMap.put("ve", "Venezuela");
        countryMap.put("vn", "Vietnam");
        countryMap.put("ye", "Yemen");
        countryMap.put("zm", "Zambia");
        countryMap.put("zw", "Zimbabwe");     
    }
}