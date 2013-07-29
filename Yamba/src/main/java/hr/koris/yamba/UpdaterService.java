package hr.koris.yamba;

import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by slavek on 18.07.13..
 */
public class UpdaterService extends Service {
    public static final String TAG = UpdaterService.class.getSimpleName();
    //public static final int MIN_DELAY = 60000; // minuta
    private boolean runFlag = false;
    private UpdaterThread updaterThread;
    private long updaterThreadDelay;
    // Primopredajni parametri.
    public static final String NEW_TWEETS_INTENT = "hr.koris.yamba.NEW_TWEETS";
    public static final String NEW_TWEETS_COUNT = "NEW_TWEETS_COUNT";

    public IBinder onBind(Intent intent) {
        return null; // Za vezane usluge. Ovdje ne koristimo.
    }

    @Override
    public void onCreate() {
        super.onCreate();

        updaterThreadDelay = ((YambaApplication)getApplication()).getRefreshInterval();
        updaterThread = new UpdaterThread(updaterThreadDelay);
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!runFlag && updaterThreadDelay >= YambaApplication.MIN_REFRESH_INTERVAL) { // Overkill, ali za svaki slučaj.
            runFlag = true;
            updaterThread.start(); // Obrada greški se radi u odgovarajućoj dretvi (dolje).
            ((YambaApplication)getApplication()).setServiceRunning(runFlag);
            Log.d(TAG, "onStartCommand");
        }
        return START_STICKY; // Za ručno pokretane servise!
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runFlag = false;
        updaterThread.interrupt();
        updaterThread = null;
        ((YambaApplication)getApplication()).setServiceRunning(runFlag);
        Log.d(TAG, "onDestroy");
    }

    protected void doNotification(int newTweetsCount) {
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

    private class UpdaterThread extends HandlerThread {
        private long sleepPeriod;
        static final String TIMELINE_TRANSCEIVER = "hr.koris.yamba.TIMELINE_TRANSCEIVER";

        public UpdaterThread(long delay) {
            super("UpdaterService-UpdaterThread");
            sleepPeriod = delay;
        }

        @Override
        public void run() {
            UpdaterService updaterService = UpdaterService.this;
            while (updaterService.runFlag) {
                Log.d(TAG, "Running background updater thread...");
                try {
                    int newTweetsCount = ((YambaApplication)updaterService.getApplication()).fetchStatusUpdates();
                    if (newTweetsCount > 0) {
                        Log.d(TAG, "New tweet(s)!");
                        Intent broadcastingIntent = new Intent(NEW_TWEETS_INTENT).putExtra(NEW_TWEETS_COUNT, newTweetsCount);
                        updaterService.sendBroadcast(broadcastingIntent, TIMELINE_TRANSCEIVER);
                        updaterService.doNotification(newTweetsCount);
                    }
                    Thread.sleep(sleepPeriod);
                } catch (InterruptedException ex) {
                    updaterService.runFlag = false;
                    Log.e(TAG, "ERROR in UpdaterThread.", ex);
                }
            }
        }
    }
}