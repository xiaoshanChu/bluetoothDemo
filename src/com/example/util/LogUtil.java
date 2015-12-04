package com.example.util;

import android.util.Log;

/**
 * @author Xiaoshan
 * @description ����һ��boolean������ʾ���ǲ���ʾLog
 *
 */
public class LogUtil {

	private static boolean isShow = true;

	public static void setShow(boolean isShow) {
		LogUtil.isShow = isShow;
	}

	public static void d(String tag, String msg) {
		if (isShow)
			Log.d(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (isShow)
			Log.e(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (isShow)
			Log.w(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (isShow)
			Log.v(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (isShow)
			Log.i(tag, msg);
	}

	public static void wtf(String tag, String msg) {
		if (isShow)
			Log.wtf(tag, msg);
	}
}
