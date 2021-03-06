package jp.co.nyuta.android.overlaywindow;

import jp.co.nyuta.android.overlaywindow.classes.Attribute;
import jp.co.nyuta.android.overlaywindow.classes.WindowMoveTouchListener;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 *
 * OverlayWindow（他のActivityより上のLayerで表示・常駐する）用抽象クラス<br>
 * このクラスを継承して作ったクラスはServiceとなります。
 * AndroidManifestにPermissionとServiceの登録が必要です。<br>
 * <br>
 * 必要なuses-permission : android.permission.SYSTEM_ALERT_WINDOW <br>
 *
 * @author Yuta
 *
 */
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
	 * @return 最適化したWindowサイズ。Display縦横の「短い方」をWidth/Heightに設定
	 */
	public static final Point getOptimizedWindowSize(Context context){
		Point optimize = getDisplaySize(context);

		if(optimize.x > optimize.y){
			optimize.x = optimize.y;
		}
		optimize.y = optimize.x;

		return optimize;
	}
	// ____________________________________________________________
	/**
	 * 画面サイズを取得する .
	 * <p>
	 * 端末の画面サイズを取得する
	 * </p>
	 *
	 * @param  context  コンテキスト
	 * @return 端末の画面サイズ
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static final Point getDisplaySize(Context context){
		Display disp = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point sizepoint = new Point();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
			disp.getSize(sizepoint);
		}else{
			sizepoint.x = disp.getWidth();
			sizepoint.y = disp.getHeight();
		}
		return sizepoint;
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

	/**
	 * Service生成時にコールされる<br>
	 * ここで、getAttribute()によりSubClassから必要な情報を取得する。
	 *
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mAttr = getAttribute(mAttr);
		if(mAttr == null)
			mAttr = new Attribute();
	}

	/**
	 * Service破棄時にコールされる<br>
	 * ここで、必要なクラス終了、破棄処理を行う
	 *
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

	/**
	 * StartServiceによるEventを処理。<br>
	 * 初回はOverlayWindowのView構築処理を行う
	 *
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
	/* #					[public]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * Windowサイズの取得<br>
	 * RootViewのWindowSizeを取得します
	 *
	 * @return Windowのサイズ
	 */
	public final Point getWindowSize(){
		return new Point(mRootView.getWidth(), mRootView.getHeight());
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

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
						mAttr.window_width,										// width
						mAttr.window_height,									// height
						mAttr.overlay_window_layer,								// type
						mAttr.overlay_window_flag,								// flag
						PixelFormat.TRANSLUCENT);								// format


		// Viewを構築
		Context c = getContextThemeWrapper();
		if (c == null)
			c = getApplicationContext();

		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootView = setupRootView(inflater, null);
		setupOnTouchListener();

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
	 * OverlayWindow のAttributeの取得
	 * <p>
	 * onCreateView内でViewの構築や初期化のため。それ以外のタイミングではnullになります。
	 * </p>
	 *
	 * @return onStartCommandのIntent
	 */
	protected final Attribute getWindowAttribute(){
		return mAttr;
	}
	// ____________________________________________________________
	/**
	 * OverlayWindow の表示位置をDefaultに戻す
	 * <p>
	 * 画面から消えちゃったりとかした時に呼んでください。
	 * </p>
	 *
	 */
	protected final void setDefaultPotition(){
		if(mRootView == null || mWindowManager == null)
			return;

		WindowManager.LayoutParams param = (WindowManager.LayoutParams) mRootView.getLayoutParams();
		param.y = 0;
		param.x = 0;
		mWindowManager.updateViewLayout(mRootView, param);
	}
	// ____________________________________________________________
	/**
	 * OverlayWindow のRootViewの取得
	 *
	 *
	 * @return RootView
	 */
	protected final View getRootView(){
		return mRootView;
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
	// ____________________________________________________________
	/**
	 * ContextThemeWrapper の取得
	 *
	 * @return Context(テーマ情報付き)
	 */
	protected abstract ContextThemeWrapper getContextThemeWrapper();

	/* ########################################################## */
	/* #														# */
	/* #					[Listener]							# */
	/* #														# */
	/* ########################################################## */
	private void setupOnTouchListener(){
		if(mAttr.enable_overlay_window_move){
			WindowMoveTouchListener touchListener = new WindowMoveTouchListener(mRootView, getApplicationContext());
			touchListener.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					onWindowTouchEvent(event);
					return true;
				}
			});
			mRootView.setOnTouchListener(touchListener);
		}else if(mAttr.enable_overlay_window_touch){
			// WindowTouchEventを設定
			mRootView.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					onWindowTouchEvent(event);
					return false;
				}
			});
		}
	}
}
