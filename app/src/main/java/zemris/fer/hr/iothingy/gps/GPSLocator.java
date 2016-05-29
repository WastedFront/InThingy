package zemris.fer.hr.iothingy.gps;

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
import android.widget.Toast;
import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import zemris.fer.hr.iothingy.R;
import zemris.fer.hr.iothingy.utils.Constants;

/**
 * Class for finding GPS location data. It uses GPS service or Network service, depending on which one is available and
 * more accurate. It handles permissions which application needs to have to access those data in a way that it shows
 * user what the problem is, but it doesn't prompt for permissions. If both, GPS and Network, are not enabled, user will
 * have to enable them if he wants to get this data. Data is stored in
 * {@link com.gdubina.multiprocesspreferences.MultiprocessPreferences.MultiprocessSharedPreferences}.
 */
public class GPSLocator extends Service {
    /** Manager for location */
    private LocationManager locationManager = null;
    /** Constant for location time update interval. */
    private static final int MIN_TIME_BW_UPDATES = 4000; //4 sec
    /** Constant for location move interval. */
    private static final float MIN_DISTANCE_CHANGE = 50; //50 m
    /** GPS location listener. */
    private MyLocationListener gpsLocationListener = new MyLocationListener(LocationManager.GPS_PROVIDER);
    /** Network location listener. */
    private MyLocationListener networkLocationListener = new MyLocationListener(LocationManager.NETWORK_PROVIDER);
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
        //permission check
        boolean locationPerm1 = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean locationPerm2 = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (locationPerm1 && locationPerm2) {
            Toast.makeText(getApplicationContext(), R.string.error_no_location_perms,
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
        //check which connectivity you can use to get data
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE,
                    networkLocationListener);
        } else if (isGPSEnabled && !locationPerm1 && !locationPerm2) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE,
                    gpsLocationListener);
        }

        //if you can't get data, stop service
        if ((!isGPSEnabled || locationPerm1 || locationPerm2) && !isNetworkEnabled) {
            Toast.makeText(getApplicationContext(), R.string.error_cant_get_location,
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    /**
     * Method for initializing location manager and checking if there is gps or network online. It is also used to store
     * default sensor value into {@code MultiprocessSharedPreferences}.
     */
    private void initialization() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        StringBuilder locationString = new StringBuilder();
        locationString.append(getString(R.string.latitude)).append(": ")
                .append(0.0).append(" \u00B0\n")
                .append(getString(R.string.longitude)).append(": ")
                .append(0.0).append(" \u00B0\n")
                .append(getString(R.string.altitude)).append(": ")
                .append(0.0).append(" \u00B0");
        MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString(Constants.GPS_SENSOR_NAME, locationString.toString()).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //check permissions
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //do nothing
            }
            //unsubsrcibe from location manager
            locationManager.removeUpdates(gpsLocationListener);
            locationManager.removeUpdates(networkLocationListener);
        }
    }


    /**
     * Class which will provide location, it implements {@link android.location.LocationListener}. When the location is
     * changed, it's new value is stored in {@link MultiprocessPreferences}.
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
            //make value
            StringBuilder locationString = new StringBuilder();
            locationString.append(getString(R.string.latitude)).append(": ")
                    .append(location.getLatitude()).append(" \u00B0\n")
                    .append(getString(R.string.longitude)).append(": ")
                    .append(location.getLongitude()).append(" \u00B0\n")
                    .append(getString(R.string.altitude)).append(": ")
                    .append(location.getAltitude()).append(" \u00B0");
            //store value
            MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                    .edit().putString(Constants.GPS_SENSOR_NAME, locationString.toString()).apply();
        }

        @Override
        public void onProviderDisabled(String provider) {
            //do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            //do nothing
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //do nothing
        }
    }
}