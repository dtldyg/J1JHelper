package com.lyy.j1jhelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

public class Analyser {

	private static String TAG = "aaa";
	private static float MAGIC = 1.433f; // 阴影比例
	private static int PLAYER_R = 56;    // 角色颜色
	private static int PLAYER_G = 56;    // 角色颜色
	private static int PLAYER_B = 96;    // 角色颜色
	private static int SIMILAR = 1;

	public static float analyse(MyService ms) throws Exception {
		Bitmap bitmap = null;
		while (bitmap == null) {
			bitmap = BitmapFactory.decodeFile(Const.FILE_PATH);
			Thread.sleep(100);
		}
		return getLength(bitmap, ms);
	}

	private static float getLength(Bitmap bitmap, MyService ms) {
		int dx = bitmap.getWidth();
		int dy = bitmap.getHeight();
		int offset = dy / 4;

		int p_x = -1;
		int p_y = -1;
		int t_x = -1;
		int t_y = -1;

		// 找到玩家点
		for (int y = offset; y < dy; y++) {
			for (int x = 0; x < dx; x++) {
				int[] c = getRGB(bitmap.getPixel(x, y));
				if (c[0] == PLAYER_R && c[1] == PLAYER_G && c[2] == PLAYER_B) {
					p_x = x;
					p_y = y;
					break;
				}
			}
			if (p_x > -1) {
				break;
			}
		}
		Log.e(TAG, "player_x_y:" + p_x + "," + p_y);
		addPoint(ms, p_x, p_y);

		// 找到目标点
		int tot_x1 = -1;
		int tot_x2 = -1;
		int tot_y = -1;
		int btn_y = -1;
		int bg;
		for (int y = offset; y < dy; y++) {
			bg = bitmap.getPixel(0, y);
			for (int x = 0; x < dx; x++) {
				if (filter(bitmap.getPixel(x, y), bg)) {
					tot_x2 = x;
					if (tot_x1 == -1 && tot_y == -1) {
						tot_x1 = x;
						tot_y = y;
					}
				}
			}
			if (tot_y != -1) {
				break;
			}
		}
		t_x = (tot_x1 + tot_x2) / 2;
		bg = bitmap.getPixel(0, tot_y);
		for (int y = tot_y; y < dy; y++) {
			// 从左还是从右
			if (isSimilarPix(bitmap.getPixel(0, y), bg)) {
				bg = bitmap.getPixel(0, y);
			} else {
				bg = bitmap.getPixel(dx - 1, y);
			}
			if (!filter(bitmap.getPixel(t_x, y), bg)) {
				btn_y = y - 1;
				break;
			}
		}
		t_y = tot_y + (btn_y - tot_y) * 2 / 7;
		Log.e(TAG, "target_x_y:" + t_x + "," + t_y);
		addPoint(ms, t_x, t_y);

		int ddx = Math.abs(p_x - t_x);
		int ddy = Math.abs(p_y - t_y);
		float len = (float) Math.sqrt(ddx * ddx + ddy * ddy);
		Log.e(TAG, "len:" + len);


		return len;
	}

	// 近似像素（rgb最多都差1）
	private static boolean isSimilarPix(int c, int lc) {
		int[] cc = getRGB(c);
		int[] lcc = getRGB(lc);
		if ((cc[0] >= lcc[0] - SIMILAR && cc[0] <= lcc[0] + SIMILAR) &&
				(cc[1] >= lcc[1] - SIMILAR && cc[1] <= lcc[1] + SIMILAR) &&
				(cc[2] >= lcc[2] - SIMILAR && cc[2] <= lcc[2] + SIMILAR)) {
			return true;
		}
		return false;
	}

	private static boolean filter(int c, int bg) {
		// 背景
		if (isSamePix(c, bg)) {
			return false;
		}
		// 判断阴影
		int[] bb = getRGB(bg);
		bb[0] = f2i(bb[0] / MAGIC);
		bb[1] = f2i(bb[1] / MAGIC);
		bb[2] = f2i(bb[2] / MAGIC);

		int[] cb = getRGB(c);
		if ((cb[0] >= bb[0] - SIMILAR && cb[0] <= bb[0] + SIMILAR) &&
				(cb[1] >= bb[1] - SIMILAR && cb[1] <= bb[1] + SIMILAR) &&
				(cb[2] >= bb[2] - SIMILAR && cb[2] <= bb[2] + SIMILAR)) {
			return false;
		}
		return true;
	}

	private static int f2i(float f) {
		int rt = (int) f;
		if ((f - rt) >= 0.5) {
			rt++;
		}
		return rt;
	}

	// 相同像素（rgb完全一致）
	private static boolean isSamePix(int c, int lc) {
		int[] cc = getRGB(c);
		int[] lcc = getRGB(lc);
		if ((cc[0] == lcc[0]) && (cc[1] == lcc[1]) && (cc[2] == lcc[2])) {
			return true;
		}
		return false;
	}

	private static int[] getRGB(int c) {
		int[] rt = new int[3];
		rt[0] = Color.red(c);
		rt[1] = Color.green(c);
		rt[2] = Color.blue(c);
		return rt;
	}

	private static void addPoint(MyService ms, int x, int y) {
		// 不能在子线程中更新UI
		Message msg = new Message();
		msg.what = 1;
		msg.arg1 = x;
		msg.arg2 = y;
		ms.getHandler().sendMessage(msg);
	}
}
