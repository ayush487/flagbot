package com.ayushtech.flagbot.services;

import java.awt.Color;

import com.ayushtech.flagbot.dbconnectivity.UserDao;
import com.ayushtech.flagbot.utils.Constants;
import com.ayushtech.flagbot.utils.LRUCache;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class UpdateReminder {

    private static UpdateReminder instance = null;

    private LRUCache<Long, Integer> cache;

    private UpdateReminder() {
        this.cache = new LRUCache<Long, Integer>(5000);
    }

    public static synchronized UpdateReminder getInstance() {
        if (instance == null)
            instance = new UpdateReminder();
        return instance;
    }

    public void handleUser(User user, MessageChannel channel) {
        if (!cache.containsKey(user.getIdLong())) {
            int userLatestUpdate = UserDao.getInstance().getUserLatestUpdate(user.getIdLong());
            cache.put(user.getIdLong(), Constants.UPDATE_VERSION);
            if (userLatestUpdate != Constants.UPDATE_VERSION) {
                channel.sendMessageEmbeds(changeLogs(user.getAvatarUrl())).queue();
                UserDao.getInstance().setUserUpdateVersion(user.getIdLong());
            }
        }
    }

    private MessageEmbed changeLogs(String avatarUrl) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.PINK);
        eb.setTitle("🎉 Flag Bot Update — New Feature!");
        eb.setDescription("Here's what's new in the latest update:");
        eb.addField("⚔️ New Command: `/crossduel`",
                "Challenge another player to a head-to-head crossword duel! Race to solve the puzzle faster than your opponent and claim victory.",
                false);
        eb.addField(
                "💎 Patreon Perks Updated!",
                "Patreon supporters now receive even more exclusive perks:\n\n"
                        + "🧩 **+1 Extra Hint** — Get an additional hint in Crossword games.\n"
                        + "💰 **2× Daily Rewards** — Receive double the rewards from your daily claim.\n"
                        + "🎨 **Crossword Appearance** — Customize the look of your crossword using `/crossword_appearance`.\n\n"
                        + "Thank you for supporting Flag Bot! ❤️",
                false);
        eb.setFooter("Flag Bot • Changelog", avatarUrl);
        return eb.build();
    }

}
