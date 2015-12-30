package com.example.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;

/**
 * @author Xiaoshan
 * 		蓝牙的连接、断开、和连接状态
 *
 */
public class BluetoothService {
	
	public static final int STATE_CONNECTED = 10000;//蓝牙连接状态已连接
	public static final int STATE_CONNECTING = 10001; //连接中
	public static final int STATE_DISCONNECT = 10002;//状态断开连接
	public static final int STATE_CONNECT_FAILURE = 10003;//连接失败
	
	private int state;
	private static String TAG = "BluetoothService";
	private static BluetoothService instance;
	private InputStream inputStream;//蓝牙输入输出流对象
	private OutputStream outputStream;
	private ConnectThread connectThread;
	private Handler handler;
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public InputStream getInputStream(){
		return inputStream;
	}
	
	public OutputStream getOutputStream(){
		return outputStream;
	}
	
	private synchronized void setState(int state){
		this.state = state;
	}
	
	public int getState(){
		return state;
	}
	
	public static synchronized BluetoothService getInstance(){
		if(instance == null){
			instance = new BluetoothService();
		}
		return instance;
	}
	
	public void connet(BluetoothDevice device){
		close();
		connectThread = new ConnectThread(device);
		connectThread.start();
	}
	
	public void close(){
		if(connectThread != null){
			if(!connectThread.isInterrupted()){
				connectThread.close();
				connectThread.interrupt();
			}
			connectThread = null;
		}
	}
	
	/**
	 * 蓝牙连接线程
	 */
	private class ConnectThread extends Thread {
		
		private BluetoothDevice device;
		private BluetoothSocket socket;
		
		public ConnectThread(BluetoothDevice device) {
			this.device = device;
		}
		
		@Override
		public void run() {
			setStateAndMsg(STATE_CONNECTING);
			try {
				if(Build.VERSION.SDK_INT >= 10){
					socket = device.createInsecureRfcommSocketToServiceRecord(
							UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				}else{
					socket = device.createRfcommSocketToServiceRecord(
							UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				}
				socket.connect();
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				
				if(inputStream != null && outputStream != null){
					setStateAndMsg(STATE_CONNECTED);
				}else{
					setStateAndMsg(STATE_CONNECT_FAILURE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				setStateAndMsg(STATE_CONNECT_FAILURE);
			}
		}
		
		private void setStateAndMsg(int state){
			setState(state);
			handler.obtainMessage(state).sendToTarget();
		}
		
		public synchronized void close(){
			if(socket != null){
				try {
					socket.close();
					socket = null;
					setStateAndMsg(STATE_DISCONNECT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
