package com.smartroom.smartroomapp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.util.converter.LocalDateStringConverter;




@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorRead {
    public double temperature;
    public double humidity;

    /* private String deviceId;
    // private Double lightLevel;
    private Boolean rfidAuthorized; */


    public double getTemperature(){
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }


}