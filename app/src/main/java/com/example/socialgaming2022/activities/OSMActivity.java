package com.example.socialgaming2022.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.socialgaming2022.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class OSMActivity extends AppCompatActivity {
    private static final String TAG = "OSMActivity";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;

    private MapView mapView = null;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference activePlayers;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final long LOCATION_REQUEST_INTERVAL = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here
        requestPermissionsIfNecessary();

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get Firebase realtime database instance
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://socialgaming2022-default-rtdb.europe-west1.firebasedatabase.app/");

        // Get reference to activePlayers
        activePlayers = firebaseDatabase.getReference().child("activePlayers");

        // Get location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_osmactivity);

        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(15.0);

        // Update Firebase realtime database with current player location
        updateFirebaseRealtimeDatabasePlayerLocation();

        // Clear map and add all markers to map whenever data in Firebase-RTD changes
        addMarkersToMap();

        // Add TUM marker to OSM map
        addTumMarkerToMap();
    }

    private void addPlayerMarkerToMap() {
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(this);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        Bitmap bitmapMapMarkerBlue = BitmapFactory.decodeResource(getResources(), R.drawable.mapmarkerblue);
        Bitmap bitmapArrowBlue = BitmapFactory.decodeResource(getResources(), R.drawable.arrowblue);
        myLocationNewOverlay.setPersonIcon(bitmapMapMarkerBlue);
        myLocationNewOverlay.setDirectionIcon(bitmapArrowBlue);
        myLocationNewOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    private void addTumMarkerToMap() {
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(48.26245, 11.66839));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.mapmarkeryellow));
        startMarker.setTitle("TUM");
        mapView.getOverlays().add(startMarker);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @SuppressLint("MissingPermission")
    private void updateFirebaseRealtimeDatabasePlayerLocation() {
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Save current location into Firebase real-time database
                        if(firebaseAuth.getCurrentUser() != null) {
                            activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("latitude").setValue(location.getLatitude());
                            activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("longitude").setValue(location.getLongitude());
                        }
                    }
                }
            });

        // Get player location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Save new location into Firebase real-time database
                if(firebaseAuth.getCurrentUser() != null) {
                    activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("latitude").setValue(locationResult.getLastLocation().getLatitude());
                    activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("longitude").setValue(locationResult.getLastLocation().getLongitude());
                }
            }
        };

        // Request updates on the current user's location
        requestLocationUpdate();
    }

    private void addMarkersToMap() {
        // Read Firebase realtime database
        activePlayers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the new data from Firebase realtime database
                Gson gson = new Gson();
                JsonElement activePlayers = gson.toJsonTree(snapshot.getValue());

                // Clear all markers
                mapView.getOverlays().clear();

                for(Map.Entry<String, JsonElement> activePlayer : activePlayers.getAsJsonObject().entrySet()) {
                    // If FirebaseUID is equal to current user -> skip
                    if(firebaseAuth.getCurrentUser() != null && activePlayer.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                        addPlayerMarkerToMap();
                    } else {
                        // For every other user add a green marker to map
                        addOtherPlayerMarkersToMap(activePlayer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read data from Firebase realtime database!", error.toException());
            }
        });
    }

    private void addOtherPlayerMarkersToMap(Map.Entry<String, JsonElement> activePlayer) {
        // Get information from the Firebase realtime database
        String firebaseUID = activePlayer.getKey();
        JsonElement latitude = activePlayer.getValue().getAsJsonObject().get("latitude");
        JsonElement longitude = activePlayer.getValue().getAsJsonObject().get("longitude");

        if(firebaseUID != null && latitude != null && longitude != null) {
            // For every other user add a marker to map
            Marker playerMarker = new Marker(mapView);
            playerMarker.setPosition(new GeoPoint(latitude.getAsDouble(), longitude.getAsDouble()));
            playerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            playerMarker.setIcon(ContextCompat.getDrawable(OSMActivity.this, R.drawable.mapmarkergreen));
            playerMarker.setTitle(firebaseUID);
            mapView.getOverlays().add(playerMarker);
        }
    }

    private void requestPermissionsIfNecessary() {
        // The required permissions
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        // Save all permissions that the user has not granted yet
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, add it to the list
                permissionsToRequest.add(permission);
            }
        }

        // If there are permissions missing
        if (permissionsToRequest.size() > 0) {
            // Request those permissions
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Save all permissions that the user has not granted yet
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));

        // If there are permissions missing
        if (permissionsToRequest.size() > 0) {
            // Request those permissions
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}