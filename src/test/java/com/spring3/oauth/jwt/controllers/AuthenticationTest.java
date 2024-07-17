package com.spring3.oauth.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring3.oauth.jwt.dtos.AuthRequestDTO;
import com.spring3.oauth.jwt.dtos.RefreshTokenRequestDTO;
import com.spring3.oauth.jwt.models.RefreshToken;
import com.spring3.oauth.jwt.models.UserInfo;
import com.spring3.oauth.jwt.services.JwtService;
import com.spring3.oauth.jwt.services.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Zeeshan Adil
 * User:mhmdz
 * Date:14-07-2024
 * Time:22:37
 */

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;


    // Login api integration test
    @Test
    void authenticateAndGetToken_Login_Success_Test() throws Exception {
        // given
        AuthRequestDTO authRequestDTO = new AuthRequestDTO("testUser", "password");

        // Mock JwtService behavior to return a mock access token
        String mockAccessToken = "mockAccessToken";
        when(jwtService.GenerateToken(authRequestDTO.getUsername())).thenReturn(mockAccessToken);

        // Mock authentication manager to return authenticated token
        UserDetails userDetails = new User(authRequestDTO.getUsername(), authRequestDTO.getPassword(), new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // Mock RefreshTokenService to return a mock RefreshToken
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mockRefreshToken");
        when(refreshTokenService.createRefreshToken(authRequestDTO.getUsername())).thenReturn(mockRefreshToken);

        // when and then
        mockMvc.perform(post("/api/v1/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(authRequestDTO))).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value(mockAccessToken)) // Assert mock access token
                .andExpect(jsonPath("$.token").value(mockRefreshToken.getToken())); // Assert mock refresh token

    }

    @Test
    void refreshToken_Success_Test() throws Exception {
        // Given
        String token = "mockTokenUUID";
        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO(token);

        // Create and set up the mock RefreshToken
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(token);
        mockRefreshToken.setExpiryDate(Instant.now().plusMillis(600000)); // Set expiry date in the future
        UserInfo mockUser = new UserInfo();
        mockUser.setUsername("testUser");
        mockRefreshToken.setUserInfo(mockUser);

        // Mock RefreshTokenService to return a mock RefreshToken
        when(refreshTokenService.findByToken(token)).thenReturn(Optional.of(mockRefreshToken));
        when(refreshTokenService.verifyExpiration(mockRefreshToken)).thenReturn(mockRefreshToken);

        // Mock JwtService behavior to return a mock access token
        String mockAccessToken = "mockAccessToken";
        when(jwtService.GenerateToken(mockUser.getUsername())).thenReturn(mockAccessToken);

        // When & Then
        mockMvc.perform(post("/api/v1/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(mockAccessToken))
                .andExpect(jsonPath("$.token").value(token));

    }





}