package com.example.alexmarion.househunter;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;

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

/**
 * Created by Alex Marion on 8/17/2015.
 */
public class DistanceMatrixGetter extends AsyncTask<House, Integer, int[][]> {

    protected String urlCreator(House[] houseList, String apiKey) {
        String matrixURL = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
        String origins = "";
        String destinations = "";

        for(int i = 0; i < houseList.length; i++) {
            Address tempAddress = houseList[i].getAddress();
            if(i < houseList.length - 1) {
                origins += tempAddress.formatForURL() + URLEncoder.encode("|");
                destinations += tempAddress.formatForURL() + URLEncoder.encode("|");
            } else {
                // On the last element formatting is different
                origins += tempAddress.formatForURL();
                destinations += tempAddress.formatForURL();
            }
        }

        matrixURL += origins + "&destinations=" + destinations + "&mode=driving&language=eng-ENG&key=" + apiKey;;
        return matrixURL;
    }
    @Override
    protected int[][] doInBackground(House... houseList) {

        int[][] driveTimeMatrix = new int[houseList.length][houseList.length];

        // FINGERPRINT -->      10:06:84:A7:20:06:3B:B5:16:3D:CA:19:B7:C9:96:0B:DF:7A:3E:EE
        // SERVER IP -->        68.234.154.65

        System.out.println("DISTANCE MATRIX GETTER");
        //SERVER KEY
        String apiKey = "AIzaSyCtd9YmXKYnna2KF89LL74XT2ms1-kCoqw";

        String matrixURL = urlCreator(houseList, apiKey);
        System.out.println(matrixURL);
        HttpClient matrixHttpClient = new DefaultHttpClient();

        try {
            HttpResponse response = matrixHttpClient.execute(new HttpGet(matrixURL));
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                JsonParser jsonParser = new JsonParser();
                JsonObject rootobj = (JsonObject)jsonParser.parse(responseString);

                // PARSING JSON
                JsonArray rows  = rootobj.get("rows").getAsJsonArray();
                for(int i = 0; i < rows.size(); i++) {
                    JsonArray elements = rows.get(i).getAsJsonObject().get("elements").getAsJsonArray();
                    for(int j = 0; j < elements.size(); j++) {
                        int driveTime = elements.get(j).getAsJsonObject().get("duration").getAsJsonObject().get("value").getAsInt();
                        driveTimeMatrix[i][j] = driveTime;
                    }
                }
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return driveTimeMatrix;
    }
}
