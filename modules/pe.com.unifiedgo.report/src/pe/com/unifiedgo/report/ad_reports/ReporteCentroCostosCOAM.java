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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
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

public class ReporteCentroCostosCOAM extends HttpSecureAppServlet {
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
      String strAdOrgId = vars.getGlobalVariable("inpOrgId", "ReporteCentroCostosCOAM|OrgId","");
      String strYearId = vars.getGlobalVariable("inpYearId", "ReporteCentroCostosCOAM|YearId", "");
      String strCostCenterId = vars.getGlobalVariable("inpCostCenterId", "ReporteCentroCostosCOAM|CostCenterId", "");

      printPageDataSheet(request, response, vars, strAdOrgId, strYearId, strCostCenterId);
    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
    	String strAdOrgId = vars.getStringParameter("inpOrgId");
        String strYearId = vars.getStringParameter("inpYearId");
        String strCostCenterId = vars.getStringParameter("inpCostCenterId");

      printPageDataSheet(request, response, vars, strAdOrgId, strYearId, strCostCenterId);
    } else if (vars.commandIn("PDF","XLS")) {// aqui para imprimir como excel
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String strAdOrgId = vars.getStringParameter("inpOrgId");
      String strYearId = vars.getStringParameter("inpYearId");
      String strCostCenterId = vars.getStringParameter("inpCostCenterId");
      
      String strSeparateDocs = vars.getSessionValue("inpSeparateDocs");

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("PDF","XLS")) {
        printPageXLS(request, response, vars, strAdOrgId, strYearId, strCostCenterId, "");
      }

    } else {
      pageError(response);
    }
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAdOrgId, String strYearId, String strCostCenterId, String strSeparateDocs) throws IOException, ServletException {

    String strOutput;
    String strReportName;

    ReporteCentroCostosCOAMData[] data = null;

    org.openbravo.model.financialmgmt.calendar.Year year = OBDal.getInstance().get(org.openbravo.model.financialmgmt.calendar.Year.class, strYearId);
    
    String anual = year.getFiscalYear();
    String strStartDate = "01-01-" + anual;
    String strEndDate = "31-12-" + anual;
    
    ReporteCentroCostosCOAMData[] DataFinal = null;
    
    
    /*
    if(strSeparateDocs.equalsIgnoreCase("Y")){
    	// Generar dos documentos 
		
		
	}else{
		//Generar un solo documento 
		try{
			
		}catch(OBException e){
			advisePopUp(request, response, "WARNING",
			          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
			          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
		}
		
	}
    */
    
    
    try {
    	
      data = ReporteCentroCostosCOAMData.selectDataFull(this, strStartDate, strEndDate, strAdOrgId,
    		  strCostCenterId);
      
      
      String[] meses = {"01","02","03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
      
      
      ArrayList<ReporteCentroCostosCOAMData> ListaData = new ArrayList<ReporteCentroCostosCOAMData>();
      
      ReporteCentroCostosCOAMData temp = null;
      
      if(data.length==0){
    	  advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
          return;
      }
      
      ReporteCentroCostosCOAMData inicial = data[0];
      
      if(inicial.periodo.equals("NN")){
    	  for(int j=0;j<meses.length;j++){
			  temp = new ReporteCentroCostosCOAMData();
			  
			  temp.patron = inicial.patron;
			  temp.cuenta = inicial.cuenta;
			  temp.nombre = inicial.nombre;
			  temp.grupopos = inicial.grupopos;
			  temp.tipogrupo = inicial.tipogrupo;
			  temp.mes = getMonth(meses[j]);
			  temp.gasto = "0.0";
			  temp.periodo = meses[j] + "-" + anual;
			  ListaData.add(temp);
		  }
      }else{
    	  for(int j=0;j<meses.length;j++){
			  temp = new ReporteCentroCostosCOAMData();
			  
			  temp.patron = inicial.patron;
			  temp.cuenta = inicial.cuenta;
			  temp.nombre = inicial.nombre;
			  temp.grupopos = inicial.grupopos;
			  temp.tipogrupo = inicial.tipogrupo;
			  temp.mes = getMonth(meses[j]);
			  
			  if(inicial.mes == meses[j])
				  temp.gasto = inicial.gasto;
			  else
				  temp.gasto = "0.0";
			  temp.periodo = meses[j] + "-" + anual;
			  ListaData.add(temp);
		  }
      }
      
      for(int k=1;k<data.length;k++){
    	  
    	  ReporteCentroCostosCOAMData doc = data[k];
    	  
    	  if(doc.cuenta.length()>2 && doc.periodo.equalsIgnoreCase("NN")){
    		  continue;
    	  }
    	  
    	  if(doc.periodo.equals("NN")){
    		  
    		  for(int j=0;j<meses.length;j++){
    			  temp = new ReporteCentroCostosCOAMData();
    			  
    			  temp.patron = doc.patron;
    			  temp.cuenta = doc.cuenta;
    			  temp.nombre = doc.nombre;
    			  temp.grupopos = doc.grupopos;
    			  temp.tipogrupo = doc.tipogrupo;
    			  temp.mes = getMonth(meses[j]);
    			  temp.gasto = "0.0";
    			  temp.periodo = meses[j] + "-" + anual;
    			  ListaData.add(temp);
    		  }
    		  
    	  }else{
    		  temp = new ReporteCentroCostosCOAMData();
    		  
    		  temp.patron = doc.patron;
			  temp.cuenta = doc.cuenta;
			  temp.nombre = doc.nombre;
			  temp.grupopos = doc.grupopos;
			  temp.tipogrupo = doc.tipogrupo;
			  temp.mes = getMonth(doc.mes);
			  temp.gasto = doc.gasto;
			  temp.periodo = doc.mes + "-" + anual;
			  ListaData.add(temp);
    		  
    	  }
    	  
      }
      DataFinal = new ReporteCentroCostosCOAMData[ListaData.size()];
      
      ListaData.toArray(DataFinal);
      
    } catch (OBException e) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
    }
    
    
    if(vars.commandIn("PDF")){
    	strOutput = "pdf";
    }else {
    	strOutput = "xls";
    }

    
    strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReporteCentroCostosFullCOAM.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    String orgname = ReporteCentroCostosCOAMData.selectSocialName(this, strAdOrgId);

    /* parameters */
    parameters.put("organizacion", orgname);
    parameters.put("Ruc", ReporteCentroCostosCOAMData.selectRucOrg(this, strAdOrgId));
    parameters.put("periodo", anual);
    
    parameters.put("startdate", strStartDate);
    parameters.put("enddate", strEndDate);

    renderJR(vars, response, strReportName, "Reporte_General_Centros_de_Costos", strOutput, parameters,
    		DataFinal, null);
  }



  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strAdOrgId, String strYearId, String strCostCenterId) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReporteStockConsolidadoData[] data = null;

    

    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/ReporteCentroCostosCOAM", discard)
        .createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();
      
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

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReporteCentroCostosCOAM", false,
          "", "", "imprimirPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.ReporteCentroCostosCOAM");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReporteCentroCostosCOAM.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReporteCentroCostosCOAM.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      

      {
        OBError myMessage = vars.getMessage("ReporteCentroCostosCOAM");
        vars.removeMessage("ReporteCentroCostosCOAM");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", "");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDate", "");
      xmlDocument.setParameter("proyDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("proyDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      

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

      
        xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      

      xmlDocument.setParameter("adOrg", strAdOrgId);

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }
  
  private String getMonth(String mes){
	  if(mes.equals("01")){
		  return "ENERO";
	  } else if(mes.equals("02")){
		  return "FEBRERO";
	  }else if(mes.equals("03")){
		  return "MARZO";
	  }else if(mes.equals("04")){
		  return "ABRIL";
	  }else if(mes.equals("05")){
		  return "MAYO";
	  }else if(mes.equals("06")){
		  return "JUNIO";
	  }else if(mes.equals("07")){
		  return "JULIO";
	  }else if(mes.equals("08")){
		  return "AGOSTO";
	  }else if(mes.equals("09")){
		  return "SEPTIEMBRE";
	  }else if(mes.equals("10")){
		  return "OCTUBRE";
	  }else if(mes.equals("11")){
		  return "NOVIEMBRE";
	  }else if(mes.equals("12")){
		  return "DICIEMBRE";
	  } else return"";
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
