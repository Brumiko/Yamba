package hr.koris.yamba;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by slavek on 26.07.13..
 */
public class YambaWidget extends AppWidgetProvider {
    private static final String TAG = YambaWidget.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Piše u dokumentaciji da treba izraditi vlastitu metodu.
        //super.onUpdate(context, appWidgetManager, appWidgetIds);

        Cursor cursor = context.getContentResolver().query(StatusProvider.CONTENT_URI, null, null, null, StatusData.C_CREATED_AT + " DESC");
        try {
            if (cursor.moveToFirst()) {
                CharSequence user = cursor.getString(cursor.getColumnIndex(StatusData.C_USER));
                CharSequence createdAt = DateUtils.getRelativeTimeSpanString(context, cursor.getLong(cursor.getColumnIndex(StatusData.C_CREATED_AT)));
                CharSequence message = cursor.getString(cursor.getColumnIndex(StatusData.C_TEXT));
                for (int appWidgetId : appWidgetIds) { // Trčimo kroz sve instance widgeta.
                    Log.d(TAG, "Updating widget " + appWidgetId + ".");
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yamba_widget);
                    views.setTextViewText(R.id.lblUser, user);
                    views.setTextViewText(R.id.lblCreatedAt, createdAt);
                    views.setTextViewText(R.id.lblStatus, message);
                    views.setOnClickPendingIntent(R.id.imgYamba, PendingIntent.getActivity(context, 0, new Intent(context, TimelineActivity.class), 0));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            } else {
                Log.d(TAG, "No new tweets.");
            }
        } finally {
            cursor.close();
        }
        Log.d(TAG, "onUpdate");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(UpdaterService.NEW_TWEETS_INTENT)) {
            Log.d(TAG, "onReceive - new tweet detected!");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, YambaWidget.class)));
        }
    }
}
