package com.example.giz_proj.act;

import java.util.ArrayList;
import java.util.List;

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
import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final int LOGIN_FAIL = 0;
	protected static final int LOGIN_SUCCESS = 1;
	protected static final int DISCOVER = 2;
	List<XPGWifiDevice>list_dev;
	SpeechSynthesizer mTts;
	Setting set;
	String auid;
	String atoken;
	XPGWifiDeviceListener dev_listener = new XPGWifiDeviceListener(){
		public boolean onReceiveData(String data) {
			Log.i("data", data);
			return true;
		};
		public void onConnected() {
			for(XPGWifiDevice dev:list_dev){
				dev.Login(auid, atoken);
			}
		};
		
		public void onLoginMQTT(int result) {
			for(XPGWifiDevice dev :list_dev){
				if(dev.IsConnected()){
					try{
						JSONObject json = new JSONObject();
						JSONObject cmd = new JSONObject();
						cmd.put("person", (set.getPerson()+1)+"");
						json.put("cmd", 1);
						json.put("entity0", cmd);
						dev.write(json.toString());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		public void onLogin(int result) {
			
		};
	
		
		
	};
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DISCOVER:
				for(XPGWifiDevice dev :list_dev){
					if(dev.IsOnline()&&!dev.IsConnected()){
						dev.setListener(dev_listener);
						dev.ConnectToMQTT();
					}
					
				}
				break;
			case LOGIN_FAIL:
				Toast.makeText(getApplicationContext(), "login_fail", Toast.LENGTH_SHORT).show();
				XPGWifiSDK.sharedInstance().RegisterAnonymousUser(set.getAndroidId());
				break;
			case LOGIN_SUCCESS:
				XPGWifiSDK.sharedInstance().BindDevice(auid, atoken, "ibMBoWnrbdHB9D6NR3nZWM", "YHPXRSTSWG");
				XPGWifiSDK.sharedInstance().GetBoundDevices(auid, atoken);
				break;
			default:
				break;
			}
		};
	};
	XPGWifiSDKListener sdk_listener = new XPGWifiSDKListener(){
		public void onBindDevice(int error, String errorMessage) {
			Log.i("msg", errorMessage);
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
		@Override
		public void onUserLogin(int error, String errorMessage, String uid,
				String token) {
			// TODO Auto-generated method stub'
			if(error == 0){
				auid = uid;
				atoken = token;
				handler.sendEmptyMessage(LOGIN_SUCCESS);
			}else{
				handler.sendEmptyMessage(LOGIN_FAIL);
			}
			
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		set = new Setting(this);
		initTTS();
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		XPGWifiSDK.sharedInstance().RegisterAnonymousUser(set.getAndroidId());
		
		
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
//		mTts.startSpeaking("科大讯飞,让世界聆听我们的声音", mSynListener);
		
	}

}
