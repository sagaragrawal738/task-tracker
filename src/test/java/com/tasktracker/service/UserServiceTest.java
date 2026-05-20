package com.tasktracker.service;

import com.tasktracker.dto.UserRequest;
import com.tasktracker.dto.UserResponse;
import com.tasktracker.entity.User;
import com.tasktracker.exception.DuplicateResourceException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice Johnson", "alice@example.com", "DEVELOPER");
        sampleUser.setId(1L);
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        UserRequest request = new UserRequest("Alice Johnson", "alice@example.com", "DEVELOPER");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("Alice Johnson", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("DEVELOPER", result.getRole());
    }

    @Test
    void createUser_duplicateEmail_shouldThrowException() {
        UserRequest request = new UserRequest("Alice Johnson", "alice@example.com", "DEVELOPER");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Alice Johnson", result.getName());
    }

    @Test
    void getUserById_notFound_shouldThrowException() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(42L));
    }

    @Test
    void getAllUsers_shouldReturnPagedResults() {
        Page<User> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("alice@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        UserRequest request = new UserRequest("Alice Smith", "alice.smith@example.com", "QA");
        User updated = new User("Alice Smith", "alice.smith@example.com", "QA");
        updated.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("alice.smith@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserResponse result = userService.updateUser(1L, request);

        assertEquals("Alice Smith", result.getName());
        assertEquals("QA", result.getRole());
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_shouldThrowException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    }
}
