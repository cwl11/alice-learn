package com.alice.learn.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private BigDecimal targetScore;
    private LocalDateTime createdAt;
}
