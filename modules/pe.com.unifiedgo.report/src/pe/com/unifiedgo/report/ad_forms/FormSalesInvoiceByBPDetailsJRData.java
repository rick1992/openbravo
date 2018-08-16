//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.Utility;

class FormSalesInvoiceByBPDetailsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormSalesInvoiceByBPDetailsJRData.class);
  private String InitRecordNumber = "0";
  public String documentno;
  public String documentid;
  public String dateordered;
  public String grandtotal;
  public String currency;
  public String bpname;
  public String invref;
  public String ioref;
  public String doctype;
  public String orgid;
  public String vendedor;

  public String codcliente;
  public String descliente;
  public String organizacion;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("documentno"))
      return documentno;
    else if (fieldName.equalsIgnoreCase("documentid"))
      return documentid;
    else if (fieldName.equalsIgnoreCase("dateordered"))
      return dateordered;
    else if (fieldName.equalsIgnoreCase("grandtotal"))
      return grandtotal;
    else if (fieldName.equalsIgnoreCase("currency"))
      return currency;
    else if (fieldName.equalsIgnoreCase("bpname"))
      return bpname;
    else if (fieldName.equalsIgnoreCase("invref"))
      return invref;
    else if (fieldName.equalsIgnoreCase("ioref"))
      return ioref;
    else if (fieldName.equalsIgnoreCase("doctype"))
      return doctype;
    else if (fieldName.equalsIgnoreCase("orgid"))
      return orgid;
    else if (fieldName.equalsIgnoreCase("vendedor"))
      return vendedor;

    else if (fieldName.equalsIgnoreCase("codcliente"))
      return codcliente;
    else if (fieldName.equalsIgnoreCase("descliente"))
      return descliente;
    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;

    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static Vector<SalesInvoiceByBPData> getData(String adOrgId, String adClientId,
      String adUserId, String strSalesRepID, String startDate, String endDate)
      throws ServletException {
    Vector<SalesInvoiceByBPData> so_vector = new Vector<SalesInvoiceByBPData>(0);
    int BigDecimalScale = 5;

    String strSql = "";
    strSql = "SELECT i.c_invoice_id, i.em_scr_physical_documentno, i.dateacct, "
        + "          sum(CASE WHEN i.em_sco_specialdoctype='SCOARCREDITMEMO' THEN -il.linenetamt ELSE il.linenetamt END) as grandtotal, "
        + "          dttrl.name as doctype, "
        + "          (select text(cursymbol) from c_currency where c_currency.c_currency_id=i.c_currency_id) as cursymbol, "
        + "          (select (taxid ||' - '||name) from c_bpartner where c_bpartner.c_bpartner_id=i.c_bpartner_id) as bpname, "
        + "          coalesce((select o.documentno from c_order o where o.c_order_id=i.c_order_id),'--') as soref, "
        + "          coalesce((select io.em_scr_physical_documentno from m_inout io, m_inoutline iol where io.m_inout_id=iol.m_inout_id and iol.m_inoutline_id=(select il.m_inoutline_id from c_invoiceline il where il.c_invoice_id=i.c_invoice_id limit 1) and io.docstatus='CO'),'--') as ioref, "
        + "          i.ad_org_id"
        + "         ,(select u.name from ad_user u where ad_user_id=i.salesrep_id) as vendedor"
        + "          ,(select (taxid ) from c_bpartner where c_bpartner.c_bpartner_id=i.c_bpartner_id) as codcliente "
        + "          ,(select (name ) from c_bpartner where c_bpartner.c_bpartner_id=i.c_bpartner_id) as descliente "
        + "			 , (select ao.name from ad_org ao where ao.ad_org_id=i.ad_org_id ) as organizacion"

        + "     FROM c_invoice i "
        + "          INNER JOIN c_invoiceline il ON i.c_invoice_id=il.c_invoice_id"
        + "          INNER JOIN c_doctype_trl dttrl ON i.c_doctypetarget_id=dttrl.c_doctype_id "
        + "    WHERE i.em_sco_specialdoctype in ('SCOARINVOICE','SCOARTICKET','SCOARCREDITMEMO','SCOARINVOICERETURNMAT')";
    strSql = strSql
        + ((strSalesRepID != null && !"".equals(strSalesRepID) && !"--".equals(strSalesRepID)) ? "      AND i.salesrep_id = '"
            + strSalesRepID + "'  "
            : " AND (i.salesrep_id IS NULL)");
    strSql = strSql
        + "      AND i.issotrx='Y' AND il.financial_invoice_line='N' AND i.docstatus='CO'"
        + "      AND i.em_sco_isforfree='N' "
        + "      AND TRUNC(i.dateacct) BETWEEN TO_DATE('"
        + startDate
        + "', 'DD-MM-YYYY') and TO_DATE('"
        + endDate
        + "', 'DD-MM-YYYY')"
        + "      AND AD_ISORGINCLUDED(i.ad_org_id,'"
        + adOrgId
        + "', '"
        + adClientId
        + "') <> -1"
        + "     GROUP BY i.c_invoice_id, i.em_scr_physical_documentno, i.dateacct, i.poreference, dttrl.name, i.c_currency_id, i.c_bpartner_id, i.c_order_id, i.ad_org_id, i.salesrep_id"
        + "     ORDER BY i.em_scr_physical_documentno ";
    System.out.println("strSql1:" + strSql);
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        SalesInvoiceByBPData soData = new SalesInvoiceByBPData();

        soData.documentid = (String) obj[0];
        System.out.println("soData.documentid:" + soData.documentid);
        soData.documentno = (String) obj[1];
        System.out.println("soData.invref:" + soData.invref);
        soData.dateordered = (Date) obj[2];
        System.out.println("soData.documentno:" + soData.documentno);
        soData.grandtotal = ((BigDecimal) obj[3]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        System.out.println("soData.grandtotal:" + soData.grandtotal);
        soData.doctype = (String) obj[4];
        System.out.println("soData.doctype:" + soData.doctype);
        soData.currency = (String) obj[5];
        System.out.println("soData.currency:" + soData.currency);
        soData.bpname = (String) obj[6];
        System.out.println("soData.bpname:" + soData.bpname);
        soData.invref = (String) obj[7];
        System.out.println("soData.invref:" + soData.invref);
        soData.ioref = (String) obj[8];
        System.out.println("soData.ioref:" + soData.ioref);
        soData.orgid = (String) obj[9];
        System.out.println("soData.orgid:" + soData.orgid);
        soData.vendedor = (String) obj[10];
        soData.codcliente = (String) obj[11];
        soData.descliente = (String) obj[12];
        soData.organizacion = (String) obj[13];

        so_vector.add(soData);
      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex.getMessage());
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    return so_vector;
  }

  public static FormSalesInvoiceByBPDetailsJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String adUserId, String strSalesRepID, String dateFrom, String dateTo)
      throws ServletException {
    return select(vars, adOrgId, adClientId, adUserId, strSalesRepID, dateFrom, dateTo, 0, 0);
  }

  public static FormSalesInvoiceByBPDetailsJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String adUserId, String strSalesRepID, String dateFrom, String dateTo,
      int firstRegister, int numberRegisters) throws ServletException {
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);
    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);
    long countRecord = 0;

    Vector<SalesInvoiceByBPData> data = getData(adOrgId, adClientId, adUserId, strSalesRepID,
        dateFrom, dateTo);
    for (int k = 0; k < data.size(); k++) {
      countRecord++;

      FormSalesInvoiceByBPDetailsJRData objSODetail = new FormSalesInvoiceByBPDetailsJRData();

      objSODetail.documentno = data.get(k).documentno;
      System.out.println("objSODetail.documentno:" + objSODetail.documentno);
      objSODetail.documentid = data.get(k).documentid;
      System.out.println("objSODetail.documentid:" + objSODetail.documentid);
      objSODetail.dateordered = (data.get(k).dateordered != null) ? dfDate
          .format(data.get(k).dateordered) : "--";
      System.out.println("objSODetail.dateordered:" + objSODetail.dateordered);
      objSODetail.grandtotal = dfPrice.format(data.get(k).grandtotal);
      System.out.println("objSODetail.grandtotal:" + objSODetail.grandtotal);
      objSODetail.currency = data.get(k).currency;
      System.out.println("objSODetail.currency:" + objSODetail.currency);
      objSODetail.bpname = data.get(k).bpname;
      System.out.println("objSODetail.bpname:" + objSODetail.bpname);
      objSODetail.invref = (data.get(k).invref != null) ? data.get(k).invref : "--";
      objSODetail.ioref = (data.get(k).ioref != null) ? data.get(k).ioref : "--";
      objSODetail.doctype = (data.get(k).doctype != null) ? data.get(k).doctype : "--";
      objSODetail.orgid = data.get(k).orgid;
      objSODetail.vendedor = data.get(k).vendedor;
      objSODetail.codcliente = data.get(k).codcliente;
      objSODetail.descliente = data.get(k).descliente;
      objSODetail.organizacion = data.get(k).organizacion;

      objSODetail.rownum = Long.toString(countRecord);
      objSODetail.InitRecordNumber = Integer.toString(firstRegister);

      vector.addElement(objSODetail);
    }

    FormSalesInvoiceByBPDetailsJRData objSODetail[] = new FormSalesInvoiceByBPDetailsJRData[vector
        .size()];
    vector.copyInto(objSODetail);

    return (objSODetail);
  }

  public static FormSalesInvoiceByBPDetailsJRData[] set() throws ServletException {
    FormSalesInvoiceByBPDetailsJRData objSODetail[] = new FormSalesInvoiceByBPDetailsJRData[1];
    objSODetail[0] = new FormSalesInvoiceByBPDetailsJRData();
    objSODetail[0].documentid = "";
    objSODetail[0].documentno = "";
    objSODetail[0].dateordered = null;
    objSODetail[0].grandtotal = "0";
    objSODetail[0].currency = "--";
    objSODetail[0].bpname = "--";
    objSODetail[0].invref = "--";
    objSODetail[0].ioref = "--";
    objSODetail[0].vendedor = "--";

    return objSODetail;
  }
}

class SalesInvoiceByBPData {
  public String documentno;
  public String documentid;
  public Date dateordered;
  public BigDecimal grandtotal;
  public String currency;
  public String bpname;
  public String invref;
  public String ioref;
  public String doctype;
  public String orgid;
  public String vendedor;
  public String codcliente;
  public String descliente;
  public String organizacion;

  public SalesInvoiceByBPData() {
    documentid = "";
    documentno = "--";
    dateordered = null;
    grandtotal = BigDecimal.ZERO;
    currency = "--";
    bpname = "--";
    invref = "--";
    ioref = "--";
    doctype = "--";
    orgid = "--";
    vendedor = "--";
    codcliente = "--";
    descliente = "--";
    organizacion = "--";
  }
}
