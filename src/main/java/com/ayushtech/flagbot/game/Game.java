package com.ayushtech.flagbot.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Game {

    public static HashMap<String, String> countryMap = new HashMap<>(300);
    protected static Set<String> nonSoverignCountries = new HashSet<>();
    protected static List<String> isoList;
    protected static final String flagLink = "https://raw.githubusercontent.com/ayush487/image-library/main/flags/";
    protected static final String suffix = ".png";
    protected static Map<String,String> alternativeNames = new HashMap<>();

    static {
        Game.loadCountries();
        Game.loadNonSoverignCountries();
        Game.loadAlternativeNames();
        isoList = new ArrayList<>(countryMap.keySet());
    }

    public abstract void endGameAsWin(MessageReceivedEvent msgEvent);

    public abstract void endGameAsLose();

    public abstract void disableButtons();

    public abstract boolean guess(String country);

    public static void increaseCoins(long userId, long amount) {
        CoinDao.getInstance().addCoins(userId, amount);
    }

    public static long getAmount(long userId) {
        return CoinDao.getInstance().getBalance(userId);
    }

    private static void loadCountries() {
        countryMap.put("aa", "Saba");
        countryMap.put("ab", "Abkhazia");
        countryMap.put("ay", "Adygea");
        countryMap.put("aj", "Adjara");
        countryMap.put("ald", "Alderney");
        countryMap.put("alr", "Altai Republic");
        countryMap.put("ad", "Andorra");
        countryMap.put("ae", "United Arab Emirates");
        countryMap.put("af", "Afghanistan");
        countryMap.put("ag", "Antigua and Barbuda");
        countryMap.put("ai", "Anguilla");
        countryMap.put("al", "Albania");
        countryMap.put("am", "Armenia");
        countryMap.put("ao", "Angola");
        countryMap.put("aq", "Antarctica");
        countryMap.put("an", "Antarctica");
        countryMap.put("ar", "Argentina");
        countryMap.put("as", "American Samoa");
        countryMap.put("at", "Austria");
        countryMap.put("au", "Australia");
        countryMap.put("aw", "Aruba");
        countryMap.put("ax", "Aland Islands");
        countryMap.put("az", "Azerbaijan");
        countryMap.put("ba", "Bosnia and Herzegovina");
        countryMap.put("bb", "Barbados");
        countryMap.put("bd", "Bangladesh");
        countryMap.put("be", "Belgium");
        countryMap.put("bf", "Burkina Faso");
        countryMap.put("bg", "Bulgaria");
        countryMap.put("bh", "Bahrain");
        countryMap.put("bi", "Burundi");
        countryMap.put("bj", "Benin");
        countryMap.put("bl", "Saint Barthelemy");
        countryMap.put("bm", "Bermuda");
        countryMap.put("bn", "Brunei");
        countryMap.put("bo", "Bolivia");
        countryMap.put("bq", "Caribbean Netherlands");
        countryMap.put("baq", "British Antarctic");
        countryMap.put("br", "Brazil");
        countryMap.put("bs", "Bahamas");
        countryMap.put("bt", "Bhutan");
        countryMap.put("bv", "Bouvet Island");
        countryMap.put("bw", "Botswana");
        countryMap.put("by", "Belarus");
        countryMap.put("bz", "Belize");
        countryMap.put("ca", "Canada");
        countryMap.put("cc", "Cocos Islands");
        countryMap.put("cd", "Democratic Republic of the Congo");
        countryMap.put("cf", "Central African Republic");
        countryMap.put("cg", "Congo");
        countryMap.put("ch", "Switzerland");
        countryMap.put("ci", "Ivory Coast");
        countryMap.put("ck", "Cook Islands");
        countryMap.put("cl", "Chile");
        countryMap.put("cm", "Cameroon");
        countryMap.put("cn", "China");
        countryMap.put("co", "Colombia");
        countryMap.put("cr", "Costa Rica");
        countryMap.put("cu", "Cuba");
        countryMap.put("cv", "Cape Verde");
        countryMap.put("cw", "Curacao");
        countryMap.put("cx", "Christmas Island");
        countryMap.put("cy", "Cyprus");
        countryMap.put("cz", "Czech Republic");
        countryMap.put("de", "Germany");
        countryMap.put("dj", "Djibouti");
        countryMap.put("dk", "Denmark");
        countryMap.put("dm", "Dominica");
        countryMap.put("do", "Dominican Republic");
        countryMap.put("dz", "Algeria");
        countryMap.put("ec", "Ecuador");
        countryMap.put("ee", "Estonia");
        countryMap.put("eg", "Egypt");
        countryMap.put("eh", "Western Sahara");
        countryMap.put("ei", "Sint Eustatius");
        countryMap.put("er", "Eritrea");
        countryMap.put("es", "Spain");
        countryMap.put("et", "Ethiopia");
        countryMap.put("fi", "Finland");
        countryMap.put("fj", "Fiji");
        countryMap.put("fk", "Falkland Islands");
        countryMap.put("fm", "Micronesia");
        countryMap.put("fo", "Faroe Islands");
        countryMap.put("fr", "France");
        countryMap.put("ga", "Gabon");
        countryMap.put("gb", "United Kingdom");
        countryMap.put("gb-eng", "England");
        countryMap.put("gb-nir", "Northern Ireland");
        countryMap.put("gb-sct", "Scotland");
        countryMap.put("gb-wls", "Wales");
        countryMap.put("gd", "Grenada");
        countryMap.put("ge", "Georgia");
        countryMap.put("gf", "French Guiana");
        countryMap.put("gg", "Guernsey");
        countryMap.put("gh", "Ghana");
        countryMap.put("gi", "Gibraltar");
        countryMap.put("gl", "Greenland");
        countryMap.put("gm", "Gambia");
        countryMap.put("gn", "Guinea");
        countryMap.put("gp", "Guadeloupe");
        countryMap.put("gq", "Equatorial Guinea");
        countryMap.put("gr", "Greece");
        countryMap.put("gs", "South Georgia and the South Sandwich Islands");
        countryMap.put("gt", "Guatemala");
        countryMap.put("gu", "Guam");
        countryMap.put("gw", "Guinea-Bissau");
        countryMap.put("gy", "Guyana");
        countryMap.put("hk", "Hong Kong");
        countryMap.put("hm", "Heard Island and McDonald Islands");
        countryMap.put("hn", "Honduras");
        countryMap.put("hr", "Croatia");
        countryMap.put("ht", "Haiti");
        countryMap.put("hu", "Hungary");
        countryMap.put("id", "Indonesia");
        countryMap.put("ie", "Ireland");
        countryMap.put("il", "Israel");
        countryMap.put("im", "Isle of Man");
        countryMap.put("in", "India");
        countryMap.put("io", "British Indian Ocean Territory");
        countryMap.put("iq", "Iraq");
        countryMap.put("ir", "Iran");
        countryMap.put("is", "Iceland");
        countryMap.put("it", "Italy");
        countryMap.put("je", "Jersey");
        countryMap.put("jm", "Jamaica");
        countryMap.put("jo", "Jordan");
        countryMap.put("jp", "Japan");
        countryMap.put("ke", "Kenya");
        countryMap.put("kg", "Kyrgyzstan");
        countryMap.put("kh", "Cambodia");
        countryMap.put("ki", "Kiribati");
        countryMap.put("km", "Comoros");
        countryMap.put("kn", "Saint Kitts and Nevis");
        countryMap.put("kp", "North Korea");
        countryMap.put("kr", "South Korea");
        countryMap.put("kw", "Kuwait");
        countryMap.put("ky", "Cayman Islands");
        countryMap.put("kz", "Kazakhstan");
        countryMap.put("la", "Laos");
        countryMap.put("lb", "Lebanon");
        countryMap.put("lc", "Saint Lucia");
        countryMap.put("li", "Liechtenstein");
        countryMap.put("lk", "Sri Lanka");
        countryMap.put("lr", "Liberia");
        countryMap.put("ls", "Lesotho");
        countryMap.put("lt", "Lithuania");
        countryMap.put("lu", "Luxembourg");
        countryMap.put("lv", "Latvia");
        countryMap.put("ly", "Libya");
        countryMap.put("ma", "Morocco");
        countryMap.put("mc", "Monaco");
        countryMap.put("md", "Moldova");
        countryMap.put("me", "Montenegro");
        countryMap.put("mf", "Saint Martin");
        countryMap.put("mg", "Madagascar");
        countryMap.put("mh", "Marshall Islands");
        countryMap.put("mk", "North Macedonia");
        countryMap.put("ml", "Mali");
        countryMap.put("mm", "Myanmar");
        countryMap.put("mn", "Mongolia");
        countryMap.put("mo", "Macau");
        countryMap.put("mp", "Northern Mariana Islands");
        countryMap.put("mq", "Martinique");
        countryMap.put("mi", "Martinique");
        countryMap.put("mr", "Mauritania");
        countryMap.put("ms", "Montserrat");
        countryMap.put("mt", "Malta");
        countryMap.put("mu", "Mauritius");
        countryMap.put("mv", "Maldives");
        countryMap.put("mw", "Malawi");
        countryMap.put("mx", "Mexico");
        countryMap.put("my", "Malaysia");
        countryMap.put("mz", "Mozambique");
        countryMap.put("na", "Namibia");
        countryMap.put("nc", "New Caledonia");
        countryMap.put("ne", "Niger");
        countryMap.put("nf", "Norfolk Island");
        countryMap.put("ng", "Nigeria");
        countryMap.put("ni", "Nicaragua");
        countryMap.put("nl", "Netherlands");
        countryMap.put("no", "Norway");
        countryMap.put("np", "Nepal");
        countryMap.put("nr", "Nauru");
        countryMap.put("nu", "Niue");
        countryMap.put("nz", "New Zealand");
        countryMap.put("om", "Oman");
        countryMap.put("pa", "Panama");
        countryMap.put("pe", "Peru");
        countryMap.put("pf", "French Polynesia");
        countryMap.put("pg", "Papua New Guinea");
        countryMap.put("ph", "Philippines");
        countryMap.put("pk", "Pakistan");
        countryMap.put("pl", "Poland");
        countryMap.put("pm", "Saint Pierre and Miquelon");
        countryMap.put("pn", "Pitcairn Islands");
        countryMap.put("pr", "Puerto Rico");
        countryMap.put("ps", "Palestine");
        countryMap.put("pt", "Portugal");
        countryMap.put("pw", "Palau");
        countryMap.put("py", "Paraguay");
        countryMap.put("qa", "Qatar");
        countryMap.put("re", "Reunion");
        countryMap.put("ro", "Romania");
        countryMap.put("rs", "Serbia");
        countryMap.put("ru", "Russia");
        countryMap.put("rw", "Rwanda");
        countryMap.put("sa", "Saudi Arabia");
        countryMap.put("sb", "Solomon Islands");
        countryMap.put("sc", "Seychelles");
        countryMap.put("sd", "Sudan");
        countryMap.put("se", "Sweden");
        countryMap.put("sg", "Singapore");
        countryMap.put("sh", "Saint Helena");
        countryMap.put("si", "Slovenia");
        countryMap.put("sj", "Svalbard and Jan Mayen");
        countryMap.put("sk", "Slovakia");
        countryMap.put("sl", "Sierra Leone");
        countryMap.put("sm", "San Marino");
        countryMap.put("sn", "Senegal");
        countryMap.put("so", "Somalia");
        countryMap.put("sr", "Suriname");
        countryMap.put("ss", "South Sudan");
        countryMap.put("st", "Sao Tome and Principe");
        countryMap.put("sv", "El Salvador");
        countryMap.put("sx", "Sint Maarten");
        countryMap.put("sy", "Syria");
        countryMap.put("sz", "Eswatini");
        countryMap.put("tc", "Turks and Caicos Islands");
        countryMap.put("td", "Chad");
        countryMap.put("tf", "French Southern and Antarctic Lands");
        countryMap.put("tg", "Togo");
        countryMap.put("th", "Thailand");
        countryMap.put("tj", "Tajikistan");
        countryMap.put("tk", "Tokelau");
        countryMap.put("tl", "Timor-Leste");
        countryMap.put("tm", "Turkmenistan");
        countryMap.put("tn", "Tunisia");
        countryMap.put("to", "Tonga");
        countryMap.put("tr", "Turkiye");
        countryMap.put("tt", "Trinidad and Tobago");
        countryMap.put("tv", "Tuvalu");
        countryMap.put("tw", "Taiwan");
        countryMap.put("tz", "Tanzania");
        countryMap.put("ua", "Ukraine");
        countryMap.put("ug", "Uganda");
        countryMap.put("um", "United States Minor Outlying Islands");
        countryMap.put("us", "United States of America");
        countryMap.put("uy", "Uruguay");
        countryMap.put("uz", "Uzbekistan");
        countryMap.put("va", "Vatican City");
        countryMap.put("vc", "Saint Vincent and the Grenadines");
        countryMap.put("ve", "Venezuela");
        countryMap.put("vg", "British Virgin Islands");
        countryMap.put("vi", "US Virgin Islands");
        countryMap.put("vn", "Vietnam");
        countryMap.put("vu", "Vanuatu");
        countryMap.put("wf", "Wallis and Futuna");
        countryMap.put("ws", "Samoa");
        countryMap.put("xk", "Kosovo");
        countryMap.put("ye", "Yemen");
        countryMap.put("yt", "Mayotte");
        countryMap.put("za", "South Africa");
        countryMap.put("zm", "Zambia");
        countryMap.put("zw", "Zimbabwe");
        countryMap.put("ac", "Ascension");
        countryMap.put("ti", "Tristan da Cunha");
        countryMap.put("bas", "Bashkortostan");
        countryMap.put("bia", "Bikini Atoll");
        countryMap.put("bri", "Brittany");
        countryMap.put("bur", "Buryatia");
        countryMap.put("cri", "Chechen Republic of Ichkeria");
        countryMap.put("che", "Chechen Republic");
        countryMap.put("chu", "Chuvashia");
        countryMap.put("crm", "Crimea");
        countryMap.put("eai", "Easter Island");
        countryMap.put("dag", "Dagestan");
        countryMap.put("eu", "European Union");
        countryMap.put("her", "Herm");
        countryMap.put("ing", "Ingushetia");
        countryMap.put("kpr", "Kuban People's Republic");
        countryMap.put("kab", "Kabardino-Balkaria");
        countryMap.put("kal", "Kalmykia");
        countryMap.put("kar", "Karachay-Cherkessia");
        countryMap.put("kkp", "Karakalpakstan");
        countryMap.put("krl", "Karelia");
        countryMap.put("kha", "Khakassia");
        countryMap.put("kom", "Komi");
        countryMap.put("lad", "Ladonia");
        countryMap.put("mar", "Mari El");
        countryMap.put("mor", "Mordovia");
        countryMap.put("nag", "Nagorno-Karabakh");
        countryMap.put("nos", "North Ossetia");
        countryMap.put("rsr", "Republika Srpska");
    }

    private static void loadNonSoverignCountries() {
        nonSoverignCountries.add("rsr");
        nonSoverignCountries.add("nag");
        nonSoverignCountries.add("nos");
        nonSoverignCountries.add("lad");
        nonSoverignCountries.add("mar");
        nonSoverignCountries.add("mor");
        nonSoverignCountries.add("her");
        nonSoverignCountries.add("ing");
        nonSoverignCountries.add("kpr");
        nonSoverignCountries.add("kab");
        nonSoverignCountries.add("kal");
        nonSoverignCountries.add("kar");
        nonSoverignCountries.add("kkp");
        nonSoverignCountries.add("krl");
        nonSoverignCountries.add("kha");
        nonSoverignCountries.add("kom");
        nonSoverignCountries.add("eai");
        nonSoverignCountries.add("dag");
        nonSoverignCountries.add("eu");
        nonSoverignCountries.add("bas");
        nonSoverignCountries.add("bia");
        nonSoverignCountries.add("bri");
        nonSoverignCountries.add("bur");
        nonSoverignCountries.add("cri");
        nonSoverignCountries.add("che");
        nonSoverignCountries.add("chu");
        nonSoverignCountries.add("crm");
        nonSoverignCountries.add("baq");
        nonSoverignCountries.add("aa");
        nonSoverignCountries.add("ab");
        nonSoverignCountries.add("ay");
        nonSoverignCountries.add("ald");
        nonSoverignCountries.add("alr");
        nonSoverignCountries.add("aj");
        nonSoverignCountries.add("ei");
        nonSoverignCountries.add("ac");
        nonSoverignCountries.add("an");
        nonSoverignCountries.add("ti");
        nonSoverignCountries.add("ai");
        nonSoverignCountries.add("aq");
        nonSoverignCountries.add("as");
        nonSoverignCountries.add("aw");
        nonSoverignCountries.add("ax");
        nonSoverignCountries.add("bm");
        nonSoverignCountries.add("bl");
        nonSoverignCountries.add("bq");
        nonSoverignCountries.add("bv");
        nonSoverignCountries.add("cc");
        nonSoverignCountries.add("ck");
        nonSoverignCountries.add("cw");
        nonSoverignCountries.add("cx");
        nonSoverignCountries.add("eh");
        nonSoverignCountries.add("fk");
        nonSoverignCountries.add("fo");
        nonSoverignCountries.add("gb-eng");
        nonSoverignCountries.add("gb-nir");
        nonSoverignCountries.add("gb-sct");
        nonSoverignCountries.add("gb-wls");
        nonSoverignCountries.add("gg");
        nonSoverignCountries.add("gi");
        nonSoverignCountries.add("gp");
        nonSoverignCountries.add("gf");
        nonSoverignCountries.add("gs");
        nonSoverignCountries.add("gu");
        nonSoverignCountries.add("hk");
        nonSoverignCountries.add("hm");
        nonSoverignCountries.add("im");
        nonSoverignCountries.add("io");
        nonSoverignCountries.add("je");
        nonSoverignCountries.add("ky");
        nonSoverignCountries.add("mo");
        nonSoverignCountries.add("mf");
        nonSoverignCountries.add("mp");
        nonSoverignCountries.add("mq");
        nonSoverignCountries.add("mi");
        nonSoverignCountries.add("ms");
        nonSoverignCountries.add("nc");
        nonSoverignCountries.add("nf");
        nonSoverignCountries.add("nu");
        nonSoverignCountries.add("pf");
        nonSoverignCountries.add("pm");
        nonSoverignCountries.add("pn");
        nonSoverignCountries.add("re");
        nonSoverignCountries.add("sh");
        nonSoverignCountries.add("sj");
        nonSoverignCountries.add("sx");
        nonSoverignCountries.add("tc");
        nonSoverignCountries.add("tf");
        nonSoverignCountries.add("tk");
        nonSoverignCountries.add("um");
        nonSoverignCountries.add("vg");
        nonSoverignCountries.add("vi");
        nonSoverignCountries.add("wf");
        nonSoverignCountries.add("xk");
        nonSoverignCountries.add("yt");
    }

    private static void loadAlternativeNames() {
        alternativeNames.put("ae","UAE");
        alternativeNames.put("cd","DR Congo");
        alternativeNames.put("ci", "Côte d'Ivoire");
        alternativeNames.put("cv", "Cabo Verde");
        alternativeNames.put("cz", "Czechia");
        alternativeNames.put("tr", "Turkey");
        alternativeNames.put("us", "USA/United States");
        alternativeNames.put("gb", "UK");
        alternativeNames.put("tl", "East Timor");
        alternativeNames.put("in","Bharat");
        alternativeNames.put("ba", "Bosnia");
        alternativeNames.put("mm","Burma");
        alternativeNames.put("eu","EU");
        alternativeNames.put("cf", "CAR");
        alternativeNames.put("gs", "South Georgia");
    }
}