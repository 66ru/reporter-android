package reporter66.ru;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ReporterActivity extends Activity implements LocationListener {
	
	//intents codes
	private static final int THUMBNAIL_SIZE = 1;
	
	// geo
	private LocationManager locationManager;
	private String provider;
	private double longitude;
	private double latitute;
	// elements
	private Button submit;
	private ProgressDialog dialog;
	private ImageButton add_photo;
	// media
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private ImageAdapter imageAdapter;
	private static final int INTENT_IMAGE_PICK = 1;
	
	private static final int INTENT_IMAGE_CAPTURE = 2;
	private Uri ImageCaptureUri;
	
	private Gallery gallery;
	private List<Uri> galleryItems = new ArrayList<Uri>();

	// Called at the start of the full lifetime.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
		// Initialize activity.

		if (gallery == null) {
			gallery = (Gallery) findViewById(R.id.gallery);
			if (imageAdapter == null)
				imageAdapter = new ImageAdapter(this);
			gallery.setAdapter(imageAdapter);

			gallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v,
						int position, long id) {
					onGalleryItemClick(position);
				}
			});
		}
		if (submit == null) {
			submit = (Button) findViewById(R.id.submit);
			submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onSubmit();
				}
			});
		}
		if (add_photo == null) {
			add_photo = (ImageButton) findViewById(R.id.add_photo);
			add_photo.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onAppend();
				}
			});
		}
	}

	// data submit
	protected void onSubmit() {
		CheckBox add_geo = (CheckBox) findViewById(R.id.add_geo);
		if (add_geo.isChecked()) {
			dialog = ProgressDialog.show(ReporterActivity.this, "",
					"Устанавливаем связь с космосом...", true);
			provider = getProvider();
			if (provider != null) {
				locationManager.requestLocationUpdates(provider, 400, 1, this);
				Location location = locationManager
						.getLastKnownLocation(provider);

				if (location != null) {
					System.out.println("Provider " + provider
							+ " has been selected.");
					latitute = location.getLatitude();
					longitude = location.getLongitude();
					Toast.makeText(
							ReporterActivity.this,
							"Связь со спутниками установлена, ваши координаты: lat: "
									+ latitute + ", lng: " + longitude,
							Toast.LENGTH_SHORT).show();
					dialog.setMessage("Связь со спутниками установлена, ваши координаты: lat: "
							+ latitute + ", lng: " + longitude);

				} else {
					Toast.makeText(ReporterActivity.this,
							"Не удалось найти ваше местоположение",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(ReporterActivity.this,
						"Не удалось найти ваше местоположение, включите GPS",
						Toast.LENGTH_SHORT).show();
			}

			dialog.dismiss();
		}
	}

	// select intents for media append
	protected void onAppend() {
		final CharSequence[] items = { "Фото из галереи" , "Открыть камеру" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Добавить:");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent,
							"Выберите изображение"), INTENT_IMAGE_PICK);
					break;
				case 1:
					Intent CaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					ImageCaptureUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
					if (hasImageCaptureBug()) {
						CaptureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/tmp")));
					} else {
						CaptureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					}
					//CaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,ImageCaptureUri);
					Log.i("ImageCaptureUri", ImageCaptureUri.toString());
					startActivityForResult(CaptureIntent, INTENT_IMAGE_CAPTURE);
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case INTENT_IMAGE_PICK:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();
				galleryItems.add(selectedImageUri);
				// gallery.setAdapter(imageAdapter);
				imageAdapter.checkUi();
				gallery.setVisibility(View.VISIBLE);
			}
			break;
		case INTENT_IMAGE_CAPTURE:
			 Uri u;
             if (hasImageCaptureBug()) {
                 File fi = new File("/sdcard/tmp");
                 try {
                     u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), fi.getAbsolutePath(), null, null));
                     Log.i("bug_img",u.toString());
                     galleryItems.add(u);
                     imageAdapter.checkUi();
                     if (!fi.delete()) {
                         Log.i("logMarker", "Failed to delete " + fi);
                     }
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 }
             } else {
                if(data != null) {
                	u = data.getData();
                	Log.i("no_img",u.toString());
                	galleryItems.add(u);
                	imageAdapter.checkUi();
                }
            }
			break;
		}
	}

	protected void onGalleryItemClick(final int position) {
		final CharSequence[] items = { "Открыть", "Удалить", "Отмена" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Действия:");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(galleryItems.get(position), "image/*");
					startActivity(intent);
					break;
				case 1:
					GalleryItemRemove(position);
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void GalleryItemRemove(int position) {
		galleryItems.remove(position);
		if (galleryItems.size() < 1) {
			gallery.setVisibility(View.GONE);
		} else {
			imageAdapter.checkUi();
			// gallery.setAdapter(imageAdapter);
		}
		Toast.makeText(ReporterActivity.this, "Удалено", Toast.LENGTH_SHORT)
				.show();
	}


	// Called after onCreate has finished, use to restore UI state
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
	}

	// Called before subsequent visible lifetimes
	// for an activity process.
	@Override
	public void onRestart() {
		super.onRestart();
		// Load changes knowing that the activity has already
		// been visible within this process.
	}

	// Called at the start of the visible lifetime.
	@Override
	public void onStart() {
		super.onStart();
		// Apply any required UI change now that the Activity is visible.
	}

	// Called at the start of the active lifetime.
	@Override
	public void onResume() {
		super.onResume();
		provider = getProvider();
		// Resume any paused UI updates, threads, or processes required
		// by the activity but suspended when it was inactive.
	}
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    imageAdapter.checkUi();
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        //Toast.makeText(this, "landscape "+galleryItems.size(), Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        //Toast.makeText(this, "portrait "+galleryItems.size(), Toast.LENGTH_SHORT).show();
	    }
	}

	// Called to save UI state changes at the
	// end of the active lifecycle.
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		super.onSaveInstanceState(savedInstanceState);
	}

	// Called at the end of the active lifetime.
	@Override
	public void onPause() {
		// Suspend UI updates, threads, or CPU intensive processes
		// that don’t need to be updated when the Activity isn’t
		// the active foreground activity.
		super.onPause();
		locationManager.removeUpdates(this);
	}

	protected String getProvider() {
		if (locationManager == null)
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return locationManager.getBestProvider(criteria, true);
	}

	@Override
	public void onLocationChanged(Location location) {
		latitute = location.getLatitude();
		longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	// Called at the end of the visible lifetime.
	@Override
	public void onStop() {
		// Suspend remaining UI updates, threads, or processing
		// that aren’t required when the Activity isn’t visible.
		// Persist all edits or state changes
		// as after this call the process is likely to be killed.
		super.onStop();
	}

	// Called at the end of the full lifetime.
	@Override
	public void onDestroy() {
		// Clean up any resources including ending threads,
		// closing database connections etc.
		super.onDestroy();
	}

	static final private int MENU_EXIT = Menu.FIRST;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		/*
		 * // Group ID int groupId = 0; // Unique menu item identifier. Used for
		 * event handling. int menuItemId = MENU_EXIT; // The order position of
		 * the item int menuItemOrder = Menu.NONE; // Text to be displayed for
		 * this menu item. int menuItemText = R.string.menu_exit; // Create the
		 * menu item and keep a reference to it. MenuItem menuItem =
		 * menu.add(groupId, menuItemId, menuItemOrder, menuItemText);
		 * 
		 * menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		 * public boolean onMenuItemClick(MenuItem _menuItem) {
		 * 
		 * return true; } });
		 */

		return true;
	}

	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
			TypedArray attr = mContext
					.obtainStyledAttributes(R.styleable.DefTheme);
			mGalleryItemBackground = attr.getResourceId(
					R.styleable.DefTheme_android_galleryItemBackground, 0);
			attr.recycle();
		}

		public int getCount() {
			return galleryItems.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public void checkUi() {
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);

			Uri uri = galleryItems.get(position);
			Bitmap img = decodeFile(uri);

			imageView.setImageBitmap(img);
			imageView.setLayoutParams(new Gallery.LayoutParams(150, -1));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setAdjustViewBounds(true);
			imageView.setBackgroundResource(mGalleryItemBackground);

			return imageView;
		}
	}

	private Bitmap decodeFile(Uri uri) {

		File f = new File(getRealPathFromURI(uri));
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
		} catch (FileNotFoundException e) {
			Log.e("img", "File not found " + getRealPathFromURI(uri));
			e.printStackTrace();
		}

		// The new size we want to scale to
		final int REQUIRED_SIZE = 100;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;

		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
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
			Log.e("img", "File not found");
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	public String getRealPathFromURI(Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	public boolean hasImageCaptureBug() {

		// list of known devices that have the bug
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");

		return devices.contains(android.os.Build.BRAND + "/"
				+ android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);

	}
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	    return mediaFile;
	}

}