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

class CreateInvoiceFromLinkedOrdersData implements FieldProvider {
  static Logger log4j = Logger.getLogger(CreateInvoiceFromLinkedOrdersData.class);
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
  public String isininvoice;
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
  public String invdoc;
  public String invclientid;
  public String invphyinvo;
  public String invstatus;
  public String invcurrency;
  public String invpartner;
  public String invdateinvoice;
  public String invtotalline;
  public String invgrantotal;

  // ------------

  public String orderreviewid;
  public String orderreviewstate;
  public String orderreviewdocnum;

  // ---------------
  public String businesspartnerid;
  public String busineslocationid;

  // ------------

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("movementqtyFinal"))
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
    else if (fieldName.equalsIgnoreCase("isininvoice"))
      return isininvoice;
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
    else if (fieldName.equalsIgnoreCase("invdoc"))
      return invdoc;
    else if (fieldName.equalsIgnoreCase("invphyinvo"))
      return invphyinvo;
    else if (fieldName.equalsIgnoreCase("invclientid"))
      return invclientid;
    else if (fieldName.equalsIgnoreCase("invstatus"))
      return invstatus;
    else if (fieldName.equalsIgnoreCase("invcurrency"))
      return invcurrency;
    else if (fieldName.equalsIgnoreCase("invpartner"))
      return invpartner;
    else if (fieldName.equalsIgnoreCase("invdateinvoice"))
      return invdateinvoice;
    else if (fieldName.equalsIgnoreCase("invtotalline"))
      return invtotalline;
    else if (fieldName.equalsIgnoreCase("invgrantotal"))
      return invgrantotal;
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

  public static CreateInvoiceFromLinkedOrdersData[] selectOrdertoReviewInvoice(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId)
      throws ServletException {
    return selectOrdertoReviewInvoice(connectionProvider, adOrgId, adToOrgId, adClientId, 0, 0);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectOrdertoReviewInvoice(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql
        + " SELECT co.c_order_id as ORDERID, "
        + "      co.ad_client_id CLIENTID, "
        + "      COUNT(inv.c_invoice_id) as ISINVOICED, "
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
        + "   inner join c_invoice inv on "
        + "     co.c_order_id = inv.em_swa_c_order_other_org_id "
        + " WHERE co.issotrx='N' "
        + "       AND co.ad_client_id = ? "
        + "       AND cb.EM_Sre_Other_Org_ID = ? "
        + "       AND co.docstatus= 'CO' "
        + "       AND AD_ISORGINCLUDED(CO.ad_org_id, ?, ?) > -1 "
        + " GROUP BY ORDERID, CLIENTID, ORGNAME, ORGID, co.DOCUMENTNO, co.DATEORDERED,CURSYMBOL, CURRENCYID,ORDERTOTALLINES, co.GRANDTOTAL ";

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
      UtilSql.setValue(st, iParameter, 12, null, adToOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      System.out.println("DATA2");
      System.out.println(st);
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
        CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData = new CreateInvoiceFromLinkedOrdersData();
        objectCreateInvoiceFromLinkedOrdersData.orderid = UtilSql.getValue(result, "ORDERID");
        objectCreateInvoiceFromLinkedOrdersData.clientid = UtilSql.getValue(result, "CLIENTID");
        objectCreateInvoiceFromLinkedOrdersData.isininvoice = UtilSql
            .getValue(result, "ISINVOICED");
        objectCreateInvoiceFromLinkedOrdersData.orgname = UtilSql.getValue(result, "ORGNAME");
        objectCreateInvoiceFromLinkedOrdersData.orgid = UtilSql.getValue(result, "ORGID");
        objectCreateInvoiceFromLinkedOrdersData.ordernum = UtilSql.getValue(result, "DOCUMENTNO");//
        objectCreateInvoiceFromLinkedOrdersData.orderdate = UtilSql.getDateValue(result,
            "DATEORDERED");
        objectCreateInvoiceFromLinkedOrdersData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
        objectCreateInvoiceFromLinkedOrdersData.currencyid = UtilSql.getValue(result, "CURRENCYID");//
        objectCreateInvoiceFromLinkedOrdersData.ordertotallines = UtilSql.getValue(result,
            "ORDERTOTALLINES");//
        objectCreateInvoiceFromLinkedOrdersData.taxamt = UtilSql.getValue(result, "TAXAMT");//
        objectCreateInvoiceFromLinkedOrdersData.ordertotal = UtilSql.getValue(result, "GRANDTOTAL");
        objectCreateInvoiceFromLinkedOrdersData.rownum = Long.toString(countRecord);
        objectCreateInvoiceFromLinkedOrdersData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectCreateInvoiceFromLinkedOrdersData);
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
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[vector
        .size()];
    vector.copyInto(objectCreateInvoiceFromLinkedOrdersData);
    return (objectCreateInvoiceFromLinkedOrdersData);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectOrdertoCreateInvoice(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId)
      throws ServletException {
    return selectOrdertoCreateInvoice(connectionProvider, adOrgId, adToOrgId, adClientId, 0, 0);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectOrdertoCreateInvoice(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql
        + " SELECT co.c_order_id as ORDERID, "
        + "      co.ad_client_id CLIENTID, "
        + "      '---' as ISINVOICED, "
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
        + "   left join c_invoice inv on "
        + "     co.c_order_id = inv.em_swa_c_order_other_org_id "
        + " WHERE co.issotrx='N' "
        + "       AND co.ad_client_id = ? "
        + "       AND cb.EM_Sre_Other_Org_ID = ? "
        + "       AND co.docstatus= 'CO' "
        + "       AND AD_ISORGINCLUDED(CO.ad_org_id, ?, ?) > -1 "
        + "       AND inv.em_swa_c_order_other_org_id is null "
        + " GROUP BY ORDERID, CLIENTID, ORGNAME, ORGID, co.DOCUMENTNO, co.DATEORDERED,CURSYMBOL, CURRENCYID,ORDERTOTALLINES, co.GRANDTOTAL ";

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
      UtilSql.setValue(st, iParameter, 12, null, adToOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adClientId);
      System.out.println("DATA");
      System.out.println(st);
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
        CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData = new CreateInvoiceFromLinkedOrdersData();
        objectCreateInvoiceFromLinkedOrdersData.orderid = UtilSql.getValue(result, "ORDERID");
        objectCreateInvoiceFromLinkedOrdersData.clientid = UtilSql.getValue(result, "CLIENTID");
        objectCreateInvoiceFromLinkedOrdersData.isininvoice = UtilSql
            .getValue(result, "ISINVOICED");
        objectCreateInvoiceFromLinkedOrdersData.orgname = UtilSql.getValue(result, "ORGNAME");
        objectCreateInvoiceFromLinkedOrdersData.orgid = UtilSql.getValue(result, "ORGID");
        objectCreateInvoiceFromLinkedOrdersData.ordernum = UtilSql.getValue(result, "DOCUMENTNO");//
        objectCreateInvoiceFromLinkedOrdersData.orderdate = UtilSql.getDateValue(result,
            "DATEORDERED");
        objectCreateInvoiceFromLinkedOrdersData.scurrency = UtilSql.getValue(result, "CURSYMBOL");
        objectCreateInvoiceFromLinkedOrdersData.currencyid = UtilSql.getValue(result, "CURRENCYID");//
        objectCreateInvoiceFromLinkedOrdersData.ordertotallines = UtilSql.getValue(result,
            "ORDERTOTALLINES");//
        objectCreateInvoiceFromLinkedOrdersData.taxamt = UtilSql.getValue(result, "TAXAMT");//
        objectCreateInvoiceFromLinkedOrdersData.ordertotal = UtilSql.getValue(result, "GRANDTOTAL");
        objectCreateInvoiceFromLinkedOrdersData.rownum = Long.toString(countRecord);
        objectCreateInvoiceFromLinkedOrdersData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectCreateInvoiceFromLinkedOrdersData);
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
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[vector
        .size()];
    vector.copyInto(objectCreateInvoiceFromLinkedOrdersData);
    return (objectCreateInvoiceFromLinkedOrdersData);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectBusinessPartner(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat) throws ServletException {
    return selectBusinessPartner(connectionProvider, adOrgId, adToOrgId, adClientId,
        purchaseOrderConcat, 0, 0);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectBusinessPartner(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + " SELECT bp.c_bpartner_id AS BPARTNERID, "
        + "      bl.c_bpartner_location_id AS BPARTNERLOCATIONID " + " FROM c_bpartner  bp "
        + "    INNER JOIN c_bpartner_location bl "
        + "            ON bp.c_bpartner_id = bl.c_bpartner_id "
        + "  WHERE bp.EM_Sre_Other_Org_ID = ? " + "  AND bp.ad_client_id = ? "
        + "  AND bl.c_bpartner_location_id is not null" + "      Limit 1 ";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
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
        CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData = new CreateInvoiceFromLinkedOrdersData();
        objectCreateInvoiceFromLinkedOrdersData.businesspartnerid = UtilSql.getValue(result,
            "BPARTNERID");
        objectCreateInvoiceFromLinkedOrdersData.busineslocationid = UtilSql.getValue(result,
            "BPARTNERLOCATIONID");
        objectCreateInvoiceFromLinkedOrdersData.rownum = Long.toString(countRecord);
        objectCreateInvoiceFromLinkedOrdersData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectCreateInvoiceFromLinkedOrdersData);
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
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[vector
        .size()];
    vector.copyInto(objectCreateInvoiceFromLinkedOrdersData);
    return (objectCreateInvoiceFromLinkedOrdersData);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectOrderReviewStatus(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat) throws ServletException {
    return selectOrderReviewStatus(connectionProvider, adOrgId, adToOrgId, adClientId,
        purchaseOrderConcat, 0, 0);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectOrderReviewStatus(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + " SELECT  co.c_order_id as ORDERID, " + "       co.documentno as DOCNUM, "
        + "       co.docstatus  as ORDERSTATUSORG" + " FROM  c_order co "
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
        CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData = new CreateInvoiceFromLinkedOrdersData();
        objectCreateInvoiceFromLinkedOrdersData.orderreviewid = UtilSql.getValue(result, "ORDERID");
        objectCreateInvoiceFromLinkedOrdersData.orderreviewdocnum = UtilSql.getValue(result,
            "DOCNUM");
        objectCreateInvoiceFromLinkedOrdersData.orderreviewstate = UtilSql.getValue(result,
            "ORDERSTATUSORG");
        objectCreateInvoiceFromLinkedOrdersData.rownum = Long.toString(countRecord);
        objectCreateInvoiceFromLinkedOrdersData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectCreateInvoiceFromLinkedOrdersData);
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
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[vector
        .size()];
    vector.copyInto(objectCreateInvoiceFromLinkedOrdersData);
    return (objectCreateInvoiceFromLinkedOrdersData);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectInvoiceReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat) throws ServletException {
    return selectInvoiceReview(connectionProvider, adOrgId, adToOrgId, adClientId,
        purchaseOrderConcat, 0, 0);
  }

  public static CreateInvoiceFromLinkedOrdersData[] selectInvoiceReview(
      ConnectionProvider connectionProvider, String adOrgId, String adToOrgId, String adClientId,
      String purchaseOrderConcat, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = strSql + " SELECT  documentno 				 as INVDOC, "
        + "       em_scr_physical_documentno as INVPHYINVO, "
        + "       inv.ad_client_id           as INVCLIENTID, "
        + "       trl.name                   as INVSTATUS,"
        + "       cu.cursymbol 				 as INVCURRENCY,  " + "       bp.name 					 as INVPARTNER, "
        + "       dateinvoiced               as INVDATE,   "
        + "       totallines    			 as INVTOTALLINES, "
        + "       grandtotal    			 as INVGRANDTOTAL  " + " FROM  c_invoice inv  "
        + "   inner join c_currency cu on inv.c_currency_id = cu.c_currency_id  "
        + "   inner join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id "
        + "   inner join ad_ref_list reflist on inv.docstatus = reflist.value "
        + "   inner join ad_ref_list_trl trl on reflist.ad_ref_list_id = trl.ad_ref_list_id "
        + " WHERE reflist.ad_reference_id ='131' " + "  AND inv.em_swa_c_order_other_org_id = ? "
        + "  AND inv.ad_client_id = ?";

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
        CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData = new CreateInvoiceFromLinkedOrdersData();
        objectCreateInvoiceFromLinkedOrdersData.invdoc = UtilSql.getValue(result, "INVDOC");
        objectCreateInvoiceFromLinkedOrdersData.invphyinvo = UtilSql.getValue(result, "INVPHYINVO");
        objectCreateInvoiceFromLinkedOrdersData.invclientid = UtilSql.getValue(result,
            "INVCLIENTID");
        objectCreateInvoiceFromLinkedOrdersData.invstatus = UtilSql.getValue(result, "INVSTATUS");
        objectCreateInvoiceFromLinkedOrdersData.invcurrency = UtilSql.getValue(result,
            "INVCURRENCY");
        objectCreateInvoiceFromLinkedOrdersData.invpartner = UtilSql.getValue(result, "INVPARTNER");
        objectCreateInvoiceFromLinkedOrdersData.invdateinvoice = UtilSql.getDateValue(result,
            "INVDATE");
        objectCreateInvoiceFromLinkedOrdersData.invtotalline = UtilSql.getValue(result,
            "INVTOTALLINES");
        objectCreateInvoiceFromLinkedOrdersData.invgrantotal = UtilSql.getValue(result,
            "INVGRANDTOTAL");

        objectCreateInvoiceFromLinkedOrdersData.rownum = Long.toString(countRecord);
        objectCreateInvoiceFromLinkedOrdersData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectCreateInvoiceFromLinkedOrdersData);
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
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[vector
        .size()];
    vector.copyInto(objectCreateInvoiceFromLinkedOrdersData);
    return (objectCreateInvoiceFromLinkedOrdersData);
  }

  public static CreateInvoiceFromLinkedOrdersData[] set() throws ServletException {
    CreateInvoiceFromLinkedOrdersData objectCreateInvoiceFromLinkedOrdersData[] = new CreateInvoiceFromLinkedOrdersData[1];
    objectCreateInvoiceFromLinkedOrdersData[0] = new CreateInvoiceFromLinkedOrdersData();
    objectCreateInvoiceFromLinkedOrdersData[0].searchkey = "";
    objectCreateInvoiceFromLinkedOrdersData[0].internalcode = "";
    objectCreateInvoiceFromLinkedOrdersData[0].name = "";
    objectCreateInvoiceFromLinkedOrdersData[0].prdID = "";
    objectCreateInvoiceFromLinkedOrdersData[0].transactionID = "";
    objectCreateInvoiceFromLinkedOrdersData[0].movementDate = "";
    objectCreateInvoiceFromLinkedOrdersData[0].warehousename = "";
    objectCreateInvoiceFromLinkedOrdersData[0].storagebin = "";
    objectCreateInvoiceFromLinkedOrdersData[0].movementqty = "";
    objectCreateInvoiceFromLinkedOrdersData[0].movementqtynegative = "";
    objectCreateInvoiceFromLinkedOrdersData[0].shipmentid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].inventoryid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].movementid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].shipmentline = "";
    objectCreateInvoiceFromLinkedOrdersData[0].inventoryline = "";
    objectCreateInvoiceFromLinkedOrdersData[0].movementline = "";
    objectCreateInvoiceFromLinkedOrdersData[0].productionline = "";
    objectCreateInvoiceFromLinkedOrdersData[0].productionid = "";

    objectCreateInvoiceFromLinkedOrdersData[0].orderid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].clientid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].orgname = "";
    objectCreateInvoiceFromLinkedOrdersData[0].orgid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].ordernum = "";
    objectCreateInvoiceFromLinkedOrdersData[0].orderdate = "";
    objectCreateInvoiceFromLinkedOrdersData[0].scurrency = "";
    objectCreateInvoiceFromLinkedOrdersData[0].currencyid = "";
    objectCreateInvoiceFromLinkedOrdersData[0].ordertotallines = "";
    objectCreateInvoiceFromLinkedOrdersData[0].taxamt = "";
    objectCreateInvoiceFromLinkedOrdersData[0].ordertotal = "";

    return objectCreateInvoiceFromLinkedOrdersData;

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
    strSql = strSql + "        UPDATE C_ORDER " + "            SET description = '' "
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
