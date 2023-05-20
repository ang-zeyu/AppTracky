package com.demo.apptracky.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotNull
        String uuid,
        @NotNull
        @Size(min = 8, message = "password should be at least 8 characters long")
        String password
) {}
