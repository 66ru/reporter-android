package reporter66.ru;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import reporter66.ru.models.PostItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
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
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ReporterActivity extends Activity implements LocationListener {

	/* intent codes */
	private static final int INTENT_IMAGE_PICK = 1;
	private static final int INTENT_IMAGE_CAPTURE = 2;

	private static final int INTENT_VIDEO_PICK = 11;
	private static final int INTENT_VIDEO_CAPTURE = 12;

	private static final int INTENT_AUDIO_PICK = 21;
	private static final int INTENT_AUDIO_CAPTURE = 22;

	/* geo */
	private LocationManager locationManager;
	private String provider;
	private double longitude;
	private double latitute;

	/* elements */
	private Button submit;
	private ProgressDialog dialog;
	private ImageButton add_photo;
	private EditText fullText;

	/* media */
	private static final int THUMBNAIL_SIZE = 150;

	private ImageAdapter imageAdapter;
	private Gallery gallery;
	private List<PostItem> galleryItems = new ArrayList<PostItem>();

	private Uri ImageCaptureUri;

	private static final int TYPE_IMAGE = 0;
	private static final int TYPE_VIDEO = 1;
	private static final int TYPE_AUDIO = 2;
	
	/* db */
	private PostDataSource postDataSource;
	private PostItemDataSource postItemsSource;

	// Called at the start of the full lifetime.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("action", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
		// Initialize activity.
		
		postDataSource = new PostDataSource(this);
		postDataSource.open();
		
		postItemsSource = new PostItemDataSource(this);
		postItemsSource.open();
		
		galleryItems = postItemsSource.getAllPostItems();
		Log.i("onCreate", "Loaded " + galleryItems.size() + " PostItems from db.");
		
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
			imageAdapter.checkUi();
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
		if (fullText == null) {
			fullText = (EditText) findViewById(R.id.fullText);
		}

		Intent incomingIntent = getIntent();
		Log.i("intent", incomingIntent.toString());
		if (incomingIntent != null) {
			Bundle data = incomingIntent.getExtras();
			String type = incomingIntent.getType();
			if (type == null)
				return;
			String[] fullType = type.split("/", 0);
			if (fullType[0].startsWith("image")) {
				Uri image = (Uri) data.get(Intent.EXTRA_STREAM);
				File v = new File(getRealPathFromURI(image));
				if (v.canRead() && v.isFile()) {
					Log.d("income_image", image.toString());
					
					galleryItems.add(postItemsSource.createPostItem(image, TYPE_IMAGE, 0));
					imageAdapter.checkUi();
				}
			} else if (fullType[0].startsWith("video")) {
				Uri video = (Uri) data.get(Intent.EXTRA_STREAM);
				File v = new File(getRealPathFromURI(video));
				if (v.canRead() && v.isFile()) {
					Log.d("income_video", video.toString());
					
					galleryItems.add(postItemsSource.createPostItem(video, TYPE_VIDEO, 0));
					imageAdapter.checkUi();
				}
			} else if (fullType[0].startsWith("audio")) {
				Uri audio = (Uri) data.get(Intent.EXTRA_STREAM);
				File v = new File(getRealPathFromURI(audio));
				if (v.canRead() && v.isFile()) {
					Log.d("income_audio", audio.toString());
					
					galleryItems.add(postItemsSource.createPostItem(audio, TYPE_AUDIO, 0));
					imageAdapter.checkUi();
				}
			} else if (fullType[0].startsWith("text")) {
				CharSequence text = (CharSequence) data.get(Intent.EXTRA_TEXT);
				Log.i("income", "text recieved: " + text);
				fullText.setText(text, TextView.BufferType.EDITABLE);
			} else {
				Log.i("income", fullType[0].getClass().toString());
			}
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
		final CharSequence[] items = { "Фото из галереи", "Сделать фото",
				"Видео из галереи", "Снять видео", "Аудио" };

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
					Intent CaptureIntent = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE);
					ImageCaptureUri = Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(),
							"reporter66_temp.jpg"));
					CaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							ImageCaptureUri);
					Log.i("ImageCaptureUri", ImageCaptureUri.toString());
					startActivityForResult(CaptureIntent, INTENT_IMAGE_CAPTURE);
					break;
				case 2:
					Intent VideoIntent = new Intent();
					VideoIntent.setType("video/*");
					VideoIntent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(
							Intent.createChooser(VideoIntent, "Выберите ролик"),
							INTENT_VIDEO_PICK);
					break;
				case 3:
					Intent VideoCaptureIntent = new Intent();
					VideoCaptureIntent
							.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
					startActivityForResult(VideoCaptureIntent,
							INTENT_VIDEO_CAPTURE);
					break;
				case 4:
					Intent AudioIntent = new Intent();
					AudioIntent.setType("audio/*");
					AudioIntent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(AudioIntent,
							"Выберите запись"), INTENT_AUDIO_PICK);
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("action", "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INTENT_IMAGE_PICK:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();
				
				galleryItems.add(postItemsSource.createPostItem(selectedImageUri, TYPE_IMAGE, 0));
				imageAdapter.checkUi();
			}
			break;
		case INTENT_VIDEO_PICK:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedVideoUri = data.getData();
				Log.i("INTENT_VIDEO_PICK",
						"Selected uri: " + selectedVideoUri.toString());
				
				galleryItems.add(postItemsSource.createPostItem(selectedVideoUri, TYPE_VIDEO, 0));
				imageAdapter.checkUi();
			}
			break;

		case INTENT_IMAGE_CAPTURE:
			Uri u;
			if (resultCode == Activity.RESULT_OK) {
				try {
					File f = new File(ImageCaptureUri.getPath());
					u = Uri.parse(android.provider.MediaStore.Images.Media
							.insertImage(getContentResolver(),
									f.getAbsolutePath(), null, null));
					f.delete();
					Log.i("INTENT_IMAGE_CAPTURE", "Uri: " + u.toString());
					
					galleryItems.add(postItemsSource.createPostItem(u, TYPE_IMAGE, 0));
					imageAdapter.checkUi();
				} catch (FileNotFoundException e) {
					Toast.makeText(
							ReporterActivity.this,
							"Не удалось получить файл, попробуйте загрузить через галлерею.",
							Toast.LENGTH_SHORT).show();
					Log.e("INTENT_IMAGE_CAPTURE", "File not found: "
							+ ImageCaptureUri.getPath());
					e.printStackTrace();
				}
			} else
				Log.i("INTENT_IMAGE_CAPTURE", "resutCode is abnormal");
			break;
		case INTENT_VIDEO_CAPTURE:
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					Uri newVideoUri = data.getData();
					if (newVideoUri != null) {
						galleryItems.add(postItemsSource.createPostItem(newVideoUri, TYPE_VIDEO, 0));
						imageAdapter.checkUi();
					} else {
						Log.i("INTENT_IMAGE_CAPTURE", "data returned no uri");
					}
				} else {
					Log.i("INTENT_IMAGE_CAPTURE", "data is null");
				}
			} else
				Log.i("INTENT_IMAGE_CAPTURE", "resutCode is abnormal");
			break;
		case INTENT_AUDIO_PICK:
			if (resultCode == Activity.RESULT_OK) {

				Uri selectedAudioUri = data.getData();
				Log.i("INTENT_AUDIO_PICK",
						"Selected uri: " + selectedAudioUri.toString());

				galleryItems.add(postItemsSource.createPostItem(selectedAudioUri, TYPE_AUDIO, 0));
				imageAdapter.checkUi();
			} else
				Log.i("INTENT_IMAGE_CAPTURE", "resutCode is abnormal");
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
					PostItem curItem = galleryItems.get(position);
					String mime = "*/*";
					switch (curItem.getType()) {
					case TYPE_IMAGE:
						mime = "image/*";
						break;
					case TYPE_VIDEO:
						mime = "video/*";
						break;
					case TYPE_AUDIO:
						mime = "audio/*";
						break;
					}
					intent.setDataAndType(curItem.getUri(), mime);
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
		postItemsSource.deletePostItem(galleryItems.get(position));
		galleryItems.remove(position);
		imageAdapter.checkUi();
		Toast.makeText(ReporterActivity.this, "Удалено", Toast.LENGTH_SHORT)
				.show();
	}

	// Called after onCreate has finished, use to restore UI state
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i("action", "onRestoreIstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
	}

	// Called before subsequent visible lifetimes
	// for an activity process.
	@Override
	public void onRestart() {
		Log.i("action", "onRestart");
		super.onRestart();
		// Load changes knowing that the activity has already
		// been visible within this process.
	}

	// Called at the start of the visible lifetime.
	@Override
	public void onStart() {
		Log.i("action", "onStart");
		super.onStart();
		// Apply any required UI change now that the Activity is visible.
	}

	// Called at the start of the active lifetime.
	@Override
	public void onResume() {
		Log.i("action", "onResume");
		super.onResume();
		provider = getProvider();
		// Resume any paused UI updates, threads, or processes required
		// by the activity but suspended when it was inactive.
	}

	public void onConfigurationChanged(Configuration newConfig) {
		Log.i("action", "onConfigChanged");
		super.onConfigurationChanged(newConfig);
		imageAdapter.checkUi();
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// Toast.makeText(this, "landscape "+galleryItems.size(),
			// Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Toast.makeText(this, "portrait "+galleryItems.size(),
			// Toast.LENGTH_SHORT).show();
		}
	}

	// Called to save UI state changes at the
	// end of the active lifecycle.
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.i("action", "onSaveInstanceState");
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		super.onSaveInstanceState(savedInstanceState);
	}

	// Called at the end of the active lifetime.
	@Override
	public void onPause() {
		Log.i("action", "onPause");
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
		Log.i("action", "onLocationChanged");
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
		Log.i("action", "onStop");
		// Suspend remaining UI updates, threads, or processing
		// that aren’t required when the Activity isn’t visible.
		// Persist all edits or state changes
		// as after this call the process is likely to be killed.
		super.onStop();
	}

	// Called at the end of the full lifetime.
	@Override
	public void onDestroy() {
		Log.i("action", "onDestroy");
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
			Log.i("action", "checkUI");
			if (galleryItems.size() > 0) {
				gallery.setVisibility(View.VISIBLE);
				notifyDataSetChanged();
			} else
				gallery.setVisibility(View.GONE);
			System.gc();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);

			PostItem item = galleryItems.get(position);
			switch (item.getType()) {
			case TYPE_IMAGE:
				Bitmap img = decodeImageFile(item.getUri());
				if (img != null) {
					imageView.setImageBitmap(img);
					imageView
							.setLayoutParams(new Gallery.LayoutParams(185, 150));
					imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					imageView.setAdjustViewBounds(true);
					imageView.setBackgroundResource(mGalleryItemBackground);

					return imageView;
				}
				break;
			case TYPE_VIDEO:
				int fileID = Integer.parseInt(item.getUri()
						.getLastPathSegment());
				ContentResolver crThumb = getContentResolver();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(
						crThumb, fileID,
						MediaStore.Video.Thumbnails.MICRO_KIND, options);

				if (curThumb != null) {
					imageView.setImageBitmap(curThumb);
					imageView
							.setLayoutParams(new Gallery.LayoutParams(185, 150));
					imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					imageView.setAdjustViewBounds(true);
					imageView.setBackgroundResource(mGalleryItemBackground);
					return imageView;
				} else {
					Log.e("decodeImageFile", "File not found "
							+ getRealPathFromURI(item.getUri()));
				}

				break;
			case TYPE_AUDIO:
				imageView.setImageResource(android.R.drawable.ic_media_play);
				imageView.setLayoutParams(new Gallery.LayoutParams(150, 150));
				imageView.setScaleType(ImageView.ScaleType.CENTER);
				imageView.setAdjustViewBounds(true);
				imageView.setBackgroundResource(mGalleryItemBackground);
				return imageView;
			}
			return null;
		}
	}

	private Bitmap decodeImageFile(Uri uri) {

		File f = new File(getRealPathFromURI(uri));
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
		} catch (FileNotFoundException e) {
			Log.e("decodeImageFile", "File not found "
					+ getRealPathFromURI(uri));
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
}