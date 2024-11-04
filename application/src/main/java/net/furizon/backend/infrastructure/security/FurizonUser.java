package net.furizon.backend.infrastructure.security;

import lombok.Builder;
import lombok.Getter;
import net.minidev.json.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class FurizonUser implements UserDetails {
    private final long userId;

    @NotNull
    private final UUID sessionId;

    @NotNull
    @Builder.Default
    private final List<? extends GrantedAuthority> authorities = List.of();
    // TODO -> Import from user database model

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
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

    public boolean is2FaEnabled() {
        return false;
    }
}
