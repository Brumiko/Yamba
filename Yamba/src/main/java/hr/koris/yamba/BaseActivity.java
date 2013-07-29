package hr.koris.yamba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by slavek on 23.07.13..
 */
public class BaseActivity extends Activity {
    YambaApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (YambaApplication)getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        MenuItem mnuToggleService = menu.findItem(R.id.mnuToggleService);
        if (app.isServiceRunning()) {
            mnuToggleService.setTitle(R.string.serviceStop);
            mnuToggleService.setIcon(android.R.drawable.ic_media_pause);
        } else {
            mnuToggleService.setTitle(R.string.serviceStart);
            mnuToggleService.setIcon(android.R.drawable.ic_media_play);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuStatus:
                startActivity(new Intent(this, StatusActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.mnuTimeline:
                startActivity(new Intent(this, TimelineActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.mnuSettings:
                startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;
            case R.id.mnuPurge:
                ((YambaApplication)getApplication()).getStatusData().delete();
                Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG).show();
                break;
            case R.id.mnuToggleService:
                if (app.isServiceRunning()) {
                    stopService(new Intent(this, UpdaterService.class));
                } else {
                    if (((YambaApplication)getApplication()).getRefreshInterval() >= YambaApplication.MIN_REFRESH_INTERVAL) {
                        startService(new Intent(this, UpdaterService.class));
                    } else {
                        Toast.makeText(this, R.string.msgRefreshInterval, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.mnuRefresh:
                startService(new Intent(this, RefreshService.class));
                break;
        }
        return true;
    }
}
