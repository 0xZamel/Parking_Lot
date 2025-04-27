package com.example.parking;
import com.example.parking.json.JSONUtils;
import com.example.parking.spot.Spot;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;

public class Slot {
    private final int slotID, spotID;
    private final LocalDateTime startDate, endDate; // 2024-12-07T14:30:00 --> yyyy-MM-dd'T'HH:mm:ss
    private final double hours, amount;
    private boolean isAvailable;

    public Slot(int slotID, int spotID, LocalDateTime startDate, LocalDateTime endDate) {
        this.slotID = slotID;
        this.spotID = spotID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hours = Duration.between(startDate,endDate).toMinutes() / 60.0;
        this.isAvailable = true;

        this.amount = Payment.slotAmount(this);
    }

    public void bookSlot() {
        isAvailable = false;
    }
    public void cancelBooking() {
        isAvailable = true;
    }

    public int getSlotID() {
        return slotID;
    }
    public int getSpotID() {
        return spotID;
    }
    public Spot getSpot() {
        return SystemManager.getSpot(spotID);
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }

    public VehicleType getVehicleType() {
        return SystemManager.getSpot(spotID).getVehicleType();
    }
    public double getAmount() {
        return amount;
    }

    public double getHours() {
        return hours;
    }
    public boolean isAvailable() { return isAvailable; }

    // Save all Slots to a JSON file
    public static void saveSlots(ArrayList<Slot> slots) {
        JSONUtils.saveToFile(slots, "slots.json");
    }
}