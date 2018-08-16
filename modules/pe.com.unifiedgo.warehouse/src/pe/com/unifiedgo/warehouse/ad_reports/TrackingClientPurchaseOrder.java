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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.WindowTreeData;
import org.openbravo.erpCommon.utility.WindowTreeUtility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class TrackingClientPurchaseOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String RemoteOrg = null;
  private static String OrderID = null;

  private static final String CHILD_SHEETS = "frameWindowTreeF3"; // tree

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "TrackingClientPurchaseOrder|Org", "0");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "TrackingClientPurchaseOrder|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "TrackingClientPurchaseOrder|inpDateTo",
          SREDateTimeData.today(this));
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "TrackingClientPurchaseOrder|CB_PARTNER_ID", "");

      String mProductId = vars.getGlobalVariable("inpmProductId",
          "TrackingClientPurchaseOrder|M_Product_Id", "");

      String strNumOrder = vars.getStringParameter("paramProductOrder");
      
      String strShowPending = vars.getGlobalVariable("inpShowPending", "TrackingClientPurchaseOrder|ShowPending", "N");


      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateto, strcBpartnetId,
          mProductId, strNumOrder, strShowPending);

    } else if (vars.commandIn("LISTAR")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String mProductId = vars.getStringParameter("inpmProductId");
      String strNumOrder = vars.getStringParameter("paramProductOrder");
      String strPending = vars.getStringParameter("inpShowPending");

      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateTo, strcBpartnetId,
          mProductId, strNumOrder, strPending);
    } else if (vars.commandIn("XLS")) {
    	   String strOrg = vars.getStringParameter("inpadOrgId");
    	      String strDocDate = vars.getStringParameter("inpDocDate");
    	      String strDateTo = vars.getStringParameter("inpDateTo");
    	      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
    	      String mProductId = vars.getStringParameter("inpmProductId");
    	      String strNumOrder = vars.getStringParameter("paramProductOrder");
    	      String strPending = vars.getStringParameter("inpShowPending");
      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strOrg, strDocDate, strDateTo, strcBpartnetId,
                mProductId, strNumOrder,strPending);
      }

    } else
      pageError(response);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateTo,
	      String strBpartnetId, String mProductId, String mStrNumOrder, String strShowPending) throws IOException,
      ServletException {

    TrackingClientPurchaseOrderData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();
    
    TrackingClientPurchaseOrderData[] getData = null;
    TrackingClientPurchaseOrderData[] getDataSub1 = null;
    TrackingClientPurchaseOrderData[] getDataPartialFolio = null;
    
    TrackingClientPurchaseOrderData[] dataFinal = null;

    ArrayList<TrackingClientPurchaseOrderData> listData= new ArrayList<TrackingClientPurchaseOrderData>();
    
    boolean entroo=false;

    try {
        String ad_org_id = strOrg;
        
        System.out.println("strShowPending: " + strShowPending);
        
        if(strShowPending!=null && strShowPending.equals("Y")){ 
         	getData = TrackingClientPurchaseOrderData.selectPurchaseOrdertoReviewPending(this, ad_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
                    mProductId, mStrNumOrder.toLowerCase());
        }
        else{
        	getData = TrackingClientPurchaseOrderData.selectPurchaseOrdertoReview(this, ad_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
                    mProductId, mStrNumOrder);
        }
        
        
        if (getData.length==0) {
            advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
            return;
          }

        

        for (int i = 0; i < getData.length; i++) {
          getDataSub1 = TrackingClientPurchaseOrderData.selectOrderClientLine(this, getData[i].purchasesalesorderid,
              mProductId);
          
       TrackingClientPurchaseOrderData objCabecera= new TrackingClientPurchaseOrderData ();

       objCabecera.orgname=getData[i].orgname;
       objCabecera.ordernum=getData[i].ordernum;
       objCabecera.orderdate=getData[i].orderdate;
       objCabecera.partnername=getData[i].partnername;
       objCabecera.qtyordered=getData[i].qtyordered;
       objCabecera.qtysalesordered=getData[i].qtysalesordered;
       objCabecera.qtyshipment=getData[i].qtyshipment;
       objCabecera.qtyreturned=getData[i].qtyreturned;
       objCabecera.qtypending=getData[i].qtypending;
    	
   /* 	 System.out.println(" obj.qtyordered : " + objCabecera.qtyordered);
    	 System.out.println(" obj.qtysalesordered : " + objCabecera.qtysalesordered);
    	 System.out.println(" obj.qtyshipment : " + objCabecera.qtyshipment);
    	 System.out.println(" obj.qtyreturned : " + objCabecera.qtyreturned);
    	 System.out.println(" obj.qtypending : " + objCabecera.qtypending);
  */
          entroo=false;
          for (int j = 0; j < getDataSub1.length; j++) {
        	  
            getDataPartialFolio = TrackingClientPurchaseOrderData.selectShipmentLine(this,
                getDataSub1[j].purchasesalesorderlineid);
            
            
           TrackingClientPurchaseOrderData obj= new TrackingClientPurchaseOrderData ();

            obj.orgname=objCabecera.orgname;
       	    obj.ordernum=objCabecera.ordernum;
       	    obj.orderdate=objCabecera.orderdate;
       	    obj.partnername=objCabecera.partnername;
       	    obj.qtyordered=objCabecera.qtyordered;
       	    obj.qtysalesordered=objCabecera.qtysalesordered;
       	    obj.qtyshipment=objCabecera.qtyshipment;
       	    obj.qtyreturned=objCabecera.qtyreturned;
       	    obj.qtypending=objCabecera.qtypending;
            
        	obj.productkey=getDataSub1[j].productkey;
        	obj.productname=getDataSub1[j].productname;
        	obj.uomname=getDataSub1[j].uomname;
        	obj.qtyorderedline=getDataSub1[j].qtyorderedline;
        	obj.qtysalesorderedline=getDataSub1[j].qtysalesorderedline;
        	obj.qtyshipmentline=getDataSub1[j].qtyshipmentline;
        	obj.qtyreturnedline=getDataSub1[j].qtyreturnedline;
        	obj.qtypendingline=getDataSub1[j].qtypendingline;
        	
        	/* System.out.println(" obj.productname : " + obj.productname);
        	 System.out.println(" obj.qtysalesorderedline : " + obj.qtysalesorderedline);
        	 System.out.println(" obj.qtyshipmentline : " + obj.qtyshipmentline);
        	 System.out.println(" obj.qtyreturnedline : " + obj.qtyreturnedline);
        	 System.out.println(" obj.qtypendingline : " + obj.qtypendingline);
        	*/
            
            entroo=false;
            for(int k=0;k<getDataPartialFolio.length;k++ ){
            	obj.physicaldocnum=getDataPartialFolio[k].physicaldocnum;
            	obj.movementshipmentdate=getDataPartialFolio[k].movementshipmentdate;
            	obj.qtyshipmentocline=getDataPartialFolio[k].qtyshipmentocline;
            	obj.qtyreturnedocline=getDataPartialFolio[k].qtyreturnedocline;
            	
            	listData.add(obj);
            	entroo=true;
            }
            
            if(!entroo)listData.add(obj);
            entroo=true;
            
          }
          if(!entroo)listData.add(objCabecera);
          
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    
    dataFinal = new TrackingClientPurchaseOrderData [listData.size()]  ;
    
    for(int i=0;i<listData.size();i++){
    	
    	/*
    	System.out.println(" ===================== ");
    	
    	 System.out.println(" obj.qtyordered : " + listData.get(i).qtyordered);
    	 System.out.println(" obj.qtysalesordered : " + listData.get(i).qtysalesordered);
    	 System.out.println(" obj.qtyshipment : " + listData.get(i).qtyshipment);
    	 System.out.println(" obj.qtyreturned : " + listData.get(i).qtyreturned);
    	 System.out.println(" obj.qtypending : " + listData.get(i).qtypending);
    	 
    	 System.out.println(" obj.productname : " + listData.get(i).productname);
    	 System.out.println(" obj.qtysalesorderedline : " + listData.get(i).qtysalesorderedline);
    	 System.out.println(" obj.qtyshipmentline : " + listData.get(i).qtyshipmentline);
    	 System.out.println(" obj.qtyreturnedline : " + listData.get(i).qtyreturnedline);
    	 System.out.println(" obj.qtypendingline : " + listData.get(i).qtypendingline);
    	*/
    	 
    	 
    	 
    	 
    	dataFinal[i]=listData.get(i);
    }
    

    String strOutput;
    String strReportName;

    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/TrackingClientPurchaseOrder.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();


    renderJR(vars, response, strReportName, "Seguimiento_Ordenes_de_Cliente", strOutput,
        parameters, dataFinal, null);

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateTo,
      String strBpartnetId, String mProductId, String mStrNumOrder, String strShowPending) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    TrackingClientPurchaseOrderData[] getData = null;
    TrackingClientPurchaseOrderData[] getDataSub1 = null;
    TrackingClientPurchaseOrderData[] getDataPartialFolio = null;

    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/TrackingClientPurchaseOrder", discard).createXmlDocument();

    if (vars.commandIn("DEFAULT")) {
      xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
    }

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();

      // Map<TrackingClientPurchaseOrderData, TrackingClientPurchaseOrderData[]> resMap = new
      // HashMap<TrackingClientPurchaseOrderData, TrackingClientPurchaseOrderData[]>();
      Map<String, TrackingClientPurchaseOrderData[]> resMap = new HashMap<String, TrackingClientPurchaseOrderData[]>();
      Map<String, TrackingClientPurchaseOrderData[]> resMapSub = new HashMap<String, TrackingClientPurchaseOrderData[]>();

      try {
        String ad_org_id = strOrg;
        
        if(strShowPending!=null && strShowPending.equals("Y")){
        	getData = TrackingClientPurchaseOrderData.selectPurchaseOrdertoReviewPending(this, ad_org_id,
                    vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
                    mProductId, mStrNumOrder.toLowerCase());	
        }else{
         getData = TrackingClientPurchaseOrderData.selectPurchaseOrdertoReview(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
            mProductId, mStrNumOrder.toLowerCase());
        }
        
        for (int i = 0; i < getData.length; i++) {
          getDataSub1 = TrackingClientPurchaseOrderData.selectOrderClientLine(this, getData[i].purchasesalesorderid,
              mProductId);
          
          if(getData[i].partnername.length()> 35)
        	  getData[i].partnername = getData[i].partnername.substring(0, 30) + "..." ;
      
          resMap.put(getData[i].purchasesalesorderid, getDataSub1);
          for (int j = 0; j < getDataSub1.length; j++) {
        	  
        	if(getDataSub1[j].productname.length()> 50)
              	  getDataSub1[j].productname = getDataSub1[j].productname.substring(0, 50) + "..." ;  
        	  
            getDataPartialFolio = TrackingClientPurchaseOrderData.selectShipmentLine(this,
                getDataSub1[j].purchasesalesorderlineid);
            resMapSub.put(getDataSub1[j].purchasesalesorderlineid, getDataPartialFolio);
          }
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if ((getData == null || getData.length == 0)) {
          discard[0] = "selEliminar";
          getData = TrackingClientPurchaseOrderData.set();
          xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
        } else {
          String strLevel = "0"; // Level Tree Initial
          xmlDocument.setParameter("menu", loadNodes(getData, resMap, resMapSub, strLevel));

          // xmlDocument.setData("structure5", getData);
        }
      }

    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "TrackingClientPurchaseOrder", false, "", "",
          "", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "TrackingClientPurchaseOrder", false, "",
      // "",
      // "", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.setEmail(false);
      // toolbar.prepareSimpleToolBarTemplate();
      // // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.TrackingClientPurchaseOrder");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "TrackingClientPurchaseOrder.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "TrackingClientPurchaseOrder.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("TrackingClientPurchaseOrder");
        vars.removeMessage("TrackingClientPurchaseOrder");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "TrackingClientPurchaseOrder"), Utility.getContext(this, vars,
                "#User_Client", "TrackingClientPurchaseOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "TrackingClientPurchaseOrder", strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "AD_ORG_ID", "", "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
       * "#AccessibleOrgTree", "TrackingClientPurchaseOrder"), Utility.getContext(this, vars,
       * "#User_Client", "TrackingClientPurchaseOrder"), 0); Utility.fillSQLParameters(this, vars, null,
       * comboTableData, "TrackingClientPurchaseOrder", strOrg); xmlDocument.setData("reportTOAD_Org_ID",
       * "liststructure", comboTableData.select(false)); } catch (Exception ex) { throw new
       * ServletException(ex); }
       */

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("adOrgId", strOrg);

      xmlDocument.setParameter("paramBPartnerId", strBpartnetId);
      xmlDocument.setParameter("paramBPartnerDescription",
          TrackingClientPurchaseOrderData.selectBpartner(this, strBpartnetId));
      // xmlDocument.setParameter("adToOrgId", strToOrg);

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          TrackingClientPurchaseOrderData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("paramProductOrder", mStrNumOrder);
      xmlDocument.setParameter("paramShowPending", strShowPending);

      // xmlDocument.setParameter("menu", loadNodes(vars, "test"));
      // xmlDocument.setParameter("menu", "TEST");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "TrackingClientPurchaseOrder"), Utility.getContext(this, vars,
                "#User_Client", "TrackingClientPurchaseOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "TrackingClientPurchaseOrder", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private String strLevel_1(TrackingClientPurchaseOrderData data) {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1200px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"270\" class=\"DataGrid_Header_Cell\">Organización</th>");
    element.append("<th width=\"130\" class=\"DataGrid_Header_Cell\">Nro. Orden</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fec. Aprox</th>");
    element.append("<th width=\"400\" class=\"DataGrid_Header_Cell\">Proveedor</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Pedido</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Recibido</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Saldo</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Moneda</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Total</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Cierre</th>");

    element.append("</tr>");
    // CABECERA FIN
    //
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.orgname + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.documentno + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.datepromised + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.partnername + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtyordered + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtyreceived + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtydifference + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.scurrency + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.ordergrandtotal + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.cierramanual + "</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strLevel_2(TrackingClientPurchaseOrderData data) {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"900px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"15%\" class=\"DataGrid_Header_Cell\">Código</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Nro Partida</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Producto</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">U.M</th>");
    element.append("<th width=\"15%\" class=\"DataGrid_Header_Cell\">Pedido</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Recibido</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    element.append("<th width=\"5%\" class=\"DataGrid_Header_Cell\">P. Unitario</th>");

    element.append("</tr>");
    // CABECERA FIN
    //
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productkey + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.partidaarancel + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productname + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.uomname + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productqty + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productreceived + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productdiff + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productprice + "</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String Cabecera1() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"60%\" class=\"DataGrid_Header_Table \" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"50%\" class=\"DataGrid_Header_Cell\">Organization</th>");
    element.append("<th width=\"50%\" class=\"DataGrid_Header_Cell\">Product</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td  class=\"DataGrid_Body_Cell\">Grupo Coam - Regrión Lima</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\">SK-20010001 - Lentes</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strHeaderGridLevel_1() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1300px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"200\" class=\"DataGrid_Header_Cell\">Organización </th>");
    element.append("<th width=\"150\" class=\"DataGrid_Header_Cell\">Número de Orden </th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fecha O/C</th>");
    element.append("<th width=\"500\" class=\"DataGrid_Header_Cell\">Cliente </th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Cant O/C</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Enviado</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Devuelto</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">En Pedido</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strBodyGridLevel_1(TrackingClientPurchaseOrderData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1300px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td width=\"200\" class=\"DataGrid_Body_Cell\"> " + data.orgname + "</td>");
    element.append(" <td width=\"150\" class=\"DataGrid_Body_Cell\"> " + data.ordernum + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.orderdate  + "</td>");
    element.append(" <td width=\"470\" class=\"DataGrid_Body_Cell\"> " + data.partnername + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyordered + "</td>");
    element.append(" <td width=\"75\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtysalesordered + "</td>");
    element.append(" <td width=\"80\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreturned  + "</td>");
    element.append(" <td width=\"75\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyshipment  + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> "  + data.qtypending + "</td>");
    
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  private String strHeaderGridLevel_2() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1255px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Código</th>");
    element.append("<th width=\"500\" class=\"DataGrid_Header_Cell\">Producto</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">U.M</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Cant O/C</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Enviado</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Devuelto</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">En Pedido</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Saldo</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }
  
  private String strBodyGridLevel_2(TrackingClientPurchaseOrderData data) {
	    StringBuffer element = new StringBuffer();

	    element.append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1250px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
	    // CUERPO BEGIN
	    element.append("<tr>");
	    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.productkey + "</td>");
	    element.append(" <td width=\"500\" class=\"DataGrid_Body_Cell\"> " + data.productname + "</td>");
	    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell\"> " + data.uomname + "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyorderedline  + "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtysalesorderedline + "</td>");
	    element.append(" <td width=\"62\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreturnedline + "</td>");
	    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyshipmentline  + "</td>");
	    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtypendingline + "</td>");
	    element.append("</tr>");
	    // CUERPO END
	    element.append("</table>");
	    return element.toString();
	  }

  private String strHeaderGridLevel_3() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"805px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th  class=\"DataGrid_Header_Cell\">Nro Documento</th>");
    element.append("<th width=\"180\" class=\"DataGrid_Header_Cell\">Fecha Movimiento</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">Enviado</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">Devuelto</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  

  private String strBodyGridLevel_3(TrackingClientPurchaseOrderData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"800px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.physicaldocnum + "</td>");
    element.append(" <td width=\"180\" class=\"DataGrid_Body_Cell\"> " + data.movementshipmentdate + "</td>");
    element.append(" <td width=\"77\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyshipmentocline + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreturnedocline + "</td>");
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  // private String loadNodes(VariablesSecureApp vars, String key, TrackingClientPurchaseOrderData[]
  // getData)
  private String loadNodes(TrackingClientPurchaseOrderData[] dataPut,
      Map<String, TrackingClientPurchaseOrderData[]> resMapAsociate,
      Map<String, TrackingClientPurchaseOrderData[]> resMapSub, String strLevel) throws ServletException {
    String TreeType = "00";
    String TreeID = "ID_REF";
    // String TreeName = "PRUEBA";
    String TreeDescription = "";
    StringBuffer menu = new StringBuffer();

    menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");

    if (dataPut != null) {
      if (strLevel.equals("0"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_1(), null, null, true, TreeID));
      else if (strLevel.equals("1"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_2(), null, null, true, TreeID));
      else if (strLevel.equals("2"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_3(), null, null, true, TreeID));
    }

    if (dataPut != null)
      menu.append(generateTree(dataPut, resMapAsociate, resMapSub, true, strLevel));

    menu.append("\n</ul>\n");

    /*
     * if(strLevel.equals("0")){ menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_1(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, resMapAsociate,resMapSub, true,"0"));
     * menu.append("\n</ul>\n"); } else if(strLevel.equals("1")){
     * menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_2(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, resMapAsociate,null,true,"1"));
     * menu.append("\n</ul>\n"); } else if(strLevel.equals("2")){
     * menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_3(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, null,null,true,"2"));
     * menu.append("\n</ul>\n"); }
     */

    // nodeIdList = null;
    return menu.toString();
  }

  private static Map<String, List<WindowTreeData>> buildTree(WindowTreeData[] input) {
    Map<String, List<WindowTreeData>> resMap = new HashMap<String, List<WindowTreeData>>();

    for (WindowTreeData elem : input) {
      List<WindowTreeData> list = resMap.get(elem.parentId);
      if (list == null) {
        list = new ArrayList<WindowTreeData>();
      }
      list.add(elem);
      resMap.put(elem.parentId, list);
    }

    return resMap;
  }

  private String generateTree(TrackingClientPurchaseOrderData[] data,
      Map<String, TrackingClientPurchaseOrderData[]> resMap,
      Map<String, TrackingClientPurchaseOrderData[]> resMapSub, boolean haschild, String strlevel)
      throws ServletException {

    boolean hayDatos = false;
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("<ul style=\"display:block\">");

    String tosetTree = null;

    if (strlevel.equals("0")) {

      for (int i = 0; i < data.length; i++) {
        // tosetTree = strLevel_1(data[i]);

        tosetTree = strBodyGridLevel_1(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        TrackingClientPurchaseOrderData[] dataline = resMap.get(data[i].purchasesalesorderid);

        if (dataline != null)
          strResultado.append(loadNodes(dataline, resMapSub, null, "1"));

      }

    } else if (strlevel.equals("1")) {
      for (int i = 0; i < data.length; i++) {
        // tosetTree = strLevel_2(data[i]);
        tosetTree = strBodyGridLevel_2(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        // TrackingClientPurchaseOrderData[] dataline = resMapSub.get(data[i].orderimportlineid); //este es
        TrackingClientPurchaseOrderData[] dataline = resMap.get(data[i].purchasesalesorderlineid);
        if (dataline != null) {
          strResultado.append(loadNodes(dataline, null, null, "2"));
        }

      }

    } else if (strlevel.equals("2")) {
      for (int i = 0; i < data.length; i++) {

        tosetTree = strBodyGridLevel_3(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));

      }

    }

    strResultado.append("</li></ul>");
    // return (hayDatos ? strResultado.toString() : "");
    return (strResultado.toString());
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
