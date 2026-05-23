package com.financemanager.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateTransactionRequest {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String category;

    private String description;
}
