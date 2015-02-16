package com.jfsanda.sunshine;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
     * A placeholder fragment containing a simple view.
     */
    public class ForecastFragment extends Fragment {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private ArrayAdapter<String> adapter;

        private SharedPreferences preferences;

    public ForecastFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            ArrayList<String> forecastArray = new ArrayList();
            adapter = new ArrayAdapter<String>(
                    getActivity(),R.layout.file_item_forecast,R.id.list_item_forecast_textView,forecastArray);
            ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String item = (String) parent.getItemAtPosition(position);
                            if(item != null) {
                                Intent intent = new Intent(getActivity(), DetailActivity.class);
                                intent.putExtra(Intent.EXTRA_TEXT,item);
                                startActivity(intent);
                            }
                        }
                    }
            );

            return rootView;
        }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                updateWeather();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute(
                preferences.getString(getString(R.string.pref_postalCode_key), getString(R.string.pref_postalCodeDefault)),
                null,
                preferences.getString(getString(R.string.pref_metrics_key), getString(R.string.pref_metrics_default)),
                null, null
        );
    }


    class FetchWeatherTask extends AsyncTask<String,Void,String[]> {

            private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
            private final String FORECAST_SERVICE_URL = getString(R.string.url_forecast_service);
            private final String COUNTRY_PARAM = getString(R.string.country_param);
            private final String MODE_PARAM = getString(R.string.mode_param);
            private final String UNITS_PARAM = getString(R.string.units_param);
            private final String LANG_PARAM = getString(R.string.lang_param);
            private final String COUNT_PARAM = getString(R.string.count_param);


        @Override
        protected void onPostExecute(String[] strings) {
            try {
                if(strings != null) {
                    adapter.clear();
                    adapter.addAll(Arrays.asList(strings));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error fetching forecast", e);
            }
        }

        @Override
            protected String[] doInBackground(String... params) {
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String forecastJsonStr = null;
                int i = 0;
                String country = getParam(i++,getString(R.string.default_postal_code),params);
                String mode = getParam(i++,getString(R.string.default_format_mode),params);
                String units = getParam(i++,getString(R.string.default_metric),params);
                String lang = getParam(i++,getString(R.string.default_lang),params);
                String numDays = getParam(i++,getString(R.string.default_forecast_days),params);

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are available at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    Uri.Builder uriBuilder = new Uri.Builder();
                    uriBuilder.scheme("http");
                    uriBuilder.encodedPath(FORECAST_SERVICE_URL);
                    uriBuilder.appendQueryParameter(COUNTRY_PARAM, country);
                    uriBuilder.appendQueryParameter(MODE_PARAM,mode);
                    uriBuilder.appendQueryParameter(UNITS_PARAM,units);
                    uriBuilder.appendQueryParameter(LANG_PARAM,lang);

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) new URL(uriBuilder.build().toString()).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                    String [] result = WeatherDataParser.getWeatherDataFromJson(forecastJsonStr,Integer.valueOf(numDays));
                    return result;
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error of IO", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    return null;
                } catch (JSONException e) {
                    Log.e(LOG_TAG,"Error transforming JSON",e);
                    return null;
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("ForecastFragment", "Error closing stream", e);
                        }
                    }
                }
            }
        }

        private String getParam(int i,String defecto, String... params){
            if(params != null && params.length > i && params[i]!=null && !"".equals(params[i])){
                return params[i];
            }
            return defecto;
        }


    }