package com.udacity.stockhawk.ui;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomBundler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.udacity.stockhawk.R.id.change;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int LOADER_ID = 1;
    private static final int PAGE_LIMIT = 2;
    //@formatter:off
    private Uri stockUri;
    @State(CustomBundler.class) public Map<Integer, String> fragmentTags = new HashMap<>();
    @State public boolean isDataLoaded = false;
    @BindView(R.id.toolbar) public Toolbar toolbar;
    @BindView(R.id.viewpager) public ViewPager viewPager;
    @BindView(R.id.tabs) public TabLayout tabLayout;
    @BindView(R.id.symbol) public TextView symbolTextView;
    @BindView(R.id.exchange) public TextView stockExchangeTextView;
    @BindView(R.id.price) public TextView priceTextView;
    @BindView(R.id.change) public TextView changeTextView;
    //@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stockUri = getIntent().getData();
        addViewPager();
        tabLayout.setupWithViewPager(viewPager, true);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void addViewPagerAdapterFragment(ViewPagerAdapter adapter, String key, String title) {
        Bundle bundle = new Bundle();
        DetailFragment detailFragment = new DetailFragment();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), key);
        detailFragment.setArguments(bundle);
        adapter.addFragment(detailFragment, title);
    }

    private void addViewPager() {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        addViewPagerAdapterFragment(adapter, getString(R.string.DAILY), getString(R.string.days_fragment_title));
        addViewPagerAdapterFragment(adapter, getString(R.string.WEEKLY), getString(R.string.weeks_fragment_title));
        addViewPagerAdapterFragment(adapter, getString(R.string.MONTHLY), getString(R.string.months_fragment_title));
        addViewPagerAdapterFragment(adapter, getString(R.string.YEARLY), getString(R.string.year_fragment_title));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    this,
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
        if (data.moveToFirst()) {
            String stockExchange = "";//data.getString(Contract.Quote.POSITION_EXCHANGE);
            String stockName = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.detail_activity_cd), stockName));

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);

            stockExchangeTextView.setText(stockExchange);
            symbolTextView.setText(stockName);
            priceTextView.setText(dollarFormat.format(stockPrice));
            priceTextView.setContentDescription(String.format(getString(R.string.stock_price_cd), priceTextView.getText()));
            changeTextView.setText(dollarFormat.format(absoluteChange));
            if (change > 0) {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
                changeTextView.setContentDescription(
                        String.format(getString(R.string.stock_increment_cd), changeTextView.getText()));
            } else {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
                changeTextView.setContentDescription(
                        String.format(getString(R.string.stock_decrement_cd), changeTextView.getText()));
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        isDataLoaded = false;
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {

            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentList.get(position);
        }

        @Override
        public int getCount() {

            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}