package deepdive.jsonstore.domain.admin.service;

import deepdive.jsonstore.domain.admin.dto.AdminDto;
import deepdive.jsonstore.domain.admin.dto.AdminJoinRequest;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminJoinService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AdminJoinValidationService adminJoinValidationService;

    public void adminJoinProcess(@Valid AdminJoinRequest adminJoinRequest) {
        // 회원 가입 전 검증 수행
        adminJoinValidationService.AdminValidateJoinRequest(adminJoinRequest);
        // DTO 생성
        AdminDto adminDto = new AdminDto(
                null, // uid - 회원가입 시 생성 예정
                adminJoinRequest.username(),
                adminJoinRequest.email(),
                adminJoinRequest.phone(),
                false, // isDeleted
                null   // deletedAt
        );

        // 엔티티 변환 및 저장
        Admin admin = adminDto.toEntity(bCryptPasswordEncoder.encode(adminJoinRequest.password()));
        adminRepository.save(admin);
        log.info("관리자 회원가입 완료: {}", admin.getUsername());
    }
}
