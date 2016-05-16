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
 * Class for handling sensor data. For every event, it checks which sensor's data is changed and stores it's new
 * value into the global map.
 */
public class DeviceSensors extends Service implements SensorEventListener {

    /** Sensor manager. */
    private SensorManager sensorManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        float[] values = new float[]{0, 0, 0};
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            String value = getValueForSensor(sensor, values);
            MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                    .edit().putString(sensor.getName(), value).apply();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String value = getValueForSensor(event.sensor, event.values);
        MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString(event.sensor.getName(), value).apply();
    }

    /**
     * Method for getting value for given sensor.
     * Value is in following format:
     * valueName1: someValue valueUnit\n
     * valueName2: someValue valueUnit\n
     * ....
     *
     * @param sensor
     *         sensor
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
     * Method for creating sensor data value.
     *
     * @param valueNames
     *         array of data names (e.g. x,y,z)
     * @param unit
     *         unit for values
     * @param values
     *         array of sensor values
     * @return properly formatted string, format is: valueName1: someValue valueUnit\n
     */
    private String makeValueString(String[] valueNames, String unit, float[] values) {
        StringBuilder valueString = new StringBuilder();
        int nameSize = valueNames.length;
        for (int i = 0; i < nameSize; ++i) {
            valueString.append(valueNames[i]).append(": ").append(values[i]).append(" ").append(unit);
            if (i != (values.length - 1)) {
                valueString.append("\n");
            }
        }
        while (nameSize < 2) {
            nameSize++;
            valueString.append("\n");
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
