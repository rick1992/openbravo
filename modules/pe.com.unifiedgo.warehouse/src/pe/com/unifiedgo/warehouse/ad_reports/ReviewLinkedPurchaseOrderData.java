//Sqlc generated V1.O00-1
package pe.com.unifiedgo.warehouse.ad_reports;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReviewLinkedPurchaseOrderData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReviewLinkedPurchaseOrderData.class);
  private String InitRecordNumber = "0";

  public String searchkey;
  public String internalcode;
  public String name;
  public String padre;
  public String id;

  public String transactionID;
  public String movementDate;
  public String warehousename;
  public String storagebin;
  public String movementqty;
  public String movementqtynegative;
  public String movementtype;
  public String shipmentid;
  public String inventoryid;
  public String movementid;
  public String productionid;
  public String shipmentline;
  public String inventoryline;
  public String movementline;
  public String productionline;
  public String prdID;

  public String movementqtyInitial;
  public String prdInitialID;

  public String movementqtyFinal;
  public String prdFinalID;

  // --------------
  public String orderid;
  public String clientid;
  public String orgname;
  public String orgid;
  public String ordernum;
  public String orderdate;
  public String scurrency;
  public String currencyid;
  public String ordertotallines;
  public String taxamt;
  public String ordertotal;
  // ------------
  public String orderlineid;
  public String lineclientid;
  public String docnumberorder;
  public String productid;
  public String fromcodeproductid;
  public String codeproduct;
  public String productname;
  public String uomname;
  public String qtyorderline;
  public String pricelist;
  public String unitprice;
  public String linetax;
  public String totallineprice;

  // ------------

  public String orderreviewid;
  public String orderreviewstate;
  public String orderreviewdocnum;

  // ---------------

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("movementqtyFinal"))
      return movementqtyFinal;
    else if (fieldName.equalsIgnoreCase("id"))
      return id;
    else if (fieldName.equalsIgnoreCase("padre"))
      return padre;
    else if (fieldName.equalsIgnoreCase("prdFinalID"))
      return prdFinalID;
    else if (fieldName.equalsIgnoreCase("movementqtyInitial"))
      return movementqtyInitial;
    else if (fieldName.equalsIgnoreCase("prdInitialID"))
      return prdInitialID;
    else if (fieldName.equalsIgnoreCase("shipmentline"))
      return shipmentline;
    else if (fieldName.equalsIgnoreCase("inventoryline"))
      return inventoryline;
    else if (fieldName.equalsIgnoreCase("movementline"))
      return movementline;
    else if (fieldName.equalsIgnoreCase("productionline"))
      return productionline;
    else if (fieldName.equalsIgnoreCase("prdID"))
      return prdID;
    else if (fieldName.equalsIgnoreCase("transactionID"))
      return transactionID;
    else if (fieldName.equalsIgnoreCase("movementDate"))
      return movementDate;
    else if (fieldName.equalsIgnoreCase("warehousename"))
      return warehousename;
    else if (fieldName.equalsIgnoreCase("storagebin"))
      return storagebin;
    else if (fieldName.equalsIgnoreCase("movementqty"))
      return movementqty;
    else if (fieldName.equalsIgnoreCase("movementqtynegative"))
      return movementqtynegative;
    else if (fieldName.equalsIgnoreCase("movementtype"))
      return movementtype;
    else if (fieldName.equalsIgnoreCase("shipmentid"))
      return shipmentid;
    else if (fieldName.equalsIgnoreCase("inventoryid"))
      return inventoryid;
    else if (fieldName.equalsIgnoreCase("inventoryid"))
      return movementid;
    else if (fieldName.equalsIgnoreCase("productionid"))
      return productionid;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("internalcode"))
      return internalcode;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;

    //
    else if (fieldName.equalsIgnoreCase("orderid"))
      return orderid;
    else if (fieldName.equalsIgnoreCase("clientid"))
      return clientid;
    else if (fieldName.equalsIgnoreCase("orgname"))
      return orgname;
    else if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("ordernum"))
      return ordernum;
    else if (fieldName.equalsIgnoreCase("orderdate"))
      return orderdate;
    else if (fieldName.equalsIgnoreCase("scurrency"))
      return scurrency;
    else if (fieldName.equalsIgnoreCase("currencyid"))
      return currencyid;
    else if (fieldName.equalsIgnoreCase("ordertotallines"))
      return ordertotallines;
    else if (fieldName.equalsIgnoreCase("taxamt"))
      return taxamt;
    else if (fieldName.equalsIgnoreCase("ordertotal"))
      return ordertotal;
    //

    //
    else if (fieldName.equalsIgnoreCase("orderlineid"))
      return orderlineid;
    else if (fieldName.equalsIgnoreCase("lineclientid"))
      return lineclientid;
    else if (fieldName.equalsIgnoreCase("docnumberorder"))
      return docnumberorder;
    else if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("fromcodeproductid"))
      return fromcodeproductid;
    else if (fieldName.equalsIgnoreCase("codeproduct"))
      return codeproduct;
    else if (fieldName.equalsIgnoreCase("productname"))
      return productname;
    else if (fieldName.equalsIgnoreCase("uomname"))
      return uomname;
    else if (fieldName.equalsIgnoreCase("qtyorderline"))
      return qtyorderline;
    else if (fieldName.equalsIgnoreCase("pricelist"))
      return pricelist;
    else if (fieldName.equalsIgnoreCase("unitprice"))
      return unitprice;
    else if (fieldName.equalsIgnoreCase("linetax"))
      return linetax;
    else if (fieldName.equalsIgnoreCase("totallineprice"))
      return totallineprice;
    //
    else if (fieldName.equalsIgnoreCase("orderreviewid"))
      return orderreviewid;
    else if (fieldName.equalsIgnoreCase("orderreviewstate"))
      return orderreviewstate;
    else if (fieldName.equalsIgnoreCase("orderreviewdocnum"))
      return orderreviewdocnum;
    //
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReviewLinkedPurchaseOrderData[] selectOrdertoReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId)
      throws ServletException {
    return selectOrdertoReview(connectionProvider, adOrgId, adToOrgId, adClientId, 0, 0);
  }

  public static ReviewLinkedPurchaseOrderData[] selectOrdertoReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql
        + " SELECT co.c_order_id as ORDERID,"
        + "      co.ad_client_id CLIENTID,"
        + "      ad.name as ORGNAME, "
        + "      co.ad_org_id ORGID, "
        + "      co.documentno DOCUMENTNO, "
        + "      co.dateordered DATEORDERED,"
        + "      cu.cursymbol CURSYMBOL, "
        + "      co.c_currency_id CURRENCYID, "
        + "      co.totallines as ORDERTOTALLINES, "
        + "      sum(taxamt) as TAXAMT, "
        + "      co.grandtotal GRANDTOTAL"
        + " FROM c_order  co "
        + "   inner join c_bpartner cb on "
        + "    co.c_bpartner_id = cb.c_bpartner_id "
        + "   inner join ad_org ad on "
        + "    co.ad_org_id = ad.ad_org_id "
        + "   inner join c_currency cu on "
        + "     co.c_currency_id = cu.c_currency_id "
        + "   inner join c_ordertax cot on "
        + "     co.c_order_id = cot.c_order_id "
        + " WHERE co.issotrx='N' "
        + "       AND co.ad_client_id = ? "
        // + "       AND cb.EM_Sre_Other_Org_ID = ? "
        + "       AND AD_ISORGINCLUDED(?, cb.EM_Sre_Other_Org_ID, ?) > -1 "
        + "       AND co.docstatus= 'DR' "
        + "       AND AD_ISORGINCLUDED(CO.ad_org_id, ?, ?) > -1 "
        + " GROUP BY ORDERID, CLIENTID, ORGNAME, ORGID, DOCUMENTNO, DATEORDERED,CURSYMBOL, CURRENCYID,ORDERTOTALLINES, GRANDTOTAL ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adToOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
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
        ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData = new ReviewLinkedPurchaseOrderData();
        objectReviewLinkedPurchaseOrderData.orderid = UtilSql.getValue(result, "ORDERID");
        objectReviewLinkedPurchaseOrderData.clientid = UtilSql.getValue(result, "CLIENTID");
        objectReviewLinkedPurchaseOrderData.orgname = UtilSql.getValue(result, "ORGNAME");
        objectReviewLinkedPurchaseOrderData.orgid = UtilSql.getValue(result, "ORGID");
        objectReviewLinkedPurchaseOrderData.ordernum = UtilSql.getValue(result, "DOCUMENTNO");//
        objectReviewLinkedPurchaseOrderData.orderdate = UtilSql.getDateValue(result, "DATEORDERED");
        objectReviewLinkedPurchaseOrderData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
        objectReviewLinkedPurchaseOrderData.currencyid = UtilSql.getValue(result, "CURRENCYID");//
        objectReviewLinkedPurchaseOrderData.ordertotallines = UtilSql.getValue(result,
            "ORDERTOTALLINES");//
        objectReviewLinkedPurchaseOrderData.taxamt = UtilSql.getValue(result, "TAXAMT");//
        objectReviewLinkedPurchaseOrderData.ordertotal = UtilSql.getValue(result, "GRANDTOTAL");
        objectReviewLinkedPurchaseOrderData.rownum = Long.toString(countRecord);
        objectReviewLinkedPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReviewLinkedPurchaseOrderData);
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
    ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData[] = new ReviewLinkedPurchaseOrderData[vector
        .size()];
    vector.copyInto(objectReviewLinkedPurchaseOrderData);
    return (objectReviewLinkedPurchaseOrderData);
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

  public static ReviewLinkedPurchaseOrderData[] selectOrderReviewStatus(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat) throws ServletException {
    return selectOrderReviewStatus(connectionProvider, adOrgId, adToOrgId, adClientId,
        purchaseOrderConcat, 0, 0);
  }

  public static ReviewLinkedPurchaseOrderData[] selectOrderReviewStatus(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + " SELECT  co.c_order_id as ORDERID, " + "       co.documentno as DOCNUM, "
        + "       co.docstatus   as ORDERSTATUSORG" + " FROM  c_order co "
        + "  WHERE co.c_order_id = ? " + "  AND co.ad_client_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, purchaseOrderConcat);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
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
        ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData = new ReviewLinkedPurchaseOrderData();
        objectReviewLinkedPurchaseOrderData.orderreviewid = UtilSql.getValue(result, "ORDERID");
        objectReviewLinkedPurchaseOrderData.orderreviewdocnum = UtilSql.getValue(result, "DOCNUM");
        objectReviewLinkedPurchaseOrderData.orderreviewstate = UtilSql.getValue(result,
            "ORDERSTATUSORG");
        objectReviewLinkedPurchaseOrderData.rownum = Long.toString(countRecord);
        objectReviewLinkedPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReviewLinkedPurchaseOrderData);
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
    ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData[] = new ReviewLinkedPurchaseOrderData[vector
        .size()];
    vector.copyInto(objectReviewLinkedPurchaseOrderData);
    return (objectReviewLinkedPurchaseOrderData);
  }

  public static ReviewLinkedPurchaseOrderData[] selectOrderLinetoReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat) throws ServletException {
    return selectOrderLinetoReview(connectionProvider, adOrgId, adToOrgId, adClientId,
        purchaseOrderConcat, 0, 0);
  }

  public static ReviewLinkedPurchaseOrderData[] selectOrderLinetoReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + " SELECT  col.c_orderline_id as ORDERLINEID,"
        + "       col.ad_client_id   as LINECLIENTID,"
        + "       co.documentno          as DOCNUMORDER, "
        + "       col.m_product_id       as PRODUCTID,"
        + "       pr.value               as FROMCODEPRODUCTID, "
        + "       pr.em_scr_internalcode as CODEPRODUCT, "
        + "       pr.name                as PRODUCTNAME, "
        + "       uo.name		         as UOMNAME, "
        + "       col.qtyordered         as QTYORDEREDLINE, "
        + "       col.pricelist          as PRICELIST, "
        + "       col.priceactual        as UNITPRICE, "
        + "       tx.name                as LINETAX, "
        + "       col.linenetamt         as TOTALLINEPRICE " + " FROM  c_orderline col "
        + "   inner join c_order co on col.c_order_id = co.c_order_id  "
        + "   inner join m_product pr on col.m_product_id = pr.m_product_id "
        + "   inner join c_uom uo on col.c_uom_id = uo.c_uom_id "
        + "   inner join c_tax tx on col.c_tax_id = tx.c_tax_id " + " WHERE co.docstatus='DR' "
        + "  AND co.c_order_id = ? " + "  AND col.ad_client_id = ?";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, purchaseOrderConcat);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
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
        ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData = new ReviewLinkedPurchaseOrderData();
        objectReviewLinkedPurchaseOrderData.orderlineid = UtilSql.getValue(result, "ORDERLINEID");
        objectReviewLinkedPurchaseOrderData.lineclientid = UtilSql.getValue(result, "LINECLIENTID");
        objectReviewLinkedPurchaseOrderData.docnumberorder = UtilSql
            .getValue(result, "DOCNUMORDER");
        objectReviewLinkedPurchaseOrderData.productid = UtilSql.getValue(result, "PRODUCTID");
        objectReviewLinkedPurchaseOrderData.fromcodeproductid = UtilSql.getValue(result,
            "FROMCODEPRODUCTID");
        objectReviewLinkedPurchaseOrderData.codeproduct = UtilSql.getValue(result, "CODEPRODUCT");
        objectReviewLinkedPurchaseOrderData.productname = UtilSql.getValue(result, "PRODUCTNAME");
        objectReviewLinkedPurchaseOrderData.uomname = UtilSql.getValue(result, "UOMNAME");
        objectReviewLinkedPurchaseOrderData.qtyorderline = UtilSql.getValue(result,
            "QTYORDEREDLINE");
        objectReviewLinkedPurchaseOrderData.pricelist = UtilSql.getValue(result, "PRICELIST");
        objectReviewLinkedPurchaseOrderData.unitprice = UtilSql.getValue(result, "UNITPRICE");
        objectReviewLinkedPurchaseOrderData.linetax = UtilSql.getValue(result, "LINETAX");
        objectReviewLinkedPurchaseOrderData.totallineprice = UtilSql.getValue(result,
            "TOTALLINEPRICE");
        objectReviewLinkedPurchaseOrderData.rownum = Long.toString(countRecord);
        objectReviewLinkedPurchaseOrderData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReviewLinkedPurchaseOrderData);
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
    ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData[] = new ReviewLinkedPurchaseOrderData[vector
        .size()];
    vector.copyInto(objectReviewLinkedPurchaseOrderData);
    return (objectReviewLinkedPurchaseOrderData);
  }

  public static ReviewLinkedPurchaseOrderData[] set() throws ServletException {
    ReviewLinkedPurchaseOrderData objectReviewLinkedPurchaseOrderData[] = new ReviewLinkedPurchaseOrderData[1];
    objectReviewLinkedPurchaseOrderData[0] = new ReviewLinkedPurchaseOrderData();
    objectReviewLinkedPurchaseOrderData[0].productid = "";
    objectReviewLinkedPurchaseOrderData[0].searchkey = "";
    objectReviewLinkedPurchaseOrderData[0].internalcode = "";
    objectReviewLinkedPurchaseOrderData[0].name = "";
    objectReviewLinkedPurchaseOrderData[0].prdID = "";
    objectReviewLinkedPurchaseOrderData[0].transactionID = "";
    objectReviewLinkedPurchaseOrderData[0].movementDate = "";
    objectReviewLinkedPurchaseOrderData[0].warehousename = "";
    objectReviewLinkedPurchaseOrderData[0].storagebin = "";
    objectReviewLinkedPurchaseOrderData[0].movementqty = "";
    objectReviewLinkedPurchaseOrderData[0].movementqtynegative = "";
    objectReviewLinkedPurchaseOrderData[0].shipmentid = "";
    objectReviewLinkedPurchaseOrderData[0].inventoryid = "";
    objectReviewLinkedPurchaseOrderData[0].movementid = "";
    objectReviewLinkedPurchaseOrderData[0].shipmentline = "";
    objectReviewLinkedPurchaseOrderData[0].inventoryline = "";
    objectReviewLinkedPurchaseOrderData[0].movementline = "";
    objectReviewLinkedPurchaseOrderData[0].productionline = "";
    objectReviewLinkedPurchaseOrderData[0].productionid = "";

    objectReviewLinkedPurchaseOrderData[0].orderid = "";
    objectReviewLinkedPurchaseOrderData[0].clientid = "";
    objectReviewLinkedPurchaseOrderData[0].orgname = "";
    objectReviewLinkedPurchaseOrderData[0].orgid = "";
    objectReviewLinkedPurchaseOrderData[0].ordernum = "";
    objectReviewLinkedPurchaseOrderData[0].orderdate = "";
    objectReviewLinkedPurchaseOrderData[0].scurrency = "";
    objectReviewLinkedPurchaseOrderData[0].currencyid = "";
    objectReviewLinkedPurchaseOrderData[0].ordertotallines = "";
    objectReviewLinkedPurchaseOrderData[0].taxamt = "";
    objectReviewLinkedPurchaseOrderData[0].ordertotal = "";

    return objectReviewLinkedPurchaseOrderData;

  }

  public static int updateOrderLines(ConnectionProvider connectionProvider, String OrderLineId,
      String newPrice, String adClientId) throws ServletException {
    String strSql = "";
    strSql = strSql + "        UPDATE C_ORDERLINE " + "            SET priceactual = '"
        + Float.parseFloat(newPrice) + "' " + "         WHERE C_ORDERLINE_ID = ? "
        + "         AND AD_Client_ID = ? ";

    int updateCount = 0;
    PreparedStatement st = null;
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, OrderLineId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      updateCount = st.executeUpdate();
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
    return (updateCount);

  }

  public static int updateOrder(ConnectionProvider connectionProvider, String OrderLineId,
      String adClientId) throws ServletException {
    String strSql = "";
    strSql = strSql + "        UPDATE C_ORDER " + "            SET description = ''"
        + "         WHERE C_ORDER_ID = ? " + "         AND AD_Client_ID = ? ";

    int updateCount = 0;
    PreparedStatement st = null;
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, OrderLineId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      System.out.println(st);
      updateCount = st.executeUpdate();
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
    return (updateCount);

  }

}
