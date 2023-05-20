package com.demo.apptracky.dto;

import com.demo.apptracky.entities.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Model mapper dosen't support records yet
public class RegisterDto {
    // For OAuth, username and password is null
    @NotNull
    @Size(min = 3, max = 30, message = "username should be between 3 and 30 characters")
    private String username;
    @NotNull
    @Size(min = 8, message = "password should be at least 8 characters long")
    private String password;
    @NotNull
    @Email(regexp=".+@.+\\..+")
    private String email;

    public RegisterDto(
            final String username,
            final String password,
            final String email
    ) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
