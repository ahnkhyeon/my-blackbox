package com.example.myblackbox;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.myblackbox.etc.BluetoothService;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;
import com.example.myblackbox.view.MainView;
import com.example.myblackbox.view.SettingView;

public class MyBlackBox extends Activity {

	/** Global Variable */
	private GlobalVar theGlobalVar;

	/** Bluetooth Variable */
	private BluetoothAdapter theBluetoothAdapter;
	private BluetoothService theBluetoothService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_black_box);

		// Setting Global Variable
		theGlobalVar = (GlobalVar) getApplicationContext();
		theGlobalVar.theBlueCommandHandler = mBlueCommandHandler;

		// Setting Bluetooth
		theBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (theBluetoothAdapter == null) {
			GlobalVar.popupToast(MyBlackBox.this, "Bluetooth is not available");

			new Handler().postDelayed(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					finish();
				}
			}, 3000);
		} else if (!theBluetoothAdapter.isEnabled()) {
			Intent enableBlueIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBlueIntent,
					GlobalVar.REQUEST_ENABLE_BT);
		} else {
			connectBluetooth(true);
		}
		
		// Video Storage
		File thePath = new File(GlobalVar.VIDEO_PATH);
		if(!thePath.isDirectory()) {
			thePath.mkdirs();
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case GlobalVar.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns

			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session

				new Handler().postDelayed(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						connectBluetooth(true);
					}
				}, 500);
			} else {
				// User did not enable Bluetooth or an error occured
				GlobalVar.popupToast(MyBlackBox.this, "블루투스를 실행해 주세요.");
				startActivity(new Intent(MyBlackBox.this, MainView.class));
			}

		}
	}

	private void connectBluetooth(boolean isStart) {
		String theBlueAddress = theGlobalVar.getSharedPref("BlueAddress");

		if (theBlueAddress.length() != 0) {
			if (theBluetoothService == null) {
				theBluetoothService = new BluetoothService(mHandler);
			}
				BluetoothDevice theDevice = theBluetoothAdapter
						.getRemoteDevice(theBlueAddress);
				theBluetoothService.connect(theDevice);
			
		} else {
			GlobalVar.popupToast(MyBlackBox.this, "블루투스를 등록해 주세요.");
		}

		if (isStart) {
			startActivity(new Intent(MyBlackBox.this, MainView.class));
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			OBD_Info theObdInfo;
			Message theMsg;
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					GlobalVar.popupToast(MyBlackBox.this,
							"OBD Server와 연결되었습니다.");

					// String theSendString = "" + BLUE_REQ_OBD_INFO;
					// setState(BLUE_REQ_OBD_INFO);
					// mBluetoothService.write(theSendString.getBytes());

					break;
				case BluetoothService.STATE_CONNECTING:
					GlobalVar
							.popupToast(MyBlackBox.this, "OBD Server와 연결중입니다.");
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					GlobalVar.popupToast(MyBlackBox.this,
							"OBD Server와 연결이 끊겼습니다.");

					// setState(BLUE_OBD_NONE);
					break;
				}

				break;
			case BluetoothService.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				if (GlobalVar.isDebug) {
					Log.e(GlobalVar.TAG, "Send : " + writeMessage);
				}
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case BluetoothService.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// popupToast(readMessage);

				String[] theSplit = readMessage.split("/");

//				if (GlobalVar.isDebug)
//					Log.e(GlobalVar.TAG, "Recv : " + readMessage);

				switch (theGlobalVar.getCurrentView()) {
				case GlobalVar.CURRENT_OBD_VIEW:
					theObdInfo = new OBD_Info();

					theObdInfo.setObdEngineRpm(theSplit[2]);
					theObdInfo.setObdEngineTemp(theSplit[3]);
					theObdInfo.setObdAirFlow(theSplit[4]);
					theObdInfo.setObdSpeed(theSplit[5]);
					theObdInfo.setObdThrottlePos(theSplit[6]);
					theObdInfo.setObdDate(theSplit[7]);

					theMsg = theGlobalVar.theObdHandler.obtainMessage(
							GlobalVar.OBD_INFO_FROM_OBD, theObdInfo);
					theGlobalVar.theObdHandler.sendMessage(theMsg);
					break;
				case GlobalVar.CURRENT_CAMERA_VIEW:
					theObdInfo = new OBD_Info();

					theObdInfo.setObdEngineRpm(theSplit[2]);
					theObdInfo.setObdEngineTemp(theSplit[3]);
					theObdInfo.setObdAirFlow(theSplit[4]);
					theObdInfo.setObdSpeed(theSplit[5]);
					theObdInfo.setObdThrottlePos(theSplit[6]);
					theObdInfo.setObdDate(theSplit[7]);

					theMsg = theGlobalVar.theCameraHandler.obtainMessage(
							GlobalVar.OBD_INFO_FROM_CAMERA, theObdInfo);
					theGlobalVar.theCameraHandler.sendMessage(theMsg);
					break;

				default:
					break;
				}

				/*
				 * switch (Integer.parseInt(theSplit[0])) { case
				 * BLUE_ACK_OBD_INFO: if (getState() == BLUE_REQ_OBD_INFO) {
				 * setState(BLUE_SEND_OBD_INFO); if (GlobalVar.isDebug)
				 * Log.e(GlobalVar.TAG, "Send Ack");
				 * 
				 * }
				 * 
				 * break; case BLUE_REQ_CON_FIN: // 종료 요청 보내기
				 * 
				 * break; case BLUE_ACK_OBD_FIN: // 연결 종료
				 * 
				 * break; case BLUE_SEND_OBD_INFO: if (getState() ==
				 * BLUE_SEND_OBD_INFO) { if (blueSeqNo <
				 * Long.parseLong(theSplit[1])) {
				 * 
				 * theGlobalVar.getObdInfo().addObdData(theSplit[7],
				 * theSplit[2], theSplit[3], theSplit[4], theSplit[6],
				 * theSplit[5]); } }
				 * 
				 * // 데이터 저장하는 부분 break;
				 * 
				 * default: break; }
				 */

				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:

				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(BluetoothService.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private final Handler mBlueCommandHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String theSendData;
			switch (msg.what) {
			case GlobalVar.BLUE_REQ_OBD_INFO:

				theSendData = "" + GlobalVar.BLUE_REQ_OBD_INFO + "/" + msg.arg1;
				theBluetoothService.write(theSendData.getBytes());

				break;
			case GlobalVar.BLUE_SEND_OBD_INFO:

				break;
			case GlobalVar.BLUE_FIN_SEND_DATA:
				theSendData = "" + GlobalVar.BLUE_FIN_SEND_DATA;
				theBluetoothService.write(theSendData.getBytes());

				break;
			case GlobalVar.BLUE_CONNECT:
				Log.e(GlobalVar.TAG ,"ASDF");
				connectBluetooth(false);
				break;
			case GlobalVar.BLUE_DISCONNECT:
				if (theBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
					theBluetoothService.stop();
				}
				break;
			}
		}
	};

	public void confirmBlueConnect(Context context) {
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
		alert_confirm.setMessage("블루투스 연결이 해제되었습니다.\n다시 연결하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// 'YES'
						connectBluetooth(false);

					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// 'No'

					}
				});
		AlertDialog alert = alert_confirm.create();
		alert.show();
	}
}
