package com.alocanote.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.json:}")
    private String credentialsJson;

    @Value("${firebase.project-id:}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = resolveCredentialsStream()) {
            if (serviceAccount == null) {
                System.out.println("ℹ️ Credenciais do Firebase Admin não encontradas (serviceAccountKey.json / FIREBASE_CREDENTIALS_PATH). Operando em modo de desenvolvimento local com validação flexível de token Firebase.");
                return;
            }

            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount));

            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId.trim());
            }

            FirebaseApp.initializeApp(builder.build());
            System.out.println("✅ Firebase Admin SDK inicializado com sucesso.");
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível inicializar o Firebase Admin SDK com credenciais de serviço (" + e.getMessage() + "). Operando em modo flexível de desenvolvimento.");
        }
    }

    private InputStream resolveCredentialsStream() throws Exception {
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            Path path = Path.of(credentialsPath.trim());
            if (Files.exists(path)) {
                return new FileInputStream(path.toFile());
            }
            ClassPathResource resource = new ClassPathResource(credentialsPath.trim());
            if (resource.exists()) {
                return resource.getInputStream();
            }
        }

        ClassPathResource defaultResource = new ClassPathResource("firebase-service-account.json");
        if (defaultResource.exists()) {
            return defaultResource.getInputStream();
        }

        ClassPathResource altResource = new ClassPathResource("serviceAccountKey.json");
        if (altResource.exists()) {
            return altResource.getInputStream();
        }

        return null;
    }
}
