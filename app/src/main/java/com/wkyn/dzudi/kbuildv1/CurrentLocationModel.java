package com.wkyn.dzudi.kbuildv1;

import java.util.ArrayList;

/**
 * Created by Dzudi on 6/23/2017.
 */

public class CurrentLocationModel {
    String email;
    Double longitude;
    Double latitude;

  public CurrentLocationModel(String eml, Double latitude, Double longitude){

        this.email = eml;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public CurrentLocationModel(){
        this.email = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setLongitude(Double lng){
        this.longitude = lng;
    }
    public void setLatitude(Double ltt){
        this.latitude = ltt;
    }
    public String getEmail(){
        return email;
    }
    public Double getLatitude(){
        return latitude;
    }
    public Double getLongitude(){
        return longitude;
    }
}
