package com.demo.apptracky.rest;

import com.demo.apptracky.dto.ApplicationDto;
import com.demo.apptracky.services.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private ApplicationService applicationService;

    public ApplicationController(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationDto> getUserApplications(final JwtAuthenticationToken authenticationToken) {
        return applicationService.getUserApplications(authenticationToken);
    }

    @PostMapping
    public void addUserApplication(
            @RequestBody @Valid final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        applicationService.addOrUpdateApplication(applicationDto, authenticationToken, true);
    }

    @PutMapping
    public void updateUserApplication(
            @RequestBody @Valid final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        applicationService.addOrUpdateApplication(applicationDto, authenticationToken, false);
    }

    @DeleteMapping
    public void deleteUserApplication(
            @RequestBody final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        applicationService.deleteUserApplication(applicationDto, authenticationToken);
    }
}
