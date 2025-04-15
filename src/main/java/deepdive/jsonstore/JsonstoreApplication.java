package deepdive.jsonstore;

import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@EnableRetry
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class JsonstoreApplication {

	private Admin createdAdmin; // 생성된 관리자 계정 정보를 저장

	public static void main(String[] args) {
		SpringApplication.run(JsonstoreApplication.class, args);
	}

	@Bean
	public CommandLineRunner testRedis(RedisTestService redisTestService) {
		return args -> redisTestService.testRedisConnection();
	}


	// 테스트를 위한 관리자 계정 생성
	@Bean
	public CommandLineRunner insertAdminAccount(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// 관리자 계정 생성
			Admin admin = Admin.builder()
					.uid(UUID.randomUUID())
					.username("admin1")
					.password(passwordEncoder.encode("admin1234"))
					.email("admin1@example.com")
					.phone("01012345678")
					.deleted(false)
					.build();

			createdAdmin = adminRepository.save(admin); // 생성된 관리자 계정 저장
			System.out.println("관리자 계정 생성: " + admin.getEmail());
		};
	}
	@Bean
	public CommandLineRunner deleteAdminOnShutdown(AdminRepository adminRepository) {
		return args -> {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					// 애플리케이션 컨텍스트가 활성 상태인지 확인
					if (adminRepository != null) {
						adminRepository.delete(createdAdmin);
						System.out.println("관리자 계정 삭제: " + createdAdmin.getEmail());
					}
				} catch (IllegalStateException e) {
					// 컨텍스트가 종료된 상태에서 접근 시 예외 처리
					System.err.println("애플리케이션 컨텍스트가 종료된 후 작업 시도. 무시합니다.");
				}
			}));
		};
	}


	}

