package com.example.parking;
import com.example.parking.json.JSONUtils;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class Owner extends Person {
    private final int ownerID;
    private String licenseNumber;
    private final ArrayList<Vehicle> vehicles;
    private final ArrayList<Integer> reservations;
    private final Payment payment;

    public Owner(String userName, String password, int ownerID,  String licenseNumber, ArrayList<Vehicle> vehicles,
          double balance)
    {
        super(userName, password);

        this.ownerID = ownerID;
        this.licenseNumber = licenseNumber;
        this.vehicles = vehicles;
        this.payment = new Payment(ownerID, balance);
        this.reservations = new ArrayList<>();
    }

    public int getOwnerID() {
        return ownerID;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
    public Payment getPayment() {
        return payment;
    }

    // Reservations
    public Reservation getReservation(int reservationID) {
        return SystemManager.getReservation(reservationID);
    }
    public ArrayList<Reservation> getReservations(){
        ArrayList<Reservation> res = new ArrayList<>();
        for (Integer reservationID : reservations) {
            res.add(SystemManager.getReservation(reservationID));
        }
        return res;
    }
    public boolean isReservationExist(int reservationID) {
        for (Reservation res : getReservations()) {
            if (res.isActive() && res.getReservationID() == reservationID) {
                return true;
            }
        }
        return false;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(SystemManager.nextReservationID);
        SystemManager.reservations.put(reservation.getReservationID(), reservation);
    }
    public void removeReservation(Reservation reservation) {
        reservations.removeIf(r -> r == reservation.getReservationID());
        SystemManager.reservations.remove(reservation.getReservationID());
    }
    public void makeReservation(Slot slot) {
        double reservationAmount = payment.reservationAmount(slot);
        Reservation reservation = new Reservation(SystemManager.nextReservationID, getOwnerID(),
                                  slot.getSlotID(), reservationAmount);

        addReservation(reservation);

        // Confirm Payment
        payment.confirmReservation(reservation);
        SystemManager.nextReservationID++;
    }
    public void cancelReservation(int reservationID) {
        // Cancel Payment
        Reservation reservation = getReservation(reservationID);
        reservation.cancelReservation();
        payment.ownerCancelReservation(reservation);
    }
    public void updateReservation(Reservation reservation, ArrayList<Slot> slots) {
        reservation.cancelReservation();
        payment.updateReservation(reservation);
        removeReservation(reservation);
        for (Slot slot1 : slots) {
            makeReservation(slot1);
        }
    }

    // Feedback
    public void makeFeedback(int ownerID, int reservationID, double rate, String message) {
        Feedback feedback = new Feedback(ownerID, reservationID, rate, message);
        SystemManager.feedbacksList.add(feedback);
    }

    // Owners
    public static boolean isOwnerExist(String userName, String password) {
        for (Owner owner : SystemManager.owners.values()) {
            if (userName.equals(owner.getUserName()) && password.equals(owner.getPassword()))
                return true;
        }
        return false;
    }
    public static boolean isOwnerExist(String userName) {
        for (Owner owner : SystemManager.owners.values()) {
            if (userName.equals(owner.getUserName()))
                return true;
        }
        return false;
    }
    public static void addOwner(String userName, String Password, String licenceNumber,
                                ArrayList<Vehicle> vehicles, double balance)
    {
        Owner newOwner = new Owner(userName, Password, SystemManager.nextOwnerID, licenceNumber, vehicles, balance);
        SystemManager.owners.put(newOwner.getOwnerID(), newOwner);
        SystemManager.nextOwnerID++;
    }
    public static Owner getOwnerByUsername(String userName) {
        for (Owner owner : SystemManager.owners.values()) {
            if (owner.getUserName().equals(userName)) {
                return owner;
            }
        }
        return null;
    }

    // Validations
    public static boolean userNameValid(String userName) {
        return !(isOwnerExist(userName));
    }
    public static boolean passwordValid(String password) {
        return (password.length() >= 6);
    }
    public static boolean balanceValid(String balanceText) {
        try {
            double balanceDouble = Double.parseDouble(balanceText);
            // Check Greater Than 0
            return balanceDouble >= 0;
        } catch (NumberFormatException e) {
            // If Not Number
            return false;
        }
    }

    // Save all Owners to a JSON file
    public static void saveOwners(ArrayList<Owner> owners) {
        JSONUtils.saveToFile(owners, "owners.json");
    }
    // Load all Owners from a JSON file
    public static ArrayList<Owner> loadOwners() {
        return JSONUtils.loadFromFile("owners.json", new TypeToken<ArrayList<Owner>>() {}.getType());
    }
}