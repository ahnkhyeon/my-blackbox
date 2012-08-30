package com.example.myblackbox.setting;

import java.util.ArrayList;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingCameraStorage extends Activity {

	
	public final static int STORAGE_SIZE[] = {1,2,3,5,8};
	
	ArrayList<String> theStorageSize;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.setting_camera_storage);
	    // TODO Auto-generated method stub
	    
	    theStorageSize = new ArrayList<String>();
	    
	    for(int i = 0 ; i < STORAGE_SIZE.length ; i++) { 
	    	theStorageSize.add(STORAGE_SIZE[i]+" GB");
	    }
	    
		// 어댑터 생성
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, theStorageSize);

		// 어댑터 연결
		ListView list = (ListView) findViewById(R.id.list_setting_camera_storage);
		list.setAdapter(adapter);

		// 클릭 이벤트 설정
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent();
				intent.putExtra(GlobalVar.CAMERA_STORAGE, STORAGE_SIZE[position]+"");
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

}
