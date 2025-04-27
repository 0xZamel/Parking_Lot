package com.example.parking;

import com.example.parking.spot.*;
import java.util.*;

public class Payment {
    private final int ownerID;
    private double balance, penalty;
    // Example data: total hours for each VehicleType
    EnumMap<VehicleType, Double> lastHours;

    private final int REWARD_HOURS = 7; // Every 6 Hours Get 1 Hour Free
    private final double PENALTY_AMOUNT = 3; // Three Dollars

    public Payment(int ownerID, double balance) {
        this.ownerID = ownerID;
        this.balance = balance;

        this.penalty = 0;
        lastHours = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            lastHours.put(type, 0.0);
        }
    }
    // Copy constructor
    public Payment(Payment otherPayment) {
        this.ownerID = otherPayment.ownerID;
        this.balance = otherPayment.balance;
        this.penalty = otherPayment.penalty;
        this.lastHours = new EnumMap<>(otherPayment.lastHours);  // Copy the map
    }

    public double getBalance() {
        return balance;
    }
    public void deposit(double balance) {
        this.balance += balance;
    }
    public void withdraw(double balance) {
        this.balance -= balance;
    }

    // LastHours
    private double getLastHours(VehicleType vehicleType) {
        return lastHours.get(vehicleType);
    }
    private void setLastHours(VehicleType vehicleType, double hours) {
        this.lastHours.put(vehicleType, hours);
    }
    private void addHours(VehicleType vehicleType, double hours) {
        double newHours = (getLastHours(vehicleType) + hours) % REWARD_HOURS;
        setLastHours(vehicleType, newHours);
    }
    private void minusHours(VehicleType vehicleType, double hours) {
        double newHours = (getLastHours(vehicleType) % REWARD_HOURS - hours % REWARD_HOURS + REWARD_HOURS) % REWARD_HOURS;
        setLastHours(vehicleType, newHours);
    }

    // Penalty
    public double getPenalty() {
        return penalty;
    }
    public void addPenalty(double penalty) {
        this.penalty += penalty;
    }
    public void resetPenalty() {
        this.penalty = 0;
    }

    private static double calcFees(double hours, double hour_rate) {
        return hours * hour_rate;
    }
    // Basic Slot Amount
    public static double slotAmount(Slot slot) {
        Spot spot = slot.getSpot();
        return calcFees(slot.getHours(), spot.getHourRate());
    }

    // Reservation Amount Consider Free Hours
    public double reservationAmount(Slot slot) {
        Spot spot = slot.getSpot();
        double amount = slot.getAmount();

        double freeHours = (lastHours.get(spot.getVehicleType()) + slot.getHours()) / REWARD_HOURS;
        freeHours = Math.floor(freeHours);
        return amount - calcFees(freeHours, spot.getHourRate());
    }

    // While He is Choosing The Slots
    public static double totalAmount(Payment payment, ArrayList<Slot> slots) {
        double total = 0;
        Payment currentPayment = new Payment(payment);
        for (Slot slot : slots) {
            total += currentPayment.reservationAmount(slot);
            currentPayment.addHours(slot.getVehicleType(), slot.getHours());
        }
        return total;
    }

    public void confirmReservation(Reservation reservation) {
        Slot slot = reservation.getSlot();

        double amount = reservation.getAmount() + getPenalty();
        addHours(slot.getVehicleType(), slot.getHours());
        withdraw(amount);
        resetPenalty();
    }
    // Owner Cancel Reservation
    public void ownerCancelReservation(Reservation reservation) {
        Slot slot = reservation.getSlot();
        double amount = reservation.getAmount();
        minusHours(slot.getVehicleType(), slot.getHours());
        deposit(reservation.getAmount());
        addPenalty(PENALTY_AMOUNT);
    }
    // Admin Cancel My Reservation -> don't Apply Penalty On Owner
    public void adminCancelReservation(Reservation reservation) {
        Slot slot = reservation.getSlot();
        double amount = reservation.getAmount();
        minusHours(slot.getVehicleType(), slot.getHours());
        deposit(reservation.getAmount());
    }
    // When He Update Reservation , He Get The Money Back
    public void updateReservation(Reservation reservation) {
        deposit(reservation.getAmount());
    }

}
