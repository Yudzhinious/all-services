package com.example.userservice.controller;

import com.example.userservice.dto.PayCardDto;
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
public class PayCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PayCardDto> createCard(@Valid @RequestBody PayCardDto cardDto) {
        PayCardDto created = paymentCardService.createCard(cardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayCardDto> getCardById(@PathVariable Long id) {
        PayCardDto card = paymentCardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<PayCardDto>> getAllCards(Pageable pageable) {
        Page<PayCardDto> cards = paymentCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PayCardDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PayCardDto> cards = paymentCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayCardDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody PayCardDto cardDto) {
        PayCardDto updated = paymentCardService.updateCard(id, cardDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PayCardDto> activateCard(@PathVariable Long id) {
        PayCardDto activated = paymentCardService.activateCard(id);
        return ResponseEntity.ok(activated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PayCardDto> deactivateCard(@PathVariable Long id) {
        PayCardDto deactivated = paymentCardService.deactivateCard(id);
        return ResponseEntity.ok(deactivated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}