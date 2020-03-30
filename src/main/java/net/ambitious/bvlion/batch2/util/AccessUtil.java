package net.ambitious.bvlion.batch2.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.ExecTimeEntity;
import net.ambitious.bvlion.batch2.enums.ExecTimeEnum;
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
import java.time.LocalDateTime;
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

	private static final List<TrustManager> TM = Collections.unmodifiableList(Collections.singletonList(
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] xc, String type) {
				}

				public void checkServerTrusted(X509Certificate[] xc, String type) {
				}
			}
	));

	public static void postGoogleHome(String message, Logger log, AppParams appParams, int volume) {
		if (!appParams.isProduction()) {
			log.info(String.format("{message: \"%s\", volume: %s}", message, volume));
			return;
		}
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("notifier");
		ref.setValueAsync(String.format("%s … %s … %s", message, getYmdhms(), volume));
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
			return String.format(formatWord.toString(), jaWord.toString());
		} catch (JSONException e) {
			log.error("Convert Error", e);
			return enWord;
		}
	}

	public static String getNow(String format) {
		return DateTimeFormatter.ofPattern(format).format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo")));
	}

	public static String getNextDate(String format) {
		var calendar = Calendar.getInstance(AccessUtil.TOKYO);
		calendar.add(Calendar.DATE, 1);
		return DateTimeFormatter.ofPattern(format).format(
				LocalDateTime.ofInstant(calendar.toInstant(),
						ZoneId.of("Asia/Tokyo"))
		);
	}

	static void exceptionPost(String message, Logger log, Exception exception, AppParams appParams) {
		postGoogleHome(message, log, appParams, 20);

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

	public static boolean isExecTime(boolean isHoliday, List<ExecTimeEntity> execTimes) {
		if (isHoliday) {
			return false;
		}

		Calendar cal = Calendar.getInstance(AccessUtil.TOKYO);

		Calendar start1Time = getTargetTime(ExecTimeEnum.FROM1, execTimes);
		Calendar end1Time = getTargetTime(ExecTimeEnum.TO1, execTimes);

		if (cal.after(start1Time) && cal.before(end1Time)) {
			return true;
		}

		Calendar start5Time = getTargetTime(ExecTimeEnum.FROM5, execTimes);
		Calendar end5Time = getTargetTime(ExecTimeEnum.TO5, execTimes);

		return cal.after(start5Time) && cal.before(end5Time) && cal.get(Calendar.MINUTE) % 5 == 0;
	}

	private static Calendar getTargetTime(ExecTimeEnum execTimeEnum, List<ExecTimeEntity> execTimes) {
		Calendar cal = Calendar.getInstance(AccessUtil.TOKYO);
		return execTimes.stream()
				.filter(value -> value.getType() == execTimeEnum.getType())
				.map(value -> {
					cal.set(Calendar.HOUR_OF_DAY, value.getHours());
					cal.set(Calendar.MINUTE, value.getMinutes());
					return cal;
				}
				).findFirst().orElse(cal);
	}

	public static void sendTokenMessage(
			List<String> tos, String title, String body, String userName,
			String channelId, String url) {
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
			if (error != null) {
				try {
					log.info("Send token response is " + touchFirebaseFunctions(url + "token"));
				} catch (IOException e) {
					log.warn("Send Topic Error", e);
				}
			}
		});
	}

	public static void sendTopicMessage(String title, String body, String channelId, String url) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("fcm/topic");
		Map<String, String> data = new HashMap<>();
		data.put("title", title);
		data.put("body", body);
		data.put("channelId", channelId);
		data.put("date", getYmdhms());
		ref.setValue(data, (error, reference) -> {
			if (error != null) {
				try {
					log.info("Send Topic response is " + touchFirebaseFunctions(url + "topic"));
				} catch (IOException e) {
				    log.warn("Send Topic Error", e);
				}
			}
		});
	}

	private static String touchFirebaseFunctions(String url) throws IOException {
		var uri = new URL(url);
		var con = (HttpURLConnection) uri.openConnection();
		con.setRequestMethod("POST");
		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(true);
		con.connect();

		try (var br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
			return br.lines().collect(Collectors.joining("\n"));
		}
	}
}
