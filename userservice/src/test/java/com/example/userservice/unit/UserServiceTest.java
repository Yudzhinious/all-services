package com.example.userservice.unit;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.exception.BusinessException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private User user;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;

        userDto = UserDto.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .build();

        user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .active(true)
                .build();
    }

    @Test
    void create_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any(UserDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.create(userDto));

        assertEquals("Email already exists: john.doe@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDtoWithCards(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getById(userId);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getById(userId));

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        Page<UserDto> result = userService.getAll(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getName());
    }

    @Test
    void update_Success() {
        UserDto updateDto = UserDto.builder()
                .name("John Updated")
                .surname("Doe Updated")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(updateDto);

        UserDto result = userService.update(userId, updateDto);

        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        verify(userMapper, times(1)).updateFromDto(updateDto, user);
    }

    @Test
    void activate_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.activate(userId);

        assertNotNull(result);
        assertTrue(user.getActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deactivate_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.deactivate(userId);

        assertNotNull(result);
        assertFalse(user.getActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void delete_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.delete(userId));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}