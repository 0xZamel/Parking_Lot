package com.example.parking.spot;

import com.example.parking.VehicleType;
import com.example.parking.json.JSONUtils;

import java.util.ArrayList;

public class FourByFourSpot extends Spot{
    public FourByFourSpot(int id){
        super(id, TRUCK_HOUR_RATE);
    }
    public VehicleType getVehicleType() {
        return VehicleType.FourByFour;
    }

    public static void saveSpots(ArrayList<FourByFourSpot> spots) {
        JSONUtils.saveToFile(spots, "FourByFourSpots.json");
    }
    public static ArrayList<FourByFourSpot> loadSpots() {
        return JSONUtils.loadFromFile("FourByFourSpots.json", new com.google.gson.reflect.TypeToken<ArrayList<FourByFourSpot>>() {}.getType());
    }

}