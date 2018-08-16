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

public class TrackingNationalPurchaseOrder extends HttpSecureAppServlet {
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
      String strOrg = vars.getGlobalVariable("inpOrg", "TrackingNationalPurchaseOrder|Org", "0");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "TrackingNationalPurchaseOrder|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "TrackingNationalPurchaseOrder|inpDateTo",
          SREDateTimeData.today(this));
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "TrackingNationalPurchaseOrder|CB_PARTNER_ID", "");

      String mProductId = vars.getGlobalVariable("inpmProductId",
          "TrackingNationalPurchaseOrder|M_Product_Id", "");

      String strNumOrder = vars.getStringParameter("paramProductOrder");
      
      String strShowPending = vars.getGlobalVariable("inpShowPending", "TrackingNationalPurchaseOrder|ShowPending", "N");

    
      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateto, strcBpartnetId,
          mProductId, strNumOrder,strShowPending);

    } else if (vars.commandIn("LISTAR")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String mProductId = vars.getStringParameter("inpmProductId");
      String strNumOrder = vars.getStringParameter("paramProductOrder");
      
      String strShowPending = vars.getGlobalVariable("inpShowPending", "TrackingNationalPurchaseOrder|ShowPending");
      String strPending = vars.getStringParameter("inpShowPending");
    
      
      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateTo, strcBpartnetId,
          mProductId, strNumOrder.trim(), strPending);
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
/*
  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateTo,
	      String strBpartnetId, String mProductId, String mStrNumOrder) throws IOException,
      ServletException {

    TrackingNationalPurchaseOrderData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();
    
    TrackingNationalPurchaseOrderData[] getData = null;
    TrackingNationalPurchaseOrderData[] getDataSub1 = null;
    TrackingNationalPurchaseOrderData[] getDataPartialFolio = null;
    
    TrackingNationalPurchaseOrderData[] dataFinal = null;

    ArrayList<TrackingNationalPurchaseOrderData> listData= new ArrayList<TrackingNationalPurchaseOrderData>();
    
    boolean entroo=false;

    try {
        String ad_org_id = strOrg;
        
        getData = TrackingNationalPurchaseOrderData.selectPurchaseOrdertoReview(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
            mProductId, mStrNumOrder);
        
        if (getData.length==0) {
            advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
            return;
          }

        

        for (int i = 0; i < getData.length; i++) {
          getDataSub1 = TrackingNationalPurchaseOrderData.selectOrderClientLine(this, getData[i].purchasesalesorderid,
              mProductId);
          
       TrackingNationalPurchaseOrderData obj= new TrackingNationalPurchaseOrderData ();

          
      	obj.orgname=getData[i].orgname;
    	obj.ordernum=getData[i].ordernum;
    	obj.orderdate=getData[i].orderdate;
    	obj.partnername=getData[i].partnername;
    	obj.qtyordered=getData[i].qtyordered;
    	obj.qtysalesordered=getData[i].qtysalesordered;
    	obj.qtyshipment=getData[i].qtyshipment;
    	obj.qtyreturned=getData[i].qtyreturned;
    	obj.qtypending=getData[i].qtypending;
  
          entroo=false;
          for (int j = 0; j < getDataSub1.length; j++) {
            getDataPartialFolio = TrackingNationalPurchaseOrderData.selectShipmentLine(this,
                getDataSub1[j].purchasesalesorderlineid);
            
        	obj.productkey=getDataSub1[j].productkey;
        	obj.productname=getDataSub1[j].productname;
        	obj.uomname=getDataSub1[j].uomname;
        	obj.qtyorderedline=getDataSub1[j].qtyorderedline;
        	obj.qtysalesorderedline=getDataSub1[j].qtysalesorderedline;
        	obj.qtyshipmentline=getDataSub1[j].qtyshipmentline;
        	obj.qtyreturnedline=getDataSub1[j].qtyreturnedline;
        	obj.qtypendingline=getDataSub1[j].qtypendingline;
            
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
          if(!entroo)listData.add(obj);
          
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    
    dataFinal = new TrackingNationalPurchaseOrderData [listData.size()]  ;
    
    for(int i=0;i<listData.size();i++){
    	dataFinal[i]=listData.get(i);
    }
    

    String strOutput;
    String strReportName;

    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/TrackingNationalPurchaseOrder.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();


    renderJR(vars, response, strReportName, "Seguimiento_Ordenes_de_Cliente", strOutput,
        parameters, dataFinal, null);

  }*/
  
  
  
  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateTo,
	      String strBpartnetId, String mProductId, String mStrNumOrder, String strShowPending) throws IOException,
      ServletException {

    TrackingNationalPurchaseOrderData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();
    
    TrackingNationalPurchaseInfoData[] getDataInfo = null;
    TrackingNationalPurchaseInfoData[] getDataLines = null;
    TrackingNationalPurchaseInfoData[] getDataReceipt = null;
    
    TrackingNationalPurchaseInfoData[] dataFinal = null;

    ArrayList<TrackingNationalPurchaseInfoData> listData= new ArrayList<TrackingNationalPurchaseInfoData>();
    
    boolean entroo=false;

    try {
        String ad_org_id = strOrg;
        
        if(strShowPending!=null && strShowPending.equals("Y"))
            getDataInfo = TrackingNationalPurchaseInfoData.selectDataLevel1OnlyPending(this, vars.getSessionValue("#AD_CLIENT_ID"), ad_org_id, strDocDate, strDateTo, mProductId, strBpartnetId, mStrNumOrder.toLowerCase());
          else
            getDataInfo = TrackingNationalPurchaseInfoData.selectDataLevel1(this, vars.getSessionValue("#AD_CLIENT_ID"), ad_org_id, strDocDate, strDateTo, mProductId, strBpartnetId, mStrNumOrder.toLowerCase());
        
        
        if (getDataInfo.length==0) {
            advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
            return;
          }

        

        for (int i = 0; i < getDataInfo.length; i++) {

        	
        	getDataLines = TrackingNationalPurchaseInfoData.selectDataLevel2(this, getDataInfo[i].orderid);
          
        	TrackingNationalPurchaseInfoData obj= new TrackingNationalPurchaseInfoData ();
          
	      	obj.orgname=getDataInfo[i].orgname;
	    	obj.documentno=getDataInfo[i].documentno;
	    	obj.isserviceorder =getDataInfo[i].isserviceorder;
	    	obj.dateordered=getDataInfo[i].dateordered;
	    	obj.partnername=getDataInfo[i].partnername;
	    	obj.qtyordered=getDataInfo[i].qtyordered;
	    	obj.qtyreceived=getDataInfo[i].qtyreceived;
	    	obj.qtydifference=getDataInfo[i].qtydifference; //Saldo
	    	obj.cursymbol=getDataInfo[i].cursymbol;
	    	obj.totalamount=getDataInfo[i].totalamount;
	    	obj.qtyclose=getDataInfo[i].qtyclose;
  
          entroo=false;
          for (int j = 0; j < getDataLines.length; j++) {
        	  
            /*getDataPartialFolio = TrackingNationalPurchaseOrderData.selectShipmentLine(this,
                getDataSub1[j].purchasesalesorderlineid);*/
            
      	    getDataReceipt = TrackingNationalPurchaseInfoData.selectDataLevel3Enhanced(this, getDataLines[j].orderlineid);
 
        	obj= new TrackingNationalPurchaseInfoData ();
            
	      	obj.orgname=getDataInfo[i].orgname;
	    	obj.documentno=getDataInfo[i].documentno;
	    	obj.isserviceorder =getDataInfo[i].isserviceorder;
	    	obj.dateordered=getDataInfo[i].dateordered;
	    	obj.partnername=getDataInfo[i].partnername;
	    	obj.qtyordered=getDataInfo[i].qtyordered;
	    	obj.qtyreceived=getDataInfo[i].qtyreceived;
	    	obj.qtydifference=getDataInfo[i].qtydifference; //Saldo
	    	obj.cursymbol=getDataInfo[i].cursymbol;
	    	obj.totalamount=getDataInfo[i].totalamount;
	    	obj.qtyclose=getDataInfo[i].qtyclose;
            
        	obj.productcode=getDataLines[j].productcode;
        	obj.productname=getDataLines[j].productname;
        	obj.uomname=getDataLines[j].uomname;
        	obj.qtyordered2=getDataLines[j].qtyordered;
        	obj.qtyreceived2=getDataLines[j].qtyreceived;
        	obj.qtydifference2=getDataLines[j].qtydifference;
        	obj.priceactual=getDataLines[j].priceactual;
        	obj.orderreference=getDataLines[j].orderreference;
            
            entroo=false;
            for(int k=0;k<getDataReceipt.length;k++ ){
            	
            	obj= new TrackingNationalPurchaseInfoData ();
                
    	      	obj.orgname=getDataInfo[i].orgname;
    	    	obj.documentno=getDataInfo[i].documentno;
    	    	obj.isserviceorder =getDataInfo[i].isserviceorder;
    	    	obj.dateordered=getDataInfo[i].dateordered;
    	    	obj.partnername=getDataInfo[i].partnername;
    	    	obj.qtyordered=getDataInfo[i].qtyordered;
    	    	obj.qtyreceived=getDataInfo[i].qtyreceived;
    	    	obj.qtydifference=getDataInfo[i].qtydifference; //Saldo
    	    	obj.cursymbol=getDataInfo[i].cursymbol;
    	    	obj.totalamount=getDataInfo[i].totalamount;
    	    	obj.qtyclose=getDataInfo[i].qtyclose;
    	    	
            	obj.productcode=getDataLines[j].productcode;
            	obj.productname=getDataLines[j].productname;
            	obj.uomname=getDataLines[j].uomname;
            	obj.qtyordered2=getDataLines[j].qtyordered;
            	obj.qtyreceived2=getDataLines[j].qtyreceived;
            	obj.qtydifference2=getDataLines[j].qtydifference;
            	obj.priceactual=getDataLines[j].priceactual;
            	obj.orderreference=getDataLines[j].orderreference;
            	
            	obj.receiptdocno=getDataReceipt[k].receiptdocno;
            	obj.productiondocno=getDataReceipt[k].productiondocno;
            	obj.movementname=getDataReceipt[k].movementname;
            	obj.movementdate=getDataReceipt[k].movementdate;
            	obj.qtyordered3=getDataReceipt[k].qtyordered;
            	obj.physicalinvdocno=getDataReceipt[k].physicalinvdocno;
            	obj.dateinvoiced=getDataReceipt[k].dateinvoiced;
            	obj.ispaid=getDataReceipt[k].ispaid;
            	obj.invoicedatedue=getDataReceipt[k].invoicedatedue;
            	
            	listData.add(obj);
            	entroo=true;
            }
            
            if(!entroo)listData.add(obj);
            entroo=true;
            
          }
          if(!entroo)listData.add(obj);
          
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
    
    dataFinal = new TrackingNationalPurchaseInfoData [listData.size()]  ;
    
    for(int i=0;i<listData.size();i++){
    	dataFinal[i]=listData.get(i);
    }
    

    String strOutput;
    String strReportName;

    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/TrackingNationalPurchaseOrder.jrxml";

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
    
    TrackingNationalPurchaseInfoData[] getDataInfo = null;
    TrackingNationalPurchaseInfoData[] getDataLines = null;
    TrackingNationalPurchaseInfoData[] getDataReceipt = null;
    
   // TrackingNationalPurchaseOrderData[] getData = null;
   // TrackingNationalPurchaseOrderData[] getDataSub1 = null;
   // TrackingNationalPurchaseOrderData[] getDataReceipt = null;

    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/TrackingNationalPurchaseOrder", discard).createXmlDocument();

    if (vars.commandIn("DEFAULT")) {
      xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
    }

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();

      // Map<TrackingNationalPurchaseOrderData, TrackingNationalPurchaseOrderData[]> resMap = new
      // HashMap<TrackingNationalPurchaseOrderData, TrackingNationalPurchaseOrderData[]>();
      //Map<String, TrackingNationalPurchaseOrderData[]> resMap = new HashMap<String, TrackingNationalPurchaseOrderData[]>();
      //Map<String, TrackingNationalPurchaseOrderData[]> resMapSub = new HashMap<String, TrackingNationalPurchaseOrderData[]>();
      
      Map<String, TrackingNationalPurchaseInfoData[]> resMap = new HashMap<String, TrackingNationalPurchaseInfoData[]>();
      Map<String, TrackingNationalPurchaseInfoData[]> resMapSub = new HashMap<String, TrackingNationalPurchaseInfoData[]>();

      try {
        String ad_org_id = strOrg;
        
        if(strShowPending!=null && strShowPending.equals("Y"))
          getDataInfo = TrackingNationalPurchaseInfoData.selectDataLevel1OnlyPending(this, vars.getSessionValue("#AD_CLIENT_ID"), ad_org_id, strDocDate, strDateTo, mProductId, strBpartnetId, mStrNumOrder.toLowerCase());
        else
          getDataInfo = TrackingNationalPurchaseInfoData.selectDataLevel1(this, vars.getSessionValue("#AD_CLIENT_ID"), ad_org_id, strDocDate, strDateTo, mProductId, strBpartnetId, mStrNumOrder.toLowerCase());
        
        
       for (int i = 0; i < getDataInfo.length; i++) {
        	if(getDataInfo[i].partnername.length()> 35)
        		getDataInfo[i].partnername = getDataInfo[i].partnername.substring(0, 30) + "..." ;
        	
        	getDataLines = TrackingNationalPurchaseInfoData.selectDataLevel2(this, getDataInfo[i].orderid);
        	resMap.put(getDataInfo[i].orderid, getDataLines);

          for (int j = 0; j < getDataLines.length; j++) {
        	  
        	  if(getDataLines[j].productname.length()> 50)
        		  getDataLines[j].productname = getDataLines[j].productname.substring(0, 50) + "..." ;  
        	  
        	  getDataReceipt = TrackingNationalPurchaseInfoData.selectDataLevel3Enhanced(this, getDataLines[j].orderlineid);
        	  resMapSub.put(getDataLines[j].orderlineid, getDataReceipt);

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
        if ((getDataInfo == null || getDataInfo.length == 0)) {
          discard[0] = "selEliminar";
          getDataInfo = TrackingNationalPurchaseInfoData.set();
          xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
        } else {
          String strLevel = "0"; // Level Tree Initial
          xmlDocument.setParameter("menu", loadNodes(getDataInfo, resMap, resMapSub, strLevel));
        }
      }

    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "TrackingNationalPurchaseOrder", false, "", "",
          "", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "TrackingNationalPurchaseOrder", false, "",
      // "",
      // "", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.setEmail(false);
      // toolbar.prepareSimpleToolBarTemplate();
      // // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.TrackingNationalPurchaseOrder");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "TrackingNationalPurchaseOrder.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "TrackingNationalPurchaseOrder.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("TrackingNationalPurchaseOrder");
        vars.removeMessage("TrackingNationalPurchaseOrder");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "TrackingNationalPurchaseOrder"), Utility.getContext(this, vars,
                "#User_Client", "TrackingNationalPurchaseOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "TrackingNationalPurchaseOrder", strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "AD_ORG_ID", "", "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
       * "#AccessibleOrgTree", "TrackingNationalPurchaseOrder"), Utility.getContext(this, vars,
       * "#User_Client", "TrackingNationalPurchaseOrder"), 0); Utility.fillSQLParameters(this, vars, null,
       * comboTableData, "TrackingNationalPurchaseOrder", strOrg); xmlDocument.setData("reportTOAD_Org_ID",
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
          TrackingNationalPurchaseOrderData.selectBpartner(this, strBpartnetId));
      // xmlDocument.setParameter("adToOrgId", strToOrg);

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          TrackingNationalPurchaseOrderData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("paramProductOrder", mStrNumOrder);
      
      xmlDocument.setParameter("paramShowPending", strShowPending);

      // xmlDocument.setParameter("menu", loadNodes(vars, "test"));
      // xmlDocument.setParameter("menu", "TEST");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "TrackingNationalPurchaseOrder"), Utility.getContext(this, vars,
                "#User_Client", "TrackingNationalPurchaseOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "TrackingNationalPurchaseOrder", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      out.println(xmlDocument.print());
      out.close();
    }
  }

 
  private String strHeaderGridLevel_1() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1205px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"200\" class=\"DataGrid_Header_Cell\">Organización </th>");
    element.append("<th width=\"150\" class=\"DataGrid_Header_Cell\">Número de Orden </th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Servicio </th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fecha Emisión</th>");
    element.append("<th width=\"400\" class=\"DataGrid_Header_Cell\">Proveedor </th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Ctd. Pedido</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Ctd. Recibido</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    element.append("<th width=\"10\" class=\"DataGrid_Header_Cell\">Moneda</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Total</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Ctd. Cierre</th>");
    
    

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

  private String strBodyGridLevel_1(TrackingNationalPurchaseInfoData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1200px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td width=\"200\" class=\"DataGrid_Body_Cell\"> " + data.orgname + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.documentno + "</td>");
    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell\"> " + data.isserviceorder + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.dateordered  + "</td>");
    element.append(" <td width=\"300\" class=\"DataGrid_Body_Cell\"> " + data.partnername + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyordered + "</td>");
    element.append(" <td width=\"75\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreceived + "</td>");
    element.append(" <td width=\"80\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtydifference  + "</td>");
    element.append(" <td width=\"40\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.cursymbol  + "</td>");
    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.totalamount  + "</td>");
    element.append(" <td width=\"75\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyclose  + "</td>");
    
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
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1080px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Código</th>");
    element.append("<th width=\"550\" class=\"DataGrid_Header_Cell\">Producto</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">U.M</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Cant Pedido</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Cant Recibido</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">P. Unitario</th>");
    element.append("<th width=\"150\" class=\"DataGrid_Header_Cell\">Nro. Reque.</th>");

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
  
  private String strBodyGridLevel_2(TrackingNationalPurchaseInfoData data) {
	    StringBuffer element = new StringBuffer();

	    element.append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1080px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
	    // CUERPO BEGIN
	    element.append("<tr>");
	    
	    element.append(" <td width=\"80\" class=\"DataGrid_Body_Cell\"> " + data.productcode + "</td>");
	    element.append(" <td width=\"450\" class=\"DataGrid_Body_Cell\"> " + data.productname + "</td>");
	    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell\"> " + data.uomname + "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyordered  + "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreceived+ "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtydifference+ "</td>");
	    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.priceactual  + "</td>");
	    element.append(" <td width=\"130\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.orderreference + "</td>");
	    
	    
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
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1060px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"180\"  class=\"DataGrid_Header_Cell\">Nro Ingreso</th>");
    element.append("<th width=\"180\" class=\"DataGrid_Header_Cell\">Nro Armado</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Tipo de Mov.</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fecha Movimiento</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Recibido</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Nro. Factura</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fecha Factura</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Pagada</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fecha Vcto</th>");

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

  

  private String strBodyGridLevel_3(TrackingNationalPurchaseInfoData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1060px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    
    
    element.append(" <td width=\"180\" class=\"DataGrid_Body_Cell\"> " + data.receiptdocno + "</td>");
    element.append(" <td width=\"180\" class=\"DataGrid_Body_Cell\"> " + data.productiondocno + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.movementname + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.movementdate + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyordered + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.dateinvoiced + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.physicalinvdocno + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.ispaid + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.invoicedatedue + "</td>");
    
    
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  // private String loadNodes(VariablesSecureApp vars, String key, TrackingNationalPurchaseOrderData[]
  // getData)
  private String loadNodes(TrackingNationalPurchaseInfoData[] dataPut,
      Map<String, TrackingNationalPurchaseInfoData[]> resMapAsociate,
      Map<String, TrackingNationalPurchaseInfoData[]> resMapSub, String strLevel) throws ServletException {
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

  private String generateTree(TrackingNationalPurchaseInfoData[] data,
      Map<String, TrackingNationalPurchaseInfoData[]> resMap,
      Map<String, TrackingNationalPurchaseInfoData[]> resMapSub, boolean haschild, String strlevel)
      throws ServletException {

    boolean hayDatos = false;
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("<ul style=\"display:block\">");

    String tosetTree = null;

    if (strlevel.equals("0")) {

      for (int i = 0; i < data.length; i++) {
        tosetTree = strBodyGridLevel_1(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        
        TrackingNationalPurchaseInfoData[] dataline = resMap.get(data[i].orderid);

        if (dataline != null)
          strResultado.append(loadNodes(dataline, resMapSub, null, "1"));

      }

    } else if (strlevel.equals("1")) {
      for (int i = 0; i < data.length; i++) {
        tosetTree = strBodyGridLevel_2(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        
        TrackingNationalPurchaseInfoData[] dataline = resMap.get(data[i].orderlineid);
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
