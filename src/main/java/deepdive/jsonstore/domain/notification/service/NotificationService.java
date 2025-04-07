package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.notification.service.NotificationValidationService;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public void saveToken(Long memberId, String token) {
        try {
            // 입력값 검증 로직
            NotificationValidationService.validateInput(memberId, token);

            NotificationValidationService.validateMemberExistence(memberId, memberRepository);

            redisTemplate.opsForValue().set("fcm:token:" + memberId, token);
            log.info("FCM Token saved successfully for member: {}", memberId);

        } catch (CommonException e) {
            throw e; // 도메인 예외는 그대로 전파
        } catch (Exception e) {
            log.error("Unexpected error while saving FCM token for member: {}", memberId, e);
            throw new CommonException.InternalServerException();
        }
    }
}
