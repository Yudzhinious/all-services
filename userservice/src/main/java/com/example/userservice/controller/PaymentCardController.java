package com.example.userservice.controller;

import com.example.userservice.dto.PaymentCardDto;
import com.example.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDto> createCard(@Valid @RequestBody PaymentCardDto cardDto) {
        PaymentCardDto created = paymentCardService.createCard(cardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getCardById(@PathVariable Long id) {
        PaymentCardDto card = paymentCardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAllCards(Pageable pageable) {
        Page<PaymentCardDto> cards = paymentCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCardDto> cards = paymentCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardDto cardDto) {
        PaymentCardDto updated = paymentCardService.updateCard(id, cardDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardDto> activateCard(@PathVariable Long id) {
        PaymentCardDto activated = paymentCardService.activateCard(id);
        return ResponseEntity.ok(activated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PaymentCardDto> deactivateCard(@PathVariable Long id) {
        PaymentCardDto deactivated = paymentCardService.deactivateCard(id);
        return ResponseEntity.ok(deactivated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}