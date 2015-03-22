package com.jfsanda.sunshine;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jfsanda.sunshine.data.WeatherContract;
import com.jfsanda.sunshine.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            getSupportActionBar().setElevation(0f);
            mTwoPane = false;
        }
        ForecastFragment fm = (ForecastFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        fm.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }else if(id == R.id.action_map) {
            showMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMap() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String loc = preferences.getString(getString(R.string.pref_postalCode_key), getString(R.string.pref_postalCodeDefault));

        Uri geo = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", loc).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geo);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Map intent not available");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
            }
        }
        mLocation = location;
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI,dateUri);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.weather_detail_container,df,DETAILFRAGMENT_TAG).
                    commit();
        }else{
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }
}
