package com.javaisnotdead.dstbug;

import com.javaisnotdead.dstbug.domain.TimeInterval;
import com.javaisnotdead.dstbug.repository.BuggyRepository;
import com.javaisnotdead.dstbug.repository.FixedRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests demonstrating the DST bug in JDBC timestamp retrieval.
 *
 * SQL Server starts automatically via Testcontainers - no manual docker-compose needed.
 *
 * The key insight: the buggy read path passes when the JVM runs in UTC -
 * which is exactly how most CI environments are configured. The bug only
 * surfaces in a DST-observing timezone like Europe/Warsaw.
 */
@SpringBootTest
@Testcontainers
class DstTimestampTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
        .acceptLicense();

    @Autowired
    private BuggyRepository buggyRepository;

    @Autowired
    private FixedRepository fixedRepository;

    private TimeZone originalTimeZone;

    @BeforeEach
    void saveTimezone() {
        originalTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void restoreTimezone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Nested
    @DisplayName("BuggyRepository - getTimestamp() without Calendar")
    class BuggyRepositoryTests {

        @Test
        @DisplayName("Europe/Warsaw: timestamps near DST boundary come back inverted")
        void buggyRead_warsawTimezone_invertedTimestamps() {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));

            List<TimeInterval> rows = buggyRepository.findAll();

            TimeInterval boundaryRecord = rows.stream()
                .filter(r -> r.getDescription().equals("Gap start, CEST end"))
                .findFirst()
                .orElseThrow();

            // The buggy row mapper returns intervalStart AFTER intervalEnd
            // because getTimestamp() resolves the two UTC values with different
            // offsets near the DST transition
            assertThat(boundaryRecord.isInverted())
                .as("Buggy read in Europe/Warsaw should produce inverted timestamps at DST boundary")
                .isTrue();
        }

        @Test
        @DisplayName("UTC: same code, same data, no bug - this is why CI never catches it")
        void buggyRead_utcTimezone_noBug() {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            List<TimeInterval> rows = buggyRepository.findAll();

            // Every record looks correct in UTC - no DST transition, no ambiguity
            assertThat(rows).noneMatch(TimeInterval::isInverted);
        }
    }

    @Nested
    @DisplayName("FixedRepository - getObject() with java.time.LocalDateTime")
    class FixedRepositoryTests {

        @Test
        @DisplayName("Europe/Warsaw: timestamps are correct regardless of DST")
        void fixedRead_warsawTimezone_correctTimestamps() {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));

            List<TimeInterval> rows = fixedRepository.findAll();

            assertThat(rows).noneMatch(TimeInterval::isInverted);
        }

        @Test
        @DisplayName("UTC: timestamps are correct as expected")
        void fixedRead_utcTimezone_correctTimestamps() {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            List<TimeInterval> rows = fixedRepository.findAll();

            assertThat(rows).noneMatch(TimeInterval::isInverted);
        }
    }
}
