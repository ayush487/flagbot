package com.ayushtech.flagbot.utils;

import java.util.List;

public class LbData {
    private long offset;
    private List<LbEntry> entries;
    private long totalCount;
    private String type;
    private long userRank;
    public LbData(long offset, String type) {
        this.offset = offset;
        this.type = type;
    }

    public long getOffset() {
        return offset;
    }

    public List<LbEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LbEntry> entries) {
        this.entries = entries;
    }

    public long getUserRank() {
        return userRank;
    }

    public void setUserRank(long userRank) {
        this.userRank = userRank;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public String getType() {
        return type;
    }
}
