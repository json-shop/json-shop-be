package deepdive.jsonstore.common.util;

import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final MemberRepository memberRepository; // MemberRepository 의존성 주입

    // 현재 로그인한 사용자(Member 객체)를 반환
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomMemberDetails customMemberDetails) {
            // UUID로 멤버를 찾기
            return memberRepository.findByUid(customMemberDetails.getUid())
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 사용자입니다."));
        } else {
            throw new RuntimeException("유효하지 않은 인증 정보입니다.");
        }
    }
}
