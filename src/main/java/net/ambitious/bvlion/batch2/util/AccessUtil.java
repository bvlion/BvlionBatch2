package net.ambitious.bvlion.batch2.util;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jcraft.jsch.JSchException;
import net.ambitious.bvlion.batch2.entity.ExecTimeEntity;
import net.ambitious.bvlion.batch2.enums.ExecTimeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class AccessUtil {

	private static final String E2K_URL = "https://www.sljfaq.org/cgi/e2k.cgi?o=json&word=%s";

	static final String SPRING_ICON = "https://www.ambitious-i.net/img/article_main.png";

	public static final TimeZone TOKYO = TimeZone.getTimeZone("Asia/Tokyo");

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

	public static void postGoogleHome(String message, Logger log, AppParams appParams) {
		if (!appParams.isProduction()) {
			log.info(message + ":OK");
			return;
		}
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("notifier");
		ref.setValueAsync(message + " … " + FastDateFormat.getInstance("yyyyMMddHHmmss").format(Calendar.getInstance(AccessUtil.TOKYO)));
	}

	public static void accessGet(String accessUrl, Logger log, Class<?> clazz) {
		try {
			var ctx = SSLContext.getInstance("SSL");
			ctx.init(null, TM.toArray(new TrustManager[0]), new SecureRandom());

			var url = new URL(accessUrl);
			var conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setSSLSocketFactory(ctx.getSocketFactory());
			var responseCode = conn.getResponseCode();

			if (log != null) {
				log.info("GET " + accessUrl + " HTTP/1.1 -> Response Code : " + responseCode);
			}
		} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
			if (log == null) {
				log = LoggerFactory.getLogger(AccessUtil.class);
			}
			log.error(clazz.getName() + " Access Error", e);
		}
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

	public static String getNextDate(String format, int addDate) {
		var calendar = Calendar.getInstance(AccessUtil.TOKYO);
		calendar.add(Calendar.DATE, addDate);
		return DateTimeFormatter.ofPattern(format).format(
				LocalDateTime.ofInstant(calendar.toInstant(),
						ZoneId.of("Asia/Tokyo"))
		);
	}

	public static String getNextDate(String format) {
		return getNextDate(format, 1);
	}

	public static String getNextDate() {
		return getNextDate("yyyy/MM/dd");
	}

	public static void exceptionPost(String message, Logger log, Exception exception, AppParams appParams) {
		postGoogleHome(message, log, appParams);

		try {
			new SlackHttpPost(
					"server_api",
					"BvlionBatch2",
					message + "\\n\\n" + exception.getMessage(),
					AccessUtil.SPRING_ICON
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
		Calendar cal = Calendar.getInstance(AccessUtil.TOKYO);

		Calendar startTime = getTargetTime(ExecTimeEnum.FROM, execTimes);
		Calendar endTime = getTargetTime(ExecTimeEnum.TO, execTimes);

		return !isHoliday && cal.after(startTime) && cal.before(endTime);
	}
	private static Calendar getTargetTime(ExecTimeEnum execTimeEnum, List<ExecTimeEntity> execTimes) {
		Calendar cal = Calendar.getInstance(AccessUtil.TOKYO);
		return execTimes.stream()
				.filter(value -> value.getType() == execTimeEnum.getType())
				.map(value ->
						new GregorianCalendar(
								cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH),
								cal.get(Calendar.DATE),
								value.getHours(),
								value.getMinutes()
						)
				).findFirst().orElse(new GregorianCalendar());
	}

	public static void sendFcm(String message, AppParams appParams, Logger log) {
		try {
			var url = new URL("https://fcm.googleapis.com/v1/projects/" + appParams.getFirebaseId() + "/messages:send");
			var con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + getAccessToken(appParams));
			con.setRequestProperty("Content-Type", "application/json; UTF-8");
			con.setDoOutput(true);

			con.connect();

			try (var ps = new PrintStream(con.getOutputStream(), false, StandardCharsets.UTF_8)) {
				ps.print(message);
			}

			try (var br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				log.info(br.lines().collect(Collectors.joining("\n")));
			}

		} catch (IOException e) {
			log.error("Access Error", e);
		}
	}

	private static String getAccessToken(AppParams appParams) throws IOException {
		var googleCredential = GoogleCredential
				.fromStream(new URL(appParams.getAdminSdkJsonUrl()).openStream())
				.createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
		googleCredential.refreshToken();
		return googleCredential.getAccessToken();
	}

	public static String createTokenMessage(String token, String title, String body, String channelId) {
		try {
			var data = new JSONObject();
			data.put("body", body);
			data.put("title", title);
			data.put("channelId", channelId);
			var message = new JSONObject();
			message.put("token", token);
			message.put("data", data);
			message.put("android", new JSONObject().put("priority", "high"));
			var main = new JSONObject();
			main.put("message", message);
			return main.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String createTopicMessage(String title, String body, String channelId) {
		try {
			var data = new JSONObject();
			data.put("body", body);
			data.put("title", title);
			data.put("channelId", channelId);
			var message = new JSONObject();
			message.put("topic", "server_message");
			message.put("data", data);
			message.put("android", new JSONObject().put("priority", "high"));
			var main = new JSONObject();
			main.put("message", message);
			return main.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
