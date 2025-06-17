package com.example.group56.dto;

import com.example.group56.model.User;
import com.example.group56.annotation.UniqueFieldValues;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* Simplified user DTO used for validation when registering an account or changing a password */

@UniqueFieldValues(
        fieldNames = {"email"},
        entityClass = User.class
)
public class AccountDTO {
    private long id;

    @NotBlank(message = "Email is required.")
    @Size(max = 49, message = "Must be less than 50 characters long.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Must be at least 6 characters long.")
    @Size(max = 99, message = "Must be less than 100 characters long.")
    @Pattern(regexp = "^(?:.{0,5}|(?=.{6,})(?=.*[A-Z]).*)$", message = "Must contain at least one uppercase letter.")
    @Pattern(regexp = "^(?:.{0,5}|(?=.{6,})(?=.*\\d).*)$", message = "Must contain at least one number.")
    @Pattern(regexp = "^(?:.{0,5}|(?=.{6,})(?=.*[@$!%*?&]).*)$", message = "Must contain at least one special character.")
    private String password;

    public AccountDTO() {}
    public AccountDTO(String email, String password) {
        this.password = password;
        this.email = email;
    }

    public AccountDTO(User user) {
        this.id = user.getId();
        this.password = user.getPassword();
        this.email = user.getEmail();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
