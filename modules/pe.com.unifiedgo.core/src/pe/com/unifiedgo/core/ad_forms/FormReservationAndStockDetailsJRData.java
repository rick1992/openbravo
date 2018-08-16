//Sqlc generated V1.O00-1
package pe.com.unifiedgo.core.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;

class FormReservationAndStockDetailsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormReservationAndStockDetailsJRData.class);
  private String InitRecordNumber = "0";
  public String mproductid;
  public String mproductname;
  public String reservationid;
  public String reservationref;
  public String reservationname;
  public String reservationdate;
  public String resrefclient;
  public String reservationuser;
  public String qtyreserved;
  public String quantity;
  public String orgname;
  public String adtabid;
  public String recordid;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("mproductid"))
      return mproductid;
    else if (fieldName.equalsIgnoreCase("mproductname"))
      return mproductname;
    else if (fieldName.equalsIgnoreCase("reservationid"))
      return reservationid;
    else if (fieldName.equalsIgnoreCase("reservationref"))
      return reservationref;
    else if (fieldName.equalsIgnoreCase("reservationname"))
      return reservationname;
    else if (fieldName.equalsIgnoreCase("reservationdate"))
      return reservationdate;
    else if (fieldName.equalsIgnoreCase("resrefclient"))
      return resrefclient;
    else if (fieldName.equalsIgnoreCase("reservationuser"))
      return reservationuser;
    else if (fieldName.equalsIgnoreCase("qtyreserved"))
      return qtyreserved;
    else if (fieldName.equalsIgnoreCase("quantity"))
      return quantity;
    else if (fieldName.equalsIgnoreCase("orgname"))
      return orgname;
    else if (fieldName.equalsIgnoreCase("adtabid"))
      return adtabid;
    else if (fieldName.equalsIgnoreCase("recordid"))
      return recordid;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static Vector<ReservationData> getData(String adOrgId, String adClientId,
      String mProductId, String strM_Warehouse_ID) throws ServletException {
    Vector<ReservationData> reserv_data = new Vector<ReservationData>(0);

    OBCriteria<Reservation> m_reservation = OBDal.getInstance().createCriteria(Reservation.class);
    m_reservation.add(Restrictions.eq(Reservation.PROPERTY_PRODUCT,
        OBDal.getInstance().get(Product.class, mProductId)));
    m_reservation.add(Restrictions.ltProperty(Reservation.PROPERTY_RELEASED,
        Reservation.PROPERTY_QUANTITY));
    m_reservation.add(Restrictions.or(Restrictions.eq(Reservation.PROPERTY_RESSTATUS, "CO"),
        Restrictions.eq(Reservation.PROPERTY_RESSTATUS, "HO")));
    m_reservation.add(Restrictions.eq(Reservation.PROPERTY_ACTIVE, true));
    m_reservation.add(Restrictions.eq(Reservation.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, adClientId)));
    m_reservation.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( ad_org_id, " + "'" + adOrgId
        + "','" + adClientId + "') > -1"));
    m_reservation.addOrderBy(Reservation.PROPERTY_CREATIONDATE, false);
    m_reservation.addOrderBy(Reservation.PROPERTY_QUANTITY, false);
    List<Reservation> reservation_list = m_reservation.list();
    Reservation res;
    for (int k = 0; k < reservation_list.size(); k++) {
      res = reservation_list.get(k);
      if (strM_Warehouse_ID != null && !"".equals(strM_Warehouse_ID)) {
        String resMWarehouseId = (res.getSalesOrderLine() != null) ? res.getSalesOrderLine()
            .getWarehouse().getId() : ((res.getWarehouse() != null) ? res.getWarehouse().getId()
            : "");
        if (!strM_Warehouse_ID.equals(resMWarehouseId))
          continue;
      }

      ReservationData data = new ReservationData();
      data.mproductid = res.getProduct().getId();
      data.mproductname = res.getProduct().getSearchKey() + " - " + res.getProduct().getName();
      if (res.getSalesOrderLine() != null) {
        data.reservationref = "Pedido de Venta";
        data.recordid = res.getSalesOrderLine().getSalesOrder().getId();
        data.adtableid = "186";
        data.reservationname = res.getSalesOrderLine().getSalesOrder().getDocumentNo() + " - "
            + res.getSalesOrderLine().getLineNo();
        data.resrefclient = res.getSalesOrderLine().getSalesOrder().getBusinessPartner().getName();
        data.reservationuser = (res.getSalesOrderLine().getSalesOrder().getSalesRepresentative() != null) ? (res
            .getSalesOrderLine().getSalesOrder().getSalesRepresentative()).getName() : res
            .getCreatedBy().getName();

      } else if (res.getSwaRequerepoDetail() != null) {
        data.reservationref = "Transferencia";
        data.recordid = res.getSwaRequerepoDetail().getSWARequerimientoreposicion().getId();
        data.adtableid = "687964F89F964AD3B5CDCEE3735E9460";
        data.reservationname = res.getSwaRequerepoDetail().getSWARequerimientoreposicion()
            .getDocumentNo()
            + " - " + res.getSwaRequerepoDetail().getLineNo();
        data.resrefclient = res.getSwaRequerepoDetail().getSWARequerimientoreposicion()
            .getBusinessPartner().getName();
        data.reservationuser = res.getSwaRequerepoDetail().getSWARequerimientoreposicion()
            .getCreatedBy().getName();

      } else if (res.getSwaMInoutline() != null) {
        data.reservationref = "Documento de Salida";
        data.recordid = res.getSwaMInoutline().getShipmentReceipt().getId();
        data.adtableid = "257";
        data.reservationname = res.getSwaMInoutline().getShipmentReceipt().getDocumentNo() + " - "
            + res.getSwaMInoutline().getLineNo();
        data.resrefclient = res.getSwaMInoutline().getShipmentReceipt().getBusinessPartner()
            .getName();
        data.reservationuser = res.getSwaMInoutline().getShipmentReceipt().getCreatedBy().getName();

      } else if (res.getSwaMProductionline() != null) {
        data.reservationref = "Producción LDM";
        data.recordid = res.getSwaMProductionline().getProductionPlan().getProduction().getId();
        data.adtableid = "319";
        data.reservationname = res.getSwaMProductionline().getProductionPlan().getProduction()
            .getDocumentNo()
            + " - "
            + res.getSwaMProductionline().getProductionPlan().getLineNo()
            + " - "
            + res.getSwaMProductionline().getLineNo();
        data.resrefclient = res.getSwaMProductionline().getClient().getName();
        data.reservationuser = res.getSwaMProductionline().getProductionPlan().getProduction()
            .getCreatedBy().getName();

      } else if (res.getSwaServiceorderline() != null) {
        data.reservationref = "Orden de Servicio";
        data.recordid = res.getSwaServiceorderline().getSalesOrder().getId();
        data.adtableid = "294";
        data.reservationname = res.getSwaServiceorderline().getSalesOrder().getDocumentNo() + " - "
            + res.getSwaServiceorderline().getLineNo();
        data.resrefclient = res.getSwaServiceorderline().getClient().getName();
        data.reservationuser = res.getSwaServiceorderline().getSalesOrder().getCreatedBy()
            .getName();

      } else if (res.getSwaMInventoryline() != null) {
        data.reservationref = "Diferencia por Inventario";
        data.recordid = res.getSwaMInventoryline().getPhysInventory().getId();
        data.adtableid = "255";
        data.reservationname = res.getSwaMInventoryline().getPhysInventory().getDocumentNo()
            + " - " + res.getSwaMInventoryline().getLineNo();
        data.resrefclient = res.getSwaMInventoryline().getClient().getName();
        data.reservationuser = res.getSwaMInventoryline().getPhysInventory().getCreatedBy()
            .getName();
      }

      data.reservationdate = res.getCreationDate();
      data.qtyreserved = res.getReservedQty().subtract(
          (res.getReleased() != null) ? res.getReleased() : BigDecimal.ZERO);
      data.quantity = res.getQuantity();
      data.orgname = res.getOrganization().getName();
      data.reservationid = res.getId();

      reserv_data.add(data);
    }

    if (reserv_data.size() == 0) {
      reserv_data.add(new ReservationData(mProductId));
    }
    return reserv_data;
  }

  public static FormReservationAndStockDetailsJRData[] select(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mProductId, String strM_Warehouse_ID)
      throws ServletException {
    return select(vars, adOrgId, adClientId, mProductId, strM_Warehouse_ID, 0, 0);
  }

  public static FormReservationAndStockDetailsJRData[] select(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mProductId, String strM_Warehouse_ID,
      int firstRegister, int numberRegisters) throws ServletException {
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat df = Utility.getFormat(vars, "qtyExcel");
    df.setRoundingMode(RoundingMode.HALF_UP);
    long countRecord = 0;

    Vector<ReservationData> data = getData(adOrgId, adClientId, mProductId, strM_Warehouse_ID);
    for (int k = 0; k < data.size(); k++) {
      countRecord++;

      FormReservationAndStockDetailsJRData objReservStockDetail = new FormReservationAndStockDetailsJRData();
      objReservStockDetail.mproductid = data.get(k).mproductid;
      objReservStockDetail.mproductname = data.get(k).mproductname;

      objReservStockDetail.reservationid = data.get(k).reservationid;
      objReservStockDetail.reservationref = data.get(k).reservationref;
      objReservStockDetail.reservationname = data.get(k).reservationname;
      objReservStockDetail.resrefclient = data.get(k).resrefclient;
      objReservStockDetail.reservationuser = data.get(k).reservationuser;

      objReservStockDetail.reservationdate = (data.get(k).reservationdate != null) ? sdf
          .format(data.get(k).reservationdate) : "--";
      objReservStockDetail.qtyreserved = (data.get(k).qtyreserved.compareTo(BigDecimal.ZERO) != 0) ? df
          .format(data.get(k).qtyreserved) : "--";
      objReservStockDetail.quantity = (data.get(k).quantity.compareTo(BigDecimal.ZERO) != 0) ? df
          .format(data.get(k).quantity) : "--";
      objReservStockDetail.orgname = data.get(k).orgname;

      objReservStockDetail.adtabid = data.get(k).adtableid;
      objReservStockDetail.recordid = data.get(k).recordid;

      objReservStockDetail.rownum = Long.toString(countRecord);
      objReservStockDetail.InitRecordNumber = Integer.toString(firstRegister);

      vector.addElement(objReservStockDetail);
    }

    FormReservationAndStockDetailsJRData objReservStockDetail[] = new FormReservationAndStockDetailsJRData[vector
        .size()];
    vector.copyInto(objReservStockDetail);

    return (objReservStockDetail);
  }

  public static FormReservationAndStockDetailsJRData[] set() throws ServletException {
    FormReservationAndStockDetailsJRData objReservStockDetail[] = new FormReservationAndStockDetailsJRData[1];
    objReservStockDetail[0] = new FormReservationAndStockDetailsJRData();
    objReservStockDetail[0].mproductid = "";
    objReservStockDetail[0].mproductname = "";
    objReservStockDetail[0].reservationid = "";
    objReservStockDetail[0].reservationref = "";
    objReservStockDetail[0].reservationname = "";
    objReservStockDetail[0].reservationdate = "";
    objReservStockDetail[0].resrefclient = "";
    objReservStockDetail[0].reservationuser = "";
    objReservStockDetail[0].qtyreserved = "";
    objReservStockDetail[0].quantity = "";
    objReservStockDetail[0].orgname = "";
    return objReservStockDetail;
  }
}

class ReservationData {
  public String mproductid;
  public String mproductname;
  public String reservationid;
  public String reservationref;
  public String reservationname;
  public Date reservationdate;
  public String resrefclient;
  public String reservationuser;
  public BigDecimal qtyreserved;
  public BigDecimal quantity;
  public String adtableid;
  public String recordid;
  public String orgname;

  public ReservationData() {
    mproductname = "--";
    reservationid = "--";
    reservationref = "--";
    reservationname = "Sistema";
    reservationdate = null;
    resrefclient = "--";
    reservationuser = "--";
    qtyreserved = BigDecimal.ZERO;
    quantity = BigDecimal.ZERO;
    orgname = "--";
  }

  public ReservationData(String mProductId) {
    Product product = OBDal.getInstance().get(Product.class, mProductId);
    mproductid = product.getId();
    mproductname = product.getSearchKey() + " - " + product.getName();
    reservationid = "--";
    reservationref = "--";
    reservationname = "--";
    reservationdate = null;
    resrefclient = "--";
    reservationuser = "--";
    qtyreserved = BigDecimal.ZERO;
    quantity = BigDecimal.ZERO;
    orgname = "--";
  }
}
