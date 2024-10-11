package net.furizon.backend.security.entities;

import lombok.Getter;
import net.furizon.backend.db.entities.users.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserSecurity implements UserDetails {

    private String username;

    private String passwordHash;

    private boolean credentialsExpired;

    private boolean loginDisabled;

    @Getter
    private boolean requiresMfa;

    public UserSecurity() {}

    public UserSecurity (User from) {
        this.username = from.getAuthentication().getEmail();
        this.passwordHash = from.getAuthentication().getPasswordHash();
        this.credentialsExpired = from.getAuthentication().isLoginExpired();
        this.loginDisabled = from.getAuthentication().isLoginDisabled();
        this.requiresMfa = from.getAuthentication().is2faEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
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
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return !this.loginDisabled;
    }
}
