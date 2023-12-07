package com.ayushtech.flagbot.game.flag;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.RegionDao;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class RegionHandler {

	public static RegionHandler regionHandler = null;
	
	private RegionHandler () {}
	
	public static synchronized RegionHandler getInstance() {
		if(regionHandler==null) {
			regionHandler = new RegionHandler();
		}
		return regionHandler;
	}
	
	public String requestForHint(ButtonInteractionEvent buttonEvent) {
		
		if(buttonEvent.isFromGuild()) {
			Member member = buttonEvent.getMember();
			if(member==null) {
				return "Something went wrong!";
			}
			
			long memberBalance = CoinDao.getInstance().getBalance(member.getIdLong());
			
			if(memberBalance<60) {
				return "You don't have enough coins!";
			} else {
				CoinDao.getInstance().addCoins(member.getIdLong(), -60l);
				return getRegion(buttonEvent.getChannel().getIdLong());
			}
		} else {
			return "Not Allowed!";
		}
	}
	
	public String getRegion(long eventChannelId) {
    	String countryCode = FlagGameHandler.getInstance().getGameMap()
    			.get(eventChannelId).getCountryCode();
    	return RegionDao.getInstance().getRegion(countryCode);
    }
}
