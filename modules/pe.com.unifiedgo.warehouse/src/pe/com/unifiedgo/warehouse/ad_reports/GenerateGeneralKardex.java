/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.warehouse.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class GenerateGeneralKardex extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "GenerateGeneralKardex|M_Product_Id", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateGeneralKardex|docDate",
          SREDateTimeData.FirstDayOfMonth(this));

      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateGeneralKardex|inpDateTo",
          SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateGeneralKardex|NumMonths", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateGeneralKardex|M_Warehouse_ID");

      String strShowExcluded = vars.getGlobalVariable("inpShowExcluded",
          "GenerateGeneralKardex|ShowExcluded", "N");

      // String strSearchKey = vars.getStringParameter("inpSearchKey");

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateto, strWarehouse, strShowExcluded);

    } else if (vars.commandIn("LISTAR")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateGeneralKardex|NumMonths", "");
      String strOrg = vars.getStringParameter("inpadOrgId");

      // String strSearchKey = vars.getStringParameter("inpSearchKey");

      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateGeneralKardex|M_Warehouse_ID");

      String mProductId = vars.getGlobalVariable("inpmProductId",
          "GenerateGeneralKardex|M_Product_Id", "");

      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "GenerateGeneralKardex|ShowExcluded");

      /*
       * Organization org = OBDal.getInstance().get(Organization.class, strOrg); OBCriteria<Product>
       * product = OBDal.getInstance().createCriteria(Product.class);
       * product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, strSearchKey));
       * product.add(Restrictions.eq(Product.PROPERTY_ORGANIZATION, org)); List<Product> ProductList
       * = product.list(); String mProductId = ""; if (ProductList == null || ProductList.size() ==
       * 0) { mProductId = ""; } else { mProductId = ProductList.get(0).getId(); }
       */

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateTo, strWarehouse, strShowExcluded);

    } else if (vars.commandIn("PDF", "XLS")) {

      String strOrg = vars.getGlobalVariable("inpadOrgId", "GenerateGeneralKardex|inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateGeneralKardex|M_Warehouse_ID");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateGeneralKardex|inpDocDate");
      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateGeneralKardex|inpDateTo");

      String strProduct = vars.getStringParameter("inpmProductId");

      String strShowExcluded = vars.getGlobalVariable("inpShowExcluded",
          "GenerateGeneralKardex|ShowExcluded");

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDocDate, strDateto, strWarehouse, strProduct,
            strOrg, strShowExcluded);
      }
      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strDateto, strWarehouse, strProduct,
            strOrg, strShowExcluded);
      }
    } else if (vars.commandIn("GETTABID")) {
      String recordId = vars.getRequestGlobalVariable("recordId", "");
      String adTableId = vars.getRequestGlobalVariable("adTableId", "");
      String strIssotrx = vars.getRequestGlobalVariable("issotrx", "");

      boolean issotrx = false;
      if (strIssotrx != null && strIssotrx.compareTo("Y") == 0)
        issotrx = true;

      getJSONTab(response, vars, recordId, adTableId, issotrx);
    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg, String strShowExcluded)
      throws IOException, ServletException {

    OBError myMessage = null;
    myMessage = new OBError();

    GenerateGeneralKardexData[] dataOrganizacion = null;
    GenerateGeneralKardexData[] dataAlmacen = null;
    GenerateGeneralKardexData[] dataProducto = null;
    GenerateGeneralKardexData[] SaldosIniciales = null;
    GenerateGeneralKardexData[] SaldosFinales = null;

    GenerateGeneralKardexData[] StockReservado = null;

    List<ReportKardexData> listData = new ArrayList<ReportKardexData>();

    ReportKardexData[] data = null;

    String strShwowOnlyExcluded = "N";
    if (strShowExcluded.equals("Y"))
      strShwowOnlyExcluded = "Y";

    try {
      dataOrganizacion = GenerateGeneralKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      // myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateGeneralKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateGeneralKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateGeneralKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse,
          strShwowOnlyExcluded);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateGeneralKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse,
          strShwowOnlyExcluded);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = ReportKardexData.selectKardexAnalityc(this, mProductId, strWarehouse, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateto, strShwowOnlyExcluded);

    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      StockReservado = GenerateGeneralKardexData.selectStockReservado(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strReportName;
    String strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateGeneralKardex.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION",
        dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);
    parameters.put("PRODUCTO", dataProducto.length == 0 ? "" : dataProducto[0].producto);

    parameters.put("SALDO_INICIAL",
        SaldosIniciales.length == 0 ? "" : SaldosIniciales[0].movementqtyInitial);
    parameters.put("SALDO_FINAL",
        SaldosFinales.length == 0 ? "" : SaldosFinales[0].movementqtyFinal);

    parameters.put("STOCK_RESERVADO",
        StockReservado.length == 0 ? "" : StockReservado[0].qtyreserved);

    parameters.put("STOCK_DISPONIBLE",
        (SaldosIniciales.length == 0 ? new BigDecimal("0.0")
            : new BigDecimal(SaldosFinales[0].movementqtyFinal))
                .subtract(StockReservado.length == 0 ? new BigDecimal("0.0")
                    : new BigDecimal(StockReservado[0].qtyreserved)));

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    if (data.length != 0) {
      format = new SimpleDateFormat("dd-MM-yyyy");
      try {
        Date dtt = format.parse(data[0].movementdate);
        int mesCurrent = dtt.getMonth();
        double entradas = 0, salidas = 0;
        for (int i = 0; i < data.length; i++) {

          // if (data[i].movementtype.toLowerCase().contains("anulaci")) {
          if (data[i].movementtype.toLowerCase().contains("anulado")) {
            data[i].movementtype = "<font color='red'>" + data[i].movementtype + "</font>";
          }
          dtt = format.parse(data[i].movementdate);
          int mes = dtt.getMonth();
          if (mesCurrent == mes) {

            double qtyPos = 0, qtyNeg = 0;
            if (data[i].qtypossitive != null && !data[i].qtypossitive.equals(""))
              qtyPos = Double.parseDouble(data[i].qtypossitive);
            if (data[i].qtynegative != null && !data[i].qtynegative.equals(""))
              qtyNeg = Double.parseDouble(data[i].qtynegative);
            entradas += qtyPos;
            salidas += qtyNeg;

            listData.add(data[i]);
          } else {// Add linea al saldo
            ReportKardexData gdata = new ReportKardexData();
            gdata.transactionid = "saldo" + data[i].transactionid;

            gdata.qtynegative = String.valueOf(salidas);
            gdata.qtypossitive = String.valueOf(entradas);

            gdata.productid = data[0].productid;
            gdata.movementdate = getMes(mesCurrent);
            gdata.movementhour = "--";
            gdata.movementtype = "ACUM. DEL MES";
            gdata.physicalnumber = "--";
            gdata.referencedocument = "--";
            gdata.clientid = data[0].clientid;
            gdata.transactionid = data[0].transactionid;
            listData.add(gdata);
            // reset data
            entradas = 0;
            salidas = 0;
            mesCurrent = mes;
            i--;
          }
        }

        if (entradas != 0 || salidas != 0) {
          // poner saldo
          ReportKardexData gdata = new ReportKardexData();

          gdata.qtynegative = String.valueOf(salidas);
          gdata.qtypossitive = String.valueOf(entradas);

          gdata.productid = data[0].productid;
          gdata.movementdate = getMes(mesCurrent);
          gdata.movementhour = "--";
          gdata.movementtype = "ACUM. DEL MES";
          gdata.physicalnumber = "--";
          gdata.referencedocument = "--";
          gdata.clientid = data[0].clientid;
          gdata.transactionid = data[0].transactionid;

          listData.add(gdata);
        }

      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }

      BigDecimal saldo = new BigDecimal(
          SaldosIniciales.length == 0 ? "0.00" : SaldosIniciales[0].movementqtyInitial);
      Iterator<ReportKardexData> it = listData.iterator();
      while (it.hasNext()) {
        ReportKardexData x = it.next();
        if (x.movementtype.compareToIgnoreCase("ACUM. DEL MES") == 0)
          continue;
        if (x.qtypossitive == null)
          x.qtypossitive = "0.00";
        if (x.qtynegative == null)
          x.costnegative = "0.00";
        saldo = (new BigDecimal(x.qtypossitive).subtract(new BigDecimal(x.qtynegative))).add(saldo);
        x.saldo = saldo.toString();
      }

      data = listData.toArray(new ReportKardexData[listData.size()]);
    }

    renderJR(vars, response, strReportName, "Kardex_Analitico", strOutput, parameters, data, null);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg, String strShowExcluded)
      throws IOException, ServletException {

    GenerateGeneralKardexData[] dataOrganizacion = null;
    GenerateGeneralKardexData[] dataAlmacen = null;
    GenerateGeneralKardexData[] dataProducto = null;
    GenerateGeneralKardexData[] SaldosIniciales = null;
    GenerateGeneralKardexData[] SaldosFinales = null;

    GenerateGeneralKardexData[] StockReservado = null;

    String strShwowOnlyExcluded = "N";
    if (strShowExcluded.equals("Y"))
      strShwowOnlyExcluded = "Y";

    List<ReportKardexData> listData = new ArrayList<ReportKardexData>();

    ReportKardexData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = GenerateGeneralKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateGeneralKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateGeneralKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateGeneralKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse,
          strShwowOnlyExcluded);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateGeneralKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse,
          strShwowOnlyExcluded);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = ReportKardexData.selectKardexAnalityc(this, mProductId, strWarehouse, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateto, strShwowOnlyExcluded);

    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      StockReservado = GenerateGeneralKardexData.selectStockReservado(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strOutput = "xls";
    String strReportName;

    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateGeneralKardexExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ORGANIZACION",
        dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);
    parameters.put("PRODUCTO", dataProducto.length == 0 ? "" : dataProducto[0].producto);

    parameters.put("SALDO_INICIAL",
        SaldosIniciales.length == 0 ? "" : SaldosIniciales[0].movementqtyInitial);
    parameters.put("SALDO_FINAL",
        SaldosFinales.length == 0 ? "" : SaldosFinales[0].movementqtyFinal);

    parameters.put("STOCK_RESERVADO",
        StockReservado.length == 0 ? "" : StockReservado[0].qtyreserved);

    parameters.put("STOCK_DISPONIBLE",
        (SaldosIniciales.length == 0 ? new BigDecimal("0.0")
            : new BigDecimal(SaldosFinales[0].movementqtyFinal))
                .subtract(StockReservado.length == 0 ? new BigDecimal("0.0")
                    : new BigDecimal(StockReservado[0].qtyreserved)));

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    if (data.length != 0) {
      format = new SimpleDateFormat("dd-MM-yyyy");
      try {
        Date dtt = format.parse(data[0].movementdate);
        int mesCurrent = dtt.getMonth();
        double entradas = 0, salidas = 0;
        for (int i = 0; i < data.length; i++) {

          if (data[i].movementtype.toLowerCase().contains("anulado")) {
            data[i].movementtype = "<font color='red'>" + data[i].movementtype + "</font>";
          }
          dtt = format.parse(data[i].movementdate);
          int mes = dtt.getMonth();
          if (mesCurrent == mes) {

            double qtyPos = 0, qtyNeg = 0;
            if (data[i].qtypossitive != null && !data[i].qtypossitive.equals(""))
              qtyPos = Double.parseDouble(data[i].qtypossitive);
            if (data[i].qtynegative != null && !data[i].qtynegative.equals(""))
              qtyNeg = Double.parseDouble(data[i].qtynegative);
            entradas += qtyPos;
            salidas += qtyNeg;

            listData.add(data[i]);
          } else {// Add linea al saldo
            ReportKardexData gdata = new ReportKardexData();
            gdata.transactionid = "saldo" + data[i].transactionid;

            gdata.qtynegative = String.valueOf(salidas);
            gdata.qtypossitive = String.valueOf(entradas);

            gdata.productid = data[0].productid;
            gdata.movementdate = getMes(mesCurrent);
            gdata.movementhour = "--";
            gdata.movementtype = "ACUM. DEL MES";
            gdata.physicalnumber = "--";
            gdata.referencedocument = "--";
            gdata.clientid = data[0].clientid;
            gdata.transactionid = data[0].transactionid;
            listData.add(gdata);
            entradas = 0;
            salidas = 0;
            mesCurrent = mes;
            i--;
          }
        }

        if (entradas != 0 || salidas != 0) {
          // poner saldo
          ReportKardexData gdata = new ReportKardexData();

          gdata.qtynegative = String.valueOf(salidas);
          gdata.qtypossitive = String.valueOf(entradas);

          gdata.productid = data[0].productid;
          gdata.movementdate = getMes(mesCurrent);
          gdata.movementhour = "--";
          gdata.movementtype = "ACUM. DEL MES";
          gdata.physicalnumber = "--";
          gdata.referencedocument = "--";
          gdata.clientid = data[0].clientid;
          gdata.transactionid = data[0].transactionid;

          listData.add(gdata);
        }

      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }

      BigDecimal saldo = new BigDecimal(
          SaldosIniciales.length == 0 ? "0.00" : SaldosIniciales[0].movementqtyInitial);
      Iterator<ReportKardexData> it = listData.iterator();
      while (it.hasNext()) {
        ReportKardexData x = it.next();
        if (x.movementtype.compareToIgnoreCase("ACUM. DEL MES") == 0)
          continue;
        if (x.qtypossitive == null)
          x.qtypossitive = "0.00";
        if (x.qtynegative == null)
          x.costnegative = "0.00";
        saldo = (new BigDecimal(x.qtypossitive).subtract(new BigDecimal(x.qtynegative))).add(saldo);
        x.saldo = saldo.toString();
      }
      data = listData.toArray(new ReportKardexData[listData.size()]);
    }

    renderJR(vars, response, strReportName, "Kardex_Analitico_Excel", strOutput, parameters, data,
        null);

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String strOrg, String strDateTo, String strWarehouse, String strShowExcluded)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GenerateGeneralKardexData[] data = null;
    ReportKardexData[] data2 = null;
    GenerateGeneralKardexData[] dataINIT = null;
    GenerateGeneralKardexData[] dataFINAL = null;
    GenerateGeneralKardexData[] dataGENERAL = null;
    GenerateGeneralKardexData[] dataQTYRESERVADO = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/warehouse/ad_reports/GenerateGeneralKardex", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        // data = GenerateGeneralKardexData.select(ad_org_id,
        // vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strNumMonths);
        data = GenerateGeneralKardexData.selectKardex(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, null);

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data2 == null || data2.length == 0) {
          discard[0] = "selEliminar";
          // data2 = GenerateGeneralKardexData.set();
        } else {
          xmlDocument.setData("structure2", data2);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        // data = GenerateGeneralKardexData.set();

      }
    }

    String strShwowOnlyExcluded = "N";
    if (strShowExcluded.equals("Y"))
      strShwowOnlyExcluded = "Y";

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        String warehouse_id = strWarehouse;
        dataINIT = GenerateGeneralKardexData.selectKardexInitial(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id,
            strShwowOnlyExcluded);
        dataFINAL = GenerateGeneralKardexData.selectKardexFinal(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id,
            strShwowOnlyExcluded);
        dataGENERAL = GenerateGeneralKardexData.selectDataGeneral(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);

        data2 = ReportKardexData.selectKardexAnalityc(this, mProductId, warehouse_id, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strShwowOnlyExcluded);

        /*
         * data = GenerateGeneralKardexData.selectKardex(this, ad_org_id,
         * vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
         */
        dataQTYRESERVADO = GenerateGeneralKardexData.selectStockReservado(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }

      List<ReportKardexData> listData = new ArrayList<ReportKardexData>();
      if (data2.length != 0) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
          Date dtt = format.parse(data2[0].movementdate);
          int mesCurrent = dtt.getMonth();
          double entradas = 0, salidas = 0;
          String movementype = null;
          for (int i = 0; i < data2.length; i++) {

            // if (data2[i].movementtype.toLowerCase().contains("anulaci")) {
            if (data2[i].movementtype.toLowerCase().contains("anulado")) {
              data2[i].movementtype = "<font color='red'>" + data2[i].movementtype + "</font>";
            }
            dtt = format.parse(data2[i].movementdate);
            int mes = dtt.getMonth();
            if (mesCurrent == mes) {

              double qtyPos = 0, qtyNeg = 0;
              if (data2[i].qtypossitive != null && !data2[i].qtypossitive.equals(""))
                qtyPos = Double.parseDouble(data2[i].qtypossitive);
              if (data2[i].qtynegative != null && !data2[i].qtynegative.equals(""))
                qtyNeg = Double.parseDouble(data2[i].qtynegative);
              entradas += qtyPos;
              salidas += qtyNeg;

              listData.add(data2[i]);
            } else {// Add linea al saldo
              ReportKardexData gdata = new ReportKardexData();
              gdata.transactionid = "saldo" + data2[i].transactionid;

              double qtyPos = Double.parseDouble(dataINIT[0].movementqty);
              double qtyNeg = Double.parseDouble(dataINIT[0].movementqtynegative);

              gdata.qtynegative = "<b>" + String.valueOf(salidas) + "</b>";
              gdata.qtypossitive = "<b>" + String.valueOf(entradas) + "</b>";

              gdata.productid = data2[0].productid;
              gdata.movementdate = "<b>" + getMes(mesCurrent) + "</b>";
              gdata.movementhour = "--";
              gdata.movementtype = "<b>" + "ACUM. DEL MES" + "</b>";
              // gdata.transactionid = "--";
              gdata.physicalnumber = "--";
              gdata.referencedocument = "--";
              gdata.clientid = data2[0].clientid;
              gdata.transactionid = data2[0].transactionid;
              listData.add(gdata);
              // reset data
              entradas = 0;
              salidas = 0;
              mesCurrent = mes;
              i--;
            }
          }

          if (entradas != 0 || salidas != 0) {
            // poner saldo
            ReportKardexData gdata = new ReportKardexData();

            double qtyPos = Double.parseDouble(dataINIT[0].movementqty);
            double qtyNeg = Double.parseDouble(dataINIT[0].movementqtynegative);
            gdata.qtynegative = "<b>" + String.valueOf(salidas) + "</b>";
            gdata.qtypossitive = "<b>" + String.valueOf(entradas) + "<b>";

            gdata.productid = data2[0].productid;
            gdata.movementdate = "<b>" + getMes(mesCurrent) + "</b>";
            gdata.movementhour = "--";
            gdata.movementtype = "<b>" + "ACUM. DEL MES" + "</b>";
            gdata.physicalnumber = "--";
            gdata.referencedocument = "--";
            // gdata.prdID = data[0].prdID;
            gdata.clientid = data2[0].clientid;
            gdata.transactionid = data2[0].transactionid;

            listData.add(gdata);
          }

        } catch (Exception e) {
          // TODO: handle exception
          e.printStackTrace();
        }
        data2 = listData.toArray(new ReportKardexData[listData.size()]);
      }

      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if ((data2 == null || data2.length == 0) && (dataINIT == null || dataINIT.length == 0)
            && (dataFINAL == null || dataFINAL.length == 0)
            && (dataGENERAL == null || dataGENERAL.length == 0)
            && (dataQTYRESERVADO == null || dataQTYRESERVADO.length == 0)) {
          discard[0] = "selEliminar";
          // data = GenerateGeneralKardexData.set();
        } else {
          xmlDocument.setData("structure3", dataINIT);
          xmlDocument.setData("structure4", dataFINAL);
          xmlDocument.setData("structure7", dataGENERAL);
          xmlDocument.setData("structure8", dataQTYRESERVADO);
          xmlDocument.setData("structure2", data2);
        }
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateGeneralKardex", false, "",
          "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.GenerateGeneralKardex");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "GenerateGeneralKardex.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateGeneralKardex.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        // OBError myMessage = vars.getMessage("GenerateGeneralKardex");
        // vars.removeMessage("GenerateGeneralKardex");
        // if (myMessage != null) {
        // xmlDocument.setParameter("messageType", myMessage.getType());
        // xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        // xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        // }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6",
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GenerateGeneralKardex"),
            Utility.getContext(this, vars, "#User_Client", "GenerateGeneralKardex"), 0);
        // comboTableData.fillParameters(null, "GenerateGeneralKardex", "");
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateGeneralKardex",
            strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("paramShowExcluded", strShwowOnlyExcluded);
      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          GenerateGeneralKardexData.selectMproduct(this, mProductId));

      // xmlDocument.setParameter("mWarehouseId", strWarehouse);
      // xmlDocument.setParameter("adOrgId", strOrg);
      // xmlDocument.setParameter("productDescription",
      // GenerateGeneralKardexData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("mWarehouseDescription",
          GenerateGeneralKardexData.selectMWarehouse(this, strWarehouse));

      xmlDocument.setParameter("adOrgId", strOrg);

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "",
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GenerateGeneralKardex"),
            Utility.getContext(this, vars, "#User_Client", "GenerateGeneralKardex"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateGeneralKardex", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      // xmlDocument.setParameter("NumMonths", "".equals(strNumMonths) ? "1" : strNumMonths);
      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getMes(int month) {
    String result;
    switch (month) {
    case 0: {
      result = "Enero";
      break;
    }
    case 1: {
      result = "Febrero";
      break;
    }
    case 2: {
      result = "Marzo";
      break;
    }
    case 3: {
      result = "Abril";
      break;
    }
    case 4: {
      result = "Mayo";
      break;
    }
    case 5: {
      result = "Junio";
      break;
    }
    case 6: {
      result = "Julio";
      break;
    }
    case 7: {
      result = "Agosto";
      break;
    }
    case 8: {
      result = "Septiembre";
      break;
    }
    case 9: {
      result = "Octubre";
      break;
    }
    case 10: {
      result = "Noviembre";
      break;
    }
    case 11: {
      result = "Diciembre";
      break;
    }
    default: {
      result = "Error";
      break;
    }
    }

    return result;
  }

  private void getJSONTab(HttpServletResponse response, VariablesSecureApp vars, String recordId,
      String adTableId, boolean issotrx) throws IOException, ServletException {
    JSONObject msg = new JSONObject();
    try {
      msg.put("adTabId", SCO_Utils.getTabId(this, adTableId, recordId, issotrx));
      msg.put("recordId", recordId);
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println(msg.toString());
    out.close();

  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
