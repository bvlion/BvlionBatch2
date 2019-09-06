package net.ambitious.bvlion.batch2.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

@Log
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
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials.fromStream(new URL(appParams.getAdminSdkJsonUrl()).openStream()))
						.setDatabaseUrl("https://" + appParams.getFirebaseId() + ".firebaseio.com/")
						.build();

				FirebaseApp.initializeApp(options);
			} catch (IOException e) {
				log.log(Level.WARNING, "Firebase initialize Error", e);
			}
		}

		log.info("Context Initialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.info("Context Destroyed");
	}
}
