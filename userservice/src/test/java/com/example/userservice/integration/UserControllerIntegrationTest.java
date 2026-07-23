package com.example.userservice.integration;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

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

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userDto = UserDto.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.test@example.com")
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.test@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createUser_EmailAlreadyExists_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists: john.test@example.com"));
    }

    @Test
    void createUser_ValidationFailed_ReturnsBadRequest() throws Exception {
        userDto.setEmail("invalid-email");
        userDto.setName("");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void getUserById_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/api/v1/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.test@example.com"));
    }

    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    void getAllUsers_ReturnsPage() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllUsers_WithFilters_ReturnsFiltered() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/users")
                        .param("name", "Jane")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void updateUser_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);
        created.setName("Jane");
        created.setSurname("Smith");

        mockMvc.perform(put("/api/v1/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"));
    }

    @Test
    void activateUser_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(patch("/api/v1/users/{id}/activate", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateUser_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteUser_Success() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(delete("/api/v1/users/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}