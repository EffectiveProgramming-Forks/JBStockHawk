package com.udacity.stockhawk.data;

import yahoofinance.Stock;

/**
 * Created by jkbreunig on 2/28/17.
 */

public class StockUtils {

    public static boolean isValidSymbol(String name) {

        return !name.isEmpty() && PrefUtils.isAlpha(name);
    }

    public static boolean isValidStock(Stock stock) {

        if (stock == null || stock.getName() == null || stock.getName().isEmpty()) {
            return false;
        }
        return true;
    }
}
