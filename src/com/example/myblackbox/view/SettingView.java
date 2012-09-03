package com.example.myblackbox.view;

import java.util.ArrayList;
import java.util.List;

import com.example.myblackbox.R;
import com.example.myblackbox.setting.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.example.myblackbox.etc.GlobalVar;

public class SettingView extends PreferenceActivity {

	private Preference theAppInfo;
	private Preference theCameraResolution;
	private Preference theCameraStorage;
	private Preference theCameraRecordTime;
	private Preference theBluetoothConnection;
	private Preference theBluetoothDisconnection;
	private Preference theWebLogin;
	private Preference theWebLogout;

	/** Global Variable */
	private GlobalVar theGlobalVar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Global Variable Setting
		if (theGlobalVar == null) {
			theGlobalVar = (GlobalVar) getApplicationContext();
		}

		addPreferencesFromResource(R.layout.setting_view);

		theAppInfo = findPreference("setting_app_info");

		theCameraResolution = findPreference("settting_camera_resolution");
		theCameraStorage = findPreference("settting_camera_storage");
		theCameraRecordTime = findPreference("settting_camera_record_time");

		theBluetoothConnection = findPreference("settting_bluetooth_connection");
		theBluetoothDisconnection = findPreference("settting_bluetooth_disconnection");

		theWebLogin = findPreference("settting_web_login");
		theWebLogout = findPreference("settting_web_logout");

		theAppInfo.setOnPreferenceClickListener(thePreferenceListener);

		theCameraResolution.setOnPreferenceClickListener(thePreferenceListener);
		theCameraStorage.setOnPreferenceClickListener(thePreferenceListener);
		theCameraRecordTime.setOnPreferenceClickListener(thePreferenceListener);

		theBluetoothConnection
				.setOnPreferenceClickListener(thePreferenceListener);
		theBluetoothDisconnection
				.setOnPreferenceClickListener(thePreferenceListener);

		theWebLogin.setOnPreferenceClickListener(thePreferenceListener);
		theWebLogout.setOnPreferenceClickListener(thePreferenceListener);

		SharedPreferences thePrefs = getSharedPreferences("settingValues",
				MODE_PRIVATE);
		String theBlueName = thePrefs.getString(GlobalVar.SHARED_BLUE_NAME, "");
		String theBlueAddress = thePrefs.getString(
				GlobalVar.SHARED_BLUE_ADDRESS, "");
		String theWebId = thePrefs.getString(GlobalVar.SHARED_LOGIN_ID, "");
		String theWebIdentity = thePrefs.getString(
				GlobalVar.SHARED_LOGIN_IDENTITY, "");

		String theCameraQuailty = thePrefs.getString(
				GlobalVar.SHARED_CAMERA_QUAILTY, "");
		String theRecordTime = thePrefs.getString(
				GlobalVar.SHARED_CAMERA_RECORD_TIME, "");
		String theStorageSize = thePrefs.getString(
				GlobalVar.SHARED_CAMERA_STORAGE_SIZE, "");

		if (theBlueName.length() == 0 && theBlueAddress.length() == 0) {
			theBluetoothConnection.setTitle("Bluetooth 연결");
			theBluetoothConnection.setSummary("");
			theBluetoothDisconnection.setEnabled(false);

		} else {
			theBluetoothConnection.setTitle("Bluetooth 다른 기기 연결");
			theBluetoothConnection.setSummary("" + theBlueName + "("
					+ theBlueAddress + ")");
			theBluetoothDisconnection.setEnabled(true);
		}

		if (theWebId.length() == 0 && theWebIdentity.length() == 0) {
			theWebLogin.setTitle("Web 로그인");
			theWebLogin.setSummary("");
			theWebLogout.setEnabled(false);
		} else {
			theWebLogin.setTitle("Web 다른 아이디 로그인");
			theWebLogin.setSummary("" + theWebId);
			theWebLogout.setEnabled(true);
		}

