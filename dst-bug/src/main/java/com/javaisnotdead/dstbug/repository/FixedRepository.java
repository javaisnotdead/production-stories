package com.javaisnotdead.dstbug.repository;

import com.javaisnotdead.dstbug.domain.TimeInterval;
import org.joda.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FixedRepository {

    private final JdbcTemplate jdbcTemplate;

    public FixedRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TimeInterval> findAll() {
        return jdbcTemplate.query(
            "SELECT id, description, interval_start, interval_end FROM dstdemo.dbo.time_intervals ORDER BY id",
            (rs, rowNum) -> {
                LocalDateTime intervalStart = toJoda(
                    rs.getObject(rs.findColumn("interval_start"), java.time.LocalDateTime.class));
                LocalDateTime intervalEnd = toJoda(
                    rs.getObject(rs.findColumn("interval_end"), java.time.LocalDateTime.class));

                return new TimeInterval(
                    rs.getLong("id"),
                    rs.getString("description"),
                    intervalStart,
                    intervalEnd
                );
            }
        );
    }

    private static LocalDateTime toJoda(java.time.LocalDateTime ldt) {
        return new LocalDateTime(
            ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
            ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano() / 1_000_000
        );
    }
}
