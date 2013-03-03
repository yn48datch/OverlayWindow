/**
 *
 */
package jp.co.nyuta.android.overlaywindow;

import jp.co.nyuta.android.overlaywindow.classes.Attribute;
import jp.co.nyuta.android.overlaywindow.classes.WindowMoveTouchListener;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

	private ViewGroup 					mWindowBarLayout = null;
	private TextView  					mWindowTitleTextView = null;
	private BroadcastReceiver 			mWindowReceiver = null;
	private WindowManager.LayoutParams 	mBeforeMaximizationLayout = null;
	private WindowMoveTouchListener 	mMoveTouchListener = null;
	private GestureDetector				mGestureDet = null;
	private ImageButton					mMaxToggleButton = null;
	private View						mDivider = null;

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
	public int getWindowBarHeight(){
		if(isWindowBarShown())
			return mWindowBarLayout.getHeight();
		return 0;
	}
	// ____________________________________________________________
	/**
	 * WindowBarの幅を取得 (表示領域をUser側で確認するため)
	 *
	 * @return WindowBarのWidth
	 *
	 */
	public int getWindowBarWidth(){
		if(isWindowBarShown())
			return mWindowBarLayout.getWidth();
		return 0;
	}
	// ____________________________________________________________
	/**
	 * WindowBarの表示
	 *
	 */
	public void showWindowBar(){
		mWindowBarLayout.setVisibility(View.VISIBLE);
		mDivider.setVisibility(View.VISIBLE);
	}
	// ____________________________________________________________
	/**
	 * WindowBarの非表示
	 *
	 */
	public void hideWindowBar(){
		mWindowBarLayout.setVisibility(View.GONE);
		mDivider.setVisibility(View.GONE);
	}

	// ____________________________________________________________
	/**
	 * WindowBarが表示中か？の確認
	 *
	 * @return true : 表示中 false : 非表示中
	 *
	 */
	public boolean isWindowBarShown(){
		if(mWindowBarLayout.getVisibility() == View.GONE){
			return false;
		}
		return true;
	}
	// ____________________________________________________________
	/**
	 * 最大化状態かどうかの確認
	 *
	 * @return true : 最大化中 false : 最大化中ではない
	 *
	 */
	public boolean isMaximization(){
		if(mBeforeMaximizationLayout == null){
			return false;
		}
		return true;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[OverlayWindow]						# */
	/* #														# */
	/* ########################################################## */
	@Override
	protected View setupRootView(LayoutInflater inflater, ViewGroup root) {
		View tobeRoot = inflater.inflate(R.layout.basic_window, root);
		mWindowBarLayout = (ViewGroup) tobeRoot.findViewById(R.id.windowbar_layout);
		mDivider = tobeRoot.findViewById(R.id.windowbar_divider);
		mWindowTitleTextView = (TextView) tobeRoot.findViewById(R.id.windowbar_title_textView);
		ImageView windowIcon = (ImageView) tobeRoot.findViewById(R.id.windowbar_appicon);
		setTitle(getThisClass().getSimpleName());
		ImageButton del = (ImageButton) tobeRoot.findViewById(R.id.windowbar_delete_imageButton);
		ImageButton min = (ImageButton) tobeRoot.findViewById(R.id.windowbar_hide_imageButton);
		mMaxToggleButton = (ImageButton) tobeRoot.findViewById(R.id.windowbar_fit_display_imageButton);
		del.setOnClickListener(mWindowClickListener);
		setupMinimization(min);
		setupMaximization(mMaxToggleButton);
		setupOnTouchListener(mWindowBarLayout, tobeRoot);

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
	// 最大化ボタンの設定
	private void setupMaximization(ImageButton maxButton){
		if(getWindowAttribute().enable_maximization){
			// Click Listenerの登録
			maxButton.setOnClickListener(mWindowClickListener);
		}else{
			maxButton.setVisibility(View.GONE);
			mMaxToggleButton = null;
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
		mBeforeMaximizationLayout = new WindowManager.LayoutParams();
		mBeforeMaximizationLayout.copyFrom(param);

		param.width = WindowManager.LayoutParams.MATCH_PARENT;
		param.height = WindowManager.LayoutParams.MATCH_PARENT;
		param.x = 0;
		param.y = 0;
		param.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(rootView, param);
		if(mMoveTouchListener != null){
			mMoveTouchListener.setMoveEnable(false);
		}
		mMaxToggleButton.setImageResource(R.drawable.window_normal_display);
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
		if(mBeforeMaximizationLayout == null){
			// fail safe
			mBeforeMaximizationLayout = (WindowManager.LayoutParams) rootView.getLayoutParams();
			mBeforeMaximizationLayout.width = getWindowAttribute().window_width;
			mBeforeMaximizationLayout.height = getWindowAttribute().window_height;
			mBeforeMaximizationLayout.x = 0;
			mBeforeMaximizationLayout.y = 0;
		}
		((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(rootView, mBeforeMaximizationLayout);
		mBeforeMaximizationLayout = null;
		if(mMoveTouchListener != null){
			mMoveTouchListener.setMoveEnable(true);
		}
		mMaxToggleButton.setImageResource(R.drawable.window_fit_display);
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
				.setContentTitle(mWindowTitleTextView.getText())
				.setContentText(subTitle)
				.getNotification();
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			// for jb
			notification = new Notification.Builder(getApplicationContext())
			.setSmallIcon(iconResId)
			.setOngoing(true)
			.setContentIntent(pi)
			.setContentTitle(mWindowTitleTextView.getText())
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
			}else if(id == R.id.windowbar_fit_display_imageButton){
				ToggleMaxNormalWindow();
			}

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
					// TODO 自動生成されたメソッド・スタブ
					super.onLongPress(e);
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
