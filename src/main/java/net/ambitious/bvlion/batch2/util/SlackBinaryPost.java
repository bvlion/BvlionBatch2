package net.ambitious.bvlion.batch2.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class SlackBinaryPost {

	/** 改行コード */
	private static final String CRLF = "\r\n";
	/** バウンダリのヘッダ（ハイフン2つ） */
	private static final String BOUNDARY_HEADER = "--";
	/** ファイルアップロード先 */
	private static final String SLACK_POST_URL = "https://slack.com/api/files.upload";
	/** 読み込みタイムアウト値 */
	private static final int READ_TIMEOUT = 10 * 1000;
	/** 接続タイムアウト値 */
	private static final int CONNECTION_TIMEOUT = 10 * 1000;

	/** HttpURLConnection */
	private HttpURLConnection con;
	/** バウンダリの本体（ハイフン2つ以降） */
	private String boundaryBody;

	/** ファイル名 */
	private String fileName;
	/** 送信データ（byte配列） */
	private byte[] fileData;
	/** タイトル等を入れるマップ */
	private Map<String, String> textDataMap;

	/**
	 * コンストラクタ
	 * @param builder インナークラスBuilder
	 */
	private SlackBinaryPost(Builder builder) throws IOException {
		this.boundaryBody = "*****" + UUID.randomUUID().toString() + "*****";
		this.con = (HttpURLConnection) new URL(SLACK_POST_URL).openConnection();

		this.textDataMap = builder.textDataMap;
		this.fileData = builder.fileData;
		this.fileName = builder.fileName;

		createConnection();
	}

	/** HttpURLConnectionを生成する */
	private void createConnection() throws ProtocolException {
		this.con.setRequestMethod("POST");
		this.con.setDoOutput(true);
		this.con.setDoInput(true);
		this.con.setUseCaches(false);
		this.con.setReadTimeout(READ_TIMEOUT);
		this.con.setConnectTimeout(CONNECTION_TIMEOUT);
		this.con.setRequestProperty("Connection", "Keep-Alive");
		this.con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundaryBody);
	}

	/**
	 * 送信する
	 */
	public void post(AppParams appParams) {
		String data;
		try {
			write();
			data = read();
			var json = new JSONObject(data);
			if (json.getBoolean("ok")) {
				log.info("Slack Binary Post response is " + data);
			} else {
				log.warn("Slack Binary Post response is " + data + "\n" + this.fileName);

				new SlackHttpPost(
						"server_api",
						"BOT Twitter",
						"画像送信でエラーが発生しました。\\nファイル名 -> " + this.fileName,
						"http://4s.ambitious-i.net/icon/syobon.png"
				).send(appParams);
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		this.con.disconnect();
	}

	/** データの書き込みを行う */
	private void write() throws IOException {
		try (var request = new DataOutputStream(this.con.getOutputStream())) {
			request.writeBytes(BOUNDARY_HEADER + this.boundaryBody + CRLF);
			request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
					+ this.fileName + "\"" + CRLF);
			request.writeBytes(CRLF);
			request.write(this.fileData);
			request.writeBytes(CRLF);
			// テキストデータの設定
			for (var entry : this.textDataMap.entrySet()) {
				request.writeBytes(BOUNDARY_HEADER + this.boundaryBody + CRLF);
				request.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF);
				request.writeBytes("Content-Type: text/plain" + CRLF);
				request.writeBytes(CRLF);
				request.write(entry.getValue().getBytes(StandardCharsets.UTF_8.toString()));
				request.writeBytes(CRLF);
			}
			request.writeBytes(BOUNDARY_HEADER + this.boundaryBody + BOUNDARY_HEADER + CRLF);
		}
	}

	/**
	 * レスポンスを読み込む
	 * @return レスポンスボディ
	 */
	private String read() throws IOException {
		try (var br = new BufferedReader(
				new InputStreamReader(this.con.getInputStream(), StandardCharsets.UTF_8.toString()))) {
			return br.lines().collect(Collectors.joining("\n"));
		}
	}

	/** Builder */
	public static class Builder {

		/** token等のテキストデータ */
		Map<String, String> textDataMap = new HashMap<>();
		/** ファイルのバイトデータ */
		byte[] fileData;
		/** ファイル名 */
		String fileName;

		/**
		 * 送信するチャンネル名を設定する
		 * @param channels チャンネル
		 * @return Builder
		 */
		public Builder channels(String channels) {
			this.textDataMap.put("channels", channels);
			return this;
		}

		/**
		 * イメージのタイトルを設定する
		 * @param title イメージのタイトル
		 * @return Builder
		 */
		public Builder title(String title) {
			this.textDataMap.put("title", title);
			return this;
		}

		/**
		 * バイトデータを設定する
		 * @param fileData バイトデータ
		 * @return Builder
		 */
		public Builder fileData(byte[] fileData) {
			this.fileData = fileData;
			return this;
		}

		/**
		 * ファイル名を設定する
		 * @param fileName ファイル名
		 * @return Builder
		 */
		public Builder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		/**
		 * SlackBinaryPostのインスタンスを生成する
		 * @return SlackBinaryPostインスタンス
		 */
		public SlackBinaryPost build(String token) throws IOException {
			this.textDataMap.put("token", token);
			if (this.fileData == null || this.textDataMap.size() != 3) {
				throw new IllegalArgumentException("必要データが登録されていません。");
			}
			if (StringUtils.isBlank(this.fileName)) {
				this.fileName = FastDateFormat.getInstance("yyyyMMddHHmmss", AccessUtil.TOKYO).format(Calendar.getInstance(AccessUtil.TOKYO));
			}
			return new SlackBinaryPost(this);
		}
	}
}
