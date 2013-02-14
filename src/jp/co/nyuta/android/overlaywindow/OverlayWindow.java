package jp.co.nyuta.android.overlaywindow;

import jp.co.nyuta.android.overlaywindow.classes.Attribute;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;

public abstract class OverlayWindow extends Service {
	private WindowManager	mWindowManager = null;
	private Attribute 		mAttr = new Attribute();
	private View			mRootView = null;
	private Intent			mIntent = null;
	/* ########################################################## */
	/* #														# */
	/* #					[static]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * 最適化したWindowサイズを取得する .
	 * <p>
	 * getAttribute()で返却するAttributeに設定すると、最適化したWindowサイズが適用されます。
	 * </p>
	 *
	 * @param  context  コンテキスト
	 * @return 最適化したWindowサイズ。Display縦横の「短い方」をWidth, HeightはWRAP_CONTENTS
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static final Point getOptimizedWindowSize(Context context){
		Point optimize = new Point(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
		(((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()).getSize(optimize);

		if(optimize.x > optimize.y){
			optimize.x = optimize.y;
		}
		optimize.y = WindowManager.LayoutParams.WRAP_CONTENT;

		return optimize;
	}


	/* ########################################################## */
	/* #														# */
	/* #					[Service]							# */
	/* #														# */
	/* ########################################################## */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/* (非 Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mAttr = getAttribute(mAttr);
		if(mAttr == null)
			mAttr = new Attribute();
	}

	/* (非 Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if(mWindowManager == null){
			mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		}
		mWindowManager.removeView(mRootView);
		mWindowManager = null;
		mRootView = null;
		super.onDestroy();
	}

	/* (非 Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(mRootView == null){
			// 構築
			mIntent = intent;
			Log.d(getThisClass().getSimpleName(), "onStartCommand");
			setupView();
			mIntent = null;
		}else{
			// 要求来たよ
			onCommand(intent);
		}
		return mAttr.service_start_kind;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[private]							# */
	/* #														# */
	/* ########################################################## */
	private void setupView(){
		if(mWindowManager != null){
			// 多重起動不可
			return;
		}
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		int window_flag = 	WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
    			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
    			WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
    			WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
    			WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
						mAttr.window_width,										// width
						mAttr.window_height,									// height
						WindowManager.LayoutParams.TYPE_PHONE,					// type
						window_flag,											// flag
						PixelFormat.TRANSLUCENT);								// format


		// Viewを構築
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootView = setupRootView(inflater, null);
		mRootView.setOnTouchListener(mWindowTouchListener);

		// WindowManagerにViewを追加
		mWindowManager.addView(mRootView, params);
	}

	/* ########################################################## */
	/* #														# */
	/* #					[protected]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * RootView作成 .
	 * <p>
	 * Overrideする場合は、superを呼ぶか、onCreateView()を呼ぶか、final修飾子付加が推奨
	 * </p>
	 *
	 * @param  inflater  LayoutInflater
	 * @param  root  inflateの第2引数に渡すことを推奨するrootのViewGroup
	 * @return アプリケーション・Layoutのルート
	 */
	protected View setupRootView(LayoutInflater inflater, ViewGroup root){
		return onCreateView(inflater, root);
	}
	// ____________________________________________________________
	/**
	 * Intentの取得 .
	 * <p>
	 * onCreateView内でViewの構築や初期化のため。それ以外のタイミングではnullになります。
	 * </p>
	 *
	 * @return onStartCommandのIntent
	 */
	protected final Intent getIntent(){
		return mIntent;
	}

	// ____________________________________________________________
	/**
	 * onStartCommandで追加のIntentが来た時のEvent
	 *
	 * @param  intent intent
	 */
	protected void onCommand(Intent intent){
	}

	// ____________________________________________________________
	/**
	 * onTouchEvent
	 *
	 * @param  event タッチイベントの情報
	 */
	protected void onWindowTouchEvent(MotionEvent event){
	}

	/* ########################################################## */
	/* #														# */
	/* #					[abstract]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * 	クラス取得
	 *
	 * @return クラスを返却
	 */
	protected abstract Class<?> getThisClass();

	// ____________________________________________________________
	/**
	 * OverlayWindowのattribute 取得
	 *
	 * @param  default_attr  デフォルトのattribute
	 * @return subクラスで規定したい設定<br>nullを返却した場合はdefaultの設定を行う
	 */
	protected abstract Attribute getAttribute(Attribute default_attr);
	// ____________________________________________________________
	/**
	 * View生成時のイベント
	 *
	 * @param  inflater  LayoutInflater
	 * @param  root  inflateの第2引数に渡すことを推奨するrootのViewGroup
	 * @return アプリケーション・Layoutのルート
	 */
	protected abstract View onCreateView(LayoutInflater inflater, ViewGroup root);
	/* ########################################################## */
	/* #														# */
	/* #					[Listener]							# */
	/* #														# */
	/* ########################################################## */

	private final OnTouchListener mWindowTouchListener = new OnTouchListener(){
		private int mPreMoveX = -1;
		private int mPreMoveY = -1;
		private int mPreFirstPointX = -1;
		private int mPreFirstPointY = -1;
		private boolean mIsMoving = false;
		private int MOVE_THRESHOLD = 0;				// 移動閾値
		private int MOVE_JUDGE_THRESHOLD = 8;		// 移動判定閾値

		private void judgeMovingStatus(){
			if(Math.abs(mPreFirstPointX - mPreMoveX)  >= MOVE_JUDGE_THRESHOLD ||
			   Math.abs(mPreFirstPointY - mPreMoveY)  >= MOVE_JUDGE_THRESHOLD){
				mIsMoving = true;
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean isUserEventExpire = true;
			switch(event.getAction()){
			case MotionEvent.ACTION_MOVE:
				move((int)event.getRawX(), (int)event.getRawY());
				break;
			case MotionEvent.ACTION_UP:
				mPreMoveX = -1;
				mPreMoveY = -1;
				if(mIsMoving)
					isUserEventExpire = false;
				mIsMoving = false;
				break;
			}
			if(!mIsMoving && isUserEventExpire)
				onWindowTouchEvent(event);
			return true;
		}
		private void move(int x, int y){
			if(mPreMoveX == -1 || mPreMoveY == -1){
				mPreMoveX = x;
				mPreMoveY = y;
				mPreFirstPointX = x;
				mPreFirstPointY = y;
				return;
			}

			// 動きがあったかみる
			if(Math.abs(x - mPreMoveX) >= MOVE_THRESHOLD && Math.abs(y - mPreMoveY) >= MOVE_THRESHOLD){
				// 差分を抽出
				int dif_x = x - mPreMoveX;
				int dif_y = y - mPreMoveY;

				//移動箇所
				WindowManager.LayoutParams param = (WindowManager.LayoutParams) mRootView.getLayoutParams();
				int margine_x = param.x + dif_x;
				int margine_y = param.y + dif_y;

				// 移動処理
				param.y = margine_y;
				param.x = margine_x;
				mWindowManager.updateViewLayout(mRootView, param);
				// 移動の保存
				mPreMoveX = x;
				mPreMoveY = y;
				if(!mIsMoving){
					judgeMovingStatus();
				}

			}
		}


	};
}
