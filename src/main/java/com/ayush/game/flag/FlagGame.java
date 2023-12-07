package com.ayush.game.flag;

import java.awt.Color;
import java.util.Random;

import javax.annotation.Nullable;

import com.ayush.game.Game;

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

	static {
		random = new Random();

	}

	{
		countryCode = isoList.get(random.nextInt(isoList.size()));
	}

	public FlagGame(MessageChannel channel) {
		super();
		this.channel = channel;
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Guess the Country Flag");
		eb.setImage(flagLink + countryCode + suffix);
		eb.setColor(new Color(38, 187, 237));
		eb.setFooter("*See Region will cost you 60 coins");
		MessageEmbed embed = eb.build();
		setMessageEmbed(embed);
		channel.sendMessageEmbeds(embed)
				.setActionRow(Button.primary("skipButton", "Skip"), Button.primary("checkRegionButton", "See Region"))
				.queue(message -> setMessageId(message.getIdLong()));
	}

	@Override
	public void endGameAsWin(MessageReceivedEvent msgEvent) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Correct!");
		eb.setDescription(msgEvent.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
				+ Game.getAmount(msgEvent.getAuthor().getIdLong()) + "(+100)` " + ":coin:"
				+ "  \n **Correct Answer :** " + countryMap.get(countryCode));
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(13, 240, 52));
		msgEvent.getChannel().sendMessageEmbeds(eb.build())
				.setActionRow(Button.primary("playAgainButton", "Play Again")).queue();
		FlagGameHandler.getInstance().getGameMap().remove(channel.getIdLong());
		Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
		disableButtons();
	}

	@Override
	public void endGameAsLose() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("No one guessed the flag!");
		eb.setDescription("**Correct Answer :** \n" + countryMap.get(countryCode));
		eb.setThumbnail(flagLink + countryCode + suffix);
		eb.setColor(new Color(240, 13, 52));
		this.channel.sendMessageEmbeds(eb.build()).setActionRow(Button.primary("playAgainButton", "Play Again"))
				.queue();
		FlagGameHandler.getInstance().endGame(channel.getIdLong());
		disableButtons();
	}

//    @Override
	public void disableButtons() {
		this.channel.retrieveMessageById(this.getMessageId()).complete().editMessageEmbeds(getMessageEmbed())
				.setActionRow(Button.primary("skipButton", "Skip").asDisabled(),
						Button.primary("checkRegionButton", "See Region").asDisabled())
				.queue();
	}

	public boolean guess(String guessCountry) {
		if (countryMap.get(countryCode).equalsIgnoreCase(guessCountry)) {
			return true;
		} else {
			return false;
		}
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

}