package com.demo.apptracky.entities;

import com.demo.apptracky.entities.enums.ApplicationStage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

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

    @Column(name = "has_resume")
    private boolean hasResume = false;

    @Column(name = "date", updatable = false, nullable = false)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
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

    @PrePersist
    protected void beforePersist() {
        date = new Date();
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

    public boolean getHasResume() {
        return hasResume;
    }

    public void setHasResume(boolean hasResume) {
        this.hasResume = hasResume;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
