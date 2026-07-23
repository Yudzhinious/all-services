package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayCardDto implements Serializable {

    private Long id;

    private Long userId;

    @Pattern(regexp = "\\d{16}")
    private String number;

    @NotBlank
    @Size(min = 2, max = 100)
    private String holder;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}