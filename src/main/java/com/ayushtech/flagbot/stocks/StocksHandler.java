package com.ayushtech.flagbot.stocks;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.StocksDao;
import com.ayushtech.flagbot.dbconnectivity.StocksTransactionsDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

public class StocksHandler {

  private static Map<String, Integer> initialStockPriceMap;
  private static StocksHandler stocksHandler = null;
  private Map<Company, StockData> stocksMap;
  private ScheduledExecutorService stockPricesUpdateService;
  private String greenUpEmoji = "<:green_up:1207014598528602162>";
  private String redDownEmoji = "<:red_down:1207014620792102922>";
  private String[] companyArray = { "DOOGLE", "MAPPLE", "RAMSUNG", "MICROLOFT", "LOCKSTAR", "SEPSICO", "LETFLIX",
      "STARMUCKS", "TWEETER", "DISKORD" };

  private Set<Company> companies;

  private StocksHandler() {
    this.stockPricesUpdateService = new ScheduledThreadPoolExecutor(1);
    stocksMap = new HashMap<>();
    loadStocksMap();
    companies = stocksMap.keySet();
    stockPricesUpdateService.scheduleWithFixedDelay(() -> {
      companies.stream().forEach(c -> {
        int initialValue = stocksMap.get(c).getValue();
        int change = stocksMap.get(c).getStockFluctuation();
        stocksMap.get(c).resetData();
        stocksMap.get(c).setValue(initialValue + change);
        initialStockPriceMap.put(c.toString(), initialValue + change);
        stocksMap.get(c).setChange(change);
      });
      StocksDao.getInstance().setStockValue(initialStockPriceMap);
    }, 2, 2, TimeUnit.MINUTES);
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
          sSb.append("**" + c.toString() + "** (`" + stocksMap.get(c).getValue() + "`)"
              + getEmoji(stocksMap.get(c).getChange()) + "\n");
        });
    eb.addField("__**Company** (`Price`)__", sSb.toString(), false);
    eb.addField("__Last Updated__", TimeFormat.TIME_SHORT.now() + "", false);
    eb.setFooter("'/stocks buy' to buy stocks.");
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
        totalInvestment += (stocksOwnedData[i] * stocksMap.get(Company.valueOf(companyArray[i])).getValue());
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

    eb.setFooter("'/stocks sell' to sell your stocks");
    return eb.build();
  }

  public int[] buyStocks(Company company, int count, long userId) {
    int priceOfStock = stocksMap.get(company).getValue();
    boolean isBought = StocksDao.getInstance().buyStocks(userId, company.toString(), count,
        priceOfStock);
    if (isBought) {
      stocksMap.get(company).buyStock(count);
      StocksTransactionsDao.getInstance().addTransactionData(
          new StocksTransaction(userId, TransactionType.Bought, company, count, System.currentTimeMillis(),
              priceOfStock));
      return new int[] { 1, priceOfStock };
    }
    return new int[] { 0 };
  }

  public int[] sellStock(Company company, int count, long userId) {
    int priceOfStock = stocksMap.get(company).getValue();
    boolean isSold = StocksDao.getInstance().sellStocks(userId, company.toString(), count, priceOfStock);
    if (isSold) {
      stocksMap.get(company).sellStock(count);
      StocksTransactionsDao.getInstance().addTransactionData(
          new StocksTransaction(userId, TransactionType.Sold, company, count, System.currentTimeMillis(),
              priceOfStock));
      return new int[] { 1, priceOfStock };
    }
    return new int[] { 0 };
  }

  public MessageEmbed getTransactionsEmbed(long userId, int page) {
    List<StocksTransaction> list = StocksTransactionsDao.getInstance().getTransactions(userId, page, 10);
    System.out.println(list.size());
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

  private void loadStocksMap() {
    stocksMap.put(Company.DISKORD, new StockData(Company.DISKORD, initialStockPriceMap.get("DISKORD")));
    stocksMap.put(Company.DOOGLE, new StockData(Company.DOOGLE, initialStockPriceMap.get("DOOGLE")));
    stocksMap.put(Company.LETFLIX, new StockData(Company.LETFLIX, initialStockPriceMap.get("LETFLIX")));
    stocksMap.put(Company.LOCKSTAR, new StockData(Company.LOCKSTAR, initialStockPriceMap.get("LOCKSTAR")));
    stocksMap.put(Company.MAPPLE, new StockData(Company.MAPPLE, initialStockPriceMap.get("MAPPLE")));
    stocksMap.put(Company.MICROLOFT, new StockData(Company.MICROLOFT, initialStockPriceMap.get("MICROLOFT")));
    stocksMap.put(Company.RAMSUNG, new StockData(Company.RAMSUNG, initialStockPriceMap.get("RAMSUNG")));
    stocksMap.put(Company.SEPSICO, new StockData(Company.SEPSICO, initialStockPriceMap.get("SEPSICO")));
    stocksMap.put(Company.STARMUCKS, new StockData(Company.STARMUCKS, initialStockPriceMap.get("STARMUCKS")));
    stocksMap.put(Company.TWEETER, new StockData(Company.TWEETER, initialStockPriceMap.get("TWEETER")));
  }

  public static void loadInitialPriceMap() {
    initialStockPriceMap = StocksDao.getInstance().getStocksValue();
  }

  public boolean isCompanyValid(String company) {
    return initialStockPriceMap.containsKey(company);
  }

  private String getEmoji(int changed) {
    return (changed > 0) ? (greenUpEmoji) : ((changed < 0) ? redDownEmoji : "");
  }
}