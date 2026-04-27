package com.antivision.repositories;

import com.antivision.models.AntiVisionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AntiVisionEntryRepository extends JpaRepository<AntiVisionEntry, Long> {
    
    // Find all entries for a specific pattern
    List<AntiVisionEntry> findByPatternIdOrderByLoggedAtDesc(Long patternId);
    
    // Find all entries where a pattern was successfully avoided
    List<AntiVisionEntry> findByWasAvoidedTrueOrderByLoggedAtDesc();
    
    // Find all entries where the user gave in to the pattern
    List<AntiVisionEntry> findByWasAvoidedFalseOrderByLoggedAtDesc();
}
