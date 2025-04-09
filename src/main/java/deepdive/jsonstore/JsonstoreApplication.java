package deepdive.jsonstore;

import deepdive.jsonstore.common.config.RedisTestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class JsonstoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(JsonstoreApplication.class, args);
	}

	@Bean
	public CommandLineRunner testRedis(RedisTestService redisTestService) {
		return args -> {
			redisTestService.testRedisConnection();
		};
	}
}
