package deepdive.jsonstore.domain.member.facade;

import deepdive.jsonstore.common.lock.RedisLockService;
import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.service.JoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class JoinFacade {

    private final RedisLockService redisLockService;
    private final JoinService joinService;

    public void joinWithLock(JoinRequest request) {
        String lockKey = "lock:join:" + request.email();

        redisLockService.runWithLock(
                lockKey,
                Duration.ofSeconds(3),   // 락 대기 시간
                Duration.ofSeconds(10),  // 락 점유 시간
                () -> {
                    joinService.joinProcess(request);
                    return null;
                }
        );
    }
}
