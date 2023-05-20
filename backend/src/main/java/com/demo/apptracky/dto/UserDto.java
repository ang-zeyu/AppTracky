package com.demo.apptracky.dto;

import java.util.List;

public class UserDto {
    private String jwtToken;

    private Long id;

    private String username;

    private String email;

    private boolean isGoogle;

    private boolean isValidated;

    private List<String> roles;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsGoogle() {
        return isGoogle;
    }

    public void setIsGoogle(boolean google) {
        isGoogle = google;
    }

    public boolean isIsValidated() {
        return isValidated;
    }

    public void setIsValidated(boolean validated) {
        isValidated = validated;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
