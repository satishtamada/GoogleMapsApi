package com.tamada.googlemapsapi.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tamada.googlemapsapi.R;
import com.tamada.googlemapsapi.app.AppController;
import com.tamada.googlemapsapi.receivers.ConnectivityReceiver;
import com.tamada.googlemapsapi.receivers.GPSConnectivityReceiver;

/**
 * Created by inventbird on 21/7/16.
 */

public class SplashScreenActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener, GPSConnectivityReceiver.GPSConnectivityReceiverListener {
    private TextView lblNoInternetConnection;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private String RegistrationID, apiKey;
    private LinearLayout layoutDeniedPermissionLayout, layoutUpdateApp;
    private TextView lblUpdateAppTag;
    private String strAppVersionCode;
    private RelativeLayout relativeLayout;
    private Button btnAppUpdate;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private ImageView imgAppLogo;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private String deviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash_screen);
        imgAppLogo = (ImageView) findViewById(R.id.idImgLogo);
        lblNoInternetConnection = (TextView) findViewById(R.id.lbl_no_net_connection);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(AppController.getInstance().getApplicationContext(), R.color.colorPrimary));
        }
        AppController.getInstance().setConnectivityListener(this);
        AppController.getInstance().setGpsConnectivityListener(this);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        if (!ConnectivityReceiver.isConnected()) {
            lblNoInternetConnection.setVisibility(View.VISIBLE);
        } else {
            lblNoInternetConnection.setVisibility(View.GONE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                accessDevicePermissions();
            } else {
                 launchMainActivity();
            }
        }
        if (!GPSConnectivityReceiver.isGPSTurnOn(AppController.getInstance().getApplicationContext())) {
            showGPSDialog();
        }
    }


    /**
     * Method request to server for location permissions
     */
    private void accessDevicePermissions() {
        if (ActivityCompat.checkSelfPermission(SplashScreenActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(SplashScreenActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(SplashScreenActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[2])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("Google maps api needs Location and Storage permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        layoutDeniedPermissionLayout.setVisibility(View.VISIBLE);
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("Google maps api needs Location and Storage permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        layoutDeniedPermissionLayout.setVisibility(View.VISIBLE);
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(SplashScreenActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            //TODO launch main activity once got locations
            launchMainActivity();
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(SplashScreenActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                launchMainActivity();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectivityReceiver.isConnected()) {
            lblNoInternetConnection.setVisibility(View.GONE);
        } else {
            lblNoInternetConnection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if (allgranted) {
                launchMainActivity();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SplashScreenActivity.this, permissionsRequired[2])) {
                layoutDeniedPermissionLayout.setVisibility(View.VISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("Google maps api needs Location and  Storage permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        layoutDeniedPermissionLayout.setVisibility(View.VISIBLE);
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(SplashScreenActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                launchMainActivity();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            lblNoInternetConnection.setVisibility(View.GONE);
        } else {
            lblNoInternetConnection.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onGpsStatusChanged(boolean isConnected) {
        if (!isConnected) {
            showGPSDialog();
        }
    }

    private void showGPSDialog() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme)) // Theme
                .setTitle(R.string.gps_lable_gps) // setTitle
                .setMessage(R.string.gps_lable_warning_message) // setMessage
                .setInverseBackgroundForced(false).setCancelable(false) //
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.cancel();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.cancel();
                finish();
            }
        }).setIcon(R.drawable.ic_map_pin).show();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(AppController.getInstance().getApplicationContext(), ListActivity.class);
        startActivity(intent);
        finish();
    }
}
