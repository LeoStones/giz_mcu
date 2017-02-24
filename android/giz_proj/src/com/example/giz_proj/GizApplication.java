package com.example.giz_proj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.xtremeprog.xpgconnect.XPGWifiConfig;
import com.xtremeprog.xpgconnect.XPGWifiLogLevel;
import com.xtremeprog.xpgconnect.XPGWifiSDK;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class GizApplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		String path = this.getFilesDir().toString();
		try {
			copyFileTo(this.getApplicationContext(), "07b24913b07f4b38b8bafeac1dd1d7e2.json", path+"/giz/07b24913b07f4b38b8bafeac1dd1d7e2.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XPGWifiConfig.sharedInstance().SetAppID("82cca2016fe4420db5156a6038adcb3c");
		XPGWifiConfig.sharedInstance().EnableProductFilter(true);
		XPGWifiConfig.sharedInstance().SetDebug(true);
		XPGWifiSDK.SetLogLevel(XPGWifiLogLevel.XPGWifiLogLevelAll);
		XPGWifiConfig.sharedInstance().RegisterProductKey("07b24913b07f4b38b8bafeac1dd1d7e2");
		XPGWifiConfig.sharedInstance().SetProductPath(path+"/giz");
		
		

		}
	/**
	 * 从assert中复制出文件到某个文件
	 * @param c
	 * @param orifile
	 * @param desfile
	 * @return
	 * @throws IOException 
	 */
	static public boolean copyFileTo(Context c,String orifile,String desfile) throws IOException{
		File f = new File(desfile);
		File dir = new File(c.getFilesDir()+"/giz");
		if(!dir.exists()){
			dir.mkdir();
		}
		if(f.exists()){
			Log.i("file", "exists");
			return true;
		}
		InputStream myInput;  
        OutputStream myOutput = new FileOutputStream(desfile);  
        myInput = c.getAssets().open(orifile);  
        byte[] buffer = new byte[1024];  
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length); 
            length = myInput.read(buffer);
        }
        
        myOutput.flush();  
        myInput.close();  
        myOutput.close(); 
		
		return true;
	}
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
