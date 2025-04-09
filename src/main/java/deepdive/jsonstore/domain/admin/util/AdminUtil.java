package deepdive.jsonstore.domain.admin.util;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AdminUtil {

    public Admin getCurrentAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AdminMemberDetails) {
            return ((AdminMemberDetails) principal).getAdmin();
        }

        throw new IllegalStateException("현재 인증된 관리자가 존재하지 않거나 형식이 올바르지 않습니다.");
    }
}
