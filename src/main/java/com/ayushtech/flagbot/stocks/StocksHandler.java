package com.ayushtech.flagbot.stocks;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ayushtech.flagbot.dbconnectivity.StocksDao;
import com.ayushtech.flagbot.dbconnectivity.StocksTransactionsDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class StocksHandler {

  private static Map<String, Integer> initialStockPriceMap;
  private static StocksHandler stocksHandler = null;
  private String[] companyArray = { "DOOGLE", "MAPPLE", "RAMSUNG", "MICROLOFT", "LOCKSTAR", "SEPSICO", "LETFLIX",
      "STARMUCKS", "TWEETER", "DISKORD" };

  private StocksHandler() {
  }

  public static synchronized StocksHandler getInstance() {
    if (stocksHandler == null) {
      stocksHandler = new StocksHandler();
    }
    return stocksHandler;
  }

  public MessageEmbed getStockList() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Stock Market");
    eb.setThumbnail(
        "https://media.discordapp.net/attachments/1133277774010925206/1207270561785319424/stock_market_image.jpg?ex=65df0953&is=65cc9453&hm=a26c6292faca229755619ab63cfb05a2070c6d8ffef3d453c7a5c8cf4017f4b2&=&format=webp&width=150&height=150");
    StringBuilder sSb = new StringBuilder();
    Arrays.stream(companyArray)
        .map(Company::valueOf)
        .forEach(c -> {
          sSb.append("**" + c.toString() + "** (`" + initialStockPriceMap.get(c.name()) + "`)\n");
        });
    eb.addField("__**Company** (`Price`)__", sSb.toString(), false);
    eb.addField("_Note__", "You can no longer buy them, you can still sell if you have using command `/stocks sell`",
        false);

    eb.setColor(Color.YELLOW);
    return eb.build();
  }

  public MessageEmbed getStocksOwned(User user) {
    int[] stocksOwnedData = StocksDao.getInstance().getUserStocks(user.getIdLong(), companyArray);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(user.getName() + "'s Portfolio");

    boolean isUserOwnAnyStock = false;
    int totalInvestment = 0;
    for (int i = 0; i < companyArray.length; i++) {
      if (stocksOwnedData[i] > 0) {
        eb.addField(companyArray[i], stocksOwnedData[i] + "", true);
        isUserOwnAnyStock = true;
        totalInvestment += (stocksOwnedData[i] * initialStockPriceMap.get(Company.valueOf(companyArray[i]).name()));
      }
    }
    if (isUserOwnAnyStock) {
      eb.setDescription("Stocks owned");
      eb.setColor(Color.green);
      eb.addField("__Total Investments__", totalInvestment + " :coin:", false);
    } else {
      eb.setDescription("You don't own any stocks, `/stocks list` to view available stocks.");
      eb.setColor(Color.red);
    }
    eb.setFooter("You can no longer buy stocks!");
    return eb.build();
  }

  public int[] sellStock(Company company, int count, long userId) {
    int priceOfStock = initialStockPriceMap.get(company.name());
    boolean isSold = StocksDao.getInstance().sellStocks(userId, company.toString(), count, priceOfStock);
    if (isSold) {
      StocksTransactionsDao.getInstance().addTransactionData(
          new StocksTransaction(userId, TransactionType.Sold, company, count, System.currentTimeMillis(),
              priceOfStock));
      return new int[] { 1, priceOfStock };
    }
    return new int[] { 0 };
  }

  public static void loadInitialPriceMap() {
    initialStockPriceMap = StocksDao.getInstance().getStocksValue();
  }

  public boolean isCompanyValid(String company) {
    return initialStockPriceMap.containsKey(company);
  }

  public void handleStockTransactionButton(ButtonInteractionEvent event) {
    int page = Integer.parseInt(event.getComponentId().split("_")[1]);
    MessageEmbed eb = StocksHandler.getInstance().getTransactionsEmbed(event.getUser().getIdLong(), page);
    event.replyEmbeds(eb).setEphemeral(true).queue();
  }

  private MessageEmbed getTransactionsEmbed(long userId, int page) {
    List<StocksTransaction> list = StocksTransactionsDao.getInstance().getTransactions(userId, page, 10);
    EmbedBuilder eb = new EmbedBuilder();
    if (list.size() == 0) {
      eb.setColor(Color.red);
      eb.setTitle("No Transaction Data Available");
    } else {
      eb.setColor(Color.green);
      eb.setTitle(String.format("__Your last %d transactions__", list.size()));
      StringBuilder sb = new StringBuilder();
      list.stream()
          .map(StocksTransaction::getTransactionMessage)
          .forEach(m -> sb.append(m));
      eb.setDescription(sb.toString());
    }
    return eb.build();
  }
}