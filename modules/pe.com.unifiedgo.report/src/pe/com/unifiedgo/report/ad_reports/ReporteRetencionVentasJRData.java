//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

class ReporteRetencionVentasJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReporteRetencionVentasJRData.class);
  private String InitRecordNumber = "0";
  public String RETENCION_ID;
  public String TERCERO_RUC;
  public String NRO_RETENCION;
  public String FECHA;
  public String NRO_FACTURA;
  public BigDecimal MONTO;
  public String ORG;
  public String ORG_RUC;
  public String TIPO_DOC;
  public String FECHA_DOC;
  public BigDecimal IMPORTE_DOC;

  public String OFICINA;
  public String PLANILLA_PAGO;
  public String NRO_DOC_RETENCION;
  
  public BigDecimal TOTAL_FISICO;

  public String TERCERO;

  
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("RETENCION_ID"))
      return RETENCION_ID;
    else if (fieldName.equalsIgnoreCase("TERCERO_RUC"))
        return TERCERO_RUC;
    else if (fieldName.equalsIgnoreCase("NRO_RETENCION"))
      return NRO_RETENCION;
    else if (fieldName.equalsIgnoreCase("FECHA"))
      return FECHA;
    else if (fieldName.equalsIgnoreCase("NRO_FACTURA"))
      return NRO_FACTURA;
    else if (fieldName.equalsIgnoreCase("MONTO"))
      return MONTO.toString();
    else if (fieldName.equalsIgnoreCase("ORG"))
      return ORG;
    else if (fieldName.equalsIgnoreCase("ORG_RUC"))
      return ORG_RUC;
    else if (fieldName.equalsIgnoreCase("TIPO_DOC"))
        return TIPO_DOC;
    else if (fieldName.equalsIgnoreCase("FECHA_DOC"))
        return FECHA_DOC;
    else if (fieldName.equalsIgnoreCase("IMPORTE_DOC"))
        return IMPORTE_DOC.toString();
    else if (fieldName.equalsIgnoreCase("OFICINA"))
        return OFICINA;
    else if (fieldName.equalsIgnoreCase("PLANILLA_PAGO"))
        return PLANILLA_PAGO;
    else if (fieldName.equalsIgnoreCase("NRO_DOC_RETENCION"))
        return NRO_DOC_RETENCION;
    else if (fieldName.equalsIgnoreCase("TOTAL_FISICO"))
            return TOTAL_FISICO.toString();
    else if (fieldName.equalsIgnoreCase("TERCERO"))
            return TERCERO;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReporteRetencionVentasJRData[] select(String adClientId, String startDate,
      String endDate, String adOrgId) throws ServletException {
    return select(adClientId, startDate, endDate, adOrgId, 0, 0);
  }

  public static ReporteRetencionVentasJRData[] select(String adClientId, String startDate,
      String endDate, String adOrgId, int firstRegister, int numberRegisters)
      throws ServletException {

    String strSql = "";
    strSql = 

    		"	select wre.sco_swithholding_receipt_id as RETENCION_ID, "
    				+ " cbp.taxid as TERCERO_RUC, "

    				+ " coalesce(wre.documentno,'') as NRO_RETENCION, "
    				+ " to_char(wre.dategen) as FECHA, "
    				+ " coalesce(inv.em_scr_physical_documentno,coalesce(inv.documentno,'')) as NRO_FACTURA, "
    				+ " lin.convertedamount as MONTO, "
    				+ " org.name as ORG, "
    				+ " inf.taxid as ORG_RUC , "
    				+ "	coalesce(NULL,'Factura') as TIPO_DOC, "

    				+ "		to_char(inv.dateacct) AS FECHA_DOC, "

    				+ "		inv.grandtotal AS IMPORTE_DOC, "

    				+ "	cl.city as OFICINA, "

    				+ " coalesce(NULL,'') as PLANILLA_PAGO "
    				
    				
    				+ " ,wre.withholdingnumber as NRO_DOC_RETENCION"
    				
    				+", wre.totalamount_physicaldoc as TOTAL_FISICO "
    				+ ", cbp.name as TERCERO "

    				 
    				
    				+ " from sco_swithholding_receipt wre "
    				+ " join sco_swithho_rec_line lin on wre.sco_swithholding_receipt_id = lin.sco_swithholding_receipt_id "
    				+ " left join c_invoice inv on lin.invoiceref_id = inv.c_invoice_id "
    				+ " left join c_bpartner cbp on cbp.c_bpartner_id=inv.c_bpartner_id, "
    				+ " ad_org org join ad_orgtype typ using (ad_orgtype_id) "
    				+ " join ad_orginfo inf on org.ad_org_id = inf.ad_org_id "
    				+ "	left join c_location cl on inf.c_location_id= cl.c_location_id "

        
        + "    where AD_ISORGINCLUDED(wre.ad_org_id, org.ad_org_id, wre.ad_client_id)<>-1 "
        + "      and (typ.IsLegalEntity='Y' or typ.IsAcctLegalEntity='Y') "
        + "      and wre.dateacct between TO_DATE('" + startDate + "', 'DD-MM-YYYY') and TO_DATE('" + endDate + "', 'DD-MM-YYYY') " + "      and wre.docstatus = 'CO' "
        + "      and wre.isactive = 'Y' " + "      and org.ad_org_id in ('" + adOrgId + "') order by wre.withholdingnumber,cbp.taxid,wre.sco_swithholding_receipt_id,wre.documentno";

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    long countRecord = 0;

    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();

      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);
        countRecord++;
        ReporteRetencionVentasJRData objectReporteRetencionVentasJRData = new ReporteRetencionVentasJRData();
        objectReporteRetencionVentasJRData.RETENCION_ID = (String) obj[0];
        objectReporteRetencionVentasJRData.TERCERO_RUC = (String) obj[1];
        objectReporteRetencionVentasJRData.NRO_RETENCION = (String) obj[2];
        objectReporteRetencionVentasJRData.FECHA = (String) obj[3];
        objectReporteRetencionVentasJRData.NRO_FACTURA = (String) obj[4];

        objectReporteRetencionVentasJRData.MONTO = ((BigDecimal) (obj[5])).setScale(3,
            BigDecimal.ROUND_HALF_UP);
        objectReporteRetencionVentasJRData.ORG = (String) obj[6];
        objectReporteRetencionVentasJRData.ORG_RUC = (String) obj[7];
        objectReporteRetencionVentasJRData.TIPO_DOC = (String) obj[8];
        objectReporteRetencionVentasJRData.FECHA_DOC = (String) obj[9];
        objectReporteRetencionVentasJRData.IMPORTE_DOC = ((BigDecimal) (obj[10])).setScale(3,
                BigDecimal.ROUND_HALF_UP);
                
        objectReporteRetencionVentasJRData.OFICINA = (String) obj[11];   
        objectReporteRetencionVentasJRData.PLANILLA_PAGO = (String) obj[12];
        objectReporteRetencionVentasJRData.NRO_DOC_RETENCION = (String) obj[13];
        objectReporteRetencionVentasJRData.TOTAL_FISICO = ((BigDecimal) (obj[14])).setScale(3,
                BigDecimal.ROUND_HALF_UP);
        objectReporteRetencionVentasJRData.TERCERO = (String) obj[15];


        objectReporteRetencionVentasJRData.rownum = Long.toString(countRecord);
        objectReporteRetencionVentasJRData.InitRecordNumber = Integer.toString(firstRegister);

        vector.addElement(objectReporteRetencionVentasJRData);

      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReporteRetencionVentasJRData objectReporteRetencionVentasJRData[] = new ReporteRetencionVentasJRData[vector
        .size()];
    vector.copyInto(objectReporteRetencionVentasJRData);

    return (objectReporteRetencionVentasJRData);
  }

  public static ReporteRetencionVentasJRData[] set() throws ServletException {
    ReporteRetencionVentasJRData objectReporteRetencionVentasJRData[] = new ReporteRetencionVentasJRData[1];
    objectReporteRetencionVentasJRData[0] = new ReporteRetencionVentasJRData();
    objectReporteRetencionVentasJRData[0].RETENCION_ID = "";
    objectReporteRetencionVentasJRData[0].NRO_RETENCION = "";
    objectReporteRetencionVentasJRData[0].FECHA = "";
    objectReporteRetencionVentasJRData[0].NRO_FACTURA = "";
    objectReporteRetencionVentasJRData[0].MONTO = new BigDecimal(0);
    objectReporteRetencionVentasJRData[0].ORG = "";
    objectReporteRetencionVentasJRData[0].ORG_RUC = "";

    return objectReporteRetencionVentasJRData;
  }

}
