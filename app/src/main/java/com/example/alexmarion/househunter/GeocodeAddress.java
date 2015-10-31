package com.example.alexmarion.househunter;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;

/**
 * Created by Alex Marion on 8/20/2015.
 */
public class GeocodeAddress extends AsyncTask<Address, Integer, LatLng[]> {

    protected String urlCreator(Address address, String apiKey) {
        String addressURL = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address.formatForURL() + "&key=" + apiKey;
        return addressURL;
    }

    protected LatLng geoJsonParser(Address address, String apiKey) {
        LatLng coordinates = null;
        String geocoderURL = urlCreator(address, apiKey);
        HttpClient geocoderHttpClient = new DefaultHttpClient();
        try {
            HttpResponse response = geocoderHttpClient.execute(new HttpGet(geocoderURL));
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                JsonParser jsonParser = new JsonParser();
                JsonObject rootobj = (JsonObject)jsonParser.parse(responseString);

                // PARSING JSON
                JsonArray results = rootobj.get("results").getAsJsonArray();
                JsonObject location = results.get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject();
                Double lat = location.get("lat").getAsDouble();
                Double lng = location.get("lng").getAsDouble();

                coordinates = new LatLng(lat, lng);
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    @Override
    protected LatLng[] doInBackground(Address... addressList) {
        LatLng[] coordinatesList = new LatLng[addressList.length];

        System.out.println("GEOCODER");

        //SERVER KEY
        String apiKey = "AIzaSyCtd9YmXKYnna2KF89LL74XT2ms1-kCoqw";

        for(int i = 0; i < coordinatesList.length; i++) {
            coordinatesList[i] = geoJsonParser(addressList[i], apiKey);
        }
        return coordinatesList;
    }
}
