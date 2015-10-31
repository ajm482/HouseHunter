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
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Alex Marion on 8/20/2015.
 */
public class DirectionsGetter extends AsyncTask<Address, Integer, String[]> {

    protected String urlCreator(Address[] addressList, String apiKey) {
        //https://maps.googleapis.com/maps/api/directions/json?origin=Boston,MA&destination=Concord,MA&waypoints=Charlestown,MA|Lexington,MA&key=AIzaSyCUCbejsvrLHTjp2CqcvsaSvYT-VLHuKtc
        String directionsURL = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        String origin = addressList[0].formatForURL();
        String destination = addressList[addressList.length - 1].formatForURL();
        String waypoints = "";
        for(int i = 1; i < addressList.length - 1; i++) {
            if(i < addressList.length - 2) {
                waypoints += addressList[i].formatForURL() + URLEncoder.encode("|");
            } else {
                waypoints += addressList[i].formatForURL();
            }
        }
        directionsURL += origin + "&destination=" + destination + "&waypoints=" + waypoints + "&key=" + apiKey;
        return directionsURL;
    }



    @Override
    protected String[] doInBackground(Address... addressList) {

        //SERVER KEY
        String apiKey = "AIzaSyCtd9YmXKYnna2KF89LL74XT2ms1-kCoqw";

        String directionsURL = urlCreator(addressList, apiKey);
        HttpClient directionsHttpClient = new DefaultHttpClient();

        ArrayList<String> pointsArrayList = new ArrayList<>();

        try {
            HttpResponse response = directionsHttpClient.execute(new HttpGet(directionsURL));
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                JsonParser jsonParser = new JsonParser();
                JsonObject rootobj = (JsonObject)jsonParser.parse(responseString);

                // PARSING JSON
                JsonArray routes = rootobj.get("routes").getAsJsonArray();
                JsonObject overviewPolyLines = routes.get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject();
                String encodedString = overviewPolyLines.get("points").getAsString();
                pointsArrayList.add(encodedString);

            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] pointsList = new String[pointsArrayList.size()];
        for(int i = 0; i < pointsArrayList.size(); i++) {
            pointsList[i] = pointsArrayList.get(i);
        }

        return pointsList;
    }
}
