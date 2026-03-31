package com.swl.hr.system.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
	    info = @Info(
	        title = "HR System API",
	        version = "v1",
	        description = "API documentation for the HR System"
	    )
	)
public class SwaggerConfig {
}