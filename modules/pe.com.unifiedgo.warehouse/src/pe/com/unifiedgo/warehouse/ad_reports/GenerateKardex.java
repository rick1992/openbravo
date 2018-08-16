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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class GenerateKardex extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String mProductId = vars
          .getGlobalVariable("inpmProductId", "GenerateKardex|M_Product_Id", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateKardex|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateKardex|inpDateTo",
          SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths", "GenerateKardex|NumMonths", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
      // String strSearchKey = vars.getStringParameter("inpSearchKey");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateKardex|M_Warehouse_ID");

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateto, strWarehouse);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String mProductId = vars.getRequestGlobalVariable("inpmProductId",
          "GenerateKardex|M_Product_Id");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths", "GenerateKardex|NumMonths", "");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateKardex|M_Warehouse_ID");
      String strSearchKey = vars.getStringParameter("inpSearchKey");
      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateTo, strWarehouse);

    } else if (vars.commandIn("LISTAR")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths", "GenerateKardex|NumMonths", "");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateKardex|M_Warehouse_ID");
      // String strSearchKey = vars.getStringParameter("inpSearchKey");

      String mProductId = vars.getGlobalVariable("inpmProductId",
          "GenerateAnaliticKardex|M_Product_Id", "");

      /*
       * Organization org = OBDal.getInstance().get(Organization.class, strOrg); OBCriteria<Product>
       * product = OBDal.getInstance().createCriteria(Product.class);
       * product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, strSearchKey));
       * product.add(Restrictions.eq(Product.PROPERTY_ORGANIZATION, org)); List<Product> ProductList
       * = product.list(); String mProductId = ""; if (ProductList == null || ProductList.size() ==
       * 0) { mProductId = ""; } else { mProductId = ProductList.get(0).getId(); }
       */

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateTo, strWarehouse);
    } else if (vars.commandIn("PDF", "XLS")) {
      String strOrg = vars.getGlobalVariable("inpadOrgId", "GenerateKardex|inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateKardex|M_Warehouse_ID");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateKardex|inpDocDate");
      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateKardex|inpDateTo");
      String strProduct = vars.getGlobalVariable("inpmProductId", "GenerateKardex|inpmProductId");

      setHistoryCommand(request, "DEFAULT");

      // OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
      // product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value));
      //
      // List<Product> ProductList = product.list();
      //
      // String mProductId = "";
      // if (ProductList == null || ProductList.size() == 0) {
      // mProductId = "";
      // } else {
      // mProductId = ProductList.get(0).getId();
      // }

      // System.out.println("organizacion");
      // System.out.println(strOrg);
      // System.out.println("almacen");
      // System.out.println(strWarehouse);
      // System.out.println("producto");
      // System.out.println(mProductId);

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDocDate, strDateto, strWarehouse, strProduct,
            strOrg);
      }
      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strDateto, strWarehouse, strProduct,
            strOrg);
      }

    }

    else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg) throws IOException, ServletException {

    GenerateKardexData[] dataOrganizacion = null;
    GenerateKardexData[] dataAlmacen = null;
    GenerateKardexData[] dataProducto = null;
    GenerateKardexData[] SaldosIniciales = null;
    GenerateKardexData[] SaldosFinales = null;

    GenerateKardexData[] StockReservado = null;

    GenerateKardexData[] data = null;

    Date fecha = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = GenerateKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = GenerateKardexData.selectKardex(this, strOrg, vars.getSessionValue("#AD_CLIENT_ID"),
          mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      StockReservado = GenerateKardexData.selectStockReservado(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateKardex.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateKardexExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ORGANIZACION", dataOrganizacion.length == 0 ? ""
        : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);
    parameters.put("PRODUCTO", dataProducto.length == 0 ? "" : dataProducto[0].producto);

    parameters.put("SALDO_INICIAL", SaldosIniciales.length == 0 ? ""
        : SaldosIniciales[0].movementqtyInitial);
    parameters.put("SALDO_FINAL", SaldosFinales.length == 0 ? ""
        : SaldosFinales[0].movementqtyFinal);

    parameters.put("STOCK_RESERVADO", StockReservado.length == 0 ? ""
        : StockReservado[0].qtyreserved);

    parameters.put("STOCK_DISPONIBLE", (SaldosIniciales.length == 0 ? new BigDecimal("0.0")
        : new BigDecimal(SaldosFinales[0].movementqtyFinal))
        .subtract(StockReservado.length == 0 ? new BigDecimal("0.0") : new BigDecimal(
            StockReservado[0].qtyreserved)));

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    BigDecimal acumulado;
    if (SaldosIniciales.length == 0) {
      acumulado = new BigDecimal("0");
    } else {
      acumulado = new BigDecimal(SaldosIniciales[0].movementqtyInitial);
    }

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    for (int i = 0; i < data.length; i++) {
      acumulado = acumulado
          .add(
              (data[i].movementqty == null || data[i].movementqty == "" ? new BigDecimal(0.0)
                  : new BigDecimal(data[i].movementqty)))
          .subtract(
              data[i].movementqtynegative == null || data[i].movementqtynegative == "" ? new BigDecimal(
                  0.0) : new BigDecimal(data[i].movementqtynegative));
      data[i].saldo = acumulado.toString();
      // sacar el mes de cada linea
      try {
        Date dtt = format.parse(data[i].movementDate);
        data[i].mes = getMes(dtt.getMonth());
      } catch (Exception ex) {
        System.out.println("Error en la conversion");
      }
      // System.out.println(acumulado);
      // System.out.println(data[i].movementqty);
      // System.out.println(data[i].movementqtynegative);
      // System.out.println(data[i].mes);
    }

    renderJR(vars, response, strReportName, "Kardex_Detallado", strOutput, parameters, data, null);

  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg) throws IOException, ServletException {

    GenerateKardexData[] dataOrganizacion = null;
    GenerateKardexData[] dataAlmacen = null;
    GenerateKardexData[] dataProducto = null;
    GenerateKardexData[] SaldosIniciales = null;
    GenerateKardexData[] SaldosFinales = null;

    GenerateKardexData[] StockReservado = null;

    GenerateKardexData[] data = null;

    Date fecha = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = GenerateKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = GenerateKardexData.selectKardex(this, strOrg, vars.getSessionValue("#AD_CLIENT_ID"),
          mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      StockReservado = GenerateKardexData.selectStockReservado(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateKardex.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateKardexExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ORGANIZACION", dataOrganizacion.length == 0 ? ""
        : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);
    parameters.put("PRODUCTO", dataProducto.length == 0 ? "" : dataProducto[0].producto);

    parameters.put("SALDO_INICIAL", SaldosIniciales.length == 0 ? ""
        : SaldosIniciales[0].movementqtyInitial);
    parameters.put("SALDO_FINAL", SaldosFinales.length == 0 ? ""
        : SaldosFinales[0].movementqtyFinal);

    parameters.put("STOCK_RESERVADO", StockReservado.length == 0 ? ""
        : StockReservado[0].qtyreserved);

    parameters.put("STOCK_DISPONIBLE", (SaldosIniciales.length == 0 ? new BigDecimal("0.0")
        : new BigDecimal(SaldosFinales[0].movementqtyFinal))
        .subtract(StockReservado.length == 0 ? new BigDecimal("0.0") : new BigDecimal(
            StockReservado[0].qtyreserved)));

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    BigDecimal acumulado;
    if (SaldosIniciales.length == 0) {
      acumulado = new BigDecimal("0");
    } else {
      acumulado = new BigDecimal(SaldosIniciales[0].movementqtyInitial);
    }

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    for (int i = 0; i < data.length; i++) {
      acumulado = acumulado
          .add(
              (data[i].movementqty == null || data[i].movementqty == "" ? new BigDecimal(0.0)
                  : new BigDecimal(data[i].movementqty)))
          .subtract(
              data[i].movementqtynegative == null || data[i].movementqtynegative == "" ? new BigDecimal(
                  0.0) : new BigDecimal(data[i].movementqtynegative));
      data[i].saldo = acumulado.toString();
      // sacar el mes de cada linea
      try {
        Date dtt = format.parse(data[i].movementDate);
        data[i].mes = getMes(dtt.getMonth());
      } catch (Exception ex) {
        System.out.println("Error en la conversion");
      }
      // System.out.println(acumulado);
      // System.out.println(data[i].movementqty);
      // System.out.println(data[i].movementqtynegative);
      // System.out.println(data[i].mes);
    }

    renderJR(vars, response, strReportName, "Kardex_Detallado_Excel", strOutput, parameters, data,
        null);

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String strOrg, String strDateTo, String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GenerateKardexData[] data = null;
    GenerateKardexData[] dataINIT = null;
    GenerateKardexData[] dataFINAL = null;
    GenerateKardexData[] dataGENERAL = null;
    GenerateKardexData[] dataQTYRESERVADO = null;

    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/warehouse/ad_reports/GenerateKardex",
        discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        // data = GenerateKardexData.select(ad_org_id,
        // vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strNumMonths);
        data = GenerateKardexData.selectKardex(this, ad_org_id,
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
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = GenerateKardexData.set();
        } else {
          xmlDocument.setData("structure2", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = GenerateKardexData.set();
      }
    }

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        String warehouse_id = strWarehouse;
        dataINIT = GenerateKardexData.selectKardexInitial(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataFINAL = GenerateKardexData.selectKardexFinal(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataGENERAL = GenerateKardexData.selectDataGeneral(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        data = GenerateKardexData.selectKardex(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataQTYRESERVADO = GenerateKardexData.selectStockReservado(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }

      List<GenerateKardexData> listData = new ArrayList<GenerateKardexData>();
      if (data.length != 0) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
          Date dtt = format.parse(data[0].movementDate);
          int mesCurrent = dtt.getMonth();
          double entradas = 0, salidas = 0;
          String movementype = null;
          for (int i = 0; i < data.length; i++) {
            if (data[i].movementtype.toLowerCase().contains("anulaci")) {
              data[i].movementtype = "<font color='red'>" + data[i].movementtype + "</font>";
            }
            dtt = format.parse(data[i].movementDate);
            int mes = dtt.getMonth();
            if (mesCurrent == mes) {
              double qtyPos = 0, qtyNeg = 0;
              if (data[i].movementqty != null && !data[i].movementqty.equals(""))
                qtyPos = Double.parseDouble(data[i].movementqty);
              else if (data[i].movementqtynegative != null
                  && !data[i].movementqtynegative.equals(""))
                qtyNeg = Double.parseDouble(data[i].movementqtynegative);

              entradas += qtyPos;
              salidas += qtyNeg;
              listData.add(data[i]);
            } else {
              // poner saldo
              GenerateKardexData gdata = new GenerateKardexData();
              gdata.id = "saldo" + data[i].id;

              // double qtyPos = Double.parseDouble(dataINIT[0].movementqty);
              // double qtyNeg = Double.parseDouble(dataINIT[0].movementqtynegative);

              gdata.movementqtynegative = "<b>" + String.valueOf(salidas) + "</b>";
              gdata.movementqty = "<b>" + String.valueOf(entradas) + "</b>";

              gdata.name = "ACUM. DEL MES";
              gdata.productid = data[0].productid;
              gdata.movementDate = "<b>" + "ACUM. DEL MES" + "</b>";
              gdata.movementtype = "<b>" + getMes(mesCurrent) + "</b>";
              gdata.transactionID = "--";
              // gdata.movementtype = "--";
              gdata.inventoryid = "--";
              gdata.storagebin = "--";
              gdata.shipmentid = "--";
              gdata.prdID = data[0].prdID;
              gdata.transactionID = data[0].transactionID;

              listData.add(gdata);

              // reset
              entradas = 0;
              salidas = 0;
              mesCurrent = mes;
              i--;
            }

          }

          if (entradas != 0 || salidas != 0) {
            // poner saldo
            GenerateKardexData gdata = new GenerateKardexData();
            gdata.id = data[0].id;

            /*
             * double qtyPos = Double.parseDouble(dataINIT[0].movementqty); double qtyNeg =
             * Double.parseDouble(dataINIT[0].movementqtynegative);
             */

            gdata.movementqtynegative = "<b>" + String.valueOf(salidas) + "</b>";
            gdata.movementqty = "<b>" + String.valueOf(entradas) + "<b>";

            gdata.name = "ACUM. DEL MES";
            gdata.productid = data[0].productid;
            gdata.movementDate = "<b>" + "ACUM. DEL MES" + "</b>";
            gdata.movementtype = "<b>" + getMes(mesCurrent) + "</b>";
            gdata.transactionID = "--";
            // gdata.movementtype = "--";
            gdata.storagebin = "--";
            gdata.shipmentid = "--";
            gdata.prdID = data[0].prdID;
            gdata.transactionID = data[0].transactionID;
            listData.add(gdata);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        // agregar por mes
        data = listData.toArray(new GenerateKardexData[listData.size()]);
      }

      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if ((data == null || data.length == 0) && (dataINIT == null || dataINIT.length == 0)
            && (dataFINAL == null || dataFINAL.length == 0)
            && (dataGENERAL == null || dataGENERAL.length == 0)
            && (dataQTYRESERVADO == null || dataQTYRESERVADO.length == 0)) {
          discard[0] = "selEliminar";
          data = GenerateKardexData.set();
        } else {
          xmlDocument.setData("structure3", dataINIT);
          xmlDocument.setData("structure4", dataFINAL);
          xmlDocument.setData("structure7", dataGENERAL);
          xmlDocument.setData("structure2", data);
          xmlDocument.setData("structure8", dataQTYRESERVADO);
        }
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateKardex", false, "", "",
          "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.GenerateKardex");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GenerateKardex.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateKardex.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("GenerateKardex");
        vars.removeMessage("GenerateKardex");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "GenerateKardex"), Utility.getContext(this, vars,
                "#User_Client", "GenerateKardex"), 0);
        // comboTableData.fillParameters(null, "GenerateKardex", "");
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateKardex", strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
       * "GenerateKardex"), Utility.getContext(this, vars, "#User_Client", "GenerateKardex"), 0);
       * Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateKardex",
       * strWarehouse); xmlDocument.setData("reportM_Warehouse_ID", "liststructure",
       * comboTableData.select(false)); comboTableData = null; } catch (Exception ex) { throw new
       * ServletException(ex); }
       */

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      // xmlDocument.setParameter("SearchKey", strSearchKey);
      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          GenerateKardexData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("mWarehouseDescription",
          GenerateKardexData.selectMWarehouse(this, strWarehouse));

      xmlDocument.setParameter("adOrgId", strOrg);
      // xmlDocument.setParameter("productDescription",
      // GenerateKardexData.selectMproduct(this, mProductId));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "GenerateKardex"), Utility.getContext(this, vars,
                "#User_Client", "GenerateKardex"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateKardex", "");
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

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
