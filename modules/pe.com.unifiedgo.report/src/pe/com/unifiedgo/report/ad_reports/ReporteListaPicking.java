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
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReporteListaPicking extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String OrderID = null;

  private static final String CHILD_SHEETS = "frameWindowTreeF3"; // tree

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "ReporteListaPicking|Org", "");
      String physicalNumber = vars.getGlobalVariable("inpPhysicalNumber",
          "ReporteListaPicking|PhysicalNumber", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "ReporteListaPicking|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "ReporteListaPicking|dateTo",
          SREDateTimeData.today(this));
      String stradUserId = vars
          .getGlobalVariable("inpadUserId", "ReporteListaPicking|adUserId", "");// Pickero
      String groupByPickero = vars.getGlobalVariable("inpAgrupadoPorPickero",
          "ReporteListaPicking|inpAgrupadoPorPickero", "");// Agrupar por
      // pickero
      String showLines = vars.getGlobalVariable("inpMostrarDetalles",
          "ReporteListaPicking|MostrarDetalles", "");// Mostrar lineas de picking

      printPageDataSheet(request, response, vars, strOrg, physicalNumber, strDocDate, strDateto,
          stradUserId, groupByPickero, showLines);

    } else if (vars.commandIn("LISTAR")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String physicalNumber = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String stradUserId = vars.getStringParameter("inpcBPartnerId");
      String groupByPickero = vars.getStringParameter("inpmProductId");
      String showLines = vars.getStringParameter("paramProductOrder");

      printPageDataSheet(request, response, vars, strOrg, physicalNumber, strDocDate, strDateTo,
          stradUserId, groupByPickero, showLines);
    } else if (vars.commandIn("PDF", "XLS")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String physicalNumber = vars.getStringParameter("inpPhysicalNumber");
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String stradUserId = vars.getStringParameter("inpadUserId");// Pickero
      String groupByPickero = vars.getStringParameter("inpAgrupadoPorPickero");// Agrupar por
                                                                               // pickero
      String showLines = vars.getStringParameter("inpMostrarDetalles");// Mostrar lineas de picking

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("PDF")) {
        printPagePDF(request, response, vars, strOrg, physicalNumber, strDateFrom, strDateTo,
            stradUserId, groupByPickero, showLines);
      }

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strOrg, physicalNumber, strDateFrom, strDateTo,
            stradUserId, groupByPickero, showLines);
      }

    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String physicalNumber, String strDateFrom,
      String strDateTo, String stradUserId, String groupByPickero, String showLines)
      throws IOException, ServletException {

    ReporteListaPickingData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    Date fecha = null;

    String strOutput;
    String strReportName;

    // System.out.println("PDF");
    // System.out.println(strOrg);
    // System.out.println(physicalNumber);
    // System.out.println(strDateFrom);
    // System.out.println(strDateTo);
    // System.out.println(stradUserId);
    // System.out.println(groupByPickero);
    // System.out.println(showLines);

    try {
      data = ReporteListaPickingData.select(this, vars.getSessionValue("#AD_CLIENT_ID"), strOrg,
          physicalNumber, strDateFrom, strDateTo, stradUserId, groupByPickero, showLines);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
      data = ReporteListaPickingData.set();
    }
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }


    strOutput = "pdf";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteListaPicking.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    OBCriteria<Organization> organization = OBDal.getInstance().createCriteria(Organization.class);
    organization.add(Restrictions.eq(Organization.PROPERTY_ID, strOrg));

    List<Organization> OrganizationList = organization.list();

    parameters.put("ORGANIZACION", OrganizationList.get(0).getSocialName());

    parameters.put("FECINICIO", StringToDate(strDateFrom));
    parameters.put("FECFIN", StringToDate(strDateTo));
    parameters.put("NUMGUIA", physicalNumber);
    parameters.put("MOSTRARLINEAS", showLines.toString());
    parameters.put("AGRUPARPORPICKERO", groupByPickero.toString());

    renderJR(vars, response, strReportName, "Reporte_Lista_Picking", strOutput, parameters, data,
        null);

  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String physicalNumber, String strDateFrom,
      String strDateTo, String stradUserId, String groupByPickero, String showLines)
      throws IOException, ServletException {

    ReporteListaPickingData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    String strOutput;
    String strReportName;

    // System.out.println("XLS");
    // System.out.println(strOrg);
    // System.out.println(physicalNumber);
    // System.out.println(strDateFrom);
    // System.out.println(strDateTo);
    // System.out.println(stradUserId);
    // System.out.println(groupByPickero);
    // System.out.println(showLines);

    try {
      data = ReporteListaPickingData.select(this, vars.getSessionValue("#AD_CLIENT_ID"), strOrg,
          physicalNumber, strDateFrom, strDateTo, stradUserId, groupByPickero, showLines);
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
      data = ReporteListaPickingData.set();
    }
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }


    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteListaPickingExcel.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    OBCriteria<Organization> organization = OBDal.getInstance().createCriteria(Organization.class);
    organization.add(Restrictions.eq(Organization.PROPERTY_ID, strOrg));

    List<Organization> OrganizationList = organization.list();

    parameters.put("ORGANIZACION", OrganizationList.get(0).getSocialName());

    parameters.put("FECINICIO", StringToDate(strDateFrom));
    parameters.put("FECFIN", StringToDate(strDateTo));
    parameters.put("NUMGUIA", physicalNumber);
    parameters.put("MOSTRARLINEAS", showLines.toString());
    parameters.put("AGRUPARPORPICKERO", groupByPickero.toString());

    renderJR(vars, response, strReportName, "Reporte_Lista_Picking", strOutput, parameters, data,
        null);

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

  //
  //
  //
  //
  //
  //
  //

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String physicalNumber, String strDateFrom,
      String strDateTo, String stradUserId, String groupByPickero, String showLines)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    ReporteListaPickingData[] datausers = null;

    ReporteListaPickingData[] dataorgs = null;

    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteListaPicking", discard).createXmlDocument();

    if (vars.commandIn("DEFAULT")) {
      // xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteListaPicking", false, "", "",
          "printPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteListaPicking");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReporteListaPicking.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReporteListaPicking.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        dataorgs = ReporteListaPickingData.selectOrgs(this, vars.getSessionValue("#AD_CLIENT_ID"),
            strOrg, physicalNumber, strDateFrom, strDateTo, stradUserId, groupByPickero);

        FieldProvider orgs[] = new FieldProvider[dataorgs.length];
        Vector<Object> vector = new Vector<Object>(0);

        for (int i = 0; i < dataorgs.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", dataorgs[i].orgid);
          sqlReturnObject.setData("NAME", dataorgs[i].orgname);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(orgs);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", orgs);
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        datausers = ReporteListaPickingData.selectUsers2(this);

        FieldProvider usuarios[] = new FieldProvider[datausers.length];
        Vector<Object> vector = new Vector<Object>(0);

        for (int i = 0; i < datausers.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("ID", datausers[i].userid);
          sqlReturnObject.setData("NAME", datausers[i].username);
          vector.add(sqlReturnObject);
        }
        vector.copyInto(usuarios);

        xmlDocument.setData("reportUsers", "liststructure", usuarios);

         xmlDocument.setParameter("paramUsuariosArray",
         Utility.arrayInfinitasEntradas("userid;username;userorgid", "arrUsuarios", datausers));
        //
        // System.out.println(Utility.arrayInfinitasEntradas("userid;username;userorgid",
        // "arrUsuarios", datausers));

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      //
      //

      {
        OBError myMessage = vars.getMessage("ReporteListaPicking");
        vars.removeMessage("ReporteListaPicking");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
      // "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
      // "#AccessibleOrgTree", "ReporteListaPicking"), Utility.getContext(this, vars,
      // "#User_Client", "ReporteListaPicking"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteListaPicking", strOrg);
      // xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("docDate", strDateFrom);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("adOrgId", strOrg);

      // xmlDocument.setParameter("paramBPartnerId", strBpartnetId);
      // xmlDocument.setParameter("paramBPartnerDescription",
      // ReporteListaPickingData.selectBpartner(this, strBpartnetId));
      // xmlDocument.setParameter("adToOrgId", strToOrg);

      // xmlDocument.setParameter("mProduct", mProductId);
      // xmlDocument.setParameter("productDescription",
      // ReporteListaPickingData.selectMproduct(this, mProductId));

      // /xmlDocument.setParameter("paramProductOrder", mStrNumOrder);

      // xmlDocument.setParameter("menu", loadNodes(vars, "test"));
      // xmlDocument.setParameter("menu", "TEST");

      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
      // "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
      // "#AccessibleOrgTree", "ReporteListaPicking"), Utility.getContext(this, vars,
      // "#User_Client", "ReporteListaPicking"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "ReporteListaPicking", "");
      // xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
      // comboTableData = null;
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