		if (theCameraQuailty.length() == 0) {
			Camera mCamera = Camera.open();
			Camera.Parameters params = mCamera.getParameters();
			
			thePrefs = getSharedPreferences("settingValues", MODE_PRIVATE);
			SharedPreferences.Editor thePrefEdit = thePrefs.edit();
			thePrefEdit.putString(GlobalVar.SHARED_CAMERA_QUAILTY, CamcorderProfile.QUALITY_HIGH+"");
			thePrefEdit.commit();
			
			theCameraResolution.setSummary("High Quailty");
		} else {
			
			switch (Integer.parseInt(theCameraQuailty)) {
			case CamcorderProfile.QUALITY_HIGH:
				theCameraResolution.setSummary("High Quailty");	
				break;
			case CamcorderProfile.QUALITY_LOW:
				theCameraResolution.setSummary("Low Quailty");
				break;
			}
			
			
		}

		if (theRecordTime.length() == 0) {
			thePrefs = getSharedPreferences("settingValues", MODE_PRIVATE);
			SharedPreferences.Editor thePrefEdit = thePrefs.edit();
			thePrefEdit.putString(GlobalVar.SHARED_CAMERA_RECORD_TIME, "1");
			thePrefEdit.commit();
			
			theCameraRecordTime.setSummary("1");
		} else {
			theCameraRecordTime.setSummary(theRecordTime+"분");
		}

