//Sqlc generated V1.O00-1
package pe.com.unifiedgo.sales.ad_reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.QueryTimeOutUtil;

import pe.com.unifiedgo.accounting.SCO_Utils;

class ReportPriceListAndStockJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportPriceListAndStockJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String productid;
  public String searchkey;
  public String brand;
  public String productname;
  public String stocknopicking;
  public String stockonhand;
  public String stockonhandavailable;
  public String stockreserved;
  public String realstockonhandavailable;
  public String stockSummaryDetail;

  public String mainPENlistprice;
  public String mainUSDlistprice;

  public String padre;
  public String id;
  public String name;

  public String PENlistprice;
  public String PENIGV;
  public String PENlistpriceIGV;
  public String USDlistprice;
  public String USDIGV;
  public String USDlistpriceIGV;
  public String taxrate;

  public String PENlistpriceOrig;
  public String USDlistpriceOrig;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("brand"))
      return brand;
    else if (fieldName.equalsIgnoreCase("productname"))
      return productname;
    else if (fieldName.equalsIgnoreCase("stockonhand"))
      return stockonhand;
    else if (fieldName.equalsIgnoreCase("stockonhandavailable"))
      return stockonhandavailable;
    else if (fieldName.equalsIgnoreCase("realstockonhandavailable"))
      return realstockonhandavailable;
    else if (fieldName.equalsIgnoreCase("stockreserved"))
      return stockreserved;
    else if (fieldName.equalsIgnoreCase("stocknopicking"))
      return stocknopicking;
    else if (fieldName.equalsIgnoreCase("stockSummaryDetail"))
      return stockSummaryDetail;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("PENlistprice"))
      return PENlistprice;
    else if (fieldName.equalsIgnoreCase("PENIGV"))
      return PENIGV;
    else if (fieldName.equalsIgnoreCase("PENlistpriceIGV"))
      return PENlistpriceIGV;
    else if (fieldName.equalsIgnoreCase("USDlistprice"))
      return USDlistprice;
    else if (fieldName.equalsIgnoreCase("USDIGV"))
      return USDIGV;
    else if (fieldName.equalsIgnoreCase("USDlistpriceIGV"))
      return USDlistpriceIGV;
    else if (fieldName.equalsIgnoreCase("taxrate"))
      return taxrate;
    else if (fieldName.equalsIgnoreCase("PENlistpriceOrig"))
      return PENlistpriceOrig;
    else if (fieldName.equalsIgnoreCase("USDlistpriceOrig"))
      return USDlistpriceOrig;
    else if (fieldName.equalsIgnoreCase("mainPENlistprice"))
      return mainPENlistprice;
    else if (fieldName.equalsIgnoreCase("mainUSDlistprice"))
      return mainUSDlistprice;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReportPriceListAndStockJRData[] selectProductPrice(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mPENPriceListID, String mUSDPriceListID,
      String cTaxID, String mProductId) throws ServletException {
    return selectProductPrice(vars, adOrgId, adClientId, mPENPriceListID, mUSDPriceListID, cTaxID,
        mProductId, 0, 0);
  }

  public static ReportPriceListAndStockJRData[] selectProductPrice(VariablesSecureApp vars,
      String adOrgId, String adClientId, String mPENPriceListID, String mUSDPriceListID,
      String cTaxID, String mProductId, int firstRegister, int numberRegisters)
      throws ServletException {
    ConnectionProvider conn = new DalConnectionProvider();
    DecimalFormat dfPrices = Utility.getFormat(vars, "priceInform");
    DecimalFormat dfPricesEdit = Utility.getFormat(vars, "priceEdition");
    dfPrices.setRoundingMode(RoundingMode.HALF_UP);
    dfPricesEdit.setRoundingMode(RoundingMode.HALF_UP);
    int BigDecimalScale = 5;

    Organization org = OBDal.getInstance().get(Organization.class, adOrgId);
    TaxRate tax = OBDal.getInstance().get(TaxRate.class, cTaxID);
    BigDecimal PENlistprice, PENIGV, PENlistpriceIGV;
    BigDecimal USDlistprice, USDIGV, USDlistpriceIGV;

    // PEN list prices
    String PEN_product_prices[] = SCO_Utils.getProductPricesByDate(conn, org.getId(), mProductId,
        mPENPriceListID, new Date());

    PENlistprice = new BigDecimal(PEN_product_prices[0]);
    PENIGV = PENlistprice.multiply(tax.getRate());
    PENlistpriceIGV = PENlistprice.multiply(tax.getRate().add(new BigDecimal(100.00000))).divide(
        new BigDecimal(100.00000), BigDecimalScale, RoundingMode.HALF_UP);

    // USD list prices
    String USD_product_prices[] = SCO_Utils.getProductPricesByDate(conn, org.getId(), mProductId,
        mUSDPriceListID, new Date());

    USDlistprice = new BigDecimal(USD_product_prices[0]);
    USDIGV = USDlistprice.multiply(tax.getRate());
    USDlistpriceIGV = USDlistprice.multiply(tax.getRate().add(new BigDecimal(100.00000))).divide(
        new BigDecimal(100.00000), BigDecimalScale, RoundingMode.HALF_UP);

    //
    // Formatting the Output data
    ReportPriceListAndStockJRData[] objPriceListAndStkData = new ReportPriceListAndStockJRData[1];
    objPriceListAndStkData[0] = new ReportPriceListAndStockJRData();
    objPriceListAndStkData[0].productid = mProductId;
    objPriceListAndStkData[0].taxrate = dfPrices.format(tax.getRate());

    objPriceListAndStkData[0].PENlistprice = dfPrices.format(PENlistprice);
    objPriceListAndStkData[0].PENIGV = dfPrices.format(PENIGV);
    objPriceListAndStkData[0].PENlistpriceIGV = dfPrices.format(PENlistpriceIGV);
    objPriceListAndStkData[0].USDlistprice = dfPrices.format(USDlistprice);
    objPriceListAndStkData[0].USDIGV = dfPrices.format(USDIGV);
    objPriceListAndStkData[0].USDlistpriceIGV = dfPrices.format(USDlistpriceIGV);

    objPriceListAndStkData[0].PENlistpriceOrig = dfPricesEdit.format(PENlistprice);
    objPriceListAndStkData[0].USDlistpriceOrig = dfPricesEdit.format(USDlistprice);

    return objPriceListAndStkData;
  }

  public static BigDecimal calculateNoPickingStockByProduct(String mProductId) throws Exception {
    Query q;
    q = OBDal
        .getInstance()
        .getSession()
        .createSQLQuery(
            "SELECT COALESCE(sum(loc.qtyonhand),0) FROM swa_product_by_anaquel_v loc "
                + " WHERE loc.locatortype='swa_RCP' AND loc.m_product_id='" + mProductId + "'; ");
    System.out.println("ACAAA"
        + "SELECT COALESCE(sum(loc.qtyonhand),0) FROM swa_product_by_anaquel_v loc "
        + " WHERE loc.locatortype='swa_RCP' AND loc.m_product_id='" + mProductId + "' ");

    BigDecimal stocknopicking = (BigDecimal) q.uniqueResult();
    if (stocknopicking == null)
      return BigDecimal.ZERO;
    return stocknopicking;
  }

  public static Vector<PriceListStockData> getData(String adOrgId, String adClientId,
      String mProductId, String strmBrandID, String strStockMin, String strStockMax,
      String mPENPriceListID, String mUSDPriceListID) throws ServletException {
    ConnectionProvider conn = new DalConnectionProvider();

    Vector<PriceListStockData> vt_data = new Vector<PriceListStockData>(0);
    int BigDecimalScale = 5;
    Product product;

    String strProductIds = getProductIdsBySearchKey(adClientId, adOrgId, mProductId.trim(),
        strmBrandID);
    if ("".equals(strProductIds) || strProductIds == null) {
      return vt_data;
    }

    BigDecimal PENlistprice, USDlistprice;
    Organization org = OBDal.getInstance().get(Organization.class, adOrgId);

    String strSql = "";
    strSql = "SELECT p.m_product_id, "
        + "          coalesce(sum(ppwv.qtyonhand),0) AS qty_onhand, "
        + "          coalesce(sum(ppwv.qtyreserved),0) AS qty_reserved, "
        + "          coalesce(sum((ppwv.qtyonhand-ppwv.qtyreserved)),0) AS qty_available "
        + "     FROM m_warehouse w "
        + "          JOIN m_product p on p.m_product_id IN ("
        + strProductIds
        + ") "
        + "          JOIN c_uom uom ON p.c_uom_id = uom.c_uom_id "
        + "          LEFT JOIN scr_warehouse_total_qty_v ppwv ON ppwv.m_product_id=p.m_product_id AND ppwv.m_warehouse_id=w.m_warehouse_id "
        + "    WHERE w.em_swa_warehousetype='NO' ";
    strSql = strSql
        + ((strStockMin == null || "".equals(strStockMin)) ? ""
            : "          AND ppwv.qtyonhand >= '" + strStockMin + "'  ");
    strSql = strSql
        + ((strStockMax == null || "".equals(strStockMax)) ? ""
            : "          AND ppwv.qtyonhand <= '" + strStockMax + "'  ");

    strSql = strSql + "  AND AD_ISORGINCLUDED('" + adOrgId + "', p.ad_org_id, '" + adClientId
        + "') > -1" + "              AND AD_ISORGINCLUDED('" + adOrgId + "', w.ad_org_id, '"
        + adClientId + "') > -1 GROUP BY p.m_product_id " + "    ORDER BY p.m_product_id ";
    System.out.println("R-SQL:" + strSql);
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        PriceListStockData stk_data = new PriceListStockData();
        stk_data.orgid = adOrgId;
        stk_data.productid = (String) obj[0];

        product = OBDal.getInstance().get(Product.class, stk_data.productid);
        stk_data.searchkey = product.getSearchKey();
        stk_data.productname = formatProductDescToHTMLString(product.getName(),
            selectProductUOMSymbolTrl(conn, stk_data.productid));

        stk_data.brand = (product.getBrand() == null || "".equals(product.getBrand())) ? "--"
            : product.getBrand().getName();

        stk_data.stockonhand = ((BigDecimal) obj[1])
            .setScale(BigDecimalScale, RoundingMode.HALF_UP);

        stk_data.stockreserved = ((BigDecimal) obj[2]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        stk_data.realstockonhandavailable = ((BigDecimal) obj[3]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        stk_data.stocknopicking = calculateNoPickingStockByProduct(stk_data.productid);

        if (stk_data.realstockonhandavailable.compareTo(BigDecimal.ZERO) > 0)
          stk_data.stockonhandavailable = stk_data.realstockonhandavailable
              .subtract(stk_data.stocknopicking);
        else
          stk_data.stockonhandavailable = BigDecimal.ZERO;

        // Resumen Stock Disp.
        stk_data.stk_sample = getStockSummaryDetail(adOrgId, adClientId, stk_data.productid);

        // PEN list prices
        String PEN_product_prices[] = SCO_Utils.getProductPricesByDate(conn, org.getId(),
            stk_data.productid, mPENPriceListID, new Date());
        PENlistprice = new BigDecimal(PEN_product_prices[0]);

        stk_data.mainPENlistprice = PENlistprice;

        // USD list prices
        String USD_product_prices[] = SCO_Utils.getProductPricesByDate(conn, org.getId(),
            stk_data.productid, mUSDPriceListID, new Date());
        USDlistprice = new BigDecimal(USD_product_prices[0]);

        stk_data.mainUSDlistprice = USDlistprice;

        vt_data.add(stk_data);
      }

    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    return vt_data;
  }

  private static String formatProductDescToHTMLString(String desc, String uom) {
    int max_length = 54;

    String new_str = "";
    if (desc.length() > max_length) {
      int index = 0;
      while (index < desc.length()) {
        if (index == 0) {
          new_str = desc.substring(index, Math.min(index + max_length, desc.length()));
        } else {
          new_str = new_str + "<br>"
              + desc.substring(index, Math.min(index + max_length, desc.length()));
        }
        index += max_length;
      }

    } else {
      new_str = desc;
    }

    new_str = new_str + "<br>" + "<b>U.Medida:</b> " + (uom != null ? uom : "");
    return new_str;
  }

  public static ReportPriceListAndStockJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String mProductId, String strmBrandID, String strStockMin,
      String strStockMax, String mPENPriceListID, String mUSDPriceListID) throws ServletException {
    return select(vars, adOrgId, adClientId, mProductId, strmBrandID, strStockMin, strStockMax,
        mPENPriceListID, mUSDPriceListID, 0, 0);
  }

  public static ReportPriceListAndStockJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String mProductId, String strmBrandID, String strStockMin,
      String strStockMax, String mPENPriceListID, String mUSDPriceListID, int firstRegister,
      int numberRegisters) throws ServletException {
    DecimalFormat dfPrices = Utility.getFormat(vars, "priceInform");
    DecimalFormat df = Utility.getFormat(vars, "qtyExcel");
    df.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    Vector<PriceListStockData> data = getData(adOrgId, adClientId, mProductId, strmBrandID,
        strStockMin.trim(), strStockMax.trim(), mPENPriceListID, mUSDPriceListID);
    // Iterator<String> dt;
    // int j = 0;
    for (int i = 0; i < data.size(); i++) {
      countRecord++;

      ReportPriceListAndStockJRData objPriceListAndStkData = new ReportPriceListAndStockJRData();
      objPriceListAndStkData.orgid = data.get(i).orgid;
      objPriceListAndStkData.productid = data.get(i).productid;

      objPriceListAndStkData.searchkey = data.get(i).searchkey;
      objPriceListAndStkData.productname = data.get(i).productname;
      objPriceListAndStkData.brand = data.get(i).brand;
      objPriceListAndStkData.stockonhand = df.format(data.get(i).stockonhand);
      objPriceListAndStkData.stockonhandavailable = df.format(data.get(i).stockonhandavailable);
      objPriceListAndStkData.realstockonhandavailable = df
          .format(data.get(i).realstockonhandavailable);
      objPriceListAndStkData.stockreserved = df.format(data.get(i).stockreserved);
      objPriceListAndStkData.stocknopicking = df.format(data.get(i).stocknopicking);

      objPriceListAndStkData.stockSummaryDetail = "";
      for (int k = 0; k < data.get(i).stk_sample.size(); k++) {
        objPriceListAndStkData.stockSummaryDetail = objPriceListAndStkData.stockSummaryDetail
            + ((k > 0) ? "<br>&nbsp" : "") + data.get(i).stk_sample.get(k).warehouseid
            + " - Disp:&nbsp" + df.format(data.get(i).stk_sample.get(k).stockavailable)
            + " / No Disp:&nbsp" + df.format(data.get(i).stk_sample.get(k).stocknopicking);
      }
      // dt = data.get(i).stk_sample.keySet().iterator();
      // j = 0;
      // while (dt.hasNext()) {
      // String sk = dt.next();
      // objPriceListAndStkData.stockSummaryDetail =
      // objPriceListAndStkData.stockSummaryDetail
      // + ((j > 0) ? "<br>&nbsp" : "") + sk + " - Disp:&nbsp"
      // + df.format(data.get(i).stk_sample.get(sk));
      // j++;
      // }

      objPriceListAndStkData.mainPENlistprice = "S/."
          + dfPrices.format(data.get(i).mainPENlistprice);
      objPriceListAndStkData.mainUSDlistprice = "$" + dfPrices.format(data.get(i).mainUSDlistprice);

      objPriceListAndStkData.rownum = Long.toString(countRecord);
      objPriceListAndStkData.InitRecordNumber = Integer.toString(firstRegister);

      vector.addElement(objPriceListAndStkData);
    }

    ReportPriceListAndStockJRData objPriceListAndStkData[] = new ReportPriceListAndStockJRData[vector
        .size()];
    vector.copyInto(objPriceListAndStkData);

    return (objPriceListAndStkData);
  }

  public static List<WarehouseStockData> getStockSummaryDetail(String adOrgId, String adClientId,
      String mProductId) throws ServletException {
    HashMap<String, BigDecimal> stk_detail = new HashMap<String, BigDecimal>();
    int BigDecimalScale = 5;
    Warehouse warehouse;

    List<WarehouseStockData> wdata = new ArrayList<WarehouseStockData>();
    String strSql = "SELECT ppwv.m_warehouse_id, COALESCE(ppwv.qtyonhand-ppwv.qtyreserved,0) as qty_available,"
        + "                          coalesce((select sum(loc.qtyonhand) from swa_product_by_anaquel_v loc where loc.m_warehouse_id=ppwv.m_warehouse_id and loc.locatortype='swa_RCP' and loc.m_product_id='"
        + mProductId
        + "'"
        + "),0) as qtynopicking"
        + "     FROM scr_warehouse_total_qty_v ppwv "
        + "          INNER JOIN m_product p ON p.m_product_id=ppwv.m_product_id "
        + "          INNER JOIN m_warehouse w ON w.m_warehouse_id=ppwv.m_warehouse_id "
        + "    WHERE ppwv.m_product_id='"
        + mProductId
        + "'"
        + "      AND (ppwv.qtyonhand!= 0 OR (ppwv.qtyonhand-ppwv.qtyreserved) != 0)"
        + "      AND w.em_swa_warehousetype='NO'"
        + "      AND ppwv.ad_client_id='"
        + adClientId
        + "'"
        + "      AND AD_ISORGINCLUDED('"
        + adOrgId
        + "', p.ad_org_id, '"
        + adClientId
        + "') > -1"
        + "      AND AD_ISORGINCLUDED('"
        + adOrgId
        + "', w.ad_org_id, '"
        + adClientId
        + "') > -1 "
        + "    GROUP BY ppwv.m_warehouse_id,ppwv.qtyonhand, qty_available"
        + "    ORDER BY qty_available DESC" + "    ";
    System.out.println("aca strSql summary:" + strSql);
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        warehouse = OBDal.getInstance().get(Warehouse.class, (String) obj[0]);

        WarehouseStockData result = new WarehouseStockData();
        result.warehouseid = warehouse.getSearchKey();
        result.stockavailable = ((BigDecimal) obj[1]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);
        result.stocknopicking = ((BigDecimal) obj[2]).setScale(BigDecimalScale,
            RoundingMode.HALF_UP);

        result.stockavailable = result.stockavailable.subtract(result.stocknopicking).setScale(
            BigDecimalScale, RoundingMode.HALF_UP);

        wdata.add(result);
        // stk_detail.put(warehouse.getSearchKey(),
        // ((BigDecimal) obj[1]).setScale(BigDecimalScale,
        // RoundingMode.HALF_UP));
      }

    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    // return stk_detail;
    return wdata;
  }

  public static ReportPriceListAndStockJRData[] selectPENPriceListDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectPENPriceListDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportPriceListAndStockJRData[] selectPENPriceListDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT ad_org.AD_ORG_ID AS PADRE, M_PRICELIST.M_PRICELIST_ID AS ID, TO_CHAR(M_PRICELIST.NAME) AS NAME"
        + "         FROM M_PRICELIST, ad_org, ad_orgtype"
        + "        WHERE 1=1"
        + "          AND ad_orgtype.ad_orgtype_id=ad_org.ad_orgtype_id "
        + "          AND ad_org.ad_org_id!='0' "
        + "          AND ad_orgtype.islegalEntity='N' "
        + "          AND M_PRICELIST.C_CURRENCY_ID=(SELECT C_CURRENCY_ID FROM C_CURRENCY WHERE ISO_CODE='PEN' LIMIT 1) "
        + "          AND M_PRICELIST.ISSOPRICELIST='Y' " + "         AND M_PRICELIST.ISACTIVE='Y'"
        + "          AND M_PRICELIST.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ") "
        + "         AND ad_isorgincluded(ad_org.ad_org_id, M_PRICELIST.ad_org_id, M_PRICELIST.ad_client_id) <> -1"
        + "       ORDER BY PADRE, NAME";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportPriceListAndStockJRData objRptPriceListAndStkData = new ReportPriceListAndStockJRData();
        objRptPriceListAndStkData.padre = UtilSql.getValue(result, "padre");
        objRptPriceListAndStkData.id = UtilSql.getValue(result, "id");
        objRptPriceListAndStkData.name = UtilSql.getValue(result, "name");
        objRptPriceListAndStkData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptPriceListAndStkData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ReportPriceListAndStockJRData objRptPriceListAndStkData[] = new ReportPriceListAndStockJRData[vector
        .size()];
    vector.copyInto(objRptPriceListAndStkData);
    return (objRptPriceListAndStkData);
  }

  public static ReportPriceListAndStockJRData[] selectUSDPriceListDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectUSDPriceListDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportPriceListAndStockJRData[] selectUSDPriceListDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT ad_org.AD_ORG_ID AS PADRE, M_PRICELIST.M_PRICELIST_ID AS ID, TO_CHAR(M_PRICELIST.NAME) AS NAME"
        + "         FROM M_PRICELIST, ad_org, ad_orgtype"
        + "        WHERE 1=1 "
        + "          AND ad_orgtype.ad_orgtype_id=ad_org.ad_orgtype_id "
        + "          AND ad_org.ad_org_id!='0' "
        + "          AND ad_orgtype.islegalEntity='N' "
        + "          AND M_PRICELIST.C_CURRENCY_ID=(SELECT C_CURRENCY_ID FROM C_CURRENCY WHERE ISO_CODE='USD' LIMIT 1) "
        + "          AND M_PRICELIST.ISSOPRICELIST='Y' " + "         AND M_PRICELIST.ISACTIVE='Y'"
        + "          AND M_PRICELIST.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ") "
        + "          AND ad_isorgincluded(ad_org.ad_org_id, M_PRICELIST.ad_org_id, M_PRICELIST.ad_client_id) <> -1"
        + "        ORDER BY PADRE, NAME";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportPriceListAndStockJRData objRptPriceListAndStkData = new ReportPriceListAndStockJRData();
        objRptPriceListAndStkData.padre = UtilSql.getValue(result, "padre");
        objRptPriceListAndStkData.id = UtilSql.getValue(result, "id");
        objRptPriceListAndStkData.name = UtilSql.getValue(result, "name");
        objRptPriceListAndStkData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptPriceListAndStkData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ReportPriceListAndStockJRData objRptPriceListAndStkData[] = new ReportPriceListAndStockJRData[vector
        .size()];
    vector.copyInto(objRptPriceListAndStkData);
    return (objRptPriceListAndStkData);
  }

  public static ReportPriceListAndStockJRData[] set() throws ServletException {
    ReportPriceListAndStockJRData objRptPriceListAndStkData[] = new ReportPriceListAndStockJRData[1];
    objRptPriceListAndStkData[0] = new ReportPriceListAndStockJRData();
    objRptPriceListAndStkData[0].productid = "--";
    objRptPriceListAndStkData[0].searchkey = "--";
    objRptPriceListAndStkData[0].brand = "--";
    objRptPriceListAndStkData[0].productname = "--";
    objRptPriceListAndStkData[0].stockonhand = "--";
    objRptPriceListAndStkData[0].stockonhandavailable = "--";
    objRptPriceListAndStkData[0].realstockonhandavailable = "--";
    objRptPriceListAndStkData[0].stockreserved = "--";
    objRptPriceListAndStkData[0].stockSummaryDetail = "--";
    objRptPriceListAndStkData[0].padre = "";
    objRptPriceListAndStkData[0].id = "";
    objRptPriceListAndStkData[0].name = "";

    objRptPriceListAndStkData[0].PENlistprice = "0.00000";
    objRptPriceListAndStkData[0].PENIGV = "0.00000";
    objRptPriceListAndStkData[0].PENlistpriceIGV = "0.00000";
    objRptPriceListAndStkData[0].USDlistprice = "0.00000";
    objRptPriceListAndStkData[0].USDIGV = "0.00000";
    objRptPriceListAndStkData[0].USDlistpriceIGV = "0.00000";
    objRptPriceListAndStkData[0].taxrate = "0.00000";

    objRptPriceListAndStkData[0].mainPENlistprice = "0.00000";
    objRptPriceListAndStkData[0].mainUSDlistprice = "0.00000";

    return objRptPriceListAndStkData;
  }

  static public String getProductIdsBySearchKey(String adClientId, String adOrgId,
      String searchKey, String strmBrandID) {
    String result = "";
    String strSql = "SELECT M_Product.M_Product_ID  FROM M_Product "
        + "           WHERE trim(lower(M_Product.value)) LIKE lower('" + searchKey + "%') ";
    strSql = strSql
        + ((strmBrandID == null || "".equals(strmBrandID)) ? ""
            : "             AND M_Product.M_Brand_ID='" + strmBrandID + "'");
    strSql = strSql + "             AND M_Product.AD_Client_ID='" + adClientId + "' "
        + "             AND AD_ISORGINCLUDED('" + adOrgId + "', M_Product.AD_Org_ID, '"
        + adClientId + "') > -1; ";
    try {
      Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = q.list();
      for (int k = 0; k < data.size(); k++) {
        result = result + "'" + (String) data.get(k) + "'";
        if (k != data.size() - 1)
          result = result + ",";
      }
      return result;
    } catch (Exception ex) {
      return result;
    }
  }

  public static ReportPriceListAndStockJRData[] selectProductBrandDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectProductBrandDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportPriceListAndStockJRData[] selectProductBrandDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT ORG.AD_ORG_ID AS PADRE, M_BRAND.M_BRAND_ID AS ID, TO_CHAR(M_BRAND.NAME) AS NAME"
        + "         FROM M_BRAND, AD_ORG ORG " + "        WHERE 1=1"
        + "          AND M_BRAND.ISACTIVE='Y'" + "          AND M_BRAND.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")         AND ad_isorgincluded(ORG.AD_ORG_ID,M_BRAND.AD_ORG_ID,M_BRAND.AD_Client_ID)<>-1"
        + "        ORDER BY PADRE, NAME";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }
      if (adUserClient != null && !(adUserClient.equals(""))) {
      }

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        ReportPriceListAndStockJRData objRptPriceListAndStkData = new ReportPriceListAndStockJRData();
        objRptPriceListAndStkData.padre = UtilSql.getValue(result, "padre");
        objRptPriceListAndStkData.id = UtilSql.getValue(result, "id");
        objRptPriceListAndStkData.name = UtilSql.getValue(result, "name");
        objRptPriceListAndStkData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRptPriceListAndStkData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    ReportPriceListAndStockJRData objRptPriceListAndStkData[] = new ReportPriceListAndStockJRData[vector
        .size()];
    vector.copyInto(objRptPriceListAndStkData);
    return (objRptPriceListAndStkData);
  }

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_PRODUCT.VALUE || ' - ' || M_PRODUCT.NAME AS NAME"
        + "      FROM M_PRODUCT" + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

  public static String selectProductUOMSymbolTrl(ConnectionProvider connectionProvider,
      String mProductId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT cast(coalesce((select uomsymbol from c_uom_trl where c_uom_id=uom.c_uom_id and ad_language='es_PE'),uom.uomsymbol) as varchar) as uom "
        + "      FROM m_product  INNER JOIN c_uom uom ON m_product.c_uom_id = uom.c_uom_id"
        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "uom");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

  public static String selectPrdcProductgroup(ConnectionProvider connectionProvider,
      String mProductLineId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION AS NAME"
        + "      FROM PRDC_PRODUCTGROUP" + "      WHERE PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductLineId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

  public static String selectBase64Image(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT encode(img.binarydata,'base64') AS image64 FROM m_product p, ad_image img WHERE p.ad_image_id = img.ad_image_id AND p.m_product_id= ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "image64");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }
}

