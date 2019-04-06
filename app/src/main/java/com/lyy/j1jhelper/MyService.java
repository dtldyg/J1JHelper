package com.lyy.j1jhelper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
	public MyService myService;
	public List<View> windowViewList;
	public List<View> pointList;
	public WindowManager windowManager;
	public static boolean autoRunning;
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					// 添加点
					int x = msg.arg1;
					int y = msg.arg2;
					Log.e("aaa", "handleMessage:" + x + "," + y);

					WindowManager.LayoutParams params = new WindowManager.LayoutParams();
					params.width = WindowManager.LayoutParams.WRAP_CONTENT;
					params.height = WindowManager.LayoutParams.WRAP_CONTENT;
					params.type = WindowManager.LayoutParams.TYPE_PHONE;
					params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
					params.format = PixelFormat.TRANSPARENT;
					params.gravity = Gravity.LEFT | Gravity.TOP;
					params.x = x;
					params.y = y;

					ImageView im = new ImageView(myService);
					im.setBackgroundResource(R.mipmap.red_point);
					pointList.add(im);
					windowManager.addView(im, params);
					break;
				case 2:
					// 删除点
					for (View v : pointList) {
						windowManager.removeViewImmediate(v);
					}
					pointList = new ArrayList<>();
					break;
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		createWindow();
	}

	// 生成悬浮窗
	private void createWindow() {
		myService = this;
		windowViewList = new ArrayList<>();
		pointList = new ArrayList<>();

		windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.width = 240;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		params.format = PixelFormat.TRANSPARENT;

		// 单步
		params.gravity = Gravity.LEFT | Gravity.BOTTOM;
		Button btn1 = new Button(this);
		btn1.setText("单步");
		windowViewList.add(btn1);
		windowManager.addView(btn1, params);
		// 自动
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		Button btn2 = new Button(this);
		btn2.setText("自动:关");
		windowViewList.add(btn2);
		windowManager.addView(btn2, params);
		// 退出
		params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
		Button btn3 = new Button(this);
		btn3.setText("退出");
		windowViewList.add(btn3);
		windowManager.addView(btn3, params);

		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// 分析距离
						float distance = Parser.parse(myService);
						float time = distance * Const.JUMP_PARAM;
						// 模拟触摸屏幕
						Parser.press((int) time);
						try {
							Thread.sleep((int) time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 不能在子线程中更新UI
						Message msg = new Message();
						msg.what = 2;
						myService.getHandler().sendMessage(msg);
					}
				}).start();
			}
		});
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!autoRunning) {
					((Button) v).setText("自动:开");
					autoRunning = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							while (autoRunning) {
								// 分析距离
								float distance = Parser.parse(myService);
								float time = distance * Const.JUMP_PARAM;
								// 模拟触摸屏幕
								Parser.press((int) time);
								try {
									Thread.sleep((int) time);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// 不能在子线程中更新UI
								Message msg = new Message();
								msg.what = 2;
								myService.getHandler().sendMessage(msg);
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}).start();
				} else {
					((Button) v).setText("自动:关");
					autoRunning = false;
				}
			}
		});
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopSelf();
			}
		});
	}

	@Override
	public void onDestroy() {
		for (View v : windowViewList) {
			windowManager.removeViewImmediate(v);
		}
		for (View v : pointList) {
			windowManager.removeViewImmediate(v);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
