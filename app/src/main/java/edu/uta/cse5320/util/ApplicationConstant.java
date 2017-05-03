package edu.uta.cse5320.util;

import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by Akshay on 3/28/2017.
 */

public class ApplicationConstant {
    //Firebase DB
    public static String root_prop = "test";
    public static String root_trip_prop = "Trips";
    public static String airline_prop = "Airlines";
    public static String trip_bag_prop = "Bags";
    public static String bag_item_prop = "Items";
    public static String trip_val;
    public static String bag_val;
    public static String root_val;


    //Right Menu Strings
    public static final String logout = "Logout";
    public static final String Airline_Information = "Airline Information";
    public static final String Home = "Home";
    public static final String Tip_On = "Tip On";
    public static final String Tip_Off = "Tip Off";

    //Tip related constants
    public static boolean firstLaunch = true;
    public static final String tipflag = "tipflag";
    public static final String MySharedPrefName = "SuitcaseManagerPreference";
    public static final String[] tipheading = { "Make a packing list, or just use ours",
                                                "Roll or Cube your clothes",
                                                "Ziplock bags for Electronics",
                                                "Captures the image of your luggage bags",
                                                "Buy a lightweight suitcase and Know Airlines Limits"
                                                };
    public static final String[] tipBody = {
            "Worried you might have left something essential at home? Passport? Check. Toothbrush? \n" +
                    "For peace of mind on what to pack, Keep adding your items in the bag list.",
            "Don't arrive at your holiday destination and be faced with a pile of ironing. To save space and stop creasing, roll your clothes instead of folding them, then place them in vacuum compression bags.",
            "Well, if you want to organise your packing then get yourself a stash of ziplock bags.\n" +
                    "Phone charger, camera charger, adaptors, headphones everthing could put up at once place.\n" +
                    "Easy at security checkup to manage them as well.",
            "App captures the image of the luggage bags along with its tracking number and barcode which will be helpful in providing information about the luggage, if it’s misplaced.",
            "Don't assume that buying the most expensive designer suitcase will get you an upgrade - instead, it's more likely to attract thieves at the airport and on your travels.\n" +
                    "Baggage allowance can vary from airline to airline. Make sure you are aware of your limits before you reach the airport."
    };

    //Location Request Configuration
    public static long minTime =120000;
    public static float minDistance =120000;
    public static double latitude = 0;
    public static double longitude = 0;
    public static double accuracy = 0;


}
