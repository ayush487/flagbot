package com.ayushtech.flagbot.memoflip;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.MemoflipDao;
import com.ayushtech.flagbot.services.GameEndService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class MemoflipHandler {

  private static MemoflipHandler memoflipHandler = null;
  private Map<Long, Memoflip> memoflipGameMap;
  private Emoji[] emojis;
  private List<Card> easyCardList;
  private List<Card> mediumCardList;
  private List<Card> hardCardList;
  private Emoji questionEmoji = Emoji.fromCustom("question_mark", 1231126072909631518l, false);

  private MemoflipHandler() {
    memoflipGameMap = new HashMap<>();
    emojis = new Emoji[12];
    easyCardList = new ArrayList<>(8);
    mediumCardList = new ArrayList<>(16);
    hardCardList = new ArrayList<>(24);
    loadEmojis();
  }

  public static MemoflipHandler getInstance() {
    if (memoflipHandler == null) {
      memoflipHandler = new MemoflipHandler();
    }
    return memoflipHandler;
  }

  public void handleMemoflipCommand(SlashCommandInteractionEvent event) {
    String difficulty = event.getSubcommandName();
    switch (difficulty) {
      case "easy":
        startEasyGame(event);
        return;
      case "hard":
        startHardGame(event);
        return;
      case "medium":
        startMediumGame(event);
        return;
      default:
        sendScoresEmbed(event);
        return;
    }

  }

  private void sendScoresEmbed(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setColor(Color.CYAN);
    int[] scores = MemoflipDao.getInstance().getScores(event.getUser().getIdLong());
    eb.addField("__Best Scores__",
        String.format("**Easy Mode** : `%d turns`\n**Medium Mode** : `%d turns`\n**Hard Mode** : `%d turns`", scores[0],
            scores[1], scores[2]),
        false);
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  private void startEasyGame(SlashCommandInteractionEvent event) {
    Collections.shuffle(easyCardList);
    Card[][] gameCards = new Card[3][3];
    int index = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (i == 1 && j == 1) {
          gameCards[i][j] = new Card(69, Emoji.fromCustom("flagbot", 1230836096548601907l, false));
        } else {
          Card c = easyCardList.get(index);
          gameCards[i][j] = new Card(c.getId(), c.getEmoji());
          index++;
        }
      }
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription("**Difficulty** : `Easy`\n__Rewards__ : `300` <:flag_coin:1472232340523843767>");
    eb.setColor(Color.BLUE);
    ActionRow[] rows = getButtons(gameCards, event.getUser().getIdLong());
    event.getHook().sendMessageEmbeds(eb.build())
        .addComponents(rows[0], rows[1], rows[2])
        .queue(m -> {
          long messageId = m.getIdLong();
          Memoflip game = new Memoflip(event.getUser().getIdLong(), gameCards, m, Difficulty.EASY, 300);
          memoflipGameMap.put(messageId, game);
          GameEndService.getInstance().scheduleEndGame(() -> {
            if (memoflipGameMap.containsKey(messageId)) {
              game.endGame(true);
              memoflipGameMap.remove(messageId);
            }
          }, 150, TimeUnit.SECONDS);
        });
  }

  private void startMediumGame(SlashCommandInteractionEvent event) {
    Collections.shuffle(mediumCardList);
    Card[][] gameCards = new Card[4][4];
    int index = 0;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        Card c = mediumCardList.get(index);
        gameCards[i][j] = new Card(c.getId(), c.getEmoji());
        index++;
      }
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription("**Difficulty** : `Medium`\n__Rewards__ : `600` <:flag_coin:1472232340523843767>");
    eb.setColor(Color.BLUE);
    ActionRow[] rows = getButtons(gameCards, event.getUser().getIdLong());
    event.getHook().sendMessageEmbeds(eb.build())
        .addComponents(rows[0], rows[1], rows[2], rows[3])
        .queue(m -> {
          long messageId = m.getIdLong();
          Memoflip game = new Memoflip(event.getUser().getIdLong(), gameCards, m, Difficulty.MEDIUM, 600);
          memoflipGameMap.put(messageId, game);
          GameEndService.getInstance().scheduleEndGame(() -> {
            if (memoflipGameMap.containsKey(messageId)) {
              game.endGame(true);
              memoflipGameMap.remove(messageId);
            }
          }, 240, TimeUnit.SECONDS);
        });
  }

  private void startHardGame(SlashCommandInteractionEvent event) {
    Collections.shuffle(hardCardList);
    Card[][] gameCards = new Card[5][5];
    int index = 0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (i == 2 && j == 2) {
          gameCards[i][j] = new Card(69, Emoji.fromCustom("flagbot", 1230836096548601907l, false));
        } else {
          Card c = hardCardList.get(index);
          gameCards[i][j] = new Card(c.getId(), c.getEmoji());
          index++;
        }
      }
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription("**Difficulty** : `HARD`\n__Rewards__ : `1000` <:flag_coin:1472232340523843767>");
    eb.setColor(Color.BLUE);
    ActionRow[] rows = getButtons(gameCards, event.getUser().getIdLong());
    event.getHook().sendMessageEmbeds(eb.build())
        .addComponents(rows[0], rows[1], rows[2], rows[3], rows[4])
        .queue(m -> {
          long messageId = m.getIdLong();
          Memoflip game = new Memoflip(event.getUser().getIdLong(), gameCards, m, Difficulty.HARD, 1000);
          memoflipGameMap.put(messageId, game);
          GameEndService.getInstance().scheduleEndGame(() -> {
            if (memoflipGameMap.containsKey(messageId)) {
              game.endGame(true);
              memoflipGameMap.remove(messageId);
            }
          }, 360, TimeUnit.SECONDS);
        });
  }

  public void handleCardButton(ButtonInteractionEvent event) {
    String[] cmdData = event.getComponentId().split("_");
    String id = cmdData[3];
    if (!event.getUser().getId().equals(id)) {
      event.reply("You can't use this button").setEphemeral(true).queue();
      return;
    }
    int i = Integer.parseInt(cmdData[1]);
    int j = Integer.parseInt(cmdData[2]);
    long messageId = event.getMessage().getIdLong();
    Memoflip game = memoflipGameMap.get(messageId);
    game.incrementTurns();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Memory Flip");
    eb.setDescription(String.format("**Difficulty** : `%s`\n**Turns** : `%d`", game.getDifficulty(), game.getTurns()));
    eb.setDescription(String.format("**Difficulty** : `%s`\n**Turns** : `%d`\n__Reward__ : `%d` <:flag_coin:1472232340523843767>",
        game.getDifficulty(), game.getTurns(), game.getRewards()));
    eb.setColor(Color.BLUE);
    if (game.isCardSelected()) {
      Card selectedCard = game.getSelectedCard();
      Card choosenCard = game.getCard(i, j);
      game.removeSelection();
      if (selectedCard.getId() == choosenCard.getId()) {
        selectedCard.setStatus(CardStatus.CORRECT);
        choosenCard.setStatus(CardStatus.CORRECT);
        game.increasePoints();
        if (game.isWin()) {
          memoflipGameMap.remove(game.getMessageId());
          game.endGameAsWin(event);
          return;
        } else {
          event.editMessageEmbeds(eb.build())
              .setComponents(getButtons(game.getCards(), game.getUserId()))
              .queue();
          return;
        }
      } else {
        selectedCard.setStatus(CardStatus.WRONG);
        choosenCard.setStatus(CardStatus.WRONG);
        event.editMessageEmbeds(eb.build())
            .setComponents(getButtonsAsDisabled(game.getCards()))
            .queue(hook -> {
              selectedCard.setStatus(CardStatus.HIDDEN);
              choosenCard.setStatus(CardStatus.HIDDEN);
              if (game.isNotEnd())
                hook.editMessageEmbedsById(game.getMessageId(), eb.build())
                    .setComponents(getButtons(game.getCards(), game.getUserId())).queueAfter(1, TimeUnit.SECONDS);

            });
      }
    } else {
      game.getCard(i, j).setStatus(CardStatus.SELECTED);
      game.setSelectedCard(i, j);
      event.editMessageEmbeds(eb.build())
          .setComponents(getButtons(game.getCards(), game.getUserId()))
          .queue();
    }

  }

  public ActionRow[] getButtons(Card[][] cards, long userId) {
    switch (cards.length) {
      case 3:
        return getButtonsEasy(cards, userId);
      case 4:
        return getButtonsMedium(cards, userId);
      case 5:
        return getButtonsHard(cards, userId);
      default:
        return getButtonsHard(cards, userId);
    }
  }

  private ActionRow[] getButtonsEasy(Card[][] cards, long userId) {
    ActionRow[] rows = new ActionRow[3];
    for (int i = 0; i < 3; i++) {
      rows[i] = ActionRow.of(
          getButton(cards[i][0], i, 0, userId, 3),
          getButton(cards[i][1], i, 1, userId, 3),
          getButton(cards[i][2], i, 2, userId, 3));
    }
    return rows;
  }

  private ActionRow[] getButtonsMedium(Card[][] cards, long userId) {
    ActionRow[] rows = new ActionRow[4];
    for (int i = 0; i < 4; i++) {
      rows[i] = ActionRow.of(
          getButton(cards[i][0], i, 0, userId, 4),
          getButton(cards[i][1], i, 1, userId, 4),
          getButton(cards[i][2], i, 2, userId, 4),
          getButton(cards[i][3], i, 3, userId, 4));
    }
    return rows;
  }

  private ActionRow[] getButtonsHard(Card[][] cards, long userId) {
    ActionRow[] rows = new ActionRow[5];
    for (int i = 0; i < 5; i++) {
      rows[i] = ActionRow.of(
          getButton(cards[i][0], i, 0, userId, 5),
          getButton(cards[i][1], i, 1, userId, 5),
          getButton(cards[i][2], i, 2, userId, 5),
          getButton(cards[i][3], i, 3, userId, 5),
          getButton(cards[i][4], i, 4, userId, 5));
    }
    return rows;
  }

  public ActionRow[] getButtonsAsDisabled(Card[][] cards) {
    switch (cards.length) {
      case 3:
        return getButtonsEasyDisabled(cards);
      case 4:
        return getButtonsMediumDisabled(cards);
      case 5:
        return getButtonsHardDisabled(cards);
      default:
        return getButtonsHardDisabled(cards);
    }
  }

  private ActionRow[] getButtonsEasyDisabled(Card[][] cards) {
    ActionRow[] rows = new ActionRow[3];
    for (int i = 0; i < 3; i++) {
      rows[i] = ActionRow.of(
          getButtonAsDisabled(cards[i][0], i, 0, 3),
          getButtonAsDisabled(cards[i][1], i, 1, 3),
          getButtonAsDisabled(cards[i][2], i, 2, 3));
    }
    return rows;
  }

  private ActionRow[] getButtonsMediumDisabled(Card[][] cards) {
    ActionRow[] rows = new ActionRow[4];
    for (int i = 0; i < 4; i++) {
      rows[i] = ActionRow.of(
          getButtonAsDisabled(cards[i][0], i, 0, 4),
          getButtonAsDisabled(cards[i][1], i, 1, 4),
          getButtonAsDisabled(cards[i][2], i, 2, 4),
          getButtonAsDisabled(cards[i][3], i, 3, 4));
    }
    return rows;
  }

  private ActionRow[] getButtonsHardDisabled(Card[][] cards) {
    ActionRow[] rows = new ActionRow[5];
    for (int i = 0; i < 5; i++) {
      rows[i] = ActionRow.of(
          getButtonAsDisabled(cards[i][0], i, 0, 5),
          getButtonAsDisabled(cards[i][1], i, 1, 5),
          getButtonAsDisabled(cards[i][2], i, 2, 5),
          getButtonAsDisabled(cards[i][3], i, 3, 5),
          getButtonAsDisabled(cards[i][4], i, 4, 5));
    }
    return rows;
  }

  private Button getButton(Card card, int i, int j, long userId, int size) {
    if (size % 2 != 0 && i == j && j == (size / 2)) {
      return Button.secondary("logoDisplay", card.getEmoji()).asDisabled();
    } else {
      if (card.getStatus() == CardStatus.HIDDEN) {
        return Button.primary("cardButton_" + i + "_" + j + "_" + userId, questionEmoji);
      } else if (card.getStatus() == CardStatus.SELECTED) {
        return Button.primary("cardButton_" + i + "_" + j + "_" + userId, card.getEmoji()).asDisabled();
      } else if (card.getStatus() == CardStatus.CORRECT) {
        return Button.success("cardButton_" + i + "_" + j + "_" + userId, card.getEmoji()).asDisabled();
      } else {
        return Button.danger("cardButton_" + i + "_" + j + "_" + userId, card.getEmoji()).asDisabled();
      }
    }
  }

  private Button getButtonAsDisabled(Card card, int i, int j, int size) {
    if (size % 2 != 0 && i == j && j == (size / 2)) {
      return Button.secondary("logoDisplay", card.getEmoji()).asDisabled();
    }
    if (card.getStatus() == CardStatus.HIDDEN) {
      return Button.primary("cardButton_" + i + "_" + j, questionEmoji).asDisabled();
    } else if (card.getStatus() == CardStatus.SELECTED) {
      return Button.primary("cardButton_" + i + "_" + j, card.getEmoji()).asDisabled();
    } else if (card.getStatus() == CardStatus.CORRECT) {
      return Button.success("cardButton_" + i + "_" + j, card.getEmoji()).asDisabled();
    } else {
      return Button.danger("cardButton_" + i + "_" + j, card.getEmoji()).asDisabled();
    }
  }

  private void loadEmojis() {
    emojis[0] = Emoji.fromCustom("mickey", 1227219971399094283l, false);
    emojis[1] = Emoji.fromCustom("doramon", 1227201968766713869l, false);
    emojis[2] = Emoji.fromCustom("DonaldDuck", 1227224502107377664l, false);
    emojis[3] = Emoji.fromCustom("pepe", 1227202432442957874l, false);
    emojis[4] = Emoji.fromCustom("peng", 1227207404651941959l, false);
    emojis[5] = Emoji.fromCustom("tom", 1227216763478085654l, false);
    emojis[6] = Emoji.fromCustom("Shinchan", 1227223965270020191l, false);
    emojis[7] = Emoji.fromCustom("jerry", 1227218745135599668l, false);
    emojis[8] = Emoji.fromCustom("spongebob", 1227219444238127176l, false);
    emojis[9] = Emoji.fromCustom("Courage", 1227220836377825281l, false);
    emojis[10] = Emoji.fromCustom("oggy", 1227221518157615168l, false);
    emojis[11] = Emoji.fromCustom("tweety", 1227222248822276096l, false);
    for (int i = 0; i < emojis.length; i++) {
      hardCardList.add(new Card(i + 1, emojis[i]));
      hardCardList.add(new Card(i + 1, emojis[i]));
    }
    for (int i = 0; i < 4; i++) {
      easyCardList.add(new Card(i + 1, emojis[i]));
      easyCardList.add(new Card(i + 1, emojis[i]));
    }
    for (int i = 0; i < 8; i++) {
      mediumCardList.add(new Card(i + 1, emojis[i]));
      mediumCardList.add(new Card(i + 1, emojis[i]));
    }
  }
}