package com.tamada.googlemapsapi.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.app.AppController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MapPinsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = MapDirectionsActivity.class.getSimpleName();
    private MapView mMapView;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 102;
    private static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LatLng CURRENT_LATLNG;
    private LatLng DEST_LATLNG;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;


    private GoogleMap googleMap;
    private TabLayout tabLayout;
    private LinearLayout pinColorLayout;
    private String strCabType;

    private String strTabTitles[] = {"cabs",
            "petrol",
            "restaurants",
            "Colors"};
    private int tabIcons[] = {R.drawable.ic_tab_cabs,
            R.drawable.ic_tab_petrol,
            R.drawable.ic_tab_restarunts,
            R.drawable.ic_map_pins};
    private MarkerOptions marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_pins);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Map pins");
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        accessMap();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.idRadioGroup);
        pinColorLayout = (LinearLayout) findViewById(R.id.idPinColorLayout);
        tabLayout = (TabLayout) findViewById(R.id.idTabLayout);
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorTabSelect), getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorWhite));
        prepareTabInfo();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    tab.select();
                    if (tab.getCustomView() != null && tab.getText() != null)
                        strCabType = tab.getText().toString();
                    if(strCabType.equals("Colors")){
                        if(googleMap!=null){
                            googleMap.clear();
                        }
                        pinColorLayout.setVisibility(View.VISIBLE);
                        marker = new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Hello Maps");
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        googleMap.addMarker(marker);
                    }else{
                        if(pinColorLayout.getVisibility()==View.VISIBLE){
                            pinColorLayout.setVisibility(View.GONE);
                        }
                        if(googleMap!=null)
                            googleMap.clear();
                        setPinsOnMap(strCabType);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab != null) {
                    if (tab.getCustomView() != null && tab.getText() != null)
                        strCabType = tab.getText().toString();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton1) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    googleMap.addMarker(marker);
                } else if (checkedId == R.id.radioButton2) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    googleMap.addMarker(marker);
                } else if (checkedId == R.id.radioButton3) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    googleMap.addMarker(marker);
                } else if (checkedId == R.id.radioButton4) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    googleMap.addMarker(marker);
                } else{
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    googleMap.addMarker(marker);
                }
            }
        });


    }

    private void setPinsOnMap(String strCabType) {
        for(int i=0;i<10;i++){
            drawMarker(getRandomLocation(new LatLng(currentLatitude,currentLongitude),2000),strCabType);
        }
    }


    private void accessMap() {
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(AppController.getInstance().getApplicationContext(), PERMISSION_ACCESS_FINE_LOCATION);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            boolean showRequestAgain = ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_ACCESS_FINE_LOCATION);
            // Log.e(TAG, "showRequestAgain: " + showRequestAgain);
            if (showRequestAgain) {
                new AlertDialog.Builder(MapPinsActivity.this).setMessage("Map requires access location permission")
                        .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapPinsActivity.this, new String[]{PERMISSION_ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ACCESS_FINE_LOCATION);
                            }
                        }).setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapPinsActivity.this, new String[]{PERMISSION_ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
                return;
            }
        }
        showMap();
    }

    private void showMap() {
        try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMap() {
        MapsInitializer.initialize(AppController.getInstance().getApplicationContext());
        // Check if we were successful in obtaining the map.
        if (googleMap != null) {
            setUpMap();
        }
    }

    private void setUpMap() {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void createLocationRequest() {
        // Create the LocationRequest object
        int UPDATE_INTERVAL = 10000;
        int FATEST_INTERVAL = 5000;
        int DISPLACEMENT = 50;
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 10 seconds, in milliseconds
                .setFastestInterval(FATEST_INTERVAL).setSmallestDisplacement(DISPLACEMENT); // 1 second, in milliseconds
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(AppController.getInstance().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).addApi(Places.GEO_DATA_API).enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(AppIndex.API).build();
        createLocationRequest();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                // Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(AppController.getInstance().getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onResume() {
        //  Log.d("Case", "onResume");
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause() {
        // Log.d("Case", "onPause");
        super.onPause();
        mMapView.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        // Log.d("Case", "onDestroy");
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        mMapView.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    onLocationChanged(location);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                }
                this.googleMap.setMyLocationEnabled(true);
            }
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
            }
            this.googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            if (googleMap != null)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        } else {
            try {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {
                    currentLatitude = mLastLocation.getLatitude();
                    currentLongitude = mLastLocation.getLongitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    if (googleMap != null)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareTabInfo() {
        for (int i = 0; i < strTabTitles.length; i++) {
            View tabView = this.getLayoutInflater().inflate(R.layout.tabitem, null);
            ImageView imgTabIcon = (ImageView) tabView.findViewById(R.id.tabIcon);
            TextView lblTabTitle = (TextView) tabView.findViewById(R.id.tabTitle);
            lblTabTitle.setText(strTabTitles[i]);
            imgTabIcon.setImageResource(tabIcons[i]);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabView).setText(strTabTitles[i]));
        }
    }

    private void drawMarker(LatLng latLng, String carType) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        switch (carType) {
            case "cabs":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_car));
                break;
            case "petrol":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_gas));
                break;
            case "restaurants":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_restaurant));
                break;

        }
        Marker marker = googleMap.addMarker(markerOptions);
        marker.showInfoWindow();
    }


    public LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        //This is to generate 10 random points
        for(int i = 0; i<10; i++) {
            double x0 = point.latitude;
            double y0 = point.longitude;

            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
            randomPoints.add(randomLatLng);
            Location l1 = new Location("");
            l1.setLatitude(randomLatLng.latitude);
            l1.setLongitude(randomLatLng.longitude);
            randomDistances.add(l1.distanceTo(myLocation));
        }
        //Get nearest point to the centre
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }
}