		if (theStorageSize.length() == 0) {
			thePrefs = getSharedPreferences("settingValues", MODE_PRIVATE);
			SharedPreferences.Editor thePrefEdit = thePrefs.edit();
			thePrefEdit.putString(GlobalVar.SHARED_CAMERA_STORAGE_SIZE, "1");
			thePrefEdit.commit();
			
			theCameraStorage.setSummary("1 GB");
		} else {
			theCameraStorage.setSummary(theStorageSize + " GB");
		}
	}

	OnPreferenceClickListener thePreferenceListener = new OnPreferenceClickListener() {

		public boolean onPreferenceClick(Preference preference) {
			// TODO Auto-generated method stub

			if (preference.getKey().equals("setting_app_info")) {
				/** 앱 정보 */
				GlobalVar.popupToast(SettingView.this, "setting_app_info");
				//Log.e(GlobalVar.TAG, "app info");
			} else if (preference.getKey().equals("settting_camera_resolution")) {
				/** 카메라 해상도 */
				//Log.e(GlobalVar.TAG, "Camera Resolution");
				Intent ResolutionIntent = new Intent(SettingView.this,
						SettingCameraResolution.class);
				startActivityForResult(ResolutionIntent,
						GlobalVar.REQUEST_CAMERA_RESOLUTION);
			} else if (preference.getKey().equals("settting_camera_storage")) {
				/** 동영상 저장 총 용량 */

				Intent StorageIntent = new Intent(SettingView.this,
						SettingCameraStorage.class);
				startActivityForResult(StorageIntent,
						GlobalVar.REQUEST_CAMERA_STORAGE);
			} else if (preference.getKey()
					.equals("settting_camera_record_time")) {
				/** 카메라 저장 시간 */

				Intent RecordTimeIntent = new Intent(SettingView.this,
						SettingCameraRecordTime.class);
				startActivityForResult(RecordTimeIntent,
						GlobalVar.REQUEST_CAMERA_RECORD_TIME);
			} else if (preference.getKey().equals(
					"settting_bluetooth_connection")) {
				/** 블루투스 연결 */

				// Get local Bluetooth adapter
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();

				if (mBluetoothAdapter == null) {
					GlobalVar.popupToast(SettingView.this,
							"블루투스를 사용할 수 없습니다.");
				} else if (!mBluetoothAdapter.isEnabled()) {
					Intent enableBlueIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBlueIntent,
							GlobalVar.REQUEST_ENABLE_BT);
				} else {
					Intent BluetoothConnect = new Intent(SettingView.this,
							SettingBluetoothConnection.class);
					startActivityForResult(BluetoothConnect,
							GlobalVar.REQUEST_CONNECT_DEVICE);
				}
			} else if (preference.getKey().equals(
					"settting_bluetooth_disconnection")) {
				/** 블루투스 연결 해제 */
				AlertDialog.Builder alert_confirm = new AlertDialog.Builder(
						SettingView.this);
				alert_confirm
						.setMessage("Bluetooth 연결을 해제하시겠습니까?")
						.setCancelable(false)
						.setPositiveButton("확인",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// 'YES'
										SharedPreferences thePrefs = getSharedPreferences(
												"settingValues", MODE_PRIVATE);
										SharedPreferences.Editor thePrefEdit = thePrefs
												.edit();
										thePrefEdit
												.remove(GlobalVar.SHARED_BLUE_NAME);
										thePrefEdit
												.remove(GlobalVar.SHARED_BLUE_ADDRESS);
										thePrefEdit.commit();

										theBluetoothConnection
												.setTitle("Bluetooth 연결");
										theBluetoothConnection.setSummary("");
										theBluetoothDisconnection
												.setEnabled(false);

										Message theMsg = theGlobalVar.theBlueCommandHandler
												.obtainMessage(GlobalVar.BLUE_DISCONNECT);
										theGlobalVar.theBlueCommandHandler
												.sendMessage(theMsg);

									}
								})
						.setNegativeButton("취소",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// 'No'

										return;
									}
								});
				AlertDialog alert = alert_confirm.create();
				alert.show();
			} else if (preference.getKey().equals("settting_web_login")) {
				/** 웹 로그인 */

				Intent LoginIntent = new Intent(SettingView.this,
						SettingWebLogin.class);
				startActivityForResult(LoginIntent, GlobalVar.REQUEST_LOGIN);

			} else if (preference.getKey().equals("settting_web_logout")) {
				/** 웹 로그아웃 */
				AlertDialog.Builder alert_confirm = new AlertDialog.Builder(
						SettingView.this);
				alert_confirm
						.setMessage("로그아웃 하시겠습니까?")
						.setCancelable(false)
						.setPositiveButton("확인",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// 'YES'
										SharedPreferences thePrefs = getSharedPreferences(
												"settingValues", MODE_PRIVATE);
										SharedPreferences.Editor thePrefEdit = thePrefs
												.edit();
										thePrefEdit
												.remove(GlobalVar.SHARED_LOGIN_ID);
										thePrefEdit
												.remove(GlobalVar.SHARED_LOGIN_IDENTITY);
										thePrefEdit.commit();

										theWebLogin.setTitle("Web 로그인");
										theWebLogin.setSummary("");
										theWebLogout.setEnabled(false);

									}
								})
						.setNegativeButton("취소",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// 'No'
										return;
									}
								});
				AlertDialog alert = alert_confirm.create();
				alert.show();
			}

			return false;
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GlobalVar.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String theInfo = data.getExtras().getString(
						GlobalVar.EXTRA_DEVICE);
				String theName = theInfo.substring(0, theInfo.length() - 18);
				String theAddress = theInfo.substring(theInfo.length() - 17);

				SharedPreferences thePrefs = getSharedPreferences(
						"settingValues", MODE_PRIVATE);
				SharedPreferences.Editor thePrefEdit = thePrefs.edit();
				thePrefEdit.putString(GlobalVar.SHARED_BLUE_NAME, theName);
				thePrefEdit
						.putString(GlobalVar.SHARED_BLUE_ADDRESS, theAddress);
				thePrefEdit.commit();

				theBluetoothConnection.setTitle("Bluetooth 다른 기기 연결");
				theBluetoothConnection.setSummary("" + theName + "("
						+ theAddress + ")");
				theBluetoothDisconnection.setEnabled(true);

				Message theMsg = theGlobalVar.theBlueCommandHandler
						.obtainMessage(GlobalVar.BLUE_CONNECT);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

			} else {

			}
			break;
		case GlobalVar.REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {

				new Handler().postDelayed(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						Intent BluetoothConnect = new Intent(SettingView.this,
								SettingBluetoothConnection.class);
						startActivityForResult(BluetoothConnect,
								GlobalVar.REQUEST_CONNECT_DEVICE);

					}
				}, 500);
			}
			break;
		case GlobalVar.REQUEST_LOGIN:
			if (resultCode == Activity.RESULT_OK) {
				String theInfo = data.getExtras().getString(GlobalVar.LOGIN);
				String[] theInfos = theInfo.split("/");

				SharedPreferences thePrefs = getSharedPreferences(
						"settingValues", MODE_PRIVATE);
				SharedPreferences.Editor thePrefEdit = thePrefs.edit();
				thePrefEdit.putString(GlobalVar.SHARED_LOGIN_ID, theInfos[0]);
				thePrefEdit.putString(GlobalVar.SHARED_LOGIN_IDENTITY,
						theInfos[1]);
				thePrefEdit.commit();

				theWebLogin.setTitle("Web 다른 아이디 로그인");
				theWebLogin.setSummary(theInfos[0]);
				theWebLogout.setEnabled(true);

			}
			break;
		case GlobalVar.REQUEST_CAMERA_RESOLUTION:
			if (resultCode == Activity.RESULT_OK) {
				String theInfo = data.getExtras().getString(
						GlobalVar.CAMERA_RESOLUTION);
				

				SharedPreferences thePrefs = getSharedPreferences(
						"settingValues", MODE_PRIVATE);
				SharedPreferences.Editor thePrefEdit = thePrefs.edit();
				thePrefEdit.putString(GlobalVar.SHARED_CAMERA_QUAILTY, theInfo);
				thePrefEdit.commit();
				
				switch (Integer.parseInt(theInfo)) {
				case CamcorderProfile.QUALITY_HIGH:
					theCameraResolution.setSummary("High Quailty");	
					break;
				case CamcorderProfile.QUALITY_LOW:
					theCameraResolution.setSummary("Low Quailty");
					break;
				}
			}
			break;
		case GlobalVar.REQUEST_CAMERA_STORAGE:
			if (resultCode == Activity.RESULT_OK) {
				String theInfo = data.getExtras().getString(
						GlobalVar.CAMERA_STORAGE);
				

				SharedPreferences thePrefs = getSharedPreferences(
						"settingValues", MODE_PRIVATE);
				SharedPreferences.Editor thePrefEdit = thePrefs.edit();
				thePrefEdit.putString(GlobalVar.SHARED_CAMERA_STORAGE_SIZE,	theInfo);
				thePrefEdit.commit();

				theCameraStorage.setSummary(theInfo+" GB");

			}
			break;

		case GlobalVar.REQUEST_CAMERA_RECORD_TIME:
			if (resultCode == Activity.RESULT_OK) {
				String theInfo = data.getExtras().getString(
						GlobalVar.CAMERA_RECORD_TIME);

				//Log.e(GlobalVar.TAG, "Time : " + theInfo);

				SharedPreferences thePrefs = getSharedPreferences(
						"settingValues", MODE_PRIVATE);
				SharedPreferences.Editor thePrefEdit = thePrefs.edit();
				thePrefEdit.putString(GlobalVar.SHARED_CAMERA_RECORD_TIME,
						theInfo);
				thePrefEdit.commit();

				theCameraRecordTime.setSummary(theInfo+"분");

			}
			break;
		}
	}

	/** Custom Hardware Button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (GlobalVar.isDebug)
				//Log.e(GlobalVar.TAG, "KeyCode Back");

			finish();

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
//		//Log.e(GlobalVar.TAG,"onDestroy()");
	
	}
}
