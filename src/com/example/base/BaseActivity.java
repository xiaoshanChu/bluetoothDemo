package com.example.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class BaseActivity extends Activity {
	
	private Toast toast = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * Toast时间长
	 */
	public void toastLong(String msg) {
		if(!TextUtils.isEmpty(msg)){
			if (toast != null) {
				toast.cancel();
			}
			toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	/**
	 * Toast时间短
	 */
	public void toastShort(String msg) {
		if(!TextUtils.isEmpty(msg)){
			if (toast != null) {
				toast.cancel();
			}
			toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

}
