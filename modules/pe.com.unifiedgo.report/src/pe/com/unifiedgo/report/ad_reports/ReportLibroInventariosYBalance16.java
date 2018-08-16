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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

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

public class ReportLibroInventariosYBalance16 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance16|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance16|DateTo", "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance16|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance16|Record", "");
      String strTable = vars.getGlobalVariable("inpTable",
          "ReportLibroInventariosYBalance16|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars
          .getGlobalVariable("inpTable", "ReportLibroInventariosYBalance16|Table");
      String strRecord = vars.getGlobalVariable("inpRecord",
          "ReportLibroInventariosYBalance16|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportLibroInventariosYBalance16.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportLibroInventariosYBalance16|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportLibroInventariosYBalance16|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportLibroInventariosYBalance16|Org", "0");
      vars.setSessionValue("ReportLibroInventariosYBalance16.initRecordNumber", "0");
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
    ReportLibroInventariosYBalance16Data[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportLibroInventariosYBalance16",
        false, "", "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();

	toolbar.prepareRelationBarTemplate(false, false,
			"imprimirXLS();return false;");
    
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance16", discard)
          .createXmlDocument();
      data = ReportLibroInventariosYBalance16Data.set("0");
      data[0].rownum = "0";
    } 
    
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance16");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance16.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "ReportLibroInventariosYBalance16.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportLibroInventariosYBalance16");
      vars.removeMessage("ReportLibroInventariosYBalance16");
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
              "ReportLibroInventariosYBalance16"), Utility.getContext(this, vars, "#User_Client",
              "ReportLibroInventariosYBalance16"), '*');
      comboTableData.fillParameters(null, "ReportLibroInventariosYBalance16", "");
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
    
    //para cargar la variables javascrip de periodos

  	xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion","arrPeriodos",
			ReportLibroInventariosYBalance16Data
			.select_periodos(this)));

		 //FIN  para cargar la variables javascrip de periodos
    
    vars.setSessionValue("ReportLibroInventariosYBalance16|Record", strRecord);
    vars.setSessionValue("ReportLibroInventariosYBalance16|Table", strTable);
    
    

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

	private void printPagePDF(HttpServletRequest request,HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg)
      throws IOException, ServletException {

    ReportLibroInventariosYBalance16Data[] data2 = null;
    ReportLibroInventariosYBalance16Data[] data = null;
    
    data2 = ReportLibroInventariosYBalance16Data.getSaldoCuenta16y17V2(this, strDateTo, strOrg);
    
	if (data2.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
        }
	
	data = returnDataLikeAnalytic(data2);
    
    SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
    Calendar caldttTo = new GregorianCalendar();
    caldttTo.setTime(StringToDate(strDateTo));
    int year = caldttTo.get(Calendar.YEAR);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, 1);
    strDateFrom = dt.format(cal.getTime());

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportLibroInventariosYBalance16Data.selectCompany(this, vars.getClient()) + "\n"
        + "RUC:" + ReportLibroInventariosYBalance16Data.selectRucOrg(this, strOrg) + "\n";

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportLibroInventariosYBalance16Data.selectOrg(this, strOrg) + "\n";
    
    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance16.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportLibroInventariosYBalance16Excel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    parameters.put("Ruc", ReportLibroInventariosYBalance16Data.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportLibroInventariosYBalance16Data.selectSocialName(this, strOrg));
    parameters.put("strDateFrom", strDateFrom);
    parameters.put("strDateTo", strDateTo);
    parameters.put("dateTo", StringToDate(strDateTo));
    renderJR(vars, response, strReportName, "LibroIB_Cuenta16", strOutput, parameters, data, null);
  }
  
  private static ReportLibroInventariosYBalance16Data[] returnDataLikeAnalytic(ReportLibroInventariosYBalance16Data[] data){	  
	  ReportLibroInventariosYBalance16Data[] dataFinal;
	  ArrayList<ReportLibroInventariosYBalance16Data> listData = new ArrayList<ReportLibroInventariosYBalance16Data>();;
	    ArrayList<ReportLibroInventariosYBalance16Data> listDataSoloPendiente = new ArrayList<ReportLibroInventariosYBalance16Data>();

	      String previousCodCuenta = data[0].codcuenta;
	      String previousTercero = data[0].name;
	      String previousNumeroDoc = data[0].phydoc;

	      for (int kkk = 1; kkk < data.length; kkk++) {

	        String currCodCuenta = data[kkk].codcuenta;
	        String currTercero = data[kkk].name;
	        String currNumeroDoc = data[kkk].phydoc;

	        if (previousCodCuenta.equals(currCodCuenta) && previousTercero.equals(currTercero) && previousNumeroDoc.equals(currNumeroDoc)) {
	          data[kkk].saldo = (new BigDecimal(data[kkk].saldo).add(new BigDecimal(data[kkk - 1].saldo))).setScale(2).toString();
	        }

	        previousCodCuenta = currCodCuenta;
	        previousTercero = currTercero;
	        previousNumeroDoc = currNumeroDoc;
	      }

	      // PARA AGRUPAR EL HISTORIAL DE LINEAS EN UNA SOLA :

	      SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	      Date fechaDoc = new Date();
	      Date fechaTope = new Date();	      

	      BigDecimal montoDolares = BigDecimal.ZERO;
	      BigDecimal montoDebe = BigDecimal.ZERO;
	      BigDecimal montoHaber = BigDecimal.ZERO;
	      BigDecimal montoSaldo = BigDecimal.ZERO;
	      Integer contador = 0;

	      for (int i = 0; i < data.length; i++) {
	    	  ReportLibroInventariosYBalance16Data actual = data[i];
	    	  ReportLibroInventariosYBalance16Data siquiente = i + 1 == data.length ? data[i] : data[i + 1];

	        try {
	          fechaDoc = formatter.parse(siquiente.dateacct);
	        } catch (ParseException e) {
	          e.printStackTrace();
	        }

	        montoDebe = new BigDecimal(actual.debe).add(montoDebe);
	        montoHaber = new BigDecimal(actual.haber).add(montoHaber);
	        montoSaldo = montoDebe.subtract(montoHaber);

	        //if (actual.codcuenta.compareTo(siquiente.codcuenta) == 0 && actual.taxid.compareTo(siquiente.taxid) == 0 && actual.phydoc.compareTo(siquiente.phydoc) == 0 && fechaDoc.before(fechaTope)) {
	        if (actual.codcuenta.compareTo(siquiente.codcuenta) == 0 && actual.taxid.compareTo(siquiente.taxid) == 0 && actual.phydoc.compareTo(siquiente.phydoc) == 0) {
	          contador++;
	          if (i != data.length - 1)
	            continue;
	        }

	        if (contador > 0) {
	          if (montoSaldo.compareTo(BigDecimal.ZERO) != 0) {

	     	    montoDebe = montoSaldo.compareTo(BigDecimal.ZERO) > 0 ? montoSaldo : BigDecimal.ZERO;
	            montoHaber = montoSaldo.compareTo(BigDecimal.ZERO) < 0 ? montoSaldo.abs() : BigDecimal.ZERO;

	            ReportLibroInventariosYBalance16Data nuevo = new ReportLibroInventariosYBalance16Data();
	            
	              nuevo.cuo = actual.cuo;
		          nuevo.taxid= actual.taxid;
		          nuevo.dateacct = actual.dateacct;
		          nuevo.dateacct = actual.dateacct;
		          nuevo.codcuenta = actual.codcuenta;
	              nuevo.phydoc = actual.phydoc;
		          nuevo.name = actual.name;
		          nuevo.adOrgId = actual.adOrgId;
		          nuevo.cBpartnerId = actual.cBpartnerId;
		          nuevo.amt = montoSaldo.toString();
		          nuevo.debe = montoDebe.toString();
		          nuevo.haber = montoHaber.toString();


	            if (actual.codcuenta.compareTo(siquiente.codcuenta) == 0 && actual.taxid.compareTo(siquiente.taxid) == 0 && actual.phydoc.compareTo(siquiente.phydoc) == 0 && i != data.length - 1) {
	              nuevo.saldo = null;
	            } else {
	              nuevo.saldo = montoSaldo.toString();
	            }
	            listData.add(nuevo);
	          }
	        } else {

	          if (actual.codcuenta.compareTo(siquiente.codcuenta) == 0 && actual.taxid.compareTo(siquiente.taxid) == 0 && actual.phydoc.compareTo(siquiente.phydoc) == 0 && i != data.length - 1) {
	            actual.saldo = null;
	          }

	          listData.add(actual);
	        }
	        montoDolares = BigDecimal.ZERO;
	        montoDebe = BigDecimal.ZERO;
	        montoHaber = BigDecimal.ZERO;
	        montoSaldo = BigDecimal.ZERO;
	        contador = 0;
	      }
	      int ini = -1;
	      int fin = 0;
	      BigDecimal sumaDebe = BigDecimal.ZERO;
	      BigDecimal sumaHaber = BigDecimal.ZERO;

	      for (int i = 0; i < listData.size(); i++) {

	          ReportLibroInventariosYBalance16Data actual = listData.get(i);
	          ReportLibroInventariosYBalance16Data siguiente = i + 1 == listData.size() ? actual : listData.get(i + 1);
	          System.out.println("listData " + actual.name);
	          sumaDebe = sumaDebe.add(new BigDecimal(actual.debe));
	          sumaHaber = sumaHaber.add(new BigDecimal(actual.haber));

	          if (!actual.codcuenta.equals(siguiente.codcuenta) || !actual.taxid.equals(siguiente.taxid) || !actual.phydoc.equals(siguiente.phydoc) || i + 1 == listData.size()) {// variar
	        	     System.out.println("sumaDebe.subtract(sumaHaber): " + sumaDebe.subtract(sumaHaber));
	            if (sumaDebe.subtract(sumaHaber).compareTo(BigDecimal.ZERO) != 0) {
	              for (int k = ini + 1; k <= i; k++) {
	                listDataSoloPendiente.add(listData.get(k));
	              }
	            }

	            ini = i;
	            sumaDebe = BigDecimal.ZERO;
	            sumaHaber = BigDecimal.ZERO;

	          } else {
	            continue;
	          }
	        }

	        dataFinal = new ReportLibroInventariosYBalance16Data[listDataSoloPendiente.size()];

	        for (int k = 0; k < listDataSoloPendiente.size(); k++) {
	          dataFinal[k] = listDataSoloPendiente.get(k);
	          if(dataFinal[k].phydoc.equals("????")){
	          	dataFinal[k].dateacct="";
	          	dataFinal[k].phydoc="";
	          }
	        }
	    return dataFinal;
  }
  
  
  public static StructPle getDataPLECuenta16y17(
	      ReportLibroInventariosYBalance16Data[] data, Date dateFrom, Date dateTo, String strOrg)
	      throws Exception {

	    StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;

	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    String period = dt.format(dateTo);
	    
	    String OpStatus = "1";

	    for (int i = 0; i < data.length; i++) {

	      ReportLibroInventariosYBalance16Data doc = data[i];

	      String cuo = doc.cuo;

	      if (cuo == null || cuo.equals(""))
	        continue;
	      String nroCorrelativo = doc.correlativo;		      
	      String bpDocumentType = doc.bpDocumentType;
	      String bpDocumentNumber = doc.taxid;
	      String bpName = doc.name;
	      

	      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
	      Date prev_dateacct = df.parse(doc.dateacct);

	      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

	      double amtToPay = 0; // Monto Cta x Cobrar
	      amtToPay = Double.valueOf(doc.amt);

	      String linea = period + "|" + cuo + "|" + nroCorrelativo + "|" + bpDocumentType + "|"
	          + bpDocumentNumber + "|" + bpName + "|" + dateacct + "|" + String.format("%.2f", amtToPay)
	          + "|" + OpStatus + "|";

	      if (!sb.toString().equals(""))
	        sb.append("\n");
	      sb.append(linea);
	      sunatPle.numEntries++;

	    }

	    sunatPle.data = sb.toString();

	    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
	    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

	    String filename = "LE" + rucAdq + dt.format(dateTo) + "030300011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

	    sunatPle.filename = filename;

	    return sunatPle;
	  }
  
  
  public ReportLibroInventariosYBalance16Data[] getDataCuenta16y17(ConnectionProvider conn, String strDateTo, String strOrgId) throws Exception
  {
	  ReportLibroInventariosYBalance16Data[] data = ReportLibroInventariosYBalance16Data.getSaldoCuenta16y17(conn, strDateTo, strOrgId);
	  
	  return returnDataLikeAnalytic(data);
  }
  
  
  /* Generar objeto del PLE */
	public static StructPle getStructPLECuenta16y17(ConnectionProvider conn, VariablesSecureApp vars,  Date strDateFrom,
			Date strDateTo, String strOrg) throws Exception {
		
		String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
		String strDttTo = sdf.format(strDateTo);

		StringBuffer sb = new StringBuffer();
	    StructPle sunatPle = new StructPle();
	    sunatPle.numEntries = 0;
	    
	    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
	    
	    ReportLibroInventariosYBalance16Data[] data = ReportLibroInventariosYBalance16Data.getSaldoCuenta16y17V2(conn, strDttTo, strOrg);
	    
	    if (data == null || data.length == 0) {
		      return sunatPle;
		    }
	    
	    data = returnDataLikeAnalytic(data);

	    String period = dt.format(strDateTo); // Periodo

	    String OpStatus = "1"; // Estado de la Operación
	    

	    for (int i = 0; i < data.length; i++) {

	      ReportLibroInventariosYBalance16Data doc = data[i];

	      String regnumber = doc.cuo;// .replaceAll("-", "").replaceAll("_", ""); // CUO

	      if (regnumber == null || regnumber.equals(""))
	        continue;

	      // String bPartnerID = doc.cBpartnerId;
	      String bpDocumentType = doc.bpDocumentType; // Tipo de documento
	      String bpDocNumber = doc.taxid; // Nro de documento
	      String bpName = doc.name; // Razon Social

	      String assetNo = doc.correlativo; // Nro Asiento (correlativo)

	      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	      DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
	      Date prev_dateacct = df.parse(doc.dateacct);

	      String dateacct = df2.format(prev_dateacct); // Fecha de Emision

	      double amtToPay = 0; // Monto Cta x Cobrar
	      amtToPay = Double.valueOf(doc.amt);

	      String linea = period + "|" + regnumber + "|" + assetNo + "|" + bpDocumentType + "|"
	          + (bpDocNumber.length() > 15 ? bpDocNumber.substring(0, 14) : bpDocNumber) + "|" + bpName
	          + "|" + dateacct + "|" + String.format("%.2f", amtToPay) + "|" + OpStatus + "|";

	      if (!sb.toString().equals(""))
	        sb.append("\n");
	      sb.append(linea);
	      sunatPle.numEntries++;
	    }

	    sunatPle.data = sb.toString();
	    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
	    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

	    String filename = "LE" + rucAdq + dt.format(strDateTo) + "030500011111.TXT"; // LERRRRRRRRRRRAAAAMMDD030300CCOIM1.TXT

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

  @Override
  public String getServletInfo() {
    return "Servlet ReportLibroInventariosYBalance16. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
