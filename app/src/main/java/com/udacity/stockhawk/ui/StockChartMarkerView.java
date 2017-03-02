package com.udacity.stockhawk.ui;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jkbreunig on 3/2/17.
 */

public class StockChartMarkerView extends MarkerView {

    private final TextView mTextView;
    private final Entry mFinalEntry;
    private final Date mDate;
    private final Float mReferenceTime;
    private final SimpleDateFormat mDateFormat;
    private final DecimalFormat mDollarFormat;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context        this is need to fetch string resources.
     * @param layoutResource the layout resource to use for the MarkerView.
     * @param referenceTime  value relative to each data value. This is needed to normalize the values back.
     */
    public StockChartMarkerView(Context context, int layoutResource, Entry finalEntry, Float referenceTime) {
        super(context, layoutResource);
        mTextView = (TextView) findViewById(R.id.marker_text);
        mFinalEntry = finalEntry;
        mReferenceTime = referenceTime;
        mDateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        mDate = new Date();
        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        super.refreshContent(entry, highlight);
        Float stockValue = entry.getY();
        mDate.setTime((long) (entry.getX() + mReferenceTime));
        String formattedDate = mDateFormat.format(mDate);
        mTextView.setText(String.format(getContext().getString(R.string.marker_text), mDollarFormat.format(stockValue), formattedDate));
    }
}