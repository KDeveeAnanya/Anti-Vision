package com.antivision.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a negative life pattern that the user wants to avoid.
 */
@Entity
@Table(name = "patterns")
public class Pattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // A Pattern can have multiple Rules (preventive measures)
    @OneToMany(mappedBy = "pattern", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rule> rules;

    // A Pattern can have multiple Anti-Vision entries (instances where it occurred or was avoided)
    @OneToMany(mappedBy = "pattern", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AntiVisionEntry> entries;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Default constructor is required by JPA
    public Pattern() {}

    public Pattern(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Rule> getRules() { return rules; }
    public void setRules(List<Rule> rules) { this.rules = rules; }

    public List<AntiVisionEntry> getEntries() { return entries; }
    public void setEntries(List<AntiVisionEntry> entries) { this.entries = entries; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
