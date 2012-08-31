package com.example.myblackbox.view;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainView extends Activity {

	/** UI Variable */

	private Button theViewCameraBtn;
	private Button theViewVideoBtn;
	private Button theViewObdBtn;
	private Button theSettingBtn;


	
	/** Global Variable */
	private GlobalVar theGlobalVar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_view);
		
		// Global Variable Setting;
		theGlobalVar = (GlobalVar) getApplicationContext();

		// UI Setting
		theViewCameraBtn = (Button) findViewById(R.id.view_camera);
		theViewVideoBtn = (Button) findViewById(R.id.view_video);
		theViewObdBtn = (Button) findViewById(R.id.view_obd);
		theSettingBtn = (Button) findViewById(R.id.setting_view);

		theViewCameraBtn.setOnClickListener(theButtonListener);
		theViewVideoBtn.setOnClickListener(theButtonListener);
		theViewObdBtn.setOnClickListener(theButtonListener);
		theSettingBtn.setOnClickListener(theButtonListener);

	}

	private final OnClickListener theButtonListener = new OnClickListener() {

		public void onClick(View theView) {
			// TODO Auto-generated method stub

			switch (theView.getId()) {
			case R.id.view_camera:
				setCurrentView(GlobalVar.CURRENT_CAMERA_VIEW);
				Intent theCameraViewIntent = new Intent(MainView.this, CameraView.class);
				startActivityForResult(theCameraViewIntent, GlobalVar.CURRENT_CAMERA_VIEW);
 
				break;
			case R.id.view_video:
				setCurrentView(GlobalVar.CURRENT_VIDEO_VIEW);
				Intent theVideoViewIntent = new Intent(MainView.this, VideoView.class);
				startActivityForResult(theVideoViewIntent, GlobalVar.CURRENT_VIDEO_VIEW);

				break;
			case R.id.view_obd:
				setCurrentView(GlobalVar.CURRENT_OBD_VIEW);
 
				Intent theObdViewIntent = new Intent(MainView.this,
						OBD_View.class);
				startActivityForResult(theObdViewIntent, GlobalVar.CURRENT_OBD_VIEW);

				break;
			case R.id.setting_view:
				// TODO Auto-generated method stub
				setCurrentView(GlobalVar.CURRENT_SETTING_VIEW);
				startActivity(new Intent(MainView.this, SettingView.class));
				break;

			default:
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case GlobalVar.CURRENT_CAMERA_VIEW:

			break;
		case GlobalVar.CURRENT_VIDEO_VIEW:

			break;
		case GlobalVar.CURRENT_OBD_VIEW:
			

			break;
		case GlobalVar.CURRENT_SETTING_VIEW:

			break;

		default:
			break;
		}
		setCurrentView(GlobalVar.CURRENT_MAIN_VIEW);
	}

	public synchronized int getCurrentView() {
		return theGlobalVar.getCurrentView();
	}

	private void setCurrentView(int theView) {
		if (GlobalVar.isDebug) {
			//Log.e(GlobalVar.TAG, "setCurrentView() : "
//					+ getCurrentViewName(theGlobalVar.getCurrentView()) + " -> "
//					+ getCurrentViewName(theView));
		}
		theGlobalVar.setCurrentView(theView);
		
		
	}

	private String getCurrentViewName(int theView) {
		switch (theView) {
		case GlobalVar.CURRENT_MAIN_VIEW:
			return "CURRENT_MAIN_VIEW";
		case GlobalVar.CURRENT_CAMERA_VIEW:
			return "CURRENT_CAMERA_VIEW";
		case GlobalVar.CURRENT_VIDEO_VIEW:
			return "CURRENT_VIDEO_VIEW";
		case GlobalVar.CURRENT_OBD_VIEW:
			return "CURRENT_OBD_VIEW";
		case GlobalVar.CURRENT_SETTING_VIEW:
			return "CURRENT_SETTING_VIEW";
		}
		return "CURRENT_NONE";
	}

	/** Custom Hardware Button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (GlobalVar.isDebug)
				//Log.e(GlobalVar.TAG, "KeyCode Back");
			return false;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (GlobalVar.isDebug)
				//Log.e(GlobalVar.TAG, "KeyCode Valume Up");
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (GlobalVar.isDebug)
				//Log.e(GlobalVar.TAG, "KeyCode Balume Down");
			return false;
		}

		return true;
	}

	/** Android Life Cycle */
	@Override
	public void onStart() {
		super.onStart();
//		//Log.e(GlobalVar.TAG,"onStart()");
	}

	@Override
	public void onRestart() {
		super.onRestart();
//		//Log.e(GlobalVar.TAG,"onRestart()");

	}

	@Override
	public void onResume() {
		super.onResume();
//		//Log.e(GlobalVar.TAG,"onResume()");
		
		if(getCurrentView() != GlobalVar.CURRENT_MAIN_VIEW) {
			setCurrentView(GlobalVar.CURRENT_MAIN_VIEW);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
//		//Log.e(GlobalVar.TAG,"onPause()");

	}

	@Override
	public void onStop() {
		super.onStop();
//		//Log.e(GlobalVar.TAG,"onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
