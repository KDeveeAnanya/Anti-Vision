package com.antivision.controllers;

import com.antivision.dto.ReflectionRequest;
import com.antivision.dto.RuleResponse;
import com.antivision.models.AntiVisionEntry;
import com.antivision.models.Pattern;
import com.antivision.models.Rule;
import com.antivision.repositories.AntiVisionEntryRepository;
import com.antivision.repositories.PatternRepository;
import com.antivision.repositories.RuleRepository;
import com.antivision.services.RuleEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller that exposes endpoints for the frontend to interact with.
 * 
 * @RestController is a convenience annotation combining @Controller and @ResponseBody.
 * It tells Spring that the methods in this class return domain objects directly written 
 * to the HTTP response as JSON, rather than rendering an HTML view.
 * 
 * @RequestMapping("/api") sets a base path for all endpoints in this controller.
 * 
 * @CrossOrigin(origins = "*") allows Cross-Origin Resource Sharing. This is critical
 * because our React frontend will likely run on a different port (e.g., 3000 or 5173),
 * and browsers block requests between different origins by default.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AntiVisionController {

    private final RuleEngineService ruleEngineService;
    private final PatternRepository patternRepository;
    private final RuleRepository ruleRepository;
    private final AntiVisionEntryRepository entryRepository;

    /**
     * @Autowired tells Spring to inject instances of these dependencies automatically.
     * We use Constructor Injection (instead of Field Injection) because it makes testing 
     * easier and ensures the controller cannot be instantiated without its required dependencies.
     */
    @Autowired
    public AntiVisionController(RuleEngineService ruleEngineService, 
                                PatternRepository patternRepository, 
                                RuleRepository ruleRepository, 
                                AntiVisionEntryRepository entryRepository) {
        this.ruleEngineService = ruleEngineService;
        this.patternRepository = patternRepository;
        this.ruleRepository = ruleRepository;
        this.entryRepository = entryRepository;
    }

    /**
     * Endpoint to analyze a new journal entry.
     * 
     * @PostMapping("/reflections") maps HTTP POST requests to this method.
     * @Valid triggers the validation annotations inside ReflectionRequest (like @NotBlank).
     * @RequestBody maps the incoming JSON payload into our ReflectionRequest Java object.
     */
    @PostMapping("/reflections")
    public ResponseEntity<RuleResponse> analyzeReflection(@Valid @RequestBody ReflectionRequest request) {
        
        // Step 1: Core Logic - Analyze the raw input using our service
        RuleResponse response = ruleEngineService.analyzeInput(request.getRawInput());
        
        // Step 2: Extract the pattern name (fallback if unknown)
        String patternName = "Unknown Trigger".equals(response.getTrigger()) 
                ? "Generic Pattern: " + System.currentTimeMillis() // Make it unique if generic
                : response.getTrigger();
        
        // Step 3: Database Interaction - Find existing pattern or create a new one
        Pattern pattern = patternRepository.findByName(patternName)
                .orElseGet(() -> {
                    Pattern newPattern = new Pattern(patternName, response.getConsequence());
                    return patternRepository.save(newPattern);
                });

        // Step 4: Database Interaction - Save the generated rule
        if (!response.getPreventiveRule().contains("Unknown Trigger")) {
            Rule newRule = new Rule(pattern, response.getPreventiveRule());
            ruleRepository.save(newRule);
        }

        // Step 5: Database Interaction - Log the reflection as an entry (failure instance)
        AntiVisionEntry entry = new AntiVisionEntry(
                pattern,
                request.getRawInput(), // We save the raw journal entry as the trigger context
                response.getConsequence(),
                false // Set to false because this reflection usually means they succumbed to the pattern
        );
        entryRepository.save(entry);

        // Step 6: Return the 200 OK status along with the JSON response
        return ResponseEntity.ok(response);
    }
}
