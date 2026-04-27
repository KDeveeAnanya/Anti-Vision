package com.antivision.repositories;

import com.antivision.models.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Pattern entity.
 * Extending JpaRepository provides standard CRUD operations automatically.
 */
@Repository
public interface PatternRepository extends JpaRepository<Pattern, Long> {
    
    // We can define custom query methods here simply by naming them correctly.
    // Spring Data JPA will automatically parse the method name and generate the SQL.
    
    // Example: Find a pattern exactly by its unique name
    Optional<Pattern> findByName(String name);
    
    // Example: Find patterns that contain a specific word in their name (case-insensitive)
    Iterable<Pattern> findByNameContainingIgnoreCase(String keyword);
}
