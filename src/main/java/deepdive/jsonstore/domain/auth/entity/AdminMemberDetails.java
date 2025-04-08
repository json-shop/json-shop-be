package deepdive.jsonstore.domain.auth.entity;

import deepdive.jsonstore.domain.admin.entity.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AdminMemberDetails implements UserDetails {

    private final Admin admin;
    private final Collection<? extends GrantedAuthority> authorities;

    public AdminMemberDetails(Admin admin, Collection<? extends GrantedAuthority> authorities) {
        this.admin = admin;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getEmail();
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

    public Admin getAdmin() {
        return admin;
    }
}