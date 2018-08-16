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
import org.openbravo.model.common.plm.Product;

class FormSalesOrderDetailsJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(FormSalesOrderDetailsJRData.class);
  private String InitRecordNumber = "0";
  public String mproductid;
  public String productname;
  public String documentno;
  public String documentid;
  public String dateordered;
  public String orderedqty;
  public String listprice;
  public String discount;
  public String unitprice;
  public String linenetamt;
  public String currency;
  public String orgname;
  public String invref;
  public String ioref;

  public String bpname;
  public String salesrepname;

  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("mproductid"))
      return mproductid;
    if (fieldName.equalsIgnoreCase("productname"))
      return productname;
    else if (fieldName.equalsIgnoreCase("documentno"))
      return documentno;
    else if (fieldName.equalsIgnoreCase("documentid"))
      return documentid;
    else if (fieldName.equalsIgnoreCase("dateordered"))
      return dateordered;
    else if (fieldName.equalsIgnoreCase("orderedqty"))
      return orderedqty;
    else if (fieldName.equalsIgnoreCase("listprice"))
      return listprice;
    else if (fieldName.equalsIgnoreCase("discount"))
      return discount;
    else if (fieldName.equalsIgnoreCase("unitprice"))
      return unitprice;
    else if (fieldName.equalsIgnoreCase("linenetamt"))
      return linenetamt;
    else if (fieldName.equalsIgnoreCase("currency"))
      return currency;
    else if (fieldName.equalsIgnoreCase("orgname"))
      return orgname;
    else if (fieldName.equalsIgnoreCase("invref"))
      return invref;
    else if (fieldName.equalsIgnoreCase("ioref"))
      return ioref;
    else if (fieldName.equalsIgnoreCase("bpname"))
      return bpname;
    else if (fieldName.equalsIgnoreCase("salesrepname"))
      return salesrepname;
    else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static Vector<SalesOrderData> getData(String adOrgId, String adClientId, String adUserId,
      String mProductId, String strC_BPartner_ID, String strCurrencyId, String startDate,
      String endDate, String strSalesRep_ID) throws ServletException {
    Vector<SalesOrderData> so_vector = new Vector<SalesOrderData>(0);
    int BigDecimalScale = 5;

    String strSql = "";
    strSql = "SELECT i.c_invoice_id, i.em_scr_physical_documentno, i.dateacct, il.qtyinvoiced, il.pricelist, "
        + "          (case when abs(il.pricelist) = 0 then 0 else round(100*(1-il.priceactual/il.pricelist), 2) end) as discount, "
        + "          il.priceactual, il.linenetamt, "
        + "          (select text(cursymbol) from c_currency where c_currency.c_currency_id=i.c_currency_id) as cursymbol, "
        + "          (select name from ad_org where ad_org.ad_org_id=i.ad_org_id) as orgname, "
        + "          (select o.documentno from c_order o, c_orderline ol where o.c_order_id=ol.c_order_id and ol.c_orderline_id=il.c_orderline_id) as soref, "
        + "          (select io.em_scr_physical_documentno from m_inout io, m_inoutline iol where io.m_inout_id=iol.m_inout_id and iol.c_orderline_id=il.c_orderline_id and io.docstatus='CO' limit 1) as ioref,"
        + "          (select coalesce(bp.name,'--') from c_bpartner bp where bp.c_bpartner_id=i.c_bpartner_id) as bpname, "
        + "          (select coalesce(u.name,'--') from ad_user u where u.ad_user_id=i.salesrep_id) as salesrep "
        + "     FROM c_invoice i, c_invoiceline il"
        + "    WHERE i.c_invoice_id=il.c_invoice_id "
        + "      AND il.m_product_id = '"
        + mProductId
        + "'     AND (i.em_sco_specialdoctype='SCOARINVOICE' OR i.em_sco_specialdoctype='SCOARTICKET')";
    strSql = strSql
        + ((strC_BPartner_ID == null || "".equals(strC_BPartner_ID)) ? ""
            : "  AND i.C_BPartner_ID = '" + strC_BPartner_ID + "'  ");
    strSql = strSql
        + ((strSalesRep_ID != null && !"".equals(strSalesRep_ID) && !"--".equals(strSalesRep_ID)) ? "      AND i.salesrep_id = '"
            + strSalesRep_ID + "'  "
            : "");
    strSql = strSql
        + "      AND i.issotrx='Y' AND i.docstatus='CO'"
        + "      AND i.c_currency_id='"
        + strCurrencyId
        + "' "
        + "      AND i.dateacct BETWEEN TO_DATE('"
        + startDate
        + "', 'DD-MM-YYYY') and TO_DATE('"
        + endDate
        + "', 'DD-MM-YYYY')"
        + "      AND AD_ISORGINCLUDED(i.ad_org_id,'"
        + adOrgId
        + "', '"
        + adClientId
        + "') <> -1";
    System.out.println("strSql1:" + strSql);
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> data = sqlQuery.list();
      Product p;
      for (int k = 0; k < data.size(); k++) {
        Object[] obj = (Object[]) data.get(k);

        SalesOrderData soData = new SalesOrderData();
        soData.mproductid = mProductId;

        p = OBDal.getInstance().get(Product.class, soData.mproductid);
        soData.productname = (p != null) ? p.getSearchKey() + " - " + p.getName() : "--";
        soData.documentid = (String) obj[0];
        soData.documentno = (String) obj[1];
        soData.dateordered = (Date) obj[2];
        soData.orderedqty = ((BigDecimal) obj[3]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        soData.listprice = ((BigDecimal) obj[4]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        soData.discount = ((BigDecimal) obj[5]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        soData.unitprice = ((BigDecimal) obj[6]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        soData.linenetamt = ((BigDecimal) obj[7]).setScale(BigDecimalScale, RoundingMode.HALF_UP);
        soData.currency = (String) obj[8];
        soData.orgname = (String) obj[9];
        soData.invref = (String) obj[10];
        soData.ioref = (String) obj[11];
        soData.bpname = (String) obj[12];
        soData.salesrepname = (String) obj[13];

        so_vector.add(soData);
      }
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex.getMessage());
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    return so_vector;
  }

  public static FormSalesOrderDetailsJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String adUserId, String mProductId, String strC_BPartner_ID,
      String strCurrencyId, String dateFrom, String dateTo, String strSalesRep_ID)
      throws ServletException {
    return select(vars, adOrgId, adClientId, adUserId, mProductId, strC_BPartner_ID, strCurrencyId,
        dateFrom, dateTo, strSalesRep_ID, 0, 0);
  }

  public static FormSalesOrderDetailsJRData[] select(VariablesSecureApp vars, String adOrgId,
      String adClientId, String adUserId, String mProductId, String strC_BPartner_ID,
      String strCurrencyId, String dateFrom, String dateTo, String strSalesRep_ID,
      int firstRegister, int numberRegisters) throws ServletException {
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);
    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);
    long countRecord = 0;

    Vector<SalesOrderData> data = getData(adOrgId, adClientId, adUserId, mProductId,
        strC_BPartner_ID, strCurrencyId, dateFrom, dateTo, strSalesRep_ID);
    for (int k = 0; k < data.size(); k++) {
      countRecord++;

      FormSalesOrderDetailsJRData objSODetail = new FormSalesOrderDetailsJRData();
      objSODetail.mproductid = data.get(k).mproductid;
      objSODetail.productname = data.get(k).productname;
      objSODetail.documentno = data.get(k).documentno;
      objSODetail.documentid = data.get(k).documentid;
      objSODetail.dateordered = (data.get(k).dateordered != null) ? dfDate
          .format(data.get(k).dateordered) : "--";
      objSODetail.orderedqty = dfQty.format(data.get(k).orderedqty);
      objSODetail.listprice = dfPrice.format(data.get(k).listprice);
      objSODetail.discount = dfQty.format(data.get(k).discount);
      objSODetail.unitprice = dfPrice.format(data.get(k).unitprice);
      objSODetail.linenetamt = dfPrice.format(data.get(k).linenetamt);
      objSODetail.currency = data.get(k).currency;
      objSODetail.orgname = data.get(k).orgname;
      objSODetail.invref = (data.get(k).invref != null) ? data.get(k).invref : "--";
      objSODetail.ioref = (data.get(k).ioref != null) ? data.get(k).ioref : "--";

      objSODetail.bpname = (data.get(k).bpname != null) ? data.get(k).bpname : "--";
      objSODetail.salesrepname = (data.get(k).salesrepname != null) ? data.get(k).salesrepname
          : "--";

      objSODetail.rownum = Long.toString(countRecord);
      objSODetail.InitRecordNumber = Integer.toString(firstRegister);

      vector.addElement(objSODetail);
    }

    FormSalesOrderDetailsJRData objSODetail[] = new FormSalesOrderDetailsJRData[vector.size()];
    vector.copyInto(objSODetail);

    return (objSODetail);
  }

  public static FormSalesOrderDetailsJRData[] set() throws ServletException {
    FormSalesOrderDetailsJRData objSODetail[] = new FormSalesOrderDetailsJRData[1];
    objSODetail[0] = new FormSalesOrderDetailsJRData();
    objSODetail[0].mproductid = "";
    objSODetail[0].documentid = "";
    objSODetail[0].documentno = "";
    objSODetail[0].dateordered = null;
    objSODetail[0].orderedqty = "0";
    objSODetail[0].listprice = "0";
    objSODetail[0].discount = "0";
    objSODetail[0].unitprice = "0";
    objSODetail[0].linenetamt = "0";
    objSODetail[0].currency = "--";
    objSODetail[0].orgname = "--";
    objSODetail[0].invref = "--";
    objSODetail[0].ioref = "--";
    return objSODetail;
  }
}

class SalesOrderData {
  public String mproductid;
  public String productname;
  public String documentno;
  public String documentid;
  public Date dateordered;
  public BigDecimal orderedqty;
  public BigDecimal listprice;
  public BigDecimal discount;
  public BigDecimal unitprice;
  public BigDecimal linenetamt;
  public String currency;
  public String orgname;
  public String invref;
  public String ioref;

  public String bpname;
  public String salesrepname;

  public SalesOrderData() {
    productname = "--";
    documentid = "";
    documentno = "--";
    dateordered = null;
    orderedqty = BigDecimal.ZERO;
    listprice = BigDecimal.ZERO;
    discount = BigDecimal.ZERO;
    unitprice = BigDecimal.ZERO;
    linenetamt = BigDecimal.ZERO;
    currency = "--";
    orgname = "--";
    invref = "--";
    ioref = "--";

    bpname = "--";
    salesrepname = "--";
  }
}
