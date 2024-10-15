package net.furizon.backend.security.entities;

import lombok.Getter;
import net.furizon.backend.db.entities.users.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserSecurity implements UserDetails {

    @Getter
    private User user;

    public UserSecurity() {}

    public UserSecurity (User from) {
        this.user = from;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.user.getAuthentication().getPasswordHash();
    }

    @Override
    public String getUsername() {
        return this.user.getAuthentication().getEmail();
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
        return !this.user.getAuthentication().isLoginExpired();
    }

    @Override
    public boolean isEnabled() {
        return !this.user.getAuthentication().isLoginDisabled();
    }

    public boolean is2faEnabled () {
        return this.user.getAuthentication().is2faEnabled();
    }
}
