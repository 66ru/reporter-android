package reporter66.ru;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class reporterUtils {
	/* media */
	private static final int THUMBNAIL_SIZE = 150;
	
	
	private static Context context;
	
	public reporterUtils(Context context) {
        this.context = context;
    }
	public static String getRealPathFromURI(Uri contentUri) {
		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Log.i("res",context.getContentResolver().toString());
		Cursor cursor = context.getContentResolver().query(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		Log.i("cursor","Loaded, size: "+cursor.getCount());
		cursor.moveToFirst();
		
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		

		return cursor.getString(column_index);
	}

	public static Bitmap decodeImageFile(Uri uri) {

		File f = new File(reporterUtils.getRealPathFromURI(uri));
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
		} catch (FileNotFoundException e) {
			Log.e("decodeImageFile", "File not found "
					+ reporterUtils.getRealPathFromURI(uri));
			e.printStackTrace();
		}

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;

		while (true) {
			if (width_tmp / 2 < THUMBNAIL_SIZE
					|| height_tmp / 2 < THUMBNAIL_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;

		try {
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
			Log.e("decodeImageFile", "File not found");
			e.printStackTrace();
		}

		return null;
	}
}
