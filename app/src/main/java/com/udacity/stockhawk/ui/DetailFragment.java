package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Parser;
import com.udacity.stockhawk.utils.XAxisDateValueFormatter;
import com.udacity.stockhawk.utils.YAxisPriceValueFormatter;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @State public int loaderId;
    @State public int dataColumnPosition;
    @State public String fragmentDataType;
    @State public String dateFormat;
    @State public Uri stockUri;
    @State public String historyData;
    @State public String fragmentTitle;
    @State public boolean isChartDescriptionAnnounced = false;
    @BindView(R.id.chart) public LineChart linechart;
    @BindColor(R.color.white) public int dataColor;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if (savedInstanceState == null) {
            setupInitialState();
        }
    }

    private void setupInitialState() {
        fragmentDataType = getArguments().getString(getString(R.string.FRAGMENT_DATA_TYPE_KEY));
        if (fragmentDataType.equals(getString(R.string.MONTHLY))) {
            setState(Contract.Quote.POSITION_MONTH_HISTORY, "MMM", 1);
        } else if (fragmentDataType.equals(getString(R.string.WEEKLY))) {
            setState(Contract.Quote.POSITION_WEEK_HISTORY, "dd", 2);
        } else if (fragmentDataType.equals(getString(R.string.DAILY))) {
            setState(Contract.Quote.POSITION_DAY_HISTORY, "dd", 3);
        } else {
            setState(Contract.Quote.POSITION_YEAR_HISTORY, "MMM", 4);
        }
    }

    private void setState(int dataColumnPosition, String dateFormat, int loaderId) {
        this.dataColumnPosition = dataColumnPosition;
                this.dateFormat = dateFormat;
        this.loaderId = loaderId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        if (historyData != null) {
            setUpLineChart();
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stockUri = getActivity().getIntent().getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(loaderId, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    mContext,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst() && historyData == null) {
            historyData = data.getString(dataColumnPosition);
            setUpLineChart();
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    private void setUpLineChart() {
        Pair<Float, List<Entry>> result = Parser.getFormattedStockHistory(historyData);
        Float referenceTime = result.first;

        LineData lineData = new LineData(setupLineDataSet(result.second));
        linechart.setData(lineData);

        XAxis xAxis = linechart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateValueFormatter(dateFormat, referenceTime));
        setupAxisBase(xAxis);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = linechart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = linechart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceValueFormatter());
        setupAxisBase(yAxis);

        Legend legend = linechart.getLegend();
        legend.setEnabled(false);

        setLinechartValues();
    }

    private LineDataSet setupLineDataSet(List<Entry> dataPairs) {
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(dataColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(dataColor);
        dataSet.setCircleRadius(4f);
        dataSet.setHighLightColor(dataColor);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    private void setupAxisBase(AxisBase axisBase) {
        axisBase.setDrawGridLines(false);
        axisBase.setAxisLineColor(dataColor);
        axisBase.setAxisLineWidth(1.5f);
        axisBase.setTextColor(dataColor);
        axisBase.setTextSize(14f);
    }

    private void setLinechartValues() {
        linechart.setDragEnabled(false);
        linechart.setScaleEnabled(false);
        linechart.setDragDecelerationEnabled(false);
        linechart.setPinchZoom(false);
        linechart.setDoubleTapToZoomEnabled(false);
        linechart.setExtraOffsets(10, 0, 0, 10);
        linechart.animateX(1000, Easing.EasingOption.Linear);
        Description description = new Description();
        description.setText(" ");
        linechart.setDescription(description);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //No action needed
    }
}

