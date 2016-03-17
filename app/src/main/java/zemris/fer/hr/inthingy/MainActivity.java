package zemris.fer.hr.inthingy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import com.guna.libmultispinner.MultiSelectionSpinner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zemris.fer.hr.inthingy.custom.EmptyTabFactory;
import zemris.fer.hr.inthingy.custom.PopulateMultiSelectionSpinnerTask;
import zemris.fer.hr.inthingy.gps.GPSLocator;
import zemris.fer.hr.inthingy.utils.Constants;

/**
 * Activity for displaying main screen. It provides user options to send new message or to see received messages.
 * When application is loaded, it needs to populate {@link com.guna.libmultispinner.MultiSelectionSpinner} with
 * available sensors on device.
 * It handles sending messages where user needs to choose sensor which data will be sent
 */
public class MainActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener,
        View.OnClickListener {

    /** Tab host for sensor data. */
    private TabHost tabHost;
    /** TextView for displaying sensor data. */
    private TextView tvSensorData;
    /** Button for sending message. */
    private Button bSendMessage;
    /** List of sensors and its data. */
    private Map<String, String> sensorDataMap;
    /** Multi selection spinner for sensors. */
    private MultiSelectionSpinner spDeviceSensors;
    /** GPS service Intent. */
    private Intent gpsService;
    /** Unique device ID. */
    public static final String THING_ID = "1234567890";
    /** Default sensor data. */
    private static final String DEFAULT_SENSOR_DATA = "NULL\nNULL\nNULL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorDataMap = new HashMap<>();
        initializeSensorSpinner();
        initializeElements();
        gpsService = new Intent(this, GPSLocator.class);
    }

    /**
     * Method for initializing spinner with list of sensor's that are available for current device.
     */
    private void initializeSensorSpinner() {
        //init spinner
        spDeviceSensors = (MultiSelectionSpinner) findViewById(R.id.spDeviceSensors);
        spDeviceSensors.setListener(MainActivity.this);
    }

    /**
     * Method for initializing global elements which are used in this class.
     */
    private void initializeElements() {
        //tabhost
        initTabHost();
        //device id
        ((TextView) findViewById(R.id.tvThingID)).setText(THING_ID);
        //textview for sensor data
        tvSensorData = (TextView) findViewById(R.id.tvSensorData);
        //buttons
        initButtons();
    }

    /**
     * Method for initialization of all buttons (setting onClickListener, etc)
     */
    private void initButtons() {
        bSendMessage = (Button) findViewById(R.id.bSendMessage);
        int[] buttonIds = new int[]{R.id.bCheckSensors, R.id.bChooseDestination, R.id.bPreviewMessage,
                R.id.bSendMessage, R.id.bStartService, R.id.bStopService};
        for (int buttonId : buttonIds) {
            findViewById(buttonId).setOnClickListener(MainActivity.this);
        }
    }

    /**
     * Method for {#link tabHost} initialization.
     */
    private void initTabHost() {
        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                String text = "Sensor: " + tabId + "\n" + sensorDataMap.get(tabId);
                tvSensorData.setText(text);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //check location permission
        checkLocationPermissions();
        //run task to populate spinner with proper data
        (new PopulateMultiSelectionSpinnerTask(MainActivity.this, spDeviceSensors)).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopService(gpsService);
    }

    /**
     * Method for checking location permissions. If user don't give location permission, app will automatically close.
     */
    private void checkLocationPermissions() {
        boolean locationPerm1 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean locationPerm2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        if (locationPerm1 && locationPerm2) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            //start gps service
            startService(gpsService);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success! run service
                startService(gpsService);
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(MainActivity.this, "Application don't have location permission, please enable it",
                        Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    //
    // HANDLING SPINNER CHECKED INDICES
    //
    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(final List<String> strings) {
        //first clear all tabHost
        tabHost.clearAllTabs();
        //clear text view for sensor data
        tvSensorData.setText(DEFAULT_SENSOR_DATA);
        //shared preferences
        if (strings != null && strings.size() > 0) {
            for (int i = 0; i < strings.size(); ++i) {
                String name = strings.get(i);
                //populate map with data
                String value = MultiprocessPreferences.
                        getDefaultSharedPreferences(getApplicationContext()).getString(name, DEFAULT_SENSOR_DATA);
                sensorDataMap.put(name, value);
                //add new tabHost
                TabHost.TabSpec spec = tabHost.newTabSpec(name);
                spec.setContent(new EmptyTabFactory(MainActivity.this));
                spec.setIndicator(String.valueOf(i + 1));
                tabHost.addTab(spec);
            }
        } else {  //make send button disabled (if it is enabled)
            bSendMessage.setEnabled(false);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCheckSensors:
                //get new values
                for (String name : spDeviceSensors.getSelectedStrings()) {
                    String value = MultiprocessPreferences.
                            getDefaultSharedPreferences(getApplicationContext()).getString(name, DEFAULT_SENSOR_DATA);
                    sensorDataMap.put(name, value);
                }
                if (spDeviceSensors.getSelectedStrings().size() > 0) {
                    //update value
                    String tabName = tabHost.getCurrentTabTag();
                    String text = "Sensor: " + tabName + "\n" + sensorDataMap.get(tabName);
                    tvSensorData.setText(text);
                }
                break;
            case R.id.bChooseDestination:
                break;
            case R.id.bPreviewMessage:
                bSendMessage.setEnabled(true);
                break;
            case R.id.bSendMessage:
                bSendMessage.setEnabled(false);
                break;
            case R.id.bStopService:
                findViewById(R.id.bStartService).setEnabled(true);
                break;
            case R.id.bStartService:
                findViewById(R.id.bStartService).setEnabled(false);
                break;
            default:
                //some error
                break;
        }
    }

}
