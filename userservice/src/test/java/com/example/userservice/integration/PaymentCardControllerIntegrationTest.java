package com.example.userservice.integration;

import com.example.userservice.dto.PayCardDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.repository.PaymentCardRepository;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentCardControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    private UserDto userDto;
    private PayCardDto cardDto;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        userDto = UserDto.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.cards@example.com")
                .build();

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);
        userId = created.getId();

        cardDto = PayCardDto.builder()
                .userId(userId)
                .number("1234567890123456")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 12, 31))
                .build();
    }

    @Test
    void createCard_Success() throws Exception {
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("John Doe"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createCard_UserNotFound_ReturnsNotFound() throws Exception {
        cardDto.setUserId(999L);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }

    @Test
    void createCard_MaxCardsExceeded_ReturnsBadRequest() throws Exception {
        for (int i = 0; i < 5; i++) {
            PayCardDto card = PayCardDto.builder()
                    .userId(userId)
                    .number(String.format("%016d", i + 1))
                    .holder("John Doe")
                    .expirationDate(LocalDate.of(2026, 12, 31))
                    .build();

            mockMvc.perform(post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Max cards exceeded: 5"));
    }

    @Test
    void getCardById_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PayCardDto created = objectMapper.readValue(response, PayCardDto.class);

        mockMvc.perform(get("/api/v1/cards/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("John Doe"));
    }

    @Test
    void getCardsByUserId_Success() throws Exception {
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateCard_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PayCardDto created = objectMapper.readValue(response, PayCardDto.class);
        created.setHolder("Jane Doe");

        mockMvc.perform(put("/api/v1/cards/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("Jane Doe"));
    }

    @Test
    void activateCard_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PayCardDto created = objectMapper.readValue(response, PayCardDto.class);

        mockMvc.perform(patch("/api/v1/cards/{id}/activate", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateCard_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PayCardDto created = objectMapper.readValue(response, PayCardDto.class);

        mockMvc.perform(patch("/api/v1/cards/{id}/deactivate", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteCard_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PayCardDto created = objectMapper.readValue(response, PayCardDto.class);

        mockMvc.perform(delete("/api/v1/cards/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cards/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}