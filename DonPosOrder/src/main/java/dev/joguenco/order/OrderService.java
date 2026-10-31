package dev.joguenco.order;

import com.unicenta.pos.customers.CustomerInfoExt;
import com.unicenta.pos.ticket.ProductInfoExt;
import com.unicenta.pos.ticket.TaxInfo;
import com.unicenta.pos.ticket.TicketInfo;
import com.unicenta.pos.ticket.TicketLineInfo;
import dev.joguenco.assigner.AssignerService;
import dev.joguenco.assigner.repository.SharedTicketRepository;
import dev.joguenco.floor.model.Place;
import dev.joguenco.floor.repository.PlaceRepository;
import dev.joguenco.order.model.BundleComponent;
import dev.joguenco.order.model.MenuCategory;
import dev.joguenco.order.model.ProductLot;
import dev.joguenco.order.model.SaleProduct;
import dev.joguenco.order.model.Tax;
import dev.joguenco.order.repository.MenuRepository;
import dev.joguenco.order.repository.TaxRepository;
import dev.joguenco.order.repository.VolumeDiscountRepository;
import dev.joguenco.util.TicketSerializer;
import dev.joguenco.waiter.WaiterException;
import dev.joguenco.waiter.model.Waiter;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

// Agregar y quitar productos de una mesa igual que JPanelTicket de DonPos
@Singleton
public class OrderService {

  private static final DateTimeFormatter MEMO_DATE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  protected final SharedTicketRepository sharedTicketRepository;
  protected final PlaceRepository placeRepository;
  protected final MenuRepository menuRepository;
  protected final TaxRepository taxRepository;
  protected final VolumeDiscountRepository volumeDiscountRepository;
  protected final AssignerService assignerService;

  // Un cambio a la vez por mesa: dos toques seguidos no se pisan el pedido
  private final Map<String, Object> tableGuards = new ConcurrentHashMap<>();

  public OrderService(
      SharedTicketRepository sharedTicketRepository,
      PlaceRepository placeRepository,
      MenuRepository menuRepository,
      TaxRepository taxRepository,
      VolumeDiscountRepository volumeDiscountRepository,
      AssignerService assignerService) {
    this.sharedTicketRepository = sharedTicketRepository;
    this.placeRepository = placeRepository;
    this.menuRepository = menuRepository;
    this.taxRepository = taxRepository;
    this.volumeDiscountRepository = volumeDiscountRepository;
    this.assignerService = assignerService;
  }

  public List<MenuCategory> listCategories() {
    return menuRepository.findCategories();
  }

  public List<CatalogItemDto> listProducts(String categoryId) {
    List<Tax> taxes = taxRepository.findAllOrderByName();

    return menuRepository.findProducts(categoryId).stream()
        .map(
            product -> {
              TaxInfo tax = findTax(taxes, product.getTaxCategory(), null);
              double rate = tax == null ? 0.0 : tax.getRate();
              return new CatalogItemDto(
                  product.getId(),
                  product.getName(),
                  product.getPriceSell() * (1.0 + rate),
                  product.isVariablePrice());
            })
        .toList();
  }

  public OrderDto getOrder(String placeId, Waiter waiter) {
    assignerService.signal(placeId, waiter);
    return toDto(placeId, load(placeId));
  }

  public OrderDto addProduct(String placeId, String productId, Waiter waiter) {
    synchronized (guard(placeId)) {
      assignerService.signal(placeId, waiter);

      Optional<SaleProduct> found =
          productId == null ? Optional.empty() : menuRepository.findById(productId);
      SaleProduct saleProduct =
          found.orElseThrow(
              () ->
                  new WaiterException(
                      HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "El producto no existe"));
      // DonPos pide el precio a mano en estos productos; eso se hace en caja
      if (saleProduct.isVariablePrice()) {
        throw new WaiterException(
            HttpStatus.CONFLICT,
            "VARIABLE_PRICE",
            "Este producto tiene precio variable, agrégalo en caja");
      }
      String lot = findLot(productId);

      TicketInfo ticket = load(placeId);
      ProductInfoExt product = toProductInfo(saleProduct);
      TaxInfo tax =
          findTax(
              taxRepository.findAllOrderByName(), product.getTaxCategoryID(), ticket.getCustomer());

      // Igual que JPanelTicket.addTicketLine: precio normal y descuento por volumen
      TicketLineInfo line =
          new TicketLineInfo(
              product,
              1.0,
              product.getPriceSell(),
              tax,
              (Properties) product.getProperties().clone());
      // Sin lote DonPos no puede guardar la factura (ticketlines.lot no acepta NULL)
      line.setLot(lot);
      line.setPriceNormal(line.getPrice());
      volumeDiscountRepository
          .findBest(productId, line.getMultiply())
          .ifPresent(
              discount -> {
                line.setDiscount(discount.getValue());
                line.setPrice(line.getPrice() * (1 - discount.getValue() / 100));
              });

      ticket.addLine(line);
      save(placeId, ticket, waiter);
      return toDto(placeId, ticket);
    }
  }

  public OrderDto removeLine(String placeId, int index, Waiter waiter) {
    synchronized (guard(placeId)) {
      assignerService.signal(placeId, waiter);

      TicketInfo ticket = load(placeId);
      if (index < 0 || index >= ticket.getLinesCount()) {
        throw new WaiterException(
            HttpStatus.NOT_FOUND, "LINE_NOT_FOUND", "Ese producto ya no está en la mesa");
      }

      ticket.removeLine(index);
      save(placeId, ticket, waiter);
      return toDto(placeId, ticket);
    }
  }

