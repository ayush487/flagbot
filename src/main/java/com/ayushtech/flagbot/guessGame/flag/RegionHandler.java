package com.ayushtech.flagbot.guessGame.flag;

import java.util.concurrent.CompletableFuture;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.RegionDao;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class RegionHandler {

	public static RegionHandler regionHandler = null;

	private RegionHandler() {
	}

	public static synchronized RegionHandler getInstance() {
		if (regionHandler == null) {
			regionHandler = new RegionHandler();
		}
		return regionHandler;
	}

	public String requestForHint(ButtonInteractionEvent buttonEvent) {
		String countryCode = buttonEvent.getComponentId().split("_")[1];
		String region = RegionDao.getInstance().getRegion(countryCode);
		CompletableFuture.runAsync(() -> {
			CoinDao.getInstance().addCoins(buttonEvent.getUser().getIdLong(), -60l);
		});
		return region;
	}

	public void handleRegionButton(ButtonInteractionEvent event) {
		event.deferReply().setEphemeral(true).queue();
		String response = requestForHint(event);
		response = (response != null) ? response : "Region Not Found!";
		event.getHook().sendMessage(response).setEphemeral(true).queue();
	}
}
