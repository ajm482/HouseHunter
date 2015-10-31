package com.example.alexmarion.househunter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity {
    ArrayList<House> houseArrayList = new ArrayList<>();
    int[] visitedOrderList = null;
    boolean viewMapPressed = false;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_menu);
        LinearLayout inputMenu = (LinearLayout) findViewById(R.id.input_menu);
        inputMenu.setVisibility(View.VISIBLE);

        Button enterAddress = (Button) findViewById(R.id.enterAddress);
        String houseNumber;
        enterAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText houseNumberInput = (EditText) findViewById(R.id.houseNumber);
                String houseNumber = houseNumberInput.getText().toString();

                EditText streetInput = (EditText) findViewById(R.id.street);
                String street = streetInput.getText().toString();

                EditText cityInput = (EditText) findViewById(R.id.city);
                String city = cityInput.getText().toString();

                EditText stateInput = (EditText) findViewById(R.id.state);
                String state = stateInput.getText().toString();

                EditText timeOpenInput = (EditText) findViewById(R.id.timeOpen);
                String timeOpen = timeOpenInput.getText().toString();

                EditText timeCloseInput = (EditText) findViewById(R.id.timeClose);
                String timeClose = timeCloseInput.getText().toString();


                Address tempAddress = new Address(houseNumber, street, city, state);
                House tempHouse = new House(tempAddress, timeOpen, timeClose);
                houseArrayList.add(tempHouse);

                houseNumberInput.setText("");
                streetInput.setText("");
                cityInput.setText("");
                stateInput.setText("");
                timeOpenInput.setText("");
                timeCloseInput.setText("");

                for(int i = 0; i < houseArrayList.size(); i++) {
                    houseArrayList.get(i).getAddress().printAddress();
                }
            }
        });

        Button viewMap = (Button) findViewById(R.id.viewMap);
        addButtonListener_viewMap(viewMap);
    }

    public void addButtonListener_viewMap(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMapPressed = true;
                setContentView(R.layout.activity_maps);
                setUpMapIfNeeded();
                FrameLayout map = (FrameLayout) findViewById(R.id.map);


                int numHouses = houseArrayList.size();
                Address[] addressList = new Address[numHouses];
                House[] houseList = new House[numHouses];
                for (int i = 0; i < houseArrayList.size(); i++) {
                    addressList[i] = houseArrayList.get(i).getAddress();
                    houseList[i] = houseArrayList.get(i);
                }



                /*
                int numHouses = 6;
                Address[] addressList = new Address[numHouses];
                House[] houseList = new House[numHouses];

                addressList[0] = new Address("3506", "Old Court Road", "Pikesville", "MD");
                addressList[1] = new Address("2520", "Stonemill Road", "Pikesville", "MD");
                addressList[2] = new Address("10801", "Longacre Lane", "Stevenson", "MD");
                addressList[3] = new Address("7914", "Knollwood Road", "Towson", "MD");
                addressList[4] = new Address("3103", "Acton Road", "Baltimore", "MD");
                addressList[5] = new Address("6013", "Eunice Avenue", "Baltimore", "MD");

                houseList[0] = new House(addressList[0], "08:00", "08:30");
                houseList[1] = new House(addressList[1], "08:00", "13:30");
                houseList[2] = new House(addressList[2], "08:00", "13:00");
                houseList[3] = new House(addressList[3], "08:00", "14:30");
                houseList[4] = new House(addressList[4], "08:00", "11:00");
                houseList[5] = new House(addressList[5], "08:00", "14:30");
                */

                // GETTING DISTANCE MATRIX

                DistanceMatrixGetter distanceMatrixGetter = new DistanceMatrixGetter();
                try {
                    int[][] adjacencyMatrix = distanceMatrixGetter.execute(houseList).get();

                    DateFormat formatter = new SimpleDateFormat("hh:mm");
                    Date startTime = formatter.parse("08:00");
                    House[] housePath = NearestNeighbour(adjacencyMatrix, houseList, startTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                    System.out.println("PLEASE FORMAT TIME CORRECTLY HH:MM IN 24 HOUR TIME");
                }

                // SETTING NEW ADDRESS ORDER

                Address[] orderedAddressList = new Address[addressList.length];
                for (int i = 0; i < orderedAddressList.length; i++) {
                    orderedAddressList[i] = addressList[visitedOrderList[i]];
                }

                // GETTING LAT AND LNG FROM ADDRESSES
                GeocodeAddress geocoder = new GeocodeAddress();
                LatLng[] coordinatesList = new LatLng[orderedAddressList.length];
                try {
                    coordinatesList = geocoder.execute(orderedAddressList).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // SETTING PINS ON MAP AND ZOOMING (UNNECESSARY ZOOOOOM)
                ArrayList<Marker> markerList = new ArrayList<>();
                for (int i = 0; i < coordinatesList.length; i++) {
                    float color = 90 + ((360 * (i + 1)) / coordinatesList.length);
                    while (color > 360.0) {
                        color -= 360.0;
                    }
                    System.out.println(color);
                    Marker tempMarker = mMap.addMarker(new MarkerOptions().position(coordinatesList[i]).icon(BitmapDescriptorFactory.defaultMarker(color)));
                    tempMarker.setTitle(String.valueOf(i + 1));
                    markerList.add(tempMarker);
                }
                System.out.println("MARKER LIST LENGTH: " + markerList.size());

                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markerList) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 100;
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition arg0) {
                        // Move camera.
                        mMap.animateCamera(cameraUpdate);
                        // Remove listener to prevent position reset on camera move.
                        mMap.setOnCameraChangeListener(null);
                    }
                });


                // GETTING DIRECTIONS IN POLYLINE FORM
                DirectionsGetter directionsGetter = new DirectionsGetter();
                String[] pointsList = null;
                try {
                    pointsList = directionsGetter.execute(orderedAddressList).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // CONVERTING POINTSLIST AND DRAWING ON MAP
                drawPath(pointsList);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewMapPressed) {
            setUpMapIfNeeded();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    public House[] NearestNeighbour(int[][] adjacencyMatrix, House[] houseList, Date startTime) {
        long SECONDS_TO_MILLISECONDS = 1000;
        long MINUTES_TO_MILLISECONDS = 60000;
        Date currentTime = startTime;


        System.out.println(currentTime.toString());

        int[] visited = new int[adjacencyMatrix.length];
        Queue<Integer> visitedOrder = new LinkedList<>();
        House[] housePath = new House[adjacencyMatrix.length];
        Stack<Integer> stack = new Stack<Integer>();

        // Begin search at index 0
        visited[0] = 1;
        stack.push(0);
        visitedOrder.add(0);
        int next;
        int dst = 0;
        int min = Integer.MAX_VALUE;
        boolean foundMin = false;

        while (!stack.isEmpty()) {
            next = stack.peek();
            min = Integer.MAX_VALUE;
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                if (adjacencyMatrix[next][i] > 0 && visited[i] == 0) {
                    if (adjacencyMatrix[next][i] < min) {
                        long driveTime = adjacencyMatrix[next][i] * SECONDS_TO_MILLISECONDS;
                        Date tempTime = currentTime;
                        tempTime.setTime(currentTime.getTime() + driveTime);
                        if(tempTime.compareTo(houseList[i].getTimeOpen()) > 0 && tempTime.compareTo(houseList[i].getTimeClose()) < 0) {
                            min = adjacencyMatrix[next][i];
                            dst = i;
                            foundMin = true;

                            // Advance currentTime by driving time and 30 minutes to visit
                            currentTime.setTime(currentTime.getTime() + 30 * MINUTES_TO_MILLISECONDS);
                            currentTime.setTime(currentTime.getTime() + driveTime);

                            houseList[i].getAddress().printAddress();
                            //System.out.println(currentTime.toString());
                            System.out.println(tempTime.compareTo(houseList[i].getTimeClose()));
                        }
                    }
                }
            }
            if (foundMin) {
                visited[dst] = 1;
                visitedOrder.add(dst);
                stack.push(dst);
                foundMin = false;
            }
            stack.pop();
        }

        int index;
        visitedOrderList = new int[housePath.length];

        for (int i = 0; i < housePath.length; i++) {
            index = visitedOrder.remove();
            housePath[i] = houseList[index];
            visitedOrderList[i] = index;
        }

        return housePath;
    }

    // POLYLINE DECODER
    // TAKEN FROM STACKOVERFLOW
    // CREDIT TO Tarsem
    // http://stackoverflow.com/questions/17425499/how-to-draw-interactive-polyline-on-route-google-maps-v2-android
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    // POLYLINE DECODER
    // TAKEN FROM STACKOVERFLOW
    // CREDIT TO Tarsem
    // http://stackoverflow.com/questions/17425499/how-to-draw-interactive-polyline-on-route-google-maps-v2-android
    public void drawPath(String[] pointsList) {
        String encodedString = "";
        for (int i = 0; i < pointsList.length; i++) {
            encodedString += pointsList[i];
        }
        List<LatLng> list = decodePoly(encodedString);
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < list.size(); z++) {
            LatLng point = list.get(z);
            options.add(point);
        }
        Polyline line = mMap.addPolyline(options);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
