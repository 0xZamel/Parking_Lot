package com.example.parking;

import com.example.parking.json.JSONUtils;
import java.util.ArrayList;

public class Feedback
{
    private final double rate;
    private final String feedbackMessage;
    private final int ownerID;
    private final int ReservationID;

    public Feedback(int ownerID, int ReservationID,double rate, String feedbackMessage)
    {
        this.ownerID = ownerID;
        this.ReservationID = ReservationID;
        this.rate = rate;
        this.feedbackMessage = feedbackMessage;
    }

    public double getRate() {
        return rate;
    }
    public String getFeedbackMessage() {
        return feedbackMessage;
    }
    public int getOwnerID() {
        return ownerID;
    }
    public int getReservationID() {
        return ReservationID;
    }

    // Save the feedback to feedbacks.json
    public static void saveFeedbackToFile() {
        // Load existing feedbacks
        ArrayList<Feedback> feedbacks = SystemManager.getFeedbacks();
        // Save the updated list of feedbacks
        JSONUtils.saveToFile(feedbacks, "feedbacks.json");
    }
    // Load all feedbacks from feedbacks.json
    public static ArrayList<Feedback> loadFeedbacks() {
        return JSONUtils.loadFromFile("feedbacks.json", new com.google.gson.reflect.TypeToken<ArrayList<Feedback>>() {}.getType());
    }
}
