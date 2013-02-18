/**
 *
 */
package jp.co.nyuta.android.overlaywindow;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * WindowBarを備えた、一般アプリケーション向けのOverlayWindow用抽象クラス<br>
 * このクラスを継承して作ったクラスはServiceとなります。
 * AndroidManifestにPermissionとServiceの登録が必要です。<br>
 * <br>
 * 必要なuses-permission : android.permission.SYSTEM_ALERT_WINDOW <br>
 *
 * @author Yuta
 *
 */
public abstract class OverlayApplication extends OverlayWindow {

	//private ViewGroup mWindowBarLayout = null;
	private TextView  mWindowTitleTextView = null;
	private BroadcastReceiver mWindowReceiver = null;

	/* ########################################################## */
	/* #														# */
	/* #					[Service]							# */
	/* #														# */
	/* ########################################################## */
	/**
	 * Service破棄時にコールされる<br>
	 * ここで、必要なクラス終了、破棄処理を行う
	 *
	 * @see jp.co.nyuta.android.overlaywindow.OverlayWindow#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if(mWindowReceiver != null){
			// レシーバの登録解除
			getApplicationContext().unregisterReceiver(mWindowReceiver);
		}
		super.onDestroy();
	}

	/* ########################################################## */
	/* #														# */
	/* #					[OverlayWindow]						# */
	/* #														# */
	/* ########################################################## */
	@Override
	protected View setupRootView(LayoutInflater inflater, ViewGroup root) {
		View tobeRoot = inflater.inflate(R.layout.basic_window, root);
		//mWindowBarLayout = (ViewGroup) tobeRoot.findViewById(R.id.windowbar_layout);
		mWindowTitleTextView = (TextView) tobeRoot.findViewById(R.id.windowbar_title_textView);
		ImageView windowIcon = (ImageView) tobeRoot.findViewById(R.id.windowbar_appicon);
		setTitle(getThisClass().getSimpleName());
		ImageButton del = (ImageButton) tobeRoot.findViewById(R.id.windowbar_delete_imageButton);
		ImageButton min = (ImageButton) tobeRoot.findViewById(R.id.windowbar_hide_imageButton);
		del.setOnClickListener(mWindowClickListener);
		setupMinimization(min);

		int iconResId = getWindowIconResourceId();
		if(iconResId != 0){
			windowIcon.setImageResource(iconResId);
		}

		// Recieverを作成
		setupWindowEventReceiver();
		onCreateView(inflater, (ViewGroup) tobeRoot.findViewById(R.id.window_container));
		// ServiceのNotificationを作成
		setupServiceNotification();

		return tobeRoot;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[private]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	// 最小化ボタンの設定
	private void setupMinimization(ImageButton minButton){
		if(getWindowAttribute().enable_minimization){
			// Click Listenerの登録
			minButton.setOnClickListener(mWindowClickListener);
		}else{
			minButton.setVisibility(View.GONE);
		}
	}
	// ____________________________________________________________
	// OverlayApplication 表示再開
	private void show(View rootView){
		onResume();
		if(getWindowAttribute().resume_reset_position){
			// Windowを元の位置に戻す
			setDefaultPotition();
		}
		rootView.setVisibility(View.VISIBLE);
	}

	// ____________________________________________________________
	// OverlayApplication サスペンド
	private void hide(View rootView){
		onSuspend();
		rootView.setVisibility(View.INVISIBLE);
	}

	// ____________________________________________________________
	// 最小化・最大化のIntentを取得
	private Intent getToggleShowHideIntent(){
		if(getWindowAttribute().enable_minimization){
			Intent ret = new Intent("overlaywindow.toggle.show_hide");
			ret.putExtra("notification_id", getNotificationId());
			return ret;
		}
		return null;
	}

	// ____________________________________________________________
	// OverlayApplication のイベントレシーバ
	// すべての設定はAttribute or タイミング依存の無いように設計が必要
	// これを呼んだ時にmWindowReceiverが作られる。(null開放して、新たに作るでもいいかもしれない)
	private void setupWindowEventReceiver(){
		if(mWindowReceiver != null){
			Log.d(getClass().getSimpleName(), "unknown reciever create event!!");
			return;
		}
		// Intent filterの生成
		IntentFilter filter = new IntentFilter();
		boolean create = false;

		// ________________________________________________
		// filter - toggle show / hide
		if(getWindowAttribute().enable_minimization){
			filter.addAction("overlaywindow.toggle.show_hide");
			create = true;
		}

		// Reciever作成が必要かどうかの判断
		if(!create){
			// 作りません
			filter = null;
			return;
		}

		// BroadcastReceiverの作成
		mWindowReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {

				// ________________________________________________
				// toggle show / hide
				if("overlaywindow.toggle.show_hide".equals(intent.getAction())){
					int index = intent.getIntExtra("notification_id", 0);
					// TODO @debug start
					Log.d(getThisClass().getSimpleName(), "on Show/Hide event id : " + index);
					// TODO @debug end

					if(index != getNotificationId())
						return;
					// show/hideのトグル処理
					View root = getRootView();
					if(root.getVisibility() == View.VISIBLE){
						hide(root);
					}else{
						show(root);
					}
				}
				// end of onReceive.
			}
		};

		// Receiverを登録する
		getApplicationContext().registerReceiver(mWindowReceiver, filter);
	}

