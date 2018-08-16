//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.sql.Timestamp;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

class ReporteDetraccionVentasJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteDetraccionVentasJRData.class);
  private String InitRecordNumber = "0";
  public String NRO_FACTURA;
  public String FECHA;
  public String CLIENTE;
  public BigDecimal PORC;
  public BigDecimal MONTO;
  public String ORG;
  public String ORG_RUC;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("NRO_FACTURA"))
      return NRO_FACTURA;
    else if (fieldName.equalsIgnoreCase("FECHA"))
      return FECHA;
    else if (fieldName.equalsIgnoreCase("CLIENTE"))
      return CLIENTE;
    else if (fieldName.equalsIgnoreCase("PORC"))
      return PORC.toString();
    else if (fieldName.equalsIgnoreCase("MONTO"))
      return MONTO.toString();
    if (fieldName.equalsIgnoreCase("ORG"))
      return ORG;
    else if (fieldName.equalsIgnoreCase("ORG_RUC"))
      return ORG_RUC;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }
  
  

  public static ReporteDetraccionVentasJRData[] select(String adClientId, String startDate,
      String endDate, String adOrgId) throws ServletException {
    return select(adClientId, startDate, endDate, adOrgId, 0, 0);
  }

  public static ReporteDetraccionVentasJRData[] select(String adClientId, String startDate,
      String endDate, String adOrgId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = "select coalesce(inv.em_scr_physical_documentno,'') as NRO_FACTURA,"
        + "          to_char(inv.dateinvoiced) as FECHA,"
        + "          coalesce(bp.name,'') as CLIENTE,"
        + "          coalesce(inv.em_sco_detraction_percent,0.00) as PORC,"
        + "          coalesce(inv.grandtotal,0.00) as MONTO,"
        + "          coalesce(org.name,'') as ORG,"
        + "          coalesce(inf.taxid,'') as ORG_RUC"
        + "     from c_invoice inv"
        + "          join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id"
        + "          join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id,"
        + "          ad_org org join ad_orgtype otp using (ad_orgtype_id)"
        + "          join ad_orginfo inf on org.ad_org_id = inf.ad_org_id"
        
        + "    where AD_ISORGINCLUDED(inv.ad_org_id, '"+adOrgId+"', inv.ad_client_id)<>-1"
        + "      and (otp.IsLegalEntity='Y' or otp.IsAcctLegalEntity='Y')"
        + "      and inv.docstatus = 'CO'"
        + "      and inv.em_sco_isdetraction_affected = 'Y'"
        + "      and doc.em_sco_specialdoctype = 'SCOARINVOICE'"
        + "      and 'Y' not in (select pay.em_sco_detractionpayment "
        + "                        from fin_payment_schedule sch"
        + "                             left join fin_payment_scheduledetail det on sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice"
        + "                             left join fin_payment_detail pd on det.fin_payment_detail_id = pd.fin_payment_detail_id"
        + "                             left join fin_payment pay on pd.fin_payment_id = pay.fin_payment_id"
        + "                       where pay.isactive = 'Y'"
        + "                         and sch.c_invoice_id = inv.c_invoice_id)"
        
        
        + "      and inv.dateinvoiced between TO_DATE('" + startDate + "', 'DD-MM-YYYY') and TO_DATE('" + endDate + "', 'DD-MM-YYYY')"
        + "      and org.ad_org_id in ('" + adOrgId + "')";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;
        ReporteDetraccionVentasJRData objectReporteDetraccionVentasJRData = new ReporteDetraccionVentasJRData();
        objectReporteDetraccionVentasJRData.NRO_FACTURA = (String) obj[0];
        objectReporteDetraccionVentasJRData.FECHA = (String) obj[1];
        objectReporteDetraccionVentasJRData.CLIENTE = (String) obj[2];
        objectReporteDetraccionVentasJRData.PORC = ((BigDecimal) (obj[3])).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteDetraccionVentasJRData.MONTO = ((BigDecimal) (obj[4])).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteDetraccionVentasJRData.ORG = (String) obj[5];
        objectReporteDetraccionVentasJRData.ORG_RUC = (String) obj[6];

        objectReporteDetraccionVentasJRData.rownum = Long.toString(countRecord);
        objectReporteDetraccionVentasJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteDetraccionVentasJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteDetraccionVentasJRData objectReporteDetraccionVentasJRData[] = new ReporteDetraccionVentasJRData[vector
        .size()];
    vector.copyInto(objectReporteDetraccionVentasJRData);

    return (objectReporteDetraccionVentasJRData);
  }

  public static ReporteDetraccionVentasJRData[] set() throws ServletException {
    ReporteDetraccionVentasJRData objectReporteDetraccionVentasJRData[] = new ReporteDetraccionVentasJRData[1];
    objectReporteDetraccionVentasJRData[0] = new ReporteDetraccionVentasJRData();
    objectReporteDetraccionVentasJRData[0].NRO_FACTURA = "";
    objectReporteDetraccionVentasJRData[0].FECHA = "";
    objectReporteDetraccionVentasJRData[0].CLIENTE = "";
    objectReporteDetraccionVentasJRData[0].PORC = new BigDecimal(0);
    objectReporteDetraccionVentasJRData[0].MONTO = new BigDecimal(0);
    objectReporteDetraccionVentasJRData[0].ORG = "";
    objectReporteDetraccionVentasJRData[0].ORG_RUC = "";

    return objectReporteDetraccionVentasJRData;
  }

}
