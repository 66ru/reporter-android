package reporter66.ru;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_POSTS = "posts";
	public static final String TABLE_POST_ITEMS = "post_items";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_GEO_LAT = "geo_lat";
	public static final String COLUMN_GEO_LNG = "geo_lng";
	public static final String COLUMN_POST_ID = "post_id";
	public static final String COLUMN_URI = "uri";
	public static final String COLUMN_TYPE = "type";

	private static final String DATABASE_NAME = "posts.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE_POSTS = "create table "
			+ TABLE_POSTS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_TITLE
			+ " text not null, " + COLUMN_TEXT + " text not null, "
			+ COLUMN_GEO_LAT + " REAL not null, " + COLUMN_GEO_LNG
			+ " REAL not null, " + ");";
	private static final String DATABASE_CREATE_POST_ITEMS = "create table "
			+ TABLE_POST_ITEMS + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_URI
			+ " text not null, " + COLUMN_TYPE + " INTEGER not null, "
			+ COLUMN_POST_ID + " INTEGER not null, " + ");";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_POSTS);
		database.execSQL(DATABASE_CREATE_POST_ITEMS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS" + TABLE_POSTS);
		onCreate(db);
	}

}