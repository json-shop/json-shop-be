package deepdive.jsonstore;

import deepdive.jsonstore.common.config.RedisTestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JsonstoreApplication {

	public static void main(String[] args) {SpringApplication.run(JsonstoreApplication.class, args);}

	@Bean
	public CommandLineRunner testRedis(RedisTestService redisTestService) {
		return args -> {
			redisTestService.testRedisConnection();
		};
	}
}
