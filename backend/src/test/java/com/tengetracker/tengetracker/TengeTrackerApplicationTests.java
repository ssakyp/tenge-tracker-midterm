package com.tengetracker.tengetracker;

import com.jayway.jsonpath.JsonPath;
import com.tengetracker.TengeTrackerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Runs against in-memory H2 (see src/test/resources/application.yaml), no Docker needed. */
@SpringBootTest(classes = TengeTrackerApplication.class)
@AutoConfigureMockMvc
class TengeTrackerApplicationTests {

    @Autowired
    private MockMvc mvc;

    private String signup(String username) throws Exception {
        String body = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value(username))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    @Test
    void publicAndProtectedEndpoints() throws Exception {
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"type\":\"EXPENSE\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/accounts").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void loginOwnershipAndPasswordChange() throws Exception {
        String alice = signup("alice_test");
        String bob = signup("bob_test");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice_test\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        String created = mvc.perform(post("/api/accounts").header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice wallet\",\"balance\":1000,\"currency\":\"KZT\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer accountId = JsonPath.read(created, "$.id");

        mvc.perform(get("/api/accounts/" + accountId).header("Authorization", "Bearer " + bob))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/accounts/" + accountId).header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));

        String changed = mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"newPassword456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newToken = JsonPath.read(changed, "$.accessToken");

        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + alice))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_test"));
    }
}
