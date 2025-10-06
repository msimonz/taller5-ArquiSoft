package com.taller5.grpc;

import com.taller5.dal.TransactionalDal;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;
import com.taller5.payments.grpc.CheckoutRequest;
import com.taller5.payments.grpc.CheckoutResponse;
import com.taller5.payments.grpc.CheckoutServiceGrpc;

import java.math.BigDecimal;

@GrpcService
public class CheckoutGrpcService extends CheckoutServiceGrpc.CheckoutServiceImplBase {

  private final TransactionalDal dal;

  public CheckoutGrpcService(TransactionalDal dal) {
    this.dal = dal;
  }

  @Override
  public void checkout(CheckoutRequest req, StreamObserver<CheckoutResponse> responseObserver) {
    try {
      var res = dal.checkout(req.getCustomerId(), req.getProductId(), req.getQuantity(), BigDecimal.valueOf(req.getAmount()));
      var resp = CheckoutResponse.newBuilder()
          .setPaymentId(res.paymentId())
          .setInvoiceId(res.invoiceId())
          .setStatus(res.status())
          .build();
      responseObserver.onNext(resp);
      responseObserver.onCompleted();
    } catch (Throwable t) {
      responseObserver.onError(t);
    }
  }
}
