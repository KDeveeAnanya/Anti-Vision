package com.antivision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main application class for the Anti-Vision backend.
 * 
 * @SpringBootApplication is a convenience annotation that adds all of the following:
 * - @Configuration: Tags the class as a source of bean definitions for the application context.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the 'com.antivision' package, allowing it to find the controllers.
 */
@SpringBootApplication
@RestController
public class AntiVisionApplication {

	public static void main(String[] args) {
		// This method launches the application. It sets up the default configuration,
		// starts the Spring application context, and performs classpath scanning.
		SpringApplication.run(AntiVisionApplication.class, args);
	}

	/**
	 * A simple health check endpoint to verify the server is running.
	 * @RestController (at class level) combined with @GetMapping maps the HTTP GET request 
	 * for the root path ("/") to this method.
	 */
	@GetMapping("/")
	public String healthCheck() {
		return "Anti-Vision Backend is up and running!";
	}
}
