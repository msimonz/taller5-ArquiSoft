package com.taller5.aggregator.dal.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingDalService {
  private final InvoiceRepository repo;
  private final InvoiceItemRepository itemRepo;
  
  public BillingDalService(InvoiceRepository repo, InvoiceItemRepository itemRepo) {
    this.repo = repo;
    this.itemRepo = itemRepo;
  }

  @Transactional
  public CreateInvoiceRes create(CreateInvoiceReq req) {
    Invoice inv = Invoice.builder()
        .paymentId(req.paymentId())
        .customerId(req.customerId())
        .customerEmail(req.customerEmail())
        .totalAmount(req.totalAmount())
        .createdAt(Instant.now())
        .build();
    
    // Crear los items de la factura
    for (var itemReq : req.items()) {
      InvoiceItem item = InvoiceItem.builder()
          .productId(itemReq.productId())
          .productName(itemReq.productName())
          .supplierId(itemReq.supplierId())
          .supplierName(itemReq.supplierName())
          .quantity(itemReq.quantity())
          .unitPrice(itemReq.unitPrice())
          .subtotal(itemReq.subtotal())
          .build();
      inv.addItem(item);
    }
    
    inv = repo.save(inv);
    return new CreateInvoiceRes(inv.getId(), mapToDto(inv));
  }

  @Transactional(readOnly = true)
  public InvoiceDto get(Long id) {
    Invoice inv = repo.findById(id).orElseThrow();
    return mapToDto(inv);
  }

  private InvoiceDto mapToDto(Invoice inv) {
    List<InvoiceItemDto> itemDtos = inv.getItems().stream()
        .map(item -> new InvoiceItemDto(
            item.getId(),
            item.getProductId(),
            item.getProductName(),
            item.getSupplierId(),
            item.getSupplierName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal()))
        .collect(Collectors.toList());
    
    return new InvoiceDto(
        inv.getId(),
        inv.getPaymentId(),
        inv.getCustomerId(),
        inv.getCustomerEmail(),
        inv.getTotalAmount(),
        itemDtos,
        inv.getCreatedAt().toString());
  }

  // DTOs
  public record CreateInvoiceReq(
      Long paymentId,
      Long customerId,
      String customerEmail,
      BigDecimal totalAmount,
      List<InvoiceItemReq> items) {}

  public record InvoiceItemReq(
      Long productId,
      String productName,
      Long supplierId,
      String supplierName,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  public record CreateInvoiceRes(Long invoiceId, InvoiceDto invoice) {}

  public record InvoiceDto(
      Long id,
      Long paymentId,
      Long customerId,
      String customerEmail,
      BigDecimal totalAmount,
      List<InvoiceItemDto> items,
      String createdAt) {}

  public record InvoiceItemDto(
      Long id,
      Long productId,
      String productName,
      Long supplierId,
      String supplierName,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}
}
