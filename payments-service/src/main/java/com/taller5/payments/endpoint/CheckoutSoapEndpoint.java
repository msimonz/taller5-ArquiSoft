package com.taller5.payments.endpoint;

import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

@Endpoint
@Component
public class CheckoutSoapEndpoint {

  private static final String NAMESPACE = "http://taller5.com/soap/checkout";

  @PayloadRoot(namespace = NAMESPACE, localPart = "CheckoutRequest")
  @ResponsePayload
  public DOMSource handleCheckout(@RequestPayload Element requestElement) throws Throwable {
    try {
      // extrae campos (namespace-qualified)
      String customerIdText = getChildText(requestElement, "customerId");
      String productIdText  = getChildText(requestElement, "productId");
      String quantityText   = getChildText(requestElement, "quantity");
      String amountText     = getChildText(requestElement, "amount");

      if (customerIdText == null || productIdText == null || quantityText == null || amountText == null) {
        return buildFault("Missing required fields");
      }

      long customerId = Long.parseLong(customerIdText);
      long productId = Long.parseLong(productIdText);
      int quantity = Integer.parseInt(quantityText);
      double amount = Double.parseDouble(amountText);

      // TODO: aquí puedes invocar repositorios o clientes REST para inventory/billing
      long paymentId = System.currentTimeMillis() % 1000000L;
      long invoiceId = paymentId + 1;

      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document respDoc = db.newDocument();

      var envelope = respDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
      envelope.setAttribute("xmlns:sch", NAMESPACE);
      var body = respDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Body");

      var respRoot = respDoc.createElementNS(NAMESPACE, "sch:CheckoutResponse");
      var pId = respDoc.createElementNS(NAMESPACE, "sch:paymentId"); pId.setTextContent(String.valueOf(paymentId));
      var iId = respDoc.createElementNS(NAMESPACE, "sch:invoiceId"); iId.setTextContent(String.valueOf(invoiceId));
      var status = respDoc.createElementNS(NAMESPACE, "sch:status"); status.setTextContent("APPROVED");

      respRoot.appendChild(pId); respRoot.appendChild(iId); respRoot.appendChild(status);
      body.appendChild(respRoot); envelope.appendChild(body); respDoc.appendChild(envelope);

      return new DOMSource(respDoc);

    } catch (Exception ex) {
      return buildFault("Server error: " + ex.getMessage());
    }
  }

  private String getChildText(Element parent, String localName) {
    NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE, localName);
    if (nodes.getLength() == 0) return null;
    Node n = nodes.item(0);
    return n.getTextContent();
  }

  private DOMSource buildFault(String msg) throws Exception {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document faultDoc = db.newDocument();
    var env = faultDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
    var body = faultDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Body");
    var fault = faultDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Fault");
    var faultString = faultDoc.createElement("faultstring"); faultString.setTextContent(msg);
    fault.appendChild(faultString); body.appendChild(fault); env.appendChild(body); faultDoc.appendChild(env);
    return new DOMSource(faultDoc);
  }
}