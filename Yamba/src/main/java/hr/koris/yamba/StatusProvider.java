package hr.koris.yamba;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by slavek on 25.07.13..
 */
public class StatusProvider extends ContentProvider {
    //Dijagnostika.
    private static final String TAG = StatusProvider.class.getSimpleName();
    // URI parametri.
    public static final Uri CONTENT_URI = Uri.parse("content://hr.koris.yamba.StatusProvider");
    private static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.koris.yamba.status";
    private static final String MULTIPLE_RECORD_MIME_TYPE = "vnd.android.cursor.dir/vnd.koris.yamba.mstatus";
    // Pomoćne varijable.
    private StatusData statusData;

    @Override
    public boolean onCreate() {
        statusData = new StatusData(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] selectedCols, String whereClause, String[] whereArgs, String sortOrder) {
        Cursor cursor;
        long id = getIdFromUri(uri);
        SQLiteDatabase db = statusData.dbHelper.getReadableDatabase();
        if (id < 0) {
            cursor = db.query(StatusData.TABLE, selectedCols, whereClause, whereArgs, null, null, sortOrder);
        } else {
            cursor = db.query(StatusData.TABLE, selectedCols, StatusData.C_ID + "=" + id, null, null, null, null);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return getIdFromUri(uri) < 0 ? MULTIPLE_RECORD_MIME_TYPE : SINGLE_RECORD_MIME_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri u;
        SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
        try {
            long id = db.insertOrThrow(StatusData.TABLE, null, contentValues);
            if (id == -1) {
                throw new RuntimeException(String.format("[%s]: Failed to insert [%s] for unknown reasons.", TAG, uri));
            }
            // inače...
            u = ContentUris.withAppendedId(uri, id);
        } finally {
            db.close();
        }
        return u;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        int count = 0;
        long id = getIdFromUri(uri);
        SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
        try {
            if (id < 0) {
                count = db.delete(StatusData.TABLE, whereClause, whereArgs);
            } else {
                count = db.delete(StatusData.TABLE, StatusData.C_ID + "=" + id, null);
            }
        } finally {
            db.close();
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        int count = 0;
        long id = getIdFromUri(uri);
        SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
        try {
            if (id < 0) {
                count = db.update(StatusData.TABLE, contentValues, whereClause, whereArgs);
            } else {
                count = db.update(StatusData.TABLE, contentValues, StatusData.C_ID + "=" + id, null);
            }
        } finally {
            db.close();
        }
        //getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private long getIdFromUri(Uri uri) {
        long id = -1;
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                id = Long.parseLong(lastPathSegment);
            } catch (NumberFormatException e) {
                // TODO: nešto...
            }
        }
        return id;
    }
}
