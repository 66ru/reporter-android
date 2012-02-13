package reporter66.ru.db;

import java.util.ArrayList;
import java.util.List;

import reporter66.ru.models.Post;
import reporter66.ru.models.PostItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PostDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TITLE, MySQLiteHelper.COLUMN_TEXT,
			MySQLiteHelper.COLUMN_GEO_LAT, MySQLiteHelper.COLUMN_GEO_LNG, };

	public PostDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Post createPost(String title, String text, Float geo_lat,
			Float geo_lng) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_TEXT, text);
		values.put(MySQLiteHelper.COLUMN_GEO_LAT, geo_lat);
		values.put(MySQLiteHelper.COLUMN_GEO_LNG, geo_lng);

		long insertId = database.insert(MySQLiteHelper.TABLE_POSTS, null,
				values);
		// To show how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSTS, allColumns,
				MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null,
				null);
		cursor.moveToFirst();
		return cursorToPost(cursor);
	}

	public void deletePost(Post post) {
		long id = post.getId();
		System.out.println("post deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_POSTS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
		database.delete(MySQLiteHelper.TABLE_POST_ITEMS,
				MySQLiteHelper.COLUMN_POST_ID + " = " + id, null);
	}

	public void savePost(Post post) {
		long id = post.getId();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, post.getTitle());
		values.put(MySQLiteHelper.COLUMN_TEXT, post.getText());
		values.put(MySQLiteHelper.COLUMN_GEO_LAT, post.getGeo_lat());
		values.put(MySQLiteHelper.COLUMN_GEO_LNG, post.getGeo_lng());

		database.update(MySQLiteHelper.TABLE_POSTS, values,
				MySQLiteHelper.COLUMN_ID + " = " + id, null);

		Log.i("post", "saved with id: " + id);
	}

	public List<Post> getAllPosts() {
		List<Post> posts = new ArrayList<Post>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POSTS, allColumns,
				null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Post post = cursorToPost(cursor);
			posts.add(post);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return posts;
	}

	public Post getLastPost() {
		Cursor c = database.query(MySQLiteHelper.TABLE_POSTS, allColumns, null,
				null, null, null, MySQLiteHelper.COLUMN_ID + " DESC");
		Log.w("c.getCount()", c.getCount() + "");
		if (c.getCount() == 0) {
			c.close();
			return null;
		} else {
			c.moveToFirst();
			Post post = cursorToPost(c);
			c.close();
			return post;
		}
	}

	private Post cursorToPost(Cursor cursor) {
		Post post = new Post();
		post.setId(cursor.getLong(0));
		post.setTitle(cursor.getString(1));
		post.setText(cursor.getString(2));
		post.setGeo_lat(cursor.getDouble(3));
		post.setGeo_lng(cursor.getDouble(4));
		return post;
	}
}
