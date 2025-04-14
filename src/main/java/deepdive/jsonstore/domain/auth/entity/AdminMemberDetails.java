package deepdive.jsonstore.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class AdminMemberDetails implements UserDetails {

    private final UUID adminUid;  // Admin의 UUID
    private final String email;  // 이메일
    private final String password;  // 비밀번호
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자: 이메일과 비밀번호를 추가
    public AdminMemberDetails(UUID adminUid, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.adminUid = adminUid;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;  // 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return email;  // 이메일 반환
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
        return true;
    }

    // UUID 반환
    public UUID getAdminUid() {
        return adminUid;
    }
}