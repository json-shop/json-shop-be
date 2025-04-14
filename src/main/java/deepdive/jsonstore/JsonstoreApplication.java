package deepdive.jsonstore;

import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import jakarta.annotation.PreDestroy;
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

	private final AdminRepository adminRepository;
	private Admin createdAdmin;

	public JsonstoreApplication(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(JsonstoreApplication.class, args);
	}

	@Bean
	public CommandLineRunner insertAdminAccount(PasswordEncoder passwordEncoder) {
		return args -> {
			Admin admin = Admin.builder()
					.uid(UUID.randomUUID())
					.username("admin1")
					.password(passwordEncoder.encode("admin1234"))
					.email("admin1@example.com")
					.phone("01012345678")
					.deleted(false)
					.build();

			createdAdmin = adminRepository.save(admin);
			System.out.println("관리자 계정 생성: " + admin.getEmail());
		};
	}

	@PreDestroy
	public void cleanup() {
		if (createdAdmin != null) {
			adminRepository.delete(createdAdmin);
			System.out.println("관리자 계정 삭제: " + createdAdmin.getEmail());
		}
	}
}