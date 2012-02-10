package reporter66.ru.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import reporter66.ru.ReporterActivity;
import reporter66.ru.models.PostItem;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SendService extends Service {
	private static final String TAG = "SendService";
	public static String URL = "http://new.afisha96.ru/r.php";

	static final int BUFF_SIZE = 1024;
	static final byte[] buffer = new byte[BUFF_SIZE];
	protected static final long DEBUG_DELAY = 1;

	private final IBinder mBinder = new MyBinder();

	@Override
	public void onStart(Intent intent, int startid) {
		// super.onStart(intent,startid);
		Log.d(TAG, "onStart");
	}

	public void sendFiles() {
		int i = 0;
		Log.i(TAG, "Sending " + ReporterActivity.galleryItems.size()
				+ "files to server");

		for (PostItem item : ReporterActivity.galleryItems) {
			ReporterActivity.progressDialog.setProgress(i);
			i++;
			Log.i(TAG, "Processing file " + i + " of total "
					+ ReporterActivity.galleryItems.size());
			uploadFile(item);
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

	public void uploadFile(PostItem item) {
		String response = null;

		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;

		String pathToOurFile = item.getPath();
		String urlServer = "http://bazilio91.ru/r.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));

			java.net.URL url = new java.net.URL(urlServer);
			connection = null;
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");

			// file send

			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
							+ pathToOurFile + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			Log.d(TAG, "file read start");
			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			Log.d(TAG, "file read end");

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);
			outputStream.writeBytes("\r\n" + "--" + boundary + "\r\n");

			// additional params

			outputStream
					.writeBytes("content-disposition: form-data; name=\"file_id\"\r\n\r\n");
			outputStream.writeBytes(item.getId() + "");
			outputStream.writeBytes("\r\n" + "--" + boundary + "\r\n");

			outputStream
					.writeBytes("content-disposition: form-data; name=\"id\"\r\n\r\n");
			outputStream.writeBytes(item.getPost_id() + "");
			outputStream.writeBytes("\r\n" + "--" + boundary + "\r\n");

			// Responses from the server (code and message)
			Log.i("response", connection.getResponseCode() + "");
			Log.i("response", connection.getResponseMessage());

			response = connection.getResponseCode() + "";

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
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
