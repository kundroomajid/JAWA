package com.mak.jawa.models;

import java.util.HashMap;
import java.util.Map;

public class ForecastIcons {
    private static Map<String, String>  forecastIcons = new HashMap<>();

    public ForecastIcons() {
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
        this.forecastIcons = forecastIcons;
    }

    public static Map<String, String> getForecastIcons() {
        return ForecastIcons.forecastIcons;
    }

    public void setForecastIcons() {
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
        this.forecastIcons = forecastIcons;
    }

    public String getForecastIcon(String iconname)
    {
        forecastIcons = this.getForecastIcons();
        return forecastIcons.get(iconname);
    }
}
