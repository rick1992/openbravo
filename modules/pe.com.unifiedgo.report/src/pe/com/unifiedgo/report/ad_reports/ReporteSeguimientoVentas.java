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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteSeguimientoVentas extends HttpSecureAppServlet {
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
      /**/
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "ReporteSeguimientoVentas|OrgId",
          "");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.today(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      String strCGlItemId = vars.getStringParameter("inpFinFinancialAccount");
      String strCBpartnerId = vars.getStringParameter("inpcBPartnerId");
      String strOnlyActive = vars.getStringParameter("inpOnlyActive", "N");
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo,
          strCGlItemId, strCBpartnerId, strOnlyActive);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      /**/
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.today(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      String strCGlItemId = vars.getStringParameter("inpFinFinancialAccount");
      String strCBpartnerId = vars.getStringParameter("inpcBPartnerId");
      String strOnlyActive = vars.getStringParameter("inpOnlyActive", "N");
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo,
          strCGlItemId, strCBpartnerId, strOnlyActive);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      /**/
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpProyDate");
      String strSalesRep = vars.getStringParameter("inpSalesRepId");
      String strCProjectId = vars.getStringParameter("inpcProjectId");
      /**/

      setHistoryCommand(request, "DEFAULT");

      //if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo, strSalesRep,
        		strCProjectId);
      //}

    } else {
      pageError(response);
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDateFrom, String strDateTo,
      String strSalesRep, String strCProjectId) throws IOException, ServletException {

    String strOutput;
    String strReportName;

    ReporteSeguimientoVentasData[] data = null;

    try {
      data = ReporteSeguimientoVentasData.selectData(this, strAD_Org_ID, strDateFrom, strDateTo,
    		  strSalesRep, strCProjectId);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
    }
    
    if(vars.commandIn("PDF")){
    	strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSeguimientoVentas.jrxml";
    }else{
    	strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteSeguimientoVentasExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    
    /* parameters */
    parameters.put("ORGANIZACION", ReporteSeguimientoVentasData.selectOrg(this, strAD_Org_ID));
    parameters.put("RUC", ReporteSeguimientoVentasData.selectRuc(this, strAD_Org_ID));
    parameters.put("strDateFrom", strDateFrom);
    parameters.put("strDateTo", strDateTo);

    renderJR(vars, response, strReportName, "Reporte_Seguimiento_Ventas", strOutput, parameters,
        data, null);
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

  private boolean isNumeric(String str) {
    try {
      double d = Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDocDate, String strProyDate,
      String glitemId, String cBpartnerId, String onlyActive) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteStockConsolidadoData[] data = null;

    ReporteSeguimientoVentasData[] dataItems = null;

    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteSeguimientoVentas", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        data = ReporteStockConsolidadoData.select(strAD_Org_ID,
            vars.getSessionValue("#AD_CLIENT_ID"), "", "");

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
          data = ReporteStockConsolidadoData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReporteStockConsolidadoData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteSeguimientoVentas", false,
          "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteSeguimientoVentas");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteSeguimientoVentas.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteSeguimientoVentas.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /* Llenar controles */

      /*
       * try {
       * 
       * ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
       * "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree",
       * "ReporteSeguimientoVentas"), Utility.getContext(this, vars, "#User_Client",
       * "ReporteSeguimientoVentas"), 0); comboTableData.fillParameters(null,
       * "ReporteSeguimientoVentas", ""); xmlDocument.setData("reportAD_Org_ID", "liststructure",
       * comboTableData.select(false)); } catch (Exception ex) { throw new ServletException(ex); }
       */

//      try {
//        dataItems = ReporteSeguimientoVentas.getGlItems(this, strAD_Org_ID);
//
//        FieldProvider items[] = new FieldProvider[dataItems.length + 1];
//        Vector<Object> vector = new Vector<Object>(0);
//
//        SQLReturnObject ini = new SQLReturnObject();
//        ini.setData("ID", "");
//        ini.setData("NAME", "");
//        vector.add(ini);
//
//        for (int i = 0; i < dataItems.length; i++) {
//          SQLReturnObject sqlReturnObject = new SQLReturnObject();
//          sqlReturnObject.setData("ID", dataItems[i].id);
//          sqlReturnObject.setData("NAME", dataItems[i].name);
//          vector.add(sqlReturnObject);
//        }
//        vector.copyInto(items);
//
//        xmlDocument.setData("reportFinFinancialAccount", "liststructure", items);
//
//        xmlDocument.setParameter("paramFinFinancialAccountArray",
//            Utility.arrayInfinitasEntradas("id;name;padre", "arrFinFinancialAccount", dataItems));
//        //
//        // System.out.println(Utility.arrayInfinitasEntradas("userid;username;userorgid",
//        // "arrUsuarios", datausers));
//
//      } catch (Exception ex) {
//        throw new ServletException(ex);
//      }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "c_glitem_id", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
       * "ReporteSeguimientoVentas"), Utility.getContext(this, vars, "#User_Client",
       * "ReporteSeguimientoVentas"), 0); Utility.fillSQLParameters(this, vars, null,
       * comboTableData, "ReporteSeguimientoVentas", glitemId);
       * xmlDocument.setData("reportFinFinancialAccount", "liststructure",
       * comboTableData.select(false)); comboTableData = null; } catch (Exception ex) { throw new
       * ServletException(ex); }
       */

//      xmlDocument.setParameter(
//          "FinFinancialAccountArray",
//          Utility.arrayDobleEntrada("arrFinFinancialAccount",
//              ReporteSeguimientoVentasData.getGlItems(this, strAD_Org_ID)));

      /* FIN Llenar controles */

      {
        OBError myMessage = vars.getMessage("ReporteSeguimientoVentas");
        vars.removeMessage("ReporteSeguimientoVentas");
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
        ReporteStockConsolidadoData[] dataorgs = ReporteStockConsolidadoData.select_organizaciones(
            this, vars.getSessionValue("#AD_CLIENT_ID"));

        FieldProvider organizaciones[] = new FieldProvider[dataorgs.length];
        Vector<Object> vector = new Vector<Object>(0);

        for (int i = 0; i < dataorgs.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", dataorgs[i].organizacionid);
          sqlReturnObject.setData("NAME", dataorgs[i].organizacion);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(organizaciones);

        xmlDocument.setData("reportAD_Org_ID", "liststructure", organizaciones);

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        // cargar tipo de almacen al html :3
        // vars.getSessionValue("#AD_CLIENT_ID") : para recuperar el id del cliente
        ReporteStockConsolidadoData[] dataTipoAlmacenes = ReporteStockConsolidadoData
            .select_tipos_almacenes(this, vars.getSessionValue("#AD_CLIENT_ID"));

        FieldProvider TiposAlmacenes[] = new FieldProvider[dataTipoAlmacenes.length + 1];
        Vector<Object> vector = new Vector<Object>(0);

        SQLReturnObject sqlReturnObject = new SQLReturnObject();
        sqlReturnObject.setData("ID", "");
        sqlReturnObject.setData("NAME", "-- TODOS LOS ALMACENES");
        vector.add(sqlReturnObject);

        for (int i = 0; i < dataTipoAlmacenes.length; i++) {
          sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", dataTipoAlmacenes[i].codigo);
          sqlReturnObject.setData("NAME", dataTipoAlmacenes[i].detalle);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(TiposAlmacenes);

        xmlDocument.setData("reportWAREHOUSE_TYPE_ID", "liststructure", TiposAlmacenes);

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (true) {
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

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
