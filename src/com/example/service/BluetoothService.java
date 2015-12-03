package com.example.service;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

/**
 * @author Xiaoshan
 * 		蓝牙连接
 *
 */
public class BluetoothService {
	
	private ConnectThread connectThread;
	
	private static BluetoothService instance;
	
	public static synchronized BluetoothService getInstance(){
		if(instance == null){
			instance = new BluetoothService();
		}
		Log.d("BluetoothService", "------BluetoothService");
		return instance;
	}
	
	public void connet(BluetoothDevice device){
		if(connectThread != null){
			if(connectThread.isInterrupted()){
				connectThread.interrupt();
			}
		}
		connectThread = null;
		connectThread = new ConnectThread(device);
		Log.d("BluetoothService", "------connet");
		connectThread.start();
		
	}
	
	private class ConnectThread extends Thread {
		
		private BluetoothDevice device;
		private BluetoothSocket socket;
		
		public ConnectThread(BluetoothDevice device) {
			this.device = device;
		}
		
		@Override
		public void run() {
			Log.d("BluetoothService", "------run");
			try {
				if(Build.VERSION.SDK_INT >= 10){
					socket = device.createInsecureRfcommSocketToServiceRecord(
							UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				}else{
					socket = device.createRfcommSocketToServiceRecord(
							UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				}
				socket.connect();
				Log.d("BluetoothService", "------connected");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
