package com.example.myblackbox.setting;

import java.util.ArrayList;
import java.util.List;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingCameraResolution extends Activity {

	
	ArrayList<String> theVideoSize;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_camera_resolution);
		// TODO Auto-generated method stub

		Camera mCamera = Camera.open();
		Camera.Parameters params = mCamera.getParameters();
		List<Size> tempParms = params.getSupportedPreviewSizes();

		theVideoSize = new ArrayList<String>();
		for (int i = 0; i < tempParms.size(); i++) {
			theVideoSize.add(tempParms.get(i).width + " * "
					+ tempParms.get(i).height);
		}
		mCamera.release();
		mCamera = null;
		
		
		ArrayAdapter<String> theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, theVideoSize);

		ListView theListView = (ListView) findViewById(R.id.list_setting_camera_resolution);
		theListView.setAdapter(theAdapter);
		
		
		theListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent();
				intent.putExtra(GlobalVar.CAMERA_RESOLUTION, theVideoSize.get(position));
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		
	}
}