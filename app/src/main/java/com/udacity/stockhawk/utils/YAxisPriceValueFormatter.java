package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class YAxisPriceValueFormatter implements IAxisValueFormatter {
    private final DecimalFormat mFormat;

    public YAxisPriceValueFormatter() {
        mFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
        mFormat.setMaximumFractionDigits(2);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format(mFormat.format(value), value);
    }
}