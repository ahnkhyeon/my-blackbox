package com.example.myblackbox.etc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUploader {
	
	
	private HttpURLConnection connection = null;
	private DataOutputStream outputStream = null;
	private DataInputStream inputStream = null;

	private String pathToOurFile;
	//= "/sdcard/file_to_send.mp3"; //complete path of file from your android device
	private String urlServer;
	private final static String lineEnd = "\r\n";
	private final static String twoHyphens = "--";
	private final static String boundary =  "*****";
	
	private int serverResponseCode;
	private String serverResponseMessage;
	
	public FileUploader(String thePath, String theUrl) {
		pathToOurFile = thePath;
		urlServer = theUrl;
		 
	}
	
	public String uploadFile() {
		try {
			FileInputStream theFileInputStream = new FileInputStream(new File(pathToOurFile));
			
			URL theURL = new URL(urlServer);
			connection = (HttpURLConnection) theURL.openConnection();
			
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			//Enable POST method
			connection.setRequestMethod("POST");
			
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);
			
			

			byte sAvailable = (byte) theFileInputStream.available();

			byte []buffer = new byte[4096];
			int read = 0;
			while ( (read = theFileInputStream.read(buffer)) != -1 ) {
			    outputStream.write(buffer, 0, read);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			

			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();

			theFileInputStream.close();
			outputStream.flush();
			outputStream.close();
			
			return serverResponseMessage;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
	