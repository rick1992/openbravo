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
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteInventarioValorizadoProductoTerminado extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " + session.getAttribute(name));
    // }

    if (vars.commandIn("DEFAULT")) {
      String strDocDate = vars.getGlobalVariable("inpDocDate",
          "ReporteInventarioValorizadoProductoTerminado|docDate", SREDateTimeData.today(this));
      String strProyDate = vars.getGlobalVariable("inpProyDate",
          "ReporteInventarioValorizadoProductoTerminado|proyDate", SREDateTimeData.tomorrow(this));
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteInventarioValorizadoProductoTerminado|AD_Org_ID", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReporteInventarioValorizadoProductoTerminado|CB_PARTNER_ID", "");
      String strcPeriodId = vars.getGlobalVariable("inpPeriodo",
          "ReporteInventarioValorizadoProductoTerminado|C_PERIOD_ID", "");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strcBpartnetId,
          strcPeriodId, strAD_Org_ID, true);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {

      String strDocDate = vars.getStringParameter("inpDocDate");
      String strProyDate = vars.getStringParameter("inpProyDate");
      String strAD_Org_ID = vars.getStringParameter("inpadOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strcPeriodId = vars.getStringParameter("inpPeriodo");

      printPageDataSheet(request, response, vars, strDocDate, strProyDate, strcBpartnetId,
          strcPeriodId, strAD_Org_ID, false);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como pdf o excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strMWarehouseID = vars.getStringParameter("inpmWarehouseId");
      String strProyDate = vars.getRequestGlobalVariable("inpProyDate",
          "ReporteInventarioValorizadoProductoTerminado|proyDate");
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "ReporteInventarioValorizadoProductoTerminado|AD_Org_ID", "0");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReporteInventarioValorizadoProductoTerminado|CB_PARTNER_ID", "");
      String strcProductGroup = vars.getGlobalVariable("inpProductLine",
          "ReporteInventarioValorizadoProductoTerminado|C_PERIOD_ID", "");

      String strSinStock = vars.getStringParameter("inpSinStock");

      String strWithStock = vars.getStringParameter("inpWithStock");

      String strGroupWarehouse = vars.getStringParameter("inpGroupWarehouse");

      setHistoryCommand(request, "DEFAULT");

      printPagePDF(request, response, vars, strMWarehouseID, strProyDate, "", strcProductGroup,
          strAD_Org_ID, strSinStock, strGroupWarehouse, strWithStock);

      // if (vars.commandIn("PDF")) {
      //
      // } else {
      // printPageXLS(response, vars, strMWarehouseID, strProyDate, strcProductGroup, strAD_Org_ID);
      // }
    } else {
      pageError(response);
    }
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strMWarehouseID, String strProyDate, String strcBpartnetId,
      String strcPeriodId, String strAD_Org_ID, String strSinStock, String strGroupWarehouse,
      String strWithStock) throws IOException, ServletException {

    ReporteInventarioValorizadoProductoTerminadoData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strAD_Org_ID);

    System.out.println("strGroupWarehouse: " + strGroupWarehouse);

    String strOutput = "";
    String strReportName = "";

    try {

      if (strGroupWarehouse.compareToIgnoreCase("Y") == 0) {
        data = ReporteInventarioValorizadoProductoTerminadoData.selectWarehouse(strOrgFamily,
            vars.getSessionValue("#AD_CLIENT_ID"), strMWarehouseID, strProyDate, strcBpartnetId,
            strcPeriodId, vars.getSessionValue("#AD_USER_ID"), strWithStock, "N");

        if (vars.commandIn("PDF")) {
          strOutput = "pdf";
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminadoConAlmacen.jrxml";
        } else {
          strOutput = "xls";
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminadoConAlmacenExcel.jrxml";
        }
      } else {
        data = ReporteInventarioValorizadoProductoTerminadoData.selectTodos(strOrgFamily,
            vars.getSessionValue("#AD_CLIENT_ID"), strMWarehouseID, strProyDate, strcBpartnetId,
            strcPeriodId, vars.getSessionValue("#AD_USER_ID"), strWithStock, "N");

        if (vars.commandIn("PDF")) {
          strOutput = "pdf";
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminado.jrxml";
        } else {
          strOutput = "xls";
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminadoExcel.jrxml";
        }
      }

    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }


    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strAD_Org_ID) + "\n";
    ;

    if (!("0".equals(strAD_Org_ID)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strAD_Org_ID) + "\n";

    // if (!"".equals(strDocDate) || !"".equals(strProyDate))
    // strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDocDate
    // + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strProyDate
    // + "\n";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION",
        ReporteInventarioValorizadoProductoTerminadoData.selectOrganization(this, strAD_Org_ID));
    parameters.put("RUC_ORGANIZACION",
        ReporteInventarioValorizadoProductoTerminadoData.selectRucOrganization(this, strAD_Org_ID));
    // parameters.put("TERCERO",
    // ReporteInventarioValorizadoProductoTerminadoData.selectBPartner(this, strcBpartnetId));
    parameters.put("PERIODO", ReporteInventarioValorizadoProductoTerminadoData.selectPeriodo(this,
        strProyDate, strAD_Org_ID));
    // parameters.put("DESDE", StringToDate(strDocDate));
    parameters.put("HASTA", StringToDate(strProyDate));
    parameters.put("CON_STOCK", strWithStock.toString());

    renderJR(vars, response, strReportName, "Reporte_Inventario_Valorizado_Producto_Terminado",
        strOutput, parameters, data, null);
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild);

  }

  private void printPageXLS(HttpServletResponse response, VariablesSecureApp vars,
      String strDocDate, String strProyDate, String strC_BPartner_ID, String strAD_Org_ID)
      throws IOException, ServletException {

    ReporteInventarioValorizadoProductoTerminadoData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strAD_Org_ID) + "\n";
    ;

    if (!("0".equals(strAD_Org_ID)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strAD_Org_ID) + "\n";

    if (!"".equals(strDocDate) || !"".equals(strProyDate))
      strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDocDate
          + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strProyDate
          + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminado.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminadoExcel.jrxml";
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("ORGANIZACION",
        ReporteInventarioValorizadoProductoTerminadoData.selectOrganization(this, strAD_Org_ID));
    parameters.put("RUC_ORGANIZACION",
        ReporteInventarioValorizadoProductoTerminadoData.selectRucOrganization(this, strAD_Org_ID));
    parameters.put("TERCERO",
        ReporteInventarioValorizadoProductoTerminadoData.selectBPartner(this, strAD_Org_ID));
    parameters.put("PERIODO", ReporteInventarioValorizadoProductoTerminadoData.selectPeriodo(this,
        strDocDate, strAD_Org_ID));
    parameters.put("DESDE", StringToDate(strDocDate));
    parameters.put("HASTA", StringToDate(strProyDate));

    renderJR(vars, response, strReportName, "Reporte_Inventario_Valorizado_Producto_Terminado",
        strOutput, parameters, data, null);
  }

  private Date StringToDate(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    Date date;
    try {
      if (!strDate.equals("")) {
        date = formatter.parse(strDate);
        return date;
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static StructPle getSaldoCuenta20(String strClientId, Date dateTo, Organization org,
      String strOrgFamily) throws Exception {

    StringBuffer sb = new StringBuffer();
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    // get this year uit
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(dateTo);
    int year = caldttTo.get(Calendar.YEAR);

    // get first day of year
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Date dateFrom = cal.getTime();

    String period = dt.format(dateTo); // Periodo
    String dateToValidation = df.format(dateTo);
    // System.out.println("dateToValidation: " + dateToValidation);

    String catolgCode = "9"; // table13
    String assetType = "01"; // table05
    String codeNoMandatory = "";
    String codeEvaluationMethod = "1"; // table14
    String operationStatus = "1";

    /* GET DATA */
    ReporteInventarioValorizadoProductoTerminadoData[] data = null;
    data = ReporteInventarioValorizadoProductoTerminadoData.selectTodos(strOrgFamily, strClientId,
        "", dateToValidation, "", "", "", "Y", "Y");

    for (int i = 0; i < data.length; i++) {

      ReporteInventarioValorizadoProductoTerminadoData info = data[i];
      String strProductValue = info.codigo; // Codigo del Producto
      assetType = info.codetable5; // Codigo Tabla 5
      String strProductDescription = info.descripcion; // Descricpion del Producto
      if (strProductDescription.length() > 80) {
        strProductDescription = strProductDescription.substring(0, 79);
      }

      String strProductUoM = info.uomsymbol; // Unidad de Medida

      BigDecimal stock = info.stock;
      BigDecimal costoTotal = info.costoTotal;
      BigDecimal unitCost = stock.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : costoTotal.divide(
          stock, RoundingMode.HALF_UP);

      String strStockActual = stock.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
      String strUnitCost = unitCost.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
      String strTotalCost = costoTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

      String linea = period + "|" + catolgCode + "|" + assetType + "|" + strProductValue + "|"
          + codeNoMandatory + "|" + strProductDescription + "|" + strProductUoM + "|"
          + codeEvaluationMethod + "|" + strStockActual + "|" + strUnitCost + "|" + strTotalCost
          + "|" + operationStatus + "|";

      if (!sb.toString().equals(""))
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;

    }

    sunatPle.data = sb.toString();
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    String filename = "LE" + rucAdq + dt.format(dateTo) + "030700011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030700CCOIM1.TXT

    sunatPle.filename = filename;

    return sunatPle;
  }

  private boolean isNumeric(String str) {
    try {
      double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDocDate, String strProyDate, String strcBpartnetId,
      String strcPeriodId, String strAD_Org_ID, boolean isFirstLoad) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteInventarioValorizadoProductoTerminadoData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteInventarioValorizadoProductoTerminado", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReporteInventarioValorizadoProductoTerminadoData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strProyDate, strcBpartnetId,
            strcPeriodId, vars.getSessionValue("#AD_USER_ID"));

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
          data = ReporteInventarioValorizadoProductoTerminadoData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteInventarioValorizadoProductoTerminadoData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(),
          "ReporteInventarioValorizadoProductoTerminado", false, "", "",
          "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);

      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteInventarioValorizadoProductoTerminado");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteInventarioValorizadoProductoTerminado.html", classInfo.id, classInfo.type,
            strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteInventarioValorizadoProductoTerminado.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReporteInventarioValorizadoProductoTerminado");
        vars.removeMessage("ReporteInventarioValorizadoProductoTerminado");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDate", strProyDate);
      xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "ReporteInventarioValorizadoProductoTerminado"),
            Utility.getContext(this, vars, "#User_Client",
                "ReporteInventarioValorizadoProductoTerminado"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData,
            "ReporteInventarioValorizadoProductoTerminado", strAD_Org_ID);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;

        xmlDocument.setParameter("ProductLine", strcPeriodId);
        xmlDocument.setParameter("productLineDescription",
            ReporteInventarioValorizadoProductoTerminadoData.selectPrdcProductgroup(this,
                strcPeriodId));

        ReporteInventarioValorizadoProductoTerminadoData[] datax = ReporteInventarioValorizadoProductoTerminadoData
            .select_periodos(this);

        FieldProvider periodos[] = new FieldProvider[datax.length];
        Vector<Object> vector = new Vector<Object>(0);

        for (int i = 0; i < datax.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", datax[i].idperiodo);
          sqlReturnObject.setData("NAME", datax[i].periodo);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(periodos);

        xmlDocument.setData("reportC_PERIODO", "liststructure", periodos);

        xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas(
            "idperiodo;periodo;idorganizacion", "arrPeriodos",
            ReporteInventarioValorizadoProductoTerminadoData.select_periodos(this)));

        // System.out.println(Utility.arrayInfinitasEntradas("idperiodo;periodo", "arrPeriodos",
        // ReporteInventarioValorizadoProductoTerminadoData.select_periodos(this)));

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (isFirstLoad) {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }

      xmlDocument.setParameter("adOrg", strAD_Org_ID);

      // xmlDocument.setParameter("mWarehouseId", strWarehouse);
      // xmlDocument.setParameter("mWarehouseDescription",
      // GenerateAnaliticKardexData.selectMWarehouse(this, strWarehouse));

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
