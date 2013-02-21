package jp.co.nyuta.android.overlaywindow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * OverlayWindow起動用抽象クラス<br>
 * このクラスを継承して作ったクラスはActivityとなります。<br>
 * Activityで受け取ったIntentをそのままService起動に使い、何もなかったかのように終了します。<br>
 * AndroidManifestにActivityの登録が必要です。<br>
 * <br>
 *
 * @author Yuta
 *
 */
public abstract class OverlayCreationActivity extends Activity {

	/**
	 * 継承不可！ここで、OverlayWindowサービスをたちあげて、そのまま終わります。
	 *
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Ny_OverlayActivity);
		super.onCreate(savedInstanceState);
		startOverlayService(getIntent());
		finish();
	}

	/* ########################################################## */
	/* #														# */
	/* #					[protected]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * getOverlayWindowService()で取得したServiceクラスを開始します。<br>
	 * defaultで、Activityが受け取ったIntentをそのままServiceへ渡します。<br>
	 * 何か変えたい場合は<br>
	 * 1. このメソッドをOverride (いろいろ自分でやりたい人)<br>
	 * 2. getServiceCreationIntentをOverride (Intentだけ弄りたい人)<br>
	 *
	 * @param activity_intent Activityが受け取ったIntent
	 */
	protected void startOverlayService(Intent activity_intent){
		if(!judgeCanStartService() || activity_intent == null){
			return;
		}

		Intent service_intent = getServiceCreationIntent(activity_intent);
		Class<?> service = getOverlayWindowService();

		service_intent.setClassName(service.getPackage().getName(), service.getName());
		startService(service_intent);
	}
	// ____________________________________________________________
	/**
	 * getOverlayWindowService()で取得したServiceクラスを開始するためのIntentを取得します。<br>
	 * defaultで、Activityが受け取ったIntentをそのまま返却します。<br>
	 * startOverlayService()内で使用します
	 *
	 * @param activity_intent Activityが受け取ったIntent
	 * @return Serviceクラス開始用のIntent
	 */
	protected Intent getServiceCreationIntent(Intent activity_intent){
		return activity_intent;
	}

	// ____________________________________________________________
	/**
	 * OverlayWindowの起動を制限したい場合などの判定に使います<br>
	 * Overrideして判定をしてください。<br>
	 * defaultはTrueを返却してます<br>
	 * startOverlayService()内で使用します
	 *
	 * @return true : 起動していい <br>false : 起動しないで。
	 */
	protected boolean judgeCanStartService(){
		return true;
	}

	/* ########################################################## */
	/* #														# */
	/* #					[abstract]							# */
	/* #														# */
	/* ########################################################## */
	// ____________________________________________________________
	/**
	 * OverlayWindowServiceクラスの取得
	 *
	 * @return 起動したいOverlayWindowのクラス(どんなClassを渡されてもチェックしません！！)
	 */
	protected abstract Class<?> getOverlayWindowService();
}
