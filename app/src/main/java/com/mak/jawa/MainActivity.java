package com.mak.jawa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mak.jawa.models.CurrentWeather;
import com.mak.jawa.models.ForecastIcons;
import com.mak.jawa.models.ForecastWeatherModel;
import com.mak.jawa.models.List;
import com.mak.jawa.models.Weather;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int REQUEST_CHECK_CODE = 3636;
    private final String SHARED_PREF_FILE = "com.mak.jawa.preferences";
    public static String PACKAGE_NAME = "";
    private String API_KEY = "";
    public static Context mContext;

    RecyclerView recyclerView;
    ApiInterface apiInterface;

    ImageView imSearchBtn, imForecastIcon;
    EditText etSearchBox;
    ProgressBar pbLoading;
    double temperature;
    String searchQuery, cityName, countryName, updatedAtText,forecast,
            humidity,realFeel,pressure, windSpeed, sunrises, sunsets, forecastIcon, textDay;
    TextView tvCityName, tvLastUpdatedTime, tvTemperature, tvForecast, tvHumidity,tvTextDay,
            tvRealFeel, tvSunrises, tvSunsets, tvPressure, tvWindSpeed;
    Long updatedAt, rise, set;

    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    ForecastIcons forecastIcons;

    private double currLatitude = 0.0;
    private double currLongitude = 0.0;
    private boolean isLocAvalaible = false;
    public java.util.List<List> forecastWeatherModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        mContext = this;
        API_KEY = getString(R.string.openweather_api_key);
        Log.i("key", "onCreate: "+API_KEY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        forecastIcons = new ForecastIcons();
        setContentView(R.layout.activity_main);
        init();
        {
            etSearchBox = (EditText) findViewById(R.id.edit_search_box);
            imSearchBtn = (ImageView) findViewById(R.id.image_search);
            pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
            tvCityName = (TextView) findViewById(R.id.text_city_name);
            tvTextDay = (TextView) findViewById(R.id.text_day);
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
                        getWeatherData("search");
                    }
                    return false;
                }
            });

            imSearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideVirtualKeyboard();
                    getWeatherData("search");

                }
            });
        }

        if(isNetworkAvailable())
        {
            locationRequest = setLocationRequest();
            showGPSDialog(locationRequest);
            pbLoading.setVisibility(View.VISIBLE);
            getLastLoc();
        }
        else {
            pbLoading.setVisibility(View.GONE);
            View view = findViewById(R.id.main_layout);
            Snackbar snackbar = Snackbar.make(view, "Internet Disconnected", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }

    }
    private void init()
    {
        recyclerView = (RecyclerView) findViewById(R.id.forecast_recyclerview);
        Retrofit retrofit = RetrofitInstance.getRetrofit();
        apiInterface = retrofit.create(ApiInterface.class);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        layoutManager.setInitialPrefetchItemCount(12);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("temp",tvTemperature.getText().toString());
        editor.putString("day",tvTextDay.getText().toString());
        editor.putString("city",tvCityName.getText().toString());
        editor.putString("lastUpdatedTime",tvLastUpdatedTime.getText().toString());
        editor.putString("forecast",tvForecast.getText().toString());
        editor.putString("humidity",tvHumidity.getText().toString());
        editor.putString("realFeel",tvRealFeel.getText().toString());
        editor.putString("sunrises",tvSunrises.getText().toString());
        editor.putString("sunsets",tvSunsets.getText().toString());
        editor.putString("pressure",tvPressure.getText().toString());
        editor.putString("windSpeed",tvWindSpeed.getText().toString());
        editor.putString("forecastIcon", (String) imForecastIcon.getTag());

        if(updatedAt != null)
        {
            editor.putLong("lastUpdatedAt",updatedAt);
        }

        Gson gson = new Gson();
        String json = gson.toJson(forecastWeatherModelList);
        editor.putString("forecastList",json);
        editor.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        //get required data from shared preferences
        String temp  = sharedPreferences.getString("temp", "");
        String city  = sharedPreferences.getString("city", "");
        String lastUpdatedTime = sharedPreferences.getString("lastUpdatedTime", "");
        String forecast = sharedPreferences.getString("forecast", "");
        String humidity = sharedPreferences.getString("humidity", "");
        String realFeel = sharedPreferences.getString("realFeel", "");
        String sunrises = sharedPreferences.getString("sunrises", "");
        String sunsets = sharedPreferences.getString("sunsets", "");
        String pressure = sharedPreferences.getString("pressure", "");
        String windSpeed = sharedPreferences.getString("windSpeed", "");
        String forecastIcon = sharedPreferences.getString("forecastIcon","");
        String day = sharedPreferences.getString("day","");
        //set data in textviews
        tvTemperature.setText(temp);
        tvCityName.setText(city);
        tvLastUpdatedTime.setText(lastUpdatedTime);
        tvForecast.setText(forecast);
        tvHumidity.setText(humidity);
        tvTextDay.setText(day);
        tvRealFeel.setText(realFeel);
        tvSunrises.setText(sunrises);
        tvSunsets.setText(sunsets);
        tvPressure.setText(pressure);
        tvWindSpeed.setText(windSpeed);
        if (forecast != null) {
            String iconFile = forecastIcons.getForecastIcon(forecastIcon);
            if(iconFile != null) {
                if (!iconFile.isEmpty()) {
                    String PACKAGE_NAME = MainActivity.PACKAGE_NAME;
                    int imgId = MainActivity.mContext.getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + iconFile, null, null);
                    imForecastIcon.setImageBitmap(BitmapFactory.decodeResource( MainActivity.mContext.getResources(), imgId));
                    imForecastIcon.setTag(forecastIcon);
                }
            }
        }

        Gson gson = new Gson();
        String json = sharedPreferences.getString("forecastList", null);
        Type type = new TypeToken<ArrayList<List>>() {}.getType();
        forecastWeatherModelList = gson.fromJson(json, type);
        if(forecastWeatherModelList != null) {
            ForecastAdapter fcAdapter = new ForecastAdapter(forecastWeatherModelList);
            recyclerView.setAdapter(fcAdapter);
        }


    }

    public LocationRequest setLocationRequest()
    {
        LocationRequest locationReq;
        locationReq = new LocationRequest();
        locationReq.setInterval(1000 * 30);
        locationReq.setFastestInterval(1000 * 5);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationReq;
    }

    private void showGPSDialog(LocationRequest locationRequest)
    {
        if(!isGpsEnabled())
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_FINE_LOCATION);
            }

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        task.getResult(ApiException.class);
                    }
                    catch (ApiException e)
                    {
                        switch (e.getStatusCode())
                        {
                            case LocationSettingsStatusCodes
                                    .RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                    resolvableApiException.startResolutionForResult(MainActivity.this,REQUEST_CHECK_CODE);
                                }
                                catch (IntentSender.SendIntentException ex)
                                {
                                }
                                catch (ClassCastException ex)
                                {
                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:{
                                break;
                            }


                        }
                    }
                }
            });
        }

    }

    private boolean isGpsEnabled()
    {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER)&&service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLastLoc();
                        break;
                    case Activity.RESULT_CANCELED:
                       pbLoading.setVisibility(View.GONE);
                        break;
                }
                break;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    getLastLoc();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Permission declined", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @SuppressLint("MissingPermission")
    private void getLastLoc()
    {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    currLatitude = location.getLatitude();
                    currLongitude = location.getLongitude();
                    isLocAvalaible = true;
                    getWeatherData("gps");
                }
                else
                {
                    isLocAvalaible = false;
                    getLastLoc();
                }
            }
        });
    }


    private void hideVirtualKeyboard()
    {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    private void getWeatherData(String type)
    {
        if(isNetworkAvailable())
        {
            pbLoading.setVisibility(View.VISIBLE);
            if(type == "search")
            {
                searchQuery = etSearchBox.getText().toString();
                if (!searchQuery.isEmpty()) {
                    getCurrentWeatherOnSearch(searchQuery);
                    getForecastWeatherOnSearch(searchQuery);
                }
                else
                {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Please Enter a Location to search ", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                if(currLatitude != 0.0 && currLongitude != 0.0)
                {
                    String lat =  String.valueOf(currLatitude);
                    String log = String.valueOf(currLongitude);
                    getCurrentWeatherOnLocation(lat,log);
                    getForecastWeatherOnLocation(lat,log);
                }
                else
                {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Unable to detect Your Location Please try search Instead ", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            pbLoading.setVisibility(View.GONE);
            View view = findViewById(R.id.main_layout);
            Snackbar snackbar = Snackbar.make(view, "Internet Disconnected", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }



    }


    private void getCurrentWeatherOnSearch(String searchQuery)
    {
        apiInterface.getCurrWeather(searchQuery,"metric",API_KEY).enqueue(new Callback<CurrentWeather>() {
            @Override
            public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        cityName = response.body().getName();
                        if (!cityName.equals("Globe")) {
                            countryName = response.body().getSys().getCountry();
                            updatedAt = (long) response.body().getDt();
                            textDay = new SimpleDateFormat("EEE, d MMM", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                            updatedAtText = "Last Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                            temperature = response.body().getMain().getTemp();
                            Weather weather = response.body().getWeather().get(0);
                            forecastIcon = weather.getIcon();
                            forecast = weather.getDescription();
                            humidity = response.body().getMain().getHumidity().toString();
                            realFeel = response.body().getMain().getFeelsLike().toString();
                            pressure = response.body().getMain().getPressure().toString();
                            windSpeed = response.body().getWind().getSpeed().toString();
                            rise = (long) response.body().getSys().getSunrise();
                            sunrises = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(rise * 1000));
                            set = (long) response.body().getSys().getSunset();
                            sunsets = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(set * 1000));
                            // SET ALL VALUES IN TEXTBOX :
                            tvCityName.setText(cityName);
                            tvLastUpdatedTime.setText(updatedAtText);
                            tvTemperature.setText(temperature + "°C");
                            tvForecast.setText(forecast);
                            tvHumidity.setText(humidity);
                            tvRealFeel.setText(realFeel + "°C");
                            tvSunrises.setText(sunrises);
                            tvSunsets.setText(sunsets);
                            tvPressure.setText(pressure);
                            tvWindSpeed.setText(windSpeed);
                            tvTextDay.setText(textDay);
                            //set icon here
                            String iconFile = forecastIcons.getForecastIcon(forecastIcon);
                            if(iconFile != null) {
                                if (!iconFile.isEmpty()) {
                                    String PACKAGE_NAME = MainActivity.PACKAGE_NAME;
                                    int imgId = MainActivity.mContext.getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + iconFile, null, null);
                                    imForecastIcon.setImageBitmap(BitmapFactory.decodeResource( MainActivity.mContext.getResources(), imgId));
                                    imForecastIcon.setTag(forecastIcon);
                                } else {
                                    imForecastIcon.setImageResource(R.drawable.ic_one_day);
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Unable to detect Your Location please use search instead ", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<CurrentWeather> call, Throwable t) {
                Toast.makeText(MainActivity.this,"failure :" +t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getForecastWeatherOnSearch(String searchQuery)
    {
        apiInterface.getForecast(searchQuery,"metric",API_KEY).enqueue(new Callback<ForecastWeatherModel>() {

            @Override
            public void onResponse(Call<ForecastWeatherModel> call, Response<ForecastWeatherModel> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        forecastWeatherModelList = response.body().getList();
                        ForecastAdapter fcAdapter = new ForecastAdapter(response.body().getList() );
                        recyclerView.setAdapter(fcAdapter);
                        pbLoading.setVisibility(View.GONE);
                    }
                }
                else if (response.code() == 404) {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                } else {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<ForecastWeatherModel> call, Throwable t) {
                Toast.makeText(MainActivity.this,"forecast failure :" +t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCurrentWeatherOnLocation(String latitude, String longitude)
    {
        apiInterface.getCurrWeatherOnLocation(latitude,longitude,"metric",API_KEY).enqueue(new Callback<CurrentWeather>() {
            @Override
            public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        cityName = response.body().getName();
                        if (!cityName.equals("Globe")) {
                            countryName = response.body().getSys().getCountry();
                            updatedAt = (long) response.body().getDt();
                            textDay = new SimpleDateFormat("EEE, d MMM", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                            updatedAtText = "Last Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                            temperature = response.body().getMain().getTemp();
                            Weather weather = response.body().getWeather().get(0);
                            forecastIcon = weather.getIcon();
                            forecast = weather.getDescription();
                            humidity = response.body().getMain().getHumidity().toString();
                            realFeel = response.body().getMain().getFeelsLike().toString();
                            pressure = response.body().getMain().getPressure().toString();
                            windSpeed = response.body().getWind().getSpeed().toString();
                            rise = (long) response.body().getSys().getSunrise();
                            sunrises = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(rise * 1000));
                            set = (long) response.body().getSys().getSunset();
                            sunsets = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(set * 1000));
                            // SET ALL VALUES IN TEXTBOX :
                            tvCityName.setText(cityName);
                            tvLastUpdatedTime.setText(updatedAtText);
                            tvTemperature.setText(temperature + "°C");
                            tvForecast.setText(forecast);
                            tvHumidity.setText(humidity);
                            tvRealFeel.setText(realFeel + "°C");
                            tvSunrises.setText(sunrises);
                            tvSunsets.setText(sunsets);
                            tvPressure.setText(pressure);
                            tvWindSpeed.setText(windSpeed);
                            tvTextDay.setText(textDay);
                            //set icon here
                            String iconFile = forecastIcons.getForecastIcon(forecastIcon);
                            if(iconFile != null) {
                                if (!iconFile.isEmpty()) {
                                    String PACKAGE_NAME = MainActivity.PACKAGE_NAME;
                                    int imgId = MainActivity.mContext.getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + iconFile, null, null);
                                    imForecastIcon.setImageBitmap(BitmapFactory.decodeResource( MainActivity.mContext.getResources(), imgId));
                                    imForecastIcon.setTag(forecastIcon);
                                } else {
                                    imForecastIcon.setImageResource(R.drawable.ic_one_day);
                                }
                            }
                        } else {

                            Toast.makeText(MainActivity.this, "Unable to detect Your Location please use search instead ", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<CurrentWeather> call, Throwable t) {
                Toast.makeText(MainActivity.this,"failure :" +t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getForecastWeatherOnLocation(String latitude, String longitude)
    {
        apiInterface.getForecastOnLocation(latitude,longitude,"metric",API_KEY).enqueue(new Callback<ForecastWeatherModel>() {

            @Override
            public void onResponse(Call<ForecastWeatherModel> call, Response<ForecastWeatherModel> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        forecastWeatherModelList = response.body().getList();
                        ForecastAdapter fcAdapter = new ForecastAdapter(response.body().getList() );
                        recyclerView.setAdapter(fcAdapter);
                        pbLoading.setVisibility(View.GONE);
                    }
                }
                else if (response.code() == 404) {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error : Location Not found ", Toast.LENGTH_LONG).show();
                } else {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Sorry Some Error Occurred try again", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<ForecastWeatherModel> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,"forecast failure :" +t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
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

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                boolean isGPSEnabled = isGpsEnabled();
                if (isGPSEnabled)
                {
                    getLastLoc();

                }
                else
                {
                }

            }
        }
    };


}