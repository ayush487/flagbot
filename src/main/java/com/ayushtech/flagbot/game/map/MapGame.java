package com.ayushtech.flagbot.game.map;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.game.Game;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.LanguageService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class MapGame extends Game {

	private static Random random;
	private static Set<String> ignoreSet;
	public static Map<String, String> countryOverrideMap;

	private MessageChannel channel;
	private MessageEmbed messageEmbed;
	private String countryCode;
	private Long messageId;
	private boolean isHard;
	private int rounds;
	private int roundSize;
	private String lang;

	static {
		random = new Random();
		ignoreSet = new HashSet<>(5);
		countryOverrideMap = new HashMap<>(10);
		loadIgnoreSet();
		loadCountryOverrideMap();
	}

	public MapGame(MessageChannel channel, boolean isHard, int rounds, int roundSize, String lang) {
		this.channel = channel;
		this.isHard = isHard;
		this.rounds = rounds;
		this.roundSize = roundSize;
		this.lang = lang;
		this.countryCode = getRandomCountryCode(isHard);
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Guess the country");
		eb.setImage(createImageURL(countryCode));
		eb.setColor(new Color(235, 206, 129));
		eb.setFooter("Map credit : utexas.edu");
		MessageEmbed embed = eb.build();
		setMessageEmbed(embed);
		channel.sendMessageEmbeds(embed)
				.setActionRow(Button.primary("skipMap", "Skip"))
				.queue(msg -> setMessageId(msg.getIdLong()));
	}

	@Override
	public void endGameAsWin(MessageReceivedEvent msgEvent) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Correct!");
		String answerString;
		if (lang == null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		eb.setDescription(
				msgEvent.getAuthor().getAsMention() +
						" is correct!\n**Coins :** `" +
						Game.getAmount(msgEvent.getAuthor().getIdLong()) +
						"(+100)` " + ":coin:" +
						"  \n **Correct Answer :** " +
						answerString);
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(13, 240, 52));
		MapGameHandler.getInstance().getGameMap().remove(channel.getIdLong());
		if (rounds <= 1) {
			msgEvent.getChannel().sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainMap_" + (isHard ? "Hard" : "Easy") + "_" + roundSize,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			msgEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, isHard, rounds - 1, roundSize, lang);
		}
		Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
		disableButtons();
	}

	@Override
	public void endGameAsLose() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("No one guessed the map!");
		String answerString;
		if (lang == null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		eb.setDescription("**Correct Answer :** \n" + answerString);
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(240, 13, 52));
		MapGameHandler.getInstance().endGame(channel.getIdLong());
		if (rounds <= 1) {
			this.channel.sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainMap_" + (isHard ? "Hard" : "Easy") + "_" + roundSize,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			this.channel.sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, isHard, rounds - 1, roundSize, lang);
		}
		disableButtons();
	}

	public void disableButtons() {
		this.channel.retrieveMessageById(this.getMessageId())
				.complete().editMessageEmbeds(getMessageEmbed())
				.setActionRow(Button.primary("skipButton", "Skip").asDisabled())
				.queue();
	}

	public static void startAgain(MessageChannel channel, boolean isHard, int rounds, int roundSize, String lang) {
		MapGame game = new MapGame(channel, isHard, rounds, roundSize, lang);
		MapGameHandler.getInstance().getGameMap().put(channel.getIdLong(), game);
		GameEndService.getInstance().scheduleEndGame(
				new MapGameEndRunnable(game, channel.getIdLong()), 30, TimeUnit.SECONDS);
	}

	@Override
	public boolean guess(String guessCountry) {
		return countryMap.get(countryCode).equalsIgnoreCase(guessCountry) ||
				LanguageService.getInstance().isGuessRight(lang, guessCountry, countryCode);
	}

	public String getCountryCode() {
		return countryCode;
	}

	private static String createImageURL(String countryCode) {
		StringBuffer sb = new StringBuffer("https://maps.lib.utexas.edu/maps/cia16/");
		String country = countryMap.get(countryCode);
		if (countryOverrideMap.containsKey(country)) {
			country = countryOverrideMap.get(country);
		} else {
			country = country.toLowerCase().replace(' ', '_');
		}
		sb.append(country);
		sb.append("_sm_2016.gif");
		return sb.toString();
	}

	private static void loadIgnoreSet() {
		ignoreSet.add("gm");
		ignoreSet.add("va");
		ignoreSet.add("sz");
		ignoreSet.add("ps");
		ignoreSet.add("cd");
		ignoreSet.add("aa");
		ignoreSet.add("ag");
		ignoreSet.add("re");
		ignoreSet.add("ax");
		ignoreSet.add("bq");
		ignoreSet.add("sh");
		ignoreSet.add("sj");
		ignoreSet.add("um");
		ignoreSet.add("gf");
		ignoreSet.add("gp");
		ignoreSet.add("gs");
		ignoreSet.add("gb-sct");
		ignoreSet.add("gb-nir");
		ignoreSet.add("yt");
		ignoreSet.add("gb-wls");
		ignoreSet.add("gb-eng");
		ignoreSet.add("mq");
		ignoreSet.add("ay");
		ignoreSet.add("aj");
		ignoreSet.add("alr");
		ignoreSet.add("ab");
		ignoreSet.add("ald");
		ignoreSet.add("bas");
		ignoreSet.add("bia");
		ignoreSet.add("bri");
		ignoreSet.add("bur");
		ignoreSet.add("cri");
		ignoreSet.add("che");
		ignoreSet.add("chu");
		ignoreSet.add("crm");
		ignoreSet.add("dag");
		ignoreSet.add("eai");
		ignoreSet.add("her");
		ignoreSet.add("ing");
		ignoreSet.add("kpr");
		ignoreSet.add("kab");
		ignoreSet.add("kal");
		ignoreSet.add("kar");
		ignoreSet.add("kkp");
		ignoreSet.add("krl");
		ignoreSet.add("kha");
		ignoreSet.add("kom");
		ignoreSet.add("lad");
		ignoreSet.add("mar");
		ignoreSet.add("mor");
		ignoreSet.add("nag");
		ignoreSet.add("nos");
	}

	private static void loadCountryOverrideMap() {
		countryOverrideMap.put("Myanmar", "burma");
		countryOverrideMap.put("North Macedonia", "macedonia");
		countryOverrideMap.put("South Korea", "korea_south");
		countryOverrideMap.put("North Korea", "korea_north");
		countryOverrideMap.put("Guinea-Bissau", "guinea_bissau");
		countryOverrideMap.put("Micronesia", "micronesia_federated_states_of");
		countryOverrideMap.put("United States of America", "united_states");
		countryOverrideMap.put("Turkiye", "turkey");
		countryOverrideMap.put("Cape Verde", "cabo_verde");
		countryOverrideMap.put("Ivory Coast", "cote_divoire");
		countryOverrideMap.put("Congo", "congo_republic_of_the");

	}

	public MessageEmbed getMessageEmbed() {
		return messageEmbed;
	}

	public void setMessageEmbed(MessageEmbed messageEmbed) {
		this.messageEmbed = messageEmbed;
	}

	private static String getRandomCountryCode(boolean isHard) {
		String countryCode = isoList.get(random.nextInt(isoList.size()));
		if (!isHard) {
			while (nonSoverignCountries.contains(countryCode)) {
				countryCode = isoList.get(random.nextInt(isoList.size()));
			}
		}
		while (ignoreSet.contains(countryCode)) {
			countryCode = isoList.get(random.nextInt(isoList.size()));
		}
		return countryCode;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

}
