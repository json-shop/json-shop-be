package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일을 기반으로 Admin 조회
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다."));

        log.info("DB에서 조회된 ULID = {}", Base64.getUrlEncoder().encode(admin.getUlid()));

        // AdminMemberDetails 객체를 생성하여 반환
        return new AdminMemberDetails(
                admin.getUid(),
                admin.getUlid(),
                admin.getPassword(), // 비밀번호 포함
                Collections.singleton(new SimpleGrantedAuthority("ADMIN"))
        );
    }

    // UUID 기반 사용자 로드
    public AdminMemberDetails loadUserByUuid(UUID uuid) {
        // UUID를 기반으로 Admin 조회
        Admin admin = adminRepository.findByUid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("UUID에 해당하는 관리자를 찾을 수 없습니다."));

        log.info("DB에서 조회된 ULID = {}", Base64.getUrlEncoder().encode(admin.getUlid()));

        // AdminMemberDetails 객체를 생성하여 반환
        return new AdminMemberDetails(
                admin.getUid(),
                admin.getUlid(),
                admin.getPassword(), // 비밀번호 포함
                Collections.singleton(new SimpleGrantedAuthority("ADMIN"))
        );
    }
}