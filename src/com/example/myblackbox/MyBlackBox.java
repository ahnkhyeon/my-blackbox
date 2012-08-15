package com.example.myblackbox;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MyBlackBox extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_black_box);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_my_black_box, menu);
        return true;
    }

    
}
