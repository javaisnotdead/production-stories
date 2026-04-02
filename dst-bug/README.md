# DST Bug Demo

Demo application for the article [The Hour That Doesn't Exist](https://www.javaisnotdead.com/the-hour-that-doesnt-exist/).

Demonstrates a DST bug in JDBC timestamp retrieval from SQL Server `datetime2` columns.

## The Bug

When reading `datetime2` values via `JdbcUtils.getResultSetValue()`, the MSSQL JDBC driver interprets the raw value as a local time in the JVM timezone. Near the DST boundary, this produces inconsistent UTC offsets - and for records crossing the gap, **inverted timestamps** where `intervalStart > intervalEnd`.

The fix: `rs.getObject(col, java.time.LocalDateTime.class)` - JDBC 4.2+ returns the raw value without any timezone conversion.

## Stack

- Java 25, Spring Boot 4
- SQL Server 2022 (`datetime2` columns, UTC values)
- Joda-Time 2.x (matches production code in the article)
- JVM timezone: `Europe/Warsaw` (CET/CEST, DST on 2025-03-30)

## Running

```bash
docker compose up --build
```

Then open [http://localhost:8080](http://localhost:8080).

The UI shows two tables with the same 4 records:
- **Top table** - buggy read path: `JdbcUtils.getResultSetValue()` - timestamps distorted near DST boundary, last record inverted
- **Bottom table** - fixed read path: `rs.getObject(col, java.time.LocalDateTime.class)` - UTC values as stored

## Test Data

All timestamps stored as UTC in `datetime2`. DST transition: `2025-03-30 01:00 UTC` (local `02:00 CET → 03:00 CEST`).

| Description | intervalStart (UTC) | intervalEnd (UTC) |
|---|---|---|
| Before DST window | 00:20 | 00:40 |
| Approaching transition | 01:30 | 01:45 |
| At boundary | 01:58 | 02:01 |
| Gap start, CEST end | 02:59 | 03:00 |

The last record produces the inversion: `02:59` falls in the local DST gap (treated as `02:59 Warsaw` → normalized to `03:59 CEST`), while `03:00` is read as valid CEST (`03:00`). Result: `03:59 > 03:00`.
