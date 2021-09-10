package net.ambitious.bvlion.batch2.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.mapper.RealtimeSettingMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AccessUtil {

	private static final String E2K_URL = "https://www.sljfaq.org/cgi/e2k.cgi?o=json&word=%s";

	private static final String SPRING_ICON = "https://www.ambitious-i.net/img/article_main.png";

	public static final TimeZone TOKYO = TimeZone.getTimeZone("Asia/Tokyo");

	public static String getYmdhms() {
		return FastDateFormat.getInstance("yyyyMMddHHmmss", AccessUtil.TOKYO)
				.format(Calendar.getInstance(AccessUtil.TOKYO));
	}

	private static final List<TrustManager> TM = Collections.singletonList(
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] xc, String type) {
				}

				public void checkServerTrusted(X509Certificate[] xc, String type) {
				}
			}
	);

	public static void postGoogleHome(String message, Logger log, AppParams appParams, int volume) {
		postGoogleHome(message, log, appParams, volume, false);
	}

	public static void postGoogleHome(String message, Logger log, AppParams appParams, int volume, boolean study) {
		if (!appParams.isProduction()) {
			log.info(String.format("{message: \"%s\", volume: %s}", message, volume));
			return;
		}
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("notifier");
		ref.setValueAsync(String.format("%s … %s … %s … %s", message, getYmdhms(), volume, study));
    }

	public static String convertEn2Ja(String enWord, Logger log) {
		if (StringUtils.isBlank(enWord)) {
			return "";
		}

		var formatWord = new StringBuilder();
		var isHan = false;
		var isBeforeZen = true;
		var notHasHan = true;

		var chars = enWord.toCharArray();
		for (var enWordChar : chars) {
			if (String.valueOf(enWordChar).equals(" ") || String.valueOf(enWordChar).equals("　")) {
				formatWord.append(" ");
				isHan = false;
				continue;
			}

			if (String.valueOf(enWordChar).getBytes(StandardCharsets.UTF_8).length < 2) {
				isHan = true;
				notHasHan = false;
			} else {
				isBeforeZen = true;
				formatWord.append(enWordChar);
			}

			if (isHan && isBeforeZen) {
				isBeforeZen = false;
				isHan = false;
				formatWord.append("%s");
			}
		}

		if (notHasHan) {
			return formatWord.toString();
		}

		var accessUrl = String.format(E2K_URL, URLEncoder.encode(enWord, StandardCharsets.UTF_8));

		var response = new StringBuilder();
		try {
			var ctx = SSLContext.getInstance("SSL");
			ctx.init(null, TM.toArray(new TrustManager[0]), new SecureRandom());

			var url = new URL(accessUrl);
			var conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setSSLSocketFactory(ctx.getSocketFactory());

			try (var br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				response.append(br.lines().collect(Collectors.joining()));
			}

			log.info("GET " + accessUrl + " HTTP/1.1 -> Response Code : " + conn.getResponseCode());
		} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
			log.error(accessUrl + " Access Error", e);
			return enWord;
		}

		try {
			var jsonWords = new JSONObject(response.toString()).getJSONArray("words");

			var jaWord = new StringBuilder();
			for (var i = 0; i < jsonWords.length(); i++) {
				var jsonWord = jsonWords.getJSONObject(i);
				if (!StringUtils.isBlank(jaWord)) {
					jaWord.append(" ");
				}
				jaWord.append(jsonWord.getString("j_pron_spell"));
			}
			return String.format(formatWord.toString(), jaWord);
		} catch (JSONException e) {
			log.error("Convert Error", e);
			return enWord;
		}
	}

	public static String getNow(String format) {
		return DateTimeFormatter.ofPattern(format).format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo")));
	}

	static void exceptionPost(String message, Logger log, Exception exception, AppParams appParams) {
		postGoogleHome(message, log, appParams, 20, true);

		try {
			new SlackHttpPost(
					"server_api",
					"BvlionBatch2",
					message + "\\n\\n" + exception.getMessage(),
					SPRING_ICON
			).send(appParams);
		} catch (IOException e) {
			log.error("Slack Post Error", e);
		}

		log.error(message, exception);

		if (appParams.isProduction()) {
			try {
				SSHConnection.getInstance().reConnectSSH();
				log.info("SSH ReConnect");
			} catch (JSchException e) {
				log.error("SSH ReConnect Error", e);
			}
		}
	}

	public static byte[] getBinaryBytes(String binaryUrl) throws IOException {
		var url = new URL(binaryUrl);
		var con = (HttpURLConnection) url.openConnection();

		var bout = new ByteArrayOutputStream();
		try (var is = con.getInputStream()) {
			var data = new byte[1024];
			int len;
			while ((len = is.read(data, 0, 1024)) != -1) {
				bout.write(data, 0, len);
			}
		}
		return bout.toByteArray();
	}

	public static void sendTokenMessage(
			List<String> tos, String title, String body, String userName,
			String channelId, String url, String basic) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("fcm/token");
		Map<String, String> data = new HashMap<>();
		data.put("title", title);
		data.put("body", body);
		data.put("channelId", channelId);
		data.put("userName", userName);
		data.put("date", getYmdhms());
		data.put("to", String.join(",", tos));
		ref.setValue(data, (error, reference) -> {
			if (error == null) {
				try {
					log.info("Send token response is " + touchFirebaseFunctions(url + "token", basic));
				} catch (IOException e) {
					log.warn("Send Topic Error", e);
				}
			}
		});
	}

	public static void sendTopicMessage(String title, String body, String channelId, String url, String basic) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("fcm/topic");
		Map<String, String> data = new HashMap<>();
		data.put("title", title);
		data.put("body", body);
		data.put("channelId", channelId);
		data.put("date", getYmdhms());
		ref.setValue(data, (error, reference) -> {
			if (error == null) {
				try {
					log.info("Send Topic response is " + touchFirebaseFunctions(url + "topic", basic));
				} catch (IOException e) {
				    log.warn("Send Topic Error", e);
				}
			}
		});
	}

	private static String touchFirebaseFunctions(String url, String basic) throws IOException {
		var uri = new URL(url);
		var con = (HttpURLConnection) uri.openConnection();
		con.setRequestProperty("Authorization", "Basic " +
				Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
		con.setUseCaches(false);

		try (var br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
			return br.lines().collect(Collectors.joining("\n"));
		}
	}

	public static void airconRemoPost(
			int mode,
			int temp,
			AppParams appParams,
			RealtimeSettingMapper realtimeSettingMapper
	) {
		if (StringUtils.isEmpty(appParams.getRemoAirconUrl())) {
			return;
		}

		String message;
		String parameter = null;
		var param = new StringBuilder("temperature=");
		param.append(temp);
		param.append("&air_volume=auto&operation_mode=");
		switch (mode) {
			case 0: // 停止
				message = "エアコンを停止させました。";
				parameter = "button=power-off";
				break;
			case 1: // 冷房
				message = String.format("冷房を%s度で起動させました。", temp);
				param.append("cool");
				break;
			case 2: // 除湿
				message = String.format("除湿を%s度で起動させました。", temp);
				param.append("dry");
				break;
			case 3: // 暖房
				message = String.format("暖房を%s度で起動させました。", temp);
				param.append("warm");
				break;
			default:
				return;
		}
		if (StringUtils.isEmpty(parameter)) {
			parameter = param.toString();
		}

		try {
			var url = new URL(appParams.getRemoAirconUrl());
			var con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);

			con.setRequestProperty("accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + appParams.getRemoToken());

			try (var wr = new DataOutputStream(con.getOutputStream())) {
				wr.writeBytes(parameter);
			}

			try (var br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				log.info("aircon exec response is\n" + br.lines().collect(Collectors.joining("\n")));
			}
		} catch (Exception e) {
			log.warn("aircon error", e);
		}

		realtimeSettingMapper.updateAirconMode(mode, temp);
		sendTopicMessage("エアコン起動情報", message, "aircon",
				appParams.getFirebaseFunctionUrl(), appParams.getFirebaseBasicAuth());
	}
}