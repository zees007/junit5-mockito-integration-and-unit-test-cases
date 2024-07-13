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

import java.util.HashSet;
import java.util.Set;

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


        @InjectMocks
        private UserServiceImpl userService;

        private ModelMapper modelMapper = new ModelMapper();

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                userService = new UserServiceImpl();
                userService.userRepository = userRepository;
                userService.modelMapper = modelMapper;
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


}