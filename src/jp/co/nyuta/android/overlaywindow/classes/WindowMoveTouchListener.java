package jp.co.nyuta.android.overlaywindow.classes;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
/**
 *
 * OverlayWindow（他のActivityより上のLayerで表示・常駐する）用タッチイベント<br>
 * 基本的にOverlayWindow/OverlayApplicationで使用することを想定したライブラリ内部クラスです。
 *
 * @author Yuta
 *
 */
public class WindowMoveTouchListener implements OnTouchListener {
	private View mRootView;
	private WindowManager mWindowManager = null;
	private int mPreMoveX = -1;
	private int mPreMoveY = -1;
	private int mPreFirstPointX = -1;
	private int mPreFirstPointY = -1;
	private boolean mIsMoving = false;
	private OnTouchListener mUserOnTouchListener = null;
	private int MOVE_THRESHOLD = 0;				// 移動閾値
	private int MOVE_JUDGE_THRESHOLD = 8;		// 移動判定閾値

	public WindowMoveTouchListener(View targetWindowView, Context context){
		mRootView = targetWindowView;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public void setOnTouchListener(OnTouchListener l){
		mUserOnTouchListener = l;
	}

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
		if(!mIsMoving && isUserEventExpire && mUserOnTouchListener != null)
			mUserOnTouchListener.onTouch(mRootView, event);

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
}
