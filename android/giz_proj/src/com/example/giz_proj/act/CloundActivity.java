package com.example.giz_proj.act;

import java.util.ArrayList;

import org.json.JSONObject;

import com.example.giz_proj.R;
import com.example.giz_proj.R.layout;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class CloundActivity extends Activity {
	protected static final int DISCONNECT = 0;
	protected static final int SPEAK = 1;
	SpeechSynthesizer mTts;
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case DISCONNECT:
				finish();
				break;
			case SPEAK:
				mTts.startSpeaking("小宝宝已经回到家了", null);
				break;
			}
		}
	};
	public static XPGWifiDevice device;
	XPGWifiDeviceListener dev_listener = new XPGWifiDeviceListener(){
		public boolean onReceiveData(String data) {
			try{
					JSONObject json = new JSONObject(data);
					JSONObject entity0 = json.getJSONObject("entity0");
					int person = entity0.getInt("person");
					if(person == 3){
						handler.sendEmptyMessage(SPEAK);
					}
			}catch(Exception e){
				e.printStackTrace();
			}
			return true;
		};
		public void onDisconnected() {
			handler.sendEmptyMessage(DISCONNECT);
		};
	};
	XPGWifiSDKListener sdk_listener = new XPGWifiSDKListener(){

	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clound);
		initTTS();
		XPGWifiSDK.sharedInstance().setListener(sdk_listener);
		device.setListener(dev_listener);
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
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		device.setListener(dev_listener);
	}
}
