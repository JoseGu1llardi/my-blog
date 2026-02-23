package com.joseguillard.my_blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private final AppProperties appProperties;

    public OpenApiConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blog REST API")
                        .description("Complete REST API for blog management")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Jose Wellington Ribeiro")
                                .email("junior11_junior@hotmail.com")
                                .url("https://seublog.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(appProperties.baseUrl())
                                .description("Server URL")
                ));
    }
}
