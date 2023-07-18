package com.ayush.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FlagGame {

    private static HashMap<String, String> countryMap = new HashMap<>(192);
    private static ArrayList<String> isoList;
    private static Random random;
    private static String flagLink = "https://flagcdn.com/256x192/";
    private static String suffix = ".png";

    private String countryCode;
    private SlashCommandInteractionEvent event;

    static {
        random = new Random();
        loadCountries();
        isoList = new ArrayList<String>(countryMap.keySet());
    }

    {
        countryCode = isoList.get(random.nextInt(isoList.size()));
    }

    public FlagGame (SlashCommandInteractionEvent event) {
        super();
        this.event = event;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Country Flag");
        eb.setImage(flagLink + countryCode + suffix);
        eb.setColor(new Color(1,12,64));
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
    
    void endGameAsWin(MessageReceivedEvent msgEvent) {
        msgEvent.getMessage().reply("You Guessed it right!").queue();
        // GameHandler.getInstance().endGame(event.getChannel().getIdLong());
        GameHandler.getInstance().getGameMap().remove(event.getChannel().getIdLong());
    }

    void endGameAsLose() {
        this.event.getChannel().sendMessage("No one guessed the country , it was " + countryMap.get(countryCode)).queue();
        GameHandler.getInstance().endGame(event.getChannel().getIdLong());
    }

    public boolean guess(String guessCountry) {
        if(countryMap.get(countryCode).equalsIgnoreCase(guessCountry)) {
            return true;
        }else {
            return false;
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
        countryMap.put("cd", "Democratic Republic of Congo");
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
        countryMap.put("lu", "Luxembery");
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
