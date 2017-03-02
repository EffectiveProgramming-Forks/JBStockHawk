package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.breunig.jeff.stock.hawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuote(Context context) {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();

                Stock stock = quotes.get(symbol);
                Timber.d("stock is null " + stock);
                if (stock == null || stock.getName() == null || stock.getName().isEmpty()) {
                    Timber.d("stock is null" + stock);
                    continue;
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_EXCHANGE, stock.getStockExchange());

                StockQuote quote = stock.getQuote();
                if (quote != null) {
                    putFloatValue(quoteCV, Contract.Quote.COLUMN_PRICE, quote.getPrice());
                    putFloatValue(quoteCV, Contract.Quote.COLUMN_PERCENTAGE_CHANGE, quote.getChangeInPercent());
                    putFloatValue(quoteCV, Contract.Quote.COLUMN_ABSOLUTE_CHANGE, quote.getChange());
                }

                putHistoricalQuotes(quoteCV, stock);

                quoteCVs.add(quoteCV);
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            updateWidget(context);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void putFloatValue(ContentValues contentValues, String column, BigDecimal value) {
        if (value != null) {
            float floatValue = value.floatValue();
            contentValues.put(column, floatValue);
        }
    }

    private static void putHistoricalQuotes(ContentValues contentValues, Stock stock) {
        // WARNING! Don't request historical data for a stock that doesn't exist!
        // The request will hang forever X_x
        putHistorialQuotesForUnitOfTime(contentValues, stock, Calendar.MONTH, -11, Interval.MONTHLY, Contract.Quote.COLUMN_YEAR_HISTORY);
        putHistorialQuotesForUnitOfTime(contentValues, stock, Calendar.MONTH, -5, Interval.MONTHLY, Contract.Quote.COLUMN_MONTH_HISTORY);
        putHistorialQuotesForUnitOfTime(contentValues, stock, Calendar.DAY_OF_YEAR, -63, Interval.WEEKLY,Contract.Quote.COLUMN_WEEK_HISTORY);
        putHistorialQuotesForUnitOfTime(contentValues, stock, Calendar.DAY_OF_YEAR, -4, Interval.DAILY,Contract.Quote.COLUMN_DAY_HISTORY);
    }

    private static void putHistorialQuotesForUnitOfTime(ContentValues contentValues, Stock stock, int unit, int time, Interval interval, String column) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(unit, time);

        if (StockUtils.isValidStock(stock)) {
            try {
                List<HistoricalQuote> historyQuotes = stock.getHistory(from, to, interval);
                String historyString = getHistoryString(historyQuotes);
                contentValues.put(column, historyString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getHistoryString(List<HistoricalQuote> historyQuotes) {
        StringBuilder historyBuilder = new StringBuilder();

        for (HistoricalQuote it : historyQuotes) {
            historyBuilder.append(it.getDate().getTimeInMillis());
            historyBuilder.append(":");
            historyBuilder.append(it.getClose());
            historyBuilder.append("$");
        }
        return historyBuilder.toString();
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");
        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }

    public static void updateWidget(Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);
    }

}
