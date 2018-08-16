//Sqlc generated V1.O00-1
package pe.com.unifiedgo.core.ad_reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportBusquedaOCCustomerJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportBusquedaOCCustomerJRData.class);
  private String InitRecordNumber = "0";
  public String orgid;
  public String mproductid;
  public String mproductname;
  public String corderid;
  public String orderno;
  public String specialdoctype;
  public String doctypename;
  public String cbpartnerid;
  public String partnername;
  public String qtyordered;
  public String priceactual;
  public String linenetamt;
  public String uom;
  public String cursymbol;
  public String paymentterm;
  public String paymentmethod;
  public String dateordered;
  public String ruc;
  public String organizacion;
  public String line;
  public String producto;
  public String lineap;

  public String rownum;

  public String name;
  public String padre;
  public String id;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("mproductid"))
      return mproductid;
    else if (fieldName.equalsIgnoreCase("mproductname"))
      return mproductname;
    else if (fieldName.equalsIgnoreCase("orderno"))
      return orderno;
    else if (fieldName.equalsIgnoreCase("specialdoctype"))
      return specialdoctype;
    else if (fieldName.equalsIgnoreCase("doctypename"))
      return doctypename;
    else if (fieldName.equalsIgnoreCase("corderid"))
      return corderid;
    else if (fieldName.equalsIgnoreCase("cbpartnerid"))
      return cbpartnerid;
    else if (fieldName.equalsIgnoreCase("partnername"))
      return partnername;
    else if (fieldName.equalsIgnoreCase("qtyordered"))
      return qtyordered;
    else if (fieldName.equalsIgnoreCase("priceactual"))
      return priceactual;
    else if (fieldName.equalsIgnoreCase("linenetamt"))
      return linenetamt;
    else if (fieldName.equalsIgnoreCase("uom"))
      return uom;
    else if (fieldName.equalsIgnoreCase("cursymbol"))
      return cursymbol;
    else if (fieldName.equalsIgnoreCase("paymentterm"))
      return paymentterm;
    else if (fieldName.equalsIgnoreCase("paymentmethod"))
      return paymentmethod;
    else if (fieldName.equalsIgnoreCase("dateordered"))
      return dateordered;
    else if (fieldName.equalsIgnoreCase("ruc"))
      return ruc;
    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;
    else if (fieldName.equalsIgnoreCase("producto"))
      return producto;
    else if (fieldName.equalsIgnoreCase("lineap"))
      return lineap;
    else if (fieldName.equalsIgnoreCase("line"))
      return line;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
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

  public static ReportBusquedaOCCustomerJRData[] select_trl(String adClientId, String adOrgId, String mProductId, String language, String dateFrom, String dateTo, String adUserId, String mProductLineID) throws ServletException {
    return select_trl(adClientId, adOrgId, mProductId, language, dateFrom, dateTo, adUserId, mProductLineID, 0, 0);
  }

  public static ReportBusquedaOCCustomerJRData[] select_trl(String adClientId, String adOrgId, String mProductId, String language, String dateFrom, String dateTo, String adUserId, String mProductLineID, int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = "SELECT il.m_product_id, p.name, i.C_Invoice_ID AS C_Invoice_ID, i.documentno AS orderno, COALESCE(to_char(DATE(i.dateacct)),'--') AS dateordered, "

    + "          dctrl.name AS doctypename, dc.em_sco_specialdoctype as specialdoctype," + "          i.c_bpartner_id AS C_Bpartner_ID, bp.taxid || ' - ' || bp.name AS partnername," + "          il.line AS line, il.qtyinvoiced AS qtyordered, il.priceactual AS priceactual, " + "          il.linenetamt AS linenetamt, text(uomtrl.uomsymbol) AS uom, cur.cursymbol AS cursymbol, " + "          pterm.name as paymentterm, pmethod.name as paymentmethod "

    + "  ,		 (select ao.name from ad_org ao where ao.ad_org_id=i.ad_org_id) as organizacion, " + " 		(select aoi.taxid from ad_orginfo aoi where aoi.ad_org_id=i.ad_org_id) as ruc ";

    strSql = strSql + ((mProductId == null || mProductId.compareToIgnoreCase("") == 0) ? ",0  as producto," : "  ,1 as producto, ");

    strSql = strSql + " 	COALESCE((select pp2.description from prdc_productgroup pp2 where pp2.prdc_productgroup_id=p.EM_Prdc_Productgroup_ID ),'') as lineap  "

    + "     FROM C_Invoice i, C_InvoiceLine il, C_Doctype dc, C_Doctype_Trl dctrl, C_Currency cur, " + "          C_Bpartner bp, C_Uom_Trl uomtrl, C_PaymentTerm pterm, Fin_PaymentMethod pmethod, M_Product p "

    + "    WHERE il.M_Product_ID = p.m_product_id " + "      AND dc.em_sco_specialdoctype = 'SCOARINVOICE' " + "      AND i.dateacct BETWEEN TO_DATE('" + dateFrom + "', 'DD-MM-YYYY') AND TO_DATE('" + dateTo + "', 'DD-MM-YYYY') " + "      AND i.docstatus = 'CO' ";
    strSql = strSql + ((mProductId == null || mProductId.equals("")) ? "" : " AND il.M_Product_ID = '" + mProductId + "' ");
    strSql = strSql + ((mProductLineID == null || mProductLineID.equals("")) ? "" : "                  AND p.EM_Prdc_Productgroup_ID = '" + mProductLineID + "'  ");
    strSql = strSql + "      AND il.C_UOM_ID = uomtrl.C_UOM_ID " + "      AND i.C_Invoice_ID = il.C_Invoice_ID " + "      AND i.C_Bpartner_ID = bp.C_Bpartner_ID " + "      AND i.fin_paymentmethod_id = pmethod.fin_paymentmethod_id " + "      AND i.c_paymentterm_id = pterm.c_paymentterm_id  " + "      AND dc.C_Doctype_ID = i.C_DoctypeTarget_ID " + "      AND dc.C_Doctype_ID = dctrl.C_Doctype_ID " + "      AND dctrl.AD_Language = '" + language + "' " + "      AND i.C_Currency_ID = cur.C_Currency_ID " + "      AND i.AD_Client_ID = '" + adClientId + "' ";
    strSql = strSql + "      AND AD_ISORGINCLUDED(i.AD_Org_ID,'" + adOrgId + "','" + adClientId + "') <> -1 ";
    strSql = strSql + "    ORDER BY dateordered, orderno, il.line;";

    Vector<ReportBusquedaOCCustomerJRData> vector = new Vector<ReportBusquedaOCCustomerJRData>(0);

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    DecimalFormat dfInt = new DecimalFormat("0");
    dfInt.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> results = sqlQuery.list();
      for (int k = 0; k < results.size(); k++) {
        Object[] obj = (Object[]) results.get(k);

        countRecord++;
        ReportBusquedaOCCustomerJRData objReportBusquedaOCCustomerJRData = new ReportBusquedaOCCustomerJRData();
        objReportBusquedaOCCustomerJRData.orgid = adOrgId;
        objReportBusquedaOCCustomerJRData.mproductid = (String) obj[0];
        objReportBusquedaOCCustomerJRData.mproductname = new String(((String) obj[1]).getBytes(), "UTF-8");
        objReportBusquedaOCCustomerJRData.corderid = (String) obj[2];
        objReportBusquedaOCCustomerJRData.orderno = (String) obj[3];
        objReportBusquedaOCCustomerJRData.dateordered = (String) obj[4];
        objReportBusquedaOCCustomerJRData.doctypename = (String) obj[5];
        objReportBusquedaOCCustomerJRData.specialdoctype = (String) obj[6];
        objReportBusquedaOCCustomerJRData.cbpartnerid = (String) obj[7];
        objReportBusquedaOCCustomerJRData.partnername = new String(((String) obj[8]).getBytes(), "UTF-8");
        objReportBusquedaOCCustomerJRData.line = dfInt.format(obj[9]);
        objReportBusquedaOCCustomerJRData.qtyordered = ((BigDecimal) obj[10]).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        objReportBusquedaOCCustomerJRData.priceactual = ((BigDecimal) obj[11]).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        objReportBusquedaOCCustomerJRData.linenetamt = ((BigDecimal) obj[12]).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        objReportBusquedaOCCustomerJRData.uom = (String) obj[13];
        objReportBusquedaOCCustomerJRData.cursymbol = (String) obj[14];
        objReportBusquedaOCCustomerJRData.paymentterm = (String) obj[15];
        objReportBusquedaOCCustomerJRData.paymentmethod = (String) obj[16];
        objReportBusquedaOCCustomerJRData.organizacion = (String) obj[17];
        objReportBusquedaOCCustomerJRData.ruc = (String) obj[18];
        objReportBusquedaOCCustomerJRData.producto = obj[19].toString();
        objReportBusquedaOCCustomerJRData.lineap = (String) obj[20];

        objReportBusquedaOCCustomerJRData.rownum = Long.toString(countRecord);

        vector.addElement(objReportBusquedaOCCustomerJRData);

      }

    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReportBusquedaOCCustomerJRData objReportBusquedaOCCustomerJRData[] = new ReportBusquedaOCCustomerJRData[vector.size()];
    vector.copyInto(objReportBusquedaOCCustomerJRData);

    return (objReportBusquedaOCCustomerJRData);
  }

  public static ReportBusquedaOCCustomerJRData[] set() throws ServletException {
    ReportBusquedaOCCustomerJRData objReportBusquedaOCCustomerJRData[] = new ReportBusquedaOCCustomerJRData[1];
    objReportBusquedaOCCustomerJRData[0] = new ReportBusquedaOCCustomerJRData();
    objReportBusquedaOCCustomerJRData[0].mproductid = "";
    objReportBusquedaOCCustomerJRData[0].corderid = "";
    objReportBusquedaOCCustomerJRData[0].orderno = "";
    objReportBusquedaOCCustomerJRData[0].dateordered = "";
    objReportBusquedaOCCustomerJRData[0].doctypename = "";
    objReportBusquedaOCCustomerJRData[0].specialdoctype = "";
    objReportBusquedaOCCustomerJRData[0].cbpartnerid = "";
    objReportBusquedaOCCustomerJRData[0].partnername = "";
    objReportBusquedaOCCustomerJRData[0].line = "";
    objReportBusquedaOCCustomerJRData[0].qtyordered = "";
    objReportBusquedaOCCustomerJRData[0].priceactual = "";
    objReportBusquedaOCCustomerJRData[0].linenetamt = "";
    objReportBusquedaOCCustomerJRData[0].uom = "";
    objReportBusquedaOCCustomerJRData[0].cursymbol = "";

    return objReportBusquedaOCCustomerJRData;
  }

  public static String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.taxid || ' - ' ||C_BPARTNER.NAME" + "      FROM C_BPARTNER" + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

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
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_PRODUCT.VALUE || ' - ' || M_PRODUCT.NAME AS NAME" + "      FROM M_PRODUCT" + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

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
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static String selectPrdcProductgroup(ConnectionProvider connectionProvider, String mProductId) throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION AS NAME" + "      FROM PRDC_PRODUCTGROUP" + "      WHERE PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID = ?";

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
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static ReportBusquedaOCCustomerJRData[] selectProductLineDouble(ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectProductLineDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportBusquedaOCCustomerJRData[] selectProductLineDouble(ConnectionProvider connectionProvider, String adUserClient, int firstRegister, int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql + "       SELECT ORG.AD_ORG_ID AS PADRE, PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID AS ID, TO_CHAR(PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION) AS NAME" + "         FROM PRDC_PRODUCTGROUP, AD_ORG ORG " + "        WHERE 1=1" + "          AND PRDC_PRODUCTGROUP.ISACTIVE='Y'" + "          AND PRDC_PRODUCTGROUP.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql + ")         AND ad_isorgincluded(ORG.AD_ORG_ID,PRDC_PRODUCTGROUP.AD_ORG_ID,PRDC_PRODUCTGROUP.AD_Client_ID)<>-1" + "        ORDER BY PADRE, NAME";

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
        ReportBusquedaOCCustomerJRData objRankingProductosJRData = new ReportBusquedaOCCustomerJRData();
        objRankingProductosJRData.padre = UtilSql.getValue(result, "padre");
        objRankingProductosJRData.id = UtilSql.getValue(result, "id");
        objRankingProductosJRData.name = UtilSql.getValue(result, "name");
        objRankingProductosJRData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objRankingProductosJRData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
    ReportBusquedaOCCustomerJRData objRankingProductosJRData[] = new ReportBusquedaOCCustomerJRData[vector.size()];
    vector.copyInto(objRankingProductosJRData);
    return (objRankingProductosJRData);
  }

}
