package deepdive.jsonstore.domain.auth.entity;

import deepdive.jsonstore.domain.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomMemberDetails implements UserDetails {

    private final Member member;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomMemberDetails(Member member, Collection<? extends GrantedAuthority> authorities) {
        this.member = member;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // 비밀번호
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 사용자 이름(email)
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
