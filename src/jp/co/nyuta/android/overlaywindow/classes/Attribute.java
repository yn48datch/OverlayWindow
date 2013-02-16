package jp.co.nyuta.android.overlaywindow.classes;

import android.app.Service;
import android.view.WindowManager;

/**
 * OverlayWindowの設定をまとめたクラス
 * @author Yuta
 */
public class Attribute {
	/** windowの横幅 */
	public int window_width;
	/** windowの縦幅 */
	public int window_height;
	/** serviceの種類 onStartCommandの戻り値に使用 */
	public int service_start_kind;

	public Attribute(){
		window_width  = WindowManager.LayoutParams.MATCH_PARENT;
		window_height = WindowManager.LayoutParams.WRAP_CONTENT;
		service_start_kind = Service.START_STICKY;
	}
}
