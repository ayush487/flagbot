package com.ayushtech.flagbot.utils;

public class LbEntry {
    private long rank;
    private long userId;
    private String name;
    private long score;
    public LbEntry(long rank, long userId, long score, String name) {
        this.rank = rank;
        this.userId = userId;
        this.score = score;
        this.name = name;
    }
    public long getRank() {
        return rank;
    }
    public long getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getScore() {
        return score;
    }

    public String getRankingString() {
        if (rank == 1) {
            return "🥇";
        } else if (rank == 2) {
            return "🥈";
        } else if (rank == 3) {
            return "🥉";
        } else {
            return "**" + rank + ".**";
        }
    }
}
