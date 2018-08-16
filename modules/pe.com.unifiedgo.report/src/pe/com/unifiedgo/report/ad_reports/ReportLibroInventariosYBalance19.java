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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportLibroInventariosYBalance19 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance19|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance19|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance19|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance19|Record", "");
      String strTable = vars.getGlobalVariable("inpTable",
          "ReportLibroInventariosYBalance19|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars
          .getGlobalVariable("inpTable", "ReportLibroInventariosYBalance19|Table");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance19|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportLibroInventariosYBalance19.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance19|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance19|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance19|Org", "0");
      vars.setSessionValue("ReportLibroInventariosYBalance19.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strOrg = vars.getStringParameter("inpOrg");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request,response, vars, strDateFrom, strDateTo, strOrg);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strTable, String strRecord)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportLibroInventariosYBalance19Data[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance19",
        false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    
    toolbar.prepareSimpleToolBarTemplate();

	toolbar.prepareRelationBarTemplate(false, false,
			"imprimirXLS();return false;");
	
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
//      toolbar
//          .prepareRelationBarTemplate(
//              false,
//              false,
//              "submitCommandForm('XLS', false, null, 'ReportLibroInventariosYBalance19.xls', 'EXCEL');return false;");
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance19", discard)
          .createXmlDocument();
      data = ReportLibroInventariosYBalance19Data.set("0");
      data[0].rownum = "0";
    } 
    
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance19");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance19.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance19.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance19");
      vars.removeMessage("ReportLibroInventariosYBalance19");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "49DC1D6F086945AB82F84C66F5F13F16", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "ReportLibroInventariosYBalance19"), Utility.getContext(this, vars, "#User_Client",
              "ReportLibroInventariosYBalance19"), '*');
      comboTableData.fillParameters(null, "ReportLibroInventariosYBalance19", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData(
        "reportC_ACCTSCHEMA_ID",
        "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    vars.setSessionValue("ReportLibroInventariosYBalance19|Record", strRecord);
    vars.setSessionValue("ReportLibroInventariosYBalance19|Table", strTable);
    
	xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion","arrPeriodos",
			ReportLibroInventariosYBalance19Data
		.select_periodos(this)));


    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletRequest request,HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg)
      throws IOException, ServletException {
	  
    ReportLibroInventariosYBalance19Data[] data = null;

    data = ReportLibroInventariosYBalance19Data.getSaldoCuenta19V2(this, strDateTo, strOrg);
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportLibroInventariosYBalance19Data.selectCompany(this, vars.getClient()) + "\n"
        + "RUC:" + ReportLibroInventariosYBalance19Data.selectRucOrg(this, strOrg) + "\n";

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportLibroInventariosYBalance19Data.selectOrg(this, strOrg) + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance19.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance19Excel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReportLibroInventariosYBalance10Data.selectRucOrg(this, strOrg));
    parameters.put("Razon",
        ReportLibroInventariosYBalance10Data.selectCompany(this, vars.getClient()));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "LibroIB_Cuenta19", strOutput, parameters, data, null);
  }
  
  public static StructPle getDataPLECuenta19(
	      ReportLibroInventariosYBalance19Data[] data, Date dateFrom, Date dateTo, String strOrg)
	      throws Exception {

	    StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;

	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    String period = dt.format(dateTo);
	    
	    String OpStatus = "1";

	    for (int i = 0; i < data.length; i++) {

	      ReportLibroInventariosYBalance19Data doc = data[i];

	      String cuo = doc.cuo;

	      if (cuo == null || cuo.equals(""))
	        continue;	      
	      
	      String nroCorrelativo = doc.correlativo;
	      String bpDocumentType = doc.bpDocumentType;
	      String bpDocumentNumber = doc.taxid;
	      String CDPType = doc.cdptype;
	      
	      String strserie = "";
	      String strCorrelativo = doc.correlativo;
	      
	      String bpName = doc.name;
	      

	      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
	      Date prev_dateacct = df.parse(doc.dateacct);

	      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

	      double amtToPay = 0; // Monto Cta x Cobrar
	      amtToPay = Double.valueOf(doc.amt);

	      String linea = period + "|" + cuo + "|" + nroCorrelativo + "|" + bpDocumentType + "|"
	              + (bpDocumentNumber.length() > 15 ? bpDocumentNumber.substring(0, 14) : bpDocumentNumber) + "|" + bpName
	              + "|" + CDPType + "|" + strserie + "|" + strCorrelativo + "|" + dateacct + "|"
	              + String.format("%.2f", amtToPay) + "|" + OpStatus + "|";


	      if (!sb.toString().equals(""))
	        sb.append("\n");
	      sb.append(linea);
	      sunatPle.numEntries++;

	    }

	    sunatPle.data = sb.toString();

	    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
	    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

	    String filename = "LE" + rucAdq + dt.format(dateTo) + "030600011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

	    sunatPle.filename = filename;

	    return sunatPle;
	  }

  
  
  
  /* Generar objeto del PLE */
	public static StructPle getStructPLECuenta19(ConnectionProvider conn, VariablesSecureApp vars,  Date strDateFrom,
			Date strDateTo, String strOrg) throws Exception {
		
		String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
		String strDttTo = sdf.format(strDateTo);
		
		StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;
	    
	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    
	    ReportLibroInventariosYBalance19Data[] data = ReportLibroInventariosYBalance19Data.getSaldoCuenta19V2(conn, strDttTo, strOrg);

	    String period = dt.format(strDateTo); // Periodo

	    String OpStatus = "1"; // Estado de la Operación

	    for (int i = 0; i < data.length; i++) {

	      ReportLibroInventariosYBalance19Data doc = data[i];

	      String regnumber = doc.cuo;

	      String assetNo = doc.correlativo;

	      if (regnumber == null || regnumber.equals(""))
	        continue;

	      String bpDocumentType = doc.bpDocumentType; // Tipo de documento
	      String bpDocNumber = doc.taxid; // Nro de documento
	      String bpName = doc.name; // Razon Social
	      String physicalDoc = doc.phydoc;
	      String specialDocType = doc.specialdoctype;
	      String strserie = "";
	      String strCorrelativo = doc.correlativo;
	      String doctype = doc.cdptype;

	      if (physicalDoc != null || !physicalDoc.equals("")) {

	        StringTokenizer st = new StringTokenizer(physicalDoc, "-");
	        // strserie = "0" + st.nextToken().trim();
	        strserie = LPad(st.nextToken().trim(), 4, '0');
	        strCorrelativo = st.nextToken().trim();
	      }

	      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
	      Date prev_dateacct = df.parse(doc.dateacct);

	      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

	      double amtToPay = 0; // Monto Cta x Cobrar
	      amtToPay = Double.valueOf(doc.amt);

	      String linea = period + "|" + regnumber + "|" + assetNo + "|" + bpDocumentType + "|"
	          + (bpDocNumber.length() > 15 ? bpDocNumber.substring(0, 14) : bpDocNumber) + "|" + bpName
	          + "|" + doctype + "|" + strserie + "|" + strCorrelativo + "|" + dateacct + "|"
	          + String.format("%.2f", amtToPay) + "|" + OpStatus + "|";

	      if (!sb.toString().equals(""))
	        sb.append("\n");
	      sb.append(linea);
	      sunatPle.numEntries++;

	    }

	    sunatPle.data = sb.toString();
	    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
	    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

	    String filename = "LE" + rucAdq + dt.format(strDateTo) + "030600011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

	    sunatPle.filename = filename;

	    return sunatPle;
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
  
  public static String LPad(String s, int n, char car) {
	    return String.format("%1$" + n + "s", s).replace(' ', car);
	  }

  @Override
  public String getServletInfo() {
    return "Servlet ReportLibroInventariosYBalance19. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
