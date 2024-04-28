package com.ayushtech.flagbot.game.flag;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

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

	private String countryCode;
	private MessageChannel channel;
	private Long messageId;
	private MessageEmbed messageEmbed;
	private byte difficulty;
	private int rounds;
	private int roundSize;
	private String lang;

	static {
		random = new Random();
	}

	public FlagGame(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang) {
		super();
		this.channel = channel;
		this.difficulty = difficulty;
		this.rounds = rounds;
		this.roundSize = roundSize;
		this.lang = lang;
		this.countryCode = getRandomCountryCode(difficulty);
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Guess the Country Flag");
		eb.setImage(flagLink + countryCode + suffix);
		eb.setColor(new Color(38, 187, 237));
		String mode;
		if (difficulty == 0) {
			mode = "Soverign Countries Only";
		} else if (difficulty == 1) {
			mode = "Non-Soverign Countries Only";
		} else {
			mode = "All Countries";
		}
		eb.setDescription(String.format("**Mode** : `%s`", mode));
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
		if (lang==null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		eb.setDescription(msgEvent.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
				+ Game.getAmount(msgEvent.getAuthor().getIdLong()) + "(+100)` " + ":coin:"
				+ "  \n **Correct Answer :** " + answerString);
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(13, 240, 52));
		if (rounds <= 1) {
			msgEvent.getChannel().sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			msgEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, difficulty, rounds - 1, roundSize, lang);
		}
		disableButtons();
		Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
	}

	@Override
	public void endGameAsLose() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("No one guessed the flag!");
		String answerString;
		if (lang==null) {
			answerString = countryMap.get(countryCode);
		} else {
			String altGuess = LanguageService.getInstance().getCorrectGuess(lang, countryCode);
			answerString = String.format("%s (`%s`)", countryMap.get(countryCode), altGuess);
		}
		eb.setDescription("**Correct Answer :** \n" + answerString);
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(240, 13, 52));
		FlagGameHandler.getInstance().endGame(channel.getIdLong());
		if (rounds <= 1) {
			this.channel.sendMessageEmbeds(eb.build())
					.setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize,
							roundSize <= 1 ? "Play Again" : "Start Round Again"))
					.queue();
		} else {
			this.channel.sendMessageEmbeds(eb.build()).queue();
			startAgain(channel, difficulty, rounds - 1, roundSize, lang);
		}
		disableButtons();
	}

	public static void startAgain(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang) {
		FlagGame game = new FlagGame(channel, difficulty, rounds, roundSize, lang);
		FlagGameHandler.getInstance().getGameMap().put(channel.getIdLong(), game);
		GameEndService.getInstance().scheduleEndGame(
				new FlagGameEndRunnable(game, channel.getIdLong()), 30, TimeUnit.SECONDS);
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

}