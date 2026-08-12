package com.ayushtech.flagbot.crossword;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum CrosswordBgTile {
    BLACK(Emoji.fromUnicode("U+2B1B"), "Black"),
    WHITE(Emoji.fromUnicode("U+2B1C"), "White"),
    ORANGE(Emoji.fromUnicode("U+1F7E7"), "Orange"),
    BLUE(Emoji.fromUnicode("U+1F7E6"), "Blue"),
    RED(Emoji.fromUnicode("U+1F7E5"), "Red"),
    BROWN(Emoji.fromUnicode("U+1F7EB"), "Brown"),
    PURPLE(Emoji.fromUnicode("U+1F7EA"), "Purple"),
    GREEN(Emoji.fromUnicode("U+1F7E9"), "Green"),
    YELLOW(Emoji.fromUnicode("U+1F7E8"), "Yellow");

    private Emoji emoji;
    private String name;

    CrosswordBgTile(Emoji emoji, String name) {
        this.emoji = emoji;
        this.name = name;
    }

    public Emoji getEmoji() {
        return this.emoji;
    }

    public String getName() {
        return this.name;
    }
}
