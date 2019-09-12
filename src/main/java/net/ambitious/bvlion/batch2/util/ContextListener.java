package net.ambitious.bvlion.batch2.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;

@Slf4j
@Component
@WebListener
@RequiredArgsConstructor
public class ContextListener implements ServletContextListener {

	@NonNull
	private final AppParams appParams;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (FirebaseApp.getApps().isEmpty()) {
			try {
				var options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials.fromStream(new URL(appParams.getAdminSdkJsonUrl()).openStream()))
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
			AccessUtil.postGoogleHome("バッチが起動しました。", log, ContextListener.class, appParams);
		} catch (IOException e) {
			log.warn("Slack Post Error", e);
		}

		log.info("Context Initialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.info("Context Destroyed");
	}
}
