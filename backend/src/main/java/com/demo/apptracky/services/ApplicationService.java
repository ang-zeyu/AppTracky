package com.demo.apptracky.services;

import com.demo.apptracky.dao.ApplicationDao;
import com.demo.apptracky.dto.ApplicationDto;
import com.demo.apptracky.entities.Application;
import com.demo.apptracky.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private static Logger log = LoggerFactory.getLogger(ApplicationService.class);

    @Value("${apptracky.s3-user-store-bucket}")
    private String s3UserStoreBucket;

    private final PropMapper propMapper;

    private final ApplicationDao applicationDao;

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    public ApplicationService(PropMapper propMapper, ApplicationDao applicationDao) {
        this.propMapper = propMapper;
        this.applicationDao = applicationDao;
        this.s3Client = S3Client.builder().build();
        this.s3Presigner = S3Presigner.builder().build();
    }

    private String getS3Key(final long id) {
        return "resumes/" + id + ".pdf";
    }

    private String generateS3GetPresignedUrl(final long id) {
        try {
            final GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(s3UserStoreBucket)
                    .key(getS3Key(id))
                    .build();

            final GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(3))
                    .getObjectRequest(objectRequest)
                    .build();

            final PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            log.info("Presigned URL to upload a file to: " + myURL);
            log.info("Which HTTP method needs to be used when uploading a file: " + presignedRequest.httpRequest().method());

            // Upload content to the Amazon S3 bucket by using this URL.
            return myURL;
        } catch (final S3Exception e) {
            log.error("Failed to presign URL for " + id, e);
            throw new RuntimeException(e);
        }
    }

    private String generateS3PutPresignedUrl(final long id) {
        try {
            final PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(s3UserStoreBucket)
                    .key(getS3Key(id))
                    //.contentType("multipart/form-data")
                    .build();

            final PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(3))
                    .putObjectRequest(objectRequest)
                    .build();

            final PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            log.info("Presigned URL to upload a file to: " + myURL);
            log.info("Which HTTP method needs to be used when uploading a file: " + presignedRequest.httpRequest().method());

            // Upload content to the Amazon S3 bucket by using this URL.
            return myURL;
        } catch (final S3Exception e) {
            log.error("Failed to presign URL for " + id, e);
            throw new RuntimeException(e);
        }
    }

    public ApplicationDto getUserApplication(
            final JwtAuthenticationToken authenticationToken,
            final long id,
            final Boolean includeResume
    ) {
        final long userId = UserService.getUserIdFromJwt(authenticationToken);

        final Application application = applicationDao.findById(id).orElse(null);
        if (application == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (application.getUserId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final ApplicationDto applicationDto = propMapper.map(application, ApplicationDto.class);

        if (includeResume) {
            try {
                final HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                        .bucket(s3UserStoreBucket)
                        .key(getS3Key(id))
                        .build();
                s3Client.headObject(headObjectRequest);

                final String url = generateS3GetPresignedUrl(id);
                applicationDto.setResumeGetUrl(url);
            } catch (final NoSuchKeyException e) {
                log.info("Head preflight for resume " + id + "failed", e);

                applicationDto.setHasResume(false);
                application.setHasResume(false);
                applicationDao.save(application);
            }
        }

        return applicationDto;
    }

    public List<ApplicationDto> getUserApplications(final JwtAuthenticationToken authenticationToken) {
        final long userId = UserService.getUserIdFromJwt(authenticationToken);
        final List<Application> applications = applicationDao.findByUserId(userId);

        return applications.stream()
                .map(app -> propMapper.map(app, ApplicationDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationDto addOrUpdateApplication(
            final ApplicationDto applicationDto,
            final JwtAuthenticationToken authenticationToken,
            final boolean isAdd
    ) {
        final long userId = UserService.getUserIdFromJwt(authenticationToken);
        if (!isAdd) {
            validateApplicationBelongsToUser(applicationDto, userId);
        }

        final Application application = propMapper.map(applicationDto, Application.class);
        application.setUser(new User(userId));

        final Application savedApplication = applicationDao.save(application);
        applicationDto.setId(savedApplication.getId());

        if (applicationDto.getHasResume()) {
            final String url = generateS3PutPresignedUrl(savedApplication.getId());
            applicationDto.setResumePutUrl(url);
        }

        return applicationDto;
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
