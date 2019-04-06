package com.lyy.j1jhelper;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class Parser {


	// 分析距离
	public static float parse(MyService ms) {
		screenShoot();
		try {
			return Analyser.analyse(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0f;
	}

	// 截屏
	public static void screenShoot() {
		String cmd = "screencap -p " + Const.FILE_PATH;
		try {
			// 权限设置
			Process p = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = p.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			// 将命令写入
			dataOutputStream.writeBytes(cmd);
			// 提交命令
			dataOutputStream.flush();
			// 关闭流操作
			dataOutputStream.close();
			outputStream.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	// 模拟触摸屏幕
	public static void press(int time) {
		Log.e("aaa", "press_time:" + time);
		String cmd = "input swipe 360 640 360 640 " + time;
		try {
			// 权限设置
			Process p = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = p.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			// 将命令写入
			dataOutputStream.writeBytes(cmd);
			// 提交命令
			dataOutputStream.flush();
			// 关闭流操作
			dataOutputStream.close();
			outputStream.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
