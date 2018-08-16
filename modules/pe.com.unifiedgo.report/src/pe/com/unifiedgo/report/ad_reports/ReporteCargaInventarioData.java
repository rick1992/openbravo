//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class ReporteCargaInventarioData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteCargaInventarioData.class);
  private String InitRecordNumber = "0";
  public String codigo;
  public String descripcion;
  public BigDecimal cantidad;
  public String unidad;
  public String fecha;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("codigo"))
      return codigo;
    else if (fieldName.equalsIgnoreCase("descripcion"))
      return descripcion;
    else if (fieldName.equalsIgnoreCase("cantidad"))
      return cantidad.toString();
    else if (fieldName.equalsIgnoreCase("unidad"))
      return unidad;
    else if (fieldName.equalsIgnoreCase("fecha"))
      return fecha;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteCargaInventarioData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String adUserId) throws ServletException {
    return select(adOrgId, adClientId, startDate, endDate, adUserId, 0, 0);
  }

  public static ReporteCargaInventarioData[] select(String adOrgId, String adClientId,
      String startDate, String endDate, String adUserId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = "select pro.value as codigo, pro.name as descripcion, "
        + "lin.qtyinvoiced as cantidad, "
        + "coalesce((select name from c_uom_trl where c_uom_id = uom.c_uom_id),uom.name) as unidad, "
        + "to_char(inv.dateacct) as fecha " + "from c_invoice inv "
        + "join c_invoiceline lin on inv.c_invoice_id = lin.c_invoice_id "
        + "join c_uom uom on lin.c_uom_id = uom.c_uom_id "
        + "join m_product pro on lin.m_product_id = pro.m_product_id "
        + "join c_bpartner pri on pro.em_swa_primary_partner_id = pri.c_bpartner_id, "
        + "ad_org org join ad_orgtype t using (ad_orgtype_id) "
        + "where ad_isorgincluded(inv.ad_org_id, org.ad_org_id, inv.ad_client_id)<>-1 "
        + "and (t.IsLegalEntity='Y' or t.IsAcctLegalEntity='Y') " + "and lin.isactive = 'Y' "
        + "and cast(inv.dateacct as DATE) between to_date('" + startDate + "') and to_date('"
        + endDate + "') " + "and org.ad_org_id in ('" + adOrgId + "') "
        + "and pri.taxid = '20100119227' and inv.issotrx = 'N' ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        ReporteCargaInventarioData objectReporteCargaInventarioData = new ReporteCargaInventarioData();
        objectReporteCargaInventarioData.codigo = (String) obj[0];
        objectReporteCargaInventarioData.descripcion = (String) obj[1];
        objectReporteCargaInventarioData.cantidad = ((BigDecimal) obj[2]).setScale(3,
            BigDecimal.ROUND_HALF_UP);

        objectReporteCargaInventarioData.unidad = (String) obj[3];
        objectReporteCargaInventarioData.fecha = (String) obj[4];

        objectReporteCargaInventarioData.rownum = Long.toString(countRecord);
        objectReporteCargaInventarioData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteCargaInventarioData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteCargaInventarioData objectReporteCargaInventarioData[] = new ReporteCargaInventarioData[vector
        .size()];
    vector.copyInto(objectReporteCargaInventarioData);

    return (objectReporteCargaInventarioData);
  }

  public static ReporteCargaInventarioData[] selectSaldos(String adOrgId, String adClientId,
      String startDate, String endDate, String adUserId) throws ServletException {
    return selectSaldos(adOrgId, adClientId, startDate, endDate, adUserId, 0, 0);
  }

  public static ReporteCargaInventarioData[] selectSaldos(String adOrgId, String adClientId,
      String startDate, String endDate, String adUserId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = "select * from ("
        + "select pro.value as codigo, substring(pro.name for 40) as descripcion,"
        + "substring((select uomsymbol from c_uom where c_uom_id = uom.c_uom_id) for 5) as unidad,"
        + "sum(t.movementqty) as cantidad " + "from m_transaction t "
        + "join m_product pro on t.m_product_id = pro.m_product_id "
        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id  "
        + "join c_bpartner pri on pro.em_swa_primary_partner_id = pri.c_bpartner_id "
        + "join m_locator lo on lo.m_locator_id=t.m_locator_id "
        + "join m_warehouse w on w.m_warehouse_id=lo.m_warehouse_id "
        + "join ad_org org on org.ad_org_id=w.em_swa_organization_child_id "
        + "where trunc(cast(t.movementdate as DATE)) <= to_date('" + endDate
        + "') and org.ad_org_id in ('" + adOrgId + "') " + "and pri.taxid = '20100119227' "
        + "group by pro.value, pro.name, uom.c_uom_id, uom.name " + ") a where cantidad<>0";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;

        ReporteCargaInventarioData objectReporteCargaInventarioData = new ReporteCargaInventarioData();
        objectReporteCargaInventarioData.codigo = (String) obj[0];
        objectReporteCargaInventarioData.descripcion = (String) obj[1];

        objectReporteCargaInventarioData.unidad = (String) obj[2];
        objectReporteCargaInventarioData.cantidad = ((BigDecimal) obj[3]).setScale(3,
            BigDecimal.ROUND_HALF_UP);

        objectReporteCargaInventarioData.rownum = Long.toString(countRecord);
        objectReporteCargaInventarioData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteCargaInventarioData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteCargaInventarioData objectReporteCargaInventarioData[] = new ReporteCargaInventarioData[vector
        .size()];
    vector.copyInto(objectReporteCargaInventarioData);

    return (objectReporteCargaInventarioData);
  }

  // public static String selectSocialName(ConnectionProvider connectionProvider, String
  // organization) {
  // String strSql = "";
  // strSql = "select social_name from ad_org where ad_org_id = '" + organization + "'";
  //
  // ResultSet result;
  // String strReturn = "0";
  // PreparedStatement st = null;
  //
  // int iParameter = 0;
  // try {
  // st = connectionProvider.getPreparedStatement(strSql);
  // QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
  // iParameter++;
  // UtilSql.setValue(st, iParameter, 12, null, organization);
  //
  // result = st.executeQuery();
  // if (result.next()) {
  // strReturn = UtilSql.getValue(result, "social_name");
  // }
  // result.close();
  // } catch (SQLException e) {
  // strReturn = "Error sql";
  // } catch (Exception ex) {
  // strReturn = "Error LOL";
  // } finally {
  // try {
  // connectionProvider.releasePreparedStatement(st);
  // } catch (Exception ignore) {
  // ignore.printStackTrace();
  // }
  // }
  // return (strReturn);
  //
  // }

  public static ReporteCargaInventarioData[] set() throws ServletException {
    ReporteCargaInventarioData objectReporteCargaInventarioData[] = new ReporteCargaInventarioData[1];
    objectReporteCargaInventarioData[0] = new ReporteCargaInventarioData();
    objectReporteCargaInventarioData[0].codigo = "";
    objectReporteCargaInventarioData[0].descripcion = "";
    objectReporteCargaInventarioData[0].cantidad = new BigDecimal(0);
    objectReporteCargaInventarioData[0].unidad = "";
    objectReporteCargaInventarioData[0].fecha = "";

    return objectReporteCargaInventarioData;
  }

  public static String selectSocialName(ConnectionProvider connectionProvider, String organization)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "        SELECT social_name" + "        FROM AD_ORG"
        + "        WHERE AD_ORG_ID = ?";

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