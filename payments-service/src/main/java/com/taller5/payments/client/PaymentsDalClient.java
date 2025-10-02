// client/PaymentsDalClient.java
package com.taller5.payments.client;

import com.taller5.payments.dto.PaymentDTO;
import com.taller5.payments.dto.ChargeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
  name = "paymentsDalClient", url = "${aggregator.url}", path = "/dal/payments" 
)
public interface PaymentsDalClient {


  @PostMapping("/charge")
  PaymentDTO charge(@RequestBody ChargeRequest req);

  @GetMapping("/{id}")
  PaymentDTO findById(@PathVariable Long id);

  @GetMapping
  List<PaymentDTO> findAll();
}
