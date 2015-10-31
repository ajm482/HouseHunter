package com.example.alexmarion.househunter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alex Marion on 8/17/2015.
 */
public class House {
    Address address;
    Date timeOpen;
    Date timeClose;
    public House(Address _address, String _timeOpen, String _timeClose) {
        address = _address;
        //Date and time is 24 Hours
        DateFormat formatter = new SimpleDateFormat("hh:mm");
        try {
            timeOpen = formatter.parse(_timeOpen);
            timeClose = formatter.parse(_timeClose);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("PLEASE FORMAT TIME CORRECTLY HH:MM IN 24 HOUR TIME");
        }
    }

    public Address getAddress() {
        return this.address;
    }

    public Date getTimeOpen() {
        return timeOpen;
    }

    public Date getTimeClose() {
        return timeClose;
    }
}
