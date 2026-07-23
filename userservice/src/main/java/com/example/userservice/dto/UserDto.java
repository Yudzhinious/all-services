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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {

    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(min = 2, max = 100)
    private String surname;

    @NotNull
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank
    @Email
    private String email;

    private Boolean active;

    private List<PayCardDto> paymentCards;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}