package com.jfsanda.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, new DetailFragment());
            transaction.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        public static final String SUNSHINE_APP = "#sunshineApp";
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private ShareActionProvider mShareActionProvider;
        private String mForecast;
        private int DETAIL_LOADER_ID = 0;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            return new CursorLoader(getActivity(),intent.getData(),ForecastFragment.FORECAST_COLUMNS,null,null,null);
        }

        private String getText(Cursor cursor){
            return cursor.getString(ForecastFragment.COL_LOCATION_SETTING)+
                    cursor.getString(ForecastFragment.COL_WEATHER_DATE)+
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP)+"/"+
                    cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = Utility.formatDate(
                    data.getLong(ForecastFragment.COL_WEATHER_DATE));

            String weatherDescription =
                    data.getString(ForecastFragment.COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(
                    data.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            TextView detailTextView = (TextView)getView().findViewById(R.id.textView);
            detailTextView.setText(mForecast);
            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
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
    }
}
