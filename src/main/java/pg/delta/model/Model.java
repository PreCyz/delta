package pg.delta.model;

import java.time.LocalDateTime;

public record Model(String id, LocalDateTime changeDateTime, ChangeType changeType, String details) { }
