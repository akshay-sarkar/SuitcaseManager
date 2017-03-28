package edu.uta.cse5320.dao;

/**
 * Created by Akshay on 3/28/2017.
 */

public class AirlineData {
    private String airlineName;
    private String airlineUrl;
    private String airlineCustomerCare;
    private String airlineEmail;

    public AirlineData(){}

    public AirlineData(String airlineName, String airlineUrl, String airlineCustomerCare,
                       String airlineEmail) {
        this.airlineName = airlineName;
        this.airlineUrl = airlineUrl;
        this.airlineCustomerCare = airlineCustomerCare;
        this.airlineEmail = airlineEmail;
    }

    public String getAirlineName() {
        return this.airlineName;
    }

    public String getAirlineUrl() {
        return this.airlineUrl;
    }

    public String getAirlineCustomerCare() {
        return this.airlineCustomerCare;
    }

    public String getAirlineEmail() {
        return this.airlineEmail;
    }
}
