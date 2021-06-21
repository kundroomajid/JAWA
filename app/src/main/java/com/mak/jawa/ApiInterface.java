package com.mak.jawa;
import com.mak.jawa.models.CurrentWeather;
import com.mak.jawa.models.ForecastWeatherModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("weather?")
    Call<CurrentWeather> getCurrWeather(@Query("q") String location,
                                        @Query("units") String units,
                                        @Query("appid") String API_KEY);

    @GET("weather?")
    Call<CurrentWeather> getCurrWeatherOnLocation(@Query("lat") String latitude,
                                                  @Query("lon") String longitude,
                                                  @Query("units") String units,
                                                  @Query("appid") String API_KEY);

    @GET("forecast?")
    Call<ForecastWeatherModel> getForecast(@Query("q") String location,
                                           @Query("units") String units,
                                           @Query("appid") String API_KEY);

    @GET("forecast?")
    Call<ForecastWeatherModel> getForecastOnLocation(@Query("lat") String latitude,
                                                     @Query("lon") String longitude,
                                                     @Query("units") String units,
                                                     @Query("appid") String API_KEY);


}


