package com.example.myblackbox.view;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.BluetoothService;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OBD_View extends Activity {

	TextView valueEngineRPM;
	TextView valueEngineTemp;
	TextView valueThrottlePos;
	TextView valueAirFlow;
	TextView valueSpeed;

	ProgressBar gageEngineRPM;
	ProgressBar gageEngineTemp;
	ProgressBar gageThrottlePos;
	ProgressBar gageAirFlow;
	ProgressBar gageSpeed;

	GlobalVar theGlobalVar;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.obd_view);

		// TODO Auto-generated method stub
		if (theGlobalVar == null) {
			theGlobalVar = (GlobalVar) getApplicationContext();
		}

		valueEngineRPM = (TextView) findViewById(R.id.valueEngineRPM);
		valueEngineTemp = (TextView) findViewById(R.id.valueEngineTemp);
		valueThrottlePos = (TextView) findViewById(R.id.valueThrottlePos);
		valueAirFlow = (TextView) findViewById(R.id.valueAirFlow);
		valueSpeed = (TextView) findViewById(R.id.valueSpeed);

		gageEngineRPM = (ProgressBar) findViewById(R.id.gageEngineRPM);
		gageEngineTemp = (ProgressBar) findViewById(R.id.gageEngineTemp);
		gageThrottlePos = (ProgressBar) findViewById(R.id.gageThrottlePos);
		gageAirFlow = (ProgressBar) findViewById(R.id.gageAirFlow);
		gageSpeed = (ProgressBar) findViewById(R.id.gageSpeed);

		gageEngineRPM.setMax(16384);
		gageEngineTemp.setMax(255);
		gageThrottlePos.setMax(100);
		gageAirFlow.setMax(655);
		gageSpeed.setMax(255);

		
		CheckBluetooth();
		
		
		
		if(theGlobalVar.getBlueState() == BluetoothService.STATE_CONNECTED) {
			theGlobalVar.theObdHandler = mHandler;

			Message theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
					GlobalVar.BLUE_REQ_OBD_INFO, 1, 0);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);
		}

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalVar.OBD_INFO_FROM_OBD:

				// 데이터 들어왔을때

				OBD_Info theInputData = (OBD_Info) msg.obj;

				valueEngineRPM.setText(theInputData.getObdEngineRpm());
				valueEngineTemp.setText(theInputData.getObdEngineTemp());
				valueThrottlePos.setText(theInputData.getObdTrottlePos());
				valueAirFlow.setText(theInputData.getObdAirFlow());
				valueSpeed.setText(theInputData.getObdSpeed());

				gageEngineRPM.setProgress((int) Double.parseDouble(theInputData
						.getObdEngineRpm()));
				gageEngineTemp.setProgress((int) Double
						.parseDouble(theInputData.getObdEngineTemp()) + 40);
				gageThrottlePos.setProgress((int) Double
						.parseDouble(theInputData.getObdTrottlePos()));
				gageAirFlow.setProgress((int) Double.parseDouble(theInputData
						.getObdAirFlow()));
				gageSpeed.setProgress((int) Double.parseDouble(theInputData
						.getObdSpeed()));

				break;
			}
		}
	};

	private boolean CheckBluetooth() {
		String theBlueName = theGlobalVar
				.getSharedPref(GlobalVar.SHARED_BLUE_NAME);

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			GlobalVar.popupToast(OBD_View.this, "블루투스가 꺼져있습니다.");
			return false;
		} else if (theBlueName.length() == 0) {
			GlobalVar.popupToast(OBD_View.this, "블루투스 정보를 입력해주세요.");
			return false;
		} else if (theGlobalVar.getBlueState() != BluetoothService.STATE_CONNECTED) {
			GlobalVar.popupToast(OBD_View.this, "OBD Server에 연결되어 있지 않습니다.");
			return false;
		}

		return true;
	}
	

	/** Custom Hardware Button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (GlobalVar.isDebug)
				// Log.e(GlobalVar.TAG, "KeyCode Back");

				finish();

			return false;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (GlobalVar.isDebug)
				// Log.e(GlobalVar.TAG, "KeyCode Valume Up");
				return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (GlobalVar.isDebug)
				// Log.e(GlobalVar.TAG, "KeyCode Balume Down");
				return false;
		}

		return true;
	}

	/** Android Life Cycle */
	@Override
	public void onStart() {
		super.onStart();
		// //Log.e(GlobalVar.TAG,"onStart()");
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// //Log.e(GlobalVar.TAG,"onRestart()");

	}

	@Override
	public void onResume() {
		super.onResume();
		// //Log.e(GlobalVar.TAG,"onResume()");
	}

	@Override
	public void onPause() {
		super.onPause();
		// //Log.e(GlobalVar.TAG,"onPause()");

	}

	@Override
	public void onStop() {
		super.onStop();
		// //Log.e(GlobalVar.TAG,"onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// //Log.e(GlobalVar.TAG,"onDestroy()");

		
		if(theGlobalVar.getBlueState() == BluetoothService.STATE_CONNECTED) {
		
			Message theMsg = theGlobalVar.theBlueCommandHandler
					.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

			if (theGlobalVar == null) {
				theGlobalVar = (GlobalVar) getApplicationContext();
			}

			theGlobalVar.theObdHandler = null;
		}

	}
}
