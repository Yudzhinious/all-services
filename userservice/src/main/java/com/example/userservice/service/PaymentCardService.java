package com.example.userservice.service;

import com.example.userservice.dto.PayCardDto;
import com.example.userservice.entity.PayCard;
import com.example.userservice.entity.User;
import com.example.userservice.exception.BusinessException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.PaymentCardMapper;
import com.example.userservice.repository.PaymentCardRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCardService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @CacheEvict(value = "users", key = "#cardDto.userId")
    @Transactional
    public PayCardDto createCard(PayCardDto cardDto) {
        log.info("Creating payment card for user ID: {}", cardDto.getUserId());

        User user = userRepository.findById(cardDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + cardDto.getUserId()));

        long cardCount = paymentCardRepository.countByUserId(cardDto.getUserId());
        if (cardCount >= MAX_CARDS_PER_USER) {
            throw new BusinessException("User cannot have more than " + MAX_CARDS_PER_USER + " cards");
        }

        PayCard card = paymentCardMapper.toEntity(cardDto);
        card.setUser(user);
        card.setActive(true);
        PayCard savedCard = paymentCardRepository.save(card);

        log.info("Payment card created with ID: {}", savedCard.getId());
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional(readOnly = true)
    public PayCardDto getCardById(Long id) {
        log.info("Fetching payment card with ID: {}", id);
        PayCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with ID: " + id));
        return paymentCardMapper.toDto(card);
    }

    @Transactional(readOnly = true)
    public List<PayCardDto> getCardsByUserId(Long userId) {
        log.info("Fetching payment cards for user ID: {}", userId);
        List<PayCard> cards = paymentCardRepository.findByUserId(userId);
        return paymentCardMapper.toDtoList(cards);
    }

    @Transactional(readOnly = true)
    public Page<PayCardDto> getAllCards(Pageable pageable) {
        log.info("Fetching all payment cards");
        Page<PayCard> cards = paymentCardRepository.findAll(pageable);
        return cards.map(paymentCardMapper::toDto);
    }

    @CacheEvict(value = "users", key = "#result.userId")
    @Transactional
    public PayCardDto updateCard(Long id, PayCardDto cardDto) {
        log.info("Updating payment card with ID: {}", id);
        PayCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with ID: " + id));

        paymentCardMapper.updateCardFromDto(cardDto, card);
        PayCard updatedCard = paymentCardRepository.save(card);

        log.info("Payment card updated with ID: {}", updatedCard.getId());
        return paymentCardMapper.toDto(updatedCard);
    }

    @CacheEvict(value = "users", key = "#result.userId")
    @Transactional
    public PayCardDto activateCard(Long id) {
        log.info("Activating payment card with ID: {}", id);
        PayCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with ID: " + id));
        card.setActive(true);
        return paymentCardMapper.toDto(paymentCardRepository.save(card));
    }

    @CacheEvict(value = "users", key = "#result.userId")
    @Transactional
    public PayCardDto deactivateCard(Long id) {
        log.info("Deactivating payment card with ID: {}", id);
        PayCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with ID: " + id));
        card.setActive(false);
        return paymentCardMapper.toDto(paymentCardRepository.save(card));
    }

    @Transactional
    public void deleteCard(Long id) {
        log.info("Deleting payment card with ID: {}", id);
        PayCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with ID: " + id));

        paymentCardRepository.deleteById(id);
    }
}