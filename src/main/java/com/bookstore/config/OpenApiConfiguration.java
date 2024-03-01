package com.bookstore.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@Configuration
@OpenAPIDefinition(info = @Info(title = "BookStore API", version = "v1"))
@SecurityScheme(name = "bearerAuth", type = HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfiguration {
}

