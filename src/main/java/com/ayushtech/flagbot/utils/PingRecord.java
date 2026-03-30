package com.ayushtech.flagbot.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PingRecord {

    private static PingRecord pingRecord = null;

    private Calendar pingDate;
    private static final int DAILY_PING_LIMIT = 2;
    private int chatPingCount = 0;
    private int vcPingCount = 0;
    private long lastChatPinged = 0;
    private long lastVcPinged = 0;

    private PingRecord() {
        this.pingDate = new GregorianCalendar();
    }

    public static PingRecord getInstance() {
        if (pingRecord == null) {
            pingRecord = new PingRecord();
        }
        return pingRecord;
    }

    public boolean isChatPingAllowed() {
        Calendar currentDate = new GregorianCalendar();
        if (currentDate.get(Calendar.DAY_OF_YEAR) != pingDate.get(Calendar.DAY_OF_YEAR)) {
            chatPingCount = 0;
            vcPingCount = 0;
            pingDate = currentDate;
        }
        if (System.currentTimeMillis() - lastChatPinged < 3600000)
                return false;
        if (chatPingCount < DAILY_PING_LIMIT) {
            chatPingCount++;
            lastChatPinged = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean isVcPingAllowed() {
        Calendar currentDate = new GregorianCalendar();
        if (currentDate.get(Calendar.DAY_OF_YEAR) != pingDate.get(Calendar.DAY_OF_YEAR)) {
            chatPingCount = 0;
            vcPingCount = 0;
            pingDate = currentDate;
        }
        if (System.currentTimeMillis() - lastVcPinged < 3600000)
                return false;
        if (vcPingCount < DAILY_PING_LIMIT) {
            vcPingCount++;
            lastVcPinged = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
