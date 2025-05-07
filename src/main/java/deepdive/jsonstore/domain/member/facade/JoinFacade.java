package deepdive.jsonstore.domain.member.facade;

import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.service.JoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component // 스프링 빈으로 등록
@RequiredArgsConstructor // 생성자 자동 주입
@Slf4j // 로그 사용
public class JoinFacade {

    private final RedissonClient redissonClient; // Redisson을 통한 Redis 락 클라이언트
    private final JoinService joinService; // 실제 회원 가입 로직이 담긴 서비스

    /**
     * 회원 가입 요청을 Redisson 분산 락을 이용해 동시성 제어하며 처리
     */
    public void joinWithLock(JoinRequest request) {
        // 이메일을 기준으로 락의 키 생성
        String lockKey = "lock:join:" + request.email();
        RLock lock = redissonClient.getLock(lockKey); // 분산 락 객체 획득

        try {
            // 최대 3초 동안 락 획득을 시도하고, 락을 획득하면 10초 뒤 자동 해제
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 락을 획득한 경우, 회원 가입 처리
                joinService.joinProcess(request);
            } else {
                // 락을 획득하지 못한 경우 예외 발생
                throw new IllegalStateException("다른 사용자가 동시에 가입 중입니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            // 락 획득 중 인터럽트 발생 시 예외 처리
            Thread.currentThread().interrupt(); // 인터럽트 상태 복구
            throw new RuntimeException("락 획득 중단됨", e);
        } finally {
            // 현재 스레드가 락을 가지고 있을 경우에만 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
