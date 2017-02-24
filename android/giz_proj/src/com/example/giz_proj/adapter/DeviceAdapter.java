package com.example.giz_proj.adapter;

import java.util.List;

import com.example.giz_proj.R;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceAdapter extends BaseAdapter{
	Context c;
	List<XPGWifiDevice>list_device;
	public DeviceAdapter(Context c,List<XPGWifiDevice>deivces){
		this.c = c;
		this.list_device = deivces;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list_device.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(this.c).inflate(R.layout.item_view, null);
		TextView tv_mac = (TextView)view.findViewById(R.id.tv_mac);
		TextView tv_did = (TextView)view.findViewById(R.id.tv_did);
		XPGWifiDevice device = list_device.get(pos);
		tv_mac.setText(device.GetMacAddress());
		tv_did.setText(device.GetDid());
		return view;
	}

}
