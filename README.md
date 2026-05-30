# FutureOS Aurora

Androidを未来OS風に見せるためのMVP APKです。

## 現在入っている機能

- 星空、地平線、縦の光のカーテンを使った本物寄りのオーロラライブ壁紙
- ライブ壁紙サービス。壁紙中はHUDカードを表示しません
- NotificationListenerServiceによる通知アクセス土台
- MediaSessionManagerによる再生中メディア検出
- Audio Visualizerによる低音・高音反応の土台
- 音楽再生時に演出強化、動画アプリ検出時にシネマ寄りへ切り替え
- 省電力を意識した描画レート制御
- アプリ内で通知アクセス、音声解析、オーバーレイ権限のON/OFFを表示

## APKビルド

```powershell
.\gradlew.bat assembleDebug
```

生成APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 端末へのインストール

Android SDKの `adb` がPATHにない場合:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
```

## 権限

アプリ内のボタンから以下を有効化してください。

- 通知へのアクセス
- ライブ壁紙設定
- マイク/音声解析権限
- オーバーレイ権限

通常APKではAndroid本体の通知センターやシステムUIを直接書き換えることはできないため、このMVPではライブ壁紙とアプリ内/将来のオーバーレイ演出で未来OS体験を作ります。
