package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMemberDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다."));

        return new AdminMemberDetails(
                admin.getUid(),
                Collections.singleton(new SimpleGrantedAuthority("ADMIN"))
        );
    }

    // UUID 기반 사용자 로드 메서드 추가
    public AdminMemberDetails loadUserByUuid(UUID uuid) {
        Admin admin = adminRepository.findByUid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("UUID에 해당하는 관리자를 찾을 수 없습니다."));

        return new AdminMemberDetails(
                admin.getUid(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
