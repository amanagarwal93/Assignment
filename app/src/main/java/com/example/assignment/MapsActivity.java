package com.example.assignment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.example.assignment.databinding.ActivityMapsBinding;
import com.example.assignment.utility.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLngBounds latLngBounds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);

        /*
         * check GPS and Mobile Network permissions and are enabled or not
         */
        openLocationDialog();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        // Set the desired interval for active location updates, in milliseconds.
        locationRequest.setInterval(2000);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    LatLng currentLatLong = new LatLng(location.getLatitude(), location.getLongitude());
                    placeMarkerOnMap(currentLatLong);
                    Utils.showShortToastMessage(binding.relativeLayout, "onLocationResult: " + location.getLatitude() + ", " + location.getLongitude());
                    Log.d(TAG, "onLocationResult: " + location.getLatitude() + ", " + location.getLongitude());
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.trackLocationBtn.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                startLocationUpdates();
            else
                stopLocationUpdates();
        });
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
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getUserCurrentLocation();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getUserCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLong = new LatLng(location.getLatitude(), location.getLongitude());
//                        addMarker(currentLatLong);

//                        double bottomBoundary = currentLatLong.latitude - .1;
//                        double leftBoundary = currentLatLong.longitude - .1;
//                        double topBoundary = currentLatLong.latitude + .1;
//                        double rightBoundary = currentLatLong.longitude + .1;
//
//                        latLngBounds = new LatLngBounds(
//                                new LatLng(bottomBoundary, leftBoundary),
//                                new LatLng(topBoundary, rightBoundary)
//                        );
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 12));

                        CameraPosition.Builder positionBuilder = new CameraPosition.Builder();
                        positionBuilder.target(currentLatLong);
                        positionBuilder.zoom(16f);

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(positionBuilder.build()),
                                new GoogleMap.CancelableCallback(){
                                    @Override
                                    public void onFinish() {
                                        placeMarkerOnMap(currentLatLong);
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                    }
                });
    }

    private void placeMarkerOnMap(LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_user_location));
        googleMap.addMarker(markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        if (background != null) {
            background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        }
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        }
        Bitmap bitmap = null;
        if (background != null) {
            bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = null;
        if (bitmap != null) {
            canvas = new Canvas(bitmap);
        }
        if (background != null) {
            if (canvas != null) {
                background.draw(canvas);
            }
        }
        if (vectorDrawable != null) {
            if (canvas != null) {
                vectorDrawable.draw(canvas);
            }
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    /**
     * check google play services
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(this, result, 1).show();
            }
            return false;
        }
        return true;
    }

    /**
     * check GPS Services is on / off
     */

    private void openLocationDialog() {
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!gps_enabled && !network_enabled) {
                // notify user
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Location not enabled")
                        .setPositiveButton("Open Location Settings",
                                (dialog, param) ->
                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();
            }
        } else {
            Utils.showShortToastMessage(binding.relativeLayout, "Location not supported in this device");
        }
    }
}