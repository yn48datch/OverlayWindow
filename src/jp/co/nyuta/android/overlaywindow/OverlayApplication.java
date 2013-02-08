/**
 *
 */
package jp.co.nyuta.android.overlaywindow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * @author Yuta
 *
 */
public abstract class OverlayApplication extends OverlayWindow {

	private ViewGroup mWindowBarLayout = null;
	private TextView  mWindowTitleTextView = null;

	/* (非 Javadoc)
	 * @see jp.co.nyuta.android.overlaywindow.OverlayWindow#setupRootView(android.view.LayoutInflater, android.view.ViewGroup)
	 */
	@Override
	protected View setupRootView(LayoutInflater inflater, ViewGroup root) {
		View tobeRoot = inflater.inflate(R.layout.basic_window, root, true);
		mWindowBarLayout = (ViewGroup) tobeRoot.findViewById(R.id.windowbar_layout);
		mWindowTitleTextView = (TextView) tobeRoot.findViewById(R.id.windowbar_title_textView);
		setTitle(getThisClass().getSimpleName());
		ImageButton del = (ImageButton) tobeRoot.findViewById(R.id.windowbar_delete_imageButton);
		del.setOnClickListener(mWindowClickListener);

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

	private OnClickListener mWindowClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.windowbar_delete_imageButton){
				onPreSelfDelete();
			}

		}

	};

}
