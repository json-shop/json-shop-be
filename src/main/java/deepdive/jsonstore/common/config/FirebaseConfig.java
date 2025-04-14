package deepdive.jsonstore.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.*;

@Slf4j
@Configuration
public class FirebaseConfig {

    //    @Value("${fcm.credentials.file:classpath:firebaseAccessKey.json}")
    @Value("${fcm.credentials.file}") //ec2에서 불러올 때
    private String fcmCredentialsPath;

    //ec2에서 엑세스 키를 가져올 때 classpath로 가져오는 방식으로 하지 않아서 classpath인 경우와 ec2 내부에서 파일을 가져오는 경우, 두 가지로 분기하여 동작하도록 수정해놨습니다.
    //나중에 의논해서 구조를 결정해야할 것 같습니다.

//    @PostConstruct
//    public void initialize() {
//        log.info("Firebase 초기화 시작, 파일 경로: {}", fcmCredentialsPath);
//
//        try {
//            String resourcePath = fcmCredentialsPath.replace("classpath:", "");
//            ClassPathResource resource = new ClassPathResource(resourcePath);
//
//            if (!resource.exists()) {
//                log.error("Firebase 설정 파일을 찾을 수 없습니다: {}", resourcePath);
//                throw new RuntimeException("Firebase 설정 파일을 찾을 수 없습니다");
//            }
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
//                    .build();
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//                log.info("Firebase application has been initialized");
//            }
//        } catch (IOException e) {
//            log.error("Error initializing Firebase: ", e);
//            throw new RuntimeException("Firebase 초기화 중 오류가 발생했습니다", e);
//        }
//    }

    @PostConstruct
    public void initialize() {
        log.info("Firebase 초기화 시작, 파일 경로: {}", fcmCredentialsPath);

        try (InputStream inputStream = getCredentialsInputStream()) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }

        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: ", e);
            throw new RuntimeException("Firebase 초기화 중 오류가 발생했습니다", e);
        }
    }

    private InputStream getCredentialsInputStream() throws IOException {
        if (fcmCredentialsPath.startsWith("classpath:")) {
            String path = fcmCredentialsPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                log.error("Firebase 설정 파일을 classpath에서 찾을 수 없습니다: {}", path);
                throw new FileNotFoundException("Firebase 설정 파일이 classpath에 없음");
            }

            return resource.getInputStream();
        } else {
            File file = new File(fcmCredentialsPath);

            if (!file.exists()) {
                log.error("Firebase 설정 파일을 파일 시스템에서 찾을 수 없습니다: {}", fcmCredentialsPath);
                throw new FileNotFoundException("Firebase 설정 파일이 파일 시스템에 없음");
            }

            return new FileInputStream(file);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}