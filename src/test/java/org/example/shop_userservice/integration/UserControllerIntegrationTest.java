package org.example.shop_userservice.integration;

import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.patchDto.UserPatchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser(email))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("bob@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@test.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createShouldReturn400_whenEmailIsMissing() throws Exception {
        UserDto user = validUser(null);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createShouldReturn400_whenEmailAlreadyExists() throws Exception {
        createUser("duplicate@test.com");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser("duplicate@test.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetUserById() throws Exception {
        UserDto created = createUser("get@test.com");

        mockMvc.perform(get("/api/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.email").value("get@test.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getShouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateUser() throws Exception {
        UserDto created = createUser("update@test.com");

        created.setSurname("Marley");

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .with(csrf())
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDeleteUser() throws Exception {
        UserDto created = createUser("delete@test.com");

        mockMvc.perform(delete("/api/users/{id}", created.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles = "USER")
    void getAllUsers_byNameAndSurname_returnsPage() throws Exception {
        UserDto requestUser = new UserDto();
        requestUser.setName("John");
        requestUser.setSurname("Doe");
        requestUser.setBirthDate(LocalDate.of(2000, 1, 1));
        requestUser.setEmail("John.Doe@test.com"); requestUser.setActive(true);

        String response = mockMvc.perform(post("/api/users") .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto created = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(get("/api/users")
                        .param("name", "John")
                        .param("surname", "Doe") .param("page", "0")
                        .param("size", "10")) .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(created.getId()))
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].surname").value("Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
