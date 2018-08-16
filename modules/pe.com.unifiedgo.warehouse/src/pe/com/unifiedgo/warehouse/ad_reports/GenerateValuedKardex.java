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
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class GenerateValuedKardex extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "GenerateValuedKardex|M_Product_Id", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateValuedKardex|docDate",
          SREDateTimeData.FirstDayOfMonth(this));

      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateValuedKardex|inpDateTo",
          SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateValuedKardex|NumMonths", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateValuedKardex|M_Warehouse_ID");
      String strSearchKey = vars.getStringParameter("inpSearchKey");

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateto, strWarehouse, strSearchKey);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String mProductId = vars.getRequestGlobalVariable("inpmProductId",
          "GenerateValuedKardex|M_Product_Id");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateValuedKardex|NumMonths", "");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateValuedKardex|M_Warehouse_ID");
      String strSearchKey = vars.getStringParameter("inpSearchKey");
      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateTo, strWarehouse, strSearchKey);

    } else if (vars.commandIn("LISTAR")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateValuedKardex|NumMonths", "");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateValuedKardex|M_Warehouse_ID");
      String strSearchKey = vars.getStringParameter("inpSearchKey");

      Organization org = OBDal.getInstance().get(Organization.class, strOrg);

      OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
      product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, strSearchKey));
      product.add(Restrictions.eq(Product.PROPERTY_ORGANIZATION, org));

      List<Product> ProductList = product.list();

      String mProductId = "";
      if (ProductList == null || ProductList.size() == 0) {
        mProductId = "";
      } else {
        mProductId = ProductList.get(0).getId();
      }

      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateTo, strWarehouse, strSearchKey);
    } else if (vars.commandIn("PDF", "XLS")) {
      String strOrg = vars.getGlobalVariable("inpadOrgId", "GenerateValuedKardex|inpadOrgId");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateValuedKardex|M_Warehouse_ID");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateValuedKardex|inpDocDate");
      String strDateto = vars.getGlobalVariable("inpDateTo", "GenerateValuedKardex|inpDateTo");
      String value = vars.getGlobalVariable("inpSearchKey", "GenerateValuedKardex|inpSearchKey");

      setHistoryCommand(request, "DEFAULT");

      OBCriteria<Product> product = OBDal.getInstance().createCriteria(Product.class);
      product.add(Restrictions.eq(Product.PROPERTY_SEARCHKEY, value));

      List<Product> ProductList = product.list();

      String mProductId = "";
      if (ProductList == null || ProductList.size() == 0) {
        mProductId = "";
      } else {
        mProductId = ProductList.get(0).getId();
      }

      System.out.println("organizacion");
      System.out.println(strOrg);
      System.out.println("almacen");
      System.out.println(strWarehouse);
      System.out.println("producto");
      System.out.println(mProductId);

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDocDate, strDateto, strWarehouse, mProductId,
            strOrg);
      }
      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strDateto, strWarehouse, mProductId,
            strOrg);
      }

    }

    else if (vars.commandIn("XLS")) {
      String mProductId = vars.getRequestGlobalVariable("inpmProductId",
          "GenerateValuedKardex|M_Product_Id");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "GenerateValuedKardex|docDate",
          SREDateTimeData.today(this));
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "GenerateValuedKardex|NumMonths", "");

      String reportData = vars.getStringParameter("inpReportData");
      String reportHeader = vars.getStringParameter("inpReportHeader");
      if (reportData == null || reportData.equals("")) {
        GenerateValuedKardex.strBDErrorMessage = "SRE_noselected";
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, GenerateValuedKardex.strBDErrorMessage, vars.getLanguage()));
      } else {
        printPage(request, response, vars, mProductId, strDocDate, strNumMonths, reportData,
            reportHeader);
      }

    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg) throws IOException, ServletException {

    GenerateValuedKardexData[] dataOrganizacion = null;
    GenerateValuedKardexData[] dataAlmacen = null;
    GenerateValuedKardexData[] dataProducto = null;
    GenerateValuedKardexData[] SaldosIniciales = null;
    GenerateValuedKardexData[] SaldosFinales = null;

    GenerateValuedKardexData[] data = null;

    Date fecha = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = GenerateValuedKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateValuedKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateValuedKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateValuedKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateValuedKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = GenerateValuedKardexData.selectKardex(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateValuedKardex.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateValuedKardexExcel.jrxml";
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
      System.out.println(acumulado);
      System.out.println(data[i].movementqty);
      System.out.println(data[i].movementqtynegative);
      System.out.println(data[i].mes);
    }

    renderJR(vars, response, strReportName, "Kardex_Valorizado", strOutput, parameters, data, null);

  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String mProductId, String strOrg) throws IOException, ServletException {

    GenerateValuedKardexData[] dataOrganizacion = null;
    GenerateValuedKardexData[] dataAlmacen = null;
    GenerateValuedKardexData[] dataProducto = null;
    GenerateValuedKardexData[] SaldosIniciales = null;
    GenerateValuedKardexData[] SaldosFinales = null;

    GenerateValuedKardexData[] data = null;

    Date fecha = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = GenerateValuedKardexData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = GenerateValuedKardexData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataProducto = GenerateValuedKardexData.selectProducto(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosIniciales = GenerateValuedKardexData.selectKardexInitial(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      SaldosFinales = GenerateValuedKardexData.selectKardexFinal(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = GenerateValuedKardexData.selectKardex(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateValuedKardex.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateValuedKardexExcel.jrxml";
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

    renderJR(vars, response, strReportName, "Kardex_Valorizado_Excel", strOutput, parameters, data,
        null);

  }

  private GenerateValuedKardexData[] processReportData(String reportData) {
    GenerateValuedKardex.strBDErrorMessage = "";

    String[] reportDataColumns = reportData.split(";");

    GenerateValuedKardexData objectGenerateValuedKardexData = new GenerateValuedKardexData();

    objectGenerateValuedKardexData.productid = reportDataColumns[0];
    objectGenerateValuedKardexData.searchkey = reportDataColumns[1];
    objectGenerateValuedKardexData.internalcode = (reportDataColumns[2] != null) ? reportDataColumns[2]
        : "--";
    objectGenerateValuedKardexData.name = reportDataColumns[3];
    objectGenerateValuedKardexData.avgmonthlysales1 = ("--".equals(reportDataColumns[4])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[4]);
    objectGenerateValuedKardexData.avgmonthlysales2 = ("--".equals(reportDataColumns[5])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[5]);
    objectGenerateValuedKardexData.avgmonthlysales3 = ("--".equals(reportDataColumns[6])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[6]);
    objectGenerateValuedKardexData.avgmonthlysales4 = ("--".equals(reportDataColumns[7])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[7]);
    objectGenerateValuedKardexData.avgmonthlysales5 = ("--".equals(reportDataColumns[8])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[8]);
    objectGenerateValuedKardexData.avgmonthlysales6 = ("--".equals(reportDataColumns[9])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[9]);
    objectGenerateValuedKardexData.avgmonthlysales7 = ("--".equals(reportDataColumns[10])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[10]);
    objectGenerateValuedKardexData.avgmonthlysales8 = ("--".equals(reportDataColumns[11])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[11]);
    objectGenerateValuedKardexData.avgmonthlysales9 = ("--".equals(reportDataColumns[12])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[12]);
    objectGenerateValuedKardexData.avgmonthlysales10 = ("--".equals(reportDataColumns[13])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[13]);
    objectGenerateValuedKardexData.avgmonthlysales11 = ("--".equals(reportDataColumns[14])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[14]);
    objectGenerateValuedKardexData.avgmonthlysales12 = ("--".equals(reportDataColumns[15])) ? new BigDecimal(
        -1) : new BigDecimal(reportDataColumns[15]);

    objectGenerateValuedKardexData.rownum = new Integer(0).toString();

    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    vector.addElement(objectGenerateValuedKardexData);

    GenerateValuedKardexData objectGenerateValuedKardexDataArray[] = new GenerateValuedKardexData[vector
        .size()];
    vector.copyInto(objectGenerateValuedKardexDataArray);

    return (objectGenerateValuedKardexDataArray);
  }

  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String reportData, String reportHeader) throws IOException, ServletException {
    response.setContentType("text/html; charset=UTF-8");

    GenerateValuedKardexData[] data = null;

    try {
      data = processReportData(reportData);

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }

    if (data != null && data.length == 1 && data[0] == null) {

      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoDataFound", vars.getLanguage()));

    }

    String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ProductAvgMonthlySalesExcel.jrxml";
    response.setHeader("Content-disposition", "inline; filename=ProductAvgMonthlySalesExcel.xls");

    String strSubTitle = "";
    strSubTitle = Utility.messageBD(this, "Document Date", vars.getLanguage()) + " " + strDocDate;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("REPORT_SUBTITLE", strSubTitle);

    String[] reportHeaderMonth = reportHeader.split(";");

    parameters.put("DOCDATE", strDocDate);
    parameters.put("NUMMONTHS", strNumMonths);
    parameters.put("MONTH1", (!"".equals(reportHeaderMonth[0])) ? reportHeaderMonth[0] : "Mes 1");
    parameters.put("MONTH2", (!"".equals(reportHeaderMonth[1])) ? reportHeaderMonth[1] : "Mes 2");
    parameters.put("MONTH3", (!"".equals(reportHeaderMonth[2])) ? reportHeaderMonth[2] : "Mes 3");
    parameters.put("MONTH4", (!"".equals(reportHeaderMonth[3])) ? reportHeaderMonth[3] : "Mes 4");
    parameters.put("MONTH5", (!"".equals(reportHeaderMonth[4])) ? reportHeaderMonth[4] : "Mes 5");
    parameters.put("MONTH6", (!"".equals(reportHeaderMonth[5])) ? reportHeaderMonth[5] : "Mes 6");
    parameters.put("MONTH7", (!"".equals(reportHeaderMonth[6])) ? reportHeaderMonth[6] : "Mes 7");
    parameters.put("MONTH8", (!"".equals(reportHeaderMonth[7])) ? reportHeaderMonth[7] : "Mes 8");
    parameters.put("MONTH9", (!"".equals(reportHeaderMonth[8])) ? reportHeaderMonth[8] : "Mes 9");
    parameters.put("MONTH10", (!"".equals(reportHeaderMonth[9])) ? reportHeaderMonth[9] : "Mes 10");
    parameters.put("MONTH11", (!"".equals(reportHeaderMonth[10])) ? reportHeaderMonth[10]
        : "Mes 11");
    parameters.put("MONTH12", (!"".equals(reportHeaderMonth[11])) ? reportHeaderMonth[11]
        : "Mes 12");

    if (data != null) {
      renderJR(vars, response, strReportName, "xls", parameters, data, null);
    }

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String strOrg, String strDateTo, String strWarehouse, String strSearchKey)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GenerateValuedKardexData[] data = null;
    GenerateValuedKardexData[] dataINIT = null;
    GenerateValuedKardexData[] dataFINAL = null;
    GenerateValuedKardexData[] dataGENERAL = null;
    GenerateValuedKardexData[] dataQTYRESERVADO = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/GenerateValuedKardex", discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        // data = GenerateValuedKardexData.select(ad_org_id,
        // vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strNumMonths);
        data = GenerateValuedKardexData.selectKardex(this, ad_org_id,
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
          data = GenerateValuedKardexData.set();
        } else {
          xmlDocument.setData("structure2", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = GenerateValuedKardexData.set();
      }
    }

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        String ad_org_id = strOrg;
        String warehouse_id = strWarehouse;
        dataINIT = GenerateValuedKardexData.selectKardexInitial(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataFINAL = GenerateValuedKardexData.selectKardexFinal(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataGENERAL = GenerateValuedKardexData.selectDataGeneral(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        dataQTYRESERVADO = GenerateValuedKardexData.selectStockReservado(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
        data = GenerateValuedKardexData.selectKardex(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), mProductId, strDocDate, strDateTo, warehouse_id);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }

      List<GenerateValuedKardexData> listData = new ArrayList<GenerateValuedKardexData>();
      if (data.length != 0) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
          Date dtt = format.parse(data[0].movementDate);
          int mesCurrent = dtt.getMonth();
          double entradas = 0, salidas = 0;
          String movementype = null;
          for (int i = 0; i < data.length; i++) {
            // System.out.println("COSTO: " + data[i].cost + " Costo Negativo: " +
            // data[i].costnegative);
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
              GenerateValuedKardexData gdata = new GenerateValuedKardexData();
              gdata.id = "saldo" + data[i].id;

              double qtyPos = Double.parseDouble(dataINIT[0].movementqty);
              double qtyNeg = Double.parseDouble(dataINIT[0].movementqtynegative);

              gdata.movementqtynegative = "<b>" + String.valueOf(salidas) + "</b>";
              gdata.movementqty = "<b>" + String.valueOf(entradas) + "</b>";

              gdata.name = "ACUM. DEL MES";
              gdata.productid = data[0].productid;
              gdata.movementDate = "<b>" + "ACUM. DEL MES" + "</b>";
              gdata.movementHour = "<b>" + getMes(mesCurrent) + "</b>";
              gdata.transactionID = "--";
              gdata.movementtype = "--";
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

            GenerateValuedKardexData gdata = new GenerateValuedKardexData();

            gdata.id = data[0].id;

            double qtyPos = Double.parseDouble(dataINIT[0].movementqty);
            double qtyNeg = Double.parseDouble(dataINIT[0].movementqtynegative);

            gdata.movementqtynegative = "<b>" + String.valueOf(salidas) + "</b>";
            gdata.movementqty = "<b>" + String.valueOf(entradas) + "<b>";

            gdata.name = "ACUM. DEL MES";
            gdata.productid = data[0].productid;
            gdata.movementDate = "<b>" + "ACUM. DEL MES" + "</b>";
            gdata.movementHour = "<b>" + getMes(mesCurrent) + "</b>";
            gdata.transactionID = "--";
            gdata.movementtype = "--";
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
        data = listData.toArray(new GenerateValuedKardexData[listData.size()]);
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
          data = GenerateValuedKardexData.set();
        } else {
          xmlDocument.setData("structure3", dataINIT);
          xmlDocument.setData("structure4", dataFINAL);
          xmlDocument.setData("structure7", dataGENERAL);
          xmlDocument.setData("structure8", dataQTYRESERVADO);
          xmlDocument.setData("structure2", data);

        }
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateValuedKardex", false, "",
          "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.setEmail(false);
      // toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.GenerateValuedKardex");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "GenerateValuedKardex.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateValuedKardex.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("GenerateValuedKardex");
        vars.removeMessage("GenerateValuedKardex");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "GenerateValuedKardex"), Utility.getContext(this, vars,
                "#User_Client", "GenerateValuedKardex"), 0);
        // comboTableData.fillParameters(null, "GenerateValuedKardex", "");
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateValuedKardex", strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "GenerateValuedKardex"), Utility.getContext(this, vars, "#User_Client",
                "GenerateValuedKardex"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateValuedKardex",
            strWarehouse);
        xmlDocument.setData("reportM_Warehouse_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
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

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter(
          "warehouseArray",
          Utility.arrayDobleEntrada(
              "arrWarehouse",
              GenerateValuedKardexData.selectWarehouseDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "GenerateValuedKardex"))));
      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("adOrgId", strOrg);
      xmlDocument.setParameter("SearchKey", strSearchKey);
      // xmlDocument.setParameter("productDescription",
      // GenerateValuedKardexData.selectMproduct(this, mProductId));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "GenerateValuedKardex"), Utility.getContext(this, vars,
                "#User_Client", "GenerateValuedKardex"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateValuedKardex", "");
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
