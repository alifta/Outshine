package ca.mun.outshine.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DbHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String TAG = DbHelper.class.getSimpleName();

    // Database Version
    private static final int DB_VERSION = 1;

    // Database Name
    private static final String DB_NAME = "outshine.db";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(Ap.CREATE_TABLE);
        db.execSQL(Scan.CREATE_TABLE);
        db.execSQL(User.CREATE_TABLE);
        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL(Ap.DROP_TABLE);
        db.execSQL(Scan.DROP_TABLE);
        db.execSQL(User.DROP_TABLE);

        // Create tables again
        onCreate(db);
    }

    public long addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.COLUMN_NAME, name);
        values.put(User.COLUMN_EMAIL, email);
        values.put(User.COLUMN_UID, uid);
        values.put(User.COLUMN_CREATED_AT, created_at);

        // Inserting Row
        long id = db.insert(User.TABLE_NAME, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into db: " + id);

        return id;
    }

    public HashMap<String, String> getUsers() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + User.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching users from Sqlite: " + user.toString());

        return user;
    }

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(User.TABLE_NAME, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from db");
    }

    public long addAp(String ssid, String bssid) {
        // Get writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Ap.COLUMN_SSID, ssid);
        values.put(Ap.COLUMN_BSSID, bssid);

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(Ap.TABLE_NAME, null, values);

        // Close db connection
        db.close();

        Log.d(TAG, "New Ap inserted into db: " + id);

        // Return newly inserted row id
        return id;
    }

    public Ap getAp(long id) {
        // get readable database
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Ap.TABLE_NAME,
                new String[]{Ap.COLUMN_ID, Ap.COLUMN_SSID, Ap.COLUMN_BSSID},
                Ap.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null) cursor.moveToFirst();

        // Prepare Ap object
        Ap ap = new Ap(
                cursor.getInt(cursor.getColumnIndex(Ap.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Ap.COLUMN_SSID)),
                cursor.getString(cursor.getColumnIndex(Ap.COLUMN_BSSID)));

        // Close the db connection
        cursor.close();

        // Return result
        return ap;
    }

    public ArrayList<Ap> getAllAps() {
        ArrayList<Ap> aps = new ArrayList<>();

        // Select all query
        String selectQuery = "SELECT * FROM " + Ap.TABLE_NAME
                + " ORDER BY " + Ap.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Ap ap = new Ap();
                ap.setId(cursor.getInt(cursor.getColumnIndex(Ap.COLUMN_ID)));
                ap.setSsid(cursor.getString(cursor.getColumnIndex(Ap.COLUMN_SSID)));
                ap.setBssid(cursor.getString(cursor.getColumnIndex(Ap.COLUMN_BSSID)));
                aps.add(ap);
            } while (cursor.moveToNext());
        }

        // Close db connection
        db.close();

        // Return list
        return aps;
    }

    public int getApsCount() {
        String countQuery = "SELECT * FROM " + Ap.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public int updateAp(Ap ap) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Ap.COLUMN_SSID, ap.getSsid());
        values.put(Ap.COLUMN_BSSID, ap.getBssid());

        // Updating row
        return db.update(Ap.TABLE_NAME, values, Ap.COLUMN_ID + "=?",
                new String[]{String.valueOf(ap.getId())});
    }

    public void deleteAp(Ap ap) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Ap.TABLE_NAME, Ap.COLUMN_ID + "=?",
                new String[]{String.valueOf(ap.getId())});
        db.close();
    }

    public void deleteAp(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Ap.TABLE_NAME, Ap.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean existBssid(String bssid) {
        String existQuery = "SELECT EXISTS (SELECT 1 FROM " + Ap.TABLE_NAME
                + " WHERE " + Ap.COLUMN_BSSID + "='" + bssid + "' LIMIT 1)";
        String Query = "SELECT * FROM " + Ap.TABLE_NAME
                + " WHERE " + Ap.COLUMN_BSSID + " = '" + bssid + "'";

        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(existQuery, null);
        Cursor cursor = db.rawQuery(Query, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
