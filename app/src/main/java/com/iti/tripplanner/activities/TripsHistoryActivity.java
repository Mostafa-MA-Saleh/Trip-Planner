package com.iti.tripplanner.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.iti.tripplanner.R;
import com.iti.tripplanner.models.Trip;
import com.iti.tripplanner.utilities.DatabaseAdapter;
import com.iti.tripplanner.utilities.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TripsHistoryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressDialog mProgressDialog;
    private int mRequests;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_history);
        mRequestQueue = NetworkAdapter.getRequestQueue(this);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mapFragment.getMapAsync(TripsHistoryActivity.this);
                mProgressDialog = new ProgressDialog(TripsHistoryActivity.this);
                mProgressDialog.setMessage("Fetching routes, Please wait...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
            }
        }, 200);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mMap = googleMap;
        if (currentUser != null) {
            DatabaseAdapter
                    .getInstance()
                    .getDatabase()
                    .getReference(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                if (!postSnapshot.getKey().equals("Count")) {
                                    Trip trip = postSnapshot.getValue(Trip.class);
                                    mRequestQueue.add(
                                            new JsonObjectRequest(
                                                    "https://maps.googleapis.com/maps/api/directions/json?origin="
                                                            + trip.getStartCoordinates()
                                                            + "&destination="
                                                            + trip.getDestinationCoordinates()
                                                            + "&key=AIzaSyBdKV8BgBxsEiDjArDdRRPO4xXLFbcil3Y",
                                                    null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            int colors[] = {Color.BLUE, Color.BLACK, Color.GRAY, Color.CYAN, Color.MAGENTA, Color.RED};
                                                            Random rand = new Random();
                                                            int randomColor = colors[rand.nextInt(colors.length)];
                                                            drawPath(response, mMap, randomColor);
                                                        }
                                                    },
                                                    new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                        }
                                                    }
                                            )
                                    );
                                    mRequests++;
                                    String[] coordinates = trip.getStartCoordinates().split(",");
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])), 10f));
                                }
                            }
                            mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                                @Override
                                public void onRequestFinished(Request<Object> request) {
                                    mRequests--;
                                    if (mRequests == 0)
                                        mProgressDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    public void drawPath(JSONObject result, GoogleMap Map, int color) {

        try {
            JSONArray routeArray = result.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Map.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(color)
                    .geodesic(true)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
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
}
