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
package pe.com.unifiedgo.imports.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.service.db.CallProcess;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class CreateInvoiceFromPartial extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  
  private static String COrderID="";
  

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "CreateInvoiceFromPartial|M_Product_Id", "");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "CreateInvoiceFromPartial|docDate",
          SREDateTimeData.FirstDayOfMonth(this));

      String strDateto = vars.getGlobalVariable("inpDateTo", "CreateInvoiceFromPartial|inpDateTo",
          SREDateTimeData.today(this));
      
      String strDueDate = vars.getGlobalVariable("inpDueDate", "CreateInvoiceFromPartial|inpDueDate",
              SREDateTimeData.today(this));
      
      String strNumMonths = vars.getGlobalVariable("inpNumMonths",
          "CreateInvoiceFromPartial|NumMonths", "");
      
      String strOrg = vars.getGlobalVariable("inpOrg", "CreateInvoiceFromPartial|Org", "0");
      
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "CreateInvoiceFromPartial|M_Warehouse_ID");
      
      String strDocumentoFisico = "";
      String strChkInvoiceToCo = vars.getGlobalVariable("inpChkInvoiceToCo",
              "CreateInvoiceFromPartial|ChkInvoiceToCo", "Y");
      String strAccDate = vars.getGlobalVariable("inpAccDate", "CreateInvoiceFromPartial|inpAccDate",
              SREDateTimeData.today(this));
      String strTabId = vars.getStringParameter("inpTabId");
      
      vars.setSessionValue("inpTabId", strTabId);
      
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      
      vars.setSessionValue("strCOrderId", strCOrderId);
      
      
      COrderID = strCOrderId;
      Order orderObj = OBDal.getInstance().get(Order.class, strCOrderId.trim());
    		  
      
      
     
      
    		  
      //comboItem para Comprobante no domiciliado por Defecto
      String strCboItemId = "";
      OBCriteria<SCRComboItem> comboItem = OBDal.getInstance().createCriteria(SCRComboItem.class);
      comboItem.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "tabla10_91"));
      SCRComboItem comboItem_get = null;	
  	  try {
  		comboItem_get = (SCRComboItem) comboItem.uniqueResult();
  		if (comboItem_get != null)
  			strCboItemId= comboItem_get.getId();
  	  } catch (Exception e) {
  	    throw new OBException(OBMessageUtils.messageBD("swa_not_ComboItem_repositionOut"));
  	  }
      //
      
      
      String strPaymentTerm = orderObj.getPaymentTerms().getId().trim();
      String strAmtInvoice = orderObj.getGrandTotalAmount().toString();
      
      String strChkInvoiceDiscount="N";
      
      printPageDataSheet(request, response, vars, mProductId, strDocDate, strNumMonths, strOrg,
          strDateto, strDueDate, strWarehouse, strDocumentoFisico, strPaymentTerm,strAccDate,strAmtInvoice, strChkInvoiceToCo, strCboItemId, strChkInvoiceDiscount);
    }else if (vars.commandIn("SAVE")){
    	
     OBError myError;
     myError = new OBError();
     myError.setType("Success");
     myError.setTitle(OBMessageUtils.messageBD("Success"));
    	
    	
      String  strTabId = vars.getSessionValue("inpTabId");
      String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
      if (strWindowPath.equals(""))
           strWindowPath = strDefaultServlet;
      
      String strDoctypeInvoiceId = vars.getStringParameter("inpscrComboItemId");
      String strDocumentoFisico = vars.getStringParameter("inpDocumentoFisico");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strPaymentTerm = vars.getStringParameter("inpPaymentTermId");
      String strDueDate = vars.getStringParameter("inpDueDate");
      String strAccDate =  vars.getStringParameter("inpAccDate");
      String strChkInvoiceToCo = vars.getStringParameter("inpChkInvoiceToCo");
    	
    	
      String strCOrderId = vars.getSessionValue("strCOrderId");
      Order orderObj = OBDal.getInstance().get(Order.class, strCOrderId.trim());
      
      Process process = null;
      try {
   	     process = OBDal.getInstance().get(Process.class, "77F44AC3136C4750B86FA08AF66535DD");
	  } catch (Exception e) {
		  
		  myError = new OBError();
          myError.setType("Error");
          myError.setTitle(OBMessageUtils.messageBD("Error"));
          myError.setMessage(OBMessageUtils.messageBD("swa_process_not_found"));
          vars.setMessage(strTabId, myError);
          printPageClosePopUp(response, vars, strWindowPath);
		  
	  }
      
      Date voidDate = null;
      Date voidAcctDate = null;
      
      try {
          voidDate = OBDateUtils.getDate(strDocDate);
          voidAcctDate = OBDateUtils.getDate(strAccDate);
      } catch (ParseException pe) {
        
    	  myError = new OBError();
          myError.setType("Error");
          myError.setTitle(OBMessageUtils.messageBD("Error"));
          myError.setMessage(OBMessageUtils.messageBD("swa_invalid_dates"));
          vars.setMessage(strTabId, myError);
          printPageClosePopUp(response, vars, strWindowPath);
          
      } 
      
     
      Map<String, String> parameters = null;
      parameters = new HashMap<String, String>();
      parameters.put("sim_doc_physical_invoice", strDocumentoFisico);
      parameters.put("sim_date_invoice", OBDateUtils.formatDate(voidDate, "yyyy-MM-dd"));
      parameters.put("sim_accountdate_invoice", OBDateUtils.formatDate(voidAcctDate, "yyyy-MM-dd"));
      parameters.put("sim_tocomplete_invoice", strChkInvoiceToCo);
      parameters.put("sim_documentto_invoice", strDoctypeInvoiceId);
      
   	  final ProcessInstance pinstance = CallProcess.getInstance().call(process, strCOrderId,parameters);
    	     	  
      if(pinstance.getResult() != 0L){
    	  OBDal.getInstance().commitAndClose();
          
          
      }else if (pinstance.getResult() == 0L){
    	  System.out.println("ERROR AL CREAR FACTURAS PROVEEDOR DESDE PARCIAL: "+ pinstance.getErrorMsg());
     	 
          myError = new OBError();
          myError.setType("Error");
          myError.setTitle(OBMessageUtils.messageBD("Error"));
          myError.setMessage(OBMessageUtils.messageBD("swa_invalid_invoicecreated"));
          vars.setMessage(strTabId, myError);
          printPageClosePopUp(response, vars, strWindowPath);
      }
      
  	   
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
    	
    	
    } else if (vars.commandIn("PROVISIONAR")) {
    	
      OBError myMessage = null;
      
      System.out.println("PRIMERO");
     
      String strDocumentoFisico = vars.getGlobalVariable("inpDocumentoFisico",
                 "CreateInvoiceFromPartial|DocumentoFisico", "");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      
      String strPaymentTerm = vars.getRequestGlobalVariable("inpPaymentTermPopUpId",
              "CreateInvoiceFromPartial|C_Paymentterm_ID");
      
      String strDueDate = vars.getStringParameter("inpDueDate");

      String strAccDate = vars.getGlobalVariable("inpAccDate", "CreateInvoiceFromPartial|inpAccDate",
              SREDateTimeData.today(this));
      
      String strtmp = vars.getGlobalVariable("inpcOrderId",
              "CreateInvoiceFromPartial|inpcOrderId", "");
      
      String strChkInvoiceToCo = vars.getStringParameter("inpChkInvoiceToCo");
      
      System.out.println("SEGUNDO");
 
      
      
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      Order orderObj = OBDal.getInstance().get(Order.class, strCOrderId.trim());
      
      String strDoctypeInvoiceId = vars.getStringParameter("inpscrComboItemId");
      
      System.out.println("TERCERO");

      
       Process process = null;
       try {
    	   process = OBDal.getInstance().get(Process.class, "77F44AC3136C4750B86FA08AF66535DD");
	   } catch (Exception e) {
		// TODO: handle exception
	   }
       
       System.out.println("CUARTO");

    	
           
      Date voidDate = null;
      Date voidAcctDate = null;
      
      try {
    	  
          System.out.println("QUINTO");

          voidDate = OBDateUtils.getDate(strDocDate);
          voidAcctDate = OBDateUtils.getDate(strAccDate);
        } catch (ParseException pe) {
        	
            System.out.println("SEXTO");

        	
          voidDate = new Date();
          voidAcctDate = new Date();
          log4j.error("Not possible to parse the following date: " + strDocDate, pe);
          log4j.error("Not possible to parse the following date: " + strAccDate, pe);
       } 
      
      System.out.println("SEPTIMO");

      
      Map<String, String> parameters = null;
      parameters = new HashMap<String, String>();
      parameters.put("sim_doc_physical_invoice", strDocumentoFisico);
      parameters.put("sim_date_invoice", OBDateUtils.formatDate(voidDate, "yyyy-MM-dd"));
      parameters.put("sim_accountdate_invoice", OBDateUtils.formatDate(voidAcctDate, "yyyy-MM-dd"));
      parameters.put("sim_tocomplete_invoice", strChkInvoiceToCo);
      parameters.put("sim_documentto_invoice", strDoctypeInvoiceId);
      
   	  final ProcessInstance pinstance = CallProcess.getInstance().call(process, COrderID,parameters);
    	  
   	  System.out.println(pinstance.getErrorMsg());
   	  
      if(pinstance.getResult() != 0L){
          System.out.println("OCTAVO");

          
      }else if (pinstance.getResult() == 0L){
          System.out.println("NOVENO");

        OBDal.getInstance().commitAndClose();
        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance.getId());   
      }
      
      System.out.println("DECIMO");

       
      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      
      PrintWriter out = response.getWriter();

      System.out.println("ONCEAVO");

      
      /*
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle(OBMessageUtils.messageBD("Success"));
      myMessage.setMessage(OBMessageUtils.messageBD("sim_createPartialsucess"));
      vars.setMessage(strTabId, myMessage);
      System.out.println(strTabId);
      
      
      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;
      System.out.println("HELLO. !!!");
      printPageClosePopUp(response, vars, strWindowPath);
      System.out.println("HELLO   2222. !!!");*/
      //return;
      
      
      
      
    } else if (vars.commandIn("CALCULAR")) { 
    	
    	String strDateTo = vars.getStringParameter("inpDateTo");
    	
    	String strPaymentTerm = vars.getRequestGlobalVariable("inpPaymentTermPopUpId",
                "CreateInvoiceFromPartial|C_Paymentterm_ID");
    	
    	PaymentTerm peyment = OBDal.getInstance().get(PaymentTerm.class, strPaymentTerm.trim());
    	
    	Date voidDateReception = null;
    	Date dt = null;
    	Date dto = null;
    	try {
			voidDateReception = OBDateUtils.getDate(strDateTo);
			
			Calendar c = Calendar.getInstance(); 
			c.setTime(voidDateReception);
			c.add(Calendar.DATE, (int) (long) peyment.getOverduePaymentDaysRule());
			
			dt = c.getTime();
			
			
			response.setCharacterEncoding("UTF-8");
		    response.setContentType("application/json");
		    PrintWriter out = response.getWriter();
		    out.print(getJSONDueDate(dt, dto));
		    out.close();
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    	
    } else if (vars.commandIn("REPLICARFECHA")) { 
        
      	String strDocDate = vars.getStringParameter("inpDocDate");
      	String strPaymentTerm = vars.getRequestGlobalVariable("inpPaymentTermPopUpId",
                  "CreateInvoiceFromPartial|C_Paymentterm_ID");
      	
      	PaymentTerm peyment = OBDal.getInstance().get(PaymentTerm.class, strPaymentTerm.trim());
      	
      	Date voidDateReception = null;
      	Date dt = null;
      	Date dto = null;
      	try {
  			voidDateReception = OBDateUtils.getDate(strDocDate);
  			
  			Calendar c = Calendar.getInstance(); 
  			c.setTime(voidDateReception);
  			c.add(Calendar.DATE, (int) (long) peyment.getOverduePaymentDaysRule());
  			
  			Calendar cl = Calendar.getInstance(); 
  			cl.setTime(voidDateReception);
  			
  			dt = c.getTime();
  			dto = cl.getTime(); 
  			
  			
  			response.setCharacterEncoding("UTF-8");
  		    response.setContentType("application/json");
  		    PrintWriter out = response.getWriter();
  		    out.print(getJSONDueDate(dt , dto));
  		    out.close();
         } catch (ParseException e) {
  			// TODO Auto-generated catch block
  			//e.printStackTrace();
  		}
      	
      	
      } else
      pageError(response);
  }
  
  
  private String getJSONDueDate(Date dt, Date dto) throws ServletException {
	    JSONObject json = null;

	    try {
	      OBContext.setAdminMode();

	      json = new JSONObject();
	      json.put("duedate", OBDateUtils.formatDate(dt, "dd-MM-yyyy"));
	      json.put("invoicedate", OBDateUtils.formatDate(dto, "dd-MM-yyyy"));
	    } catch (Exception e) {
	      try {
	        json.put("error", e.getMessage());
	      } catch (JSONException jex) {
	        log4j.error("Error trying to generate message: " + jex.getMessage(), jex);
	      }
	    } finally {
	      OBContext.restorePreviousMode();
	    }

	    return json.toString();
	  }

    private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String mProductId, String strDocDate, String strNumMonths,
      String strOrg, String strDateTo, String strDueDate, String strWarehouse,  String strDocumentoFisico, 
      String strPaymentTermPopUp, String strAccDate, String strAmtInvoice, String strChkInvoiceToCo, String cboItemId, String strChkInvoiceDiscount) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
   
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/imports/ad_reports/CreateInvoiceFromPartial", discard)
        .createXmlDocument();

 
    
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
      }


    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();  
       // myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      strConvRateErrorMsg = myMessage.getMessage();
    
   }
    

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateInvoiceFromPartial", false, "",
          "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.CreateInvoiceFromPartial");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "CreateInvoiceFromPartial.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateInvoiceFromPartial.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("CreateInvoiceFromPartial");
        vars.removeMessage("CreateInvoiceFromPartial");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "CreateInvoiceFromPartial"), Utility.getContext(this, vars,
                "#User_Client", "CreateInvoiceFromPartial"), 0);
        // comboTableData.fillParameters(null, "CreateInvoiceFromPartial", "");
        Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromPartial",
            strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "CreateInvoiceFromPartial"), Utility.getContext(this, vars, "#User_Client",
                "CreateInvoiceFromPartial"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromPartial",
            strWarehouse);
        xmlDocument.setData("reportM_Warehouse_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      //String cboItemInvoiceId = ""; 
      xmlDocument.setParameter("scrComboItemId", cboItemId);
      
      xmlDocument.setParameter("scrComboItemIdDescription",
          CreateInvoiceFromPartialData.selectScrComboItem(this, cboItemId));
      

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("docDueDate", strDueDate);
      xmlDocument.setParameter("docAccDate", strAccDate);
      xmlDocument.setParameter("paramPaymentTerm_ID", strPaymentTermPopUp);
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("DocumentoFisico", strDocumentoFisico);
      xmlDocument.setParameter("AmtInvoice", strAmtInvoice);
      xmlDocument.setParameter("paramChkInvoiceToCo", strChkInvoiceToCo);
      xmlDocument.setParameter("paramChkInvoiceDiscount", strChkInvoiceDiscount);
      
      /*xmlDocument.setParameter(
          "warehouseArray",
          Utility.arrayDobleEntrada(
              "arrWarehouse",
              CreateInvoiceFromPartialData.selectWarehouseDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "CreateInvoiceFromPartial"))));
      */
      
      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("adOrgId", strOrg);
      /*
      xmlDocument.setParameter("productDescription",
          CreateInvoiceFromPartialData.selectMproduct(this, mProductId));
     */
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "CreateInvoiceFromPartial"), Utility.getContext(this, vars,
                "#User_Client", "CreateInvoiceFromPartial"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromPartial", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "c_paymentterm_id", "",
              "SIM_Orderimport Payment Terms Val Categ", Utility.getContext(this, vars, "#User_Org",
                  "CreateInvoiceFromPartial"), Utility.getContext(this, vars, "#User_Client",
                  "CreateInvoiceFromPartial"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoiceFromPartial", strPaymentTermPopUp);
          xmlDocument.setData("reportPaymentTerm_ID", "liststructure", comboTableData.select(false));
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

  public String getMes(int month){
	  String result;
	  switch(month){
	  case 0:
	    {
	      result="Enero";
	      break;
	    }
	  case 1:
	    {
	      result="Febrero";
	      break;
	    }
	  case 2:
	    {
	      result="Marzo";
	      break;
	    }
	  case 3:
	    {
	      result="Abril";
	      break;
	    }
	  case 4:
	    {
	      result="Mayo";
	      break;
	    }
	  case 5:
	    {
	      result="Junio";
	      break;
	    }
	  case 6:
	    {
	      result="Julio";
	      break;
	    }
	  case 7:
	    {
	      result="Agosto";
	      break;
	    }
	  case 8:
	    {
	      result="Septiembre";
	      break;
	    }
	  case 9:
	    {
	      result="Octubre";
	      break;
	    }
	  case 10:
	    {
	      result="Noviembre";
	      break;
	    }
	  case 11:
	    {
	      result="Diciembre";
	      break;
	    }
	  default:
	    {
	      result="Error";
	      break;
	    }
	}
	  
	  return result;
  }
  
  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
