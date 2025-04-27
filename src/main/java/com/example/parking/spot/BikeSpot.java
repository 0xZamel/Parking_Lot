package com.example.parking.spot;

import com.example.parking.VehicleType;
import com.example.parking.json.JSONUtils;

import java.util.ArrayList;

public class BikeSpot extends Spot{
    public  BikeSpot(int id){
        super(id, BIKE_HOUR_RATE);
    }

    public VehicleType getVehicleType() {
        return VehicleType.Bike;
    }

    public static void saveSpots(ArrayList<BikeSpot> spots) {
        JSONUtils.saveToFile(spots, "bikeSpots.json");
    }
    public static ArrayList<BikeSpot> loadSpots() {
        return JSONUtils.loadFromFile("bikeSpots.json", new com.google.gson.reflect.TypeToken<ArrayList<BikeSpot>>() {}.getType());
    }
}