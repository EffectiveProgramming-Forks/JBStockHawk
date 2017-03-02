package com.udacity.stockhawk.data;

import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

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

    public static String getFormattedPriceValue(float value) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        return decimalFormat.format(value);
    }

    public static String getFormattedAbsoluteChangeValue(float value) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        decimalFormat.setPositivePrefix("+$");
        return decimalFormat.format(value);
    }

    public static String getFormattedPercentageChangeValue(float value) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setPositivePrefix("+");
        return decimalFormat.format(value / 100);
    }

    public static int getChangeBackgroundResource(float absoluteChange) {
        if (absoluteChange > 0) {
            return R.drawable.percent_change_pill_green;
        } else {
            return R.drawable.percent_change_pill_red;
        }
    }
}
