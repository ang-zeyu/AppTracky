package com.demo.apptracky.services;

import com.demo.apptracky.dao.ApplicationDao;
import com.demo.apptracky.dto.ApplicationDto;
import com.demo.apptracky.entities.Application;
import com.demo.apptracky.entities.enums.ApplicationStage;
import com.demo.apptracky.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private final PropMapper propMapper;

    private final ApplicationDao applicationDao;

    public ApplicationService(PropMapper propMapper, ApplicationDao applicationDao) {
        this.propMapper = propMapper;
        this.applicationDao = applicationDao;
    }

    public List<ApplicationDto> getUserApplications(final JwtAuthenticationToken authenticationToken) {
        final long userId = UserService.getUserIdFromJwt(authenticationToken);
        final List<Application> applications = applicationDao.findByUserId(userId);

        return applications.stream()
                .map(app -> propMapper.map(app, ApplicationDto.class))
                .collect(Collectors.toList());
    }

    public void addOrUpdateApplication(
            final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken,
            final boolean isAdd
    ) {
        final long userId = UserService.getUserIdFromJwt(authenticationToken);
        if (!isAdd) {
            validateApplicationBelongsToUser(applicationDto, userId);
        }

        final Application application = propMapper.map(applicationDto, Application.class);
        if (isAdd) {
            application.setApplicationStage(ApplicationStage.APPLIED);
        }
        application.setUser(new User(userId));

        applicationDao.save(application);
    }

    private void validateApplicationBelongsToUser(ApplicationDto applicationDto, Long userId) {
        if (applicationDto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing application id");
        }

        final Application application = applicationDao
                .findById(applicationDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application does not exist"));

        if (!application.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
    }

    public void deleteUserApplication(
            @RequestBody final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken
    ) {
        validateApplicationBelongsToUser(applicationDto, UserService.getUserIdFromJwt(authenticationToken));

        applicationDao.deleteById(applicationDto.getId());
    }
}
