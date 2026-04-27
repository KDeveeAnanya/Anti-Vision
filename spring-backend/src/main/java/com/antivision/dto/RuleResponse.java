package com.antivision.dto;

/**
 * Data Transfer Object (DTO) for returning the analyzed rule engine results.
 * We use a DTO here instead of directly returning an Entity because this represents 
 * processed/transient data from user input before it is necessarily saved to the database.
 */
public class RuleResponse {
    
    private String trigger;
    private String emotion;
    private String consequence;
    private String preventiveRule;
    private String earlyWarning;

    public RuleResponse() {}

    public RuleResponse(String trigger, String emotion, String consequence, String preventiveRule, String earlyWarning) {
        this.trigger = trigger;
        this.emotion = emotion;
        this.consequence = consequence;
        this.preventiveRule = preventiveRule;
        this.earlyWarning = earlyWarning;
    }

    // Getters and Setters

    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public String getConsequence() { return consequence; }
    public void setConsequence(String consequence) { this.consequence = consequence; }

    public String getPreventiveRule() { return preventiveRule; }
    public void setPreventiveRule(String preventiveRule) { this.preventiveRule = preventiveRule; }

    public String getEarlyWarning() { return earlyWarning; }
    public void setEarlyWarning(String earlyWarning) { this.earlyWarning = earlyWarning; }
}
