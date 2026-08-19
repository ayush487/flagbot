package com.ayushtech.flagbot.crossword;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum CrosswordFgTile {
    BLACK(Emoji.fromFormatted("<:bke:1537122254998085764>"), "Black"),
    WHITE(Emoji.fromFormatted("<:wte:1537122243296104549>"), "White"),
    ORANGE(Emoji.fromFormatted("<:oge:1537101334288007189>"), "Orange"),
    BLUE(Emoji.fromFormatted("<:ble:1537101323680612434>"), "Blue"),
    RED(Emoji.fromFormatted("<:rde:1537101338398163106>"), "Red"),
    BROWN(Emoji.fromFormatted("<:bwe:1537101326075564095>"), "Brown"),
    PURPLE(Emoji.fromFormatted("<:ple:1537101336238096456>"), "Purple"),
    GREEN(Emoji.fromFormatted("<:gne:1537101327786704967>"), "Green"),
    YELLOW(Emoji.fromFormatted("<:yle:1537101340336201778>"), "Yellow");

    private Emoji emoji;
    private String name;

    CrosswordFgTile(Emoji emoji, String name) {
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
