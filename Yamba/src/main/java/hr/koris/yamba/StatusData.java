package hr.koris.yamba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by slavek on 20.07.13..
 */
public class StatusData {
    // Dijagnostika.
    static final String TAG = StatusData.class.getSimpleName();
    // DB.
    static final String DB_MAME = "timeline.db";
    static final int DB_VERSION = 1;
    // Tabela.
    static final String TABLE = "timeline";
    static final String C_ID = BaseColumns._ID;
    static final String C_CREATED_AT = "created_at";
    static final String C_TEXT = "txt";
    static final String C_USER = "user";
    // Upiti.
    private static final String ORDER_BY = C_CREATED_AT + " desc";
    private static final String[] MAX_CREATED_AT = { "max(" + C_CREATED_AT + ")" }; // ???
    private static final String[] DB_TEXT_COLUMNS = { C_TEXT };
    // Pomoćne varijable.
    final DbHelper dbHelper;


    public StatusData(Context context) {
        dbHelper = new DbHelper(context);
        Log.d(TAG, "DB initialized.");
    }

    public void insertOrIgnore(ContentValues kv) {
        Log.d(TAG, "insertOrIgnore on " + kv);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE, null, kv, SQLiteDatabase.CONFLICT_IGNORE);
        } finally {
            db.close();
        }
    }

    /**
     *
     * @return Cursor where the columns are: _id, created_at, user, txt.
     */
    public Cursor getStatusUpdates() {

        /* NE OVAKO!!!
            Dobiva se 'already closed' greška.

        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            cursor = db.query(TABLE, null, null, null, null, null, ORDER_BY);
        } finally {
            db.close();
        }
        return cursor;
        */

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        return db.query(TABLE, null, null, null, null, null, ORDER_BY);
    }

    /**
     *
     * @return Timestamp of the latest status we have in the database.
     */
    public long getLatestStatusCreatedAtTime() {
        long createdAtTime = Long.MIN_VALUE;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE, MAX_CREATED_AT, null, null, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    createdAtTime = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }
        return createdAtTime;
    }

    /**
     *
     * @param id of the status we are looking for
     * @return Text of the status
     */
    public String getStatusTextById(long id) {
        String statusText = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    statusText = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }
        return statusText;
    }

    public void close() {
        this.dbHelper.close();
    }

    /**
     * Deletes ALL the data.
     */
    public void delete() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

    class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_MAME, null, DB_VERSION);
        }

        @Override // Ovo bi trebalo biti samo jednom pozvano.
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "Creating DB " + DB_MAME + " ...");
            String sqlCmd =
                    "create table " + TABLE + " (" +
                            C_ID + " int primary key, " +
                            C_CREATED_AT + " int, " +
                            C_USER + " text, " +
                            C_TEXT + " text)";
            sqLiteDatabase.execSQL(sqlCmd);
            Log.d(TAG, "DB created: " + sqlCmd);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("drop table if exists " + TABLE);
            Log.d(TAG, "onUpgrade");
            onCreate(sqLiteDatabase);
        }
    }
}
