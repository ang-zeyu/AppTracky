package com.demo.apptracky.dto;

import com.demo.apptracky.entities.enums.ApplicationStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class ApplicationDto {
    private Long id;

    @NotNull
    @Size(min = 1, max = 128)
    private String title;

    @NotNull
    @URL
    private String url;

    @NotNull
    @Size(min = 1, max = 128)
    private String company;

    @Size(min = 1, max = 128)
    private String recruiter;

    private ApplicationStage applicationStage;

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
}
