package com.example.christian.albumoftheday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Christian on 4/3/16.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ALBUM_DATABASE";
    public static final String TABLE_NAME = "ALBUM_TABLE";

    public static final String COL_ID="_id";
    public static final String COL_SONG="SONG";
    public static final String COL_ARTIST="ARTIST";
    public static final String COL_ALBUM="ALBUM";
    public static final String COL_YEAR="YEAR";
    public static final String COL_FAVORITE="FAVORITE";

    public static final String [] DATABASE_COLUMNS = {COL_ID, COL_SONG, COL_ARTIST, COL_ALBUM, COL_YEAR};

    private static final String CREATE_ALBUM_TABLE = "CREATE TABLE " + TABLE_NAME +
            " ( " +
            COL_ID + " INTEGER PRIMARY KEY, " + COL_SONG + " TEXT, " +
            COL_ARTIST + " TEXT, "
            + COL_ALBUM + " TEXT, " + COL_YEAR + " TEXT, "
             + COL_FAVORITE + "INTEGER ) ";

    private static SQLiteDatabaseHelper instance;

    public static SQLiteDatabaseHelper getInstance(Context context) {
        if(instance == null) {
            instance = new SQLiteDatabaseHelper(context);
        }
        return instance;
    }
    Context mContext;
    private SQLiteDatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALBUM_TABLE);
        addItemToDataBase(db,"Smells Like Teen Spirit","Nirvana","Nevermind","1991",0);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    private void addItemToDataBase (SQLiteDatabase db, String song, String artist, String album, String year, Integer favorite) {
        ContentValues values = new ContentValues();
        values.put(COL_SONG, song);
        values.put(COL_ARTIST, artist);
        values.put(COL_ALBUM, album);
        values.put(COL_YEAR, year);
        values.put(COL_FAVORITE, favorite);

        db.insert(TABLE_NAME, null, values);
    }

    public boolean updateAddFavorite(int id, int favorite) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
//        values.put(COL_ID, id);
//        values.put(COL_LOCATION_NAME, name);
//        values.put(COL_LOCATION_ADDRESS, address);
//        values.put(COL_LOCATION_NEIGHBORHOOD, environment);
        values.put(COL_FAVORITE, favorite);
        db.update(TABLE_NAME, values, COL_ID + " = ? ", new String[]{String.valueOf(id)});

        return true;
}
    public boolean updateRemoveFavorite(int id, int favorite) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_FAVORITE, favorite);

        db.update(TABLE_NAME, values, COL_ID + " = ? ", new String[]{String.valueOf(id)});

        return true;
    }

public Cursor getFavorites() {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query(TABLE_NAME,
            DATABASE_COLUMNS,
            COL_FAVORITE + " LIKE 1 ",
            null,
            null,
            null,
            null,
            null);

    return cursor;
}
   // public Cursor searchLocationList (String query) {
     //   SQLiteDatabase db = this.getReadableDatabase();

       // Cursor cursor = db.query(TABLE_NAME,
       // MediaStore.Audio.AlbumColumns,
        // COL_1 + " LIKE ? OR " + COL_2 + " LIKE ? OR " + COL_3 + " LIKE ? ",
        // new String []{"%" + query + "%"},
        // null,
        //null,
        //null,
        //null);

        // return cursor;
   // }

    public String[] getDescriptionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_SONG, COL_ARTIST, COL_ALBUM, COL_YEAR, COL_FAVORITE},
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        String [] detailsArray = new String[]{
                cursor.getString(cursor.getColumnIndex(COL_SONG)),
                cursor.getString(cursor.getColumnIndex(COL_ARTIST)),
                cursor.getString(cursor.getColumnIndex(COL_ALBUM)),
                cursor.getString(cursor.getColumnIndex(COL_YEAR)),
                cursor.getString(cursor.getColumnIndex(COL_FAVORITE)),
        };

        return detailsArray;

    }

    public int checkFavoriteById (int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_FAVORITE},
                COL_ID + " = ? ",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null);

        cursor.moveToFirst();

        int detailsInt = cursor.getInt(cursor.getColumnIndex(COL_FAVORITE));

        return detailsInt;
    }


}
