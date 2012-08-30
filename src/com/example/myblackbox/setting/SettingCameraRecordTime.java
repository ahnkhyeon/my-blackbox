package com.example.myblackbox.setting;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SettingCameraRecordTime extends Activity {

	public final static int RECORD_TIME[] = {1,5,10,20};
	
	ArrayList<String> theRecordTime;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_camera_record_time);
		// TODO Auto-generated method stub

		theRecordTime = new ArrayList<String>();
		
		for (int i = 0 ; i < RECORD_TIME.length ; i++) {
			theRecordTime.add(RECORD_TIME[i]+"분");
		}
		
		// 어댑터 생성
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, theRecordTime);

		// 어댑터 연결
		ListView list = (ListView) findViewById(R.id.list_setting_camera_record_time);
		list.setAdapter(adapter);

		// 클릭 이벤트 설정
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent();
				intent.putExtra(GlobalVar.CAMERA_RECORD_TIME, RECORD_TIME[position]+"");
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}
}