	/* ########################################################## */
	/* #														# */
	/* #					[protected]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * x ボタンを押した時などで、OverlayApplicationが自己終了する時のEvent
	 * <p>
	 * もし、終了確認をしたい場合は、Overrideして処理を入れてください。<br>
	 * Overrideした場合、superをコールすると、stopSelf()を実行します。
	 * </p>
	 *
	 */
	protected void onPreSelfDelete(){
		stopSelf();
	}
	// ____________________________________________________________
	/**
	 * 最小化時のEvent. ViewをHideする前に呼ばれる
	 * <p>
	 * 最小化前に処理を行いたい場合はOverrideしてください。
	 * </p>
	 *
	 */
	protected void onSuspend(){
	}
	// ____________________________________________________________
	/**
	 * 最小化復帰時のEvent. ViewをSHOWする前に呼ばれる
	 * <p>
	 * 最小化復帰前に処理を行いたい場合はOverrideしてください。
	 * </p>
	 */
	protected void onResume(){
	}

	// ____________________________________________________________
	/**
	 * WindowBarのタイトル設定 .
	 * <p>
	 * Overrideする場合は、superを呼ぶこと
	 * </p>
	 *
	 * @param  resId テキストのリソースID
	 */
	protected void setTitle(int resId){
		mWindowTitleTextView.setText(resId);
	}
	// ____________________________________________________________
	/**
	 * WindowBarのタイトル設定 .
	 * <p>
	 * Overrideする場合は、superを呼ぶこと
	 * </p>
	 *
	 * @param  title タイトル用文字列
	 */
	protected void setTitle(String title){
		mWindowTitleTextView.setText(title);
	}
	// ____________________________________________________________
	/**
	 * Notificationの設定 .
	 * <p>
	 * Overrideする場合は、superを呼ぶこと
	 * </p>
	 *
	 * @param  notify Notificationクラス
	 */
	protected void setNotification(Notification notify){
		startForeground(getNotificationId(), notify);
	}

	// ____________________________________________________________
	/**
	 * Notificationの作成と設定 .
	 * <p>
	 * Overrideする場合は、setNotification()をコールしてください
	 * </p>
	 *
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	protected void setupServiceNotification(){
		Notification notification;
		int iconResId = getWindowIconResourceId();
		if(iconResId == 0){
			iconResId = android.R.drawable.ic_menu_crop;
		}

		Intent it = getToggleShowHideIntent();
		PendingIntent pi = null;
		if(it != null)
			pi = PendingIntent.getBroadcast(getApplicationContext(), 0, it, PendingIntent.FLAG_UPDATE_CURRENT);


		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
			// for ics
			notification = new Notification.Builder(getApplicationContext())
				.setSmallIcon(iconResId)
				.setOngoing(true)
				.setContentIntent(pi)
				.setContentTitle(mWindowTitleTextView.getText())
				.getNotification();
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			// for jb
			notification = new Notification.Builder(getApplicationContext())
			.setSmallIcon(iconResId)
			.setOngoing(true)
			.setContentIntent(pi)
			.setContentTitle(mWindowTitleTextView.getText())
			.build();
		}else{
			// for gb
			notification = new Notification(
					iconResId,
			        null,
			        System.currentTimeMillis());
			notification.setLatestEventInfo(getApplicationContext(), getThisClass().getSimpleName(), null, pi);
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			notification.number = 0;
		}
		setNotification(notification);
	}

	/* ########################################################## */
	/* #														# */
	/* #					[Listener]							# */
	/* #														# */
	/* ########################################################## */
	private OnClickListener mWindowClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.windowbar_delete_imageButton){
				onPreSelfDelete();
			}else if(id == R.id.windowbar_hide_imageButton){
				View root = getRootView();
				if(root.getVisibility() == View.VISIBLE){
					hide(root);
				}
			}

		}

	};

	/* ########################################################## */
	/* #														# */
	/* #					[abstract]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * Application Icon (WIndowBarやNotificationに表示する) のリソースID取得
	 *
	 * @return Application Icon (WIndowBarやNotificationに表示する) のリソースID
	 */
	protected abstract int getWindowIconResourceId();

	// ____________________________________________________________
	/**
	 * NotificationのNumberを取得
	 *
	 * @return NotificationのNumber
	 */
	protected abstract int getNotificationId();
}
