package com.antivision.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a log entry when a user encounters a pattern.
 * This tracks the real-world behavior and the consequence of giving in to or avoiding the pattern.
 */
@Entity
@Table(name = "anti_vision_entries")
public class AntiVisionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each entry is associated with a specific Pattern
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private Pattern pattern;

    @Column(name = "trigger_description", nullable = false, columnDefinition = "TEXT")
    private String triggerDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String consequence;

    @Column(name = "was_avoided", nullable = false)
    private boolean wasAvoided;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }

    public AntiVisionEntry() {}

    public AntiVisionEntry(Pattern pattern, String triggerDescription, String consequence, boolean wasAvoided) {
        this.pattern = pattern;
        this.triggerDescription = triggerDescription;
        this.consequence = consequence;
        this.wasAvoided = wasAvoided;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pattern getPattern() { return pattern; }
    public void setPattern(Pattern pattern) { this.pattern = pattern; }

    public String getTriggerDescription() { return triggerDescription; }
    public void setTriggerDescription(String triggerDescription) { this.triggerDescription = triggerDescription; }

    public String getConsequence() { return consequence; }
    public void setConsequence(String consequence) { this.consequence = consequence; }

    public boolean isWasAvoided() { return wasAvoided; }
    public void setWasAvoided(boolean wasAvoided) { this.wasAvoided = wasAvoided; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
