package com.udacity.stockhawk.widget;

/**
 * Created by jkbreunig on 3/1/17.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockUtils;

import timber.log.Timber;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockPriceRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                Timber.d("onCreate");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {

                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item_quote);

                String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                String price = StockUtils.getFormattedPriceValue(data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE)));

                float rawAbsoluteChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));

                String changeValue;
                if (PrefUtils.getDisplayMode(getBaseContext())
                        .equals(getBaseContext().getString(R.string.pref_display_mode_absolute_key))) {
                    changeValue = StockUtils.getFormattedAbsoluteChangeValue(rawAbsoluteChange);
                } else {
                    changeValue = StockUtils.getFormattedPercentageChangeValue(data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)));
                }

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, price);
                views.setTextViewText(R.id.change, changeValue);
                views.setInt(R.id.change, "setBackgroundResource",
                        StockUtils.getChangeBackgroundResource(rawAbsoluteChange));

                setStockOnClickIntent(views, symbol);
                return views;
            }

            private void setStockOnClickIntent(RemoteViews views, String symbol) {
                final Intent intent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                intent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list_item_quote, intent);
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.price, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {

                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(Contract.Quote.POSITION_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {

                return true;
            }
        };
    }
}