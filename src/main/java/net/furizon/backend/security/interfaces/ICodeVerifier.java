package net.furizon.backend.security.interfaces;

import net.furizon.backend.db.entities.users.User;

public interface ICodeVerifier {
    boolean verify(User user, String code);
}
