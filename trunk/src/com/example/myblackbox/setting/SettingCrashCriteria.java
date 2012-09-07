package com.example.myblackbox.setting;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.ShakeEventListener;

public class SettingCrashCriteria extends Activity {

	
	ArrayList<String> theCriteria;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.setting_crash_criteria);
	    
	    
	    theCriteria = new ArrayList<String>();

	    
	    for(int i = 0 ; i < ShakeEventListener.MIN_FORCEs.length ; i++) {
	    	theCriteria.add((i+1)+" 단계");
	    }
	
	 // 어댑터 생성
	 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	 				android.R.layout.simple_list_item_1, theCriteria);

	 		// 어댑터 연결
	 		ListView list = (ListView) findViewById(R.id.list_setting_crash_criteria);
	 		list.setAdapter(adapter);

	 		// 클릭 이벤트 설정
	 		list.setOnItemClickListener(new OnItemClickListener() {
	 			public void onItemClick(AdapterView<?> parent, View view,
	 					int position, long id) {
	 				
	 				Intent intent = new Intent();
	 				intent.putExtra(GlobalVar.CRASH_CRITERIA, position+"");
	 				setResult(Activity.RESULT_OK, intent);
	 				finish();
	 			}
	 		});
	}

}
