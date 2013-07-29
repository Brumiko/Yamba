package hr.koris.yamba;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by slavek on 28.07.13..
 */
public class RefreshService extends IntentService {
    private static final String TAG = RefreshService.class.getSimpleName();
    // Primopredajni parametri.
    private static final String NEW_TWEETS_INTENT = "hr.koris.yamba.NEW_TWEETS";
    private static final String NEW_TWEETS_COUNT = "NEW_TWEETS_COUNT";
    private static final String TIMELINE_TRANSCEIVER = "hr.koris.yamba.TIMELINE_TRANSCEIVER";

    public RefreshService() {
        super(TAG);
        Log.d(TAG, "UpdaterService (extends IntentService) constructed.");
    }

    @Override
    protected void onHandleIntent(Intent broadcastingIntent) {
        Log.d(TAG, "onHandleIntent");
        try {
            int newTweetsCount = ((YambaApplication)getApplication()).fetchStatusUpdates();
            if (newTweetsCount <= 0) return;
            // inače...
            Log.d(TAG, "New tweet(s)!");
            broadcastingIntent = new Intent(NEW_TWEETS_INTENT).putExtra(NEW_TWEETS_COUNT, newTweetsCount);
            sendBroadcast(broadcastingIntent, TIMELINE_TRANSCEIVER);
            doNotification(newTweetsCount);
        } catch (Exception ex) {
            Log.e(TAG, "ERROR in onHandleIntent", ex);
        }
    }

    private void doNotification(int newTweetsCount) {
        Log.d(TAG, "Sending timeline notification...");
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(
                android.R.drawable.stat_notify_chat,
                getResources().getText(R.string.msgNotificationTitle),
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // Stvaramo PENDING intent jer želimo da VANJSKA app podigne Timeline Activity (klikom na obavjesnu traku),
        notification.setLatestEventInfo(
                this,
                getResources().getText(R.string.msgNotificationTitle),
                getResources().getString(R.string.msgNotificationMessage, newTweetsCount),
                PendingIntent.getActivity(
                        this,
                        -1,
                        new Intent(this, TimelineActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));
        notificationManager.notify(0, notification);
        Log.d(TAG, "Timeline notification send!");
    }
}
