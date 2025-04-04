package deepdive.jsonstore.common.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTestService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void testRedisConnection() {
        redisTemplate.opsForValue().set("testKey", "Hello Redis!");
        String value = (String) redisTemplate.opsForValue().get("testKey");
        System.out.println("Redis에서 가져온 값: " + value);
    }
}
