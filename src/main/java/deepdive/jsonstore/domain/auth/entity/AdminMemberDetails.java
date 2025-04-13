package deepdive.jsonstore.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class AdminMemberDetails implements UserDetails {

    private final UUID adminUid;  // Admin의 UUID
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자 수정: email과 password를 제거하고, UUID와 authorities만 받도록
    public AdminMemberDetails(UUID adminUid, Collection<? extends GrantedAuthority> authorities) {
        this.adminUid = adminUid;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;  // 비밀번호는 토큰에 포함하지 않음
    }

    @Override
    public String getUsername() {
        return null;  // 이메일은 토큰에 포함하지 않음
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;  // 기본적으로 true를 반환 (필요에 따라 수정)
    }

    // UUID만 반환 (토큰에 사용)
    public UUID getAdminUid() {
        return adminUid;
    }

    // 권한만 반환 (토큰에 사용)
    public Collection<? extends GrantedAuthority> getAuthoritiesForToken() {
        return authorities;
    }
}
