package net.ambitious.bvlion.batch2.util;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.codehaus.jettison.json.JSONArray;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class AccessUtil {

	public static final String HOST = "http://localhost:9005";

	private static final String E2K_URL = "https://www.sljfaq.org/cgi/e2k.cgi?o=json&word=%s";

	public static final String SPRING_ICON = "https://www.ambitious-i.net/img/article_main.png";

	public static final List<TrustManager> TM = Collections.unmodifiableList(Collections.singletonList(
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

	public static void postGoogleHome(String message, Logger log, Class<?> clazz, AppParams appParams) {
		if (!appParams.isProduction()) {
			log.info(message + ":OK");
			return;
		}
		try {
			URL url = new URL("http://localhost:8091/google-home-notifier");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);

			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				byte[] bytes = ("text=" + message).getBytes(StandardCharsets.UTF_8);
				for (byte textByte : bytes) {
					wr.writeByte(textByte);
				}
			}

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				log.info(br.lines().collect(Collectors.joining("\n")));
			}
		} catch (IOException e) {
			log.error(clazz.getName() + " Access Error", e);
		}
	}

	public static void accessGet(String accessUrl, Logger log, Class<?> clazz) {
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, TM.toArray(new TrustManager[0]), new SecureRandom());

			URL url = new URL(accessUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setSSLSocketFactory(ctx.getSocketFactory());
			int responseCode = conn.getResponseCode();

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

	public static void accessPut(String url, Logger log, Class<?> clazz) {
		Request request = Request.Put(HOST.concat(url));
		Response res = null;
		try {
			res = request.execute();
			log.info(request + " ->\n" + new String(res.returnContent().asBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			log.error(clazz.getName() + " Access Error", e);
		} finally {
			if (res != null) {
				res.discardContent();
			}
		}
	}

	public static String convertEn2Ja(String enWord, Logger log) {
		if (StringUtils.isBlank(enWord)) {
			return "";
		}

		StringBuilder formatWord = new StringBuilder();
		boolean isHan = false;
		boolean isBeforeZen = true;
		boolean notHasHan = true;

		char[] chars = enWord.toCharArray();
		for (char enWordChar : chars) {
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

		String accessUrl;
		accessUrl = String.format(E2K_URL, URLEncoder.encode(enWord, StandardCharsets.UTF_8));

		StringBuilder response = new StringBuilder();
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, TM.toArray(new TrustManager[0]), new SecureRandom());

			URL url = new URL(accessUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setSSLSocketFactory(ctx.getSocketFactory());

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				response.append(br.lines().collect(Collectors.joining()));
			}

			log.info("GET " + accessUrl + " HTTP/1.1 -> Response Code : " + conn.getResponseCode());
		} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
			log.error(accessUrl + " Access Error", e);
			return enWord;
		}

		try {
			JSONArray jsonWords = new JSONObject(response.toString()).getJSONArray("words");

			StringBuilder jaWord = new StringBuilder();
			for (int i = 0; i < jsonWords.length(); i++) {
				JSONObject jsonWord = jsonWords.getJSONObject(i);
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
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
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

	public static void exceptionPost(String message, Logger log, Class<?> clazz,
									 Exception exception, AppParams appParams) {
		postGoogleHome(message, log, clazz, appParams);

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
	}

	public static byte[] getBinaryBytes(String binaryUrl) throws IOException {
		URL url = new URL(binaryUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (InputStream is = con.getInputStream()) {
			byte[] data = new byte[1024];
			int len;
			while ((len = is.read(data, 0, 1024)) != -1) {
				bout.write(data, 0, len);
			}
		}
		return bout.toByteArray();
	}

	/**
	 * 実行する所定の時間かを返す
	 * @param appParams AppParams
	 * @return 所定の時間である
	 * @throws IOException ファイル読み込みミス
	 */
	public static boolean isExecTime(AppParams appParams, boolean isHoliday) throws IOException {
		// TODO DBに変更
//		List<String> lines = Files.readAllLines(Paths.get(appParams.getExecTimeFilePath()), StandardCharsets.UTF_8);
//		String[] startTimes = lines.get(0).split(":");
//		String[] endTimes = lines.get(1).split(":");
//
//		Calendar cal = Calendar.getInstance();
//
//		Calendar startTime = new GregorianCalendar(
//				cal.get(Calendar.YEAR),
//				cal.get(Calendar.MONTH),
//				cal.get(Calendar.DATE),
//				NumberUtils.toInt(startTimes[0]),
//				NumberUtils.toInt(startTimes[1]));
//		Calendar endTime = new GregorianCalendar(
//				cal.get(Calendar.YEAR),
//				cal.get(Calendar.MONTH),
//				cal.get(Calendar.DATE),
//				NumberUtils.toInt(endTimes[0]),
//				NumberUtils.toInt(endTimes[1]));
//
//		return !isHoliday && cal.after(startTime) && cal.before(endTime);
		return true;
	}

	public static void sendFcm(String message, AppParams appParams, Logger log) {
		try {
			URL url = new URL("https://fcm.googleapis.com/v1/projects/" + appParams.getFirebaseId() + "/messages:send");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + getAccessToken(appParams));
			con.setRequestProperty("Content-Type", "application/json; UTF-8");
			con.setDoOutput(true);

			con.connect();

			try (PrintStream ps = new PrintStream(con.getOutputStream(), false, StandardCharsets.UTF_8)) {
				ps.print(message);
			}

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				log.info(br.lines().collect(Collectors.joining("\n")));
			}

		} catch (IOException e) {
			log.error("Access Error", e);
		}
	}

	private static String getAccessToken(AppParams appParams) throws IOException {
		GoogleCredential googleCredential = GoogleCredential
				.fromStream(new URL(appParams.getAdminSdkJsonUrl()).openStream())
				.createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
		googleCredential.refreshToken();
		return googleCredential.getAccessToken();
	}

	public static String createTokenMessage(String token, String title, String body, String channelId) {
		try {
			JSONObject data = new JSONObject();
			data.put("body", body);
			data.put("title", title);
			data.put("channelId", channelId);
			JSONObject message = new JSONObject();
			message.put("token", token);
			message.put("data", data);
			message.put("android", new JSONObject().put("priority", "high"));
			JSONObject main = new JSONObject();
			main.put("message", message);
			return main.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String createTopicMessage(String title, String body, String channelId) {
		try {
			JSONObject data = new JSONObject();
			data.put("body", body);
			data.put("title", title);
			data.put("channelId", channelId);
			JSONObject message = new JSONObject();
			message.put("topic", "server_message");
			message.put("data", data);
			message.put("android", new JSONObject().put("priority", "high"));
			JSONObject main = new JSONObject();
			main.put("message", message);
			return main.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
