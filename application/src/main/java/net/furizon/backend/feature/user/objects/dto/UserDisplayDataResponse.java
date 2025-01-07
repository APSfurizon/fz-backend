package net.furizon.backend.feature.user.objects.dto;

import lombok.Data;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class UserDisplayDataResponse {
    private final long id;

    @NotNull
    private final String fursonaName;

    @Nullable
    private final String locale;

    @Nullable
    private final Long propicId;

    @Nullable
    private final String propicPath;

    @Nullable
    private final Integer sponsorType;

    @NotNull
    private final List<Role> roles;

    public UserDisplayDataResponse(@NotNull User user, @NotNull List<Role> roles) {
        this.id = user.getId();
        this.fursonaName = user.getFursonaName();
        this.locale = user.getLocale();
        this.propicId = user.getPropicId();
        this.propicPath = null;
        this.sponsorType = 0;
        this.roles = roles;
    }
}
