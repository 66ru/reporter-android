package reporter66.ru;

import java.util.ArrayList;
import java.util.List;

import reporter66.ru.models.Post;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PostDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TITLE,
			MySQLiteHelper.COLUMN_TEXT,
			MySQLiteHelper.COLUMN_GEO_LAT,
			MySQLiteHelper.COLUMN_GEO_LNG,
			};

	public PostDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Post createPost(String title, String text, Float geo_lat, Float geo_lng) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_TEXT, text);
		values.put(MySQLiteHelper.COLUMN_GEO_LAT, geo_lat);
		values.put(MySQLiteHelper.COLUMN_GEO_LNG, geo_lng);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_POSTS, null,
				values);
		// To show how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSTS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		return cursorToPost(cursor);
	}

	public void deletePost(Post post) {
		long id = post.getId();
		System.out.println("post deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_POSTS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	public void savePost(Post post) {
		long id = post.getId();
		System.out.println("post saved with id: " + id);
		database.delete(MySQLiteHelper.TABLE_POSTS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
		// TODO:!
		//database.update(MySQLiteHelper.TABLE_POSTS, all, whereClause, whereArgs)
	}

	public List<Post> getAllPosts() {
		List<Post> posts = new ArrayList<Post>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSTS,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Post post= cursorToPost(cursor);
			posts.add(post);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return posts;
	}

	private Post cursorToPost(Cursor cursor) {
		Post post = new Post();
		post.setId(cursor.getLong(0));
		post.setTitle(cursor.getString(1));
		post.setText(cursor.getString(2));
		post.setGeo_lat(cursor.getFloat(3));
		post.setGeo_lng(cursor.getFloat(4));
		return post;
	}
}
