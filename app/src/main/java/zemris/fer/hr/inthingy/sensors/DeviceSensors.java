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

/**
 * Class for handling sensor data.
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
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String value;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                value = makeValueString(new String[]{"x", "y", "z"}, "\u00B5T", event.values);
                break;
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                value = makeValueString(new String[]{"x", "y", "z"}, "rad/s", event.values);
                break;
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_ACCELEROMETER:
                value = makeValueString(new String[]{"x", "y", "z"}, "m/s\u00B2", event.values);
                break;
            case Sensor.TYPE_LIGHT:
                value = makeValueString(new String[]{"Illumination"}, "lx", event.values);
                break;
            case Sensor.TYPE_PROXIMITY:
                value = makeValueString(new String[]{"Proximity"}, "cm", event.values);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                value = makeValueString(new String[]{"Temperature"}, "\u2103", event.values);
                break;
            case Sensor.TYPE_PRESSURE:
                value = makeValueString(new String[]{"Pressure"}, "hPa", event.values);
                break;
            default:
                value = makeValueString(new String[]{"Error"}, "", event.values);
        }
        MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString(event.sensor.getName(), value).apply();
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
     * @return properly formatted string
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
        while (nameSize != 3) {
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
