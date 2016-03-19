package zemris.fer.hr.inthingy.messages;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Service for automatic reply for messages that are received.
 */
public class MessageReplyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MESSAGE", "onCreate");
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
