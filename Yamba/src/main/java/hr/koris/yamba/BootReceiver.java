package hr.koris.yamba;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by slavek on 23.07.13..
 */
public class BootReceiver extends BroadcastReceiver {
    static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (((YambaApplication)context.getApplicationContext()).getRefreshInterval() < YambaApplication.MIN_REFRESH_INTERVAL) return;
        // inače...
        context.startService(new Intent(context, UpdaterService.class));
        Log.d(TAG, "onReceive");

        /*
        long refreshInterval = ((YambaApplication)context.getApplicationContext()).getRefreshInterval();
        if (refreshInterval < YambaApplication.MIN_REFRESH_INTERVAL) {
            return;
        }
        // Vanjska app (Alarm) može pokretati dijelove moje app kao da su "domaći".
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                -1,
                new Intent(context, UpdaterService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), refreshInterval, pendingIntent);
        Log.d(TAG, "onReceive");
        */
    }
}
