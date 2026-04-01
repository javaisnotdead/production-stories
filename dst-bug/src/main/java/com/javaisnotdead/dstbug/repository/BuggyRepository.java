package com.javaisnotdead.dstbug.repository;

import com.javaisnotdead.dstbug.domain.TimeInterval;
import org.joda.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class BuggyRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuggyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TimeInterval> findAll() {
        return jdbcTemplate.query(
            "SELECT id, description, interval_start, interval_end FROM dstdemo.dbo.time_intervals ORDER BY id",
            (rs, rowNum) -> {
                LocalDateTime intervalStart = LocalDateTime.fromDateFields(
                    (Date) JdbcUtils.getResultSetValue(rs, rs.findColumn("interval_start")));
                LocalDateTime intervalEnd = LocalDateTime.fromDateFields(
                    (Date) JdbcUtils.getResultSetValue(rs, rs.findColumn("interval_end")));

                return new TimeInterval(
                    rs.getLong("id"),
                    rs.getString("description"),
                    intervalStart,
                    intervalEnd
                );
            }
        );
    }
}
