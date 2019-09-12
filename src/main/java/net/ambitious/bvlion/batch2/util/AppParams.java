package net.ambitious.bvlion.batch2.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "appdata")
public class AppParams {
	private String adminSdkJsonUrl;
	private String firebaseId;
	private String slackWebhookUrl;
	private boolean production;
	private String slackToken;
	private String googleCalendarKey;
	private String googleHomeNotifierUrl;
}
