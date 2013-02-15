/**
 *
 */
package jp.co.nyuta.android.overlaywindow;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * @author Yuta
 *
 */
public abstract class OverlayApplication extends OverlayWindow {

	//private ViewGroup mWindowBarLayout = null;
	private TextView  mWindowTitleTextView = null;

	@Override
	protected View setupRootView(LayoutInflater inflater, ViewGroup root) {
		View tobeRoot = inflater.inflate(R.layout.basic_window, root);
		//mWindowBarLayout = (ViewGroup) tobeRoot.findViewById(R.id.windowbar_layout);
		mWindowTitleTextView = (TextView) tobeRoot.findViewById(R.id.windowbar_title_textView);
		ImageView windowIcon = (ImageView) tobeRoot.findViewById(R.id.windowbar_appicon);
		setTitle(getThisClass().getSimpleName());
		ImageButton del = (ImageButton) tobeRoot.findViewById(R.id.windowbar_delete_imageButton);
		del.setOnClickListener(mWindowClickListener);

		int iconResId = getWindowIconResourceId();
		if(iconResId != 0){
			windowIcon.setImageResource(iconResId);
		}
		setupServiceNotification();
		onCreateView(inflater, (ViewGroup) tobeRoot.findViewById(R.id.window_container));

		return tobeRoot;
	}

	protected void onPreSelfDelete(){
		stopSelf();
	}

	protected void setTitle(int resId){
		mWindowTitleTextView.setText(resId);
	}
	protected void setTitle(String title){
		mWindowTitleTextView.setText(title);
	}
	protected void setNotification(Notification notify){
		startForeground(getNotificationId(), notify);
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	protected void setupServiceNotification(){
		Notification notification;
		int iconResId = getWindowIconResourceId();
		if(iconResId == 0){
			iconResId = android.R.drawable.ic_menu_crop;
		}
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
			// for ics
			notification = new Notification.Builder(getApplicationContext())
				.setSmallIcon(iconResId)
				.setOngoing(true)
				.getNotification();
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			// for jb
			notification = new Notification.Builder(getApplicationContext())
			.setSmallIcon(iconResId)
			.setOngoing(true)
			.setContentTitle(mWindowTitleTextView.getText())
			.build();
		}else{
			// for gb
			notification = new Notification(
					iconResId,
			        null,
			        System.currentTimeMillis());
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			notification.number = 0;
		}
		setNotification(notification);
	}

	private OnClickListener mWindowClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.windowbar_delete_imageButton){
				onPreSelfDelete();
			}

		}

	};
	protected abstract int getWindowIconResourceId();
	protected abstract int getNotificationId();
}
