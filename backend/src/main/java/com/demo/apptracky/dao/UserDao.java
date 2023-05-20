package com.demo.apptracky.dao;

import com.demo.apptracky.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    User findByEmailValidationUuid(String uuid);

    User findByForgotPwUuid(String uuid);
}
