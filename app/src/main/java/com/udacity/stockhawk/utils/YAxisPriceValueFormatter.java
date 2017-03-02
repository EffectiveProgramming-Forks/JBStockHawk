package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by jkbreunig on 3/2/17.
 */

public class YAxisPriceValueFormatter implements IAxisValueFormatter {
    private final DecimalFormat dollarFormat;

    public YAxisPriceValueFormatter() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
        dollarFormat.setMaximumFractionDigits(0);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format(dollarFormat.format(value), value);
    }
}