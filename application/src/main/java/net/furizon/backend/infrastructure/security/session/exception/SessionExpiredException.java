package net.furizon.backend.infrastructure.security.session.exception;

import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.AuthenticationException;

public class SessionExpiredException extends AuthenticationException {
    public SessionExpiredException(@Nullable String msg) {
        super(msg);
    }

    public SessionExpiredException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
