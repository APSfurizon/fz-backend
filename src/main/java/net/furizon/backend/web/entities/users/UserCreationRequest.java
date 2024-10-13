package net.furizon.backend.web.entities.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserCreationRequest {
    @Email
    private String email;

    @NotNull
    @Length(min=8)
    private String password;

    private String passwordConfirmation;

}
