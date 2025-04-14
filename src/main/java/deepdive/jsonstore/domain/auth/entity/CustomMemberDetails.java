package deepdive.jsonstore.domain.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
public class CustomMemberDetails implements UserDetails {


    private UUID uid;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomMemberDetails(UUID uid, Collection<? extends GrantedAuthority> authorities) {
        this.uid = uid;
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return null;  // 비밀번호는 포함하지 않음
    }

    @Override
    public String getUsername() {
        return null;  // 이메일도 포함하지않음
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

    public UUID getUid() {
        return uid;
    }
}
