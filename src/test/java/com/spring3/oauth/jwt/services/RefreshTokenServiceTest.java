package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.RefreshToken;
import com.spring3.oauth.jwt.models.UserInfo;
import com.spring3.oauth.jwt.repositories.RefreshTokenRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Author: Zeeshan Adil
 * User:mhmdz
 * Date:17-07-2024
 * Time:22:24
 */

class RefreshTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRefreshToken_TestCase() {
        // Given
        UserInfo mockUser = new UserInfo();
        mockUser.setUsername("testUser");

        // Mocking userRepository to return a mock user
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(mockUser);

        // Mocking refreshTokenRepository to return the saved refresh token
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID().toString());
        mockRefreshToken.setExpiryDate(Instant.now().plusMillis(600000));
        mockRefreshToken.setUserInfo(mockUser);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

        // When
        RefreshToken createdRefreshToken = refreshTokenService.createRefreshToken(mockUser.getUsername());

        // Then
        assertNotNull(createdRefreshToken);
        assertEquals(mockUser, createdRefreshToken.getUserInfo());
        assertNotNull(createdRefreshToken.getToken());
        assertTrue(createdRefreshToken.getExpiryDate().isAfter(Instant.now()));

        // Verify that the save method was called on the repository
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }


    @Test
    void verifyExpiration_NotExpired_TestCase() {
        // Given
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID().toString());
        mockRefreshToken.setExpiryDate(Instant.now().plusMillis(600000)); // Set expiry date in the future

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(mockRefreshToken);

        // Then
        assertNotNull(result);
        assertEquals(mockRefreshToken, result);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_Expired_TestCase() {
        // Given
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID().toString());
        mockRefreshToken.setExpiryDate(Instant.now().minusMillis(600000)); // Set expiry date in the past

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiration(mockRefreshToken);
        });

        assertEquals(mockRefreshToken.getToken() + " Refresh token is expired. Please make a new login..!", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(mockRefreshToken);
    }

}