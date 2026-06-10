/*
 * Copyright 2026 Aditya Patankar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.batchpilot.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Persists and reads the action audit log in a dedicated {@code BATCH_PILOT_AUDIT}
 * table, created on first use. Stores no credentials — only the principal name.
 */
public class AuditService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String DDL = """
            CREATE TABLE IF NOT EXISTS BATCH_PILOT_AUDIT (
                ID VARCHAR(36) PRIMARY KEY,
                EVENT_TIME TIMESTAMP NOT NULL,
                PRINCIPAL VARCHAR(255),
                ACTION VARCHAR(32) NOT NULL,
                TARGET VARCHAR(255),
                RESULT VARCHAR(16) NOT NULL,
                MESSAGE VARCHAR(2000)
            )
            """;

    private static final RowMapper<AuditEventDto> ROW_MAPPER = (rs, rowNum) -> {
        java.sql.Timestamp ts = rs.getTimestamp("EVENT_TIME");
        return new AuditEventDto(
                rs.getString("ID"),
                ts == null ? null : ts.toLocalDateTime().format(ISO),
                rs.getString("PRINCIPAL"),
                rs.getString("ACTION"),
                rs.getString("TARGET"),
                rs.getString("RESULT"),
                rs.getString("MESSAGE"));
    };

    private final JdbcTemplate jdbc;

    public AuditService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.jdbc.execute(DDL);
    }

    /** Records a single action outcome. */
    public void record(String principal, AuditAction action, String target, boolean success, String message) {
        jdbc.update(
                "INSERT INTO BATCH_PILOT_AUDIT (ID, EVENT_TIME, PRINCIPAL, ACTION, TARGET, RESULT, MESSAGE) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID().toString(),
                java.sql.Timestamp.valueOf(LocalDateTime.now()),
                principal,
                action.name(),
                target,
                success ? "SUCCESS" : "FAILURE",
                truncate(message, 2000));
    }

    /** Most recent audit events first, capped at {@code limit}. */
    public List<AuditEventDto> list(int limit) {
        int capped = Math.min(Math.max(1, limit), 500);
        return jdbc.query(
                "SELECT ID, EVENT_TIME, PRINCIPAL, ACTION, TARGET, RESULT, MESSAGE "
                        + "FROM BATCH_PILOT_AUDIT ORDER BY EVENT_TIME DESC, ID DESC LIMIT ?",
                ROW_MAPPER,
                capped);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
