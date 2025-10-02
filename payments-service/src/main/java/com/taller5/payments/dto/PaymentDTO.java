// dto/PaymentDTO.java
package com.taller5.payments.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDTO(Long id, Long customerId, Long productId, BigDecimal amount) {}
