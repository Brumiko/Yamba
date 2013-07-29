package hr.koris.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TimelineActivity extends BaseActivity {
    // Dijagnostika.
    static final String TAG = TimelineActivity.class.getSimpleName();
    // Pomoćne varijable.
    ListView lstTimeline;
    SimpleCursorAdapter adapter;
    Cursor cursor;
    TimelineReceiver receiver;
    IntentFilter filter;
    // Adapterski parametri.
    static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER, StatusData.C_TEXT };
    static final int[] TO = { R.id.lblCreatedAt, R.id.lblUser, R.id.lblStatus };
    // Primopredajni parametri.
    static final String TIMELINE_TRANSCEIVER = "hr.koris.yamba.TIMELINE_TRANSCEIVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // PROVJERA da li su postavke (kor.ime i lozinka) upisani.
        // Ako nisu, otvara se zaslon za postavke.
        if (app.getPrefs().getString("username", null) == null || app.getPrefs().getString("password", null) == null) {
            startActivity(new Intent(this, SettingsActivity.class));
            Toast.makeText(this, R.string.msgUsernamePassword, Toast.LENGTH_LONG).show();
        }
        // inače...
        lstTimeline = (ListView)findViewById(R.id.lstTimeline);
        // Postavljanje primopredajnika.
        receiver = new TimelineReceiver();
        filter = new IntentFilter(UpdaterService.NEW_TWEETS_INTENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            fillTimelineList();
        } catch (Exception ex) {
            Log.e(TAG, "ERROR: Fetching new tweets failed.", ex);
            Toast.makeText(this, "Fetching new tweets failed. " + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        // Registracija prijemnika. Izvodi se ovdje, a ne u manifestu
        // zato da bi OSVJEŽAVANJE PRIKAZA bilo aktivno samo kada je uključena ova aktivnost.
        //super.registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);
        super.registerReceiver(receiver, filter, TIMELINE_TRANSCEIVER, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Deregistracija prijemnika.
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.getStatusData().close();
    }

    private void fillTimelineList() {
        this.cursor = app.getStatusData().getStatusUpdates();
        startManagingCursor(this.cursor);

        adapter = new SimpleCursorAdapter(this, R.layout.row_timeline, cursor, FROM, TO);
        adapter.setViewBinder(VIEW_BINDER);
        lstTimeline.setAdapter(adapter);
    }

    static final ViewBinder VIEW_BINDER = new ViewBinder() {

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() != R.id.lblCreatedAt) {
                return false;
            }
            // inače...
            //long timestamp = cursor.getLong(columnIndex);
            //CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(view.getContext(), timestamp);
            ((TextView) view).setText(DateUtils.getRelativeTimeSpanString(view.getContext(), cursor.getLong(columnIndex)));

            return true;
        }
    };

    class TimelineReceiver extends BroadcastReceiver {
        final String TAG = TimelineReceiver.class.getSimpleName();
        @Override
        public void onReceive(Context context, Intent intent) {
            // database /data/data/hr.koris.yamba/databases/timeline.db (conn# 0) already closed
            //cursor.requery(); // Trebalo je popraviti jer ovdje počne crkavati (plavo)...
            //adapter.notifyDataSetChanged(); // ...a ovdje crkne (crveno). BTW, autorefresh!
            fillTimelineList();
            Log.d(TAG, "onReceive");
        }
    }
}
