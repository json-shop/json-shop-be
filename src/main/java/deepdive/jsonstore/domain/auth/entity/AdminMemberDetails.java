package deepdive.jsonstore.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class AdminMemberDetails implements UserDetails {

    private final UUID adminUid;  // Admin의 UUID
    private final String password; // Admin의 비밀번호
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자: UUID, 비밀번호, 권한을 받음
    public AdminMemberDetails(UUID adminUid, String password, Collection<? extends GrantedAuthority> authorities) {
        this.adminUid = adminUid;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password; // 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return adminUid.toString(); // UUID를 문자열로 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정이 만료되지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정이 잠기지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 자격 증명이 만료되지 않음
    }

    @Override
    public boolean isEnabled() {
        return true;  // 계정이 활성화 상태임
    }

    // UUID 반환
    public UUID getAdminUid() {
        return adminUid;
    }
}