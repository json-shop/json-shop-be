package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationValidationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    public String validateAndGetFcmToken(Long memberId) {
        String token = redisTemplate.opsForValue().get("fcm:token:" + memberId);
        if (token == null) {
            throw new NotificationException.MissingFcmTokenException();
        }
        return token;
    }

    public void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotificationException.NotificationMemberNotFoundException();
        }
    }

    public Member validateAndGetMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotificationException.NotificationMemberNotFoundException::new);
    }
}

