package reporter66.ru.db;

import java.util.ArrayList;
import java.util.List;

import reporter66.ru.models.PostItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class PostItemDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_URI, MySQLiteHelper.COLUMN_TYPE,
			MySQLiteHelper.COLUMN_POST_ID, MySQLiteHelper.COLUMN_EXTERNAL_ID};

	public PostItemDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public PostItem createPostItem(Uri uri, int type, long post_id) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_URI, uri.toString());
		values.put(MySQLiteHelper.COLUMN_TYPE, type);
		values.put(MySQLiteHelper.COLUMN_POST_ID, post_id);
		values.put(MySQLiteHelper.COLUMN_EXTERNAL_ID, -1);

		long insertId = database.insert(MySQLiteHelper.TABLE_POST_ITEMS, null,
				values);
		// To show how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POST_ITEMS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		return cursorToPostItem(cursor);
	}

	public void deletePostItem(PostItem postItem) {
		long id = postItem.getId();
		System.out.println("postItem deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_POST_ITEMS,
				MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public List<PostItem> getAllPostItems(long id) {
		List<PostItem> postItems = new ArrayList<PostItem>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POST_ITEMS,
				allColumns, MySQLiteHelper.COLUMN_POST_ID + " = " + id, null,
				null, null, null);
		cursor.moveToFirst();
		int i = 0;
		while (!cursor.isAfterLast()) {
			Log.i("getAllPostItems", i + "");
			PostItem postItem = cursorToPostItem(cursor);
			postItems.add(postItem);
			cursor.moveToNext();
			i++;
		}
		// Make sure to close the cursor
		cursor.close();
		return postItems;
	}
	
	public void savePostItem(PostItem item) {
		long id = item.getId();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_URI, item.getUri().toString());
		values.put(MySQLiteHelper.COLUMN_TYPE, item.getType());
		values.put(MySQLiteHelper.COLUMN_POST_ID, item.getPost_id());
		values.put(MySQLiteHelper.COLUMN_EXTERNAL_ID, item.getExternal_id());

		database.update(MySQLiteHelper.TABLE_POST_ITEMS, values,
				MySQLiteHelper.COLUMN_ID + " = " + id, null);

		Log.i("postItem", "saved with id: " + id);
	}
	
	public void deleteAllPostItems(long id) {
		database.delete(MySQLiteHelper.TABLE_POST_ITEMS,
				MySQLiteHelper.COLUMN_POST_ID + " = " + id, null);
	}

	private PostItem cursorToPostItem(Cursor cursor) {
		PostItem postItem = new PostItem();
		postItem.setId(cursor.getLong(0));
		postItem.setUri(Uri.parse(cursor.getString(1)));
		postItem.setType(cursor.getInt(2));
		postItem.setPost_id(cursor.getLong(3));
		postItem.setExternal_id(cursor.getLong(4));
		return postItem;
	}
}
