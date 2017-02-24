package com.example.giz_proj.act;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.giz_proj.R;
import com.example.giz_proj.setting.Setting;

public class OnceActivity extends Activity {
	Setting set;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_once);
		set = new Setting(this);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				jumpToActByType();
			}
		}, 2000);
	}
	private void jumpToActByType() {
		int per = set.getPerson();
		Intent it = new Intent();
		switch(per){
		case 0:
		case 1:
		case 2:
			it.setClass(OnceActivity.this, HostActivity.class);
			startActivity(it);
			finish();
			break;
		case 3:
			it.setClass(OnceActivity.this, DeviceActivity.class);
			startActivity(it);
			finish();
			break;
		case -1:
			it.setClass(OnceActivity.this, SettingActivity.class);
			startActivity(it);
			finish();
		}
	}
}
