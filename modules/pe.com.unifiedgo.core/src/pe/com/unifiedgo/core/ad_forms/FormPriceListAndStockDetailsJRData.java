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
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;

class FormPriceListAndStockDetailsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormPriceListAndStockDetailsJRData.class);
  private String InitRecordNumber = "0";
  public String productid;
  public String productname;
  public String lastpo1;
  public String lastpo2;
  public String lastpo3;
  public String wname;
  public String wstockonhand;
  public String wstockreserved;
  public String realwstockreserved;
  public String wstockavailable;
  public String wstocknopicking;
  public String viewStkDetailData;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("productname"))
      return productname;
    else if (fieldName.equalsIgnoreCase("lastpo1"))
      return lastpo1;
    else if (fieldName.equalsIgnoreCase("lastpo2"))
      return lastpo2;
    else if (fieldName.equalsIgnoreCase("lastpo3"))
      return lastpo3;
    else if (fieldName.equalsIgnoreCase("wname"))
      return wname;
    else if (fieldName.equalsIgnoreCase("wstockonhand"))
      return wstockonhand;
    else if (fieldName.equalsIgnoreCase("wstockreserved"))
      return wstockreserved;
    else if (fieldName.equalsIgnoreCase("realwstockreserved"))
      return realwstockreserved;
    else if (fieldName.equalsIgnoreCase("wstockavailable"))
      return wstockavailable;
    else if (fieldName.equalsIgnoreCase("wstocknopicking"))
      return wstocknopicking;
    else if (fieldName.equalsIgnoreCase("viewStkDetailData"))
      return viewStkDetailData;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ProductStkData getData(String adOrgId, String adClientId, String mProductId,
      BigDecimal stockDisp, String strViewStkDetailData) throws ServletException {
    Vector<ProductWarehouseData> v_warehouseData = new Vector<ProductWarehouseData>(0);
    Vector<ProductStkDetail> v_stkDetail = new Vector<ProductStkDetail>(0);
    BigDecimal totalStockDisp = stockDisp;
    int BigDecimalScale = 3;

    String strSql = "";
    strSql = "SELECT DISTINCT ON (w.m_warehouse_id) w.m_warehouse_id, (w.value || ' - ' || w.name) as warehouse_name, "
        + "          COALESCE(ppwv.qtyonhand,0) as qty_onhand, COALESCE(ppwv.qtyreserved,0) as qty_reserved, COALESCE(ppwv.qtyonhand-ppwv.qtyreserved,0) as qty_available, "
        + "          coalesce((select sum(loc.qtyonhand) from swa_product_by_anaquel_v loc where loc.locatortype='swa_RCP' and loc.m_product_id='"
        + mProductId
        + "' and loc.m_warehouse_id = ppwv.m_warehouse_id),0) as qty_nopicking "
        + "     FROM scr_warehouse_total_qty_v ppwv"
        + "          INNER JOIN m_product p ON p.m_product_id=ppwv.m_product_id"
        + "          INNER JOIN m_warehouse w ON w.m_warehouse_id=ppwv.m_warehouse_id"
        + "    WHERE p.m_product_id='"
        + mProductId
        + "' "
        + "      AND (ppwv.qtyonhand != 0 OR (ppwv.qtyonhand-ppwv.qtyreserved) != 0)"
        + "      AND (w.em_swa_warehousetype='TR' OR w.em_swa_warehousetype='NO') "
        + "      AND p.ad_client_id='"
        + adClientId
        + "' "
        + "              AND AD_ISORGINCLUDED( '"
        + adOrgId
        + "', p.ad_org_id, '"
        + adClientId
        + "') > -1"
        + "      AND AD_ISORGINCLUDED('"
        + adOrgId
        + "', w.ad_org_id, '"
        + adClientId
        + "') > -1";
    // System.out.println("form-strSql1:" + strSql);
    if (mProductId == "SK-205GM025") {
      System.out.println("form-strSql1:" + strSql);
    }
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        ProductWarehouseData warehouseData = new ProductWarehouseData();
        warehouseData.mWarehouseId = (String) obj[0];
        warehouseData.warehousename = (String) obj[1];

        if (mProductId == "SK-205GM025") {
          System.out.println("warehouseData.qtyonhand:" + warehouseData.qtyonhand);
        }
        warehouseData.qtyonhand = ((BigDecimal) obj[2]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);
        warehouseData.qtyreserved = ((BigDecimal) obj[3]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        warehouseData.realqtyavailable = ((BigDecimal) obj[4]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        warehouseData.qtynopicking = ((BigDecimal) obj[5]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        if (warehouseData.realqtyavailable.compareTo(BigDecimal.ZERO) > 0)
          warehouseData.qtyavailable = warehouseData.realqtyavailable
              .subtract(warehouseData.qtynopicking);
        else
          warehouseData.qtyavailable = BigDecimal.ZERO;

        v_warehouseData.add(warehouseData);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      log4j.error("Exception in query: " + strSql + "Exception:" + ex.getMessage());
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    if ("Y".equals(strViewStkDetailData)) {
      strSql = "SELECT datepromised, COALESCE(poqty,0), documentno "
          + "     FROM sre_product_pending_orders_v " + "    WHERE m_product_id = '" + mProductId
          + "' " + "      AND poqty != 0 " + "      AND AD_ISORGINCLUDED(order_org_id, '" + adOrgId
          + "', '" + adClientId + "') > -1"
          + "    ORDER BY COALESCE(datepromised, dateordered) LIMIT 3";
      System.out.println("strSql2:" + strSql);
      try {
        Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
        List<Object> data = sqlQuery.list();
        for (int k = 0; k < data.size(); k++) {
          Object[] obj = (Object[]) data.get(k);

          ProductStkDetail stkDtl = new ProductStkDetail();
          stkDtl.datepromised = (Date) obj[0];
          stkDtl.poqty = ((BigDecimal) obj[1]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
          // totalStockDisp = totalStockDisp.add((BigDecimal) obj[1]);
          stkDtl.documentno = (String) obj[2];
          // stkDtl.aproxqtyavail = totalStockDisp.setScale(BigDecimalScale, RoundingMode.HALF_UP);

          // if (obj[2] != null && !"".equals((String) obj[2])) {
          // RequisitionLine requiline = OBDal.getInstance().get(RequisitionLine.class,
          // (String) obj[2]);
          // stkDtl.documentno = (requiline.getRequisition().getSreSalesorder() == null) ? "--"
          // : requiline.getRequisition().getSreSalesorder().getDocumentNo();
          // }

          v_stkDetail.add(stkDtl);
        }

      } catch (Exception ex) {
        ex.printStackTrace();
        log4j.error("Exception in query: " + strSql + "Exception:" + ex.getMessage());
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }

    ProductStkData ProductStkData = new ProductStkData(mProductId);
    ProductStkData.warehouse = v_warehouseData;
    ProductStkData.po_stockintransit = v_stkDetail;

    return ProductStkData;
  }

  public static FormPriceListAndStockDetailsJRData[] select(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mProductId, String strStockDisp,
      String strViewStkDetailData) throws ServletException {
    return select(vars, adOrgId, adClientId, mProductId, strStockDisp, strViewStkDetailData, 0, 0);
  }

  public static FormPriceListAndStockDetailsJRData[] select(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mProductId, String strStockDisp,
      String strViewStkDetailData, int firstRegister, int numberRegisters) throws ServletException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);

    ProductStkDetail stock_data;
    ProductWarehouseData warehouse_data;
    String stockonhand = "", stockreserved = "", stockavailable = "", qtynopicking = "", wname = "";
    boolean filllastpo1 = false, filllastpo2 = false;
    long countRecord = 1;
    BigDecimal stockDisp;
    try {
      stockDisp = (strStockDisp != null && !"".equals(strStockDisp)) ? (new BigDecimal(
          (dfQty.parse(strStockDisp)).toString())) : BigDecimal.ZERO;
    } catch (Exception ex) {
      log4j.error("Exception 2:" + ex.getMessage());
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ProductStkData product = getData(adOrgId, adClientId, mProductId, stockDisp,
        strViewStkDetailData);

    FormPriceListAndStockDetailsJRData objProductStkData[] = new FormPriceListAndStockDetailsJRData[1];
    objProductStkData[0] = new FormPriceListAndStockDetailsJRData();
    objProductStkData[0].productid = product.productid;
    objProductStkData[0].productname = product.productname;

    objProductStkData[0].lastpo1 = "--";
    objProductStkData[0].lastpo2 = "--";
    objProductStkData[0].lastpo3 = "--";
    for (int l = 0; l < product.po_stockintransit.size(); l++) {
      stock_data = product.po_stockintransit.get(l);
      String dt = "Doc. de Compra:&nbsp" + stock_data.documentno + "<br>&nbsp" + "Fecha Aprox:"
          + "&nbsp&nbsp"
          + ((stock_data.datepromised != null) ? sdf.format(stock_data.datepromised) : "--")
          + "<br>&nbsp" + "Cantidad:" + "&nbsp&nbsp&nbsp&nbsp&nbsp"
          + dfQty.format(stock_data.poqty);
      // + "<br>&nbsp" + "Disp:" + "&nbsp&nbsp&nbsp&nbsp&nbsp"
      // + dfQty.format(stock_data.aproxqtyavail);
      if (!filllastpo1) {
        objProductStkData[0].lastpo1 = dt;
        filllastpo1 = true;
      } else if (!filllastpo2) {
        objProductStkData[0].lastpo2 = dt;
        filllastpo2 = true;
      } else {
        objProductStkData[0].lastpo3 = dt;
      }
    }

    for (int i = 0; i < product.warehouse.size(); i++) {
      warehouse_data = product.warehouse.get(i);
      wname = wname + warehouse_data.warehousename
          + ((i < (product.warehouse.size() - 1)) ? "<br>&nbsp" : "");
      stockonhand = stockonhand + dfQty.format(warehouse_data.qtyonhand)
          + ((i < (product.warehouse.size() - 1)) ? "<br>&nbsp" : "");
      stockreserved = stockreserved + dfQty.format(warehouse_data.qtyreserved)
          + ((i < (product.warehouse.size() - 1)) ? "<br>&nbsp" : "");
      stockavailable = stockavailable + dfQty.format(warehouse_data.qtyavailable)
          + ((i < (product.warehouse.size() - 1)) ? "<br>&nbsp" : "");
      qtynopicking = qtynopicking + dfQty.format(warehouse_data.qtynopicking)
          + ((i < (product.warehouse.size() - 1)) ? "<br>&nbsp" : "");
    }
    objProductStkData[0].wname = (!"".equals(wname)) ? wname : "--";
    objProductStkData[0].wstockonhand = (!"".equals(stockonhand)) ? stockonhand : "--";
    objProductStkData[0].wstockreserved = (!"".equals(stockreserved)) ? stockreserved : "--";
    objProductStkData[0].wstockavailable = (!"".equals(stockavailable)) ? stockavailable : "--";
    objProductStkData[0].wstocknopicking = (!"".equals(qtynopicking)) ? qtynopicking : "--";

    objProductStkData[0].viewStkDetailData = ("Y".equals(strViewStkDetailData)) ? "Y" : "N";

    objProductStkData[0].rownum = Long.toString(countRecord);
    objProductStkData[0].InitRecordNumber = Integer.toString(firstRegister);

    return (objProductStkData);
  }

  public static FormPriceListAndStockDetailsJRData[] set() throws ServletException {
    FormPriceListAndStockDetailsJRData objProductStkData[] = new FormPriceListAndStockDetailsJRData[1];
    objProductStkData[0] = new FormPriceListAndStockDetailsJRData();
    objProductStkData[0].productid = "";
    objProductStkData[0].productname = "";
    objProductStkData[0].lastpo1 = "--";
    objProductStkData[0].lastpo2 = "--";
    objProductStkData[0].lastpo3 = "--";
    objProductStkData[0].wname = "";
    objProductStkData[0].wstockonhand = "";
    objProductStkData[0].wstockreserved = "";
    objProductStkData[0].wstockavailable = "";
    objProductStkData[0].wstocknopicking = "";
    return objProductStkData;
  }
}

class ProductStkData {
  public String productid;
  public String productname;
  public Vector<ProductWarehouseData> warehouse;
  public Vector<ProductStkDetail> po_stockintransit;

  public ProductStkData() {
    productid = "";
    productname = "--";
  }

  public ProductStkData(String mProductId) {
    Product p = OBDal.getInstance().get(Product.class, mProductId);
    productid = mProductId;
    productname = (p != null) ? p.getSearchKey() + " - " + p.getName() : "--";
  }
}

class ProductWarehouseData {
  public String mWarehouseId;
  public String warehousename;
  public BigDecimal qtyonhand;
  public BigDecimal qtyreserved;
  public BigDecimal qtyavailable;
  public BigDecimal realqtyavailable;
  public BigDecimal qtynopicking;

  public ProductWarehouseData() {
    warehousename = "--";
    qtyonhand = BigDecimal.ZERO;
    qtyreserved = BigDecimal.ZERO;
    qtyavailable = BigDecimal.ZERO;
    realqtyavailable = BigDecimal.ZERO;
    qtynopicking = BigDecimal.ZERO;
  }
}

class ProductStkDetail {
  public String documentno;
  public Date datepromised;
  public BigDecimal poqty;

  // public BigDecimal aproxqtyavail;

  public ProductStkDetail() {
    documentno = "--";
    poqty = BigDecimal.ZERO;
  }
}
