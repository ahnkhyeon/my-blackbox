package com.example.myblackbox.setting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.gsHttpConnect;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingWebLogin extends Activity {
	
	EditText theLoginID;
	EditText theLoginPW;

	Button theCancel;
	Button theLogin;

	String theUserID;
	String theUserPass;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting_web_login);
	    // TODO Auto-generated method stub
	    
		setResult(Activity.RESULT_CANCELED);

	    theLoginID = (EditText) findViewById(R.id.loginID);
	    theLoginPW = (EditText) findViewById(R.id.loginPW);
	    
	    theCancel = (Button) findViewById(R.id.loginCancel);
	    theCancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_CANCELED);
				
			}
		});
	    
	    theLogin = (Button) findViewById(R.id.loginBtn);
	    theLogin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if (theLoginID.getText().length()==0) {
					GlobalVar.popupToast(SettingWebLogin.this, "아이디를 입력해 주세요.");
				} else if (theLoginPW.getText().length() == 0) {
					GlobalVar.popupToast(SettingWebLogin.this, "패스워드를 입력해 주세요.");
				} else {
					showDialog(GlobalVar.DIALOG_PROGRESS_ID);
				} 
				
			}
		});
	}
	final Handler handler = new Handler() {
		public void handleMessage(Message theMsg) {
			
			switch (theMsg.what) {
			case GlobalVar.LOGIN_FLAG_OK:
				GlobalVar.popupToast(SettingWebLogin.this, (String) theMsg.obj);
				
				
				
				dismissDialog(GlobalVar.DIALOG_PROGRESS_ID);
				removeDialog(GlobalVar.DIALOG_PROGRESS_ID);
				
				Intent intent = new Intent();
				intent.putExtra(GlobalVar.LOGIN, (String)theMsg.obj);
				setResult(Activity.RESULT_OK, intent);
				finish();
								
				
				break;
			case GlobalVar.LOGIN_FLAG_ERROR:
				GlobalVar.popupToast(SettingWebLogin.this, (String) theMsg.obj);
				
				dismissDialog(GlobalVar.DIALOG_PROGRESS_ID);
				removeDialog(GlobalVar.DIALOG_PROGRESS_ID);

				break;
			}
			
		
		}
	};

	
	private class ProgressThread extends Thread {
		Handler mHandler;

		public ProgressThread(Handler theHandler) {
			// TODO Auto-generated constructor stub
			mHandler = theHandler;
		}

		public void run() {
			
			
			gsHttpConnect com = new gsHttpConnect();

			Map<String, Object> params = new HashMap<String, Object>();

			params.put("user_id", theLoginID.getText());
			params.put("user_pw", theLoginPW.getText());

			try {
				String theResult = com.request(new URL(GlobalVar.theURL+"loginMobile.php"), "POST",
						params);
				Log.e(GlobalVar.TAG, theResult);

				XmlPullParserFactory parserCreator = XmlPullParserFactory
						.newInstance();
				XmlPullParser theParser = parserCreator.newPullParser();

				InputStream theInputStream = new ByteArrayInputStream(
						theResult.getBytes("UTF-8"));
				theParser.setInput(theInputStream, null);

				int parserEvent = theParser.getEventType();


				String theXmlTag = "";
				String ErrorMsg = "";
				theUserID = "";
				theUserPass = "";
				

				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					switch (parserEvent) {
					case XmlPullParser.TEXT:

						if (theXmlTag.equals("error")) {
							ErrorMsg = theParser.getText();
						} else if (theXmlTag.equals("id")) {
							theUserID = theParser.getText();
						} else if (theXmlTag.equals("identity")) {
							theUserPass = theParser.getText();
						}
						break;
					case XmlPullParser.END_TAG:
						theXmlTag = "";

						break;
					case XmlPullParser.START_TAG:
						theXmlTag = theParser.getName();
						break;
					}
					parserEvent = theParser.next();
				}
				
				
				Message theMsg;
				if (theUserID.length() != 0 & theUserPass.length() != 0) {
					theMsg = mHandler.obtainMessage(GlobalVar.LOGIN_FLAG_OK, theUserID+"/"+theUserPass);
				} else {
					theMsg = mHandler.obtainMessage(GlobalVar.LOGIN_FLAG_ERROR,ErrorMsg);
				}
				mHandler.sendMessage(theMsg);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case GlobalVar.DIALOG_PROGRESS_ID:
			ProgressDialog progressDialog = new ProgressDialog(SettingWebLogin.this);
			progressDialog.setMessage("로그인 중입니다.");
			new ProgressThread(handler).start();
			return progressDialog;
		}
		return null;
	}

}