  // Igual que JPanelTicket.findLot de DonPos. Con varios lotes DonPos pregunta cuál usar;
  // eso por ahora se hace en caja.
  private String findLot(String productId) {
    List<ProductLot> lots = menuRepository.findLots(productId);
    if (lots.isEmpty()) {
      throw new WaiterException(
          HttpStatus.CONFLICT, "NO_LOT", "El producto no tiene asignado ningún lote");
    }

    for (BundleComponent component : menuRepository.findBundleComponents(productId)) {
      if (menuRepository.findLots(component.productId()).size() != 1) {
        throw new WaiterException(
            HttpStatus.CONFLICT,
            "BUNDLE_LOT",
            "El componente del producto bundle debe tener un solo lote activo");
      }
    }

    if (lots.size() > 1) {
      throw new WaiterException(
          HttpStatus.CONFLICT, "MANY_LOTS", "Este producto tiene varios lotes, agrégalo en caja");
    }
    return lots.get(0).lot();
  }

  private Object guard(String placeId) {
    return tableGuards.computeIfAbsent(placeId, key -> new Object());
  }

  private TicketInfo load(String placeId) {
    return sharedTicketRepository
        .findById(placeId)
        .map(sharedTicket -> TicketSerializer.read(sharedTicket.getContent()))
        .orElseThrow(AssignerService::tableGone);
  }

  // Igual que DonPos al guardar la mesa: el dueño (appuser) es quien la modificó
  private void save(String placeId, TicketInfo ticket, Waiter waiter) {
    long updated =
        sharedTicketRepository.updateContent(
            placeId,
            assignerService.ticketName(ticket, waiter),
            TicketSerializer.write(ticket),
            waiter.id());
    if (updated == 0) {
      throw AssignerService.tableGone();
    }
  }

  private OrderDto toDto(String placeId, TicketInfo ticket) {
    String placeName = placeRepository.findById(placeId).map(Place::getName).orElse(placeId);

    List<OrderDto.Line> lines = new ArrayList<>();
    for (int i = 0; i < ticket.getLinesCount(); i++) {
      TicketLineInfo line = ticket.getLine(i);
      lines.add(
          new OrderDto.Line(
              i,
              line.getProductID(),
              line.getProductName(),
              line.getMultiply(),
              line.getPriceTax(),
              line.getValue()));
    }
    return new OrderDto(placeId, placeName, lines, ticket.getTotal());
  }

  // Igual que DataLogicSales.getProductInfo + ProductInfoExt.getSerializerRead
  private ProductInfoExt toProductInfo(SaleProduct saleProduct) {
    ProductInfoExt product = new ProductInfoExt();
    product.setID(saleProduct.getId());
    product.setReference(saleProduct.getReference());
    product.setCode(saleProduct.getCode());
    product.setName(saleProduct.getName());
    product.setPriceSell(saleProduct.getPriceSell());
    product.setCategoryID(saleProduct.getCategory());
    product.setTaxCategoryID(saleProduct.getTaxCategory());
    product.setAttributeSetID(saleProduct.getAttributeSetId());
    product.setCom(saleProduct.isCom());
    product.setConstant(saleProduct.isConstant());
    product.setService(saleProduct.isService());
    product.setVprice(saleProduct.isVariablePrice());
    product.setVerpatrib(saleProduct.isVerifyAttributes());
    product.setTextTip(saleProduct.getTextTip());
    product.setWarranty(saleProduct.isWarranty());
    product.setPrinter(saleProduct.getPrinter());
    if (saleProduct.getMemoDate() != null) {
      product.setMemoDate(saleProduct.getMemoDate().format(MEMO_DATE));
    }
    product.getProperties().putAll(readProperties(saleProduct.getAttributes()));
    return product;
  }

  // Igual que ImageUtils.readProperties de DonPos
  private Properties readProperties(byte[] content) {
    Properties properties = new Properties();
    if (content != null) {
      try {
        properties.loadFromXML(new ByteArrayInputStream(content));
      } catch (IOException e) {
        // DonPos también ignora propiedades dañadas
      }
    }
    return properties;
  }

  // Igual que TaxesLogic.getTaxInfo de DonPos
  private TaxInfo findTax(List<Tax> taxes, String taxCategoryId, CustomerInfoExt customer) {
    TaxInfo defaultTax = null;

    for (Tax tax : taxes) {
      if (tax.getParentId() == null && tax.getCategory().equals(taxCategoryId)) {
        if ((customer == null || customer.getTaxCustCategoryID() == null)
            && tax.getCustCategory() == null) {
          return toTaxInfo(tax);
        } else if (customer != null
            && customer.getTaxCustCategoryID() != null
            && customer.getTaxCustCategoryID().equals(tax.getCustCategory())) {
          return toTaxInfo(tax);
        }

        if (tax.getCustCategory() == null) {
          defaultTax = toTaxInfo(tax);
        }
      }
    }
    return defaultTax;
  }

  private TaxInfo toTaxInfo(Tax tax) {
    return new TaxInfo(
        tax.getId(),
        tax.getName(),
        tax.getCategory(),
        tax.getCustCategory(),
        tax.getParentId(),
        tax.getRate(),
        tax.isRateCascade(),
        tax.getRateOrder(),
        tax.getLegalCode(),
        tax.getDateStart() == null ? null : Timestamp.valueOf(tax.getDateStart()));
  }
}
