// dto/ChargeRequest.java
package com.taller5.payments.dto;

import java.math.BigDecimal;

public record ChargeRequest(Long customerId, Long productId, BigDecimal amount) {}
