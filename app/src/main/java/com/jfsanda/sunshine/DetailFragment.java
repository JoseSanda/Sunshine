package com.jfsanda.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jfsanda.sunshine.data.WeatherContract;

/**
     * A placeholder fragment containing a simple view.
     */
    public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String SUNSHINE_APP = "#sunshineApp";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI_Detalles";
    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private int DETAIL_LOADER_ID = 0;
    private ImageView iconView;
    private TextView dateTextView;
    private TextView lowTextView;
    private TextView dayTextView;
    private TextView highTextView;
    private TextView humTextView;
    private  TextView windTextView;
    private TextView pressTextView;
    private  TextView forecastTextView;
    private Uri mUri;

    public static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WIND_SPEED = 5;
    static final int COL_PRESSURE = 6;
    static final int COL_HUMIDITY = 7;
    static final int COL_DEGREES = 8;
    static final int COL_WEATHER = 9;


    public DetailFragment() {
            setHasOptionsMenu(true);
        }

    public Loader onCreateLoader(int id, Bundle args) {
       if(null != mUri) {
           return new CursorLoader(
                   getActivity(),
                   mUri,
                   DETAIL_COLUMNS,
                   null,
                   null,
                   null
           );
       }
       return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            int weatherId = data.getInt(COL_WEATHER);
            Long dateLong = data.getLong(COL_WEATHER_DATE);

            String dateString = Utility.formatDate(
                    dateLong);
            String weatherDescription =
                    data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String high = Utility.formatTemperature(this.getActivity(),
                    data.getDouble(COL_WEATHER_MAX_TEMP));
            String low = Utility.formatTemperature(this.getActivity(),
                    data.getDouble(COL_WEATHER_MIN_TEMP));
            Float humidity = data.getFloat(COL_HUMIDITY);
            Float pressure = data.getFloat(COL_PRESSURE);
            Float degrees = data.getFloat(COL_DEGREES);
            String wind = Utility.getFormattedWind(this.getActivity(),
                    data.getFloat(COL_WIND_SPEED),degrees );

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            dayTextView.setText(Utility.getDayName(this.getActivity(),dateLong));
            dateTextView.setText(Utility.getFormattedMonthDay(this.getActivity(), dateLong));
            highTextView.setText(high);
            lowTextView.setText(low);
            humTextView.setText(String.format(this.getString(R.string.format_humidity), humidity));
            windTextView.setText(wind);
            pressTextView.setText(String.format(this.getString(R.string.format_pressure), pressure));
            forecastTextView.setText(weatherDescription);
            iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            //iconView.setContentDescription(weatherDescription);


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle args = getArguments();
            if(args != null){
                mUri = args.getParcelable(DETAIL_URI);
            }

            View view = inflater.inflate(R.layout.item_detail, container, false);

            dayTextView = (TextView)view.findViewById(R.id.item_day_textView);
            dateTextView = (TextView)view.findViewById(R.id.item_date_textView);
            highTextView = (TextView)view.findViewById(R.id.item_high_temp_textView);
            lowTextView = (TextView)view.findViewById(R.id.item_low_temp_textView);
            humTextView = (TextView)view.findViewById(R.id.item_humidity_textView);
            windTextView = (TextView)view.findViewById(R.id.item_wind_textView);
            pressTextView = (TextView)view.findViewById(R.id.item_pressure_textView);
            forecastTextView = (TextView)view.findViewById(R.id.item_forecast_textView);
            iconView = (ImageView)view.findViewById(R.id.item_icon);

            return view;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_sharing);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if (mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + SUNSHINE_APP);
            return shareIntent;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_sharing && mShareActionProvider != null) {
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        void onLocationChanged( String newLocation ) {
            // replace the uri, since the location has changed
            Uri uri = mUri;
            if (null != uri) {
                long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
                Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
            }
        }


    }