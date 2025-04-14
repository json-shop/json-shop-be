package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationValidationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    public String validateAndGetFcmToken(UUID memberUid) {
        String token = redisTemplate.opsForValue().get("fcm:token:" + memberUid);
        if (token == null) {
            throw new NotificationException.MissingFcmTokenException();
        }
        return token;
    }

    public void validateMemberExists(UUID memberUid) {
        if (!memberRepository.existsByUid(memberUid)) {
            throw new NotificationException.NotificationMemberNotFoundException();
        }
    }

    public Member validateAndGetMember(UUID memberUid) {
        return memberRepository.findByUid(memberUid)
                .orElseThrow(NotificationException.NotificationMemberNotFoundException::new);
    }
}

