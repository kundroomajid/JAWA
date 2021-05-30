package com.mak.jawa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
        if(isNetworkAvailable())
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
                    System.out.println("Icon :"+forecastIcon);
                    setForecastIcon(forecastIcon,imForecastIcon);
//                    imForecastIcon.setImageResource(res);
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
            if(!iconFile.isEmpty())
            {
                String PACKAGE_NAME = getApplicationContext().getPackageName();
                int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+iconFile , null, null);
                imForecastIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));
            }
            else {
                imForecastIcon.setImageResource(R.drawable.ic_pressure);
            }



        }
    }
}
