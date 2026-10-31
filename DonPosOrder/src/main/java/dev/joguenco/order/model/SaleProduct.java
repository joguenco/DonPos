package dev.joguenco.order.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// Las columnas de products que DonPos usa para armar una línea del pedido (ProductInfoExt)
@Getter
@Setter
@MappedEntity("products")
public class SaleProduct {

  @Id private String id;

  private String reference;

  private String code;

  private String name;

  @MappedProperty("pricesell")
  private double priceSell;

  private String category;

  @MappedProperty("taxcat")
  private String taxCategory;

  @MappedProperty("attributeset_id")
  private String attributeSetId;

  @MappedProperty("iscom")
  private boolean com;

  @MappedProperty("isconstant")
  private boolean constant;

  @MappedProperty("isservice")
  private boolean service;

  // Propiedades del producto en XML, igual que en DonPos
  private byte[] attributes;

  @MappedProperty("isvprice")
  private boolean variablePrice;

  @MappedProperty("isverpatrib")
  private boolean verifyAttributes;

  @MappedProperty("texttip")
  private String textTip;

  private boolean warranty;

  @MappedProperty("printto")
  private String printer;

  @MappedProperty("memodate")
  private LocalDateTime memoDate;
}
