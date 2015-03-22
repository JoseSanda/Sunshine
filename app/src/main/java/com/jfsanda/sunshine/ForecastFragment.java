package com.jfsanda.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.jfsanda.sunshine.data.WeatherContract;
import com.jfsanda.sunshine.sync.SunshineSyncAdapter;

/**
     * A placeholder fragment containing a simple view.
     */
    public class ForecastFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>{

    public static final int LOADER_ID = 0;
    public static final String[] FORECAST_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_WEATHER = 9;
    private static final  String SELECTED_KEY = "SELECTED";
    private ListView mListView;


    private ForecastAdapter adapter;
    private SharedPreferences preferences;
    private int mposition;
    private Boolean useTodayLayout;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }



    public ForecastFragment() {
    }

    public void setUseTodayLayout(boolean useTodayLayout){
        this.useTodayLayout = useTodayLayout;
        if(adapter!=null){
            adapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        adapter.swapCursor(data);
        if(mposition != ListView.INVALID_POSITION){
           mListView.setSelection(mposition);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mposition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mposition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        CursorLoader loader = (CursorLoader) getLoaderManager().initLoader(LOADER_ID,null,this);
        Cursor cursor = loader.loadInBackground();
        adapter = new ForecastAdapter(getActivity(),cursor,0);
        if(useTodayLayout!=null) {
            adapter.setUseTodayLayout(useTodayLayout);
        }

        mListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        // CursorAdapter returns a cursor at the correct position for getItem(), or null
                        // if it cannot seek to that position.
                        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                        if (cursor != null) {
                            String locationSetting = Utility.getPreferredLocation(getActivity());
                            ((Callback)getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                        }
                        mposition = position;
                    }
                }
        );
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mposition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                updateWeather();
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        /*
        String location = Utility.getPreferredLocation(getActivity());

        Intent alarmIntent = new Intent(getActivity(),SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.PARAMS,location);

        PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),+5000,pIntent);
*/
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID,null,this);
    }
}