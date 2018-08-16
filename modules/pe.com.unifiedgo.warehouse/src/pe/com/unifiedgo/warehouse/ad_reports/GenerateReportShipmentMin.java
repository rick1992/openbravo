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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.warehouse.ad_reports;

//import PickingList;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.costing.CostingStatus;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.TreeData;
//import org.openbravo.erpCommon.ad_forms.RequisitionToOrderData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.report.ad_reports.DetalleProductosVendidosJRData;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;

public class GenerateReportShipmentMin extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection conn = null;
    OBError myMessage = null;
    String var = null;
    // HttpSession mysessions = request.getSession();
    // String[] mysess_array = mysessions.getValueNames();
    String strWarehouseLocal = vars.getSessionValue("#M_WAREHOUSE_ID");

    myMessage = new OBError();
    myMessage.setTitle("");
    SwaRequerimientoReposicion reposition = null;
    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strWarehouse = vars.getGlobalVariable("inpmWarehouseId",
          "GenerateReportShipmentMin|M_Warehouse_ID", "");
      String strWarehouseSource = vars.getGlobalVariable("inpmWarehouseSourceId",
              "GenerateReportShipmentMin|M_Warehouse_ID", "");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId",
          "GenerateReportShipmentMin|AD_Org_ID", "");
      String strAD_Org_Child_ID = vars.getGlobalVariable("inpadOrgChildId",
              "GenerateReportShipmentMin|AD_Org_ID", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "GenerateReportShipmentMin|currency", strUserCurrencyId);
      strCurrencyId = "0";
      String strQuantity = vars.getStringParameter("Quantity");
      if (strQuantity == null || "".equals(strQuantity))
        strQuantity = "1";
      String strQuantity2 = vars.getStringParameter("Quantity2");
      if (strQuantity2 == null || "".equals(strQuantity2))
        strQuantity2 = "365";
      printPageDataSheet(request, response, vars, strWarehouse, strWarehouseSource, strAD_Org_ID, strAD_Org_Child_ID, strClient,
          strCurrencyId, strQuantity,strQuantity2,true);
    } else if (vars.commandIn("FIND")) {
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateReportShipmentMin|M_Warehouse_ID");
      String strWarehouseSource = vars.getRequestGlobalVariable("inpmWarehouseSourceId",
              "GenerateReportShipmentMin|M_Warehouse_ID");
      String strClient = vars.getClient();
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "GenerateReportShipmentMin|AD_Org_ID");
      String strAD_Org_Child_ID = vars.getGlobalVariable("inpadOrgChildId",
              "GenerateReportShipmentMin|AD_Org_ID", "");
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "GenerateReportShipmentMin|currency", strUserCurrencyId);
      String strparamDays = vars.getGlobalVariable("paramDays", "GenerateReportShipmentMin");
      String strparamBase = vars.getGlobalVariable("daysBaseCalculation", "GenerateReportShipmentMin");
      String strQuantity = vars.getStringParameter("Quantity");
      String strQuantity2 = vars.getStringParameter("Quantity2");
      if (strQuantity == null || "".equals(strQuantity))
        strQuantity = strparamDays;
      if (strQuantity2 == null || "".equals(strQuantity2))
          strQuantity2 = strparamBase;

      printPageDataSheet(request, response, vars, strWarehouse,strWarehouseSource, strAD_Org_ID, strAD_Org_Child_ID,strClient,
          strparamDays, strQuantity,strQuantity2,false);

    } else if (vars.commandIn("VAFASTER_WAS_HERE")) {
    	
    	
      String strClient = vars.getClient();
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateReportShipmentMin|M_Warehouse_ID");
      String strWarehouseSource = vars.getRequestGlobalVariable("inpmWarehouseSourceId",
              "GenerateReportShipmentMin|M_Warehouse_ID");
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "GenerateReportShipmentMin|AD_Org_ID");
      String strAD_Org_Child_ID = vars.getGlobalVariable("inpadOrgChildId",
              "GenerateReportShipmentMin|AD_Org_ID", "");
      String strparamDays = vars.getGlobalVariable("paramDays", "GenerateReportShipmentMin");
      String strparamBase = vars.getGlobalVariable("daysBaseCalculation", "GenerateReportShipmentMin");
      
      String strQuantity = vars.getStringParameter("Quantity");
      String strQuantity2 = vars.getStringParameter("Quantity2");
      if (strQuantity == null || "".equals(strQuantity))
        strQuantity = strparamDays;
      if (strQuantity2 == null || "".equals(strQuantity2))
          strQuantity2 = strparamBase;
      
      Client cliente = OBDal.getInstance().get(Client.class, strClient);
      Organization org = OBDal.getInstance().get(Organization.class, strAD_Org_Child_ID);
      Warehouse towarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
      Warehouse fromwarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouseSource);
  
      String[] selectedRequisitionLines = null;
      String strRequisitionLines = null;
      int variable = 0;
      String ReturnLine = "";
      int temporal = 0;
      try {
        strRequisitionLines = vars.getRequiredInStringParameter("inpRequisitionLine",
            IsIDFilter.instance);
        strRequisitionLines = strRequisitionLines.replace("(", "");
        strRequisitionLines = strRequisitionLines.replace(")", "");
        strRequisitionLines = strRequisitionLines.replace("'", "");
        selectedRequisitionLines = strRequisitionLines.split(",");
      } catch (Exception e) {
        temporal = 1;
        myMessage.setTitle("");
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "SWA_noselected", vars.getLanguage()));
        vars.setMessage("GenerateReportShipmentMin", myMessage);

      }

      if (temporal == 1) {
        printPageDataSheet(request, response, vars, strWarehouse, strWarehouseSource, strAD_Org_ID,strAD_Org_Child_ID, strClient,
            strparamDays, strQuantity,strQuantity2,false);
      } else {
    	  
    	// validate that org import has access to warehouse
          OBCriteria<OrgWarehouse> orgWarehouse = OBDal.getInstance()
              .createCriteria(OrgWarehouse.class);
          orgWarehouse.add(Restrictions.eq(OrgWarehouse.PROPERTY_CLIENT,
              OBDal.getInstance().get(Client.class, cliente.getId())));
          orgWarehouse.add(Restrictions.eq(OrgWarehouse.PROPERTY_WAREHOUSE,
              OBDal.getInstance().get(Warehouse.class, towarehouse.getId())));
          orgWarehouse.add(Restrictions.eq(OrgWarehouse.PROPERTY_ORGANIZATION,
              OBDal.getInstance().get(Organization.class, org.getId())));
          orgWarehouse.add(Restrictions.eq(OrgWarehouse.PROPERTY_ACTIVE, true));
          List<OrgWarehouse> orgWarehouse_list = orgWarehouse.list();
          if (orgWarehouse_list.size() <= 0) {
            myMessage.setTitle("");
            myMessage.setType("Error");
            myMessage.setTitle(Utility.messageBD(this, "WrongWarehouse", vars.getLanguage()));
            vars.setMessage("GenerateReportShipmentMin", myMessage);
          }
          else{
        	try {
        		reposition = createReposition(vars, cliente, org, towarehouse, fromwarehouse, strAD_Org_ID);
		        if (reposition == null) {
		          myMessage.setTitle("");
		          myMessage.setType("Error");
		          myMessage.setTitle(Utility.messageBD(this, "SWA_DoctypeMissing", vars.getLanguage()));
		          vars.setMessage("GenerateReportShipmentMin", myMessage);
		        } else {
		          //variable = createRepositionLines(vars, cliente, org, selectedRequisitionLines, reposition);
		          ReturnLine = createRepositionLines(vars, cliente, org, selectedRequisitionLines, reposition);
		          if (ReturnLine.equals("sucess")) {
		            myMessage.setTitle("");
		            myMessage.setType("Success");
		            myMessage.setTitle(Utility.messageBD(this, "SWA_generateRepo", vars.getLanguage()));
		            myMessage.setMessage("Doc. No: " + reposition.getDocumentNo());
		            vars.setMessage("GenerateReportShipmentMin", myMessage);
		          } else if(ReturnLine.equals("error")) {
		            OBCriteria<SwaRequerimientoReposicionDetail> SWADetail = OBDal.getInstance()
		                .createCriteria(SwaRequerimientoReposicionDetail.class);
		            SWADetail.add(Restrictions.eq(
		                SwaRequerimientoReposicionDetail.PROPERTY_SWAREQUERIMIENTOREPOSICION, reposition));
		            List<SwaRequerimientoReposicionDetail> ListIems = SWADetail.list();
		            for (int i = 0; i < ListIems.size(); i++) {
		              OBDal.getInstance().remove(ListIems.get(i));
		            }
		            OBDal.getInstance().remove(reposition);
		            myMessage.setTitle("");
		            myMessage.setType("Error");
		            myMessage.setTitle(Utility.messageBD(this, "SWA_invalidQty", vars.getLanguage()));
		            vars.setMessage("GenerateReportShipmentMin", myMessage);
		          }
		          else {
		        	    OBDal.getInstance().remove(reposition);
			            myMessage.setTitle("");
			            myMessage.setType("Error");
			            myMessage.setTitle(Utility.messageBD(this,ReturnLine, vars.getLanguage()));
			            vars.setMessage("GenerateReportShipmentMin", myMessage);
		          }
		        }
			} catch (Exception e) {
				OBDal.getInstance().remove(reposition);
	            myMessage.setTitle("");
	            myMessage.setType("Error");
	            myMessage.setTitle(Utility.messageBD(this, e.getMessage(), vars.getLanguage()));
	            vars.setMessage("GenerateReportShipmentMin", myMessage);
			}  
         }
        printPageDataSheet(request, response, vars, strWarehouse,strWarehouseSource, strAD_Org_ID,strAD_Org_Child_ID, strClient,
            strparamDays, strQuantity,strQuantity2,false);
      }

    } else if (vars.commandIn("GENERATE")) {
    	
    	System.out.println("SEGUNDO ");
    	
      String strClient = vars.getClient();
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
          "GenerateReportShipmentMin|M_Warehouse_ID");
      String strWarehouseSource = vars.getRequestGlobalVariable("inpmWarehouseSourceId",
              "GenerateReportShipmentMin|M_Warehouse_ID");
      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
          "GenerateReportShipmentMin|AD_Org_ID");
      String strAD_Org_Child_ID = vars.getGlobalVariable("inpadOrgChildId",
              "GenerateReportShipmentMin|AD_Org_ID", "");
      String strparamDays = vars.getGlobalVariable("paramDays", "GenerateReportShipmentMin");
      String strparamBase = vars.getGlobalVariable("daysBaseCalculation", "GenerateReportShipmentMin");
      
      String strQuantity = vars.getStringParameter("Quantity");
      if (strQuantity == null || "".equals(strQuantity))
        strQuantity = strparamDays;
      
      String strQuantity2 = vars.getStringParameter("Quantity2");
      if (strQuantity2 == null || "".equals(strQuantity2))
        strQuantity2 = strparamBase;


      myMessage.setTitle("");
      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      vars.setMessage("GenerateReportShipmentMin", myMessage);
      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
          "GenerateReportShipmentMin|currency", strUserCurrencyId);
      printPageDataSheet(request, response, vars, strWarehouse, strWarehouseSource, strAD_Org_ID, strAD_Org_Child_ID,strClient,
          strCurrencyId, strQuantity,strQuantity2,false);
      
      
    }else if (vars.commandIn("PDF", "XLS")) {
    	 if (log4j.isDebugEnabled())
    	        log4j.debug("PDF");
    	 
    	  String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId",
    	          "GenerateReportShipmentMin|M_Warehouse_ID");
    	      String strWarehouseSource = vars.getRequestGlobalVariable("inpmWarehouseSourceId",
    	              "GenerateReportShipmentMin|M_Warehouse_ID");
    	      String strClient = vars.getClient();
    	      String strAD_Org_ID = vars.getRequestGlobalVariable("inpadOrgId",
    	          "GenerateReportShipmentMin|AD_Org_ID");
    	      String strAD_Org_Child_ID = vars.getGlobalVariable("inpadOrgChildId",
    	              "GenerateReportShipmentMin|AD_Org_ID", "");
    	      String strCurrencyId = vars.getGlobalVariable("inpCurrencyId",
    	          "GenerateReportShipmentMin|currency", strUserCurrencyId);
    	      String strparamDays = vars.getGlobalVariable("paramDays", "GenerateReportShipmentMin");
    	      String strparamBase = vars.getGlobalVariable("daysBaseCalculation", "GenerateReportShipmentMin");
    	      String strQuantity = vars.getStringParameter("Quantity");
    	      String strQuantity2 = vars.getStringParameter("Quantity2");
    	      if (strQuantity == null || "".equals(strQuantity))
    	        strQuantity = strparamDays;
    	      if (strQuantity2 == null || "".equals(strQuantity2))
    	          strQuantity2 = strparamBase;

    	        printPage(request, response, vars, strWarehouse, strWarehouseSource, strClient, strAD_Org_ID,
    	        		strAD_Org_Child_ID,strCurrencyId,strparamDays,strparamBase,strQuantity,strQuantity2);

      } else
      pageError(response);
  }
  
  private void printPage(HttpServletRequest request, HttpServletResponse response,
	      VariablesSecureApp vars, String strWarehouse, String strWarehouseSource, String strClient,
	      String strAD_Org_ID, String strAD_Org_Child_ID,String strCurrencyId,String strparamDays,String strparamBase,
	      String strQuantity,String strQuantity2) throws IOException,
	      ServletException {

	  GenerateReportShipmentData[] data2 = null;
	    try {

	      	  Warehouse fromWarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
	      	  Warehouse toWarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouseSource);
	      	  
	      	  data2 =   GenerateReportShipmentData.select(this,fromWarehouse.getName() ,toWarehouse.getName() ,strQuantity,strQuantity2,
	      			  strAD_Org_ID, strClient, strWarehouse, strWarehouseSource);  

	    } catch (OBException e) {
	      advisePopUp(request, response, "WARNING",
	          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
	          Utility.messageBD(this, "FINPR_NoConversionFound", vars.getLanguage()) + e.getMessage());
	    }
	    
	    Calendar fecha = Calendar.getInstance();
	    
	    int mesActual=fecha.get(Calendar.MONTH);
	    int anioActual=fecha.get(Calendar.YEAR);
	    
	    String strOutput = "xls";
	    String strReportName;
	 
	    if(vars.commandIn("XLS")){
	    	 strOutput = "xls";
		    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateReportShipmentMin.jrxml";
	    }else {
	   	 strOutput = "pdf";
		    strReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/GenerateReportShipmentMinPDF.jrxml";
	    }


	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    
		parameters.put("MES_ANT1", getMesAnterior(mesActual-1)+"-"+anioActual );
		parameters.put("MES_ANT2", getMesAnterior(mesActual-2)+"-"+anioActual );
		parameters.put("MES_ANT3", getMesAnterior(mesActual-3)+"-"+anioActual  );
		parameters.put("MES_ANT4", getMesAnterior(mesActual-4)+"-"+anioActual );
		parameters.put("MES_ANT5", getMesAnterior(mesActual-5)+"-"+anioActual  );
		parameters.put("MES_ANT6", getMesAnterior(mesActual-6)+"-"+anioActual );

		parameters.put("RUC_ORG", DetalleProductosVendidosJRData
				.selectRUC(this, strAD_Org_ID));
		parameters.put("ORGANIZACION",
				DetalleProductosVendidosJRData
						.selectOrg(this, strAD_Org_ID));

	    renderJR(vars, response, strReportName, "Promedio_Consumo_por_Transferencia", strOutput,
	        parameters, data2, null);
  }
  
  private String getMesAnterior(int k)
  {
	  String [] meses = { "Ene", "Feb", "Mar", "Abr", "May", "Jun",
              "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
	  
	  k=k%meses.length;
	  
	  if ( k < 0)
		  k=meses.length+k;
	  
	  return meses [k];
	  
  }

  private SwaRequerimientoReposicion createReposition(VariablesSecureApp vars, Client cliente,
      Organization org,Warehouse toWarehouse, Warehouse fromWarehouse, String ad_org_id) {
    // BusinessPartner BPartner = OBDal.getInstance().get(BusinessPartner.class, org);

    OBCriteria<BusinessPartner> BPartner = OBDal.getInstance()
        .createCriteria(BusinessPartner.class);
    BPartner.add(Restrictions.eq(DocumentType.PROPERTY_ID, org.getId()));
    BusinessPartner BPartnert = (BusinessPartner) BPartner.uniqueResult();
   
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
    		fromWarehouse.getClient().getId()); 
    
    OBCriteria<DocumentType> P_doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
    P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SWAREQUESTREPOSITION"));
    P_doctype_c.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
    List<DocumentType> DocTypeList = P_doctype_c.list();
    if(DocTypeList.size()==0)
    	return null;
    
 /*   DocumentType P_doc_type_c = null;
    try {
      P_doc_type_c = (DocumentType) P_doctype_c.uniqueResult();
      if (P_doc_type_c == null) {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
*/
    SwaRequerimientoReposicion repo = OBProvider.getInstance()
        .get(SwaRequerimientoReposicion.class);
    repo.setClient(cliente);
    repo.setOrganization(org);
    repo.setActive(true);
    repo.setUpdated(new Date());
    repo.setRequerName((new Date()).toString());

    // VAFASTER
/*    OBCriteria<Warehouse> P_warehouse = OBDal.getInstance().createCriteria(Warehouse.class);
    P_warehouse.add(Restrictions.eq(Warehouse.PROPERTY_ORGANIZATION, org));
    P_warehouse.add(Restrictions.eq(Warehouse.PROPERTY_SWAWAREHOUSETYPE, "NO"));
    List<Warehouse> warehouses = P_warehouse.list();
    if (warehouses.size() > 0)
      toWarehouse = warehouses.get(0);
    else
      toWarehouse = null;
*/

    repo.setFromMWarehouse(fromWarehouse);
    repo.setMWarehouse(toWarehouse);
    repo.setAlertStatus("DR");
    repo.setPickinglistState("NG");
    repo.setRequestStatus("DR");
    repo.setWindowsnameList("ST");
    repo.setRepositiontrx(false);
    repo.setGeneratetrx(false);
    repo.setProcesstrx(false);
    //repo.setBusinessPartner(BPartnert);
    repo.setDocumentType(DocTypeList.get(0));
    repo.setRequestMotive("Por Reposición de Consumo");
    
    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SWAINTERNALSHIPMENT"));
    shipDocType.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
    shipDocType.setMaxResults(1);
    DocumentType doctypeCreate = (DocumentType) shipDocType.uniqueResult();
    if ("".equals(doctypeCreate) || doctypeCreate == null) {
        return null;
      }
    repo.setDoctypeCreate(doctypeCreate);
    
    OBDal.getInstance().save(repo);
    return repo;
  }

  private String createRepositionLines(VariablesSecureApp vars, Client cliente, Organization org,
      String[] selectedRequisitionLines, SwaRequerimientoReposicion repo) throws IOException,
      ServletException {
    long lineNo;
    lineNo = 10L;
    
    
    try {
    	 for (int i = 0; selectedRequisitionLines != null && i < selectedRequisitionLines.length; i++) {
    	      Product producto = OBDal.getInstance().get(Product.class, selectedRequisitionLines[i].trim());
    	      String orderQty = "";
    	      try {
    	        orderQty = vars.getNumericParameter("inpOrderQty" + selectedRequisitionLines[i].trim());
    	        if (orderQty.isEmpty() || orderQty == null) {
    	          return "error"; // No Element Selected
    	        }
    	        if (Integer.parseInt(orderQty) < 1) {
    	          throw new Exception();
    	        }
    	      } catch (Exception e) {
    	        return "error"; // Invalid number
    	        // TODO: handle exception
    	      }
    	      
    	    SwaRequerimientoReposicionDetail repoLines = OBProvider.getInstance().get(
    	    SwaRequerimientoReposicionDetail.class);
    	    repoLines.setClient(cliente);
    	    repoLines.setOrganization(org);
    	    repoLines.setLineNo(lineNo);
    	    lineNo += 10L;
    	    repoLines.setActive(true);
    	    repoLines.setProduct(producto);
    	    repoLines.setUOM(producto.getUOM());
    	    repoLines.setUpdated(new Date());
    	    repoLines.setOrderedQuantity(new BigDecimal(orderQty));
    	    repoLines.setQtyrequired(new BigDecimal(orderQty));
    	    repoLines.setSWARequerimientoreposicion(repo);
    	    repoLines.setAlertStatus("PD");
    	    OBDal.getInstance().save(repoLines);
    	}
    	 OBDal.getInstance().flush();
    	
	} catch (Exception e) {
        return e.getMessage();   
	}
    
    
   
    
    
    
    
    
    return "sucess"; // Successfully
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strWarehouse,String strWarehouseSource, String strAD_Org_ID, String strAD_Org_Child_ID,String strClient,
      String strCurrencyId, String strNumFuture, String strNumBaseDay,boolean isFirstLoad) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GenerateReportShipmentMinData[] data = null;
    GenerateReportShipmentData[] data2 = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    // If the instance is not migrated the user should use the Legacy report
    if (CostingStatus.getInstance().isMigrated() == false) {
      advise(request, response, "ERROR", OBMessageUtils.messageBD("NotUsingNewCost"), "");
      return;
    }

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_reports/GenerateReportShipmentMin", discard)
        .createXmlDocument();
    if (vars.commandIn("FIND")) {
      // Checks if there is a conversion rate for each of the transactions
      // of the report
      OBError myMessage = null;
      myMessage = new OBError();
      try {
    	   
    	  Warehouse fromWarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
    	  Warehouse toWarehouse = OBDal.getInstance().get(Warehouse.class, strWarehouseSource);
    	  
    	  data2 =   GenerateReportShipmentData.select(this,fromWarehouse.getName() ,toWarehouse.getName() ,strNumFuture,strNumBaseDay, strAD_Org_ID, strClient, strWarehouse, strWarehouseSource);  
         // data = GenerateReportShipmentMinData.select1(this, strWarehouse,strWarehouseSource, strClient,
         //   vars.getLanguage(), strNumFuture,strNumBaseDay,strAD_Org_ID);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      // If a conversion rate is missing for a certain transaction, an
      // error message window pops-up.
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if (data2 == null || data2.length == 0) {
          discard[0] = "selEliminar";
          data = GenerateReportShipmentMinData.set();
          data2 = GenerateReportShipmentData.set();
        } else {
          // Apply differences in percentages applying difference to bigger percentage
          BigDecimal total = BigDecimal.ZERO;
          BigDecimal difference = BigDecimal.ZERO;
          int toAdjustPosition = 0;
          String currentOrganization = strAD_Org_Child_ID;
          xmlDocument.setData("structure1", data2);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        //data = GenerateReportShipmentMinData.set();
        data2 = GenerateReportShipmentData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      // Load Toolbar
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateReportShipmentMin", false,
          "", "", "imprimir();", false, "ad_reports", strReplaceWith, false, true);
      toolbar.setEmail(false);
      toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());
      
      ///
//      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "DetalleProductosVendidosFilterJR",
//              false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
//          toolbar.prepareSimpleToolBarTemplate();
//          xmlDocument.setParameter("toolbar", toolbar.toString());


      // Create WindowTabs
      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.warehouse.ad_reports.GenerateReportShipmentMin");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "GenerateReportShipmentMin.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "GenerateReportShipmentMin.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      // Load Message Area
      {
        OBError myMessage = vars.getMessage("GenerateReportShipmentMin");
        vars.removeMessage("GenerateReportShipmentMin");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      String strDocDate = SREDateTimeData.today(this);
      xmlDocument.setParameter("docDate",strDocDate);
      // Pass parameters to the window
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

      // Load Business Partner Group combo with data
      /*try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "GenerateReportShipmentMin"), Utility.getContext(this, vars, "#User_Client",
                "GenerateReportShipmentMin"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportShipmentMin",
            strWarehouse);
        xmlDocument.setData("reportM_Warehouse_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }*/
      
      /*try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
              "M_Warehouse_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                  "GenerateReportShipmentMin"), Utility.getContext(this, vars, "#User_Client",
                  "GenerateReportShipmentMin"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportShipmentMin",
        		  strWarehouseSource);
          xmlDocument.setData("reportM_Warehouse_Source_ID", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
       }*/
      
      try {
          ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
              "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                  "#AccessibleOrgTree", "GenerateReportShipmentMin"), Utility.getContext(this, vars,
                  "#User_Client", "GenerateReportShipmentMin"), 0);
          Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportShipmentMin",
              strAD_Org_ID);
          xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
          comboTableData = null;
        } catch (Exception ex) {
          throw new ServletException(ex);
        }
      
      
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
            "ABE594ACE1764B7799DEF0BA6E8A389B", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "GenerateReportShipmentMin"), Utility.getContext(this, vars,
                "#User_Client", "GenerateReportShipmentMin"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportShipmentMin",
            strAD_Org_Child_ID);
        xmlDocument.setData("reportAD_Org_Child_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter("ccurrencyid", strNumFuture);
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Currency_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
                "GenerateReportShipmentMin"), Utility.getContext(this, vars, "#User_Client",
                "GenerateReportShipmentMin"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "GenerateReportShipmentMin",
        		strNumFuture);
        xmlDocument.setData("reportC_Currency_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter(
          "warehouseArray",
          Utility.arrayDobleEntrada(
              "arrWarehouse",
              GenerateReportShipmentMinData.selectWarehouseDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "GenerateReportShipmentMin"))));
      
      
      xmlDocument.setParameter(
              "orgChildArray",
              Utility.arrayDobleEntrada(
                  "arrOrganizationChild",
                  GenerateReportShipmentMinData.selectOrganizationChildDouble(this,
                      Utility.getContext(this, vars, "#User_Client", "GenerateReportShipmentMin"))));

      if (isFirstLoad) {
          xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
      } else {
          xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
      }
      
      xmlDocument.setParameter("mWarehouseId", strWarehouse);
      xmlDocument.setParameter("mWarehouseDescription",
    		  GenerateReportShipmentMinData.selectMWarehouse(this, strWarehouse));
      
      xmlDocument.setParameter("mWarehouseSourceId", strWarehouseSource);
      xmlDocument.setParameter("mWarehouseSourceDescription",
    		  GenerateReportShipmentMinData.selectMWarehouse(this, strWarehouseSource));
      
      
      xmlDocument.setParameter("adOrg", strAD_Org_ID);
      xmlDocument.setParameter("adOrgChild", strAD_Org_Child_ID);
      xmlDocument.setParameter("Quantity", strNumFuture);
      xmlDocument.setParameter("Quantity2", strNumBaseDay );

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private OBError mUpdateParetoProduct(VariablesSecureApp vars, String strWarehouse,
      String strAD_Org_ID, String strAD_Client_ID) throws IOException, ServletException {
    String pinstance = SequenceIdData.getUUID();

    PInstanceProcessData.insertPInstance(this, pinstance, "1000500001", "0", "N", vars.getUser(),
        vars.getClient(), vars.getOrg());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "1", "m_warehouse_id", strWarehouse,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "2", "ad_org_id", strAD_Org_ID,
        vars.getClient(), vars.getOrg(), vars.getUser());
    PInstanceProcessData.insertPInstanceParam(this, pinstance, "3", "ad_client_id",
        strAD_Client_ID, vars.getClient(), vars.getOrg(), vars.getUser());
    GenerateReportShipmentMinData.mUpdateParetoProduct0(this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    OBError myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    return myMessage;
  }

  public String getServletInfo() {
    return "Servlet GenerateReportShipmentMin info. Insert here any relevant information";
  } // end of getServletInfo() method
}
