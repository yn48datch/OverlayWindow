/**
 *
 */
package jp.co.nyuta.android.overlaywindow;

import jp.co.nyuta.android.overlaywindow.classes.Attribute;
import jp.co.nyuta.android.overlaywindow.classes.WindowBar;
import jp.co.nyuta.android.overlaywindow.classes.WindowMoveTouchListener;
import jp.co.nyuta.android.overlaywindow.classes.WindowMoveTouchListener.OnMoveListener;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;


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

	private BroadcastReceiver 			mWindowReceiver = null;
	private BeforeMaximizationInfo		mBeforeMaximizationInfo = null;
	private WindowMoveTouchListener 	mMoveTouchListener = null;
	private GestureDetector				mGestureDet = null;
	private WindowBar					mWindowBar = null;

	/* ########################################################## */
	/* #														# */
	/* #					[Inner Class]						# */
	/* #														# */
	/* ########################################################## */
	private static class BeforeMaximizationInfo{
		public WindowManager.LayoutParams 	LayoutParam = new WindowManager.LayoutParams();
		public Point						WindowSize = new Point();
		//public BeforeMaximizationInfo(){}
		public BeforeMaximizationInfo(WindowManager.LayoutParams param, Point size){
			if(param != null)
				LayoutParam.copyFrom(param);

			if(size != null){
				WindowSize.x = size.x;
				WindowSize.y = size.y;
			}
		}
	}

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
	/* #						[public]						# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * WindowBarの高さを取得 (表示領域をUser側で確認するため)
	 *
	 * @return WindowBarのHeight
	 *
	 */
	@Deprecated
	public int getWindowBarHeight(){
		return mWindowBar.getHeight();
	}
	// ____________________________________________________________
	/**
	 * WindowBarの幅を取得 (表示領域をUser側で確認するため)
	 *
	 * @return WindowBarのWidth
	 *
	 */
	@Deprecated
	public int getWindowBarWidth(){
		return mWindowBar.getWidth();
	}
	// ____________________________________________________________
	/**
	 * WindowBarの表示
	 *
	 */
	@Deprecated
	public void showWindowBar(){
		mWindowBar.show();
	}
	// ____________________________________________________________
	/**
	 * WindowBarの非表示
	 *
	 */
	@Deprecated
	public void hideWindowBar(){
		mWindowBar.hide();
	}

	// ____________________________________________________________
	/**
	 * WindowBarが表示中か？の確認
	 *
	 * @return true : 表示中 false : 非表示中
	 *
	 */
	@Deprecated
	public boolean isWindowBarShown(){
		return mWindowBar.isShown();
	}
	// ____________________________________________________________
	/**
	 * 最大化状態かどうかの確認
	 *
	 * @return true : 最大化中 false : 最大化中ではない
	 *
	 */
	public boolean isMaximization(){
		if(mBeforeMaximizationInfo == null){
			return false;
		}
		return true;
	}
	// ____________________________________________________________
	/**
	 * WindowBarクラスの取得
	 *
	 * @return WindowBarクラス
	 *
	 */
	public WindowBar getWindowBar(){
		return mWindowBar;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[OverlayWindow]						# */
	/* #														# */
	/* ########################################################## */
	@Override
	protected View setupRootView(LayoutInflater inflater, ViewGroup root) {
		View tobeRoot = inflater.inflate(R.layout.basic_window, root);
		ViewGroup windowbarContainer = (ViewGroup) tobeRoot.findViewById(R.id.windowbar_layout);

		setupOnTouchListener(windowbarContainer, tobeRoot);

		// WindowBarの設定
		WindowBar.ResourceInformation info = new WindowBar.ResourceInformation();	// TODO Userから設定出来るように変更が必要
		mWindowBar = new WindowBar(inflater, windowbarContainer, info, getWindowAttribute());
		mWindowBar.setTitle(getThisClass().getSimpleName());
		mWindowBar.setOnWindowBarEvent(mWindowBarListener);

		int iconResId = getWindowIconResourceId();
		if(iconResId != 0){
			mWindowBar.setIcon(iconResId);
		}

		// Recieverを作成
		setupWindowEventReceiver();
		onCreateView(inflater, (ViewGroup) tobeRoot.findViewById(R.id.window_container));

		// ServiceのNotificationを作成
		setupServiceNotification();

		return tobeRoot;
	}

	// ____________________________________________________________
	/**
	 * OverlayApplicationのattribute 取得
	 *
	 * @param  default_attr  デフォルトのattribute
	 * @return subクラスで規定したい設定<br>nullを返却した場合はdefaultの設定を行う
	 */
	@Override
	protected final Attribute getAttribute(Attribute default_attr){
		Attribute ret = getOverlayApplicationAttribute(default_attr);
		if(ret.only_windowbar_move){
			ret.enable_overlay_window_move = false;
		}
		return ret;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[private]							# */
	/* #														# */
	/* ########################################################## */
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
	// 最大化・通常化のトグル処理
	private void ToggleMaxNormalWindow(){
		if(!isMaximization()){
			onLayoutFitDisplay();
		}else{
			onLayoutNormalDisplay();
		}
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
	 * 最大化時のEvent.
	 * <p>
	 * 最大化時に処理を行いたい場合はOverrideしてください。<br>
	 * defaultでは、ViewのWidth/HeightをMATCH_PARENTにします。<br>
	 * また、復帰時の情報のため、変更前のLayoutParamを保持します。
	 * </p>
	 *
	 */
	protected void onLayoutFitDisplay(){
		View rootView = getRootView();
		WindowManager.LayoutParams param = (WindowManager.LayoutParams) rootView.getLayoutParams();
		mBeforeMaximizationInfo = new BeforeMaximizationInfo(param, getWindowSize());

		param.width = WindowManager.LayoutParams.MATCH_PARENT;
		param.height = WindowManager.LayoutParams.MATCH_PARENT;
		param.x = 0;
		param.y = 0;
		param.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(rootView, param);

		// WindowBarの最大化時設定
		mWindowBar.noticeChangeApplicationStatus(true);
	}
	// ____________________________________________________________
	/**
	 * 最大化復帰時のEvent.
	 * <p>
	 * 最大化復帰時に処理を行いたい場合はOverrideしてください。<br>
	 * defaultでは、Attribute情報にて復帰します。
	 * </p>
	 */
	protected void onLayoutNormalDisplay(){
		View rootView = getRootView();
		if(mBeforeMaximizationInfo == null){
			// fail safe
			mBeforeMaximizationInfo = new BeforeMaximizationInfo((WindowManager.LayoutParams) rootView.getLayoutParams(), getWindowSize());
			mBeforeMaximizationInfo.LayoutParam.width = getWindowAttribute().window_width;
			mBeforeMaximizationInfo.LayoutParam.height = getWindowAttribute().window_height;
			mBeforeMaximizationInfo.LayoutParam.x = 0;
			mBeforeMaximizationInfo.LayoutParam.y = 0;
		}
		((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(rootView, mBeforeMaximizationInfo.LayoutParam);
		mBeforeMaximizationInfo = null;

		// WindowBarの通常化時設定
		mWindowBar.noticeChangeApplicationStatus(false);
	}
	// ____________________________________________________________
	/**
	 * Fitスクリーン状態の取得
	 *
	 * @return Fitスクリーン状態ならTrue
	 */
	protected final boolean isFitWindowScreen(){
		if(mBeforeMaximizationInfo != null){
			return true;
		}
		return false;
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
		String subTitle = OverlayApplication.class.getSimpleName();
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
				.setContentTitle(mWindowBar.getTitle())
				.setContentText(subTitle)
				.getNotification();
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			// for jb
			notification = new Notification.Builder(getApplicationContext())
			.setSmallIcon(iconResId)
			.setOngoing(true)
			.setContentIntent(pi)
			.setContentTitle(mWindowBar.getTitle())
			.setContentText(subTitle)
			.build();
		}else{
			// for gb
			notification = new Notification(
					iconResId,
			        null,
			        System.currentTimeMillis());
			notification.setLatestEventInfo(getApplicationContext(), getThisClass().getSimpleName(), subTitle, pi);
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
	private WindowBar.OnWindowBarEvent mWindowBarListener = new WindowBar.OnWindowBarEvent(){
		@Override
		public void onMinButtonClicked() {
			View root = getRootView();
			if(root.getVisibility() == View.VISIBLE){
				hide(root);
			}
		}

		@Override
		public void onMaxButtonClicked() {
			ToggleMaxNormalWindow();
		}

		@Override
		public void onDelButtonClicked() {
			onPreSelfDelete();
		}

	};

	private void setupOnTouchListener(View targetWindowBar, View root){
		if(getWindowAttribute().only_windowbar_move){
			mMoveTouchListener = new WindowMoveTouchListener(root, getApplicationContext());
			mMoveTouchListener.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(mGestureDet != null){
						mGestureDet.onTouchEvent(event);
					}
					return true;
				}
			});
			targetWindowBar.setOnTouchListener(mMoveTouchListener);
			mGestureDet = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener(){
				@Override
				public boolean onDoubleTap(MotionEvent e) {
					ToggleMaxNormalWindow();
					return super.onDoubleTap(e);
				}

				@Override
				public void onLongPress(MotionEvent e) {
					super.onLongPress(e);
				}

			});
			mMoveTouchListener.setOnMoveListener(new OnMoveListener(){
				@Override
				public boolean onMoveStart() {
					if(isFitWindowScreen()){
						// 最大化時に動いたらNormalのスクリーンに修正する

						// 最大化時、WindowBarは上にある状態なので、復帰時のY位置を上部に設定する
						Point disp_size = getDisplaySize(getApplicationContext());
						mBeforeMaximizationInfo.LayoutParam.y = ((disp_size.y - mBeforeMaximizationInfo.WindowSize.y) / 2) * -1;
						// NormalDisplay設定を行う
						onLayoutNormalDisplay();
					}
					return false;
				}
				@Override
				public boolean onMoveEnd() {
					return false;
				}
			});
		}
	}

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

	// ____________________________________________________________
	/**
	 * OverlayApplicationのattribute 取得
	 *
	 * @param  default_attr  デフォルトのattribute
	 * @return subクラスで規定したい設定<br>nullを返却した場合はdefaultの設定を行う
	 */
	protected abstract Attribute getOverlayApplicationAttribute(Attribute default_attr);
}
