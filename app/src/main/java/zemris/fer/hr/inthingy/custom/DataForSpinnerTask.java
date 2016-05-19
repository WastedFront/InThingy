package zemris.fer.hr.inthingy.custom;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.guna.libmultispinner.MultiSelectionSpinner;

import java.util.ArrayList;
import java.util.List;

import zemris.fer.hr.inthingy.R;
import zemris.fer.hr.inthingy.utils.Constants;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Task for getting all sensors that device has and populate appropriate spinner with their names.
 */
public class DataForSpinnerTask extends AsyncTask<String, Void, String> {

    /** List for sensor names. */
    private List<String> sensorNames;
    /** Context from main activity. */
    private Context mContext;
    /** Multi selection spinner. */
    private MultiSelectionSpinner multiSelectionSpinner;

    /**
     * Constructor with two parameters.
     *
     * @param mContext
     *         context of activity which call this task
     * @param multiSelectionSpinner
     *         multi selection spinner which will be populated with data
     */
    public DataForSpinnerTask(Context mContext, MultiSelectionSpinner multiSelectionSpinner) {
        this.mContext = mContext;
        this.multiSelectionSpinner = multiSelectionSpinner;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        sensorNames = new ArrayList<>();
    }

    @Override
    protected String doInBackground(String... params) {
        //get sensor manager
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Service.SENSOR_SERVICE);
        //get list of sensors
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            //get sensor
            sensorNames.add(sensor.getName().toUpperCase());
            //store sensors so that this task doesn't need to run on every application startup, only on first
            StoringUtils.addSensor(mContext, sensor.getName());
        }
        //add GPS
        sensorNames.add(Constants.GPS_SENSOR_NAME);
        StoringUtils.addSensor(mContext, Constants.GPS_SENSOR_NAME);
        return Constants.STRING_OK;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (Constants.STRING_OK.equals(result)) {
            multiSelectionSpinner.setItems(sensorNames);
        } else {
            Toast.makeText(mContext, R.string.error_checking_sensors, Toast.LENGTH_SHORT).show();
        }

    }
}