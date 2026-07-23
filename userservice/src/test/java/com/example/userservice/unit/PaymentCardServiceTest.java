package com.example.userservice.unit;

import com.example.userservice.dto.PayCardDto;
import com.example.userservice.entity.PayCard;
import com.example.userservice.entity.User;
import com.example.userservice.exception.BusinessException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.PaymentCardMapper;
import com.example.userservice.repository.PaymentCardRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.PaymentCardService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper cardMapper;

    @InjectMocks
    private PaymentCardService cardService;

    private User user;
    private PayCard card;
    private PayCardDto cardDto;
    private Long userId;
    private Long cardId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        cardId = 1L;

        user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .build();

        card = PayCard.builder()
                .id(cardId)
                .user(user)
                .number("1234567890123456")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 12, 31))
                .active(true)
                .build();

        cardDto = PayCardDto.builder()
                .id(cardId)
                .userId(userId)
                .number("1234567890123456")
                .holder("John Doe")
                .expirationDate(LocalDate.of(2026, 12, 31))
                .active(true)
                .build();
    }

    @Test
    void create_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserId(userId)).thenReturn(0L);
        when(cardMapper.toEntity(any(PayCardDto.class))).thenReturn(card);
        when(cardRepository.save(any(PayCard.class))).thenReturn(card);
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(cardDto);

        PayCardDto result = cardService.create(cardDto);

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
        assertEquals("John Doe", result.getHolder());
        verify(cardRepository, times(1)).save(any(PayCard.class));
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.create(cardDto));

        assertEquals("User not found: 1", exception.getMessage());
        verify(cardRepository, never()).save(any(PayCard.class));
    }

    @Test
    void create_MaxCardsExceeded_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserId(userId)).thenReturn(5L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.create(cardDto));

        assertEquals("Max cards exceeded: 5", exception.getMessage());
        verify(cardRepository, never()).save(any(PayCard.class));
    }

    @Test
    void getById_Success() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(cardDto);

        PayCardDto result = cardService.getById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("1234567890123456", result.getNumber());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.getById(cardId));

        assertEquals("Card not found: 1", exception.getMessage());
    }

    @Test
    void getByUserId_Success() {
        when(cardRepository.findByUserId(userId)).thenReturn(List.of(card));
        when(cardMapper.toDtoList(anyList())).thenReturn(List.of(cardDto));

        List<PayCardDto> result = cardService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234567890123456", result.get(0).getNumber());
    }

    @Test
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PayCard> cardPage = new PageImpl<>(List.of(card));

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(cardDto);

        Page<PayCardDto> result = cardService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("1234567890123456", result.getContent().get(0).getNumber());
    }

    @Test
    void update_Success() {
        PayCardDto updateDto = PayCardDto.builder()
                .holder("Jane Doe")
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(PayCard.class))).thenReturn(card);
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(updateDto);

        PayCardDto result = cardService.update(cardId, updateDto);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getHolder());
        verify(cardMapper, times(1)).updateFromDto(updateDto, card);
    }

    @Test
    void activate_Success() {
        card.setActive(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(PayCard.class))).thenReturn(card);
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(cardDto);

        PayCardDto result = cardService.activate(cardId);

        assertNotNull(result);
        assertTrue(card.getActive());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void deactivate_Success() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(PayCard.class))).thenReturn(card);
        when(cardMapper.toDto(any(PayCard.class))).thenReturn(cardDto);

        PayCardDto result = cardService.deactivate(cardId);

        assertNotNull(result);
        assertFalse(card.getActive());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void delete_Success() {
        when(cardRepository.existsById(cardId)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(cardId);

        cardService.delete(cardId);

        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(cardRepository.existsById(cardId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.delete(cardId));

        assertEquals("Card not found: 1", exception.getMessage());
        verify(cardRepository, never()).deleteById(anyLong());
    }
}