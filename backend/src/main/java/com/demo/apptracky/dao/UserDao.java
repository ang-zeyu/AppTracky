package com.demo.apptracky.dao;

import com.demo.apptracky.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserDao extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    User findByEmailValidationUuid(String uuid);

    User findByForgotPwUuid(String uuid);

    @Query("FROM User u JOIN FETCH u.applications application WHERE u.userSettings.isReportingEnabled")
    List<User> findAllByIsReportingEnabled();
}
