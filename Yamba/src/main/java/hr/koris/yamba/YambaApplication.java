package hr.koris.yamba;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import winterwell.jtwitter.Twitter;

/**
 * Created by slavek on 18.07.13..
 */
public class YambaApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    // Dijagnostika.
    private static final String TAG = YambaApplication.class.getSimpleName();
    // Pomoćne varijable.
    private Twitter twitter;
    // Privatne varijable.
    private SharedPreferences prefs;
    public static final String LOCATION_PROVIDER_NONE = "NONE";
    //public static final long REFRESH_INTERVAL_NEVER = 0;
    public static final long MIN_REFRESH_INTERVAL = 60000; // minuta
    private boolean serviceRunning;
    private StatusData statusData;
    // Geteri i seteri.
    public boolean isServiceRunning() {
        return serviceRunning;
    }
    public void setServiceRunning(boolean running) {
        serviceRunning = running;
    }
    public StatusData getStatusData() {
        if (statusData == null) {
            statusData = new StatusData(this);
        }
        return statusData;
    }
    public SharedPreferences getPrefs() {
        return prefs;
    }
    public String getLocationProvider() {
        return prefs.getString("locProvider", LOCATION_PROVIDER_NONE);
    }
    public long getRefreshInterval() {
        // For some reason storing interval as long doesn't work.
        return Long.parseLong(prefs.getString("locRefresh", "0"));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        statusData = new StatusData(this);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
    }

    // Ključ.riječ "synchronized" osigurava da samo jedna dretva poziva ovu metodu (mutex).
    public synchronized Twitter getTwitter() {
        if (twitter == null) {
            String username = prefs.getString("username", null);
            String password = prefs.getString("password", null);
            String apiRoot = prefs.getString("apiRoot", "http://yamba.marakana.com/api");
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(apiRoot)) {
                twitter = new Twitter(username, password);
                twitter.setAPIRootUrl(apiRoot);
            }
        }
        return twitter;
    }

    public synchronized int fetchStatusUpdates() {
        Log.d(TAG, "Fetching new tweets...");
        Twitter tweety = getTwitter();
        int count = 0;
        if (tweety == null) {
            Log.d(TAG, "Twitter connection not initialized.");
            return count;
        }
        try {
            List<Twitter.Status> statusUpdates = tweety.getFriendsTimeline();
            long latestStatusCreatedAtTime = getStatusData().getLatestStatusCreatedAtTime();
            ContentValues kv = new ContentValues();
            for (Twitter.Status status : statusUpdates) {
                kv.put(StatusData.C_ID, status.getId());
                long createdAt = status.getCreatedAt().getTime();
                kv.put(StatusData.C_CREATED_AT, createdAt);
                kv.put(StatusData.C_TEXT, status.getText());
                kv.put(StatusData.C_USER, status.getUser().getName());
                Log.d(TAG, "Fetched new tweet with id=" + status.getId() + ". Saving...");
                this.getStatusData().insertOrIgnore(kv);
                if (latestStatusCreatedAtTime < createdAt) {
                    count++;
                }
            }
            Log.d(TAG, count > 0 ? "Fetched " + count + " new tweets." : "No new tweets.");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to fetch new tweets.", ex);
        }
        return count;
    }

    @Override // Poništava Twitter objekt ako netko promijeni kor.ime, lozinku ili URL API-ja.
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        twitter = null;
    }
}
