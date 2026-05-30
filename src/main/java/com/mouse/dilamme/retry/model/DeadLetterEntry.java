package com.mouse.dilamme.retry.model;

import com.mouse.dilamme.retry.enums.DeadLetterReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * A row inserted here means the request exhausted all retries (or hit a terminal 4xx)
 * and will never be retried again. The original row in `requests` keeps status=FAILED
 * for query consistency; this table exists so dead jobs are separated, inspectable,
 * and replayable independently of the main queue.
 */
@Entity
@Table(name = "dead_letter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;


    @Column(nullable = false)
    private UUID requestId;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadLetterReason reason;

    @Column(nullable = false)
    private int totalAttempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    /** When the original request was first submitted */
    @Column(nullable = false)
    private Instant originalCreatedAt;

    /** When it landed in the dead-letter table */
    @Column(nullable = false)
    @Builder.Default // Forces the builder to respect this default value
    private Instant deadLetteredAt = Instant.now();
}