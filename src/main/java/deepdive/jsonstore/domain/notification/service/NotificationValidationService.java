package deepdive.jsonstore.domain.notification.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.repository.MemberRepository;

public class NotificationValidationService {
    // Input 값 검증
    public static void validateInput(Long memberId, String token) {
        if (memberId == null) {
            throw new CommonException(JsonStoreErrorCode.UNAUTHORIZED);
        }

        if (token == null || token.trim().isEmpty()) {
            throw new CommonException.InvalidInputException();
        }
    }

    // 회원 정보 검증 -> 추후 member 쪽 uid 검증으로 수정 예정
    public static void validateMemberExistence(Long memberId, MemberRepository memberRepository) {
        boolean exists = memberRepository.existsById(memberId);

        if (!exists) {
            throw new CommonException(JsonStoreErrorCode.UNAUTHORIZED);
        }
    }
}
