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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //@formatter:off
    @State public int LOADER_ID;
    @State public int dataColumnPosition;
    @State public String fragmentDataType;
    @State public String dateFormat;
    @State public Uri stockUri;
    @State public String historyData;
    @State public String fragmentTitle;
    @State public boolean isChartDescriptionAnnounced = false;
    @BindView(R.id.chart) public LineChart linechart;
    @BindColor(R.color.white) public int dataColor;
    //@formatter:on
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
            dataColumnPosition = Contract.Quote.POSITION_MONTH_HISTORY;
            dateFormat = "MMM";
            LOADER_ID = 100;
        } else if (fragmentDataType.equals(getString(R.string.WEEKLY))) {
            dataColumnPosition = Contract.Quote.POSITION_WEEK_HISTORY;
            dateFormat = "dd";
            LOADER_ID = 200;
        } else if (fragmentDataType.equals(getString(R.string.DAILY))) {
            dataColumnPosition = Contract.Quote.POSITION_DAY_HISTORY;
            dateFormat = "dd";
            LOADER_ID = 300;
        } else {
            dataColumnPosition = Contract.Quote.POSITION_YEAR_HISTORY;
            dateFormat = "MMM";
            LOADER_ID = 400;
        }
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
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private Entry getSecondToLastData(List<Entry> dataPairs) {
        if (dataPairs.size() > 1) {
            return dataPairs.get(dataPairs.size() - 2);
        } else if (dataPairs.size() > 0) {
            return dataPairs.get(dataPairs.size() - 1);
        } else {
            return null;
        }
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
        List<Entry> dataPairs = result.second;
        Float referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(dataColor);
        dataSet.setLineWidth(3f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(dataColor);
        dataSet.setCircleRadius(4f);
        dataSet.setHighLightColor(dataColor);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
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

        //disable all interactions with the graph
        linechart.setDragEnabled(false);
        linechart.setScaleEnabled(false);
        linechart.setDragDecelerationEnabled(false);
        linechart.setPinchZoom(false);
        linechart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        linechart.setDescription(description);
        linechart.setExtraOffsets(10, 0, 0, 10);
        linechart.animateX(1000, Easing.EasingOption.Linear);
    }

    private void setupAxisBase(AxisBase axisBase) {
        axisBase.setDrawGridLines(false);
        axisBase.setAxisLineColor(dataColor);
        axisBase.setAxisLineWidth(1.5f);
        axisBase.setTextColor(dataColor);
        axisBase.setTextSize(14f);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //No action needed
    }
}

