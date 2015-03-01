package com.jfsanda.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String FORECASTFRAGMENT_TAG = "FORECAST_FRAGMENT";
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_postalCode_key), getString(R.string.pref_postalCodeDefault));
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(),FORECASTFRAGMENT_TAG)
                    .commit();
        }
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
        String preferredLocation = Utility.getPreferredLocation(this);
        if(preferredLocation != mLocation){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            ff.onLocationChanged();
            mLocation = preferredLocation;
        }
    }
}
