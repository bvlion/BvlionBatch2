package net.ambitious.bvlion.batch2.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.jcraft.jsch.JSchException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@WebListener
@RequiredArgsConstructor
public class ContextListener implements ServletContextListener {

	@NonNull
	private final AppParams appParams;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (appParams.isProduction()) {
			try (InputStream in = new URL(appParams.getRsaKeyPath()).openStream()) {
				Files.copy(in, Paths.get(SSHConnection.RSA_KEY_PATH), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				log.warn("RSA file download Error", e);
			}
			try (InputStream in = new URL(appParams.getKnownHostsPath()).openStream()) {
				Files.copy(in, Paths.get(SSHConnection.KNOWN_HOSTS_PATH), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				log.warn("KnownHosts file download Error", e);
			}

			SSHConnection ssh = SSHConnection.getInstance();
			ssh.setSshPassPhrase(appParams.getSshPassPhrase());
			ssh.setSshRemotePort(appParams.getSshRemotePort());
			ssh.setSshUser(appParams.getSshUser());
			ssh.setSshRemoteServer(appParams.getSshRemoteServer());
			ssh.setMysqlRemoteServer(appParams.getMysqlRemoteServer());

			try {
				ssh.connectSSH();
			} catch (JSchException e) {
				log.warn("SSHConnection Error", e);
			}
		}

		if (FirebaseApp.getApps().isEmpty()) {
			try {
				var options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials.fromStream(
								new URL(appParams.getAdminSdkJsonUrl()).openStream()
						))
						.setDatabaseUrl("https://" + appParams.getFirebaseId() + ".firebaseio.com/")
						.build();

				FirebaseApp.initializeApp(options);
			} catch (IOException e) {
				log.warn("Firebase initialize Error", e);
			}
		}

		try {
			new SlackHttpPost(
					"reminder",
					"BvlionBatch",
					"バッチが起動しました。",
					AccessUtil.SPRING_ICON
			).send(appParams);
			AccessUtil.postGoogleHome("バッチが起動しました。", log, appParams);
		} catch (IOException e) {
			log.warn("Slack Post Error", e);
		}

		log.info("Context Initialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (appParams.isProduction()) {
			SSHConnection.getInstance().closeSSH();
		}
		log.info("Context Destroyed");
	}
}
