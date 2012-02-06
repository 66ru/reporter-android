package reporter66.ru;

import java.util.ArrayList;
import java.util.List;

import reporter66.ru.models.Post;
import reporter66.ru.models.PostItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PostItemsDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_URI,
			MySQLiteHelper.COLUMN_TYPE,
			MySQLiteHelper.COLUMN_POST_ID,
			};

	public PostItemsDataSource(Context context) {
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
		database.delete(MySQLiteHelper.TABLE_POST_ITEMS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<PostItem> getAllPostItems() {
		List<PostItem> postItems = new ArrayList<PostItem>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_POST_ITEMS,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			PostItem postItem = cursorToPostItem(cursor);
			postItems.add(postItem);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return postItems;
	}

	private PostItem cursorToPostItem(Cursor cursor) {
		PostItem postItem = new PostItem();
		postItem.setId(cursor.getLong(0));
		postItem.setUri(Uri.parse(cursor.getString(1)));
		postItem.setType(cursor.getInt(2));
		postItem.setPost_id(cursor.getLong(3));
		return postItem;
	}
}
