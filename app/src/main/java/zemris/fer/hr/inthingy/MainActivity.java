package zemris.fer.hr.inthingy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import com.guna.libmultispinner.MultiSelectionSpinner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zemris.fer.hr.inthingy.custom.DataForSpinnerTask;
import zemris.fer.hr.inthingy.custom.EmptyTabFactory;
import zemris.fer.hr.inthingy.custom.MyUtils;
import zemris.fer.hr.inthingy.gps.GPSLocator;
import zemris.fer.hr.inthingy.messages.MessageReplyService;
import zemris.fer.hr.inthingy.sensors.DeviceSensors;
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
    /** List of sensors and its data. */
    private Map<String, String> sensorDataMap = new HashMap<>();
    /** Multi selection spinner for sensors. */
    private MultiSelectionSpinner spDeviceSensors;
    /** Edit text which contains address of destination. */
    private EditText etDestination;
    /** GPS service intent. */
    private Intent gpsService;
    /** Sensor service intent. */
    private Intent sensorService;
    /** Auto reply service intent. */
    private Intent autoReplyService;
    /** Unique device ID. */
    public String thingId;
    /** Flag to check if auto reply is on or off. */
    private boolean flagAutoReply = false;
    /** Default sensor data. */
    private static final String DEFAULT_SENSOR_DATA = "NULL\nNULL\nNULL";
    /** Empty tab name. */
    private static final String EMPTY_TAB_TAG = "EMPTY";
    /** Key for saving found sensors. */
    private static final String KEY_FOUND_SENSORS = "FOUND_SENSORS";
    /** Key for saving selected sensors. */
    private static final String KEY_SEL_SENSORS = "SELECTED_SENSORS";
    /** Key auto reply service running. */
    private static final String KEY_AUTO_REPLY_ON = "AUTOREPLY_ON";
    /** Key for destination string. */
    private static final String KEY_DESTINATION = "DESTINATION";
    /** Key for encryption string. */
    private static final String KEY_ENCRYPTION = "ENCRYPTION";
    /** Key for send mode string. */
    private static final String KEY_SEND_MODE = "SEND_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thingId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        initializeSensorSpinner();
        initializeElements();
        gpsService = new Intent(this, GPSLocator.class);
        sensorService = new Intent(this, DeviceSensors.class);
        autoReplyService = new Intent(this, MessageReplyService.class);
        if (savedInstanceState == null) {
            //delete shared preferences; no need for apply()
            MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext()).edit().clear();
            //run task to populate spinner with proper data
            (new DataForSpinnerTask(MainActivity.this, spDeviceSensors)).execute();
        }
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
        //tab host
        initTabHost();
        //device id
        ((TextView) findViewById(R.id.tvThingID)).setText(thingId);
        //text view for sensor data
        tvSensorData = (TextView) findViewById(R.id.tvSensorData);
        //edit text for destination
        etDestination = (EditText) findViewById(R.id.etDestination);
        //buttons
        initButtons();
    }

    /**
     * Method for initialization of all buttons (setting onClickListener, etc)
     */
    private void initButtons() {
        int[] buttonIds = new int[]{R.id.bCheckSensors, R.id.bChooseDestination, R.id.bPreviewMessage,
                R.id.bSendMessage};
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
                if (!EMPTY_TAB_TAG.equals(tabId)) {
                    String text = MainActivity.this.getString(R.string.name_sensor) + ": " + tabId + "\n" + sensorDataMap.get(tabId);
                    tvSensorData.setText(text);
                }
            }
        });
        //add some tab
        addTabHostEmptyTab();
    }

    /**
     * Method for creating empty tab for tab host.
     */
    private void addTabHostEmptyTab() {
        TabHost.TabSpec spec = tabHost.newTabSpec(EMPTY_TAB_TAG);
        spec.setContent(new EmptyTabFactory(MainActivity.this));
        spec.setIndicator(EMPTY_TAB_TAG);
        tabHost.addTab(spec);
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(gpsService);
        startService(sensorService);
        if (flagAutoReply) {
            startService(autoReplyService);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopService(gpsService);
        stopService(sensorService);
        stopService(autoReplyService);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(KEY_FOUND_SENSORS, spDeviceSensors.getStringData());
        outState.putStringArray(KEY_SEL_SENSORS,
                spDeviceSensors.getSelectedStrings().toArray(new String[spDeviceSensors.getSelectedStrings().size()]));
        outState.putBoolean(KEY_AUTO_REPLY_ON, flagAutoReply);
        outState.putString(KEY_DESTINATION, ((EditText) findViewById(R.id.etDestination)).getText().toString());
        outState.putInt(KEY_ENCRYPTION, ((Spinner) findViewById(R.id.spEncryption)).getSelectedItemPosition());
        outState.putInt(KEY_SEND_MODE, ((Spinner) findViewById(R.id.spSendMode)).getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        spDeviceSensors.setItems(savedInstanceState.getStringArray(KEY_FOUND_SENSORS));
        spDeviceSensors.setSelection(savedInstanceState.getStringArray(KEY_SEL_SENSORS));
        updateTabHostWithData();
        flagAutoReply = savedInstanceState.getBoolean(KEY_AUTO_REPLY_ON);
        selectedStrings(spDeviceSensors.getSelectedStrings());
        ((EditText) findViewById(R.id.etDestination)).setText(savedInstanceState.getString(KEY_DESTINATION));
        ((Spinner) findViewById(R.id.spEncryption)).setSelection(savedInstanceState.getInt(KEY_ENCRYPTION));
        ((Spinner) findViewById(R.id.spSendMode)).setSelection(savedInstanceState.getInt(KEY_SEND_MODE));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //do nothing else
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (flagAutoReply) {
            menu.findItem(R.id.menuAutoReply).setIcon(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.icon_auto_reply_on));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //service for automatic message reply
        if (id == R.id.menuAutoReply) {
            Drawable offIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_auto_reply_off);
            Drawable onIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_auto_reply_on);
            if (item.getIcon().getConstantState().equals(onIcon.getConstantState())) {
                item.setIcon(offIcon);
                Toast.makeText(getApplicationContext(), R.string.text_autoreply_off, Toast.LENGTH_SHORT).show();
                if (flagAutoReply) {
                    stopService(autoReplyService);
                    flagAutoReply = false;
                }
            } else {
                item.setIcon(onIcon);
                Toast.makeText(getApplicationContext(), R.string.text_autoreply_on, Toast.LENGTH_SHORT).show();
                if (!flagAutoReply) {
                    startService(autoReplyService);
                    flagAutoReply = true;
                }
            }
        } else if (id == R.id.menuShowMessages) { //button for showing messages
            //some fragment to show messages
        }
        return super.onOptionsItemSelected(item);
    }

    //
    // HANDLING SPINNER CHECKED INDICES
    //
    @Override
    public void selectedIndices(List<Integer> indices) {
        //empty stuff
    }

    @Override
    public void selectedStrings(final List<String> strings) {
        //first clear all tabHost
        tabHost.clearAllTabs();
        //clear data from map
        sensorDataMap.clear();
        //clear text view for sensor data
        tvSensorData.setText(R.string.text_sensor_data_default);
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
        } else {  //no selection
            addTabHostEmptyTab();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCheckSensors:
                checkSensors();
                break;
            case R.id.bChooseDestination:
                chooseDestination();
                break;
            case R.id.bPreviewMessage:
                if (checkAllParameters()) {
                    previewMessage();
                }
                break;
            case R.id.bSendMessage:
                if (checkAllParameters()) {
                    String encryption = ((Spinner) findViewById(R.id.spEncryption)).getSelectedItem().toString();
                    String sendMode = ((Spinner) findViewById(R.id.spSendMode)).getSelectedItem().toString();
                    String destination = etDestination.getText().toString();
                    String source = MyUtils.getLocalIpAddress();
                    MyUtils.sendMessage(MainActivity.this, thingId, source, destination, encryption, sendMode, sensorDataMap);
                }
                break;
            default:
                //some error
                break;
        }
    }


    /**
     * Method for checking if user selected proper values and entered valid destination address.
     *
     * @return true if everything is ok, otherwise false.
     */
    private boolean checkAllParameters() {
        if (sensorDataMap.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.error_no_sensor_selected,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String destination = etDestination.getText().toString();
        if ("".equals(destination)) {
            Toast.makeText(MainActivity.this, R.string.error_no_destination, Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Patterns.IP_ADDRESS.matcher(destination).matches()) {
            Toast.makeText(MainActivity.this, R.string.error_invalid_adress_fromat, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Method for creating dialog which will display some destination addresses.
     */
    private void chooseDestination() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.text_select_destination);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        //some adresses
        arrayAdapter.add("192.168.1.1");

        dialogBuilder.setNegativeButton(MainActivity.this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                etDestination.setText(arrayAdapter.getItem(which));
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    /**
     * Method for creating dialog which will display message which will be sent.
     */
    private void previewMessage() {
        String encryption = ((Spinner) findViewById(R.id.spEncryption)).getSelectedItem().toString();
        String destination = etDestination.getText().toString();
        String source = MyUtils.getLocalIpAddress();
        if (Constants.STRING_ERROR.equals(source)) {
            Toast.makeText(MainActivity.this, R.string.error_cant_get_ip, Toast.LENGTH_LONG).show();
            return;
        }
        String message = MyUtils.createMessage(thingId, source, destination, encryption, sensorDataMap);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle(R.string.text_button_preview_message);
        builderSingle.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setMessage(message);
        builderSingle.show();
    }

    /**
     * Method for checking sensors data changes and if services are running or not.
     * It is used for button bChecksensors.
     */
    private void checkSensors() {
        //if one of service is stopped, run it again
        if (!MyUtils.isServiceRunning(GPSLocator.class, MainActivity.this)) {
            startService(gpsService);
        }
        if (!MyUtils.isServiceRunning(DeviceSensors.class, MainActivity.this)) {
            startService(sensorService);
        }
        //get new values
        updateTabHostWithData();
    }

    /**
     * Method for updating {#link tabHost} with valid data.
     */
    private void updateTabHostWithData() {
        for (String name : spDeviceSensors.getSelectedStrings()) {
            String value = MultiprocessPreferences.
                    getDefaultSharedPreferences(getApplicationContext()).getString(name, DEFAULT_SENSOR_DATA);
            sensorDataMap.put(name, value);
        }
        if (spDeviceSensors.getSelectedStrings().size() > 0) {
            //update value
            String tabName = tabHost.getCurrentTabTag();
            String text = MainActivity.this.getString(R.string.name_sensor) + ": " + tabName + "\n" + sensorDataMap.get(tabName);
            tvSensorData.setText(text);
        }
    }

}
