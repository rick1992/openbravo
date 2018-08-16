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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

public class ReporteFlujoDeCaja extends HttpSecureAppServlet {
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
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "ReporteFlujoDeCaja|OrgId",
          "");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      /**/
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getStringParameter("inpProyDate", SREDateTimeData.today(this));
      
      /**/

      printPageDataSheet(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);
    } else if (vars.commandIn("PDF", "XLS")) {// aqui para imprimir como excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strDateFrom = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpProyDate");
      
      setHistoryCommand(request, "DEFAULT");
      
      printPageXLS(request, response, vars, strAD_Org_ID, strDateFrom, strDateTo);

    } else {
      pageError(response);
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDateFrom, String strDateTo) throws IOException, ServletException {

    String strOutput;
    String strReportName;

    ReporteFlujoDeCajaData[] data = null;
    
    data = ReporteFlujoDeCajaData.selectData(this, strAD_Org_ID, strDateFrom, strDateTo);
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
    }

    int countdataini = 0;
    int countdatafin = 0;
    
    BigDecimal saldoacumulado = BigDecimal.ZERO;
    
    //Se necesitan 3 arraylist para llenar la data
    ArrayList<ReporteFlujoDeCajaData> arrData = new ArrayList<ReporteFlujoDeCajaData>();
    
    //formatos necesarios
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    
    ArrayList<ReporteFlujoDeCajaData> ArrayListDataIni = new ArrayList<ReporteFlujoDeCajaData>();
    ArrayList<ReporteFlujoDeCajaData> ArrayListDataFin = new ArrayList<ReporteFlujoDeCajaData>();
    
    String strmes;
    String stryear;
    
    Date startdate;
    Date enddate;
    
    try{
    	startdate = dateFormat.parse(strDateFrom);
    	enddate = dateFormat.parse(strDateTo);
    	
    	Date tempdate = startdate;
    	
    	strmes = monthFormat.format(tempdate);
    	stryear = yearFormat.format(tempdate);
    	
    	while(!monthFormat.format(enddate).equalsIgnoreCase(strmes) || !yearFormat.format(enddate).equalsIgnoreCase(stryear)){
    		ReporteFlujoDeCajaData tempDoc = new ReporteFlujoDeCajaData();
    		ReporteFlujoDeCajaData tempDoc2 = new ReporteFlujoDeCajaData();
    		tempDoc.month = monthFormat.format(tempdate);
    		tempDoc2.month = monthFormat.format(tempdate);
    		tempDoc.year = yearFormat.format(tempdate);
    		tempDoc2.year = yearFormat.format(tempdate);
    		tempDoc.period = getPeriod(tempDoc.month,tempdate);
    		tempDoc2.period = getPeriod(tempDoc.month,tempdate);
    		tempDoc.type = "CAJA INICIAL";
    		tempDoc2.type = "CAJA FINAL";
    		tempDoc.position = "01";
    		tempDoc2.position = "03";
    		
    		Calendar newDate = Calendar.getInstance();
    		newDate.setTime(tempdate);
    		newDate.add(Calendar.MONTH, 1);
    		tempdate = newDate.getTime();
    		
    		strmes = monthFormat.format(tempdate);
        	stryear = yearFormat.format(tempdate);
    		
        	ArrayListDataIni.add(tempDoc);
        	ArrayListDataFin.add(tempDoc2);
    	}
    	
    	Calendar cd = Calendar.getInstance();
    	cd.setTime(enddate);
    	tempdate = cd.getTime();
    	
    	ReporteFlujoDeCajaData tempDoc = new ReporteFlujoDeCajaData();
    	ReporteFlujoDeCajaData tempDoc2 = new ReporteFlujoDeCajaData();
    	
    	tempDoc.month = monthFormat.format(tempdate);
    	tempDoc2.month = monthFormat.format(tempdate);
		tempDoc.year = yearFormat.format(tempdate);
		tempDoc2.year = yearFormat.format(tempdate);
		tempDoc.period = getPeriod(tempDoc.month,tempdate);
		tempDoc2.period = getPeriod(tempDoc.month,tempdate);
		tempDoc.type = "CAJA INICIAL";
		tempDoc2.type = "CAJA FINAL";
    	tempDoc.position = "01";
    	tempDoc2.position = "03";
    	
		ArrayListDataIni.add(tempDoc);
    	ArrayListDataFin.add(tempDoc2);
    	
    }catch(Exception ex){
    	ex.printStackTrace();
    }
    
    ReporteFlujoDeCajaData[] newDataIni = new ReporteFlujoDeCajaData[ArrayListDataIni.size()];
    ArrayListDataIni.toArray(newDataIni);
    ReporteFlujoDeCajaData[] newDataFin = new ReporteFlujoDeCajaData[ArrayListDataFin.size()];
    ArrayListDataFin.toArray(newDataFin);
    
    newDataIni[countdataini].paymentamt = saldoacumulado.toString();
    
    //Calcular los saldos iniciales y finales
    for(int i=0;i<data.length;i++){
    	ReporteFlujoDeCajaData doc = data[i];
    	
    	if(!newDataIni[countdataini].period.equalsIgnoreCase(doc.period)){
    		
    		//si el periodo no coincide, avanzar hasta encontrar sus valores iguales
    		while(!newDataIni[countdataini].period.equalsIgnoreCase(doc.period))
    		{
    			newDataFin[countdatafin].paymentamt = saldoacumulado.toString();
    			countdatafin++;
    			countdataini++;
    			newDataIni[countdataini].paymentamt = saldoacumulado.toString();
    		}
    	}
    	
    	if(doc.type.equalsIgnoreCase("INGRESOS")){
    		saldoacumulado = saldoacumulado.add(new BigDecimal(doc.paymentamt));
    	}else{
    		saldoacumulado = saldoacumulado.subtract(new BigDecimal(doc.paymentamt));
    	}
    	arrData.add(doc);
    }
    
    newDataFin[countdatafin].paymentamt = saldoacumulado.toString();
    
    ArrayList<ReporteFlujoDeCajaData> dataTotal = new ArrayList<ReporteFlujoDeCajaData>();
    
    dataTotal.addAll(ArrayListDataIni);
    dataTotal.addAll(arrData);
    dataTotal.addAll(ArrayListDataFin);
    
    //Data procesada total 
    ReporteFlujoDeCajaData[] DataFinal = new ReporteFlujoDeCajaData[dataTotal.size()];
    
    dataTotal.toArray(DataFinal);
    
    if(vars.commandIn("PDF")){
    	strOutput = "pdf";
    }else{
    	strOutput = "xls";
    }
    
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteFlujoDeCaja.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    String orgname = ReporteFlujoDeCajaData.selectOrg(this, strAD_Org_ID).toString();

    /* parameters */
    parameters.put("ORGANIZACION", orgname);
    parameters.put("strDateFrom", strDateFrom);
    parameters.put("strDateTo", strDateTo);

    renderJR(vars, response, strReportName, "Reporte_Flujo_De_Caja", strOutput, parameters,
        DataFinal, null);
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
  
  private String getPeriod(String month, Date docDate){
	  String period = "";
	  
	  SimpleDateFormat yearShortFormat = new SimpleDateFormat("YY");
	  
	  if(month.equalsIgnoreCase("01")){
		  period = "ene - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("02")){
		  period = "feb - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("03")){
		  period = "mar - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("04")){
		  period = "abr - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("05")){
		  period = "may - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("06")){
		  period = "jun - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("07")){
		  period = "jul - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("08")){
		  period = "ago - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("09")){
		  period = "set - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("10")){
		  period = "oct - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("11")){
		  period = "nov - "+yearShortFormat.format(docDate);
	  }else if(month.equalsIgnoreCase("12")){
		  period= "dic - "+yearShortFormat.format(docDate);
	  }
	  
	  return period;
		  
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAD_Org_ID, String strDocDate, String strProyDate) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteFlujoDeCaja", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } 
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteFlujoDeCaja", false,
          "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteFlujoDeCaja");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteFlujoDeCaja.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteFlujoDeCaja.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      {
        OBError myMessage = vars.getMessage("ReporteFlujoDeCaja");
        vars.removeMessage("ReporteFlujoDeCaja");
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

