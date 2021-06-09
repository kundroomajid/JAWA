package com.mak.jawa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity {

    private final String API_KEY = "5624ffafab761ff66e0136b356470496";
    private final String SHARED_PREF_FILE = "com.mak.jawa.preferences";
    private static final int REQUEST_LOCATION = 1;
    private GpsTracker gpsTracker;

    ImageView imSearchBtn, imForecastIcon;
    EditText etSearchBox;
    ProgressBar pbLoading;
    String searchQuery, latitude, longitude, cityName, countryName,updatedAtText,temperature,forecast,
            humidity, realFeel, pressure, windSpeed, sunrises,sunsets, forecastIcon ;
    TextView tvCityName, tvCountryName, tvLastUpdatedTime, tvTemperature, tvForecast, tvHumidity,
            tvRealFeel, tvSunrises, tvSunsets, tvPressure, tvWindSpeed;
    Long updatedAt,rise,set;

    Map<String, String> forecastIcons = new HashMap<>();
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
            tvRealFeel = (TextView) findViewById(R.id.text_real_feel);
            tvSunrises = (TextView) findViewById(R.id.text_sunrise);
            tvSunsets = (TextView) findViewById(R.id.text_sunset);
            tvPressure = (TextView) findViewById(R.id.text_pressure);
            tvWindSpeed = (TextView) findViewById(R.id.text_wind_speed);
            imForecastIcon = (ImageView) findViewById(R.id.image_forecast);

            etSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
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

        getWeatherOnLocation();
    }

    private void getWeatherOnLocation()
    {
        if (isNetworkAvailable())
        {
            toggleProgressStatus();
            Map<String, String> coordinates = getLocation();
            if (!coordinates.isEmpty())
            {
                String latitude = coordinates.get("latitude");
                String longitude = coordinates.get("longitude");
                new weatherTask("coords").execute(latitude,longitude);
            }
            else
            {
                toggleProgressStatus();
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
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("temp",tvTemperature.getText().toString());
        editor.putString("city",tvCityName.getText().toString());
        editor.putString("country",tvCountryName.getText().toString());
        editor.putString("lastUpdatedTime",tvLastUpdatedTime.getText().toString());
        editor.putString("forecast",tvForecast.getText().toString());
        editor.putString("humidity",tvHumidity.getText().toString());
        editor.putString("realFeel",tvRealFeel.getText().toString());
        editor.putString("sunrises",tvSunrises.getText().toString());
        editor.putString("sunsets",tvSunsets.getText().toString());
        editor.putString("pressure",tvPressure.getText().toString());
        editor.putString("windSpeed",tvWindSpeed.getText().toString());
        editor.putString("forecastIcon",forecastIcon);
        if(updatedAt != null)
        {
            editor.putLong("lastUpdatedAt",updatedAt);
        }
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        //get required data from shared preferences
        String temp  = sharedPreferences.getString("temp", "");
        String city  = sharedPreferences.getString("city", "");
        String country  = sharedPreferences.getString("country", "");
        String lastUpdatedTime = sharedPreferences.getString("lastUpdatedTime", "");
        String forecast = sharedPreferences.getString("forecast", "");
        String humidity = sharedPreferences.getString("humidity", "");
        String realFeel = sharedPreferences.getString("realFeel", "");
        String sunrises = sharedPreferences.getString("sunrises", "");
        String sunsets = sharedPreferences.getString("sunsets", "");
        String pressure = sharedPreferences.getString("pressure", "");
        String windSpeed = sharedPreferences.getString("windSpeed", "");
        String forecastIcon = sharedPreferences.getString("forecastIcon","");
        //set data in textviews
       tvTemperature.setText(temp);
       tvCityName.setText(city);
       tvCountryName.setText(country);
       tvLastUpdatedTime.setText(lastUpdatedTime);
       tvForecast.setText(forecast);
       tvHumidity.setText(humidity);
       tvRealFeel.setText(realFeel);
       tvSunrises.setText(sunrises);
       tvSunsets.setText(sunsets);
       tvPressure.setText(pressure);
       tvWindSpeed.setText(windSpeed);
       if (forecast != null) {
           setForecastIcon(forecastIcon, imForecastIcon);
       }


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

    public void toggleProgressStatus()
    {
        try {
            int visibiltyStatus = pbLoading.getVisibility();
            if(visibiltyStatus == View.VISIBLE)
            {
                pbLoading.setVisibility(View.GONE);
            }
            else
            {
                pbLoading.setVisibility(View.VISIBLE);
            }
        }
        catch (NullPointerException e)
        {
            Log.i(TAG, "toggleProgressStatus: "+e.getLocalizedMessage());
        }


    }

    private void getWeatherData()
    {
        if(isNetworkAvailable())
        {
            searchQuery = etSearchBox.getText().toString();
            if (!searchQuery.isEmpty()) {
                toggleProgressStatus();
                new weatherTask("textbox").execute();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please Enter a Location to search ", Toast.LENGTH_SHORT).show();
            }
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
    private void setForecastIcon(String forecastIcon,ImageView imForecastIcon)
    {
//            https://openweathermap.org/weather-conditions
        forecastIcons.put("01d","ic_one_day");
        forecastIcons.put("01n","ic_one_night");
        forecastIcons.put("02d","ic_two_day");
        forecastIcons.put("02n","ic_two_night");
        forecastIcons.put("03d","ic_three_day");
        forecastIcons.put("03n","ic_three_night");
        forecastIcons.put("04d","ic_four_day");
        forecastIcons.put("04n","ic_four_night");
        forecastIcons.put("09d","ic_nine_day");
        forecastIcons.put("09n","ic_nine_night");
        forecastIcons.put("10d","ic_ten_day");
        forecastIcons.put("10n","ic_ten_night");
        forecastIcons.put("11d","ic_eleven_day");
        forecastIcons.put("11n","ic_eleven_night");
        forecastIcons.put("13d","ic_thirteen_day");
        forecastIcons.put("13n","ic_thirteen_night");
        forecastIcons.put("50d","ic_fifty_day");
        forecastIcons.put("50n","ic_fifty_night");
        String iconFile = forecastIcons.get(forecastIcon);
        if(iconFile != null) {
            if (!iconFile.isEmpty()) {
                String PACKAGE_NAME = getApplicationContext().getPackageName();
                int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + iconFile, null, null);
                imForecastIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));
            } else {
                imForecastIcon.setImageResource(R.drawable.ic_pressure);
            }

        }

    }

    private class weatherTask extends AsyncTask<String, Void, String>{
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
                String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units=metric&appid="+ API_KEY);
                return response;
            } else {
                String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + searchQuery + "&units=metric&appid=" + API_KEY);
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
                    if(!cityName.equals("Globe")) {
                        countryName = sys.getString("country");
                        updatedAt = jsonObj.getLong("dt");
                        updatedAtText = "Last Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                        temperature = main.getString("temp");
                        forecastIcon = weather.getString("icon");
                        forecast = weather.getString("description");
                        humidity = main.getString("humidity");
                        realFeel = main.getString("feels_like");
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
                        tvRealFeel.setText(realFeel);
                        tvSunrises.setText(sunrises);
                        tvSunsets.setText(sunsets);
                        tvPressure.setText(pressure);
                        tvWindSpeed.setText(windSpeed);
                        //set icon here
                        setForecastIcon(forecastIcon, imForecastIcon);
                        toggleProgressStatus();
                    }
                    else
                    {
                        toggleProgressStatus();
                        Toast.makeText(MainActivity.this, "Unable to detect Your Location please use search instead ", Toast.LENGTH_LONG).show();
                    }
                }
                else if (responseCode == 404 )
                {
                    toggleProgressStatus();
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();
                    toggleProgressStatus();
                }

            } catch (Exception e) {
                toggleProgressStatus();
                Toast.makeText(MainActivity.this, "Error:" + e.toString(), Toast.LENGTH_LONG).show();
            }
        }


    }
}
