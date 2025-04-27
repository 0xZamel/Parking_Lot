package com.example.parking.spot;

import com.example.parking.VehicleType;
import com.example.parking.json.JSONUtils;

import java.util.ArrayList;

public class CarSpot extends Spot {
    public CarSpot(int id) {
        super(id, CAR_HOUR_RATE);
    }
    public VehicleType getVehicleType()  {
        return VehicleType.Car;
    }

    public static void saveSpots(ArrayList<CarSpot> spots) {
        JSONUtils.saveToFile(spots, "carSpots.json");
    }
    public static ArrayList<CarSpot> loadSpots() {
        return JSONUtils.loadFromFile("carSpots.json", new com.google.gson.reflect.TypeToken<ArrayList<CarSpot>>() {}.getType());
    }
}