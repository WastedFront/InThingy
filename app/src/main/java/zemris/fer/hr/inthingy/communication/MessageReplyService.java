package zemris.fer.hr.inthingy.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import zemris.fer.hr.inthingy.utils.ReceivedServerMessage;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Service for automatic reply for messages that are received. When application communicates with some server, it can
 * get some return message which usually tells which sensor's data are needed. Those messages are stored in
 * {@link  android.content.SharedPreferences}. This service read those messages and then automatically replies to them.
 * If this service is running and there are no such messages, it will go to sleep for 5 seconds.
 * Also there is some pause them between two replies which is about 1 second.
 */
public class MessageReplyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        while (true) {
            //get list of messages
            List<ReceivedServerMessage> messages = StoringUtils.getReceivedMessages(getApplicationContext());
            if (messages.size() == 0) {
                try {
                    Thread.sleep(5000);
                    continue;
                } catch (InterruptedException e) {
                    continue;
                }
            }
            new CommunicationTask(getApplicationContext(), messages.get(0).responseMessage(getApplicationContext()), false);
            StoringUtils.removeReceivedMessage(getApplicationContext(), messages.get(0));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //do nothing
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
