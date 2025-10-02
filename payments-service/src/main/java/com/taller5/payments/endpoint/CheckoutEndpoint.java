/* package com.taller5.payments.endpoint;

import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import com.taller5.payments.soap.CheckoutRequest;
import com.taller5.payments.soap.CheckoutResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.Endpoint;

@Endpoint
@Component
public class CheckoutEndpoint {

  private static final String NAMESPACE = "http://taller5.com/soap/checkout";

  private final PaymentRepository repo;
  private final WebClient invClient;
  private final WebClient billClient;

  public CheckoutEndpoint(PaymentRepository repo,
                          @Value("${INVENTORY_BASE:http://localhost:8081}") String inventoryBase,
                          @Value("${BILLING_BASE:http://localhost:8083}") String billingBase) {
    this.repo = repo;
    this.invClient = WebClient.builder().baseUrl(inventoryBase).build();
    this.billClient = WebClient.builder().baseUrl(billingBase).build();
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "CheckoutRequest")
  @ResponsePayload
  @Transactional
  public CheckoutResponse handleCheckout(@RequestPayload CheckoutRequest req) {
    CheckoutResponse res = new CheckoutResponse();

    // 1) Reserva (llama al REST inventory)
    var reserveStatus = invClient.post().uri("/api/inventory/reserve")
      .bodyValue(new java.util.HashMap<String, Object>() {{
        put("productId", req.productId);
        put("quantity", req.quantity);
      }})
      .retrieve()
      .toBodilessEntity()
      .onErrorReturn(null)
      .block();

    if (reserveStatus == null || !reserveStatus.getStatusCode().is2xxSuccessful()) {
      res.status = "NO_STOCK";
      return res;
    }

    // 2) Persistir pago en tabla payments
    Payment p = repo.save(new Payment(null, req.customerId, req.productId, req.amount));
    res.paymentId = p.getId();

    try {
      // 3) Crear factura en billing (REST)
      Long invoiceId = billClient.post().uri("/api/invoices")
        .bodyValue(new java.util.HashMap<String, Object>() {{ put("paymentId", p.getId()); }})
        .retrieve()
        .bodyToMono(java.util.Map.class)
        .map(m -> ((Number)m.get("invoiceId")).longValue())
        .block();

      res.invoiceId = invoiceId;
      res.status = "APPROVED";
      return res;
    } catch (RuntimeException ex) {
      // compensación: release inventory
      invClient.post().uri("/api/inventory/release")
        .bodyValue(new java.util.HashMap<String, Object>() {{ put("productId", req.productId); put("quantity", req.quantity); }})
        .retrieve().toBodilessEntity().block();
      res.status = "FAILED";
      return res;
    }
  }
}
 */