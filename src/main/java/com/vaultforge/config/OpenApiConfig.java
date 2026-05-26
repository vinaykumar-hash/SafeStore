package com.vaultforge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "VaultForge API", version = "v1", description = "Distributed backup platform APIs")
)
public class OpenApiConfig {}
