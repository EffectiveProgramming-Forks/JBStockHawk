package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisDateValueFormatter implements IAxisValueFormatter {
    private final Date mDate;
    private final SimpleDateFormat mDateFormat;
    private final Float mReferenceTime;

    public XAxisDateValueFormatter(String dateFormat, Float referenceTime) {
        mDateFormat = new SimpleDateFormat("", Locale.getDefault());
        mDate = new Date();
        mReferenceTime = referenceTime;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        mDate.setTime((long) (value + mReferenceTime));
        return mDateFormat.format(mDate);
    }
}