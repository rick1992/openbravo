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

class ReporteCargaVentaData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteCargaVentaData.class);
  private String InitRecordNumber = "0";
  public String codigo;
  public String descripcion;
  public String ruc;
  public String razonSocial;
  public BigDecimal cantidad;
  public String unidad;
  public BigDecimal precioUnitario;
  public BigDecimal precioTotal;
  public String moneda;
  public String fecha;
  public String ubigeo;
  public String numeroDoc;
  public String tipoDoc;
  public String mercado;
  public String vendedor;
  public String zona;
  public String ciudad;
  public String distrito;
  public String regalo;
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
    else if (fieldName.equalsIgnoreCase("ruc"))
      return ruc;
    else if (fieldName.equalsIgnoreCase("razonSocial"))
      return razonSocial;
    else if (fieldName.equalsIgnoreCase("cantidad"))
      return cantidad.toString();
    else if (fieldName.equalsIgnoreCase("unidad"))
      return unidad;
    else if (fieldName.equalsIgnoreCase("precioUnitario"))
      return precioUnitario.toString();
    else if (fieldName.equalsIgnoreCase("precioTotal"))
      return precioTotal.toString();
    else if (fieldName.equalsIgnoreCase("moneda"))
      return moneda;
    else if (fieldName.equalsIgnoreCase("fecha"))
      return fecha;
    else if (fieldName.equalsIgnoreCase("ubigeo"))
      return ubigeo;
    else if (fieldName.equalsIgnoreCase("numeroDoc"))
      return numeroDoc;
    else if (fieldName.equalsIgnoreCase("tipoDoc"))
      return tipoDoc;
    else if (fieldName.equalsIgnoreCase("mercado"))
      return mercado;
    else if (fieldName.equalsIgnoreCase("vendedor"))
      return vendedor;
    else if (fieldName.equalsIgnoreCase("zona"))
      return zona;
    else if (fieldName.equalsIgnoreCase("ciudad"))
      return ciudad;
    else if (fieldName.equalsIgnoreCase("distrito"))
      return distrito;
    else if (fieldName.equalsIgnoreCase("regalo"))
      return regalo;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteCargaVentaData[] select(String adOrgId, String adClientId, String startDate,
      String endDate, String adUserId) throws ServletException {
    return select(adOrgId, adClientId, startDate, endDate, adUserId, 0, 0);
  }

  public static ReporteCargaVentaData[] select(String adOrgId, String adClientId, String startDate,
      String endDate, String adUserId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = ""
        + "select pro.value as codigo, "
        + "substring(pro.name for 40) as descripcion, "
        + "substring(bp.taxid for 13) as ruc, "
        + "substring(bp.name for 100) as razonSocial, "
        + "lin.qtyinvoiced as cantidad, "
        + "substring((select uomsymbol from c_uom where c_uom_id = uom.c_uom_id) for 5) as unidad, "

        + "coalesce(lin.priceactual,0) as precioUnitario, "
        + "coalesce(lin.priceactual,0)*coalesce(lin.qtyinvoiced,0) as precioTotal, "
        + "to_char((select iso_code from c_currency where c_currency_id = cur.c_currency_id)) as moneda, "
        + "to_char(inv.dateacct) as fecha, "
        + "to_char('') as ubigeo, "
        + "inv.em_scr_physical_documentno numeroDoc, "
        + "case doc.em_sco_specialdoctype "
        + "when 'SCOARINVOICE' then 'FV' "
        + "when 'SCOARTICKET' then 'BV' when 'SCOARINVOICERETURNMAT' then 'NA' "
        + " "
        + "else '' end as tipoDoc, "
        + "to_char('') as mercado, "
        + "substring(srep.name for 15) as vendedor, "

        + "to_char('CENTRO') as zona, "
        + "case when upper(re.name)='LIMA' THEN re.name ELSE loc.city END as ciudad, "
        + "loc.city as distrito, "
        + "to_char('') as regalo "

        + "from c_invoice inv "
        + "join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id "
        + "join c_invoiceline lin on inv.c_invoice_id = lin.c_invoice_id "
        + "join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id "
        + "join c_currency cur on inv.c_currency_id = cur.c_currency_id "
        + "join m_product pro on lin.m_product_id = pro.m_product_id "
        + "join c_uom uom on pro.c_uom_id = uom.c_uom_id "
        + "join c_bpartner pri on pro.em_swa_primary_partner_id = pri.c_bpartner_id "
        + "join ad_org org on org.ad_org_id=inv.ad_org_id  "
        + "join ad_orginfo oi on org.ad_org_id=oi.ad_org_id "
        + "left join ad_user sr ON sr.ad_user_id = inv.salesrep_id "
        + "left join c_bpartner srep ON srep.c_bpartner_id = sr.c_bpartner_id "
        + "join c_location loc ON loc.c_location_id=oi.c_location_id inner join c_region re ON re.c_region_id=loc.c_region_id "
        + "where  trunc(cast(inv.dateacct as DATE)) >= to_date('" + startDate + "') "
        + " and trunc(cast(inv.dateacct as DATE)) <= to_date('" + endDate + "') "
        + " and inv.issotrx = 'Y' " + " and org.ad_org_id in ('" + adOrgId + "') "
        + "and pri.taxid = '20100119227' and inv.docstatus='CO' "
        + "order by inv.dateacct,inv.c_invoice_id,lin.line ";

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

        ReporteCargaVentaData objectReporteCargaVentaData = new ReporteCargaVentaData();

        objectReporteCargaVentaData.codigo = (String) obj[0];
        objectReporteCargaVentaData.descripcion = (String) obj[1];
        objectReporteCargaVentaData.ruc = (String) (obj[2]);
        objectReporteCargaVentaData.razonSocial = (String) (obj[3]);
        objectReporteCargaVentaData.cantidad = ((BigDecimal) obj[4]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteCargaVentaData.unidad = (String) (obj[5]);
        objectReporteCargaVentaData.precioUnitario = ((BigDecimal) obj[6]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteCargaVentaData.precioTotal = ((BigDecimal) obj[7]).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteCargaVentaData.moneda = (String) (obj[8]);
        objectReporteCargaVentaData.fecha = (String) (obj[9]);
        objectReporteCargaVentaData.ubigeo = (String) (obj[10]);
        objectReporteCargaVentaData.numeroDoc = (String) (obj[11]);
        objectReporteCargaVentaData.tipoDoc = (String) (obj[12]);
        objectReporteCargaVentaData.mercado = (String) (obj[13]);
        objectReporteCargaVentaData.vendedor = (String) (obj[14]);
        objectReporteCargaVentaData.zona = (String) (obj[15]);
        objectReporteCargaVentaData.ciudad = (String) (obj[16]);
        objectReporteCargaVentaData.distrito = (String) (obj[17]);
        objectReporteCargaVentaData.regalo = (String) (obj[18]);
        objectReporteCargaVentaData.rownum = Long.toString(countRecord);
        objectReporteCargaVentaData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteCargaVentaData);

      }
    } catch (Exception ex) { 
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteCargaVentaData objectReporteCargaVentaData[] = new ReporteCargaVentaData[vector.size()];
    vector.copyInto(objectReporteCargaVentaData);

    return (objectReporteCargaVentaData);
  }

  public static ReporteCargaVentaData[] set() throws ServletException {
    ReporteCargaVentaData objectReporteCargaVentaData[] = new ReporteCargaVentaData[1];
    objectReporteCargaVentaData[0] = new ReporteCargaVentaData();
    objectReporteCargaVentaData[0].codigo = "";
    objectReporteCargaVentaData[0].descripcion = "";
    objectReporteCargaVentaData[0].ruc = "";
    objectReporteCargaVentaData[0].razonSocial = "";
    objectReporteCargaVentaData[0].cantidad = new BigDecimal(0);
    objectReporteCargaVentaData[0].unidad = "";
    objectReporteCargaVentaData[0].precioUnitario = new BigDecimal(0);
    objectReporteCargaVentaData[0].precioTotal = new BigDecimal(0);
    objectReporteCargaVentaData[0].moneda = "";
    objectReporteCargaVentaData[0].fecha = "";
    objectReporteCargaVentaData[0].ubigeo = "";
    objectReporteCargaVentaData[0].numeroDoc = "";
    objectReporteCargaVentaData[0].tipoDoc = "";
    objectReporteCargaVentaData[0].mercado = "";
    objectReporteCargaVentaData[0].vendedor = "";
    objectReporteCargaVentaData[0].zona = "";
    objectReporteCargaVentaData[0].ciudad = "";
    objectReporteCargaVentaData[0].distrito = "";
    objectReporteCargaVentaData[0].regalo = "";

    return objectReporteCargaVentaData;
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