package net.furizon.backend.feature.user.objects.dto;

import lombok.Data;
import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public UserDisplayDataResponse(User user) {
        this.id = user.getId();
        this.fursonaName = user.getFursonaName();
        this.locale = user.getLocale();
        this.propicId = user.getPropicId();
        this.propicPath = null;
        this.sponsorType = 0;
    }
}
