package com.ayushtech.flagbot.game;

import com.ayushtech.flagbot.dbconnectivity.LeaderboardDao;

import net.dv8tion.jda.api.JDA;

public class LeaderboardHandler {

	private static LeaderboardHandler leaderboardHandler = null;
	private static LeaderboardDao dao = new LeaderboardDao();

	private LeaderboardHandler() {
	}

	

	public static synchronized LeaderboardHandler getInstance() {
		if (leaderboardHandler == null) {
			leaderboardHandler = new LeaderboardHandler();
		}
		return leaderboardHandler;
	}

	public String getLeaderboard(JDA jda, int lbSize) {
		StringBuffer sb = new StringBuffer();
		sb.append("```sql\n");
		sb.append("Top " + lbSize + " players\n");
		sb.append(dao.getPlayers(jda, lbSize));
		sb.append("\n```");
		return sb.toString();
	}

}
