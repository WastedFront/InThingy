package zemris.fer.hr.inthingy.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import zemris.fer.hr.inthingy.utils.MyUtils;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Service for automatic reply for messages that are received.
 */
public class MessageReplyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        while (true) {
            List<String> messages = StoringUtils.getReceivedMessages(getApplicationContext());
            if (messages.size() == 0) {
                try {
                    Thread.sleep(5000);
                    continue;
                } catch (InterruptedException e) {
                    continue;
                }
            }
            MyUtils.respondToMessage(messages.get(0), getApplicationContext());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
