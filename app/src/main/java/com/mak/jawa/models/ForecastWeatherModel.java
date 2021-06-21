package com.mak.jawa.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForecastWeatherModel {

    @SerializedName("cod")
    @Expose
    private String cod;
    @SerializedName("message")
    @Expose
    private Integer message;
    @SerializedName("cnt")
    @Expose
    private Integer cnt;
    @SerializedName("list")
    @Expose
    private java.util.List<com.mak.jawa.models.List> list = null;
    @SerializedName("city")
    @Expose
    private City city;

    /**
     * No args constructor for use in serialization
     *
     */
    public ForecastWeatherModel() {
    }

    /**
     *
     * @param city
     * @param cnt
     * @param cod
     * @param message
     * @param list
     */
    public ForecastWeatherModel(String cod, Integer message, Integer cnt, java.util.List<com.mak.jawa.models.List> list, City city) {
        super();
        this.cod = cod;
        this.message = message;
        this.cnt = cnt;
        this.list = list;
        this.city = city;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public java.util.List<com.mak.jawa.models.List> getList() {
        return list;
    }

    public void setList(java.util.List<com.mak.jawa.models.List> list) {
        this.list = list;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    static class Sys {

        @SerializedName("pod")
        @Expose
        private String pod;

        /**
         * No args constructor for use in serialization
         *
         */
        public Sys() {
        }

        /**
         *
         * @param pod
         */
        public Sys(String pod) {
            super();
            this.pod = pod;
        }

        public String getPod() {
            return pod;
        }

        public void setPod(String pod) {
            this.pod = pod;
        }

    }

}





