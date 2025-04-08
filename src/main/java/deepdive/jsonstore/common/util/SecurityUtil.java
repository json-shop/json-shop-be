package deepdive.jsonstore.common.util;

import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    // 현재 로그인한 사용자(Member 객체)를 반환
    public static Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomMemberDetails customMemberDetails) {
            return customMemberDetails.getMember(); // 멤버 통으로 리턴
        } else {
            throw new RuntimeException("유효하지 않은 인증 정보입니다.");
        }
    }


}
