//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.QueryTimeOutUtil;

class RankingProductosJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(RankingProductosJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String productid;
  public String cbpartnerid;
  public String ccurrencyid;
  public String salesrepid;
  public String dateFrom;
  public String dateTo;

  public String searchkey;
  public String internalcode;
  public String productline;
  public String name;
  public String totalqtyordered;
  public String totallinenetamt;
  public String productuom;

  public String lastsaleprice;
  public String lastsaleqty;
  public String lastsaledate;

  public String padre;
  public String id;

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
    else if (fieldName.equalsIgnoreCase("cbpartnerid"))
      return cbpartnerid;
    else if (fieldName.equalsIgnoreCase("ccurrencyid"))
      return ccurrencyid;
    else if (fieldName.equalsIgnoreCase("salesrepid"))
      return salesrepid;
    else if (fieldName.equalsIgnoreCase("dateFrom"))
      return dateFrom;
    else if (fieldName.equalsIgnoreCase("dateTo"))
      return dateTo;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("internalcode"))
      return internalcode;
    else if (fieldName.equalsIgnoreCase("productline"))
      return productline;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("totalqtyordered"))
      return totalqtyordered;
    else if (fieldName.equalsIgnoreCase("totallinenetamt"))
      return totallinenetamt;
    else if (fieldName.equalsIgnoreCase("productuom"))
      return productuom;
    else if (fieldName.equalsIgnoreCase("lastsaleprice"))
      return lastsaleprice;
    else if (fieldName.equalsIgnoreCase("lastsaleqty"))
      return lastsaleqty;
    else if (fieldName.equalsIgnoreCase("lastsaledate"))
      return lastsaledate;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static RankingProductosJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId, String mProductLineID,
      String strCantMinVentas, String strMontoMinimo, String strCurrencyId, String strcBpartnetId,
      String strSalesRepId, String mProductId) throws ServletException {
    return select(vars, adOrgId, adClientId, startDate, endDate, adUserId, mProductLineID,
        strCantMinVentas, strMontoMinimo, strCurrencyId, strcBpartnetId, strSalesRepId, mProductId,
        0, 0);
  }

  public static RankingProductosJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String startDate, String endDate, String adUserId, String mProductLineID,
      String strCantMinVentas, String strMontoMinimo, String strCurrencyId, String strcBpartnetId,
      String strSalesRepId, String mProductId, int firstRegister, int numberRegisters)
      throws ServletException {
    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);
    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);
    SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy");

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    long countRecord = 0;
    Object[] pvData;

    String strSql = "";
    strSql = "SELECT A.m_product_id, A.value, A.em_scr_internalcode, A.name, "
        + "          (select prdc.description from prdc_ProductGroup prdc where prdc.prdc_ProductGroup_ID=A.em_prdc_ProductGroup_ID) as productLine, "
        + "          A.totalqtyordered, A.totallineamt, "
        + "          (select text(uomsymbol) from c_uom_trl where c_uom_trl.c_uom_id=A.c_uom_id) as uomsymbol "
        + "     FROM (SELECT p.value, p.em_scr_internalcode, p.name, p.em_prdc_ProductGroup_ID, i.c_currency_id, il.m_product_id, il.c_uom_id,"
        + "                  sum(il.qtyinvoiced) as totalqtyordered, sum(il.linenetamt) as totallineamt"
        + "            FROM c_invoiceline il, c_invoice i, m_product p"
        + "           WHERE il.c_invoice_id=i.c_invoice_id"
        + "             AND il.m_product_id = p.m_product_id  ";
    strSql = strSql
        + ((strcBpartnetId == null || "".equals(strcBpartnetId)) ? ""
            : "         AND i.C_BPartner_ID = '" + strcBpartnetId + "'  ");
    strSql = strSql
        + ((mProductLineID == null || "".equals(mProductLineID)) ? ""
            : "         AND p.EM_Prdc_Productgroup_ID = '" + mProductLineID + "'  ");
    strSql = strSql
        + ((mProductId == null || "".equals(mProductId)) ? "" : "         AND p.m_product_id = '"
            + mProductId + "'  ");
    strSql = strSql
        + "             AND (i.em_sco_specialdoctype='SCOARINVOICE' OR i.em_sco_specialdoctype='SCOARTICKET')"
        + "             AND i.issotrx='Y' AND i.docstatus='CO'"
        + "             AND i.c_currency_id='" + strCurrencyId + "' "
        + "             AND i.dateacct BETWEEN TO_DATE('" + startDate
        + "', 'DD-MM-YYYY') and TO_DATE('" + endDate + "', 'DD-MM-YYYY')";
    strSql = strSql
        + (("".equals(strSalesRepId.trim()) || strSalesRepId == null) ? "" : " AND i.SalesRep_ID='"
            + strSalesRepId + "'");
    strSql = strSql
        + "             AND AD_ISORGINCLUDED(i.ad_org_id,'"
        + adOrgId
        + "', '"
        + adClientId
        + "') <> -1"
        + "          GROUP BY il.m_product_id, p.value, p.em_scr_internalcode, p.name, p.em_prdc_ProductGroup_ID, i.c_currency_id,il.c_uom_id) A "
        + "WHERE A.totalqtyordered >= '" + strCantMinVentas + "' AND A.totallineamt >= '"
        + strMontoMinimo + "' ORDER BY A.totalqtyordered DESC, A.totallineamt DESC ";
    System.out.println("STRSQL:" + strSql);
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        RankingProductosJRData objRankProdJRData = new RankingProductosJRData();
        objRankProdJRData.orgid = adOrgId;
        objRankProdJRData.cbpartnerid = (strcBpartnetId != null) ? strcBpartnetId : "";
        objRankProdJRData.ccurrencyid = strCurrencyId;
        objRankProdJRData.salesrepid = strSalesRepId;
        objRankProdJRData.dateFrom = startDate;
        objRankProdJRData.dateTo = endDate;

        objRankProdJRData.productid = (String) obj[0];
        objRankProdJRData.searchkey = (String) obj[1];
        objRankProdJRData.internalcode = (obj[2] != null) ? (String) obj[2] : "--";
        objRankProdJRData.name = (String) obj[3];
        objRankProdJRData.productline = (obj[4] != null) ? (String) obj[4] : "--";

        objRankProdJRData.totalqtyordered = dfQty.format(obj[5]);
        objRankProdJRData.totallinenetamt = dfPrice.format(obj[6]);

        objRankProdJRData.productuom = (String) obj[7];

        pvData = getLastSaleData(adOrgId, adClientId, objRankProdJRData.productid,
            objRankProdJRData.cbpartnerid);
        objRankProdJRData.lastsaleprice = (pvData[0] != null && pvData[1] != null) ? ((String) pvData[0] + dfPrice
            .format(pvData[1])) : "--";
        objRankProdJRData.lastsaleqty = (pvData[2] != null) ? dfQty.format(pvData[2]) : "--";
        objRankProdJRData.lastsaledate = (pvData[3] != null) ? dfDate.format(pvData[3]) : "--";

        objRankProdJRData.rownum = Long.toString(countRecord);
        objRankProdJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objRankProdJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    RankingProductosJRData objRankProdJRData[] = new RankingProductosJRData[vector.size()];
    vector.copyInto(objRankProdJRData);

    return (objRankProdJRData);
  }

  public static Object[] getLastSaleData(String adOrgId, String adClientId, String mProdudtId,
      String strcBpartnetId) {

    String strSql = "SELECT (select text(cursymbol) from c_currency where c_currency_id=o.c_currency_id) AS cursymbol, "
        + "                  ol.PriceActual, ol.qtyordered, ol.dateordered "
        + "            FROM C_Order o, C_Orderline ol "
        + "           WHERE ol.C_Order_id = o.C_Order_ID "
        + "             AND (o.em_ssa_specialdoctype = 'SSASTANDARDORDER' OR o.em_ssa_specialdoctype = 'SSAPOSORDER' OR o.em_ssa_specialdoctype = 'SSAWAREHOUSEORDER') "
        + "             AND ol.m_product_id='" + mProdudtId + "' ";
    strSql = strSql
        + ((strcBpartnetId == null || "".equals(strcBpartnetId)) ? ""
            : "         AND o.C_BPartner_ID = '" + strcBpartnetId + "'  ");
    strSql = strSql
        + "             AND COALESCE((SELECT SUM(ABS(ol1.qtyordered))-SUM(ABS(ol1.qtyinvoiced)) "
        + "                             FROM c_orderline ol1 WHERE ol1.c_order_id=o.c_order_id AND ol1.c_order_discount_id IS NULL),0)=0 "
        + "             AND AD_ISORGINCLUDED(o.AD_Org_ID,'" + adOrgId + "','" + adClientId
        + "') > -1 " + "           ORDER BY o.created DESC LIMIT 1";
    Object[] pvData = new Object[4];
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> result = sqlQuery.list();
      if (result.size() > 0) {
        pvData[0] = ((Object[]) result.get(0))[0]; // cursymbol
        pvData[1] = (BigDecimal) ((Object[]) result.get(0))[1]; // price
        pvData[2] = (BigDecimal) ((Object[]) result.get(0))[2]; // qtyordered
        pvData[3] = (Date) ((Object[]) result.get(0))[3]; // dateordered
      }
    } catch (Exception ex) {
      log4j.error("Error getting pv last data:" + ex.getMessage());
    }
    return pvData;
  }

  public static RankingProductosJRData[] selectProductLineDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectProductLineDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static RankingProductosJRData[] selectProductLineDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT PRDC_PRODUCTGROUP.AD_ORG_ID AS PADRE, PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID AS ID, TO_CHAR(PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION) AS NAME"
        + "         FROM PRDC_PRODUCTGROUP" + "        WHERE 1=1"
        + "          AND PRDC_PRODUCTGROUP.ISACTIVE='Y'"
        + "          AND PRDC_PRODUCTGROUP.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")"
        + "       UNION "
        + "       SELECT null AS PADRE, PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID AS ID, TO_CHAR(PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION) AS NAME"
        + "         FROM PRDC_PRODUCTGROUP" + "        WHERE 2=2 "
        + "          AND PRDC_PRODUCTGROUP.ISACTIVE='Y'"
        + "          AND PRDC_PRODUCTGROUP.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql + ")    " + "        ORDER BY PADRE, NAME";

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
        RankingProductosJRData objRankProdJRData = new RankingProductosJRData();
        objRankProdJRData.padre = UtilSql.getValue(result, "padre");
        objRankProdJRData.id = UtilSql.getValue(result, "id");
        objRankProdJRData.name = UtilSql.getValue(result, "name");
        objRankProdJRData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRankProdJRData);
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
    RankingProductosJRData objRankProdJRData[] = new RankingProductosJRData[vector.size()];
    vector.copyInto(objRankProdJRData);
    return (objRankProdJRData);
  }

  public static RankingProductosJRData[] set() throws ServletException {
    RankingProductosJRData objRankProdJRData[] = new RankingProductosJRData[1];
    objRankProdJRData[0] = new RankingProductosJRData();
    objRankProdJRData[0].orgid = "";
    objRankProdJRData[0].productid = "";
    objRankProdJRData[0].cbpartnerid = "";
    objRankProdJRData[0].dateFrom = "";
    objRankProdJRData[0].dateTo = "";
    objRankProdJRData[0].searchkey = "";
    objRankProdJRData[0].internalcode = "";
    objRankProdJRData[0].name = "";
    objRankProdJRData[0].totalqtyordered = "0";
    objRankProdJRData[0].totallinenetamt = "0";
    objRankProdJRData[0].productuom = "";
    objRankProdJRData[0].lastsaleprice = "--";
    objRankProdJRData[0].lastsaleqty = "--";
    objRankProdJRData[0].lastsaledate = "--";

    return objRankProdJRData;
  }

  public static String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.TAXID || ' - ' || C_BPARTNER.NAME AS NAME"
        + "      FROM C_BPARTNER" + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cBpartnerId);

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

  public static String selectOrganization(ConnectionProvider connectionProvider,
      String organizacion_id) throws ServletException {
    String strSql = "";
    strSql = strSql + " select name from ad_org where ad_org_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, organizacion_id);

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

  public static String selectCurrency(ConnectionProvider connectionProvider, String currency_id)
      throws ServletException {
    String strSql = "";
    strSql = strSql + " select iso_code from c_currency where c_currency_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, currency_id);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "iso_code");
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

  public static String selectSalesRepresentative(ConnectionProvider connectionProvider,
      String strSalesRepId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT name" + "      FROM AD_User" + "      WHERE AD_User_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, strSalesRepId);

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

}