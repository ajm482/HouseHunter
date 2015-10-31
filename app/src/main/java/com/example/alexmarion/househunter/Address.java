package com.example.alexmarion.househunter;

/**
 * Created by Alex Marion on 8/20/2015.
 */
public class Address {
    String houseNum;
    String street;
    String city;
    String state;

    public Address(String _houseNum, String _street, String _city, String _state) {
        houseNum = _houseNum;
        street = _street;
        city = _city;
        state = _state;
    }

    public String getHouseNum() {
        return houseNum;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String formatForURL() {
        // FORMAT: 1600+Amphitheatre+Parkway,+Mountain+View,+CA
        return houseNum + "+" + street.replace(" ", "+") + ",+" + city.replace(" ", "+") + ",+" + state;
    }

    public void printAddress() {
        System.out.println(houseNum + " "
                + street + " "
                + city + " "
                + state);
    }
}
