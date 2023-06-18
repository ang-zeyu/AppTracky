package com.demo.apptracky.dto;

public record SettingsDto(
        String password,
        Boolean isReportingEnabled
) {}
