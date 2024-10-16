package net.furizon.backend.web.entities.users;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class UserLoginRequest {
    private String email;
    private String password;
}
