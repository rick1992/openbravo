//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.commons.lang.text.StrLookup;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReporteComprasPorProveedorData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteComprasPorProveedorData.class);
  private String InitRecordNumber = "0";
  public String invoice_id;
  public String fecha;
  public String feccosteo;
  public String nroregistro;
  public String tipodoc;
  public String documento;
  public String cuenta;
  public String moneda;
  public BigDecimal valorcompra;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("invoice_id"))
      return invoice_id;
    else if (fieldName.equalsIgnoreCase("fecha"))
      return fecha;
    else if (fieldName.equalsIgnoreCase("feccosteo"))
      return feccosteo;
    else if (fieldName.equalsIgnoreCase("nroregistro"))
      return nroregistro;
    else if (fieldName.equalsIgnoreCase("tipodoc"))
      return tipodoc;
    else if (fieldName.equalsIgnoreCase("documento"))
      return documento;
    else if (fieldName.equalsIgnoreCase("cuenta"))
      return cuenta;
    else if (fieldName.equalsIgnoreCase("moneda"))
      return moneda;
    else if (fieldName.equalsIgnoreCase("valorcompra"))
      return valorcompra.toString();
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteComprasPorProveedorData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String bpartner, String adUserId) throws ServletException {
    return select(adOrgId, adClientId, startDate, endDate, bpartner, adUserId, 0, 0);
  }

  public static ReporteComprasPorProveedorData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String bpartner, String adUserId, int firstRegister,
      int numberRegisters) throws ServletException {

    String strSql = "";
    strSql = " select inv.c_invoice_id as invoice_id,"
        + "to_char(inv.em_sco_newdateinvoiced) as fecha, "
        + " to_char((select tco.created from m_transaction_cost tco "
        + " join m_transaction tra on tco.m_transaction_id = tra.m_transaction_id "
        + " join m_inoutline iol on tra.m_inoutline_id = iol.m_inoutline_id "
        + " join c_invoiceline lin on iol.m_inoutline_id = lin.m_inoutline_id "
        + " where lin.c_invoice_id = inv.c_invoice_id "
        + " order by iol.created limit 1)) as feccosteo, fa.em_sco_regnumber as nroregistro, "
        + " case when doc.em_sco_specialdoctype is null then '' "
        + " when doc.em_sco_specialdoctype = 'SCOARINVOICE' then '01' "
        + " when doc.em_sco_specialdoctype = 'SCOARTICKET' then '03' "
        + " when doc.em_sco_specialdoctype = 'SCOARCREDITMEMO' or doc.em_sco_specialdoctype = 'SCOARINVOICERETURNMAT' then '07' "
        + " when doc.em_sco_specialdoctype = 'SCOAPCREDITMEMO' then '07' "
        + " when doc.em_sco_specialdoctype = 'SCOARDEBITMEMO' then '08' "
        + " when doc.em_sco_specialdoctype = 'SCOAPINVOICE'  or doc.em_sco_specialdoctype = 'SCOAPSIMPLEPROVISIONINVOICE' "
        + " then coalesce((select code from scr_combo_item where scr_combo_item_id = inv.em_sco_podoctype_comboitem_id),'') "
        + " else '' end as tipodoc, "
        + " inv.em_scr_physical_documentno as documento, fa.acctvalue as cuenta, "
        + " coalesce((select cursymbol from c_currency_trl where c_currency_id = cur.c_currency_id),cur.cursymbol) as moneda, "
        + " fa.amtacctcr as valorcompra from c_bpartner bp "
        + " join c_invoice inv on bp.c_bpartner_id = inv.c_bpartner_id "
        + " join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id "
        + " join fact_acct fa on inv.c_invoice_id = fa.record_id "
        + " join c_currency cur on fa.c_currency_id = cur.c_currency_id, "
        + " ad_org org join ad_orgtype typ using (ad_orgtype_id) "
        + " where ad_isorgincluded(inv.ad_org_id, org.ad_org_id, inv.ad_client_id)<>-1 "
        + " and(typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
        + " and doc.ad_table_id = fa.ad_table_id "
        + " and inv.docstatus = 'CO' and inv.issotrx = 'N' "
        + " and CAST(inv.em_sco_newdateinvoiced AS DATE) between to_date('" + startDate + "') and to_date('"
        + endDate + "') ";
    	if(bpartner.compareToIgnoreCase("")!=0 && bpartner!=null ){
    		strSql=strSql+ " and bp.c_bpartner_id in ('" + bpartner + "') ";
    	}
        		
    	strSql=strSql+ " and org.ad_org_id in ('" + adOrgId + "') "
        + " order by 2,inv.em_scr_physical_documentno,c_invoice_id,fa.acctvalue ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>  (0);

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        ReporteComprasPorProveedorData objectReporteComprasPorProveedorData = new ReporteComprasPorProveedorData();

        objectReporteComprasPorProveedorData.invoice_id = (String) obj[0];
        objectReporteComprasPorProveedorData.fecha = (String) obj[1];
        objectReporteComprasPorProveedorData.feccosteo = (String) obj[2];
        objectReporteComprasPorProveedorData.nroregistro = (String) (obj[3]);
        objectReporteComprasPorProveedorData.tipodoc = (String) (obj[4]);
        objectReporteComprasPorProveedorData.documento = (String) obj[5];
        objectReporteComprasPorProveedorData.cuenta = (String) (obj[6]);
        objectReporteComprasPorProveedorData.moneda = (String) obj[7];
        objectReporteComprasPorProveedorData.valorcompra = ((BigDecimal) obj[8]).setScale(3,
            BigDecimal.ROUND_HALF_UP);

        objectReporteComprasPorProveedorData.rownum = Long.toString(countRecord);
        objectReporteComprasPorProveedorData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteComprasPorProveedorData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteComprasPorProveedorData objectReporteComprasPorProveedorData[] = new ReporteComprasPorProveedorData[vector
        .size()];
    vector.copyInto(objectReporteComprasPorProveedorData);

    return (objectReporteComprasPorProveedorData);
  }

  public static ReporteComprasPorProveedorData[] set() throws ServletException {
    ReporteComprasPorProveedorData objectReporteComprasPorProveedorData[] = new ReporteComprasPorProveedorData[1];
    objectReporteComprasPorProveedorData[0] = new ReporteComprasPorProveedorData();
    objectReporteComprasPorProveedorData[0].invoice_id = "";
    objectReporteComprasPorProveedorData[0].fecha = "";
    objectReporteComprasPorProveedorData[0].feccosteo = "";
    objectReporteComprasPorProveedorData[0].nroregistro = "";
    objectReporteComprasPorProveedorData[0].tipodoc = "";
    objectReporteComprasPorProveedorData[0].documento = "";
    objectReporteComprasPorProveedorData[0].cuenta = "";
    objectReporteComprasPorProveedorData[0].moneda = "";
    objectReporteComprasPorProveedorData[0].valorcompra = new BigDecimal(0);

    return objectReporteComprasPorProveedorData;
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
    strSql = strSql + " select name from c_bpartner where c_bpartner_id = ? ";

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

  public static String selectRucBPartner(ConnectionProvider connectionProvider, String bpartner_id)
      throws ServletException {
    String strSql = "";
    strSql = strSql + " select taxid from c_bpartner where c_bpartner_id = ? ";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, bpartner_id);

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

}