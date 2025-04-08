package deepdive.jsonstore.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.credentials.file:classpath:firebaseAccessKey.json}") // 기본값 설정
    private String fcmCredentialsPath;

    @PostConstruct
    public void initialize() {
        log.info("Firebase 초기화 시작, 파일 경로: {}", fcmCredentialsPath);

        try {
            log.info("Firebase 초기화 시작, 파일 경로: {}", fcmCredentialsPath);

            // classpath: 접두사 제거
            String resourcePath = fcmCredentialsPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.error("Firebase 설정 파일을 찾을 수 없습니다: {}", resourcePath);
                throw new RuntimeException("Firebase 설정 파일을 찾을 수 없습니다");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase: ", e);
            throw new RuntimeException("Firebase 초기화 중 오류가 발생했습니다", e);
        }
    }
}