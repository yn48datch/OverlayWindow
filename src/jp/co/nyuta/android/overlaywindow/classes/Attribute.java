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
	/** OverlayWindow(OverlayWindowのView全体)でTouchEventを補足して、Window全体を動かすフラグ */
	public boolean enable_overlay_window_move;
	/** OverlayWindow(OverlayWindowのView全体)でTouchEventを補足し、User操作可能とする<br>
	 trueの場合、enable_overlay_window_move は強制的にfalseになります */
	public boolean enable_overlay_window_touch;
	/** OverlayWindowシステムで使用するWindowManager.LayoutParamsのフラグ<br>OverlayApplicationはFLAG_LAYOUT_NO_LIMITSをつけたり外したりします。  */
	public int overlay_window_flag;
	/** OverlayWindowシステムのLayerを指定します。<br>DefaultはWindowManager.LayoutParams.TYPE_PHONです。<br>注意！TYPE_SYSTEM_ERRORを使用すると、Androidシステムエラー時に何もできなくなる可能性があります。 */
	public int overlay_window_layer;
	/** minimization を有効にする・しないフラグ(For OverlayApplication) */
	public boolean enable_minimization;
	/** maximization を有効にする・しないフラグ(For OverlayApplication) */
	public boolean enable_maximization;
	/** minimization 復帰時にdefault位置に戻すOption(For OverlayApplication) */
	public boolean resume_reset_position;
	/** WindowBarのタッチイベントのみでWindowを動かすOption(For OverlayApplication) <br>
	 trueの場合、enable_overlay_window_move は強制的にfalseになります */
	public boolean only_windowbar_move;

	public Attribute(){
		window_width  = WindowManager.LayoutParams.MATCH_PARENT;
		window_height = WindowManager.LayoutParams.WRAP_CONTENT;
		service_start_kind = Service.START_STICKY;
		enable_overlay_window_move = true;
		enable_overlay_window_touch = false;
		enable_minimization = true;
		enable_maximization = true;
		resume_reset_position = true;
		only_windowbar_move = false;
		overlay_window_flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
							  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
							  WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		overlay_window_layer = WindowManager.LayoutParams.TYPE_PHONE;
	}
}
