package com.example.adapter;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bluetoothdemo.R;

public class DeviceListAdapter extends BaseAdapter {
	
	private List<BluetoothDevice> list;
	private LayoutInflater inflater;
	
	public DeviceListAdapter(Context context){
		inflater = LayoutInflater.from(context);
	}
	
	public void setData(List<BluetoothDevice> list){
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_device, null);
			viewHolder.tvDeviceAddress = (TextView) convertView.findViewById(R.id.tv_item_device_address);
			viewHolder.tvDeviceName = (TextView) convertView.findViewById(R.id.tv_item_device_name);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		BluetoothDevice device = list.get(position);
		viewHolder.tvDeviceAddress.setText(device.getAddress());
		viewHolder.tvDeviceName.setText(device.getName());
		return convertView;
	}
	
	static class ViewHolder{
		TextView tvDeviceName,tvDeviceAddress;
	}
}
