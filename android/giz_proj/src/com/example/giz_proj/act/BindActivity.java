package com.example.giz_proj.act;

import java.util.ArrayList;
import java.util.List;

import com.example.giz_proj.R;
import com.example.giz_proj.R.layout;
import com.example.giz_proj.adapter.DeviceAdapter;
import com.example.giz_proj.setting.Setting;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.a.c.e;
import com.xtremeprog.xpgconnect.XPGWifiDevice;
import com.xtremeprog.xpgconnect.XPGWifiDeviceList;
import com.xtremeprog.xpgconnect.XPGWifiDeviceListener;
import com.xtremeprog.xpgconnect.XPGWifiSDK;
import com.xtremeprog.xpgconnect.XPGWifiSDKListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BindActivity extends Activity implements OnClickListener, OnItemClickListener {
	protected static final int LOGIN_FAIL = 0;
	protected static final int LOGIN_SUCCESS = 1;
	protected static final int DISCOVER = 2;
	protected static final int DEVICE_LOGIN = 3;
	protected static final int GET_PASSCODE_FAIL = 4;
	protected static final int BIND_SUCCESS = 5;
	protected static final int BIND_FAIL = 6;
	protected static final int GET_PASSCODE_SUCCESS = 7;
	protected static final int LOGIN_MQTT = 8;
	List<XPGWifiDevice>list_dev;
	Setting set;
	String auid;
	String atoken;
	Button btn_discover;
	ListView lv_device;
	DeviceAdapter adapter;
	XPGWifiDevice select_device;
	XPGWifiSDKListener sdk_listener = new XPGWifiSDKListener(){
		public void onBindDevice(int error, String errorMessage) {
			if(error ==0){
				handler.sendEmptyMessage(BIND_SUCCESS);
			}else{
				Message msg = new Message();
				msg.what = BIND_FAIL;
				msg.obj = errorMessage;
			}
		};
		
		@Override
		public void onDiscovered(int result, XPGWifiDeviceList devices) {
			// TODO Auto-generated method stub
			Log.i("count", devices.GetCount()+"");
			list_dev  =new ArrayList<XPGWifiDevice>();
			for(int i = 0;i<devices.GetCount();i++){
				XPGWifiDevice dev = devices.GetItem(i);
				list_dev.add(dev);
			}
			if(list_dev.size()!=0){
				handler.sendEmptyMessage(DISCOVER);
			}
		}
	};
	XPGWifiDeviceListener dev_listener = new XPGWifiDeviceListener(){
		public void onDisconnected() {};
		public void onGetPasscode(int result) {
			if(result==0){
				handler.sendEmptyMessage(GET_PASSCODE_SUCCESS);
			}else{
				handler.sendEmptyMessage(GET_PASSCODE_FAIL);
				
			}
		};
		public void onLoginMQTT(int result) {
			handler.sendEmptyMessage(LOGIN_MQTT);
		};
		public void onLogin(int result){
			
		};
		public void onConnected() {
			if(select_device.IsLAN()){
				select_device.GetPasscodeFromDevice();
			}else{
				select_device.Login(auid,atoken);
			}
		};
		public void onBindDevice(int error, String errorMessage) {
			if(error ==0){
				handler.sendEmptyMessage(BIND_SUCCESS);
			}else{
				Message msg = new Message();
				msg.what = BIND_FAIL;
				msg.obj = errorMessage;
			}
		};
		
	};
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOGIN_MQTT:
				Intent it = new Intent();
				it.setClass(BindActivity.this, CloundActivity.class);
				CloundActivity.device = select_device;
				startActivity(it);
				break;
			case DISCOVER:
				adapter = new DeviceAdapter(BindActivity.this, list_dev);
				lv_device.setAdapter(adapter);
				break;
			case GET_PASSCODE_SUCCESS:
				Toast.makeText(BindActivity.this, "get passcode success", Toast.LENGTH_SHORT).show();
				String did = select_device.GetDid();
				String passcode = select_device.GetPasscode();
				String uid = auid;
				String token = atoken;
				if(!passcode.equals("")&&!did.equals("")){
					XPGWifiSDK.sharedInstance().BindDevice(uid, token, select_device.GetDid(), select_device.GetPasscode());
				}
				select_device.Disconnect();
				break;
			case GET_PASSCODE_FAIL:
				Toast.makeText(BindActivity.this, "get passcode fail", Toast.LENGTH_SHORT).show();
				break;
			case BIND_FAIL:
				String info = (String)msg.obj;
				Toast.makeText(BindActivity.this, info, Toast.LENGTH_SHORT).show();
				break;
			case BIND_SUCCESS:
				Toast.makeText(BindActivity.this, "bind success", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind);
		initView();
		initListener();
		Intent it = getIntent();
		auid = it.getStringExtra("uid");
		atoken = it.getStringExtra("token");
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		XPGWifiSDK.sharedInstance().GetBoundDevices(auid, atoken);
	}

	private void initListener() {
		// TODO Auto-generated method stub
		btn_discover.setOnClickListener(this);
		lv_device.setOnItemClickListener(this);
	}

	private void initView() {
		// TODO Auto-generated method stub
		btn_discover = (Button)findViewById(R.id.btn_discover);
		lv_device = (ListView)findViewById(R.id.lv_device);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		XPGWifiSDK.sharedInstance().DiscoverDevices();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		// TODO Auto-generated method stub
		select_device = list_dev.get(pos);
		select_device.setListener(dev_listener);
		if(select_device.IsLAN()){
			String passcode = select_device.GetPasscode();
			if(!passcode.equals("")&&!select_device.GetDid().equals("")){
				XPGWifiSDK.sharedInstance().BindDevice(auid, atoken, select_device.GetDid(), select_device.GetPasscode());
			}else{
				if(!select_device.IsConnected()){
					select_device.Connect();
				}else{
					select_device.GetPasscodeFromDevice();
				}
				
				
			}
		}else{
			if(select_device.IsOnline()){
				select_device.ConnectToMQTT();
			}
		}
	}
}
