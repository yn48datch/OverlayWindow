package jp.co.nyuta.android.overlaywindow.classes;

import android.app.Service;
import android.view.WindowManager;

public class Attribute {
	public int window_width;
	public int window_height;
	public int service_start_kind;
	public Attribute(){
		window_width  = WindowManager.LayoutParams.MATCH_PARENT;
		window_height = WindowManager.LayoutParams.WRAP_CONTENT;
		service_start_kind = Service.START_STICKY;
	}
}
