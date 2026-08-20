package com.ayushtech.flagbot.utils;

import com.ayushtech.flagbot.crossword.CrosswordBgTile;
import com.ayushtech.flagbot.crossword.CrosswordFgTile;

public interface Constants {
    public final int UPDATE_VERSION = 3;
    public final CrosswordBgTile DEFAULT_BGTILE = CrosswordBgTile.BLACK;
    public final CrosswordFgTile DEFAULT_EMPTYTILE = CrosswordFgTile.WHITE;
    public final long GAMBLE_COMMAND_COOLDOWN = 20000;
    public final int MAX_GAMBLE_AMOUNT = 10000;
    public final int BOUND = 500;

}
