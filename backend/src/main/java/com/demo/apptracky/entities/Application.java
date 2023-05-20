package com.demo.apptracky.entities;

import com.demo.apptracky.entities.enums.ApplicationStage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    @NotNull
    @Size(min = 1, max = 128)
    private String title;

    @Column(name = "url")
    @Size(min = 1, max = 2048)
    private String url;

    @Column(name = "company")
    @NotNull
    @Size(min = 1, max = 128)
    private String company;

    @Column(name = "recruiter")
    @Size(min = 1, max = 128)
    private String recruiter;

    @Column(name = "stage")
    @Enumerated(EnumType.STRING)
    @NotNull
    private ApplicationStage applicationStage;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    public Application() {}

    public Application(String title, String url, String company, String recruiter, ApplicationStage applicationStage, User user) {
        this.title = title;
        this.url = url;
        this.company = company;
        this.recruiter = recruiter;
        this.applicationStage = applicationStage;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(String recruiter) {
        this.recruiter = recruiter;
    }

    public ApplicationStage getApplicationStage() {
        return applicationStage;
    }

    public void setApplicationStage(ApplicationStage applicationStage) {
        this.applicationStage = applicationStage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
