//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

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

class ReporteHistorialPagoProveedoresData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteHistorialPagoProveedoresData.class);
  private String InitRecordNumber = "0";
  public String invoice_id;
  public String tipodoc;
  public String fechapago;
  public String formapago;
  public String numerodoc;
  public String referencia;
  public String fechafactura;
  public String banco;
  public String cuenta;
  public String monedafactura;
  public String monedapago;
  public BigDecimal importeorigen;
  public BigDecimal tc;
  public BigDecimal pago;
  public BigDecimal pagodoc;
  public BigDecimal saldoapagar;
  public String idperiodo;
  public String periodo;
  public String fechainicial;
  public String fechafinal;
  public String idorganizacion;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("invoice_id"))
      return invoice_id;
    else if (fieldName.equalsIgnoreCase("tipodoc"))
      return tipodoc;
    else if (fieldName.equalsIgnoreCase("fechapago"))
      return fechapago;
    else if (fieldName.equalsIgnoreCase("formapago"))
      return formapago;
    else if (fieldName.equalsIgnoreCase("numerodoc"))
      return numerodoc;
    else if (fieldName.equalsIgnoreCase("referencia"))
      return referencia;
    else if (fieldName.equalsIgnoreCase("fechafactura"))
      return fechafactura;
    else if (fieldName.equalsIgnoreCase("banco"))
      return banco;
    else if (fieldName.equalsIgnoreCase("cuenta"))
      return cuenta;
    else if (fieldName.equalsIgnoreCase("monedafactura"))
      return monedafactura;
    else if (fieldName.equalsIgnoreCase("monedapago"))
      return monedapago;
    else if (fieldName.equalsIgnoreCase("importeorigen"))
      return importeorigen.toString();
    else if (fieldName.equalsIgnoreCase("tc"))
      return tc.toString();
    else if (fieldName.equalsIgnoreCase("pago"))
      return pago.toString();
    else if (fieldName.equalsIgnoreCase("pagodoc"))
      return pagodoc.toString();
    else if (fieldName.equalsIgnoreCase("saldoapagar"))
      return saldoapagar.toString();
    else if (fieldName.equalsIgnoreCase("idperiodo"))
      return idperiodo;
    else if (fieldName.equalsIgnoreCase("periodo"))
      return periodo;
    else if (fieldName.equalsIgnoreCase("fechainicial"))
      return fechainicial;
    else if (fieldName.equalsIgnoreCase("fechafinal"))
      return fechafinal;
    else if (fieldName.equalsIgnoreCase("idorganizacion"))
      return idorganizacion;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteHistorialPagoProveedoresData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String strcBpartnetId, String adUserId, String adLanguage)
      throws ServletException {
    return select(adOrgId, adClientId, startDate, endDate, strcBpartnetId, adUserId, adLanguage, 0,
        0);
  }

  public static ReporteHistorialPagoProveedoresData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String strcBpartnetId, String adUserId, String adLanguage,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = "  select inv.c_invoice_id as invoice_id, "
        + "     coalesce((select name from c_doctype_trl where c_doctype_id = doc.c_doctype_id and ad_language = '"
        + adLanguage
        + "'),doc.name) as tipodoc, "
        + "to_char(COALESCE(pay.paymentdate,det.em_sco_externalpaydate)) as fechapago, "
        + "(select name from fin_paymentmethod where fin_paymentmethod_id = pay.fin_paymentmethod_id) as formapago, "
        + "inv.em_scr_physical_documentno as numerodoc,"

        + "     case when em_sco_isrendcuentapayment='Y' then 'Pago por Cuentas a Rendir' "
        + "     when em_sco_iswithholdingpayment='Y' then 'Pago por Retención' "
        + "     when em_sco_iscompensationpayment='Y' then 'Pago por Compensación' else "
        + "     pay.description end as referencia, "
        + "     to_char(inv.em_sco_newdateinvoiced) as fechafactura, "
        + "     fa.codebank as banco,fa.accountno as cuenta, "
        + "     coalesce(cur.cursymbol,'') as monedafactura, "
        + "     coalesce(paycur.cursymbol,cur.cursymbol) as monedapago, "
        + "     inv.grandtotal  as importeorigen, "
        + "     coalesce (pdt.em_sco_convert_rate,1) as tc, "
        + "     sum(coalesce(pdt.em_sco_paymentamount,0.00)) as pago, "
        + "     sum(coalesce(pdt.amount,0.00)) as pagodoc, "
        + "     inv.outstandingamt as saldoapagar "

        + "     from c_invoice inv "
        + "     join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id "
        + "     join c_currency cur on inv.c_currency_id = cur.c_currency_id "
        + "     join fin_payment_schedule sch on inv.c_invoice_id = sch.c_invoice_id "
        + "     join fin_payment_scheduledetail det on sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice "
        + "     left join fin_payment_detail pdt on det.fin_payment_detail_id = pdt.fin_payment_detail_id "
        + "     left join fin_payment pay on pdt.fin_payment_id = pay.fin_payment_id "
        + "     left join fin_financial_account fap on pay.fin_financial_account_id = fap.fin_financial_account_id "
        + "     left join c_currency paycur on fap.c_currency_id = paycur.c_currency_id "
        + "     left join fin_financial_account fa on pay.fin_financial_account_id = fa.fin_financial_account_id "
        + "     ,ad_org org join ad_orgtype typ using (ad_orgtype_id) "
        + "     where (det.FIN_Payment_Detail_ID IS NOT NULL OR det.em_sco_externalpayment='Y') and  "
        + "     ad_isorgincluded(inv.ad_org_id, org.ad_org_id, inv.ad_client_id)<>-1 "
        + "     and(typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
        + "     and inv.isactive = 'Y' "
        + "     and inv.issotrx = 'N' "
        + "     and cast(inv.em_sco_newdateinvoiced as date) between to_date('"
        + startDate
        + "') and to_date('"
        + endDate
        + "') "
        + "and inv.c_bpartner_id in ('"
        + strcBpartnetId
        + "') "
        + "and org.ad_org_id in ('"
        + adOrgId
        + "') "
        + "     group by 1,2,3,4,5,6,7,8,9,10,11,12,13,16 ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    DecimalFormat df = new DecimalFormat("#0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    DecimalFormat dfInt = new DecimalFormat("0.##");
    dfInt.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        ReporteHistorialPagoProveedoresData objectReporteHistorialPagoProveedoresData = new ReporteHistorialPagoProveedoresData();
        objectReporteHistorialPagoProveedoresData.invoice_id = (String) obj[0];
        objectReporteHistorialPagoProveedoresData.tipodoc = (String) obj[1];
        objectReporteHistorialPagoProveedoresData.fechapago = (String) obj[2];
        objectReporteHistorialPagoProveedoresData.formapago = (String) obj[3];
        objectReporteHistorialPagoProveedoresData.numerodoc = (String) obj[4];
        objectReporteHistorialPagoProveedoresData.referencia = (String) obj[5];
        objectReporteHistorialPagoProveedoresData.fechafactura = (String) obj[6];
        objectReporteHistorialPagoProveedoresData.banco = (String) obj[7];
        objectReporteHistorialPagoProveedoresData.cuenta = (String) obj[8];
        objectReporteHistorialPagoProveedoresData.monedafactura = (String) obj[9];
        objectReporteHistorialPagoProveedoresData.monedapago = (String) obj[10];
        objectReporteHistorialPagoProveedoresData.importeorigen = ((BigDecimal) obj[11]).setScale(
            3, BigDecimal.ROUND_HALF_UP);
        objectReporteHistorialPagoProveedoresData.tc = ((BigDecimal) obj[12]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteHistorialPagoProveedoresData.pago = ((BigDecimal) obj[13]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteHistorialPagoProveedoresData.pagodoc = ((BigDecimal) obj[14]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteHistorialPagoProveedoresData.saldoapagar = ((BigDecimal) obj[15]).setScale(3,
            BigDecimal.ROUND_HALF_UP);

        objectReporteHistorialPagoProveedoresData.rownum = Long.toString(countRecord);
        objectReporteHistorialPagoProveedoresData.InitRecordNumber = Integer
            .toString(firstRegister);

        vector.addElement(objectReporteHistorialPagoProveedoresData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      return (null);

      // throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteHistorialPagoProveedoresData objectReporteHistorialPagoProveedoresData[] = new ReporteHistorialPagoProveedoresData[vector
        .size()];
    vector.copyInto(objectReporteHistorialPagoProveedoresData);

    return (objectReporteHistorialPagoProveedoresData);
  }

  public static ReporteHistorialPagoProveedoresData[] select_periodos(
      ConnectionProvider connectionProvider) throws ServletException {
    return select_periodos(connectionProvider, 0, 0);
  }

  public static ReporteHistorialPagoProveedoresData[] select_periodos(
      ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "select c_period_id as idperiodo,name as periodo, "
        + "to_char(startdate,'dd-MM-yyyy') as fechainicial, "
        + "to_char(enddate,'dd-MM-yyyy') as fechafinal, ad_org_id as idorganizacion from c_period "
        + "where periodtype!='A' order by startdate";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());

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
        ReporteHistorialPagoProveedoresData objectReporteHistorialPagoProveedoresData = new ReporteHistorialPagoProveedoresData();
        objectReporteHistorialPagoProveedoresData.idperiodo = UtilSql.getValue(result, "idperiodo");
        objectReporteHistorialPagoProveedoresData.periodo = UtilSql.getValue(result, "periodo");
        objectReporteHistorialPagoProveedoresData.fechainicial = UtilSql.getValue(result,
            "fechainicial");
        objectReporteHistorialPagoProveedoresData.fechafinal = UtilSql.getValue(result,
            "fechafinal");
        objectReporteHistorialPagoProveedoresData.idorganizacion = UtilSql.getValue(result,
            "idorganizacion");
        objectReporteHistorialPagoProveedoresData.rownum = Long.toString(countRecord);
        objectReporteHistorialPagoProveedoresData.InitRecordNumber = Integer
            .toString(firstRegister);
        vector.addElement(objectReporteHistorialPagoProveedoresData);
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
    ReporteHistorialPagoProveedoresData objectReporteHistorialPagoProveedoresData[] = new ReporteHistorialPagoProveedoresData[vector
        .size()];
    vector.copyInto(objectReporteHistorialPagoProveedoresData);
    System.out.println("LOL");
    return (objectReporteHistorialPagoProveedoresData);
  }

  public static String selectSocialName(ConnectionProvider connectionProvider, String organization) {
    String strSql = "";
    strSql = "select social_name from ad_org where ad_org_id = '" + organization + "'";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, organization);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "social_name");
      }
      result.close();
    } catch (SQLException e) {
      strReturn = "Error sql";
    } catch (Exception ex) {
      strReturn = "Error LOL";
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);

  }

  public static ReporteHistorialPagoProveedoresData[] set() throws ServletException {
    ReporteHistorialPagoProveedoresData objectReporteHistorialPagoProveedoresData[] = new ReporteHistorialPagoProveedoresData[1];
    objectReporteHistorialPagoProveedoresData[0] = new ReporteHistorialPagoProveedoresData();
    objectReporteHistorialPagoProveedoresData[0].invoice_id = "";
    objectReporteHistorialPagoProveedoresData[0].tipodoc = "";
    objectReporteHistorialPagoProveedoresData[0].fechapago = "";
    objectReporteHistorialPagoProveedoresData[0].formapago = "";
    objectReporteHistorialPagoProveedoresData[0].numerodoc = "";
    objectReporteHistorialPagoProveedoresData[0].referencia = "";
    objectReporteHistorialPagoProveedoresData[0].fechafactura = "";
    objectReporteHistorialPagoProveedoresData[0].banco = "";
    objectReporteHistorialPagoProveedoresData[0].cuenta = "";
    objectReporteHistorialPagoProveedoresData[0].monedafactura = "";
    objectReporteHistorialPagoProveedoresData[0].monedapago = "";
    objectReporteHistorialPagoProveedoresData[0].importeorigen = new BigDecimal(0);
    objectReporteHistorialPagoProveedoresData[0].tc = new BigDecimal(0);
    objectReporteHistorialPagoProveedoresData[0].pago = new BigDecimal(0);
    objectReporteHistorialPagoProveedoresData[0].pagodoc = new BigDecimal(0);
    objectReporteHistorialPagoProveedoresData[0].saldoapagar = new BigDecimal(0);

    return objectReporteHistorialPagoProveedoresData;
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

  public static String selectRucOrganization(ConnectionProvider connectionProvider,
      String organizacion_id) throws ServletException {
    String strSql = "";
    strSql = strSql + " select ad_orginfo.taxid from ad_org "
        + "join ad_orginfo on ad_org.ad_org_id = ad_orginfo.ad_org_id "
        + "where ad_org.ad_org_id = ? ";

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
        strReturn = UtilSql.getValue(result, "taxid");
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

  public static String selectBPartner(ConnectionProvider connectionProvider, String bpartner_id)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        + " select taxid || ' - ' || name as name from c_bpartner where c_bpartner_id = ? ";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, bpartner_id);

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

  public static String selectPeriodo(ConnectionProvider connectionProvider, String period_id)
      throws ServletException {
    String strSql = "";
    strSql = strSql + " select name from c_period where c_period_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, period_id);

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