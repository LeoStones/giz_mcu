package com.example.giz_proj.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.example.giz_proj.R;
import com.example.giz_proj.R.layout;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HostActivity extends Activity implements OnClickListener {
	protected static final int LOGIN_FAIL = 0;
	protected static final int LOGIN_SUCCESS = 1;
	protected static final int DISCOVER = 2;
	protected static final int DEVICE_LOGIN = 3;
	protected static final int SPEAK = 4;
	List<XPGWifiDevice>list_dev;
	SpeechSynthesizer mTts;
	Setting set;
	String auid;
	String atoken;
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
		public void onConnected() {
			for(XPGWifiDevice dev:list_dev){
				String passcode = dev.GetPasscode();
				Log.w("passcode", passcode);
				if(!dev.GetPasscode().equals("")){
					dev.Login("", dev.GetPasscode());
				}
			}
		};
		public void onLogin(int result) {
			handler.sendEmptyMessage(DEVICE_LOGIN);
		};
	
		
		
	};
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DEVICE_LOGIN:
				for(XPGWifiDevice dev :list_dev){
					if(dev.IsConnected()&&dev.IsControl()){
						try{
							Log.i("command", "command");
							JSONObject json = new JSONObject();
							JSONObject cmd = new JSONObject();
							cmd.put("person", (set.getPerson()+1)+"");
							json.put("entity0", cmd);
							json.put("cmd", 1);
							dev.write(json.toString());
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				break;
			case DISCOVER:
				for(XPGWifiDevice dev :list_dev){
					String passcode = dev.GetPasscode();
					Log.i("passcode", passcode);
					if(dev.IsOnline()&&!dev.IsConnected()&&dev.IsLAN()&&!dev.GetPasscode().equals("")){
						Log.i("connect", "connnect");
						dev.setListener(dev_listener);
						dev.Connect();
					}else if(dev.IsOnline()&&dev.IsConnected()&&!dev.IsControl()&&dev.IsLAN()&&!dev.GetPasscode().equals("")){
						dev.setListener(dev_listener);
						dev.Login("", dev.GetPasscode());
					}
					
				}
				break;
			case LOGIN_FAIL:
				Toast.makeText(getApplicationContext(), "login_fail", Toast.LENGTH_SHORT).show();
				XPGWifiSDK.sharedInstance().RegisterAnonymousUser(set.getAndroidId());
				break;
			case SPEAK:
				String info1 = (String)msg.obj;
				mTts.startSpeaking(info1, null);
				break;
			case LOGIN_SUCCESS:
				break;
			default:
				break;
			}
		};
	};
	XPGWifiSDKListener sdk_listener = new XPGWifiSDKListener(){
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
		public void onUserLogin(int error, String errorMessage, String uid, String token) {
			if (error ==0){
				auid = uid;
				atoken = token;
				handler.sendEmptyMessage(LOGIN_SUCCESS);
			}else{
				handler.sendEmptyMessage(LOGIN_FAIL);
			}	
		};
	};
	Button btn_device;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);
		set = new Setting(this);
		switch(set.getPerson()+1){
		case 1:
			setTitle("男主人");
			break;
		case 2:
			setTitle("女主人");
			break;
		case 3:
			setTitle("宝宝");
			break;
		}
		
		
		initTTS();
		initView();
		initListener();

		XPGWifiSDK.sharedInstance().RegisterAnonymousUser(set.getAndroidId());
		
	}
	private void initView() {
		// TODO Auto-generated method stub
		btn_device = (Button)findViewById(R.id.btn_device);
	}
	private void initListener() {
		// TODO Auto-generated method stub
		btn_device.setOnClickListener(this);
	}
	Timer timer = new Timer();
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(auid!=null&&auid!=""&&atoken!=null&&atoken!=""){
					XPGWifiSDK.sharedInstance().GetBoundDevices(auid, atoken);
				}
			}
		}, 2000, 2000);
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		timer.cancel();
		
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
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(auid!=null&&!auid.equals("")){
			
			Intent it = new Intent();
			it.setClass(this, BindActivity.class);
			it.putExtra("uid", auid);
			it.putExtra("token", atoken);
			startActivity(it);
		}
	}

}
