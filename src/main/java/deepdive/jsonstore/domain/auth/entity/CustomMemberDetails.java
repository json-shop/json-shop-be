package deepdive.jsonstore.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class CustomMemberDetails implements UserDetails {

    private final UUID uid;  // 사용자 고유 식별자
    private final String email;  // 사용자 이메일
    private final String password;  // 사용자 비밀번호
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자 수정: 이메일과 비밀번호 추가
    public CustomMemberDetails(UUID uid, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 자격 증명 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true;  // 계정 활성화 여부
    }

    // 사용자 고유 식별자 반환
    public UUID getUid() {
        return uid;
    }
}