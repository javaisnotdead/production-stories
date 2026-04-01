package com.javaisnotdead.dstbug.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        createDatabaseIfNotExists();
        createTableIfNotExists();
        populateData();
    }

    private void createDatabaseIfNotExists() {
        jdbcTemplate.execute(
            "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'dstdemo') " +
            "CREATE DATABASE dstdemo"
        );
    }

    private void createTableIfNotExists() {
        jdbcTemplate.execute(
            "IF OBJECT_ID('dstdemo.dbo.time_intervals', 'U') IS NULL " +
            "CREATE TABLE dstdemo.dbo.time_intervals (" +
            "    id             BIGINT IDENTITY(1,1) PRIMARY KEY," +
            "    description    NVARCHAR(100) NOT NULL," +
            "    interval_start datetime2     NOT NULL," +
            "    interval_end   datetime2     NOT NULL" +
            ")"
        );
    }

    private void populateData() {
        jdbcTemplate.update("DELETE FROM dstdemo.dbo.time_intervals");

        // All timestamps stored as UTC in datetime2 columns.
        // Server JVM runs in Europe/Warsaw (CET/CEST).
        // DST transition: 2025-03-30 01:00 UTC (local 02:00 CET -> 03:00 CEST)

        insert("Before DST window",      "2025-03-30T00:20:00", "2025-03-30T00:40:00");
        insert("Approaching transition",  "2025-03-30T01:30:00", "2025-03-30T01:45:00");
        insert("At boundary",             "2025-03-30T01:58:00", "2025-03-30T02:01:00");
        insert("Gap start, CEST end",     "2025-03-30T02:59:00", "2025-03-30T03:00:00");
    }

    private void insert(String description, String start, String end) {
        // Use java.time.LocalDateTime to store raw UTC values without timezone conversion.
        // Timestamp.valueOf() would interpret the string as local time (Europe/Warsaw),
        // corrupting the UTC values we want to store.
        jdbcTemplate.update(
            "INSERT INTO dstdemo.dbo.time_intervals (description, interval_start, interval_end) VALUES (?, ?, ?)",
            description,
            java.time.LocalDateTime.parse(start),
            java.time.LocalDateTime.parse(end)
        );
    }
}
