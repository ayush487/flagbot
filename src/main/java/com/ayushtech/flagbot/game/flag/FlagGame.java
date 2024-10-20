package com.ayushtech.flagbot.game.flag;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.ayushtech.flagbot.dbconnectivity.RegionDao;
import com.ayushtech.flagbot.game.Game;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.LanguageService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FlagGame extends Game {

	private static Random random;
	private static Map<String,String> continentMap;

	private String countryCode;
	private MessageChannel channel;
	private Long messageId;
	private MessageEmbed messageEmbed;
	private byte difficulty;
	private int rounds;
	private int roundSize;
	private String lang;
	private String continentCode;
	private long startTimeStamp;

	static {
		random = new Random();
		continentMap = new HashMap<>(7);
		continentMap.put("as", "Asia");
    continentMap.put("af", "Africa");
    continentMap.put("an", "Antarctica");
    continentMap.put("eu", "Europe");
    continentMap.put("oc", "Oceania");
    continentMap.put("sa", "South America");
    continentMap.put("na", "North America");
	}

	public static void startAgain(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang, String continentCode) {
		FlagGame game = new FlagGame(channel, difficulty, rounds, roundSize, lang, continentCode);
		FlagGameHandler.getInstance().getGameMap().put(channel.getIdLong(), game);
		GameEndService.getInstance().scheduleEndGame(
				new FlagGameEndRunnable(game, channel.getIdLong()), 30, TimeUnit.SECONDS);
	}

	public FlagGame(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang,String continentCode) {
		super();
		this.channel = channel;
		this.difficulty = difficulty;
		this.rounds = rounds;
		this.roundSize = roundSize;
		this.lang = lang;
		this.continentCode = continentCode;
		this.startTimeStamp = System.currentTimeMillis();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Guess the Country Flag");
		eb.setColor(new Color(38, 187, 237));
		String mode;
		if (difficulty == 0) {
			mode = "Sovereign Countries Only";
		} else if (difficulty == 1) {
			mode = "Non-Sovereign Countries Only";
		} else {
			mode = "All Countries";
		}
		String continent;
		if (continentCode.equals("all")) {
			continent = "Not Specified";
			this.countryCode = getRandomCountryCode(difficulty);
		} else {
			continent = continentMap.get(continentCode);
			this.countryCode = RegionDao.getInstance().getRandomCountryByContinent(continentCode);
		}
		eb.setImage(flagLink + countryCode + suffix);
		eb.setDescription(String.format("**Mode** : `%s`\n**Continent** : `%s`", mode, continent));
		eb.setFooter((difficulty != 0 ? "*Regions not available in the mode" : "*See Region will cost you 60 coins"));
		MessageEmbed embed = eb.build();
		setMessageEmbed(embed);
		if (difficulty != 0) {
			channel.sendMessageEmbeds(embed)
					.setActionRow(Button.primary("skipButton", "Skip"))
					.queue(message -> setMessageId(message.getIdLong()));
		} else {
			channel.sendMessageEmbeds(embed)
					.setActionRow(Button.primary("skipButton", "Skip"), Button.primary("checkRegionButton", "See Region"))
					.queue(message -> setMessageId(message.getIdLong()));
		}
	}

	@Override
	public void endGameAsWin(MessageReceivedEvent msgEvent) {
		FlagGameHandler.getInstance().getGameMap().remove(channel.getIdLong());
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Correct!");
		String answerString;
		if (lang == null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msgEvent.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
				+ Game.getAmount(msgEvent.getAuthor().getIdLong()) + "(+100)` " + ":coin:"
				+ "  \n**Correct Answer :** " + answerString);
		if (alternativeNames.containsKey(countryCode)) {
			sb.append("\n**Alternative Answers :** " + alternativeNames.get(countryCode));
		}
		sb.append("\n**Time Taken :** " + getTimeTook());
		eb.setDescription(sb.toString());
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(13, 240, 52));
		if (rounds <= 1) {
			msgEvent.getChannel().sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize + "_" + continentCode,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			msgEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, difficulty, rounds - 1, roundSize, lang, continentCode);
		}
		disableButtons();
		Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
	}

	@Override
	public void endGameAsLose() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("No one guessed the flag!");
		String answerString;
		if (lang == null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		StringBuilder sb = new StringBuilder("**Correct Answer :** " + answerString);
		if (alternativeNames.containsKey(countryCode)) {
			sb.append("\n**Alternative Answers :** " + alternativeNames.get(countryCode));
		}
		eb.setDescription(sb.toString());
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(240, 13, 52));
		FlagGameHandler.getInstance().endGame(channel.getIdLong());
		if (rounds <= 1) {
			this.channel.sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize + "_" + continentCode,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			this.channel.sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, difficulty, rounds - 1, roundSize, lang, continentCode);
		}
		disableButtons();
	}

	@Override
	public void disableButtons() {
		this.channel.retrieveMessageById(this.getMessageId()).complete().editMessageEmbeds(getMessageEmbed())
				.setActionRow(Button.primary("skipButton", "Skip").asDisabled(),
						Button.primary("checkRegionButton", "See Region").asDisabled())
				.queue();
	}

	public boolean guess(String guessCountry) {
		return countryMap.get(countryCode).equalsIgnoreCase(guessCountry) ||
				LanguageService.getInstance().isGuessRight(lang, guessCountry, countryCode);
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long msgId) {
		this.messageId = msgId;
	}

	public void setMessageEmbed(@Nullable MessageEmbed msgEmbed) {
		this.messageEmbed = msgEmbed;
	}

	public MessageEmbed getMessageEmbed() {
		return messageEmbed;
	}

	private static String getRandomCountryCode(byte difficulty) {
		String countryCode = isoList.get(random.nextInt(isoList.size()));
		;
		if (difficulty == 2) {
			return countryCode;
		} else {
			if (difficulty == 1) {
				while (!nonSoverignCountries.contains(countryCode)) {
					countryCode = isoList.get(random.nextInt(isoList.size()));
				}
			} else {
				while (nonSoverignCountries.contains(countryCode)) {
					countryCode = isoList.get(random.nextInt(isoList.size()));
				}
			}
			return countryCode;
		}
	}

	private String getTimeTook() {
		long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
		String returnString = String.format("`%.1f seconds`", timeTookInMS/1000.0);
		return returnString;
	}
}