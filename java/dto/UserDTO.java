package com.example.group56.dto;

import com.example.group56.model.User;
import com.example.group56.annotation.UniqueFieldValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* Simplified user DTO for validating modified settings of a user */

// Stacked with @UniqueFieldValues from AccountDTO
@UniqueFieldValues(
        fieldNames = {"name"},
        entityClass = User.class
)
// Extends accountDTO which means email has repeated validation here.
public class UserDTO extends AccountDTO {
    @NotBlank(message = "Username is required.")
    @Size(min = 3, message = "Must be at least 3 characters long.")
    @Size(max = 39, message = "Must be less than 40 characters long.")
    @Pattern(regexp = "^.{0,2}$|^[a-zA-Z0-9_-]{3,}$", message = "Can only contain letters, numbers, '-' and '_'.")
    private String name;

    @Size(max = 200, message = "Limited to 200 characters.")
    private String bio;

    public UserDTO() {}
    public UserDTO(User user) {
        super(user);
        this.name = user.getName();
        this.bio = user.getBio();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
