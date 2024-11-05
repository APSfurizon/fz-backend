package net.furizon.backend.infrastructure.security.session.finder;

import net.furizon.backend.infrastructure.security.session.Session;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface SessionFinder {
    @Nullable
    Session findSessionById(UUID id);
}
