package net.ambitious.bvlion.batch2.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class SlackHttpPost {

	private String channel;
	private String username;
	private String text;
	private String icon_url;

	public SlackHttpPost(String channel, String username, String text, String icon_url) {
		this.channel = channel;
		this.username = username;
		this.text = text;
		this.icon_url = icon_url;
	}

	public void send(AppParams appParams) throws IOException {
		final String payload = "payload=" + URLEncoder.encode("{\""
				+ "channel\": \"#" + this.channel + "\","
				+ "\"as_user\": \"true\","
				+ "\"username\": \"" + this.username + "\","
				+ "\"text\": \"" + this.text + "\","
				+ "\"icon_url\": \"" + this.icon_url
				+ "\"}", StandardCharsets.UTF_8
		);

		if (!appParams.isProduction()) {
			log.info(payload);
			return;
		}
		URL url = new URL(appParams.getSlackWebhookUrl());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(true);

		try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
			wr.writeBytes(payload);
		}

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
			log.info(
					"Slack Post response is "
							+ br.lines().collect(Collectors.joining("\n")) + "\n"
							+ "user_name:" + this.username + "\n"
							+ "text:" + this.text
			);
		}
	}
}
