package com.example.myblackbox.setting;

import com.example.myblackbox.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingAppInfo extends Activity {

	
	TextView theContext;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.setting_app_info);
	
	    // TODO Auto-generated method stub
	    
	    
	    theContext = (TextView) findViewById(R.id.setting_info_context);
	    theContext.setText("BlackBox & Web & OBD\n");
	}
}
