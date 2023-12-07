package com.ayushtech.flagbot.game;

import com.ayushtech.flagbot.dbconnectivity.LeaderboardDao;

import net.dv8tion.jda.api.JDA;

public class LeaderboardHandler {

	private static LeaderboardHandler leaderboardHandler = null;
	private static LeaderboardDao dao = new LeaderboardDao();
	
	private LeaderboardHandler() {}
	
	public static synchronized LeaderboardHandler getInstance() {
        if(leaderboardHandler==null) {
        	leaderboardHandler = new LeaderboardHandler();
        }
        return leaderboardHandler;
    }
	
	public String getLeaderboard(JDA jda) {
		StringBuffer sb = new StringBuffer();
		sb.append("```");
		sb.append("Top 5 players\n");
		sb.append(dao.getPlayers(jda, 5));
		sb.append("\n```");
		return sb.toString();
	}
	
	

}
