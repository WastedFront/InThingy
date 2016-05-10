package zemris.fer.hr.inthingy.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Service for automatic reply for messages that are received.
 */
public class MessageReplyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MESSAGE", "onCreate");
        List<String> messages = StoringUtils.getReceivedMessages(this);
        messages = messages != null ? messages : new ArrayList<String>();
        for (String message : messages) {
            Log.e("MESSAGE", message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MESSAGE", "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
