package deepdive.jsonstore.domain.admin.util;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminUtil {

    private final AdminRepository adminRepository;

    public AdminUtil(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin getCurrentAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AdminMemberDetails) {
            UUID adminUid = ((AdminMemberDetails) principal).getAdminUid();  // 인증된 관리자의 UUID 추출
            return adminRepository.findByUid(adminUid)  // UUID로 관리자 정보 조회
                    .orElseThrow(() -> new AuthException.UnauthenticatedAccessException());
        }

        throw new AuthException.UnauthenticatedAccessException();
    }
}
