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
package pe.com.unifiedgo.warehouse.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.bytecode.buildtime.ExecutionException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.PriceAdjustment;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBErrorBuilder;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.financialmgmt.payment.Incoterms;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;

import pe.com.unifiedgo.warehouse.ad_actionButton.replicateLinesGoodsRecipt;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

import java.util.Enumeration;


public class replicateLinesGoodsRecipt extends HttpSecureAppServlet {
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

 
  private String strClientId= "";
  private String strOrgId= "";

  private String strTabId = "";
  private String strWindowId = "";
  
  private String strMInOutLineId = "";
  private String strMProductId ="";
  private String strQytToDistribute = "";
  private String strWarehouseId= "";

  
  
  
  
  
  private Order order=null;
  long lineNo;
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }
  
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
   // Order Order = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {
    	  
	   	 strWindowId = vars.getStringParameter("inpwindowId");
         strClientId = vars.getStringParameter("inpadClientId");
         strOrgId = vars.getStringParameter("inpadOrgId");
         strTabId = vars.getStringParameter("inpTabId");
         
         
         strWarehouseId = vars.getStringParameter("inpmWarehouseId");
         strMInOutLineId = vars.getStringParameter("inpmInoutlineId");
         String strMioLineId = strMInOutLineId;
         vars.setSessionValue("strmInoutLineId", strMioLineId);
         strMProductId = vars.getStringParameter("inpmProductId");
         strQytToDistribute = vars.getStringParameter("inpmovementqty");
         
     	 String strQyttoDistribute= strQytToDistribute;
    	 String strIncludeVendor = vars.getGlobalVariable("inpShowNullVendor",
    	          "replicateLinesGoodsRecipt|ShowNullVendor", "Y");
    	 String strCheckedbyrbtn = "pending";
    	 
       	 printPageDataSheet(response, vars, 
                     strWindowId, 
                     strTabId, 
                     strQyttoDistribute, strIncludeVendor,strCheckedbyrbtn);
        
	   		
		
    } 
    else if (vars.commandIn("SEARCH")){
         String strIncludeVendor = vars.getStringParameter("inpShowNullVendor");
   	     String strQytToD = strQytToDistribute;
   	     String strCheckedbyrbtn = vars.getStringParameter("inprbtnSearch");
   	     
   	     printPageDataSheet(response, vars, 
               strWindowId, 
               strTabId, 
               strQytToD, strIncludeVendor,strCheckedbyrbtn); 
    	
    }
    else if (vars.commandIn("SAVE")) {
    	String[] selectedLines = null;
    	String strLocatorID = null;
    	int tmp=0;
    	//Order order = null;
    	  
        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
            strWindowPath = strDefaultServlet;

        
        try {
        	strLocatorID = vars.getRequiredInStringParameter("inpRownumId",IsIDFilter.instance);
        	strLocatorID = strLocatorID.replace("(", "");
            strLocatorID = strLocatorID.replace(")", "");
            strLocatorID = strLocatorID.replace("'", "");
            selectedLines = strLocatorID.split(",");
            
        } catch (Exception e) {
        	tmp=1;
   	        OBError myError;
  	        myError = new OBError();
  	        myError.setType("Error");
  	        //myError.setTitle(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
  	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
  	        vars.setMessage(strTabId, myError);
          }
        
       // if(tmp==0){
        	OBError myError;
            myError = new OBError();
            myError = distributeInoutLine(vars , selectedLines);
          //  vars.setMessage(strTabId, myError);
        //}
       // distributeInoutLine2(vars, selectedLines);
        //vars.setMessage(strTabId, myError);

             
        printPageClosePopUp(response, vars, strWindowPath);
    } 
  }
    
     
 private OBError distributeInoutLine(VariablesSecureApp vars, String[]LocatorsArray) {
	  OBError myError;
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(OBMessageUtils.messageBD("Success"));
      myError.setMessage(OBMessageUtils.messageBD("swa_sucesdistributelines"));
      
      Long lines=0L;
      
      try {
    	 String strMIoLineId = vars.getSessionValue("strmInoutLineId");
         ShipmentInOutLine inoutLineFrom = OBDal.getInstance().get(ShipmentInOutLine.class, strMIoLineId);
         if(inoutLineFrom==null){
			myError.setType("Error");
			myError.setTitle(OBMessageUtils.messageBD("Error"));
	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
	        return myError;
	     }		
        lines =  (long) inoutLineFrom.getShipmentReceipt().getMaterialMgmtShipmentInOutLineList().size();
        lines = lines*10;
        
        BigDecimal qtyFromLineTOTAL = inoutLineFrom.getMovementQuantity();
        if(inoutLineFrom.getShipmentReceipt().isSalesTransaction()){
        	qtyFromLineTOTAL = inoutLineFrom.getMovementQuantity().multiply(new BigDecimal(-1));
        }
        
        
        BigDecimal qtytoInsert = BigDecimal.ZERO;
        BigDecimal qtyTmp = BigDecimal.ZERO;
        BigDecimal qtyConverFinalToInsert = BigDecimal.ZERO;
      
      	for(int i = 0; LocatorsArray != null && i < LocatorsArray.length; i++) {
      		try {
      		    
      			Locator bin = OBDal.getInstance().get(Locator.class, LocatorsArray[i].trim());

      			if(bin==null){
        			myError.setType("Error");
        			myError.setTitle(OBMessageUtils.messageBD("Error"));
        	        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
        	        return myError;
        		}
      			
          		String qtyInsert = vars.getNumericParameter("inpOrderQty" + LocatorsArray[i].trim());
          		qtytoInsert = new BigDecimal(qtyInsert);
          		qtyTmp = qtyTmp.add(qtytoInsert);
      			//solo para validacion
          		
      			
          		qtyConverFinalToInsert = qtytoInsert;
          		if(inoutLineFrom.getShipmentReceipt().isSalesTransaction()){
          			qtyConverFinalToInsert =  qtytoInsert.multiply(new BigDecimal(-1));
          		}
          		
          		lines = lines+ 10;
      			ShipmentInOutLine newLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
      			newLine.setOrganization(inoutLineFrom.getOrganization());
      			newLine.setLineNo(lines);
      			newLine.setShipmentReceipt(inoutLineFrom.getShipmentReceipt());
      			newLine.setSalesOrderLine(inoutLineFrom.getSalesOrderLine());
      			newLine.setProduct(inoutLineFrom.getProduct());
      			newLine.setUOM(inoutLineFrom.getUOM());
      			newLine.setStorageBin(bin);
      			newLine.setMovementQuantity(qtyConverFinalToInsert);
      			newLine.setBusinessPartner(inoutLineFrom.getBusinessPartner());
      			newLine.setSCOSpecialDocType(inoutLineFrom.getSCOSpecialDocType());
      			OBDal.getInstance().save(newLine);
      			OBDal.getInstance().flush();
			} catch (Exception e) {
				myError.setType("Error");
      			myError.setTitle(OBMessageUtils.messageBD("Error"));
      	        myError.setMessage(e.getMessage());
      	        return myError;
			}
      	}
      	
      	if(qtyFromLineTOTAL.compareTo(qtyTmp)==1){
      		
      		qtyConverFinalToInsert = qtyTmp;
      		if(inoutLineFrom.getShipmentReceipt().isSalesTransaction()){
      			qtyConverFinalToInsert =  qtyTmp.multiply(new BigDecimal(-1));
      		}
      		inoutLineFrom.setMovementQuantity(qtyFromLineTOTAL.subtract(qtyConverFinalToInsert));
      		OBDal.getInstance().save(inoutLineFrom);
      		OBDal.getInstance().flush();
      	}
      	else{
      		String Query = "delete from m_inoutline where m_inoutline_id = '" +inoutLineFrom.getId() +"'";
      		
      	    Query q2 = OBDal
  	            .getInstance()
  	            .getSession()
  	            .createSQLQuery(Query);
  	        String C_Tax_ID = (String) q2.uniqueResult();
  	        
      		OBDal.getInstance().remove(inoutLineFrom);
      	    OBDal.getInstance().flush();
      	}
      	
  	 } catch (Exception e) {
  		    myError.setType("Error");
			myError.setTitle(OBMessageUtils.messageBD("Error"));
	        myError.setMessage(e.getMessage());
	        return myError;
  	 }
      
    return myError;
 }
  
 
 
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
                                  String strWindowId,
                                  String strTabId, 
                                  String strQyttoDistribute,
                                  String strChkLocatorOut,
                                  String strCheckedbyrbtn) throws IOException, ServletException {
	   
	  replicateLinesGoodsReciptData[] data = null;
	  replicateLinesGoodsReciptData[] dataLines = null;
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
		        "pe/com/unifiedgo/warehouse/ad_actionButton/replicateLinesGoodsRecipt").createXmlDocument();

	    OBError myMessage = null;
	      myMessage = new OBError();
	      try {
	    		 data = replicateLinesGoodsReciptData.selectLocator(this, strWarehouseId,strMProductId, strChkLocatorOut,strCheckedbyrbtn );
	      } catch (ServletException ex) {
	        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
	      }
	      

	    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
	    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
	    xmlDocument.setParameter("theme", vars.getTheme());
	    xmlDocument.setParameter("windowId", strWindowId);
	    xmlDocument.setParameter("tabId", strTabId);
	    
	    String strMInoutLineId = vars.getSessionValue("strmInoutLineId");
	    ShipmentInOutLine inoutLine = OBDal.getInstance().get(ShipmentInOutLine.class, strMInoutLineId.trim());
	    
	    if(inoutLine != null){
	    	strQyttoDistribute = (inoutLine.getMovementQuantity().compareTo(BigDecimal.ZERO)==1?inoutLine.getMovementQuantity().toString() : inoutLine.getMovementQuantity().multiply(new BigDecimal(-1)).toString());
	    }
	    
	    xmlDocument.setData("structure5", data);
	    xmlDocument.setParameter("paramqtytoDistribute", strQyttoDistribute);
	    xmlDocument.setParameter("paramShowNullVendor", strChkLocatorOut);
	    
	    
	    xmlDocument.setParameter("pending", strCheckedbyrbtn);
	    xmlDocument.setParameter("created", strCheckedbyrbtn);
	    xmlDocument.setParameter("allbin", strCheckedbyrbtn);
	      
	    response.setContentType("text/html; charset=UTF-8");
	    PrintWriter out = response.getWriter();
	    out.println(xmlDocument.print());
	    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
