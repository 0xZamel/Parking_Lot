package com.example.parking.spot;
import com.example.parking.*;

import java.util.*;

public abstract class Spot {
    protected int spotID;
    protected double hourRate;
    protected Map<Integer, Slot> slots;

    protected static final double  BIKE_HOUR_RATE = 5, CAR_HOUR_RATE = 7, TRUCK_HOUR_RATE = 10;

    public Spot(int spotID, double hourRate) {
        this.spotID = spotID;
        this.hourRate = hourRate;
        slots = new HashMap<>();
    }

    public int getSpotID() {
        return spotID;
    }
    public double getHourRate() {
        return hourRate;
    }

    public abstract VehicleType getVehicleType();
    public ArrayList<Slot> getSlots() {
        return new ArrayList<>(slots.values());
    }

    public boolean isSlotExists(int slotID) {
        return slots.containsKey(slotID);
    }

    public Slot getSlot(int slotID) {
        return slots.get(slotID);
    }

    public void addSlot(Slot slot) {
        slots.put(slot.getSlotID(), slot);
    }
    public void removeSlot(int slotID) {
        slots.remove(slotID);
    }
}
