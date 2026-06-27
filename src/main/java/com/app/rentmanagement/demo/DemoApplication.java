package com.app.rentmanagement.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DemoApplication.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner createTable(JdbcTemplate jdbcTemplate) {
		return args -> {
			String createExpensesTable = "CREATE TABLE IF NOT EXISTS expenses (" +
					"expense_id SERIAL PRIMARY KEY, " +
					"description VARCHAR(255) NOT NULL, " +
					"amount NUMERIC(19, 2) NOT NULL, " +
					"expense_date DATE NOT NULL, " +
					"category VARCHAR(255) NOT NULL, " +
					"flat_id BIGINT, " +
					"created_at TIMESTAMP, " +
					"updated_at TIMESTAMP" +
					");";
			jdbcTemplate.execute(createExpensesTable);
			
			String createRevisionsTable = "CREATE TABLE IF NOT EXISTS rent_revisions (" +
					"revision_id SERIAL PRIMARY KEY, " +
					"renter_id BIGINT NOT NULL, " +
					"effective_date DATE NOT NULL, " +
					"rent_amount NUMERIC(19, 2) NOT NULL, " +
					"created_at TIMESTAMP, " +
					"updated_at TIMESTAMP" +
					");";
			jdbcTemplate.execute(createRevisionsTable);
		};
	}
}