class PriceListStockData {
  public String orgid;
  public String productid;
  public String searchkey;
  public String brand;
  public String productname;
  public BigDecimal PENlistprice;
  public BigDecimal PENlistpriceIGV;
  public BigDecimal USDlistprice;
  public BigDecimal USDlistpriceIGV;
  public BigDecimal stockonhand;
  public BigDecimal stockonhandavailable;
  public BigDecimal stockreserved;
  public BigDecimal stocknopicking;
  public BigDecimal realstockonhandavailable;

  // HashMap<String, BigDecimal> stk_sample;
  List<WarehouseStockData> stk_sample;

  public BigDecimal mainPENlistprice;
  public BigDecimal mainUSDlistprice;

  public PriceListStockData() {
    searchkey = "--";
    brand = "--";
    productname = "--";
    PENlistprice = BigDecimal.ZERO;
    PENlistpriceIGV = BigDecimal.ZERO;
    USDlistprice = BigDecimal.ZERO;
    USDlistpriceIGV = BigDecimal.ZERO;
    stockonhand = BigDecimal.ZERO;
    stockonhandavailable = BigDecimal.ZERO;
    stockreserved = BigDecimal.ZERO;
    stocknopicking = BigDecimal.ZERO;
    realstockonhandavailable = BigDecimal.ZERO;

    // stk_sample = new HashMap<String, BigDecimal>();
    stk_sample = new ArrayList<WarehouseStockData>();

    mainPENlistprice = BigDecimal.ZERO;
    mainUSDlistprice = BigDecimal.ZERO;
  }

  static public int searchProductId(Vector<PriceListStockData> results, String productId) {
    for (int i = 0; i < results.size(); i++) {
      if (productId.equals(results.get(i).productid))
        return i;
    }
    return -1;
  }

}

class WarehouseStockData {
  public String warehouseid;
  public BigDecimal stockavailable;
  public BigDecimal stocknopicking;

  public WarehouseStockData() {
    warehouseid = "--";
    stockavailable = BigDecimal.ZERO;
    stocknopicking = BigDecimal.ZERO;
  }
}
