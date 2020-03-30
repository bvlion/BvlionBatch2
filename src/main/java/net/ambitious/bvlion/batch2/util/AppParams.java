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
	private String allowUserAgent;
	private String mp3format;
	private String iftttProxyToken;
	private String iftttYoutubeToken;

	private String sshUser;
	private String sshRemoteServer;
	private int sshRemotePort;
	private String sshPassPhrase;
	private String mysqlRemoteServer;
	private String rsaKeyPath;
	private String knownHostsPath;
	private String batchStartNotificationUrl;

	private String mailHost;
	private String mailUser;
   	private String mailPassword;

   	private String firebaseFunctionUrl;
}
