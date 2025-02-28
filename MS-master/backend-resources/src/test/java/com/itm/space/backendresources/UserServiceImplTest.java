package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser(roles = "MODERATOR")
@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private Keycloak keycloak;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() throws Exception {
        userRequest = new UserRequest("testUser", "testUser@gmail.com", "testUser", "testUser", "testUser");
        if (!userExists(userRequest.getUsername())) {
            createUser(userRequest);
        }
    }

    @AfterEach
    void tearDown() {
        removeUserIfExists(userRequest.getUsername());
    }

    private boolean userExists(String username) {
        List<UserRepresentation> users = keycloak.realm("ITM").users().search(username);
        return !users.isEmpty();
    }

    private void createUser(UserRequest userRequest) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"username\":\"" + userRequest.getUsername() + "\",\"email\":\"" + userRequest.getEmail() + "\"}"))
                .andExpect(status().isOk());

        assertTrue(userExists(userRequest.getUsername()), "Пользователь не создан");
    }

    private void removeUserIfExists(String username) {
        List<UserRepresentation> users = keycloak.realm("ITM").users().search(username);
        if (!users.isEmpty()) {
            String id = users.get(0).getId();
            keycloak.realm("ITM").users().get(id).remove();
        }
    }

    @Test
    void createUserTest() {
        assertTrue(userExists(userRequest.getUsername()), "Пользователь должен существовать после setUp");
    }

    @Test
    void getUserByIdTest() throws Exception {
        List<UserRepresentation> users = keycloak.realm("ITM").users().search(userRequest.getUsername());
        assertFalse(users.isEmpty(), "Пользователь должен существовать");

        String userId = users.get(0).getId();

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk());
    }
}
