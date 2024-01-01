package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.JDA;

public class LeaderboardDao {

	private String[] spaceArray = {
			"",
			" ",
			"  ",
			"   ",
			"    ",
			"     ",
			"      ",
			"       ",
			"        ",
			"         ",
			"          ",
			"           ",
			"            ",
			"             ",
			"              ",
			"               ",
			"                ",
			"                 ",
			"                  ",
			"                   ",
			"                    ",
			"                     "
	};

	public String getPlayers(JDA jda, int players) {
		try {
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement ps = conn.prepareStatement("select * from coin_table order by coins desc limit ?;");
			ps.setInt(1, players);
			ResultSet rs = ps.executeQuery();
			StringBuffer sb = new StringBuffer();
			int index = 1;
			while (rs.next()) {
				long userId = rs.getLong("user_id");
				long amount = rs.getLong("coins");

				String userTagName = jda.retrieveUserById(userId)
						.map(user -> user.getName())
						.complete();
				int spaces = (18 - userTagName.length()) > 1 ? (18 - userTagName.length()) : 1;
				sb.append("\n" + index + ". " + userTagName + spaceArray[spaces] + amount + " Coins");
				index++;
			}
			return sb.toString();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}
