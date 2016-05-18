package zemris.fer.hr.inthingy.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;

import zemris.fer.hr.inthingy.R;

/**
 * Service for handling sensors and their data.
 * It uses {@link SensorManager} to register itself for sensor data changes. Class registers for all sensors that are
 * found on current device. List of those sensors can be found <a href="https://developer.android.com/guide/topics/sensors/sensors_overview.html">here</a>
 * When service is going to be destroyed, it unregister itself from {@link SensorManager}.
 * Data is stored in {@link com.gdubina.multiprocesspreferences.MultiprocessPreferences.MultiprocessSharedPreferences}.
 */
public class DeviceSensors extends Service implements SensorEventListener {

    /** Sensor manager which is used to access sensors and their data. */
    private SensorManager sensorManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //get sensor service
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        //default values for sensors
        float[] values = new float[]{0, 0, 0};
        //get list of devices sensors and register for their data changes
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            //register listener
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            //make default values for sensors
            String value = getValueForSensor(sensor, values);
            //save default values for sensors
            MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                    .edit().putString(sensor.getName(), value).apply();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister listener from sensor manager
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //get sensor and its new value
        String value = getValueForSensor(event.sensor, event.values);
        //update collection for new sensor data
        MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString(event.sensor.getName(), value).apply();
    }

    /**
     * Method for getting value for given sensor. In this case sensor values are in float array. That array has 3 elements,
     * even if sensor has only one data, other elements are 0.0.
     * Depending on sensor type and its value, this method will return string in following format:
     * ---  valueName:   value   unit\n---.
     * List of sensors, their data and units can be found <a href="https://developer.android.com/guide/topics/sensors/sensors_overview.html">here</a>.
     *
     * @param sensor
     *         sensor which data will be used to make valid format of values
     * @param values
     *         float values of sensor
     * @return properly formatted value string
     */
    private String getValueForSensor(Sensor sensor, float[] values) {
        String value;
        switch (sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                value = makeValueString(new String[]{"x", "y", "z"}, "\u00B5T", values);
                break;
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                value = makeValueString(new String[]{"x", "y", "z"}, "rad/s", values);
                break;
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_ACCELEROMETER:
                value = makeValueString(new String[]{"x", "y", "z"}, "m/s\u00B2", values);
                break;
            case Sensor.TYPE_LIGHT:
                value = makeValueString(new String[]{getString(R.string.illumination)}, "lx", values);
                break;
            case Sensor.TYPE_PROXIMITY:
                value = makeValueString(new String[]{getString(R.string.proximity)}, "cm", values);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                value = makeValueString(new String[]{getString(R.string.temperature)}, "\u2103", values);
                break;
            case Sensor.TYPE_PRESSURE:
                value = makeValueString(new String[]{getString(R.string.pressure)}, "hPa", values);
                break;
            default:
                value = makeValueString(new String[]{getString(R.string.error)}, "", values);
        }
        return value;
    }


    /**
     * Method for creating sensor data value. Number of elements in {#link valueNames} shows how much valid number
     * of values are in {#link values}.
     *
     * @param valueNames
     *         array of data names (e.g. x,y,z)
     * @param unit
     *         unit for values
     * @param values
     *         array of sensor values
     * @return properly formatted string, format is: valueName:   value   unit\n
     */
    private String makeValueString(String[] valueNames, String unit, float[] values) {
        StringBuilder valueString = new StringBuilder();
        int nameSize = valueNames.length;
        for (int i = 0; i < nameSize; ++i) {
            valueString.append(valueNames[i]).append(": ").append(values[i]).append(' ').append(unit);
            if (i != (values.length - 1)) {
                valueString.append('\n');
            }
        }
        while (nameSize < 2) {
            nameSize++;
            valueString.append('\n');
        }
        return valueString.toString();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
