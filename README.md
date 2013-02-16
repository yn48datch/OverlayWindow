Welcome to the OverlayWindow wiki!

# OverlayWindow

## Summary

OverlayWindowはAndroid 用のライブラリです。
* Activityより上のレイヤーで任意のレイアウト（アプリケーション）を表示。
* 動かせる
* Activityを実装する感覚で、アプリケーションを実装できる
上記のコンセプトの元で、作成しています。

## How to setup
Welcome to the OverlayWindow wiki!

# OverlayWindow

## Summary

OverlayWindowはAndroid 用のライブラリです。
* Activityより上のレイヤーで任意のレイアウト（アプリケーション）を表示。
* 動かせる
* Activityを実装する感覚で、アプリケーションを実装できる
上記のコンセプトの元で、作成しています。

## How to setup

筆者がEclipseでの開発環境を構築しているため、
一部、Eclipseのみでしか当てはまらない記載があります。  

1. Android アプリケーション開発環境の構築  
   省略します。
   
2. Android アプリケーションプロジェクトの作成(あなたのアプリ)  
   省略します。
   
3. OverlayWindowをLibraryとして登録する(少しうろ覚えです。。)  
   Eclipse で、 インポート -> 既存プロジェクトをワークスペースへ  
   でOKだと思います。  
   
4. 使用したいアプリケーションのプロジェクトにOverlayWindow Libraryを追加  
   Eclipse で、 アプリケーションのプロジェクトを右クリック  
   プロパディ -> Android -> ライブラリ で、OverlayWindow を追加  


## How to use

1.OverlayWindowは以下のパーミッションが必要です。
AndroidManifestに以下を記載してください。  

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

2.OverlayWindow もしくは OverlayApplicationを継承したクラスを作成してください。  
  
3.onCreateView() をoverrideして(eclipseなら勝手にStub追加される)  
表示させたいViewを返却してください。  

    protected View onCreateView(LayoutInflater inflater, ViewGroup root) {  
        View view = inflater.inflate(R.layout.overlay_video, root);
        return view;
    }

4.OverlayWindow および OverlayApplicationは  
Serviceを継承したクラスです。利用する(継承した)クラスは
AndroidManifestにServiceとして、記載してください。  

	<service android:name=".HogeHogeOverlayWindowService"></service>

Activityから以下のように起動できます。  

	startService(new Intent(getApplicationContext(), HogeHogeOverlayWindowService.class));


## ライセンス
----------
Copyright &copy; 2013 Ny Project 
筆者がEclipseでの開発環境を構築しているため、
一部、Eclipseのみでしか当てはまらない記載があります。  

1. Android アプリケーション開発環境の構築  
   省略します。
   
2. Android アプリケーションプロジェクトの作成(あなたのアプリ)  
   省略します。
   
3. OverlayWindowをLibraryとして登録する(少しうろ覚えです。。)  
   Eclipse で、 インポート -> 既存プロジェクトをワークスペースへ  
   でOKだと思います。  
   
4. 使用したいアプリケーションのプロジェクトにOverlayWindow Libraryを追加  
   Eclipse で、 アプリケーションのプロジェクトを右クリック  
   プロパディ -> Android -> ライブラリ で、OverlayWindow を追加  


## How to use

1.OverlayWindowは以下のパーミッションが必要です。
AndroidManifestに以下を記載してください。  

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
2.OverlayWindow もしくは OverlayApplicationを継承したクラスを作成してください。  
3.onCreateView() をoverrideして(eclipseなら勝手にStub追加される)  
表示させたいViewを返却してください。  

    protected View onCreateView(LayoutInflater inflater, ViewGroup root) {  
        View view = inflater.inflate(R.layout.overlay_video, root);
        return view;
    }

4.OverlayWindow および OverlayApplicationは
Serviceを継承したクラスです。利用する(継承した)クラスは
AndroidManifestにServiceとして、記載してください。  

	<service android:name=".HogeHogeOverlayWindowService"></service>

Activityから以下のように起動できます。  

	startService(new Intent(getApplicationContext(), HogeHogeOverlayWindowService.class));


## ライセンス
----------
Copyright &copy; 2013 Ny Project 