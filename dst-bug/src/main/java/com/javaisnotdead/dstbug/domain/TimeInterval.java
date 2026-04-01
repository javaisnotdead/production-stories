package com.javaisnotdead.dstbug.domain;

import org.joda.time.LocalDateTime;

public class TimeInterval {

    private final Long id;
    private final String description;
    private final LocalDateTime intervalStart;
    private final LocalDateTime intervalEnd;

    public TimeInterval(Long id, String description, LocalDateTime intervalStart, LocalDateTime intervalEnd) {
        this.id = id;
        this.description = description;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public LocalDateTime getIntervalStart() { return intervalStart; }
    public LocalDateTime getIntervalEnd() { return intervalEnd; }

    public boolean isInverted() {
        return intervalStart.isAfter(intervalEnd);
    }
}
