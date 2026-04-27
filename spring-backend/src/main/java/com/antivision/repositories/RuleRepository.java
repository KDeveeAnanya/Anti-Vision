package com.antivision.repositories;

import com.antivision.models.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    
    // Find all active rules
    List<Rule> findByIsActiveTrue();
    
    // Find all rules associated with a specific pattern ID
    List<Rule> findByPatternId(Long patternId);
}
