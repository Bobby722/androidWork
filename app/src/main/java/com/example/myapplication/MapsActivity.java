package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager mLocationManager;
    Location myLocationProvider;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
                return;

            final List<String> permissionsList = new ArrayList<>();
            if(this.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
                permissionsList.add(Manifest.permission.CAMERA);
            if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionsList.size()<1)
                return;
            if(this.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                this.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]) , 0x00);
            else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", this.getPackageName(), null));
                startActivityForResult(intent , 0x00);
            }
//            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocationProvider = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        lat = myLocationProvider.getLatitude();  // 取得經度
        lng = myLocationProvider.getLongitude(); // 取得緯度

        // Add a marker in Sydney and move the camera
        LatLng user = new LatLng(lat, lng);
        Log.d("lat", ":"+lat);
        Log.d("lng", ":"+lng);

        mMap.addMarker(new MarkerOptions().position(user).title("Marker in your location"));
        float zoom=17;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user,zoom));
    }
}
