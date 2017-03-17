package edu.uta.cse5320.dao;

/**
 * Created by Akshay on 3/13/2017.
 */

public class TripData {

    private long id;
    private String tripName;
    private String tripStartDate;
    private String tripEndDate;
    private String tripAirlineName;
    private String tripDetails;

    public TripData() {
    }

    public TripData(String tripName, String tripStartDate, String tripEndDate, String tripAirlineName, String tripDetails) {
        this.tripName = tripName;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
        this.tripAirlineName = tripAirlineName;
        this.tripDetails = tripDetails;
    }

    public TripData(long id, String tripName, String tripStartDate, String tripEndDate, String tripAirlineName, String tripDetails) {
        this.id = id;
        this.tripName = tripName;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
        this.tripAirlineName = tripAirlineName;
        this.tripDetails = tripDetails;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(String tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public String getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(String tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public String getTripAirlineName() {
        return tripAirlineName;
    }

    public void setTripAirlineName(String tripAirlineName) {
        this.tripAirlineName = tripAirlineName;
    }

    public String getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(String tripDetails) {
        this.tripDetails = tripDetails;
    }
}
