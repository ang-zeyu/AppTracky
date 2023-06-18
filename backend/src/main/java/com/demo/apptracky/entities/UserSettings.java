package com.demo.apptracky.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", updatable = false, insertable = false)
    private Long userId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @Column(name = "is_reporting_enabled")
    @NotNull
    private Boolean isReportingEnabled = true;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getIsReportingEnabled() {
        return isReportingEnabled;
    }

    public void setIsReportingEnabled(Boolean reportingEnabled) {
        isReportingEnabled = reportingEnabled;
    }
}
