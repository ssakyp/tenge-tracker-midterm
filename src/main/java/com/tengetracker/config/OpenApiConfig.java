package com.tengetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tengeTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tenge Tracker REST API")
                        .description("""
                                ## Overview
                                Tenge Tracker is a REST API for managing accounts, categories, budgets and recording income, expenses, and transfers.

                                ## Data model (relationships)
                                - Account 1 -> N Transaction
                                - Category 1 -> N Transaction
                                - Category 1 -> N Budget

                                ## Typical workflow
                                1. Create an account (**POST /api/accounts**) and a category (**POST /api/categories**).
                                2. Record transactions (**POST /api/transactions**); balances update automatically.
                                3. Inspect related data: **GET /api/accounts/{id}/transactions**, **GET /api/categories/{id}/transactions**, **GET /api/categories/{id}/budgets**.
                                4. Track limits with **POST /api/budgets** and **GET /api/budgets/{id}/status**; see totals with **GET /api/summary**.

                                ## Transaction rules
                                - `INCOME` adds the amount to `accountId`.
                                - `EXPENSE` subtracts the amount only when sufficient funds are available.
                                - `TRANSFER` requires a different `targetAccountId` and moves the amount between accounts.
                                - Amounts must be positive; dates use `YYYY-MM-DD`.

                                ## Error responses
                                400 validation/business-rule errors, 404 missing resources, 409 conflicts (duplicate budget, deleting something still in use), 405 wrong method. Bodies contain a timestamp and status plus an `error` message or field-level `errors`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Tenge Tracker Project"))
                        .license(new License().name("Educational project")))
                .servers(List.of(new Server().url("http://localhost:3000").description("Local Docker Compose deployment")));
    }
}
