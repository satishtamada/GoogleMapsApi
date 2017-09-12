package com.tamada.googlemapsapi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by inventbird on 15/6/16.
 */
public class GPSConnectivityReceiver extends BroadcastReceiver {
    public static GPSConnectivityReceiverListener gpsConnectivityReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsConnectivityReceiverListener != null) {
            gpsConnectivityReceiverListener.onGpsStatusChanged(isGpsEnabled);
        }
    }

    public static boolean isGPSTurnOn(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public interface GPSConnectivityReceiverListener {
        void onGpsStatusChanged(boolean isConnected);
    }
}
