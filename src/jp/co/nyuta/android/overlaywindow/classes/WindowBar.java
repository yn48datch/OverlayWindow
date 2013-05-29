package jp.co.nyuta.android.overlaywindow.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import jp.co.nyuta.android.overlaywindow.R;

/**
 *
 * OverlayApplicationのWindowBarの処理クラス<br>
 * 各々のアプリケーション用に各種カスタマイズを入れれるように想定して作成してます。
 *
 * @author Yuta
 *
 */
public class WindowBar {

	private ResourceInformation			mResInformation = null;
	private ViewGroup 					mWindowBarLayout = null;
	private ViewGroup 					mWindowBarContainer = null;
	private OnWindowBarEvent			mUserOnWindowBarEvent = null;

	// ____________________________________________________________
	/**
	 *
	 * WindowBarのリソース管理用情報クラス<br>
	 *
	 * @author Yuta
	 *
	 */
	public static class ResourceInformation{
		/** LayoutリソースID:<br> WindowBar用のリソースID(ViewGroup) */
		public int layout_window_bar = R.layout.basic_windowbar;

		/** WidgetリソースID:<br> Window icon用のリソースID(ImageView) */
		public int id_window_icon = R.id.windowbar_appicon;
		/** WidgetリソースID:<br> Window title用のリソースID(TextView) */
		public int id_window_title = R.id.windowbar_title_textView;
		/** WidgetリソースID:<br> 最小化ボタン用のリソースID(ImageButton) */
		public int id_min_button = R.id.windowbar_hide_imageButton;
		/** WidgetリソースID:<br> 最大化ボタン用のリソースID(ImageButton) */
		public int id_max_button = R.id.windowbar_fit_display_imageButton;
		/** WidgetリソースID:<br> 終了ボタン用のリソースID(ImageButton) */
		public int id_del_button = R.id.windowbar_delete_imageButton;

		/** DrawableリソースID:<br> Window Icon(OverlayApplication 用を想定) */
		public int drawable_window_icon = 0;
		/** DrawableリソースID:<br> 最大化ボタン 最大化 */
		public int drawable_to_max = R.drawable.window_fit_display;
		/** DrawableリソースID:<br> 最大化ボタン 通常化 */
		public int drawable_to_normal = R.drawable.window_normal_display;

		// ____________________________________________________________
		/**
		 * Objectの複製を返却する
		 *
		 * @return 複製されたResourceInformationクラス
		 *
		 */
		protected ResourceInformation clone() {
			ResourceInformation info = new ResourceInformation();

			info.layout_window_bar = this.layout_window_bar;

			info.id_window_icon = this.id_window_icon;
			info.id_window_title = this.id_window_title;
			info.id_min_button = this.id_min_button;
			info.id_max_button = this.id_max_button;
			info.id_del_button = this.id_del_button;

			info.drawable_window_icon = this.drawable_window_icon;
			info.drawable_to_max = this.drawable_to_max;
			info.drawable_to_normal = this.drawable_to_normal;
			return info;
		}
	}

	// ____________________________________________________________
	/**
	 *
	 * WindowBarのEventListener<br>
	 *
	 * @author Yuta
	 *
	 */
	public interface OnWindowBarEvent{
		public void onMinButtonClicked();
		public void onMaxButtonClicked();
		public void onDelButtonClicked();
	}

