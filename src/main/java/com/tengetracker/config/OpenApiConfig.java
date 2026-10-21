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
                                Tenge Tracker is a REST API for managing financial accounts and recording income, expenses, and transfers.

                                ## How to use this documentation
                                Expand an endpoint to see its purpose, parameters, request schema, sample payloads, response codes, and error examples. Use **Try it out** to send requests to the running application.

                                ## Typical workflow
                                1. Create an account with **POST /api/accounts**.
                                2. Copy the returned `id` from the response.
                                3. Record income with **POST /api/transactions** to add money, or record an expense to subtract money.
                                4. Use **GET /api/accounts** to verify account balances.

                                ## Transaction rules
                                - `INCOME` adds the amount to `accountId`.
                                - `EXPENSE` subtracts the amount only when sufficient funds are available.
                                - `TRANSFER` requires `targetAccountId` and moves the amount between the source and destination accounts.
                                - Amounts must be positive; transaction dates use `YYYY-MM-DD`.

                                ## Error responses
                                Validation and business-rule errors return HTTP 400. Requests for missing resources return HTTP 404. Error responses include a timestamp and status plus an error message or field-level validation errors.

                                ## Scope note
                                Accounts support list, retrieve, create, update, and delete operations. Transactions currently support list and create operations.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Tenge Tracker Project"))
                        .license(new License().name("Educational project")))
                .servers(List.of(new Server().url("http://localhost:3000").description("Local Docker Compose deployment")));
    }
}
