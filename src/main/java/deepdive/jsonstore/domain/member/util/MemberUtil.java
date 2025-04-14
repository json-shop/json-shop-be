package deepdive.jsonstore.domain.member.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new AuthException.UnauthenticatedAccessException();
        }

        if (principal instanceof CustomMemberDetails) {
            CustomMemberDetails customMemberDetails = (CustomMemberDetails) principal;
            // UUID를 통해 현재 사용자의 Member 정보 가져오기
            return memberRepository.findByUid(customMemberDetails.getUid())
                    .orElseThrow(() -> new AuthException.UnauthenticatedAccessException());
        }

        throw new AuthException.UnauthenticatedAccessException();
    }
}
