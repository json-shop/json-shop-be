package deepdive.jsonstore.domain.member.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        // Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없는 경우 예외 처리
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AuthException.UnauthenticatedAccessException();
        }

        Object principal = authentication.getPrincipal();

        // 인증된 principal이 CustomMemberDetails인지 확인
        if (principal instanceof CustomMemberDetails) {
            CustomMemberDetails customMemberDetails = (CustomMemberDetails) principal;

            // UUID를 통해 현재 사용자의 Member 정보 가져오기
            return memberRepository.findByUid(customMemberDetails.getUid())
                    .orElseThrow(AuthException.UnauthenticatedAccessException::new);
        }

        // 인증되지 않은 사용자 예외 처리
        throw new AuthException.UnauthenticatedAccessException();
    }
}