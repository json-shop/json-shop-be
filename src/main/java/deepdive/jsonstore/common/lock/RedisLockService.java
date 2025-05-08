package deepdive.jsonstore.common.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RedisLockService {

    private final RedissonClient redissonClient;

    /**
     * 분산락을 이용해 작업을 실행합니다.
     *
     * @param key        락 키
     * @param waitTime   락 대기 시간 (ex: Duration.ofSeconds(3))
     * @param leaseTime  락 자동 해제 시간 (ex: Duration.ofSeconds(10))
     * @param task       락 획득 후 실행할 작업
     * @return 작업 결과
     */
    public <T> T runWithLock(String key, Duration waitTime, Duration leaseTime, Supplier<T> task) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new IllegalStateException("다른 사용자가 동시에 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중단됨", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}