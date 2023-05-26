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
    private final ApplicationService applicationService;

    public ApplicationController(final ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Has S3 pre-signed URL populated
    @GetMapping("/{id}")
    public ApplicationDto getUserApplication(
            @PathVariable long id,
            @RequestParam Boolean includeResume,
            final JwtAuthenticationToken authenticationToken
    ) {
        return applicationService.getUserApplication(
                authenticationToken, id, includeResume
        );
    }

    @GetMapping
    public List<ApplicationDto> getUserApplications(final JwtAuthenticationToken authenticationToken) {
        return applicationService.getUserApplications(authenticationToken);
    }

    @PostMapping
    public ApplicationDto addUserApplication(
            @RequestBody @Valid final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        return applicationService.addOrUpdateApplication(applicationDto, authenticationToken, true);
    }

    @PutMapping
    public ApplicationDto updateUserApplication(
            @RequestBody @Valid final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        return applicationService.addOrUpdateApplication(applicationDto, authenticationToken, false);
    }

    @DeleteMapping
    public void deleteUserApplication(
            @RequestBody final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        applicationService.deleteUserApplication(applicationDto, authenticationToken);
    }
}
