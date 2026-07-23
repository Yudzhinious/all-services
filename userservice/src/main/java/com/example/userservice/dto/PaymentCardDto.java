package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCardDto implements Serializable {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String number;

    @NotBlank(message = "Card holder is required")
    @Size(min = 2, max = 100, message = "Holder name must be between 2 and 100 characters")
    private String holder;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}