package edu.bu.sandboxed.net;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import edu.bu.sandboxed.R;


public class FileTransfer {

	public int DOWNLOAD_BUFFER_SIZE = 1024;

	private final String characterEncoding = "UTF-8";
	/**
	 * URL to post to
	 */
	private final String upLoadServerUri;

	private final String lineEnd = "\r\n";
	private final String twoHyphens = "--";
	private final int maxBufferSize = 1 * 1024 * 1024;

	private Context mContext;

	public FileTransfer(Context context) {
		mContext = context;
        upLoadServerUri = context.getResources().getString(R.string.url_c2_upload);
	}

	public String upload(File sourceFile) throws FileTransferException {
		int serverResponseCode = 0;
        String response = null;
		String fileName = sourceFile.getName();

		 HttpURLConnection conn = null;
		 DataOutputStream dos = null;
		 FileInputStream fis = null;
		
		  Reader in = null;		

		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] writeBuffer;
		char[] readBuffer;
		//File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile()) {

			Log.e("uploadFile", "Source File not exist :" + sourceFile.getAbsolutePath());

			// throw

		}

		// open a URL connection to the Servlet
		try {
			fis = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
					+ fileName + "\"" + lineEnd);

			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			bytesAvailable = fis.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			writeBuffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fis.read(writeBuffer, 0, bufferSize);

			while (bytesRead > 0) {

				dos.write(writeBuffer, 0, bufferSize);
				bytesAvailable = fis.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fis.read(writeBuffer, 0, bufferSize);

			}
			
			

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

		 	StringBuilder sb = new StringBuilder();

			switch (serverResponseCode) {
				case 200:
				case 201:
				case 202:
					    in = new InputStreamReader(conn.getInputStream(),characterEncoding );
						readBuffer = new char[1024];
						int len;
						while ((len = in.read(readBuffer)) > 0){
							sb.append(readBuffer, 0, len);
						}
						break;
				default:
					throw new FileTransferException("Invalid response code: " + serverResponseCode);
			}
			// Response is ok

			//TODO need to get a URL response from the server so we can refresh the view
            response = 	sb.toString();


		} catch (FileNotFoundException e) {
			throw new FileTransferException(e);
		} catch (MalformedURLException e) {
			throw new FileTransferException(e);
		} catch (ProtocolException e) {
			throw new FileTransferException(e);
		} catch (IOException e) {
			throw new FileTransferException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Log.e("FileTransfer",
							"Error closing fis: " + e.getMessage());
				}
			}
			if (dos != null) {
				try {
					dos.flush();
					dos.close();
				} catch (IOException e) {
					Log.e("FileTransfer",
							"Error closing dos: " + e.getMessage());
				}
			}
			
			if (in != null){
				try {
					in.close();
				} catch (IOException e) {
					Log.e("FileTransfer",
							"Error closing dos: " + e.getMessage());
				}
				
			}
		}

		// close the streams //

		return response;

	}

	
	private String getFilenameFromURL(URL url){
		
		String urlPath = url.getPath();
		String [] paths = urlPath.split("/");
		return paths[paths.length - 1];
	}

	
	public class DownloadResponse {
		private File file;
		private String mime;
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
		public String getMime() {
			return mime;
		}
		public void setMime(String mime) {
			this.mime = mime;
		}
	}
}
