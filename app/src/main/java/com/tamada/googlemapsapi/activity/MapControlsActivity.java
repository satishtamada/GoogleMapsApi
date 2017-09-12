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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.app.AppController;

public class MapControlsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
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
    private double currentLatitude, userLat, userLong;
    private double currentLongitude;
    private GoogleMap googleMap;
    private FloatingActionButton fabCurrentLocation, fabZoomIn, fabZoomOut, fabSwipeUp, fabSwipeDown;
    private ToggleButton toggleTraffic;
    private ImageView imgRightArrow, imgLeftArrow, imgDownArrow, imgUpArrow;
    private static final int SCROLL_BY_PX = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_controls);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.idRadioGroup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Map Controls");
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        fabCurrentLocation = (FloatingActionButton) findViewById(R.id.idCurrentLocation);
        toggleTraffic = (ToggleButton) findViewById(R.id.idToggleTraffic);
        fabZoomIn = (FloatingActionButton) findViewById(R.id.idZoomIn);
        fabZoomOut = (FloatingActionButton) findViewById(R.id.idZoomOut);
        fabSwipeDown = (FloatingActionButton) findViewById(R.id.idSwipeDown);
        fabSwipeUp = (FloatingActionButton) findViewById(R.id.idSwipeUp);
        imgRightArrow = (ImageView) findViewById(R.id.idRightArrow);
        imgLeftArrow = (ImageView) findViewById(R.id.idLeftArrow);
        imgDownArrow = (ImageView) findViewById(R.id.idDownArrow);
        imgUpArrow = (ImageView) findViewById(R.id.idUpArrow);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        accessMap();

        fabCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userLat != 0.0) {
                    LatLng latLng = new LatLng(userLat, userLong);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                    googleMap.animateCamera(cameraUpdate);
                }
            }
        });
        toggleTraffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    assert googleMap != null;
                    googleMap.setTrafficEnabled(true);
                } else {
                    assert googleMap != null;
                    googleMap.setTrafficEnabled(false);
                }
            }
        });

        fabZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        fabZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton1) {
                    assert googleMap != null;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (checkedId == R.id.radioButton2) {
                    assert googleMap != null;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.radioButton3) {
                    assert googleMap != null;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (checkedId == R.id.radioButton4) {
                    assert googleMap != null;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            }
        });
        fabSwipeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(googleMap!=null) {
                    CameraPosition currentCameraPosition = googleMap.getCameraPosition();
                    float currentTilt = currentCameraPosition.tilt;
                    float newTilt = currentTilt + 10;
                    newTilt = (newTilt > 90) ? 90 : newTilt;
                    CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                            .tilt(newTilt).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
        fabSwipeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(googleMap!=null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(currentLatitude, currentLongitude)).zoom(15.5f).bearing(300).tilt(50).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
        imgRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.scrollBy(-SCROLL_BY_PX, 0));
            }
        });
        imgLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.scrollBy(SCROLL_BY_PX, 0));
            }
        });
        imgUpArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.scrollBy(0, -SCROLL_BY_PX));
            }
        });
        imgDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert googleMap != null;
                googleMap.animateCamera(CameraUpdateFactory.scrollBy(0, SCROLL_BY_PX));
            }
        });
    }


    private void accessMap() {
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(AppController.getInstance().getApplicationContext(), PERMISSION_ACCESS_FINE_LOCATION);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            boolean showRequestAgain = ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_ACCESS_FINE_LOCATION);
            // Log.e(TAG, "showRequestAgain: " + showRequestAgain);
            if (showRequestAgain) {
                new AlertDialog.Builder(MapControlsActivity.this).setMessage("Map requires access location permission")
                        .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapControlsActivity.this, new String[]{PERMISSION_ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ACCESS_FINE_LOCATION);
                            }
                        }).setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapControlsActivity.this, new String[]{PERMISSION_ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
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
            userLat = location.getLatitude();
            userLong = location.getLongitude();
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
}
