package zemris.fer.hr.inthingy.gps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;

import zemris.fer.hr.inthingy.utils.Constants;

/**
 * Class for finding GPS location data. It uses GPS service or Network service, depending on which one is available and
 * more accurate.
 * It handles permissions which application needs to have to access those data.
 * If both, GPS and Network, are not enabled, user will have to enable them if he wants to get this data.
 */
public class GPSLocator extends Service {
    /** Manager for location */
    private LocationManager locationManager = null;
    /** Constant for location time update interval. */
    private static final int MIN_TIME_BW_UPDATES = 4000; //4 sec
    /** Constant for location move interval. */
    private static final float MIN_DISTANCE_CHANGE = 50; //50m
    /** GPS location listener. */
    private MyLocationListener gpsLocationListener = new MyLocationListener(LocationManager.GPS_PROVIDER);
    /** Network location listener. */
    private MyLocationListener networkLocationListener = new MyLocationListener(LocationManager.NETWORK_PROVIDER);
    /** Flags for location permissions. */
    private boolean locationPerm1, locationPerm2;
    /** Flag to check if GPS is enabled. */
    private boolean isGPSEnabled = false;
    /** Flag for checking if network is enabled. */
    private boolean isNetworkEnabled = false;
    /** Variable for storing last known location. */
    private Location lastLocation;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        initialization();
        locationPerm1 = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        locationPerm2 = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (locationPerm1 && locationPerm2) {
            this.stopSelf();
        }
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE,
                    gpsLocationListener);
        }
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE,
                    networkLocationListener);
        }
    }

    /**
     * Method for initializing some stuff.
     */
    private void initialization() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationPerm1 = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            locationPerm2 = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            locationManager.removeUpdates(gpsLocationListener);
            locationManager.removeUpdates(networkLocationListener);
        }
    }


    /**
     * Class which will provide location, it implements {@link android.location.LocationListener}
     */
    private class MyLocationListener implements LocationListener {

        /**
         * Constructor with one parameter.
         *
         * @param provider
         *         name of the location provider
         */
        public MyLocationListener(String provider) {
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            lastLocation.set(location);
            StringBuilder locationString = new StringBuilder();
            locationString.append("Latitude: ").append(location.getLatitude()).append("\n")
                    .append("Longitude: ").append(location.getLongitude()).append("\n")
                    .append("Altitude: ").append(location.getAltitude());
            MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                    .edit().putString(Constants.GPS_SENSOR_NAME, locationString.toString()).apply();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}