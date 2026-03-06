package org.example.shopuserservice.integration;

import org.example.shopuserservice.model.dto.UserDto;
import org.example.shopuserservice.model.patchdto.UserPatchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@TestPropertySource(properties = {
        "internal.service-secret=test-secret",
        "internal.internal-secret=test-secret"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${internal.internal-secret}")
    private String internalSecret;

    private RequestPostProcessor withUserHeaders(Long userId, String... roles) {
        return request -> {
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-Roles", "ROLE_" + String.join(",ROLE_", roles));
            request.addHeader("X-Internal-Auth", internalSecret);
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
        String response = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser(email))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("bob@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturn400_whenEmailIsMissing() throws Exception {
        UserDto user = validUser(null);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturn400_whenEmailAlreadyExists() throws Exception {
        createUser("duplicate@test.com");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("duplicate@test.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetUserById() throws Exception {
        UserDto created = createUser("get@test.com");

        mockMvc.perform(get("/api/users/{id}", created.getId())
                        .with(withUserHeaders(created.getId(), "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.email").value("get@test.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getShouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999)
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateUser() throws Exception {
        UserDto created = createUser("update@test.com");

        created.setSurname("Marley");

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(created.getId(), "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Marley"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateShouldReturn404_whenUserDoesNotExist() throws Exception {
        UserDto user = validUser("missing@test.com");
        user.setId(999L);

        mockMvc.perform(put("/api/users/{id}", 999)
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldPatchUser() throws Exception {
        UserDto created = createUser("patch@test.com");

        UserPatchDto patch = new UserPatchDto();
        patch.setActive(false);

        mockMvc.perform(patch("/api/users/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(created.getId(), "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void patchShouldReturn404_whenUserDoesNotExist() throws Exception {
        UserPatchDto patch = new UserPatchDto();
        patch.setActive(false);

        mockMvc.perform(patch("/api/users/{id}", 999)
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        UserDto created = createUser("delete@test.com");

        mockMvc.perform(delete("/api/users/{id}", created.getId())
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_byNameAndSurname_returnsPage() throws Exception {
        UserDto requestUser = new UserDto();
        requestUser.setName("John");
        requestUser.setSurname("Doe");
        requestUser.setBirthDate(LocalDate.of(2000, 1, 1));
        requestUser.setEmail("John.Doe@test.com");
        requestUser.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/api/users")
                        .with(withUserHeaders(1L, "ADMIN"))
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(created.getId()))
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].surname").value("Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_byName_returnsPage() throws Exception {
        UserDto requestUser = new UserDto();
        requestUser.setName("Tate");
        requestUser.setSurname("McRae");
        requestUser.setBirthDate(LocalDate.of(2000, 1, 1));
        requestUser.setEmail("Tate.McRae1@test.com");
        requestUser.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/api/users")
                        .with(withUserHeaders(1L, "ADMIN"))
                        .param("name", "Tate")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(created.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Tate"))
                .andExpect(jsonPath("$.content[0].surname").value("McRae"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_bySurname_returnsPage() throws Exception {
        UserDto requestUser = new UserDto();
        requestUser.setName("Sabrina");
        requestUser.setSurname("Carpenter");
        requestUser.setBirthDate(LocalDate.of(2000, 1, 1));
        requestUser.setEmail("Sabrina.Carpenter1@test.com");
        requestUser.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(withUserHeaders(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/api/users")
                        .with(withUserHeaders(1L, "ADMIN"))
                        .param("surname", "carpenter")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(created.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Sabrina"))
                .andExpect(jsonPath("$.content[0].surname").value("Carpenter"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
