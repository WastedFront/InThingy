package zemris.fer.hr.inthingy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import com.guna.libmultispinner.MultiSelectionSpinner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import zemris.fer.hr.inthingy.communication.CommUtils;
import zemris.fer.hr.inthingy.communication.MessageReplyService;
import zemris.fer.hr.inthingy.gps.GPSLocator;
import zemris.fer.hr.inthingy.sensors.DeviceSensors;
import zemris.fer.hr.inthingy.utils.Constants;
import zemris.fer.hr.inthingy.utils.EmptyTabFactory;
import zemris.fer.hr.inthingy.utils.MyUtils;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Activity for displaying main screen. It provides user options to send new message or to see received messages.
 * When application is loaded, it needs to populate {@link com.guna.libmultispinner.MultiSelectionSpinner} with
 * available sensors on device.
 * Also it provides to start service for automatic reply to received messages.
 */
public class MainActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener,
        View.OnClickListener {

    /** Tab host for sensor data. */
    private TabHost tabHost;
    /** TextView for displaying sensor data. */
    private TextView tvSensorData;
    /** EditText for device ID and destination information. */
    private EditText etDeviceId, etDestination;
    /** List of sensors and its data. */
    private Map<String, String> sensorDataMap = new HashMap<>();
    /** Multi selection spinner for sensors. */
    private MultiSelectionSpinner spDeviceSensors;
    /** GPS, sensor, auto-reply service intent. */
    private Intent gpsService, sensorService, autoReplyService;
    /** Flag to check if auto reply is on or off. */
    private boolean flagAutoReply = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeElements();
        initializeServices();
        if (savedInstanceState == null) {
            MyUtils.getSensors(MainActivity.this, spDeviceSensors);
        }
    }

    /**
     * Method for initializing services which are used by application.
     */
    private void initializeServices() {
        gpsService = new Intent(this, GPSLocator.class);
        sensorService = new Intent(this, DeviceSensors.class);
        autoReplyService = new Intent(this, MessageReplyService.class);
    }

    /**
     * Method for initializing global elements which are used in this class.
     */
    private void initializeElements() {
        spDeviceSensors = (MultiSelectionSpinner) findViewById(R.id.spDeviceSensors);
        spDeviceSensors.setListener(MainActivity.this);
        tvSensorData = (TextView) findViewById(R.id.tvSensorData);
        etDestination = (EditText) findViewById(R.id.etDestination);
        etDeviceId = (EditText) findViewById(R.id.etThingID);
        String defaultID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        defaultID = defaultID.substring(0, 8);
        etDeviceId.setText(getPreferences(Context.MODE_PRIVATE).getString(Constants.KEY_DEVICE_ID, defaultID));
        int[] buttonIds = new int[]{R.id.bCheckData, R.id.bChooseDestination, R.id.bSendMessage};
        for (int buttonId : buttonIds) {
            findViewById(buttonId).setOnClickListener(MainActivity.this);
        }
        initTabHost();
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
                if (!Constants.EMPTY_TAB_TAG.equals(tabId)) {
                    String text = MainActivity.this.getString(R.string.name_sensor) + ": " + tabId + "\n" + sensorDataMap.get(tabId);
                    tvSensorData.setText(text);
                }
            }
        });
        addTabHostEmptyTab();
    }

    /**
     * Method for creating empty tab for tab host.
     */
    private void addTabHostEmptyTab() {
        TabHost.TabSpec spec = tabHost.newTabSpec(Constants.EMPTY_TAB_TAG);
        spec.setContent(new EmptyTabFactory(MainActivity.this));
        spec.setIndicator(Constants.EMPTY_TAB_TAG);
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
        outState.putString(Constants.KEY_DEVICE_ID, etDeviceId.getText().toString());
        outState.putStringArray(Constants.KEY_FOUND_SENSORS, spDeviceSensors.getStringData());
        outState.putStringArray(Constants.KEY_SEL_SENSORS,
                spDeviceSensors.getSelectedStrings().toArray(new String[spDeviceSensors.getSelectedStrings().size()]));
        outState.putBoolean(Constants.KEY_AUTO_REPLY_ON, flagAutoReply);
        outState.putString(Constants.KEY_DESTINATION, ((EditText) findViewById(R.id.etDestination)).getText().toString());
        outState.putInt(Constants.KEY_ENCRYPTION, ((Spinner) findViewById(R.id.spEncryption)).getSelectedItemPosition());
        outState.putInt(Constants.KEY_SEND_MODE, ((Spinner) findViewById(R.id.spSendMode)).getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        etDeviceId.setText(savedInstanceState.getString(Constants.KEY_DEVICE_ID));
        spDeviceSensors.setItems(savedInstanceState.getStringArray(Constants.KEY_FOUND_SENSORS));
        spDeviceSensors.setSelection(savedInstanceState.getStringArray(Constants.KEY_SEL_SENSORS));
        updateTabHostWithData();
        flagAutoReply = savedInstanceState.getBoolean(Constants.KEY_AUTO_REPLY_ON);
        selectedStrings(spDeviceSensors.getSelectedStrings());
        ((EditText) findViewById(R.id.etDestination)).setText(savedInstanceState.getString(Constants.KEY_DESTINATION));
        ((Spinner) findViewById(R.id.spEncryption)).setSelection(savedInstanceState.getInt(Constants.KEY_ENCRYPTION));
        ((Spinner) findViewById(R.id.spSendMode)).setSelection(savedInstanceState.getInt(Constants.KEY_SEND_MODE));
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
        if (id == R.id.menuAutoReply) {
            activateAutoReply(item);
        } else if (id == R.id.menuShowMessages) {
            try {
                showReceivedMessages();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                //do nothing
            }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for showing received messages. It also provides respond to those messages just by user clicking on them.
     * It makes {@code AlertDialog} with list view in which there are messages.
     * On {@code OnItemClickListener} user can automatically reply to message and on {@code OnItemLongClickListener} user
     * can see full message format.
     */
    private void showReceivedMessages() throws Exception {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getResources().getString(R.string.text_recevied_messages);
        builder.setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"));
        ListView listView = new ListView(this);
        final List<String> msgs = StoringUtils.getReceivedMessages(getApplicationContext());
        String[] messages = new String[msgs.size()];
        for (int i = 0, len = msgs.size(); i < len; ++i) {
            messages[i] = "" + (i + 1) + ". " + MyUtils.getReceivedMessageInfo(msgs.get(i));
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, messages);
        listView.setAdapter(modeAdapter);
        builder.setView(listView);
        final Dialog dialog = builder.create();
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MyUtils.respondToMessage(msgs.get(position), MainActivity.this)) {
                    Toast toast = Toast.makeText(MainActivity.this,
                            MainActivity.this.getResources().getString(R.string.success), Toast.LENGTH_SHORT);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    v.setTextColor(Color.GREEN);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this,
                            MainActivity.this.getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    v.setTextColor(Color.RED);
                    toast.show();
                }
                dialog.dismiss();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                createMessageLongClick(msgs.get(position));
                return false;
            }
        });
    }

    /**
     * Method for handling long click on received messages.
     *
     * @param message
     *         stored received message.
     */
    private void createMessageLongClick(String message) {
        TextView tvText = new TextView(MainActivity.this);
        tvText.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Medium);
        tvText.setText(MyUtils.parseStoredReceivedMessage(message));
        String title = getResources().getString(R.string.text_message_info_title);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"))
                .setView(tvText)
                .setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Method for handling auto-reply menu item press.
     *
     * @param item
     *         menu item of auto-reply
     */
    private void activateAutoReply(MenuItem item) {
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
    }

    @Override
    public void selectedStrings(final List<String> strings) {
        tabHost.clearAllTabs();
        sensorDataMap.clear();
        tvSensorData.setText(R.string.text_sensor_data_default);
        if (strings != null && strings.size() > 0) {
            for (int i = 0; i < strings.size(); ++i) {
                String name = strings.get(i);
                //populate map with data
                String value = MultiprocessPreferences.
                        getDefaultSharedPreferences(getApplicationContext()).getString(name, Constants.DEFAULT_SENSOR_DATA);
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
            case R.id.bCheckData:
                checkData();
                break;
            case R.id.bChooseDestination:
                chooseDestination();
                break;
            case R.id.bSendMessage:
                if (checkAllParameters()) {
                    String deviceId = etDeviceId.getText().toString();
                    String encryption = ((Spinner) findViewById(R.id.spEncryption)).getSelectedItem().toString();
                    String sendMode = ((Spinner) findViewById(R.id.spSendMode)).getSelectedItem().toString();
                    String destinationFormat = etDestination.getText().toString();
                    if (!CommUtils.sendMessage(deviceId, encryption, sendMode, destinationFormat, sensorDataMap,
                            getApplicationContext())) {
                        Toast toast = Toast.makeText(MainActivity.this,
                                MainActivity.this.getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);
                        toast.show();
                    }
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
        String deviceID = etDeviceId.getText().toString();
        if (deviceID.length() != 8) {
            Toast.makeText(getApplicationContext(), R.string.error_device_id, Toast.LENGTH_LONG).show();
            etDeviceId.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorThingIdError));
            return false;
        } else {
            getPreferences(Context.MODE_PRIVATE).edit().putString(Constants.KEY_DEVICE_ID, deviceID).apply();
            etDeviceId.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorThingIdText));
        }
        if (sensorDataMap.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.error_no_sensor_selected, Toast.LENGTH_SHORT).show();
            return false;
        }
        String destination = etDestination.getText().toString();
        if ("".equals(destination)) {
            Toast.makeText(MainActivity.this, R.string.error_no_destination, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Pattern.compile(Constants.REGEX_DESTINATION_FORMAT).matcher(destination).matches()) {
            Toast.makeText(MainActivity.this, R.string.error_invalid_adress_fromat, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Method for creating dialog which will display some destination addresses.
     */
    private void chooseDestination() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getResources().getString(R.string.text_select_destination);
        builder.setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"));
        ListView modeList = new ListView(this);
        final String[] addresses = StoringUtils.getDestinationAddresses(getApplicationContext());
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, R.layout.custom_checked_text_view,
                android.R.id.text1, addresses);
        modeList.setAdapter(modeAdapter);
        builder.setView(modeList);
        final Dialog dialog = builder.create();
        dialog.show();
        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etDestination.setText(addresses[position]);
                dialog.dismiss();
            }
        });
    }

    /**
     * Method for checking sensors data changes and if services are running or not.
     */
    private void checkData() {
        if (!MyUtils.isServiceRunning(GPSLocator.class, MainActivity.this)) {
            startService(gpsService);
        }
        if (!MyUtils.isServiceRunning(DeviceSensors.class, MainActivity.this)) {
            startService(sensorService);
        }
        updateTabHostWithData();
    }

    /**
     * Method for updating {#link tabHost} with valid data.
     */
    private void updateTabHostWithData() {
        for (String name : spDeviceSensors.getSelectedStrings()) {
            String value = MultiprocessPreferences.
                    getDefaultSharedPreferences(getApplicationContext()).getString(name, Constants.DEFAULT_SENSOR_DATA);
            sensorDataMap.put(name, value);
        }
        if (spDeviceSensors.getSelectedStrings().size() > 0) {
            String tabName = tabHost.getCurrentTabTag();
            String text = MainActivity.this.getString(R.string.name_sensor) + ": " + tabName + "\n" + sensorDataMap.get(tabName);
            tvSensorData.setText(text);
        }
    }

}
