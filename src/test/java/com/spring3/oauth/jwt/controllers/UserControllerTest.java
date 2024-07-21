package com.spring3.oauth.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.oauth.jwt.dtos.AuthRequestDTO;
import com.spring3.oauth.jwt.dtos.UserRequest;
import com.spring3.oauth.jwt.dtos.UserResponse;
import com.spring3.oauth.jwt.models.UserRole;
import com.spring3.oauth.jwt.services.JwtService;
import com.spring3.oauth.jwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Author: Zeeshan Adil
 * User:mhmdz
 * Date:13-07-2024
 * Time:20:25
 */

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;


    @BeforeEach
    void setUp() {
        // generate the jwt token for testing
        // username must be exist
        // we will generate real token and mock the service which also cover the jwt token generation test case
        jwtToken = jwtService.GenerateToken("username123");
    }

    @Test
    void saveUser_Success_Test() throws Exception {
        // given
        UserRole role1 = new UserRole(1L, "ROLE_USER");
        UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testUser");
        userRequest.setPassword("password123");
        userRequest.setRoles(roles);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testUser");
        userResponse.setRoles(roles);

        // mocking userService behavior
        when(userService.saveUser(any(UserRequest.class))).thenReturn(userResponse);

        // when and then
        mockMvc.perform(post("/api/v1/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1].name").value("ROLE_USER"));

    }

    @Test
    void updateUser_Success_Test() throws Exception {
        // given
        UserRole role1 = new UserRole(1L, "ROLE_USER");
        UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        UserRequest userRequest = new UserRequest();
        userRequest.setId(1L);
        userRequest.setUsername("testUser");
        userRequest.setPassword("password123");
        userRequest.setRoles(roles);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testUser");
        userResponse.setRoles(roles);

        // mocking userService behavior
        when(userService.saveUser(any(UserRequest.class))).thenReturn(userResponse);

        // when and then
        mockMvc.perform(post("/api/v1/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1].name").value("ROLE_USER"));

    }

    @Test
    void getAllUsers_Success_Test() throws Exception {
        // given
        UserRole role1 = new UserRole(1L, "ROLE_USER");
        UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        UserResponse userResponse1 = new UserResponse(1L, "user1", roles);
        UserResponse userResponse2 = new UserResponse(2L, "user2", roles);
        UserResponse userResponse3 = new UserResponse(3L, "user3", roles);

        List<UserResponse> userResponses = Arrays.asList(userResponse1, userResponse2, userResponse3);

        // mocking userService behavior
        when(userService.getAllUser()).thenReturn(userResponses);

        // when and then
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[0].roles[1].name").value("ROLE_USER"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1].roles[1].name").value("ROLE_USER"))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].username").value("user3"))
                .andExpect(jsonPath("$[2].roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[2].roles[1].name").value("ROLE_USER"));

    }

    @Test
    void getLoggedInUserProfile_Success_Test() throws Exception {

        // given
        UserRole role1 = new UserRole(1L, "ROLE_USER");
        UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        UserResponse userResponse = new UserResponse(1L, "user1", roles);

        // mocking userService behavior
        when(userService.getLoggedInUserProfile()).thenReturn(userResponse);

        // when and then
        mockMvc.perform(post("/api/v1/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1].name").value("ROLE_USER"));

    }

    @Test
    void getUserById_Success_Test() throws Exception {
        // given
        UserRole role1 = new UserRole(1L, "ROLE_USER");
        UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        UserResponse userResponse = new UserResponse(1L, "user1", roles);

        // mocking userService behavior
        when(userService.getUserById(1L)).thenReturn(userResponse);

        // when and then
        mockMvc.perform(get("/api/v1/user/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1].name").value("ROLE_USER"));
    }

    @Test
    void deleteUserById_Success_Test() throws Exception {
        // Given
        Long userId = 1L;

        // Mocking userService to return userId when deleteUserById is called
        when(userService.deleteUserById(userId)).thenReturn(userId);

        // When & Then
        mockMvc.perform(delete("/api/v1/delete/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(userId.toString())); // Assertion for raw Long response

    }




//    @Test
//    void getUserById_UserNotFound_Test() throws Exception {
//        // given
//        Long userId = 1L;
//
//        // mocking userService behavior to throw RuntimeException when user not found
//        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found with id: " + userId));
//
//        // when and then
//        mockMvc.perform(get("/api/v1/user/1", userId)
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error").value("Internal Server Error"))
//                .andExpect(jsonPath("$.message").value("User not found with id: " + userId));
//    }

//    @Test
//    void getUserById_NullId_Test() throws Exception {
//        // given
//        Long userId = null;
//
//        // mocking userService behavior to throw IllegalArgumentException when id is null
//        when(userService.getUserById(userId)).thenThrow(new IllegalArgumentException("Id cannot be null"));
//
//        // when and then
//        mockMvc.perform(get("/api/v1/user/{id}", userId)
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error").value("Internal Server Error"))
//                .andExpect(jsonPath("$.message").value("Id cannot be null"));
//    }






    }