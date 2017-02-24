package com.example.giz_proj.act;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.example.giz_proj.R;
import com.example.giz_proj.R.layout;
import com.example.giz_proj.adapter.DeviceAdapter;
import com.example.giz_proj.setting.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
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
import android.text.format.Time;
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

public class DeviceActivity extends Activity implements OnClickListener, OnItemClickListener {
	protected static final int LOGIN_FAIL = 0;
	protected static final int LOGIN_SUCCESS = 1;
	protected static final int DISCOVER = 2;
	protected static final int DEVICE_LOGIN = 3;
	protected static final int GET_PASSCODE_FAIL = 4;
	protected static final int BIND_SUCCESS = 5;
	protected static final int BIND_FAIL = 6;
	protected static final int GET_PASSCODE_SUCCESS = 7;
	protected static final int SPEAK = 8;
	protected static final int CONNECT = 9;
	LinkedList<String>dids = new LinkedList<String>();
	SpeechSynthesizer mTts;
	List<XPGWifiDevice>list_dev;
	Setting set;
	String auid;
	String atoken;
	Button btn_discover;
	ListView lv_device;
	DeviceAdapter adapter;
	XPGWifiDevice select_device;
	List<String>list_mac = new LinkedList<String>();
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
	Timer cmd = new Timer();
	XPGWifiDeviceListener dev_listener = new XPGWifiDeviceListener(){
		
		public boolean onReceiveData(String data) {
			Log.i("data", data);
			try{
			JSONObject json = new JSONObject(data);
			JSONObject entity0 = json.getJSONObject("entity0");
			Message msg = new Message();
			int person = entity0.getInt("person");
			String temp = "";
			String hump = "";
			String solid_hump ="";
			String p = "";
			switch(person){
			case 1:
				p = "爸爸";
				break;
			case 2:
				p = "妈妈";
				break;
			case 3:
				p = "小宝贝";
				break;
			default:
				return true;
			}
			int n_temp = entity0.getInt("home_temperature");
			int n_hump = entity0.getInt("home_humidity");
			int n_solid_hump = entity0.getInt("soil_humidity");
			if(n_temp<18){
				temp = "有点冷";
			}else if(n_temp>=18&&n_temp<=33){
				temp = "";
			}else{
				temp = "有点热";
			}
			if(n_hump<45){
				hump = "太干燥啦";
			}else if(n_hump>=45&&n_hump<=65){
				hump = "湿度适宜";
			}else{
				hump = "环境偏湿，可以开一下抽湿机";
			}
			if(n_solid_hump>70){
				solid_hump = "我现在口渴了，可以给我杯水喝吗？";
			}else if(n_solid_hump<=70&&n_solid_hump>=30){
				solid_hump = "我现在精神饱满";
			}else{
				solid_hump = "我喝太多了。醉了";
			}
			msg.what = SPEAK;
			msg.obj = "欢迎"+p+"回到家！现在室内湿度是"+entity0.getString("home_humidity")+"%,"+hump+"，室内温度是"+entity0.getString("home_temperature")+"℃,"+temp+","+solid_hump;
			handler.sendMessage(msg);
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return true;
		};
		
		public void onLogin(int result) {
			Log.w("login", result+"");
			handler.sendEmptyMessage(LOGIN_SUCCESS);
			
		};
		public void onDisconnected() {
			list_mac.clear();
		};
		public void onGetPasscode(int result) {
			if(result==0){
				handler.sendEmptyMessage(GET_PASSCODE_SUCCESS);
			}else{
				handler.sendEmptyMessage(GET_PASSCODE_FAIL);
				
			}
		};
		public void onConnected() {
			
			handler.sendEmptyMessage(CONNECT);
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
			case CONNECT:
				Toast.makeText(DeviceActivity.this, "mac"+select_device.GetMacAddress(), Toast.LENGTH_SHORT).show();
				select_device.GetPasscodeFromDevice();
				list_mac.add(select_device.GetMacAddress());
				break;
			case DISCOVER:
				adapter = new DeviceAdapter(DeviceActivity.this, list_dev);
				lv_device.setAdapter(adapter);
				if(list_dev.size()>0){
					select_device = list_dev.get(0);
					select_device.setListener(dev_listener);
					if(!list_mac.contains(select_device.GetMacAddress())){
						Toast.makeText(DeviceActivity.this, "connect ", Toast.LENGTH_SHORT).show();
						for(String test :list_mac){
							Toast.makeText(DeviceActivity.this, "mac ="+test, Toast.LENGTH_SHORT).show();
						}
						select_device.Connect();
					}
				}
				break;
			case GET_PASSCODE_SUCCESS:
				Toast.makeText(DeviceActivity.this, "get passcode success", Toast.LENGTH_SHORT).show();
				String did = select_device.GetDid();
				String passcode = select_device.GetPasscode();
				String uid = auid;
				String token = atoken;
				select_device.Login("", select_device.GetPasscode());
				break;
			case GET_PASSCODE_FAIL:
				Toast.makeText(DeviceActivity.this, "get passcode fail", Toast.LENGTH_SHORT).show();
				break;
			case BIND_FAIL:
				String info = (String)msg.obj;
				Toast.makeText(DeviceActivity.this, info, Toast.LENGTH_SHORT).show();
				break;
			case BIND_SUCCESS:
				Toast.makeText(DeviceActivity.this, "bind success", Toast.LENGTH_SHORT).show();
				break;
			case SPEAK:
				String info1 = (String)msg.obj;
				mTts.startSpeaking(info1, null);
				break;
			case LOGIN_SUCCESS:
				Toast.makeText(DeviceActivity.this, "login success", Toast.LENGTH_SHORT).show();
				cmd.cancel();
				cmd = new Timer();
				cmd.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try{
							JSONObject json = new JSONObject();
							JSONObject cmd = new JSONObject();
							cmd.put("person", 4);
							json.put("entity0", cmd);
							json.put("cmd", 1);
							select_device.write(json.toString());
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}, 15000,15000);
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
		initTTS();
		initView();
		initListener();
		Intent it = getIntent();
		set = new Setting(this);
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		XPGWifiSDK.sharedInstance().DiscoverDevices();
//		t = new Timer();
//		t.schedule(new TimerTask() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				XPGWifiSDK.sharedInstance().DiscoverDevices();
//			}
//		}, 5000, 5000);
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
//	Timer t  = new Timer();
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		t.cancel();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		// TODO Auto-generated method stub
		select_device = list_dev.get(pos);
		select_device.setListener(dev_listener);
		if(select_device.IsLAN()){
			String passcode = select_device.GetPasscode();
			if(!select_device.IsConnected()){
				dids.add(select_device.GetDid());
				select_device.Connect();
			}else{
				select_device.GetPasscodeFromDevice();
				
			}
				
				
		}
	}
	private void initTTS() {
		// TODO Auto-generated method stub
		SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5481d498");
		SynthesizerListener mSynListener = new SynthesizerListener(){

			@Override
			public void onBufferProgress(int arg0, int arg1, int arg2,
					String arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCompleted(SpeechError arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSpeakBegin() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSpeakPaused() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSpeakProgress(int arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onSpeakResumed() {
				// TODO Auto-generated method stub
				
			}
		};
		mTts= SpeechSynthesizer.createSynthesizer(this, null);
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
		mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
		mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量,范围 0~100
		
	}
}
