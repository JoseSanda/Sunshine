package com.jfsanda.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jfsanda.sunshine.data.WeatherContract;

import java.util.Date;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean useTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        this.useTodayLayout = useTodayLayout;
    }


    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(this.mContext, high) + "/" + Utility.formatTemperature(this.mContext, low);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = ForecastFragment.COL_WEATHER_MAX_TEMP;
        int idx_min_temp = ForecastFragment.COL_WEATHER_MIN_TEMP;
        int idx_date = ForecastFragment.COL_WEATHER_DATE;
        int idx_short_desc = ForecastFragment.COL_WEATHER_DESC;

        String highAndLow = formatHighLows(
                cursor.getDouble(idx_max_temp),
                cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_short_desc) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.file_item_forecast;
        if (viewType == VIEW_TYPE_TODAY){
            layoutId  = R.layout.list_item_forecast_today;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public int getViewTypeCount() {
        //Tenemos dos tipos diferentes de layouts: el de hoy y el del resto de dias
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout)?VIEW_TYPE_TODAY:VIEW_TYPE_FUTURE_DAY;
    }

    /*
                This is where we fill-in the views with the contents of the cursor.
             */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

       ViewHolder viewHolder = (ViewHolder)view.getTag();

       Integer weatherId = cursor.getInt(ForecastFragment.COL_WEATHER);
       int resource = Utility.getIconResourceForWeatherCondition(weatherId);
       if(getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
           resource = Utility.getArtResourceForWeatherCondition(weatherId);
       }
        viewHolder.icon.setImageResource(resource);
        // Read date from cursor
        Long dateLong = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String date = Utility.getFriendlyDayString(context,dateLong);
        viewHolder.dateTextView.setText(date);

        // Read weather forecast from cursor
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.forecastTextView.setText(forecast);
        //viewHolder.icon.setContentDescription(forecast);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highView.setText(Utility.formatTemperature(context, high));

        // Read low temperature from cursor
        double lowTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowView.setText(Utility.formatTemperature(context, lowTemp));

    }
}