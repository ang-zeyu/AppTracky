package com.demo.apptracky.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true)
    @NotNull // if registered through OAuth, it is the email
    private String username;

    @Column(name = "password")
    private String password;   // NULL if OAuth only

    @Column(name = "email", unique = true)
    @NotNull
    private String email;

    @Column(name = "is_google")
    @NotNull
    private Boolean isGoogle;

    @Column(name = "validated")
    @NotNull
    private Boolean isValidated;

    @Column(name = "validated_uuid", columnDefinition = "char(36)", length = 36)
    private String emailValidationUuid;

    @Column(name = "forgot_pw_uuid", columnDefinition = "char(36)", length = 36)
    private String forgotPwUuid;

    @Column(name = "forgot_pw_expiry")
    private Long forgotPwExpiry;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @NotNull
    private UserSettings userSettings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Application> applications;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL)
    private List<Authority> authorities;

    public User() {}

    public User(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    public Boolean getIsGoogle() {
        return isGoogle;
    }

    public void setIsGoogle(Boolean isGoogle) {
        this.isGoogle = isGoogle;
    }

    public Boolean getValidated() {
        return isValidated;
    }

    public void setValidated(Boolean validated) {
        isValidated = validated;
    }

    public String getEmailValidationUuid() {
        return emailValidationUuid;
    }

    public void setEmailValidationUuid(String emailValidationUuid) {
        this.emailValidationUuid = emailValidationUuid;
    }

    public String getForgotPwUuid() {
        return forgotPwUuid;
    }

    public void setForgotPwUuid(String forgotPwUuid) {
        this.forgotPwUuid = forgotPwUuid;
    }

    public Long getForgotPwExpiry() {
        return forgotPwExpiry;
    }

    public void setForgotPwExpiry(Long forgotPwExpiry) {
        this.forgotPwExpiry = forgotPwExpiry;
    }
}
