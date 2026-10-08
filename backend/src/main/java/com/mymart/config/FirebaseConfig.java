package com.mymart.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.enabled:true}")
    private boolean enabled;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @Value("${firebase.project-id:mymart-app}")
    private String projectId;

    private boolean initialized = false;

    @PostConstruct
    public void initializeFirebase() {
        if (!enabled) {
            log.info("Firebase integration is explicitly disabled via configuration.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized.");
            initialized = true;
            return;
        }

        try {
            InputStream serviceAccountStream = null;

            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                log.info("Initializing Firebase using credentials JSON from environment variable");
                serviceAccountStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
            } else if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                log.info("Initializing Firebase using credentials file from: {}", credentialsPath);
                serviceAccountStream = new FileInputStream(credentialsPath);
            }

            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("Firebase Admin SDK successfully initialized for project: {}", projectId);
            } else {
                log.warn("No Firebase credentials provided (FIREBASE_CREDENTIALS_JSON or FIREBASE_CREDENTIALS_PATH). " +
                        "Firebase token verification will fallback to local JWT / mock verification for development.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}. Running in fallback dev authentication mode.", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized && !FirebaseApp.getApps().isEmpty();
    }
}
