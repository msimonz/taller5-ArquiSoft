package com.taller5.inventory.endpoint;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import com.taller5.inventory.soap.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Endpoint
@Component
public class InventoryEndpoint {

  private static final String NAMESPACE = "http://taller5.com/soap/inventory";
  private final ProductRepository repo;

  public InventoryEndpoint(ProductRepository repo) {
    this.repo = repo;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "ReserveRequest")
  @ResponsePayload
  @Transactional
  public ReserveResponse reserve(@RequestPayload ReserveRequest req) {
    ReserveResponse res = new ReserveResponse();

    if (req == null || req.productId == null || req.quantity == null || req.quantity <= 0) {
      res.status = "BAD_REQUEST";
      return res;
    }

    var opt = repo.findById(req.productId);
    if (opt.isEmpty()) {
      res.status = "NOT_FOUND";
      return res;
    }

    Product p = opt.get();
    if (p.getStock() < req.quantity) {
      res.status = "NO_STOCK";
      return res;
    }

    p.setStock(p.getStock() - req.quantity);
    repo.save(p);
    res.status = "OK";
    return res;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "ReleaseRequest")
  @ResponsePayload
  @Transactional
  public ReleaseResponse release(@RequestPayload ReleaseRequest req) {
    ReleaseResponse res = new ReleaseResponse();
    if (req == null || req.productId == null || req.quantity == null || req.quantity <= 0) {
      res.status = "BAD_REQUEST";
      return res;
    }
    var opt = repo.findById(req.productId);
    if (opt.isEmpty()) {
      res.status = "NOT_FOUND";
      return res;
    }
    Product p = opt.get();
    p.setStock(p.getStock() + req.quantity);
    repo.save(p);
    res.status = "OK";
    return res;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "ListProductsRequest")
  @ResponsePayload
  public ListProductsResponse list(@RequestPayload ListProductsRequest req) {
    List<Product> all = repo.findAll();
    ListProductsResponse res = new ListProductsResponse();
    res.product = all.stream().map(p -> {
      ProductSoap ps = new ProductSoap();
      ps.id = p.getId();
      ps.name = p.getName();
      ps.price = p.getPrice();
      ps.stock = p.getStock();
      return ps;
    }).collect(Collectors.toList());
    return res;
  }
}
