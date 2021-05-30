package com.mak.jawa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.androdocs.httprequest.HttpRequest;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String API = "98ed13acba37b8f64a391fd3a3748865";
    private static final int REQUEST_LOCATION = 1;
    private GpsTracker gpsTracker;

    ImageView imSearchBtn;
    EditText etSearchBox;
    ProgressBar pbLoading;
    String searchQuery, latitude, longitude, cityName, countryName,updatedAtText,temperature,forecast,
            humidity, minTemperature, maxTemperature, pressure, windSpeed, sunrises,sunsets;
    TextView tvCityName, tvCountryName, tvLastUpdatedTime, tvTemperature, tvForecast, tvHumidity,
            tvMinTemperature, tvMaxTemperature, tvSunrises, tvSunsets, tvPressure, tvWindSpeed;
    Long updatedAt,rise,set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        {
            etSearchBox = (EditText) findViewById(R.id.edit_search_box);
            imSearchBtn = (ImageView) findViewById(R.id.image_search);
            pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
            tvCityName = (TextView) findViewById(R.id.text_city_name);
            tvCountryName = (TextView) findViewById(R.id.text_country_name);
            tvLastUpdatedTime = (TextView) findViewById(R.id.text_last_updated_time);
            tvTemperature = (TextView) findViewById(R.id.text_temperature);
            tvForecast = (TextView) findViewById(R.id.text_forecast);
            tvHumidity = (TextView) findViewById(R.id.text_humidity);
            tvMinTemperature = (TextView) findViewById(R.id.text_min_temp);
            tvMaxTemperature = (TextView) findViewById(R.id.text_max_temp);
            tvSunrises = (TextView) findViewById(R.id.text_sunrise);
            tvSunsets = (TextView) findViewById(R.id.text_sunset);
            tvPressure = (TextView) findViewById(R.id.text_pressure);
            tvWindSpeed = (TextView) findViewById(R.id.text_wind_speed);

            etSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        //hide keyboard
                        hideVirtualKeyboard();
                        getWeatherData();
                    }
                    return false;
                }
            });

            imSearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideVirtualKeyboard();
                    getWeatherData();

                }
            });
        }

        if (isNetworkAvailable())
        {
            if (checkGpsStatus())
            {
                pbLoading.setVisibility(ProgressBar.VISIBLE);
                Map<String, String> coordinates = getLocation();
                if (!coordinates.isEmpty())
                {
                    String latitude = coordinates.get("latitude");
                    String longitude = coordinates.get("longitude");
                    new weatherTask("coords").execute(latitude,longitude);
                }
            }
        }
        else
        {
            View view = findViewById(R.id.main_layout);
            Snackbar snackbar = Snackbar
                    .make(view, "Internet Disconnected Please Check Your connection", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }


    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
    private boolean isNetworkAvailable() {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            isConnected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return isConnected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return isConnected;
    }
    private void hideVirtualKeyboard()
    {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    private void getWeatherData()
    {
        searchQuery = etSearchBox.getText().toString();
        if (!searchQuery.isEmpty()) {
            pbLoading.setVisibility(ProgressBar.VISIBLE);
            new weatherTask("textbox").execute();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please Enter a Location to search ", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkGpsStatus()
    {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setMessage("Gps is turned Off Please turn it on");
            alertDialogBuilder.setPositiveButton("Turn On",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            enableLocationSettings();
                        }
                    });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        else {
            return true;

        }
    }
    public Map<String, String> getLocation(){
        Map<String, String> coordinates = new HashMap<>();

        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double lati = gpsTracker.getLatitude();
            double longi = gpsTracker.getLongitude();
            String latitude = String.valueOf(lati);
            String longitude = String.valueOf(longi);
            coordinates.put("longitude", longitude);
            coordinates.put("latitude", latitude);
        }else{
            gpsTracker.showSettingsAlert();
        }
        return coordinates;
    }


    class weatherTask extends AsyncTask<String, Void, String>{
        private String call_type= "";
        public weatherTask(String call_type) {
            this.call_type = call_type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            if (this.call_type == "coords") {
                String lat = args[0];
                String lon = args[1];
                String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=metric&appid="+ API);
                Log.i("MAK", "doInBackground: "+response);
                return response;
            } else {
                String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + searchQuery + "&units=metric&appid=" + API);
                Log.i("MAK", "doInBackground: "+response);
                return response;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObj = new JSONObject(result);
                int responseCode = (int) jsonObj.getInt("cod");
                if (responseCode == 200 ) {
                    JSONObject main = jsonObj.getJSONObject("main");
                    JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                    JSONObject wind = jsonObj.getJSONObject("wind");
                    JSONObject sys = jsonObj.getJSONObject("sys");
                    cityName = jsonObj.getString("name");
                    countryName = sys.getString("country");
                    updatedAt = jsonObj.getLong("dt");
                    updatedAtText = "Last Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                    temperature = main.getString("temp");
                    forecast = weather.getString("description");
                    humidity = main.getString("humidity");
                    minTemperature = main.getString("temp_min");
                    maxTemperature = main.getString("temp_max");
                    pressure = main.getString("pressure");
                    windSpeed = wind.getString("speed");
                    rise = sys.getLong("sunrise");
                    sunrises = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(rise * 1000));
                    set = sys.getLong("sunset");
                    sunsets = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(set * 1000));
                    // SET ALL VALUES IN TEXTBOX :
                    tvCityName.setText(cityName);
                    tvCountryName.setText(countryName);
                    tvLastUpdatedTime.setText(updatedAtText);
                    tvTemperature.setText(temperature + "°C");
                    tvForecast.setText(forecast);
                    tvHumidity.setText(humidity);
                    tvMinTemperature.setText(minTemperature);
                    tvMaxTemperature.setText(maxTemperature);
                    tvSunrises.setText(sunrises);
                    tvSunsets.setText(sunsets);
                    tvPressure.setText(pressure);
                    tvWindSpeed.setText(windSpeed);
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                }
                else if (responseCode == 404 )
                {
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                }

            } catch (Exception e) {
                pbLoading.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(MainActivity.this, "Error:" + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
