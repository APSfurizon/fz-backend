package net.furizon.backend.security.interfaces;

import net.furizon.backend.db.entities.users.User;

public interface CodeVerifier {
    boolean verify(User user, String code);
}
