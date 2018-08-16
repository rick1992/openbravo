//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

class ReporteAgedTrialJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteAgedTrialJRData.class);
  private String InitRecordNumber = "0";
  public String INV_ID;
  public String NRO_INV;
  public String FECHA;
  public String BPARTNER_ID;
  public String BPARTNER;
  public BigDecimal IMPORTE;
  public String TIPO;
  public String ORG;
  public String RUC_ORG;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("INV_ID"))
      return INV_ID;
    else if (fieldName.equalsIgnoreCase("NRO_INV"))
      return NRO_INV;
    else if (fieldName.equalsIgnoreCase("FECHA"))
      return FECHA;
    else if (fieldName.equalsIgnoreCase("BPARTNER_ID"))
      return BPARTNER_ID;
    else if (fieldName.equalsIgnoreCase("BPARTNER"))
      return BPARTNER;
    else if (fieldName.equalsIgnoreCase("IMPORTE"))
      return IMPORTE.toString();
    else if (fieldName.equalsIgnoreCase("TIPO"))
      return TIPO;
    if (fieldName.equalsIgnoreCase("ORG"))
      return ORG;
    else if (fieldName.equalsIgnoreCase("RUC_ORG"))
      return RUC_ORG;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteAgedTrialJRData[] select(String adClientId, String strAD_Org_ID)
      throws ServletException {
    return select(adClientId, strAD_Org_ID, 0, 0);
  }

  public static ReporteAgedTrialJRData[] select(String adClientId, String strAD_Org_ID,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";

    strSql = "select inv.c_invoice_id as INV_ID, "
        + "coalesce(inv.em_scr_physical_documentno,'') as NRO_INV, "
        + "to_char(inv.em_sco_newdateinvoiced) as FECHA, "
        + "bp.c_bpartner_id as BPARTNER_ID, "
        + "coalesce(bp.name,'') as BPARTNER, "

        + "sim_currency_convert_table(inv.grandtotal,cur.c_currency_id, "
        + "(select c_currency_id from c_currency where iso_code = 'PEN' and isactive = 'Y'), "
        + "inv.em_sco_newdateinvoiced,dtp.ad_table_id,inv.c_invoice_id,inv.ad_client_id,inv.ad_org_id) "
        + "as IMPORTE, "

        + "case " + "when ter.netdays <=30 then '1' "
        + "when ter.netdays >30 and ter.netdays <=60 then '2' "
        + "when ter.netdays >60 and ter.netdays <=90 then '3' "
        + "when ter.netdays >90 and ter.netdays <=120 then '4' "
        + "when ter.netdays >120 then '5' end as TIPO, "

        + "coalesce(org.name,'') as ORG, " + "coalesce('R.U.C.: '||inf.taxid,'') as RUC_ORG "

        + "from c_invoice inv "
        + "left join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id "
        + "left join c_paymentterm ter on inv.c_paymentterm_id = ter.c_paymentterm_id "
        + "left join c_currency cur on inv.c_currency_id = cur.c_currency_id "
        + "left join c_doctype dtp on inv.c_doctypetarget_id = dtp.c_doctype_id "
        + "left join fin_payment_schedule sch on inv.c_invoice_id = sch.c_invoice_id "

        + ",ad_org org join ad_orgtype typ on org.ad_orgtype_id = typ.ad_orgtype_id "
        + "join ad_orginfo inf on org.ad_org_id = inf.ad_org_id "

        + "where AD_ISORGINCLUDED(inv.ad_org_id, org.ad_org_id, inv.ad_client_id)<>-1 "
        + "and (typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
        + "and coalesce(sch.outstandingamt,0.00) > 0.00 "
        + "and dtp.em_sco_specialdoctype in ('SCOARINVOICE','SCOARBOEINVOICE') "
        + "and sch.isactive = 'Y' " + "and (inv.ad_org_id in ('" + strAD_Org_ID
        + "') or org.ad_org_id in ('" + strAD_Org_ID + "')) "

        + "order by bp.name desc ";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;
        ReporteAgedTrialJRData objectReporteAgedTrialJRData = new ReporteAgedTrialJRData();

        objectReporteAgedTrialJRData.INV_ID = (String) obj[0];

        objectReporteAgedTrialJRData.NRO_INV = (String) obj[1];

        objectReporteAgedTrialJRData.FECHA = (String) obj[2];

        objectReporteAgedTrialJRData.BPARTNER_ID = (String) obj[3];

        objectReporteAgedTrialJRData.BPARTNER = (String) obj[4];

        objectReporteAgedTrialJRData.IMPORTE = ((BigDecimal) (obj[5])).setScale(3,
            BigDecimal.ROUND_HALF_UP);

        objectReporteAgedTrialJRData.TIPO = (String) obj[6];

        objectReporteAgedTrialJRData.ORG = (String) obj[7];

        objectReporteAgedTrialJRData.RUC_ORG = (String) obj[8];

        objectReporteAgedTrialJRData.rownum = Long.toString(countRecord);

        objectReporteAgedTrialJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteAgedTrialJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteAgedTrialJRData objectReporteAgedTrialJRData[] = new ReporteAgedTrialJRData[vector
        .size()];
    vector.copyInto(objectReporteAgedTrialJRData);

    return (objectReporteAgedTrialJRData);
  }

  public static ReporteAgedTrialJRData[] set() throws ServletException {
    ReporteAgedTrialJRData objectReporteAgedTrialJRData[] = new ReporteAgedTrialJRData[1];
    objectReporteAgedTrialJRData[0] = new ReporteAgedTrialJRData();

    objectReporteAgedTrialJRData[0].INV_ID = "";
    objectReporteAgedTrialJRData[0].NRO_INV = "";
    objectReporteAgedTrialJRData[0].FECHA = "";
    objectReporteAgedTrialJRData[0].BPARTNER_ID = "";
    objectReporteAgedTrialJRData[0].BPARTNER = "";

    objectReporteAgedTrialJRData[0].IMPORTE = new BigDecimal(0);
    objectReporteAgedTrialJRData[0].TIPO = "";
    objectReporteAgedTrialJRData[0].ORG = "";
    objectReporteAgedTrialJRData[0].RUC_ORG = "";

    return objectReporteAgedTrialJRData;
  }

}
