package com.example.giz_proj.act;

import com.example.giz_proj.R;
import com.example.giz_proj.setting.Setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

public class SettingActivity extends Activity implements OnClickListener {
	private Button btn_ok;
	private Spinner spn_person;
	Setting set;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
		set = new Setting(this);
		
		initView();
		initListener();
	}
	private void jumpToActByType() {
		int per = set.getPerson();
		Intent it = new Intent();
		switch(per){
		case 0:
		case 1:
		case 2:
			it.setClass(this, HostActivity.class);
			startActivity(it);
			finish();
			break;
		case 3:
			it.setClass(this, DeviceActivity.class);
			startActivity(it);
			finish();
			break;
		}
	}
	private void initView() {
		// TODO Auto-generated method stub
		btn_ok = (Button)findViewById(R.id.btn_ok);
		spn_person = (Spinner)findViewById(R.id.spn_person);
	}
	private void initListener() {
		// TODO Auto-generated method stub
		btn_ok.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		set.setPerson(spn_person.getSelectedItemPosition());
		jumpToActByType();
		
	}

}
