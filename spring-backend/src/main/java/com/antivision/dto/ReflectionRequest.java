package com.antivision.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for the incoming HTTP request.
 * Represents the raw reflection or journal entry submitted by the user.
 */
public class ReflectionRequest {

    // @NotBlank ensures that the user cannot submit an empty string or just whitespace.
    // This is validated automatically because of the spring-boot-starter-validation dependency.
    @NotBlank(message = "Raw reflection cannot be empty")
    private String rawInput;

    public ReflectionRequest() {}

    public ReflectionRequest(String rawInput) {
        this.rawInput = rawInput;
    }

    public String getRawInput() {
        return rawInput;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }
}
