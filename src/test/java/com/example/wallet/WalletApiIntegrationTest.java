package com.example.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries()
                .forEach(entry ->
                        System.setProperty(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
    }
    @Test
    void createsWalletAndReturnsDetails() throws Exception {
        String body = "{\"name\":\"Test User\",\"email\":\"test-" + System.nanoTime() + "@example.com\",\"initialBalance\":1000.00}";
        String response = mockMvc.perform(post("/api/wallets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.walletId").isNumber())
                .andExpect(jsonPath("$.balance").value(1000.00)).andReturn().getResponse().getContentAsString();
        JsonNode wallet = objectMapper.readTree(response);
        mockMvc.perform(get("/api/wallets/" + wallet.get("walletId").asLong()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void rejectsZeroDeposit() throws Exception {
        mockMvc.perform(post("/api/wallets/1/deposit").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
