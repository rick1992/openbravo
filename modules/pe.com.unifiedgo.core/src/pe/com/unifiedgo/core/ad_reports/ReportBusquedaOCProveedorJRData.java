//Sqlc generated V1.O00-1
package pe.com.unifiedgo.core.ad_reports;

import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportBusquedaOCProveedorJRData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportBusquedaOCProveedorJRData.class);
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
  public String organizacion;
  public String line;
  public String mproductline;
  public String productcode;

  public String entrada;
  public String fechafactura;
  public String monedafactura;
  public String totalfactura;
  public String numerofactura;
  public String saldo;
  public String idfactura;
  public String tcFactura;
  public String numeroentrada;

  public String codigoLineaProducto;
  public String lineaProducto;
  public String proyecto;

  public String gastooperativo;

  public String nrorequerimiento;
  public String ordservicio;

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
    else if (fieldName.equalsIgnoreCase("organizacion"))
      return organizacion;
    else if (fieldName.equalsIgnoreCase("line"))
      return line;
    else if (fieldName.equalsIgnoreCase("mproductline"))
      return mproductline;

    else if (fieldName.equalsIgnoreCase("entrada"))
      return entrada;
    else if (fieldName.equalsIgnoreCase("fechafactura"))
      return fechafactura;
    else if (fieldName.equalsIgnoreCase("monedafactura"))
      return monedafactura;
    else if (fieldName.equalsIgnoreCase("totalfactura"))
      return totalfactura;
    else if (fieldName.equalsIgnoreCase("numerofactura"))
      return numerofactura;
    else if (fieldName.equalsIgnoreCase("saldo"))
      return saldo;
    else if (fieldName.equalsIgnoreCase("idfactura"))
      return idfactura;
    else if (fieldName.equalsIgnoreCase("productcode"))
      return productcode;
    else if (fieldName.equalsIgnoreCase("tcFactura"))
      return tcFactura;
    else if (fieldName.equalsIgnoreCase("numeroentrada"))
      return numeroentrada;

    else if (fieldName.equalsIgnoreCase("codigoLineaProducto"))
      return codigoLineaProducto;
    else if (fieldName.equalsIgnoreCase("lineaProducto"))
      return lineaProducto;
    else if (fieldName.equalsIgnoreCase("proyecto"))
      return proyecto;
    else if (fieldName.equalsIgnoreCase("gastooperativo"))
      return gastooperativo;
    else if (fieldName.equalsIgnoreCase("nrorequerimiento"))
      return nrorequerimiento;

    else if (fieldName.equalsIgnoreCase("ordservicio"))
      return ordservicio;

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

  public static ReportBusquedaOCProveedorJRData[] select_trl(VariablesSecureApp vars,
      String adClientId, String adOrgId, String mProductId, String language, String dateFrom,
      String dateTo, String mProductLineID, String strIdProveedor, String strcProjectId)
      throws ServletException {
    return select_trl(vars, adClientId, adOrgId, mProductId, language, dateFrom, dateTo,
        mProductLineID, strIdProveedor, strcProjectId, 0, 0);
  }

  public static ReportBusquedaOCProveedorJRData[] select_trl(VariablesSecureApp vars,
      String adClientId, String adOrgId, String mProductId, String language, String dateFrom,
      String dateTo, String mProductLineID, String strIdProveedor, String strcProjectId,
      int firstRegister, int numberRegisters) throws ServletException {

    String strSql = "";

    strSql = "	SELECT ol.m_product_id, p.name as productname, o.C_Order_ID, o.documentno, o.dateordered, 				"
        + "	dctrl.name AS doctypename, dc.em_sco_specialdoctype as specialdoctype, o.c_bpartner_id, 				"
        + "	bp.taxid || ' - ' || bp.name AS partnername, ol.line, ol.qtyordered, ol.priceactual, 					"
        + "	ol.linenetamt, text(uomtrl.uomsymbol) AS uom, cur.cursymbol AS cursymbol, pterm.name as paymentterm, "
        + "	pmethod.name as paymentmethod, (select org.name from ad_org org where org.ad_org_id=o.ad_org_id) as orgname, "
        + "	(select pp2.value || ' - '|| pp2.description from prdc_productgroup pp2 where pp2.prdc_productgroup_id=p.EM_Prdc_Productgroup_ID) as productline, "
        + "	coalesce(ol.em_swa_received,0) as entrada, 											"
        + "	(ol.qtyordered - coalesce(ol.em_swa_received,0)) as saldo, 							"
        + "	(select ci.c_invoice_id from c_invoiceline cil 										"
        + "			inner join c_invoice ci on cil.c_invoice_id=ci.c_invoice_id 				"
        + "			where ol.c_orderline_id=cil.c_orderline_id limit 1) as idfactura, 			"
        + "	(select coalesce(ci.em_scr_physical_documentno,'000-000000') from c_invoiceline cil "
        + "			inner join c_invoice ci on cil.c_invoice_id=ci.c_invoice_id 				"
        + "			where ol.c_orderline_id=cil.c_orderline_id limit 1) as numerofactura, 		"
        + "	(select ci.em_sco_newDateInvoiced from c_invoiceline cil 						"
        + "			inner join c_invoice ci on cil.c_invoice_id=ci.c_invoice_id 			"
        + "			where ol.c_orderline_id=cil.c_orderline_id limit 1) as fechafactura, 	"
        + "	(select ci.grandtotal from c_invoiceline cil 									"
        + "			inner join c_invoice ci on cil.c_invoice_id=ci.c_invoice_id 			"
        + "			where ol.c_orderline_id=cil.c_orderline_id limit 1) as totalfactura, 	"
        + "	(select cc.cursymbol from c_invoiceline cil 									"
        + "			inner join c_invoice ci on cil.c_invoice_id=ci.c_invoice_id 			"
        + "			inner join c_currency cc on ci.c_currency_id=cc.c_currency_id 			"
        + "			where ol.c_orderline_id=cil.c_orderline_id limit 1) as monedafactura, 	"
        + "	p.value as productcode, 														"
        + "	(select coalesce(mio.documentno,'') from m_inoutline miol 						"
        + "			INNER JOIN m_inout mio ON miol.m_inout_id=mio.m_inout_id 				"
        + "			WHERE ol.c_orderline_id=miol.c_orderline_id LIMIT 1) AS numeroentrada, "
        + "	(0.00) as tc_factura, 		"
        + "	(select coalesce(linpro.VALUE,'') AS name 										"
        + "			from PRDC_PRODUCTGROUP linpro 											"
        + "			WHERE linpro.PRDC_PRODUCTGROUP_ID = p.em_prdc_productgroup_id limit 1) as codigo_linea_producto, "
        + "	(SELECT coalesce( linpro.DESCRIPTION,'') AS NAME 								"
        + "			FROM PRDC_PRODUCTGROUP linpro 											"
        + "			WHERE linpro.PRDC_PRODUCTGROUP_ID = p.em_prdc_productgroup_id limit 1 ) as linea_producto, "
        + "	coalesce(cpro.name,'') as proyecto, "
        + "	case when o.em_spr_isopexpense = 'Y' then 'SI' else 'NO' end as gastooperativo 	"
        + " , coalesce((select mr.documentno from  m_requisitionorder mro "
        + " left join m_requisitionline mrl on mro.m_requisitionline_id=mrl.m_requisitionline_id "
        + " left join m_requisition mr on mrl.m_requisition_id=mr.m_requisition_id where ol.c_orderline_id=mro.c_orderline_id  limit 1	) ,'') as nrorequerimiento,		"
        + ""
        + " case dc.em_sco_specialdoctype when 'SREPURCHASEORDER' then 'NO' when 'SREPURCHASEORDERSERVICE' then 'SI' end as ordservicio 					"

        + "	FROM C_Order o, C_Orderline ol, C_Doctype dc, C_Doctype_Trl dctrl, C_Currency cur, "
        + "          C_Bpartner bp, C_Uom_Trl uomtrl, C_PaymentTerm pterm, Fin_PaymentMethod pmethod, M_Product p, c_project cpro "
        + "    WHERE ol.M_Product_ID = p.m_product_id "
        + "      AND dc.em_sco_specialdoctype IN ('SREPURCHASEORDER','SREPURCHASEORDERSERVICE') "
        + "      AND o.dateordered BETWEEN TO_DATE('" + dateFrom + "', 'DD-MM-YYYY') AND TO_DATE('"
        + dateTo + "', 'DD-MM-YYYY') " + "      AND o.docstatus IN ('CO','CL') ";
    strSql = strSql + ((mProductId == null || mProductId.equals("")) ? ""
        : " AND ol.M_Product_ID = '" + mProductId + "' ");
    strSql = strSql + ((mProductLineID == null || mProductLineID.equals("")) ? ""
        : "                  AND p.EM_Prdc_Productgroup_ID = '" + mProductLineID + "'  ");

    strSql = strSql + ((strcProjectId == null || strcProjectId.equals("") ? ""
        : " and cpro.c_project_id = '" + strcProjectId + "' "));

    strSql = strSql + "      AND dctrl.AD_Language = '" + language + "' "
        + "      AND uomtrl.AD_Language = '" + language + "' "
        + "      AND o.C_Bpartner_ID = bp.C_Bpartner_ID "
        + "      AND o.C_Order_ID = ol.C_Order_ID "
        + "      AND o.C_DoctypeTarget_ID = dc.C_Doctype_ID "
        + "      AND o.fin_paymentmethod_id = pmethod.fin_paymentmethod_id "
        + "      AND o.c_paymentterm_id = pterm.c_paymentterm_id  "
        + "      AND o.c_project_id = cpro.c_project_id " + "		  "

        + "      AND o.C_Currency_ID = cur.C_Currency_ID " + "      AND o.AD_Client_ID = '"
        + adClientId + "' " + "      AND dc.C_Doctype_ID = dctrl.C_Doctype_ID "
        + "      AND ol.C_UOM_ID = uomtrl.C_UOM_ID " + "      AND AD_ISORGINCLUDED(o.AD_Org_ID,'"
        + adOrgId + "','" + adClientId + "') <> -1 ";
    strSql = strSql + ((strIdProveedor == null || "".equals(strIdProveedor)) ? ""
        : "AND o.c_bpartner_id='" + strIdProveedor + "' ");
    strSql = strSql + "    ORDER BY dateordered, o.documentno, ol.line;";
    System.out.println("STRSQL:" + strSql);
    Vector<ReportBusquedaOCProveedorJRData> vector = new Vector<ReportBusquedaOCProveedorJRData>(0);

    SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat dfPrice = Utility.getFormat(vars, "priceInform");
    dfPrice.setRoundingMode(RoundingMode.HALF_UP);
    DecimalFormat dfQty = Utility.getFormat(vars, "qtyExcel");
    dfQty.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      List<Object> results = sqlQuery.list();
      for (int k = 0; k < results.size(); k++) {
        Object[] obj = (Object[]) results.get(k);

        countRecord++;
        ReportBusquedaOCProveedorJRData objReportBusquedaOCProveedorJRData = new ReportBusquedaOCProveedorJRData();

        objReportBusquedaOCProveedorJRData.orgid = adOrgId;
        objReportBusquedaOCProveedorJRData.mproductid = (String) obj[0];
        objReportBusquedaOCProveedorJRData.mproductname = new String(((String) obj[1]).getBytes(),
            "UTF-8");
        objReportBusquedaOCProveedorJRData.corderid = (String) obj[2];
        objReportBusquedaOCProveedorJRData.orderno = (String) obj[3];
        objReportBusquedaOCProveedorJRData.dateordered = (obj[4] != null) ? dfDate.format(obj[4])
            : "--";
        objReportBusquedaOCProveedorJRData.doctypename = (String) obj[5];
        objReportBusquedaOCProveedorJRData.specialdoctype = (String) obj[6];
        objReportBusquedaOCProveedorJRData.cbpartnerid = (String) obj[7];
        objReportBusquedaOCProveedorJRData.partnername = new String(((String) obj[8]).getBytes(),
            "UTF-8");
        objReportBusquedaOCProveedorJRData.line = dfQty.format(obj[9]);
        objReportBusquedaOCProveedorJRData.qtyordered = dfQty.format(obj[10]);
        objReportBusquedaOCProveedorJRData.priceactual = dfPrice.format(obj[11]);
        objReportBusquedaOCProveedorJRData.linenetamt = dfPrice.format(obj[12]);
        objReportBusquedaOCProveedorJRData.uom = (String) obj[13];
        objReportBusquedaOCProveedorJRData.cursymbol = (String) obj[14];
        objReportBusquedaOCProveedorJRData.paymentterm = (String) obj[15];
        objReportBusquedaOCProveedorJRData.paymentmethod = (String) obj[16];
        objReportBusquedaOCProveedorJRData.organizacion = (String) obj[17];
        objReportBusquedaOCProveedorJRData.mproductline = obj[18] != null
            ? new String(((String) obj[18]).getBytes(), "UTF-8") : "";
        objReportBusquedaOCProveedorJRData.entrada = dfQty.format(obj[19]);
        objReportBusquedaOCProveedorJRData.saldo = dfQty.format(obj[20]);
        objReportBusquedaOCProveedorJRData.idfactura = (obj[21] != null) ? (String) obj[21] : "--";
        objReportBusquedaOCProveedorJRData.numerofactura = (obj[22] != null) ? (String) obj[22]
            : "--";
        objReportBusquedaOCProveedorJRData.fechafactura = (obj[23] != null) ? dfDate.format(obj[23])
            : "--";
        objReportBusquedaOCProveedorJRData.totalfactura = (obj[24] != null)
            ? dfPrice.format(obj[24]) : "--";
        objReportBusquedaOCProveedorJRData.monedafactura = (obj[25] != null) ? (String) obj[25]
            : "--";
        objReportBusquedaOCProveedorJRData.productcode = (obj[26] != null) ? (String) obj[26]
            : "--";
        objReportBusquedaOCProveedorJRData.numeroentrada = (obj[27] != null) ? (String) obj[27]
            : "--";
        objReportBusquedaOCProveedorJRData.tcFactura = (obj[28] != null) ? String.valueOf(obj[28])
            : "";
        objReportBusquedaOCProveedorJRData.codigoLineaProducto = (obj[29] != null)
            ? (String) obj[29] : "";
        objReportBusquedaOCProveedorJRData.lineaProducto = (obj[30] != null) ? (String) obj[30]
            : "";
        objReportBusquedaOCProveedorJRData.proyecto = (obj[31] != null) ? (String) obj[31] : "";

        objReportBusquedaOCProveedorJRData.gastooperativo = (obj[32] != null) ? (String) obj[32]
            : "";
        objReportBusquedaOCProveedorJRData.nrorequerimiento = (obj[33] != null) ? (String) obj[33]
            : "";

        objReportBusquedaOCProveedorJRData.ordservicio = (obj[34] != null) ? (String) obj[34] : "";

        objReportBusquedaOCProveedorJRData.rownum = Long.toString(countRecord);
        vector.addElement(objReportBusquedaOCProveedorJRData);
      }

    } catch (Exception ex) {

      log4j.error("FILA: " + countRecord);
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    }

    ReportBusquedaOCProveedorJRData objReportBusquedaOCProveedorJRData[] = new ReportBusquedaOCProveedorJRData[vector
        .size()];
    vector.copyInto(objReportBusquedaOCProveedorJRData);

    return (objReportBusquedaOCProveedorJRData);
  }

  public static ReportBusquedaOCProveedorJRData[] set() throws ServletException {
    ReportBusquedaOCProveedorJRData objReportBusquedaOCProveedorJRData[] = new ReportBusquedaOCProveedorJRData[1];
    objReportBusquedaOCProveedorJRData[0] = new ReportBusquedaOCProveedorJRData();
    objReportBusquedaOCProveedorJRData[0].orgid = "";
    objReportBusquedaOCProveedorJRData[0].mproductid = "";
    objReportBusquedaOCProveedorJRData[0].mproductname = "";
    objReportBusquedaOCProveedorJRData[0].corderid = "";
    objReportBusquedaOCProveedorJRData[0].orderno = "";
    objReportBusquedaOCProveedorJRData[0].dateordered = "";
    objReportBusquedaOCProveedorJRData[0].doctypename = "";
    objReportBusquedaOCProveedorJRData[0].specialdoctype = "";
    objReportBusquedaOCProveedorJRData[0].cbpartnerid = "";
    objReportBusquedaOCProveedorJRData[0].partnername = "";
    objReportBusquedaOCProveedorJRData[0].line = "";
    objReportBusquedaOCProveedorJRData[0].qtyordered = "";
    objReportBusquedaOCProveedorJRData[0].priceactual = "";
    objReportBusquedaOCProveedorJRData[0].linenetamt = "";
    objReportBusquedaOCProveedorJRData[0].uom = "";
    objReportBusquedaOCProveedorJRData[0].cursymbol = "";
    objReportBusquedaOCProveedorJRData[0].paymentterm = "";
    objReportBusquedaOCProveedorJRData[0].paymentmethod = "";

    objReportBusquedaOCProveedorJRData[0].entrada = "";
    objReportBusquedaOCProveedorJRData[0].saldo = "";
    objReportBusquedaOCProveedorJRData[0].idfactura = "";
    objReportBusquedaOCProveedorJRData[0].fechafactura = "";
    objReportBusquedaOCProveedorJRData[0].monedafactura = "";
    objReportBusquedaOCProveedorJRData[0].productcode = "";
    objReportBusquedaOCProveedorJRData[0].totalfactura = "";
    objReportBusquedaOCProveedorJRData[0].numerofactura = "";
    objReportBusquedaOCProveedorJRData[0].gastooperativo = "";
    objReportBusquedaOCProveedorJRData[0].nrorequerimiento = "";

    return objReportBusquedaOCProveedorJRData;
  }

  public static String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.NAME" + "      FROM C_BPARTNER"
        + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

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
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT M_PRODUCT.NAME" + "      FROM M_PRODUCT"
        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

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
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  // para el proveedor

  public static String selectCBpartner(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.taxid || ' - ' || c_bpartner.NAME as name"
        + "      FROM c_bpartner" + "      WHERE c_bpartner.c_bpartner_id = ?";

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
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static String selectPrdcProductgroup(ConnectionProvider connectionProvider,
      String mProductId) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "      SELECT PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION AS NAME"
        + "      FROM PRDC_PRODUCTGROUP" + "      WHERE PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID = ?";

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
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static ReportBusquedaOCProveedorJRData[] selectProductLineDouble(
      ConnectionProvider connectionProvider, String adUserClient) throws ServletException {
    return selectProductLineDouble(connectionProvider, adUserClient, 0, 0);
  }

  public static ReportBusquedaOCProveedorJRData[] selectProductLineDouble(
      ConnectionProvider connectionProvider, String adUserClient, int firstRegister,
      int numberRegisters) throws ServletException {
    String strSql = "";
    strSql = strSql
        + "       SELECT ORG.AD_ORG_ID AS PADRE, PRDC_PRODUCTGROUP.PRDC_PRODUCTGROUP_ID AS ID, TO_CHAR(PRDC_PRODUCTGROUP.VALUE || ' - ' || PRDC_PRODUCTGROUP.DESCRIPTION) AS NAME"
        + "         FROM PRDC_PRODUCTGROUP, AD_ORG ORG " + "        WHERE 1=1"
        + "          AND PRDC_PRODUCTGROUP.ISACTIVE='Y'"
        + "          AND PRDC_PRODUCTGROUP.AD_Client_ID IN(";
    strSql = strSql + ((adUserClient == null || adUserClient.equals("")) ? "" : adUserClient);
    strSql = strSql
        + ")         AND ad_isorgincluded(ORG.AD_ORG_ID,PRDC_PRODUCTGROUP.AD_ORG_ID,PRDC_PRODUCTGROUP.AD_Client_ID)<>-1"
        + "        ORDER BY PADRE, NAME";

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
        ReportBusquedaOCProveedorJRData objRankingProductosJRData = new ReportBusquedaOCProveedorJRData();
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
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
    ReportBusquedaOCProveedorJRData objRankingProductosJRData[] = new ReportBusquedaOCProveedorJRData[vector
        .size()];
    vector.copyInto(objRankingProductosJRData);
    return (objRankingProductosJRData);
  }

  public static String selectRucOrg(ConnectionProvider connectionProvider, String adUserOrg)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "        SELECT a.taxid ruc " + "        FROM ad_orginfo a        "
        + "        WHERE a.ad_org_id = ?";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, adUserOrg);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "ruc");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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

  public static String selectOrg(ConnectionProvider connectionProvider, String orgId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "        SELECT NAME" + "        FROM AD_ORG" + "        WHERE AD_ORG_ID = ?";

    ResultSet result;
    String strReturn = "0";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, orgId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
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
