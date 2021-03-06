package hr.koris.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by slavek on 23.07.13..
 */
public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (isNetworkDown) {
            Log.d(TAG, "onReceive: NOT connected, stopping UpdaterService");
            context.stopService(new Intent(context, UpdaterService.class));
        } else {
            Log.d(TAG, "onReceive: connected, starting UpdaterService");
            context.startService(new Intent(context, UpdaterService.class));
        }
    }
}
