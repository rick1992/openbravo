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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.core.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.costing.CostingStatus;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportSalesParetoProduct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Get user Client's base currency
    // String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "ReportSalesParetoProduct|M_Warehouse_ID", "");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReportSalesParetoProduct|AD_Org_ID", "");

      // HASTA
      String strDateto = vars.getGlobalVariable("inpDateTo", "ReportSalesParetoProduct|inpDateTo",
          SREDateTimeData.today(this));

      // DESDE TRES MESES ATRÀS
      String strDocDate = vars.getGlobalVariable("inpDocDate", "ReportSalesParetoProduct|docDate",
          SREDateTimeData.threeMonthsBefore(this, strDateto));

      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient, true,
          strDocDate, strDateto);

    } else if (vars.commandIn("FIND")) {
      String strWarehouse = vars.getStringParameter("inpmWarehouseId");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "ReportSalesParetoProduct|AD_Org_ID");
      // String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
      // "ReportSalesParetoProduct|currency", strUserCurrencyId);
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      System.out.println("strWarehouse:" + strWarehouse);

      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient, false,
          strDocDate, strDateTo);

    } else if (vars.commandIn("GENERATE")) {
      String strClient = vars.getClient();
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportSalesParetoProduct|M_Warehouse_ID");
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "ReportSalesParetoProduct|AD_Org_ID");

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");

      int tmp = 0;
      OBError myMessage = null;
      try {
        ReportSalesParetoProductData.updateInitialABC(this, strClient, strAD_Org_ID);
      } catch (Exception e) {
        tmp = 1;
        myMessage.setTitle("");
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        vars.setMessage("ReportSalesParetoProduct", myMessage);
      }

      if (tmp == 0) {
        myMessage = mUpdateParetoProduct(request, response, vars, strWarehouse, strAD_Org_ID,
            strClient, strDocDate, strDateTo);
        myMessage.setTitle("");
        myMessage.setType("Success");
        myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
        vars.setMessage("ReportSalesParetoProduct", myMessage);
      }

      printPageDataSheet(request, response, vars, strWarehouse, strAD_Org_ID, strClient, false,
          strDocDate, strDateTo);

    }

    else if (vars.commandIn("PDF", "XLS")) {

      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "ReportSalesParetoProduct|AD_Org_ID");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "ReportSalesParetoProduct|M_Warehouse_ID");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strDocDate, strDateTo, strWarehouse, strAD_Org_ID,
            strClient);
      }
      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strDocDate, strDateTo, strWarehouse, strAD_Org_ID,
            strClient);
      }

      /*System.out.println("organizacion");
      System.out.println(strAD_Org_ID);
      System.out.println("almacen");
      System.out.println(strWarehouse);
      System.out.println("fechainicio");
      System.out.println(strDocDate);
      System.out.println("fechafin");
      System.out.println(strDateTo);*/

    }

    else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String strOrg, String strClient) throws IOException, ServletException {

    OBError myMessage = null;
    myMessage = new OBError();

    ReportSalesParetoProductData[] dataOrganizacion = null;
    ReportSalesParetoProductData[] dataAlmacen = null;

    ReportSalesParetoProductData[] data = null;

    try {
      dataOrganizacion = ReportSalesParetoProductData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), "", strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = ReportSalesParetoProductData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), "", strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = ReportSalesParetoProductData.selectReportData(this, strWarehouse, strClient,
          vars.getLanguage(), strOrg, strDocDate, strDateto);
    } catch (ServletException ex) {
      data = ReportSalesParetoProductData.set();
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    System.out.println("data");
    System.out.println(data.length);

    String strReportName;
    String strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/core/ad_reports/ReportSalesParetoProduct.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION", dataOrganizacion.length == 0 ? ""
        : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    BigDecimal acumulado;

    renderJR(vars, response, strReportName, "Report_Pareto_Warehouse", strOutput, parameters, data,
        null);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strDateto, String strWarehouse,
      String strOrg, String strClient) throws IOException, ServletException {

    ReportSalesParetoProductData[] dataOrganizacion = null;
    ReportSalesParetoProductData[] dataAlmacen = null;

    ReportSalesParetoProductData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      dataOrganizacion = ReportSalesParetoProductData.selectOrganizacion(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), "", strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      dataAlmacen = ReportSalesParetoProductData.selectAlmacen(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), "", strDocDate, strDateto, strWarehouse);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    try {
      data = ReportSalesParetoProductData.selectReportData(this, strWarehouse, strClient,
          vars.getLanguage(), strOrg, strDocDate, strDateto);
    } catch (ServletException ex) {
      data = ReportSalesParetoProductData.set();
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    String strOutput = "xls";
    String strReportName;

    strReportName = "@basedesign@/pe/com/unifiedgo/core/ad_reports/ReportSalesParetoProductExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ORGANIZACION", dataOrganizacion.length == 0 ? ""
        : dataOrganizacion[0].organizacion);
    parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" : dataOrganizacion[0].ruc);
    parameters.put("ALMACEN", dataAlmacen.length == 0 ? "" : dataAlmacen[0].almacen);

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    BigDecimal acumulado;

    renderJR(vars, response, strReportName, "Report_Pareto_Warehouse_Excel", strOutput, parameters,
        data, null);

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strWarehouse, String strAD_Org_ID, String strClient,
      boolean isFirstLoad, String strDocDate, String strDateTo) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportSalesParetoProductData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    // If the instance is not migrated the user should use the Legacy report
    if (CostingStatus.getInstance().isMigrated() == false) {
      advise(request, response, "ERROR", OBMessageUtils.messageBD("NotUsingNewCost"), "");
      return;
    }

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/core/ad_reports/ReportSalesParetoProduct", discard).createXmlDocument();
    if (vars.commandIn("FIND")) {
      // Checks if there is a conversion rate for each of the transactions
      // of the report
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReportSalesParetoProductData.selectReportData(this, strWarehouse, strClient,
            vars.getLanguage(), strAD_Org_ID, strDocDate, strDateTo);
      } catch (ServletException ex) {
        data = ReportSalesParetoProductData.set();
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = ReportSalesParetoProductData.set();
        } else {
          // Apply differences in percentages applying difference to bigger percentage
          BigDecimal total = BigDecimal.ZERO;
          BigDecimal difference = BigDecimal.ZERO;
          int toAdjustPosition = 0;
          String currentOrganization = data[0].orgid;
          for (int i = 0; i < data.length; i++) {
            if (data[i].orgid.equals(currentOrganization)) {
              total = total.add(new BigDecimal(data[i].percentage)).setScale(2,
                  BigDecimal.ROUND_HALF_UP);
            } /*
               * else { difference = new BigDecimal("100.00").subtract(total); total = new
               * BigDecimal(data[i].percentage).setScale(2, BigDecimal.ROUND_HALF_UP);
               * data[toAdjustPosition].percentage = new
               * BigDecimal(data[toAdjustPosition].percentage) .add(difference).setScale(2,
               * BigDecimal.ROUND_HALF_UP).toString(); toAdjustPosition = i; currentOrganization =
               * data[i].orgid; }
               */
          }
          // Update last group
          /*
           * difference = new BigDecimal("100.00").subtract(total);
           * data[toAdjustPosition].percentage = new BigDecimal(data[toAdjustPosition].percentage)
           * .add(difference).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
           */

          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportSalesParetoProductData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesParetoProduct", false,
          "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // Create WindowTabs
      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.core.ad_reports.ReportSalesParetoProduct");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportSalesParetoProduct.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportSalesParetoProduct.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      // Load Message Area
      {
        OBError myMessage = vars.getMessage("ReportSalesParetoProduct");
        vars.removeMessage("ReportSalesParetoProduct");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      // Pass parameters to the window
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      // Load Business Partner Group combo with data
      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
      // "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
      // "ReportSalesParetoProduct"), Utility.getContext(this, vars, "#User_Client",
      // "ReportSalesParetoProduct"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesParetoProduct",
      // "");
      // xmlDocument.setData("reportM_Warehouse_ID", "liststructure", comboTableData.select(false));
      // comboTableData = null;
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReportSalesParetoProduct"), Utility.getContext(this, vars,
                "#User_Client", "ReportSalesParetoProduct"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesParetoProduct",
            strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("mWarehouseDescription",
          ReportSalesParetoProductData.selectMWarehouse(this, strWarehouse));

      String nada = ReportSalesParetoProductData.selectMWarehouse(this, strWarehouse);

      // xmlDocument.setParameter("ccurrencyid", strCurrencyId);
      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
      // "", "",
      // Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesParetoProduct"),
      // Utility.getContext(this, vars, "#User_Client", "ReportSalesParetoProduct"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "ReportSalesParetoProduct",
      // strCurrencyId);
      // xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
      // comboTableData = null;
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }

      // xmlDocument.setParameter(
      // "warehouseArray",
      // Utility.arrayDobleEntrada(
      // "arrWarehouse",
      // ReportSalesParetoProductData.selectMWarehouseForChildOrgDouble(this,
      // Utility.getContext(this, vars, "#User_Client", "ReportSalesParetoProduct"))));

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private OBError mUpdateParetoProduct(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strWarehouse, String strAD_Org_ID, String strClient,
      String strDocDate, String strDateTo) throws IOException, ServletException {
    ReportSalesParetoProductData[] data = null;
    String strConvRateErrorMsg = "";

    OBError myMessage = new OBError();
    try {
      data = ReportSalesParetoProductData.selectReportData(this, strWarehouse, strClient,
          vars.getLanguage(), strAD_Org_ID, strDocDate, strDateTo);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    strConvRateErrorMsg = myMessage.getMessage();

    if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "NoConversionRateHeader");
    } else { // Otherwise, the report is launched
      if (data == null || data.length == 0) {
        data = ReportSalesParetoProductData.set();
      } else {
        // Apply differences in percentages applying difference to bigger percentage
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal difference = BigDecimal.ZERO;
        int toAdjustPosition = 0;
        String currentOrganization = data[0].orgid;
        for (int i = 0; i < data.length; i++) {
          if (data[i].orgid.equals(currentOrganization)) {
            total = total.add(new BigDecimal(data[i].percentage)).setScale(2,
                BigDecimal.ROUND_HALF_UP);
          } else {
            difference = new BigDecimal("100.00").subtract(total);
            total = new BigDecimal(data[i].percentage).setScale(2, BigDecimal.ROUND_HALF_UP);
            data[toAdjustPosition].percentage = new BigDecimal(data[toAdjustPosition].percentage)
                .add(difference).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            toAdjustPosition = i;
            currentOrganization = data[i].orgid;
          }
        }
        // Update last group
        difference = new BigDecimal("100.00").subtract(total);
        data[toAdjustPosition].percentage = new BigDecimal(data[toAdjustPosition].percentage)
            .add(difference).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
      }
    }

    // Updating Product ABC Category
    Product product;
    for (int i = 0; i < data.length; i++) {
      product = OBDal.getInstance().get(Product.class, data[i].id);
      product.setScrAbc(data[i].isabc);
      OBDal.getInstance().save(product);
    }
    return myMessage;
  }

  private OBError mUpdateParetoProduct_bk(VariablesSecureApp vars, String strWarehouse,
      String strAD_Org_ID, String strAD_Client_ID) throws IOException, ServletException {
    String pinstance = SequenceIdData.getUUID();

    PInstanceProcessData.insertPInstance(this, pinstance, "1000500001", "0", "N", vars.getUser(),
        vars.getClient(), vars.getOrg());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "1", "m_warehouse_id", strWarehouse,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "2", "ad_org_id", strAD_Org_ID,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "3", "ad_client_id",
        strAD_Client_ID, vars.getClient(), vars.getOrg(), vars.getUser());
    ReportSalesParetoProductData.mUpdateParetoProduct0(this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    OBError myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    return myMessage;
  }

  public String getServletInfo() {
    return "Servlet ReportSalesParetoProduct info. Insert here any relevant information";
  } // end of getServletInfo() method
}
