package org.example.shopuserservice.integration;

import org.example.shopuserservice.model.dto.UserDto;
import org.example.shopuserservice.model.entities.User;
import org.example.shopuserservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "internal.internal-secret=test-secret",
        "internal.internal-secret=test-secret"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InternalUserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Value("${internal.service-secret}")
    private String serviceSecret;

    private RequestPostProcessor withServiceAuth() {
        return request -> {
            request.addHeader("X-Service-Auth", serviceSecret);
            return request;
        };
    }

    private UserDto validUser(String email) {
        UserDto user = new UserDto();
        user.setName("Bob");
        user.setSurname("Dylan");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setEmail(email);
        user.setActive(true);
        return user;
    }

    private UserDto createUser(String email) throws Exception {
        String response = mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser(email))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("bob@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@test.com"));
    }

    @Test
    void createShouldReturn400_whenEmailIsMissing() throws Exception {
        UserDto user = validUser(null);

        mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturnExistingUser_whenEmailAlreadyExists() throws Exception {
        UserDto firstResponse = createUser("duplicate@test.com");

        mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("duplicate@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstResponse.getId()))
                .andExpect(jsonPath("$.email").value("duplicate@test.com"));
    }

    @Test
    void createShouldReturn400_whenEmailIsInvalid() throws Exception {
        UserDto user = validUser("invalid-email");

        mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShouldReturn400_whenNameIsEmpty() throws Exception {
        UserDto user = validUser("empty@test.com");
        user.setName("");

        mockMvc.perform(post("/api/internal/users")
                        .with(withServiceAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetUserById() throws Exception {
        UserDto created = createUser("get@test.com");

        mockMvc.perform(get("/api/internal/users/{id}", created.getId())
                        .with(withServiceAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.email").value("get@test.com"));
    }

    @Test
    void getShouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", 999)
                        .with(withServiceAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_whenServiceAuthHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("noauth@test.com"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenServiceAuthHeaderIsInvalid() throws Exception {
        mockMvc.perform(post("/api/internal/users")
                        .header("X-Service-Auth", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("invalid@test.com"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenGettingUserWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", 999))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUserById_WhenUserExists_ShouldReturnNoContent() throws Exception {
        UserDto createdUser = createUser("delete@test.com");
        Long userId = createdUser.getId();

        mockMvc.perform(delete("/api/internal/users/{id}", userId)
                        .with(withServiceAuth()))
                .andExpect(status().isNoContent());

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void deleteUserById_WhenUserDoesNotExist_ShouldReturnNoContent() throws Exception {
        Long nonExistentId = 9999L;

        mockMvc.perform(delete("/api/internal/users/{id}", nonExistentId)
                        .with(withServiceAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserById_ShouldBeIdempotent_WhenCalledMultipleTimes() throws Exception {
        UserDto createdUser = createUser("idempotent@test.com");
        Long userId = createdUser.getId();

        mockMvc.perform(delete("/api/internal/users/{id}", userId)
                        .with(withServiceAuth()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/internal/users/{id}", userId)
                        .with(withServiceAuth()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/internal/users/{id}", userId)
                        .with(withServiceAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserById_WithoutAuth_ShouldReturnForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/internal/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }

}
