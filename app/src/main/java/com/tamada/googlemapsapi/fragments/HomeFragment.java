package com.tamada.googlemapsapi.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.app.AppController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment
        implements Response.Listener<String>, Response.ErrorListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraMoveStartedListener,
        OnMapReadyCallback, LocationListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener,
        GoogleApiClient.ConnectionCallbacks, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TabLayout tabLayout;

    private MapView mMapView;
    private Dialog fetchDateTimeDialog;
    private double userLatitude, userLongitude, currentLatitude, currentLongitude, latitude, longitude, destLatitude, destLongitude;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private String strCabType;
    private String strTabTitles[]={"zoom",
            "fence",
            "navigation",
            "pins"};
    private int tabIcons[]={R.drawable.ic_zoom,
            R.drawable.ic_fence,
            R.drawable.ic_navigation,
            R.drawable.ic_map_pins};

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        tabLayout = (TabLayout) view.findViewById(R.id.idTabLayout);

        tabLayout.setTabTextColors(getResources().getColor(R.color.colorTabSelect), getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorWhite));

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(AppController.getInstance().getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                showMap();
            }
        } else {
            showMap();
        }

        prepareCabTabInfo();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    tab.select();
                    if (tab.getCustomView() != null && tab.getText() != null)
                        strCabType = tab.getText().toString();
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


        return view;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        isAdded();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(AppController.getInstance().getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                // Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(AppController.getInstance().getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }



    private void showMap() {
        try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        isAdded();
        mGoogleApiClient = new GoogleApiClient.Builder(AppController.getInstance().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).addApi(Places.GEO_DATA_API).enableAutoManage(getActivity(), 0 /* clientId */, this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(AppIndex.API).build();
        createLocationRequest();
    }


    //get address latitude and longitude
    private Barcode.GeoPoint getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(AppController.getInstance().getApplicationContext());
        try {
            ArrayList<android.location.Address> addresses = (ArrayList<android.location.Address>) coder.getFromLocationName(strAddress, 50);
            if (addresses != null) {
                for (android.location.Address add : addresses) {
                    if (add != null) {
                        //Controls to ensure it is right address such as country etc.
                        currentLatitude = add.getLatitude();
                        currentLongitude = add.getLongitude();
                        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                        //  Log.e("lat,long"," "+currentLatitude+"/ "+currentLongitude);
                    }
                }
            }
        } catch (IOException e) {/*
            AppController.getInstance().trackException(e);
            getCurrentLatLongFromAddress(strAddress, "source");*/
        }
        return null;
    }

    //get address latitude and longitude
    private Barcode.GeoPoint getDestinationLatLong(String strAddress) {
        Geocoder coder = new Geocoder(AppController.getInstance().getApplicationContext());
        try {
            ArrayList<android.location.Address> addresses = (ArrayList<android.location.Address>) coder.getFromLocationName(strAddress, 50);
            if (addresses != null) {
                for (android.location.Address add : addresses) {
                    if (add != null) {
                        //Controls to ensure it is right address such as country etc.
                        destLatitude = add.getLatitude();
                        destLongitude = add.getLongitude();
                    }
                }
            }
        } catch (IOException e) {/*
            AppController.getInstance().trackException(e);
            getCurrentLatLongFromAddress(strAddress, "dest");
        */}
        return null;
    }

    private void initializeMap() {
        MapsInitializer.initialize(AppController.getInstance().getApplicationContext());
        // Check if we were successful in obtaining the map.
        if (googleMap != null) {
            setUpMap();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setPadding(0, 0, 0, 50);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(AppController.getInstance().getApplicationContext(),
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
        this.googleMap.setOnCameraIdleListener(this);
        this.googleMap.setOnCameraMoveStartedListener(this);
        this.googleMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            userLatitude = location.getLatitude();
            userLongitude = location.getLongitude();
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
                    userLatitude = mLastLocation.getLatitude();
                    userLongitude = mLastLocation.getLongitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    if (googleMap != null)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /*private void drawMarker(LatLng latLng, String carType, Float locationDirection) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        switch (carType) {
            case "0":
            case "mini":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.top_mini_35));
                break;
            case "suv":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.top_suv_35));
                break;
            case "sedan":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.top_sedan_35));
                break;
            case "tino":
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.top_tino_35));
                break;

        }
        Marker marker = googleMap.addMarker(markerOptions);
        marker.setRotation(locationDirection);
        marker.showInfoWindow();
    }
*/
    private void setUpMap() {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        isAdded();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAdded();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(getActivity());
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
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(AppController.getInstance().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    onLocationChanged(location);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                isAdded();
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }



    @Override
    public void onCameraIdle() {
        if (googleMap.getCameraPosition().target.latitude != 0.0 && googleMap.getCameraPosition().target.longitude != 0.0) {
            currentLatitude = googleMap.getCameraPosition().target.latitude;
            currentLongitude = googleMap.getCameraPosition().target.longitude;
        }
    }

    private void setUserAddress(double LATITUDE, double LONGITUDE) {
        String strAdd;
        Geocoder geocoder = new Geocoder(AppController.getInstance().getApplicationContext(), Locale.getDefault());
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                android.location.Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                /*lblSearchSource.setText(strAdd);
                lblSearchSource.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);*/
            }
        } catch (Exception error) {
           // AppController.getInstance().trackException(error);
        }
    }



    /*@Override
    public void onCameraMoveStarted(int i) {
        lblSearchSource.setText(AppController.getInstance().getApplicationContext().getResources().getString(R.string.hint_fetch_your_location));
        lblSearchSource.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0);
    }*/



    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    private void prepareCabTabInfo() {
            for (int i = 0; i < strTabTitles.length; i++) {
                View tabView = getActivity().getLayoutInflater().inflate(R.layout.tabitem, null);
                ImageView imgTabIcon = (ImageView) tabView.findViewById(R.id.tabIcon);
                TextView lblTabTitle = (TextView) tabView.findViewById(R.id.tabTitle);
                lblTabTitle.setText(strTabTitles[i]);
                imgTabIcon.setImageResource(tabIcons[i]);
                tabLayout.addTab(tabLayout.newTab().setCustomView(tabView).setText(strTabTitles[i]));
            }
    }
}
