package com.example.bluetoothdemo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.ehking.kpos.Interfac.CalculationMACListener;
import com.ehking.kpos.Interfac.DeviceInfoListener;
import com.ehking.kpos.Interfac.StateListener;
import com.ehking.kpos.KposOpen;
import com.ehking.kpos.util.LogUtil;
import com.example.adapter.DeviceListAdapter;
import com.example.base.BaseActivity;
import com.example.service.BluetoothService;
import com.nexgo.oaf.datahub.device.mpos.DeviceInfo;
import com.nexgo.oaf.datahub.device.mpos.WorkingKeys;
import com.nexgo.oaf.datahub.util.ByteUtils;

public class MainActivity extends BaseActivity implements OnClickListener
	,DeviceInfoListener,StateListener,CalculationMACListener{
	
	private final String TAG = "MainActivity";
	private ListView lv;
	private Button btnDisconnect;
	private BluetoothService btService;
	private int times = 0;
	private Timer timer;
	private TimerTask task;
	private BluetoothAdapter bluetoothAdapter;
	private DeviceListAdapter deviceAdapter;
	private List<BluetoothDevice> deviceList;
	private KposOpen kposOpen;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == BluetoothService.STATE_CONNECTED){
				kposOpen.init(btService.getInputStream(), btService.getOutputStream());
				kposOpen.requestDeviceInfo();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
		
		searchRound();
	}
	
	private WorkingKeys.WorkingKey[] updateWorking(String pikText, String pikCheckValue,
			String makText, String makCheckValue,String tdkText,String tdkCheckValue) {
		byte[] pik = ByteUtils.hexString2ByteArray(pikText);
		byte[] pikCheck = ByteUtils.hexString2ByteArray(pikCheckValue);
		byte[] mak = ByteUtils.hexString2ByteArray(makText);
		byte[] makCheck = ByteUtils.hexString2ByteArray(makCheckValue);
//		byte[] tdk = ByteUtils.hexString2ByteArray(tdkText);
//		byte[] tdkCheck = ByteUtils.hexString2ByteArray(tdkCheckValue);
		
		WorkingKeys.WorkingKey[] workingKeys = new WorkingKeys.WorkingKey[] {
				new WorkingKeys.WorkingKey(WorkingKeys.FLAG_PIK,WorkingKeys.TYPE_3DES, pik, pikCheck),
				new WorkingKeys.WorkingKey(WorkingKeys.FLAG_MAK,WorkingKeys.TYPE_3DES, mak, makCheck)
//				,new WorkingKeys.WorkingKey(WorkingKeys.FLAG_TDK,WorkingKeys.TYPE_3DES, tdk, tdkCheck)
		};
		return workingKeys;
	}
	
	private void init(){
		kposOpen = new KposOpen(this,this,this);
		deviceList = new ArrayList<BluetoothDevice>();
		lv = (ListView) findViewById(R.id.lv_device_list);
		btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
		btnDisconnect.setOnClickListener(this);
		deviceAdapter = new DeviceListAdapter(this);
		lv.setAdapter(deviceAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BluetoothDevice device = (BluetoothDevice) parent.getAdapter().getItem(position);
				boundDevice(device);;
			}
		});
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
	    intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
	    intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(receiver, intentFilter);
	}
	
	private void boundDevice(BluetoothDevice device){
		int state = device.getBondState();
		if(state == BluetoothDevice.BOND_BONDED){//已绑定
			btService = BluetoothService.getInstance();
			btService.setHandler(handler);
			btService.connet(device);
		}else{//未绑定，先绑定
			try {
				Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
				createBondMethod.invoke(device);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 搜索周边蓝牙设备
	 */
	private void searchRound(){
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null){
			toastShort("该设备不支持蓝牙");
			return;
		}
		if(!bluetoothAdapter.isEnabled()){
			toastShort("请打开蓝牙");
			return;
		}
		bluetoothAdapter.startDiscovery();
		timer = new Timer();
		task = new TimerTask() {
			
			@Override
			public void run() {
				times++;
				if(times == 20){
					bluetoothAdapter.cancelDiscovery();
					times = 0;
					this.cancel();
					timer.purge();
					timer.cancel();
				}
			}
		};
		timer.schedule(task, 1000,500);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		if(task != null){
			task.cancel();
			task = null;
		}
	}
	
	
	/**
	 * 广播接收状态和搜索结果
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null && deviceList.indexOf(device) == -1){
					deviceList.add(device);
					deviceAdapter.setData(deviceList);
					deviceAdapter.notifyDataSetChanged();
				}
			}else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
				toastShort("蓝牙状态改变");
			}else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
				toastShort("设备已连接");
				
			}else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				toastShort("设备连接断开");
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_disconnect:
			btService.close();
			break;
		default:
			break;
		}
	}

	@Override
	public void MACCallBack(int arg0, Map<String, String> arg1) {
		
	}

	@Override
	public void stateCallBack(int arg0, int arg1) {
		
	}

	@Override
	public void deviceInfoCallBack(DeviceInfo info) {
		LogUtil.d(TAG, "----->"+info.getSn());
		kposOpen.updateWokingKey(updateWorking("3CF74C1BE2A6D37B3E0D132A64F66AD6", "32307B17CE9637AE",
				"9BD369511D36A7017B219574B62C06CF", "5565875F7CE2474E",
				"46478227041A52A415CF552A197F6409", "CFF9E53D4B64F584"));
	}

}
