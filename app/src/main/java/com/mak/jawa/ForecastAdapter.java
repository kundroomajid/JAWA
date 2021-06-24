package com.mak.jawa;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mak.jawa.models.ForecastIcons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private List<com.mak.jawa.models.List> forecastWeatherModelList;
    public ForecastAdapter(List<com.mak.jawa.models.List> forecastWeatherModelList) {
        this.forecastWeatherModelList = forecastWeatherModelList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);
        View forecastItemView = inflater.inflate(R.layout.forecast_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(forecastItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        com.mak.jawa.models.List fcWeather = forecastWeatherModelList.get(position);
        // Set item views based on your views and data model
        Date date;
        String dateTime = fcWeather.getDtTxt();
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = formatter.parse(dateTime);
            String time = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(date);
            String datetxt = new SimpleDateFormat("EEE, d MMM",Locale.ENGLISH).format(date);
            holder.tvForecastDate.setText(datetxt);
            holder.tvForecastTime.setText(time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ForecastIcons forecastIconsObj = new ForecastIcons();
        String iconFile = forecastIconsObj.getForecastIcon(fcWeather.getWeather().get(0).getIcon());
        if(iconFile != null) {
            if (!iconFile.isEmpty()) {
                String PACKAGE_NAME = MainActivity.PACKAGE_NAME;
                int imgId = MainActivity.mContext.getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + iconFile, null, null);
                holder.ivForecastIcon.setImageBitmap(BitmapFactory.decodeResource( MainActivity.mContext.getResources(), imgId));
            } else {
                holder.ivForecastIcon.setImageResource(R.drawable.ic_one_day);
            }

        }
        holder.tvForecastTemp.setText(fcWeather.getMain().getTemp().toString() + "°C");
        holder.tvForecastDesc.setText(fcWeather.getWeather().get(0).getDescription());


    }

    @Override
    public int getItemCount() {
        return this.forecastWeatherModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivForecastIcon;
       public TextView tvForecastDate, tvForecastTemp, tvForecastDesc,tvForecastTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ivForecastIcon = (ImageView) itemView.findViewById(R.id.forecast_icon);
            tvForecastDate = (TextView) itemView.findViewById(R.id.forecast_date);
            tvForecastTime = (TextView) itemView.findViewById(R.id.forecast_time);
            tvForecastTemp = (TextView) itemView.findViewById(R.id.forecast_temp);
            tvForecastDesc = (TextView) itemView.findViewById(R.id.forecast_desc);

        }
    }
}