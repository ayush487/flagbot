package com.ayushtech.flagbot.dbconnectivity;

import com.ayushtech.flagbot.services.PrivateServerService;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrivateServerDao {
    private static PrivateServerDao instance = null;

    private PrivateServerDao() {
    }

    public static PrivateServerDao getInstance() {
        if (instance == null) {
            instance = new PrivateServerDao();
        }
        return instance;
    }

    public Map<Long, PrivateServerService.Event> retrieveActiveEvents() {
        Map<Long, PrivateServerService.Event> activeEvents = new HashMap<>();
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, channel FROM gov_event WHERE isActive=true;");
            while (rs.next()) {
                int id = rs.getInt("id");
                long channelId = rs.getLong("channel");
                PrivateServerService.Event event = new PrivateServerService.Event(id, channelId);
                activeEvents.put(channelId, event);
            }
            for (long channelId : activeEvents.keySet()){
                PrivateServerService.Event event = activeEvents.get(channelId);
                int id = event.getEventId();
                ResultSet entriesRs = stmt.executeQuery("SELECT message_id FROM gov_event_entries WHERE event_id=" + id + ";");
                Set<Long> entries = new HashSet<>();
                while (entriesRs.next()) entries.add(entriesRs.getLong("message_id"));
                event.setEntries(entries);
                ResultSet votersRs = stmt.executeQuery(String.format("SELECT user_id, vote FROM gov_event_votes WHERE event_id=%d;", id));
                Map<Long, Long> votes = new HashMap<>();
                while (votersRs.next()) votes.put(votersRs.getLong("user_id"), votersRs.getLong("vote"));
                event.setVoters(votes);
            }
            return activeEvents;
        } catch (SQLException e) {
            e.printStackTrace();
            return activeEvents;
        }
    }

    public int addEvent(long channelId) {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT id FROM gov_event WHERE channel=%d AND isActive=%b;", channelId, true));
            if (rs.next()) return -1;
            PreparedStatement ps = conn.prepareStatement("INSERT INTO gov_event (channel, isActive) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, channelId);
            ps.setBoolean(2, true);
            ps.executeUpdate();

            try (ResultSet pars = ps.getGeneratedKeys()) {
                if (pars.next()) {
                    int eventId = pars.getInt(1);
                    return eventId;
                }
            }
            return -2;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    public void endEvent(long channelId) {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE gov_event SET isActive=false WHERE channel=%d and isActive=true;", channelId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PrivateServerService.Event restartEvent(int eventId) {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT id, channel from gov_event WHERE id=%d AND isActive=false;", eventId));
            if (!rs.next()) return null;
            long channelId = rs.getLong("channel");
            PrivateServerService.Event event = new PrivateServerService.Event(eventId, channelId);
            ResultSet entriesRs = stmt.executeQuery("SELECT message_id FROM gov_event_entries WHERE event_id=" + eventId + ";");
            Set<Long> entries = new HashSet<>();
            while (entriesRs.next()) entries.add(entriesRs.getLong("message_id"));
            event.setEntries(entries);
            ResultSet votersRs = stmt.executeQuery(String.format("SELECT user_id, vote FROM gov_event_votes WHERE event_id=%d;", eventId));
            Map<Long, Long> votes = new HashMap<>();
            while (votersRs.next()) votes.put(votersRs.getLong("user_id"), votersRs.getLong("vote"));
            event.setVoters(votes);
            stmt.executeUpdate("UPDATE gov_event SET isActive=true WHERE id=" + eventId + ";");
            return event;
        } catch (SQLException e) {
            return null;
        }
    }

    public void addEventEntries(long userId, long msgId, int eventId) {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format("INSERT INTO gov_event_entries (user_id, message_id, event_id) VALUES (%d, %d, %d);", userId, msgId, eventId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void vote(long userId, long messageId, int eventId) {
        String sql = "INSERT INTO gov_event_votes (user_id, event_id, vote) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE vote = VALUES(vote)";
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setInt(2, eventId);
            ps.setLong(3, messageId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
