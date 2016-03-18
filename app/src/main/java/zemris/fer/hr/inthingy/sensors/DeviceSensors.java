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
            sensorManager.registerListener(this, sensor, 1000000); //delay is 1s
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder valueString = new StringBuilder();
        valueString.append(event.values[0]).append("\n").append(event.values[1]).append("\n").append(event.values[2]);
        MultiprocessPreferences.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString(event.sensor.getName(), valueString.toString()).apply();
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
