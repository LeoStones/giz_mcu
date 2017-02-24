package com.example.giz_proj.setting;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

public class Setting {
	SharedPreferences sp ;
	Context c;
	public Setting(Context c){
		this.sp = c.getSharedPreferences("data", Context.MODE_PRIVATE);
	}
	public boolean isFirst(){
		boolean isFirst = sp.getBoolean("isfirst", true);
		sp.edit().putBoolean("isfirst", false).commit();
		return isFirst;
	}
	public void setPerson(int person){
		sp.edit().putInt("person", person).commit();
	}
	public int  getPerson(){
		return sp.getInt("person", -1);
	}
	public String getAndroidId(){
		String id = sp.getString("aid", null);
		if (id == null){
			id = UUID.randomUUID().toString();
			sp.edit().putString("aid", id).commit();
		}
		return id;
	}

}
