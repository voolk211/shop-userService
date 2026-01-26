package org.example.shop_userservice.integration;

import org.example.shop_userservice.model.dto.CardDto;
import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.patchDto.CardPatchDto;
import org.example.shop_userservice.model.patchDto.UserPatchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedOutputStream;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto createUser(String email) throws Exception {
        UserDto user = new UserDto();
        user.setName("Bob");
        user.setSurname("Dylan");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setEmail(email);
        user.setActive(true);

        String response = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    private CardDto createCard(Long userId, String number) throws Exception {
        CardDto card = new CardDto();
        card.setUserId(userId);
        card.setNumber(number);
        card.setHolder("Bob Dylan");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        String response = mockMvc.perform(post("/api/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, CardDto.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateCard() throws Exception {
        UserDto user = createUser("card@test.com");

        CardDto card = new CardDto();
        card.setUserId(user.getId());
        card.setNumber("1111222233334444");
        card.setHolder("Bob Dylan");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        mockMvc.perform(post("/api/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createShouldReturn400_whenUserDoesNotExist() throws Exception {
        CardDto card = new CardDto();
        card.setUserId(999L);
        card.setNumber("1111222233334445");
        card.setHolder("Bob");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        mockMvc.perform(post("/api/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetCardById() throws Exception {
        UserDto user = createUser("getcard@test.com");
        CardDto created = createCard(user.getId(), "2222333344445555");

        mockMvc.perform(get("/api/cards/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.number").value("2222333344445555"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getShouldReturn404_whenCardDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateCard() throws Exception {
        UserDto user = createUser("updatecard@test.com");
        CardDto created = createCard(user.getId(), "3333444455556666");

        created.setHolder("Updated Holder");

        mockMvc.perform(put("/api/cards/{id}", created.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("Updated Holder"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateShouldReturn400_whenIdMismatch() throws Exception {
        UserDto user = createUser("mismatch@test.com");
        CardDto created = createCard(user.getId(), "4444555566667777");

        created.setId(123L);

        mockMvc.perform(put("/api/cards/{id}", created.getId() + 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldPatchCard() throws Exception {
        UserDto user = createUser("patchcard@test.com");
        CardDto created = createCard(user.getId(), "5555666677778888");

        CardPatchDto patch = new CardPatchDto();
        patch.setActive(false);

        mockMvc.perform(patch("/api/cards/{id}", created.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void patchShouldReturn404_whenCardDoesNotExist() throws Exception {
        CardPatchDto patch = new CardPatchDto();
        patch.setActive(false);

        mockMvc.perform(patch("/api/cards/{id}", 999)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCardsByUserNameAndSurname_returnsPage() throws Exception {

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

        CardDto card = new CardDto();
        card.setUserId(created.getId());
        card.setNumber("1234123442124215");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        String responseCard = mockMvc.perform(post("/api/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/cards")
                        .param("name", created.getName())
                        .param("surname", created.getSurname())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDeleteCard() throws Exception {
        UserDto user = createUser("deletecard@test.com");
        CardDto created = createCard(user.getId(), "7777888899990000");

        mockMvc.perform(delete("/api/cards/{id}", created.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
