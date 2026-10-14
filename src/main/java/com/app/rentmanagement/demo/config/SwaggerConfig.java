package com.app.rentmanagement.demo.config;



import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI rentManagementOpenApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Rent Management API")
                        .description("Rent Management System APIs")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vaibhav Sandbhor")
                                .email("vaibhavsandbhor33@gmail.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation"));
    }
}