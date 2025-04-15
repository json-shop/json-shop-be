package deepdive.jsonstore.domain.admin.service;

import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.admin.dto.AdminJoinRequest;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminJoinValidationService {
    private final AdminRepository adminRepository;

    public void AdminValidateJoinRequest(AdminJoinRequest adminJoinRequest) {
        // 이메일 중복 체크
        if (adminRepository.existsByEmail(adminJoinRequest.email())) {
            throw new JoinException.DuplicateEmailException(JsonStoreErrorCode.DUPLICATE_EMAIL);
        }

        if (!adminJoinRequest.password().equals(adminJoinRequest.confirmPassword())) {
            throw new JoinException.PasswordMismatchException(JsonStoreErrorCode.PASSWORD_MISMATCH);
        }

    }
}
