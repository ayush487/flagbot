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
        eb.addField("🎰 New Gambling Commands!",
                "Try your luck with three new ways to gamble your coins:\n\n"
                        + "🪙 **`/coinflip`** — Bet on heads or tails for a quick double-or-nothing.\n"
                        + "🎰 **`/slots`** — Spin the reels and match symbols to win big.\n"
                        + "💣 **`/mines`** — Reveal tiles and cash out before you hit a mine!",
                false);
        eb.addField("⌨️ Prefix Commands Now Available!",
        "You can now use Flag Bot with `f!` prefix commands too, no slash needed:\n\n"
                + "`f!balance` `f!leaderboard` `f!invite` `f!help`\n"
                + "`f!crossword` `f!crossduel` `f!vote`\n"
                + "`f!coinflip` `f!mines` `f!slots`",
        false);
        eb.setFooter("Flag Bot • Changelog", avatarUrl);
        return eb.build();
    }

}
