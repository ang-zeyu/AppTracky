package com.demo.apptracky.dao;

import com.demo.apptracky.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationDao extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
}
