package deepdive.jsonstore.domain.member.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MemberUtil {

    public Member getCurrentMember() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomMemberDetails) {
            return ((CustomMemberDetails) principal).getMember();
        }

        throw new AuthException.UnauthenticatedAccessException();
    }
}
