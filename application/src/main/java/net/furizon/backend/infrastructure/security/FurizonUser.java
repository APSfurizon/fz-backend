package net.furizon.backend.infrastructure.security;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import net.furizon.backend.feature.authentication.Authentication;
import net.minidev.json.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
public class FurizonUser implements UserDetails {
    @Getter
    private final long userId;

    @Getter
    @NotNull
    private final UUID sessionId;

    @Getter(AccessLevel.NONE)
    @NotNull
    private final Authentication authentication;

    @JsonIgnore
    @NotNull
    @Builder.Default
    private final List<? extends GrantedAuthority> authorities = List.of();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return authentication.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return authentication.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return authentication.isDisabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isTwoFactorEnabled() {
        return authentication.isTwoFactorEnabled();
    }
}
