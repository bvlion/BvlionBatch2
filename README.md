# 機能

## IFTTT

* 今日は平日
* 明日は平日
* 今日は祝日
* 明日は祝日
* 眠りの音楽
* 音声テスト
    * Google Home で取得した音声をテキストにして Slack へ送信
* 登録した音楽の再生

### 以下は別サーバーで起動する

* 不快指数確認
* 秋葉原行きのバス
* 葛西行きのバス
* 生活品登録
* 百均登録
* 食品登録
* コープさん登録

### 以下はGAS

* 女の子の日が終わった
* 女の子の日が来た
* SPにログ保存
* 麻衣の体温記録

### 音声レスポンスを返すだけ

* 食洗機確認
* 保育園の電話番号

### Firebase Realtime Database

* 壁扇風機
* 加湿器消して
* 加湿器つけて
* コンポの電源
* CD起動
* テレビ チャンネル変更
* テレビ
* 掃除終了
* 掃除開始
* リズム風
* 扇風機の風量
* 扇風機の電源
* エアコン停止
* 除湿起動
* 暖房起動
* 冷房起動
* 部屋の電気（複数回）
* 部屋の電気

## Android （個人アプリ）連携

* Google Home に喋らせる
* Webhook で Alarm を鳴らす
* 記念日通知
* 現在の各種状態（エアコンとか）を返す
* FCM token 登録
* 在宅状況更新
* エアコン動作通知

## Slack 連携

* youtube-dl
    * <曲名><URL> で POST すると別サーバーで mp3 に変換して登録する。
* 占い通知
* ジョルダン遅延情報（ Twitter ）
* Twitter の 画像だけを投稿

## 諸々連携

* Slack 代理通知
    * 特定チャンネルへの投稿を Webhook で Spread Sheet へ登録し、登録をトリガーに Android へ FCM で通知する。
* メールを条件ごとにフォルダへ移動、 DB の情報に応じて Slack へ POST 。
* 動体検知通知
    * motion の `on_event_start` を利用して Slack と Android へ通知
* Firebase Realtime Database へデータ登録（赤外線用）

## Google Home

* 休日から平日、または平日から休日になるタイミングを返す。
* 生協さんの空箱を翌日出す
* 検温通知
* 通知の対象時間か否かを返す

# 開発

## Build

[![CircleCI](https://circleci.com/gh/bvlion/BvlionBatch2.svg?style=svg)](https://circleci.com/gh/bvlion/BvlionBatch2)

## Java11

```
brew tap homebrew/cask-versions
brew cask install java11
```

.bashrc
```
export JAVA_HOME=`/usr/libexec/java_home -v 11`
PATH=${JAVA_HOME}/bin:${PATH}
```

## 環境変数系のローカル準備

* application.propertiesの値はConfig Varsから値を確認し、export key=valueする
* twitter4j
    * Dropboxにあるのでsrc/main/resourcesに置く
* 初期データ
    * Dropboxにあるので実行
