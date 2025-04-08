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

@Service
@RequiredArgsConstructor
public class AdminMemberDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다."));
        return new AdminMemberDetails(admin);
    }
}