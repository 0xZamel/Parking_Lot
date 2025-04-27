package com.example.parking;
import com.example.parking.json.JSONUtils;
import com.example.parking.spot.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Reservation {
    private final int reservationID, ownerID, slotID;
    private final LocalDateTime reservationDate; // 2024-12-07T14:30:00 --> yyyy-MM-dd'T'HH:mm:ss
    private boolean status;
    // The Amount of Reservation Diff From Slot Amount , Because Free Hours
    private final double amount;

    public Reservation(int reservationID, int ownerID, int slotID, double amount) {
        this.reservationID = reservationID;
        this.ownerID = ownerID;
        this.slotID = slotID;
        this.amount = amount;

        SystemManager.getSlot(slotID).bookSlot();
        this.status = true;
        this.reservationDate = LocalDateTime.now();
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }
    public int getOwnerID() {
        return ownerID;
    }

    public int getReservationID() {
        return reservationID;
    }
    public double getAmount() {
        return amount;
    }
    public int getSlotID() {
        return slotID;
    }
    public Slot getSlot() {
        return SystemManager.getSlot(slotID);
    }
    public Spot getSpot() {
        return getSlot().getSpot();
    }
    public VehicleType getVehicleType() {
        return getSpot().getVehicleType();
    }

    public boolean isActive() {
        return status;
    }
    public void cancelReservation() {
        SystemManager.getSlot(slotID).cancelBooking();
        this.status = false;
    }

    // Save all reservations to a file
    public static void saveReservations(ArrayList<Reservation> reservations) {
        JSONUtils.saveToFile(reservations, "reservations.json");
    }
    // Load reservations from a file
    public static ArrayList<Reservation> loadReservations() {
        return JSONUtils.loadFromFile("reservations.json", new com.google.gson.reflect.TypeToken<ArrayList<Reservation>>() {}.getType());
    }
}