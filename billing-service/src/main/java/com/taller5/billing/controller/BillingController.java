package com.taller5.billing.controller;

import com.taller5.billing.client.AggregatorBillingDalClient;
import com.taller5.billing.dto.CustomerNotification;
import com.taller5.billing.dto.SupplierNotification;
import com.taller5.billing.service.JmsCustomerNotificationService;
import com.taller5.billing.service.KafkaSupplierNotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

  private final AggregatorBillingDalClient dal;
  private final KafkaSupplierNotificationService kafkaService;
  private final JmsCustomerNotificationService jmsService;

  @PostMapping
  public AggregatorBillingDalClient.CreateInvoiceRes create(@RequestBody CreateInvoiceReq req) {
    // Crear la factura usando el DAL
    // Convert controller DTOs to DAL DTOs
    List<AggregatorBillingDalClient.InvoiceItemReq> dalItems = req.items().stream()
        .map(item -> new AggregatorBillingDalClient.InvoiceItemReq(
            item.productId(),
            item.productName(),
            item.supplierId(),
            item.supplierName(),
            item.quantity(),
            item.unitPrice(),
            item.subtotal()
        ))
        .collect(Collectors.toList());

    var dalReq = new AggregatorBillingDalClient.CreateInvoiceReq(
        req.paymentId(), req.customerId(), req.customerEmail(), 
        req.amount(), dalItems
    );
    var result = dal.create(dalReq);
    
    log.info("Factura {} creada exitosamente", result.invoiceId());
    
    // Enviar notificaciones
    sendNotifications(result.invoice());
    
    return result;
  }

  @GetMapping("/{id}")
  public AggregatorBillingDalClient.InvoiceDto get(@PathVariable Long id) {
    return dal.get(id);
  }

  private void sendNotifications(AggregatorBillingDalClient.InvoiceDto invoice) {
    // 1. Agrupar items por proveedor y enviar notificación a cada uno vía Kafka
    Map<Long, List<AggregatorBillingDalClient.InvoiceItemDto>> itemsBySupplier = 
        invoice.items().stream()
            .collect(Collectors.groupingBy(AggregatorBillingDalClient.InvoiceItemDto::supplierId));
    
    for (var entry : itemsBySupplier.entrySet()) {
      Long supplierId = entry.getKey();
      List<AggregatorBillingDalClient.InvoiceItemDto> supplierItems = entry.getValue();
      
      // Tomar el nombre del proveedor del primer item
      String supplierName = supplierItems.get(0).supplierName();
      String supplierEmail = supplierItems.get(0).supplierName() + "@supplier.com"; // Placeholder
      
      BigDecimal supplierTotal = supplierItems.stream()
          .map(AggregatorBillingDalClient.InvoiceItemDto::subtotal)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      
      List<SupplierNotification.ProductItem> products = supplierItems.stream()
          .map(item -> SupplierNotification.ProductItem.builder()
              .productId(item.productId())
              .productName(item.productName())
              .quantity(item.quantity())
              .unitPrice(item.unitPrice())
              .subtotal(item.subtotal())
              .build())
          .collect(Collectors.toList());
      
      SupplierNotification supplierNotif = SupplierNotification.builder()
          .supplierId(supplierId)
          .supplierName(supplierName)
          .supplierEmail(supplierEmail)
          .invoiceId(invoice.id())
          .customerId(invoice.customerId())
          .customerEmail(invoice.customerEmail())
          .products(products)
          .totalAmount(supplierTotal)
          .timestamp(Instant.now().toString())
          .build();
      
      kafkaService.notifySupplier(supplierNotif);
    }
    
    // 2. Enviar notificación al cliente vía JMS
    List<CustomerNotification.PurchasedItem> purchasedItems = invoice.items().stream()
        .map(item -> CustomerNotification.PurchasedItem.builder()
            .productName(item.productName())
            .quantity(item.quantity())
            .unitPrice(item.unitPrice())
            .subtotal(item.subtotal())
            .build())
        .collect(Collectors.toList());
    
    CustomerNotification customerNotif = CustomerNotification.builder()
        .invoiceId(invoice.id())
        .customerId(invoice.customerId())
        .customerEmail(invoice.customerEmail())
        .totalAmount(invoice.totalAmount())
        .currency("USD")
        .items(purchasedItems)
        .timestamp(Instant.now().toString())
        .build();
    
    jmsService.notifyCustomer(customerNotif);
  }

  // DTOs
  public record CreateInvoiceReq(
      Long paymentId, // Opcional - puede ser null
      @NotNull Long customerId,
      @NotNull String customerEmail,
      @NotNull BigDecimal amount,
      @NotNull List<InvoiceItemReq> items
  ) {}
  
  public record InvoiceItemReq(
      @NotNull Long productId,
      @NotNull String productName,
      @NotNull Long supplierId,
      @NotNull String supplierName,
      @NotNull Integer quantity,
      @NotNull BigDecimal unitPrice,
      @NotNull BigDecimal subtotal
  ) {}
}
