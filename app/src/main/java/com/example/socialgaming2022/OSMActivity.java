package com.example.socialgaming2022;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class OSMActivity extends AppCompatActivity {
    private static final int RESULT_FINE_LOCATION = 0;
    private static final int RESULT_COARSE_LOCATION = 0;
    private static final int RESULT_INTERNET = 0;
    private static final int RESULT_NETWORK_STATE = 0;

    private MapView mapView = null;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here
        checkPermissions();

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

        // Add player marker to OSM map
        addPlayerMarkerToMap();

        // Add other player's markers to OSM map
        addOtherPlayerMarkersToMap();
    }

    private void addPlayerMarkerToMap() {
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        Bitmap bitmapMapMarkerBlue = BitmapFactory.decodeResource(getResources(), R.drawable.mapmarkerblue);
        Bitmap bitmapArrowBlue = BitmapFactory.decodeResource(getResources(), R.drawable.arrowblue);
        myLocationNewOverlay.setPersonIcon(bitmapMapMarkerBlue);
        myLocationNewOverlay.setDirectionIcon(bitmapArrowBlue);
        myLocationNewOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    private void addOtherPlayerMarkersToMap() {
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(48.26245, 11.66839));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.mapmarkeryellow));
        startMarker.setTitle("TUM");
        mapView.getOverlays().add(startMarker);
    }

    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(OSMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(OSMActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RESULT_FINE_LOCATION);

        if(ContextCompat.checkSelfPermission(OSMActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(OSMActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RESULT_COARSE_LOCATION);

        if(ContextCompat.checkSelfPermission(OSMActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(OSMActivity.this, new String[]{Manifest.permission.INTERNET}, RESULT_INTERNET);

        if(ContextCompat.checkSelfPermission(OSMActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(OSMActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, RESULT_NETWORK_STATE);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}