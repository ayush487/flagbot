package com.ayushtech.flagbot.game.location;

import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LocationGame {

        public LocationGame(InteractionHook hook, LocationMap locationMap, long userId) {

                hook.sendMessageEmbeds(getEmbed(locationMap))
                                .addActionRows(getActionRows(locationMap, userId))
                                .queue();
        }

        private MessageEmbed getEmbed(LocationMap locationMap) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Select the correct location of the place on the map");
                eb.setColor(Color.YELLOW);
                eb.setThumbnail(String.format(
                                "https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg",
                                locationMap.getPlaceCode()));
                eb.setImage(String.format(
                                "https://raw.githubusercontent.com/ayush487/image-library/main/places-maps/%s.png",
                                locationMap.getCode()));
                return eb.build();
        }

        private ActionRow[] getActionRows(LocationMap locationMap, long userId) {
                ActionRow[] rows = new ActionRow[2];
                rows[0] = ActionRow.of(
                                Button.primary(
                                                String.format("selectLocation_1_%d_%s_%d",
                                                                locationMap.getCorrectAnswer(),
                                                                locationMap.getCode(), userId),
                                                "1"),
                                Button.primary(
                                                String.format("selectLocation_2_%d_%s_%d",
                                                                locationMap.getCorrectAnswer(),
                                                                locationMap.getCode(), userId),
                                                "2"),
                                Button.primary(
                                                String.format("selectLocation_3_%d_%s_%d",
                                                                locationMap.getCorrectAnswer(),
                                                                locationMap.getCode(), userId),
                                                "3"),
                                Button.primary(
                                                String.format("selectLocation_4_%d_%s_%d",
                                                                locationMap.getCorrectAnswer(),
                                                                locationMap.getCode(), userId),
                                                "4"),
                                Button.primary(
                                                String.format("selectLocation_5_%d_%s_%d",
                                                                locationMap.getCorrectAnswer(),
                                                                locationMap.getCode(), userId),
                                                "5"));
                rows[1] = ActionRow.of(
                                Button.primary(
                                                String.format("skipLocation_%d_%d_%s", userId,
                                                                locationMap.getCorrectAnswer(), locationMap.getCode()),
                                                "Skip"),
                                Button.primary("viewPlace_" + locationMap.getPlaceCode(), "View Enlarged Image"));
                return rows;
        }
}