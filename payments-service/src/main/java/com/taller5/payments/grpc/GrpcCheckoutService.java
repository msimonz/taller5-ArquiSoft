package com.taller5.payments.grpc;
import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import com.taller5.payments.grpc.CheckoutProto.CheckoutRequest;
import com.taller5.payments.grpc.CheckoutProto.CheckoutResponse;
import com.taller5.payments.grpc.CheckoutServiceGrpc.CheckoutServiceImplBase;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@GRpcService
public class GrpcCheckoutService extends CheckoutServiceImplBase {

  private final PaymentRepository repo;
  private final WebClient invClient;
  private final WebClient billClient;

  public GrpcCheckoutService(PaymentRepository repo,
                            @Value("${INVENTORY_BASE:http://localhost:8081}") String inventoryBase,
                            @Value("${BILLING_BASE:http://localhost:8083}") String billingBase) {
    this.repo = repo;
    this.invClient = WebClient.builder().baseUrl(inventoryBase).build();
    this.billClient = WebClient.builder().baseUrl(billingBase).build();
  }

  @Override
  @Transactional
  public void checkout(CheckoutRequest req, StreamObserver<CheckoutResponse> responseObserver) {
    try {
      // 1) Reserve inventory (REST call)
      var reserveResponse = invClient.post()
        .uri("/api/inventory/reserve")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(java.util.Map.of("productId", req.getProductId(), "quantity", req.getQuantity()))
        .retrieve()
        .toBodilessEntity()
        .onErrorReturn(null)
        .block();

      if (reserveResponse == null || !reserveResponse.getStatusCode().is2xxSuccessful()) {
        CheckoutResponse res = CheckoutResponse.newBuilder()
          .setStatus("NO_STOCK")
          .build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
        return;
      }

      // 2) Persist payment
      Payment p = repo.save(new Payment(null, req.getCustomerId(), req.getProductId(), req.getAmount()));
      long paymentId = p.getId();

      try {
        // 3) Create invoice in billing (REST call)
        Long invoiceId = billClient.post()
          .uri("/api/invoices")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(java.util.Map.of("paymentId", paymentId))
          .retrieve()
          .bodyToMono(java.util.Map.class)
          .map(m -> ((Number)m.get("invoiceId")).longValue())
          .block();

        CheckoutResponse res = CheckoutResponse.newBuilder()
          .setPaymentId(paymentId)
          .setInvoiceId(invoiceId != null ? invoiceId : 0L)
          .setStatus("APPROVED")
          .build();

        responseObserver.onNext(res);
        responseObserver.onCompleted();
        return;

      } catch (RuntimeException ex) {
        // Compensate: release inventory
        invClient.post()
          .uri("/api/inventory/release")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(java.util.Map.of("productId", req.getProductId(), "quantity", req.getQuantity()))
          .retrieve()
          .toBodilessEntity()
          .onErrorReturn(null)
          .block();

        CheckoutResponse res = CheckoutResponse.newBuilder()
          .setPaymentId(paymentId)
          .setStatus("FAILED")
          .build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
        return;
      }

    } catch (Exception e) {
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
    }
  }
}
