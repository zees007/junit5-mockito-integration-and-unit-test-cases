package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.dtos.UserRequest;
import com.spring3.oauth.jwt.dtos.UserResponse;
import com.spring3.oauth.jwt.models.UserInfo;
import com.spring3.oauth.jwt.models.UserRole;
import com.spring3.oauth.jwt.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Author: Zeeshan Adil
 * User:mhmdz
 * Date:13-07-2024
 * Time:13:53
 */

class UserServiceImplTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private SecurityContext securityContext;

        @Mock
        private Authentication authentication;

        @Mock
        private UserDetails userDetails;

        @InjectMocks
        private UserServiceImpl userService;

        private ModelMapper modelMapper = new ModelMapper();

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                userService = new UserServiceImpl();
                userService.userRepository = userRepository;
                userService.modelMapper = modelMapper;
                SecurityContextHolder.setContext(securityContext);
        }

        @Test
        void saveUser_TestCase() {
                UserRole role1 = new UserRole(1L, "ROLE_USER");
                UserRole role2 = new UserRole(2L, "ROLE_ADMIN");
                Set<UserRole> roles = new HashSet<>();
                roles.add(role1);
                roles.add(role2);

                UserRequest userRequest = new UserRequest();
                userRequest.setUsername("testUser");
                userRequest.setPassword("password123");
                userRequest.setRoles(roles);

                UserInfo savedUser = new UserInfo();
                savedUser.setId(1L);
                savedUser.setUsername("testUser");
                savedUser.setPassword("password123");
                savedUser.setRoles(roles);

                Mockito.when(userRepository.save(any(UserInfo.class))).thenReturn(savedUser);

                //when
                UserResponse userResponse = userService.saveUser(userRequest);

                // then
                assertNotNull(userResponse);
                assertEquals(1L, userResponse.getId());
                assertEquals("testUser", userResponse.getUsername());
                assertEquals(roles.size(), userResponse.getRoles().size());
                assertTrue(userResponse.getRoles().containsAll(roles));
                verify(userRepository, times(1)).save(any(UserInfo.class));

        }

        @Test
        void updateUser_TestCase() {

                //given
                UserRequest userRequest = new UserRequest();
                userRequest.setId(1L);
                userRequest.setUsername("testUser");
                userRequest.setPassword("newPassword123");

                UserInfo existingUser = new UserInfo();
                existingUser.setId(1L);
                existingUser.setUsername("oldUser");
                existingUser.setPassword("oldPassword");

                UserInfo updatedUser = new UserInfo();
                updatedUser.setId(1L);
                updatedUser.setUsername("updatedUser");
                updatedUser.setPassword("encodedNewPassword");

                when(userRepository.findFirstById(1L)).thenReturn(existingUser);
                when(userRepository.save(any(UserInfo.class))).thenReturn(updatedUser);

                //when
                UserResponse userResponse = userService.saveUser(userRequest);

                // then
                assertNotNull(userResponse);
                assertEquals(1L, userResponse.getId());
                assertEquals("updatedUser", userResponse.getUsername());
                verify(userRepository, times(1)).findFirstById(1L);
                verify(userRepository, times(1)).save(any(UserInfo.class));

        }

        @Test
        void saveUser_MissingUsername_TestCase() {
                // given
                UserRequest userRequest = new UserRequest();
                userRequest.setPassword("password123");

                // when & then
                Exception exception = assertThrows(RuntimeException.class, () -> {
                        userService.saveUser(userRequest);
                });

                String expectedMessage = "Parameter username is not found in request..!!";
                String actualMessage = exception.getMessage();
                assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        void saveUser_MissingPassword_TestCase() {
                // given
                UserRequest userRequest = new UserRequest();
                userRequest.setUsername("testUser");

                // when & then
                Exception exception = assertThrows(RuntimeException.class, () -> {
                        userService.saveUser(userRequest);
                });

                String expectedMessage = "Parameter password is not found in request..!!";
                String actualMessage = exception.getMessage();
                assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        void getAllUsers_TestCase() {
                UserInfo user1 = new UserInfo(1L, "user1", "password1", null);
                UserInfo user2 = new UserInfo(2L, "user2", "password2", null);

                UserRole role = new UserRole(5L, "ROLE_USER");
                Set<UserRole> roles = new HashSet<>();
                roles.add(role);

                UserInfo user3 = new UserInfo(3L, "user3", "password3", roles);
                List<UserInfo> users = Arrays.asList(user1, user2, user3);

                when(userRepository.findAll()).thenReturn(users);

                // when
                List<UserResponse> userResponses = userService.getAllUser();

                // then
                assertNotNull(userResponses);
                assertEquals(3, userResponses.size());
                assertEquals("user1", userResponses.get(0).getUsername());
                assertEquals("user2", userResponses.get(1).getUsername());
                assertEquals("user3", userResponses.get(2).getUsername());
                assertEquals(roles.size(), userResponses.get(2).getRoles().size());
        }

        @Test
        void getLoggedInUserProfile_TestCase() {
                // Given
                String username = "testUser";
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn(username);

                UserRole role1 = new UserRole(1L, "ROLE_USER");
                Set<UserRole> roles = new HashSet<>();
                roles.add(role1);

                UserInfo userInfo = new UserInfo();
                userInfo.setId(1L);
                userInfo.setUsername(username);
                userInfo.setPassword("password");
                userInfo.setRoles(roles);

                when(userRepository.findByUsername(username)).thenReturn(userInfo);

                // When
                UserResponse userResponse = userService.getLoggedInUserProfile();

                // Then
                assertNotNull(userResponse);
                assertEquals(1L, userResponse.getId());
                assertEquals(username, userResponse.getUsername());
                assertEquals(roles, userResponse.getRoles());

                verify(userRepository, times(1)).findByUsername(username);
                verify(securityContext, times(1)).getAuthentication();
                verify(authentication, times(1)).getPrincipal();
                verify(userDetails, times(1)).getUsername();
        }

        @Test
        void getUserById_WhenUserExists_TestCase() {
                // Given
                UserRole role1 = new UserRole(1L, "ROLE_USER");
                Set<UserRole> roles = new HashSet<>();
                roles.add(role1);

                UserInfo userInfo = new UserInfo();
                userInfo.setId(1L);
                userInfo.setUsername("testUser");
                userInfo.setPassword("password");
                userInfo.setRoles(roles);

                when(userRepository.findFirstById(1L)).thenReturn(userInfo);

                // When
                UserResponse userResponse = userService.getUserById(1L);

                // Then
                assertNotNull(userResponse);
                assertEquals(1L, userResponse.getId());
                assertEquals("testUser", userResponse.getUsername());
                assertEquals(roles, userResponse.getRoles());

                verify(userRepository, times(1)).findFirstById(1L);
        }

        @Test
        void getUserById_WhenUserDoesNotExist_TestCase() {
                // Given
                Long userId = 1L;

                when(userRepository.findFirstById(userId)).thenReturn(null);

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        userService.getUserById(userId);
                });

                assertEquals("User not found with id: " + userId, exception.getMessage());
                verify(userRepository, times(1)).findFirstById(userId);

        }

        @Test
        void getUserById_WhenIdIsNull_TestCase() {
                // When & Then
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                        userService.getUserById(null);
                });

                assertEquals("Id cannot be null", exception.getMessage());
                verify(userRepository, times(0)).findFirstById(anyLong());
        }

        @Test
        void deleteUserById_WhenUserExists_TestCase() {
                // Given
                Long userId = 1L;
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userId);
                userInfo.setUsername("testUser");

                when(userRepository.findFirstById(userId)).thenReturn(userInfo);

                // When
                Long deletedUserId = userService.deleteUserById(userId);

                // Then
                assertNotNull(deletedUserId);
                assertEquals(userId, deletedUserId);

                verify(userRepository, times(1)).findFirstById(userId);
                verify(userRepository, times(1)).delete(userInfo);
        }

        @Test
        void deleteUserById_WhenUserDoesNotExist_TestCase() {
                // Given
                Long userId = 1L;

                when(userRepository.findFirstById(userId)).thenReturn(null);

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        userService.deleteUserById(userId);
                });
                assertEquals("User not found with id: " + userId, exception.getMessage());

                verify(userRepository, times(1)).findFirstById(userId);
                verify(userRepository, times(0)).delete(any(UserInfo.class));
        }

        @Test
        void deleteUserById_WhenIdIsNull_TestCase() {
                // When & Then
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                        userService.deleteUserById(null);
                });

                assertEquals("Id cannot be null", exception.getMessage());

                verify(userRepository, times(0)).findFirstById(anyLong());
                verify(userRepository, times(0)).delete(any(UserInfo.class));
        }

}