package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Calendar;

public class Parser {

    public static List<Entry> getStockHistory(String history) {
        List<Entry> entries = new ArrayList<>();
        List<Float> timeData = new ArrayList<>();
        List<Float> stockPrice = new ArrayList<>();
        String[] dataPairs = history.split("\\$");

        for (String pair : dataPairs) {
            String[] entry = pair.split(":");
            if (entry.length == 2) {
                timeData.add(Float.valueOf(entry[0]));
                stockPrice.add(Float.valueOf(entry[1]));
            }
        }
        Collections.reverse(timeData);
        Collections.reverse(stockPrice);
        Float referenceTime = timeData.size() > 0 ? timeData.get(0) : Calendar.getInstance().getTimeInMillis() / 1000;
        for (int i = 0; i < timeData.size(); i++) {
            entries.add(new Entry(timeData.get(i) - referenceTime, stockPrice.get(i)));
        }
        return entries;
    }
}
