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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

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
			// 애플리케이션 종료 시 생성된 관리자 계정을 삭제
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (createdAdmin != null) {
					adminRepository.delete(createdAdmin);
					System.out.println("관리자 계정 삭제: " + createdAdmin.getEmail());
				}
			}));
		};
	}

}