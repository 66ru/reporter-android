package reporter66.ru.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import reporter66.ru.ReporterActivity;
import reporter66.ru.models.Post;
import reporter66.ru.models.PostItem;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SendService extends Service {
	private static final String TAG = "SendService";
	public static String URL = "http://new.afisha96.ru/r.php";

	static final int BUFF_SIZE = 1024;
	static final byte[] buffer = new byte[BUFF_SIZE];
	protected static final long DEBUG_DELAY = 1;

	protected String urlServer = "http://bazilio91.ru/r.php";
	protected String lineEnd = "\r\n";
	protected String twoHyphens = "--";
	protected String boundary = "*****";

	private final IBinder mBinder = new MyBinder();

	@Override
	public void onStart(Intent intent, int startid) {
		// super.onStart(intent,startid);
		Log.d(TAG, "onStart");
	}

	public int sendMeta(Post post) {
		Log.i(TAG, "Sending text data to server");
		HttpURLConnection connection = null;
		try {
			java.net.URL url = new java.net.URL(urlServer);
			String param = "text=" + URLEncoder.encode(post.getText(), "UTF-8")
					+ "&title=" + URLEncoder.encode(post.getTitle(), "UTF-8")
					+ "&geo_lat="
					+ URLEncoder.encode(post.getGeo_lat().toString(), "UTF-8")
					+ "&geo_lng="
					+ URLEncoder.encode(post.getGeo_lng().toString(), "UTF-8")
					+ "&uid=" + URLEncoder.encode(post.getUid(), "UTF-8");

			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setFixedLengthStreamingMode(param.getBytes().length);

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(param);
			wr.flush();
			wr.close();

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {

				InputStream in = connection.getInputStream();

				InputStreamReader isr = new InputStreamReader(in, "UTF-8");

				StringBuffer data = new StringBuffer();
				int c;
				while ((c = isr.read()) != -1) {
					if (c > 0)
						data.append((char) c);
				}

				String resultString = new String(data.toString());

				Log.i(TAG, "Response: " + resultString);

				return Integer.parseInt(resultString);
			}

		} catch (MalformedURLException e) {
			Log.e(TAG, "NetDisconeeted");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "NetDisconeeted ");
			e.printStackTrace();
		}
		return -1;

	}

	public void sendFiles(int id) {
		int i = 0;
		Log.i(TAG, "Sending " + ReporterActivity.galleryItems.size()
				+ " files to server");

		ReporterActivity.progressDialog.setProgress(0);
		for (PostItem item : ReporterActivity.galleryItems) {
			i++;
			Log.i(TAG, "Processing file " + i + " of total "
					+ ReporterActivity.galleryItems.size());
			if (item.isSended()) {
				Log.i(TAG, "File already sent, skipping.");
			} else
				uploadFile(item, id, i);
		}
		ReporterActivity.progressDialog.dismiss();
	}

	private static void writeParam(String name, String value,
			DataOutputStream out, String boundary) {
		try {
			out.writeBytes("content-disposition: form-data; name=\"" + name
					+ "\"\r\n\r\n");
			out.writeBytes(value);
			out.writeBytes("\r\n" + "--" + boundary + "\r\n");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	private static void writeFile(String name, String filePath,
			DataOutputStream out, String boundary) {
		try {
			out.writeBytes("content-disposition: form-data; name=\"" + name
					+ "\"; filename=\"" + filePath + "\"\r\n");
			out.writeBytes("content-type: application/octet-stream"
					+ "\r\n\r\n");
			FileInputStream fis = new FileInputStream(filePath);
			while (true) {
				synchronized (buffer) {
					int amountRead = fis.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
			fis.close();
			out.writeBytes("\r\n" + "--" + boundary + "\r\n");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void uploadFile(PostItem item, int id, int current) {

		Log.i(TAG, item.getId() + " >> " + id);
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		String pathToOurFile = item.getPath();

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));
			int totalBytes = bytesAvailable = fileInputStream.available();

			java.net.URL url = new java.net.URL(urlServer);
			connection = null;
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			StringBuilder preData = new StringBuilder();
			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			preData.append(twoHyphens + boundary + lineEnd);
			preData.append("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
					+ pathToOurFile + "\"" + lineEnd);
			preData.append(lineEnd);

			StringBuilder postData = new StringBuilder();

			postData.append(lineEnd);
			postData.append(twoHyphens + boundary + twoHyphens + lineEnd);
			postData.append("\r\n" + "--" + boundary + "\r\n");

			// additional params

			postData.append("content-disposition: form-data; name=\"file_id\"\r\n\r\n");
			postData.append(item.getId() + "");
			postData.append("\r\n" + "--" + boundary + "\r\n");

			postData.append("content-disposition: form-data; name=\"id\"\r\n\r\n");
			postData.append(id + "");
			postData.append("\r\n" + "--" + boundary + "\r\n");

			postData.append("content-disposition: form-data; name=\"type\"\r\n\r\n");
			postData.append(item.getType() + "");
			postData.append("\r\n" + "--" + boundary + "\r\n");

			Log.i(TAG, (preData.toString().getBytes().length + totalBytes)
					+ " - request size");

			connection.setFixedLengthStreamingMode(totalBytes
					+ preData.toString().getBytes().length
					+ postData.toString().getBytes().length);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(preData.toString());

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			int bytesWritten = bufferSize;
			Log.d(TAG, "file read start");
			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				// outputStream.flush();
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				bytesWritten += bufferSize;
				int delta = totalBytes - bytesWritten;
				if (delta > 0) {
					float progress = ((float) bytesWritten / totalBytes)
							* 100
							/ ReporterActivity.galleryItems.size()
							+ (100 / ReporterActivity.galleryItems.size() * (current - 1));
					Log.i(TAG, "" + progress);
					ReporterActivity.progressDialog.setProgress((int) progress);
				}
			}
			Log.d(TAG, "file read end");
			outputStream.writeBytes(postData.toString());

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

			// Responses from the server (code and message)
			Log.i("response", connection.getResponseCode() + "");
			Log.i("response", connection.getResponseMessage());

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {

				InputStream in = connection.getInputStream();

				InputStreamReader isr = new InputStreamReader(in, "UTF-8");

				StringBuffer data = new StringBuffer();
				int c;
				while ((c = isr.read()) != -1) {
					if (c > 0)
						data.append((char) c);
				}

				String resultString = new String(data.toString());
				Log.i(TAG, "Response: " + resultString);

				if (resultString != "") {
					long resultId = Long.parseLong(resultString);

					if (resultId > -1) {
						Log.i(TAG, "Send successfull: " + resultId);
						item.setExternal_id(resultId);
						ReporterActivity.postItemsSource.savePostItem(item);
					}
				} else {
					Log.i(TAG, "Send failed. Server havn't return any id.");
				}
			}

			connection.disconnect();
			Log.d(TAG, "uploadFile successfull");
		} catch (MalformedURLException e) {

			Log.e(TAG, "NetDisconeeted");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "NetDisconeeted");
			e.printStackTrace();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public SendService getService() {
			return SendService.this;
		}
	}
}