	/* ########################################################## */
	/* #														# */
	/* #					[Constructor]						# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * WindowBarコンストラクタ<br>
	 * OverlayApplicationにて使用されることを想定してます。
	 *
	 * @param  inflater  LayoutInflater
	 * @param  container WindowBarの親のLayout
	 * @param  res_info  WindowBar構築用のレイアウト関連情報
	 * @param  attribute OverlayWindowのAttribute
	 *
	 */
	public WindowBar(LayoutInflater inflater, ViewGroup container, ResourceInformation res_info, Attribute attribute){
		// Layout設定
		setWindowBarLayout(inflater, res_info, container);

		if(!attribute.enable_maximization){
			ImageButton maxButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_max_button);
			maxButton.setVisibility(View.GONE);
		}
		if(!attribute.enable_minimization){
			ImageButton minButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_min_button);
			minButton.setVisibility(View.GONE);
		}
	}

	/* ########################################################## */
	/* #														# */
	/* #						[public]						# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * WindowBarレイアウト設定<br>
	 *
	 * @param  inflater  LayoutInflater
	 * @param  res_info  WindowBar構築用のレイアウト関連情報
	 *
	 */
	public void setWindowBarLayout(LayoutInflater inflater, ResourceInformation res_info){
		setWindowBarLayout(inflater, res_info, mWindowBarContainer);
	}

	// ____________________________________________________________
	/**
	 * WindowBar Title設定<br>
	 *
	 * @param  res_id  タイトル文字のリソースID
	 *
	 */
	public void setTitle(int res_id){
		TextView titleText = (TextView)mWindowBarLayout.findViewById(mResInformation.id_window_title);
		titleText.setText(res_id);
	}
	// ____________________________________________________________
	/**
	 * WindowBar Title設定<br>
	 *
	 * @param  title  タイトル文字列
	 *
	 */
	public void setTitle(CharSequence title){
		TextView titleText = (TextView)mWindowBarLayout.findViewById(mResInformation.id_window_title);
		titleText.setText(title);
	}

	// ____________________________________________________________
	/**
	 * WindowBar icon設定<br>
	 *
	 * @param  res_id  iconリソースID
	 *
	 */
	public void setIcon(int res_id){
		ImageView icon = (ImageView)mWindowBarLayout.findViewById(mResInformation.id_window_icon);
		icon.setImageResource(res_id);
	}


	// ____________________________________________________________
	/**
	 * WindowBarの高さを取得 (表示領域をUser側で確認するため)
	 *
	 * @return WindowBarのHeight
	 *
	 */
	public int getHeight(){
		if(isShown())
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
	public int getWidth(){
		if(isShown())
			return mWindowBarLayout.getWidth();
		return 0;
	}
	// ____________________________________________________________
	/**
	 * WindowBarの表示
	 *
	 */
	public void show(){
		mWindowBarLayout.setVisibility(View.VISIBLE);
	}
	// ____________________________________________________________
	/**
	 * WindowBarの非表示
	 *
	 */
	public void hide(){
		mWindowBarLayout.setVisibility(View.GONE);
	}

	// ____________________________________________________________
	/**
	 * WindowBarが表示中か？の確認
	 *
	 * @return true : 表示中 false : 非表示中
	 *
	 */
	public boolean isShown(){
		if(mWindowBarLayout.getVisibility() == View.GONE){
			return false;
		}
		return true;
	}
	// ____________________________________________________________
	/**
	 * WindowBarのTitle取得
	 *
	 * @return Title文字列
	 *
	 */
	public CharSequence getTitle(){
		TextView titleText = (TextView)mWindowBarLayout.findViewById(mResInformation.id_window_title);
		return titleText.getText();
	}


	// ____________________________________________________________
	/**
	 * WindowBar へ最大化・通常化の状態変更を通知する<br>
	 * OverlayApplication基底クラス向けのメソッド
	 *
	 * @param  isMaxStatus 最大化状態かどうか
	 *
	 */
	public void noticeChangeApplicationStatus(boolean isMaxStatus){
		ImageButton maxButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_max_button);
		int res;
		if(isMaxStatus){
			res = mResInformation.drawable_to_normal;
		}else{
			res = mResInformation.drawable_to_max;
		}
		maxButton.setImageResource(res);
	}
	// ____________________________________________________________
	/**
	 * WindowBar のEvent設定<br>
	 * OverlayApplication基底クラス向けのメソッド
	 *
	 * @param  eventListener Listener
	 *
	 */
	public void setOnWindowBarEvent(OnWindowBarEvent eventListener){
		mUserOnWindowBarEvent = eventListener;
	}

	/* ########################################################## */
	/* #														# */
	/* #						[private]						# */
	/* #														# */
	/* ########################################################## */
	private void setWindowBarLayout(LayoutInflater inflater, ResourceInformation res_info, ViewGroup container){
		mWindowBarContainer = container;
		mResInformation = res_info;

		// Viewの取得
		mWindowBarLayout = (ViewGroup)inflater.inflate(res_info.layout_window_bar, container);

		// Viewの設定
		ImageButton maxButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_max_button);
		ImageButton minButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_min_button);
		ImageButton delButton = (ImageButton)mWindowBarLayout.findViewById(mResInformation.id_del_button);

		maxButton.setOnClickListener(mWindowClickListener);
		minButton.setOnClickListener(mWindowClickListener);
		delButton.setOnClickListener(mWindowClickListener);

	}
	/* ########################################################## */
	/* #														# */
	/* #					[Listener]							# */
	/* #														# */
	/* ########################################################## */
	private OnClickListener mWindowClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mUserOnWindowBarEvent == null)
				return;

			int id = v.getId();
			if(id == mResInformation.id_del_button){
				mUserOnWindowBarEvent.onDelButtonClicked();
			}else if(id == mResInformation.id_max_button){
				mUserOnWindowBarEvent.onMaxButtonClicked();
			}else if(id == mResInformation.id_min_button){
				mUserOnWindowBarEvent.onMinButtonClicked();
			}

		}

	};
}
