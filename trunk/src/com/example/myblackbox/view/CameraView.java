package com.example.myblackbox.view;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CameraView extends Activity {

	/** Global Variable */
	GlobalVar theGlobalVar;

	/** Test UI */
	Button theStartRecordBtn;
	Button theStopRecordBtn;
	Button theShakeBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		setContentView(R.layout.camera_view);

		if (theGlobalVar == null) {
			theGlobalVar = (GlobalVar) getApplicationContext();
		}
		theGlobalVar.theCameraHandler = mHandler;

		theStartRecordBtn = (Button) findViewById(R.id.start_record);
		theStopRecordBtn = (Button) findViewById(R.id.stop_record);
		theShakeBtn = (Button) findViewById(R.id.shake);
		
		theStartRecordBtn.setOnClickListener(theButtonListener);
		theStopRecordBtn.setOnClickListener(theButtonListener);
		theShakeBtn.setOnClickListener(theButtonListener);

//		Message theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
//				GlobalVar.BLUE_REQ_OBD_INFO, 10, 0);
//		theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);
	}

	OnClickListener theButtonListener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub

			Message theMsg;
			switch (v.getId()) {
			case R.id.start_record:
				theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
						GlobalVar.BLUE_REQ_OBD_INFO, 10, 0);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

				break;
			case R.id.stop_record:
				theMsg = theGlobalVar.theBlueCommandHandler
						.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);
				break;
			case R.id.shake:

				break;

			default:
				break;
			}

		}
	};

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalVar.OBD_INFO_FROM_CAMERA:

				// 데이터 들어왔을때

				OBD_Info theInputData = (OBD_Info) msg.obj;
				Log.e(GlobalVar.TAG, "Camera : " + theInputData.getObdDate());

				break;
			}
		}
	};

	/** Custom Hardware Button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Back");

			Message theMsg = theGlobalVar.theBlueCommandHandler
					.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

			if (theGlobalVar == null) {
				theGlobalVar = (GlobalVar) getApplicationContext();
			}

			theGlobalVar.theCameraHandler = null;

			finish();

			return false;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Valume Up");
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Balume Down");
			return false;
		}

		return true;
	}
}
