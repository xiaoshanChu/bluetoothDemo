package com.example.bluetoothdemo;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.adapter.DeviceListAdapter;
import com.example.base.BaseActivity;

public class MainActivity extends BaseActivity {
	
	private ListView lv;
	/**
	 * 蓝牙适配器对象
	 */
	private BluetoothAdapter bluetoothAdapter;
	private final String TAG = "MainActivity";
	private DeviceListAdapter deviceAdapter;
	private List<BluetoothDevice> deviceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
		
		searchRound();
	}
	
	private void init(){
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		deviceList = new ArrayList<BluetoothDevice>();
		lv = (ListView) findViewById(R.id.lv_device_list);
		deviceAdapter = new DeviceListAdapter(this);
		lv.setAdapter(deviceAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});
	}
	
	/**
	 * 搜索周边蓝牙设备
	 */
	private void searchRound(){
		if(bluetoothAdapter == null){
			toastShort("该设备不支持蓝牙");
			return;
		}
		if(!bluetoothAdapter.isEnabled()){
			toastShort("请打开蓝牙");
			return;
		}
		deviceList.clear();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, intentFilter);
		bluetoothAdapter.startDiscovery();
		timeCount = new TimeCount(15*1000, 15*1000);
		timeCount.start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bluetoothAdapter.cancelDiscovery();
		unregisterReceiver(receiver);
	}
	
	private TimeCount timeCount;
	
	/**
	 * 广播接收状态和搜索结果
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				deviceList.add(device);
				deviceAdapter.setData(deviceList);
				deviceAdapter.notifyDataSetChanged();
				Log.d(TAG, "--deviceName-->"+device);
			}
		}
	};
	
	/**
	 * 计时15秒后取消搜索
	 */
	class TimeCount extends CountDownTimer {
		
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onTick(long millisUntilFinished) {
		}
		@Override
		public void onFinish() {// 计时完毕触发
			bluetoothAdapter.cancelDiscovery();
			timeCount.cancel();
			Log.d(TAG, "exe");
		}
	}
}
