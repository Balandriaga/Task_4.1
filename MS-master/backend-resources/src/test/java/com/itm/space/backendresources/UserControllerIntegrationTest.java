package com.itm.space.backendresources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "moderatorUser", roles = {"MODERATOR"})
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequest validUserRequest;
    private UserRequest invalidUserRequest;
    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        validUserRequest = new UserRequest("username", "email@mail.ru", "password", "firstName", "lastName");
        invalidUserRequest = new UserRequest("1", "email", "123", "", "");
        userId = UUID.randomUUID();
        userResponse = new UserResponse("firstName", "lastName", "email@mail.ru", List.of("MODERATOR"), List.of("MODERATORS"));
    }

    @Test
    public void createValidUserTest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk());

    }

    @Test
    public void createInvalidUserTest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username should be between 2 and 30 characters long"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password should be greater than 4 characters long"));
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value(userResponse.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userResponse.getLastName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value("MODERATOR"))
                .andExpect(jsonPath("$.groups[0]").value("MODERATORS"));

    }

    @Test
    public void helloTest() throws Exception {
        mockMvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk());

    }
}
