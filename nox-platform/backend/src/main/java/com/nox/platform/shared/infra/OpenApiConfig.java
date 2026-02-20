package com.nox.platform.shared.infra;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI noxPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("NOX Platform API")
                        .description("Nox Simulation & Code Generation Platform API")
                        .version("v0.0.1"));
    }
}
