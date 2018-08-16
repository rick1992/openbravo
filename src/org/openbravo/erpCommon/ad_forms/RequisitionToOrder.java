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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.businessUtility.WindowTabsData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.Incoterms;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.model.procurement.RequisitionPOMatch;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicion;

public class RequisitionToOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static int validate = 0;
  private static String corderid = "";
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strProductId = vars.getGlobalVariable("inpmProductId",
          "RequisitionToOrder|M_Product_ID", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "RequisitionToOrder|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "RequisitionToOrder|DateTo", "");
      String strRequesterId = vars.getGlobalVariable("inpRequesterId",
          "RequisitionToOrder|Requester_ID", "");
      String strVendorId = vars.getGlobalVariable("inpcBpartnerId",
          "RequisitionToOrder|C_BPartner_ID", "");
      String strIncludeVendor = vars.getGlobalVariable("inpShowNullVendor",
          "RequisitionToOrder|ShowNullVendor", "Y");

      String strShowService = vars.getGlobalVariable("inpShowAll", "RequisitionToOrder|ShowAll",
          "N");

      String strShowExcluded = vars.getGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded", "N");

      String strOrgId = vars.getGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID",
          vars.getOrg());
      String strProductGroupId = vars.getGlobalVariable("inpadProductTypeId",
          "RequisitionToOrder|prdc_productgroup_id", "");
      String strProductCategoryId = vars.getRequestGlobalVariable("inpProductCategory",
          "RequisitionToOrder|m_requisition_id");
      strProductGroupId = strProductCategoryId;
      String strRequisitionId = vars.getGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id", "");
      String strProductTypeId = vars.getStringParameter("inpProductTypeId");
      String strProjectId = vars.getGlobalVariable("inpProjectId", "RequisitionToOrder|project_id",
          "");

      vars.setSessionValue("RequisitionToOrder|isSOTrx", "N");

      printPage(response, vars, strProductId, strDateFrom, strDateTo, strRequesterId, strVendorId,
          strIncludeVendor, strOrgId, strProductGroupId, strRequisitionId, strProductTypeId,
          strShowService, strShowExcluded, strProjectId);

    } else if (vars.commandIn("FIND")) {
      String strProductId = vars.getRequestGlobalVariable("inpmProductId",
          "RequisitionToOrder|M_Product_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "RequisitionToOrder|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "RequisitionToOrder|DateTo");
      String strRequesterId = vars.getRequestGlobalVariable("inpRequesterId",
          "RequisitionToOrder|Requester_ID");
      String strVendorId = vars.getRequestGlobalVariable("inpcBpartnerId",
          "RequisitionToOrder|C_BPartner_ID");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");

      String strIncludeVendor = vars.getRequestGlobalVariable("inpShowNullVendor",
          "RequisitionToOrder|ShowNullVendor");
      String strShowService = vars.getRequestGlobalVariable("inpShowAll",
          "RequisitionToOrder|ShowAll");

      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded");

      vars.setSessionValue("strShowAll", strShowService);

      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID");
      String strProductGroupId = vars.getRequestGlobalVariable("paramProductCategory_ID",
          "RequisitionToOrder|prdc_productgroup_id");
      String strRequisitionId = vars.getRequestGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id");
      String strProjectId = vars.getRequestGlobalVariable("inpProjectId",
          "RequisitionToOrder|project_id");
      String strProductTypeId = vars.getStringParameter("inpProductTypeId");

      updateLockedLines(vars, strOrgId, strRequisitionId, strShowService, strShowExcluded,
          strProjectId);
      printPageDataSheet(response, vars, strProductId, strDateFrom, strDateTo, strRequesterId,
          strVendorId, strIncludeVendor, strOrgId, strProductGroupId, strRequisitionId,
          strProductTypeId, strShowService, strShowExcluded, strcBpartnetId, strProjectId);

    } else if (vars.commandIn("ADD")) {
      String strProductId = vars.getRequestGlobalVariable("inpmProductId",
          "RequisitionToOrder|M_Product_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "RequisitionToOrder|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "RequisitionToOrder|DateTo");
      String strRequesterId = vars.getRequestGlobalVariable("inpRequesterId",
          "RequisitionToOrder|Requester_ID");
      String strVendorId = vars.getRequestGlobalVariable("inpcBpartnerId",
          "RequisitionToOrder|C_BPartner_ID");
      String strIncludeVendor = vars.getRequestGlobalVariable("inpShowNullVendor",
          "RequisitionToOrder|ShowNullVendor");
      String strShowService = vars.getRequestGlobalVariable("inpShowAll",
          "RequisitionToOrder|ShowAll");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "RequisitionToOrder|CB_PARTNER_ID", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID");
      String strProductGroupId = vars.getRequestGlobalVariable("paramProductCategory_ID",
          "RequisitionToOrder|prdc_productgroup_id");
      String strRequisitionId = vars.getRequestGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id");
      String strProjectId = vars.getStringParameter("inpProjectId");
      String strProductTypeId = vars.getStringParameter("inpProductTypeId");
      String strRequisitionLines = vars.getRequiredInStringParameter("inpRequisitionLine",
          IsIDFilter.instance);
      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded");

      updateLockedLines(vars, strOrgId, strRequisitionId, strShowService, strShowExcluded,
          strProjectId);
      lockRequisitionLines(vars, strRequisitionLines);
      printPageDataSheet(response, vars, strProductId, strDateFrom, strDateTo, strRequesterId,
          strVendorId, strIncludeVendor, strOrgId, strProductGroupId, strRequisitionId,
          strProductTypeId, strShowService, strShowExcluded, strcBpartnetId, strProjectId);

    } else if (vars.commandIn("REMOVE")) {
      String strProductId = vars.getRequestGlobalVariable("inpmProductId",
          "RequisitionToOrder|M_Product_ID");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "RequisitionToOrder|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "RequisitionToOrder|DateTo");
      String strRequesterId = vars.getRequestGlobalVariable("inpRequesterId",
          "RequisitionToOrder|Requester_ID");
      String strVendorId = vars.getRequestGlobalVariable("inpcBpartnerId",
          "RequisitionToOrder|C_BPartner_ID");
      String strIncludeVendor = vars.getRequestGlobalVariable("inpShowNullVendor",
          "RequisitionToOrder|ShowNullVendor");
      String strShowService = vars.getRequestGlobalVariable("inpShowAll",
          "RequisitionToOrder|ShowAll");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "RequisitionToOrder|CB_PARTNER_ID", "");
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID");
      String strProductGroupId = vars.getRequestGlobalVariable("paramProductCategory_ID",
          "RequisitionToOrder|prdc_productgroup_id");
      String strRequisitionId = vars.getRequestGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id");
      String strProjectId = vars.getStringParameter("inpProjectId", "");
      String strProductTypeId = vars.getStringParameter("inpProductTypeId");
      String strSelectedLines = vars.getRequiredInStringParameter("inpSelectedReq",
          IsIDFilter.instance);
      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded");

      unlockRequisitionLines(vars, strSelectedLines);
      updateLockedLines(vars, strOrgId, strRequisitionId, strShowService, strShowExcluded,
          strProjectId);
      printPageDataSheet(response, vars, strProductId, strDateFrom, strDateTo, strRequesterId,
          strVendorId, strIncludeVendor, strOrgId, strProductGroupId, strRequisitionId,
          strProductTypeId, strShowService, strShowExcluded, strcBpartnetId, strProjectId);

    } else if (vars.commandIn("OPEN_CREATE")) {
      validate = 0;

      String strShowService = vars.getRequestGlobalVariable("inpShowAll",
          "RequisitionToOrder|ShowAll");
      String strSelectedLines = vars.getRequiredInStringParameter("inpSelectedReq",
          IsIDFilter.instance);
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID");
      String strRequisitionId = vars.getRequestGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id");
      String strProjectId = vars.getRequestGlobalVariable("inpProjectId",
          "RequisitionToOrder|project_id");

      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded");

      updateLockedLines(vars, strOrgId, strRequisitionId, strShowService, strShowExcluded,
          strProjectId);
      checkSelectedRequisitionLines(response, vars, strSelectedLines, strShowService, strProjectId);

    } else if (vars.commandIn("OPEN_CREATE_REPOSITION")) {
      validate = 1;
      String strShowService = vars.getRequestGlobalVariable("inpShowAll",
          "RequisitionToOrder|ShowAll");

      String strSelectedLines = vars.getRequiredInStringParameter("inpSelectedReq",
          IsIDFilter.instance);
      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "RequisitionToOrder|AD_Org_ID");
      String strRequisitionId = vars.getRequestGlobalVariable("inpRequisitionId",
          "RequisitionToOrder|m_requisition_id");
      String strProjectId = vars.getRequestGlobalVariable("inpProjectId",
          "RequisitionToOrder|project_id");

      String strShowExcluded = vars.getRequestGlobalVariable("inpShowExcluded",
          "RequisitionToOrder|ShowExcluded");

      updateLockedLines(vars, strOrgId, strRequisitionId, strShowService, strShowExcluded,
          strProjectId);
      checkSelectedRequisitionLines(response, vars, strSelectedLines, strShowService, strProjectId);

    } else if (vars.commandIn("GENERATE")) {

      /*
       * String strSelectedLines = vars.getRequiredGlobalVariable("inpSelected",
       * "RequisitionToOrderCreate|SelectedLines"); String strOrderDate =
       * vars.getRequiredGlobalVariable("inpOrderDate", "RequisitionToOrderCreate|OrderDate");
       * String strVendor = vars.getRequiredGlobalVariable("inpOrderVendorId",
       * "RequisitionToOrderCreate|OrderVendor"); String strPriceListId =
       * vars.getRequiredGlobalVariable("inpPriceListId", "RequisitionToOrderCreate|PriceListId");
       * String strOrg = vars.getRequiredGlobalVariable("inpOrderOrg",
       * "RequisitionToOrderCreate|Org"); String strWarehouse =
       * vars.getRequiredGlobalVariable("inpWarehouse", "RequisitionToOrderCreate|Warehouse");
       * String strProjectId = vars.getRequestGlobalVariable("inpProjectId",
       * "RequisitionToOrder|project_id");
       */

      String strSelectedLines = vars.getStringParameter("inpSelected");
      String strOrderDate = vars.getStringParameter("inpOrderDate");
      String strVendor = vars.getStringParameter("inpOrderVendorId");
      String strPriceListId = vars.getStringParameter("inpPriceListId");
      String strOrg = vars.getStringParameter("inpOrderOrg");
      String strWarehouse = vars.getStringParameter("inpWarehouse");

      System.out.println("strOrderDate " + strOrderDate);
      System.out.println("strVendor " + strVendor);
      System.out.println("strPriceListId " + strPriceListId);
      System.out.println("strOrg " + strOrg);
      System.out.println("strWarehouse " + strWarehouse);

      OBError myMessage = null;

      String purchaseOrderTabId = "294"; // Purchase Invoice Header tab

      if (validate == 0) {
        corderid = "";
        myMessage = processPurchaseOrder(vars, strSelectedLines, strOrderDate, strVendor,
            strPriceListId, strOrg, strWarehouse);
      } else if (validate == 1) {
        myMessage = processOrderReposition(vars, strSelectedLines, strOrderDate, strVendor,
            strPriceListId, strOrg, strWarehouse);
      }
      vars.setMessage("RequisitionToOrderCreate", myMessage);

      if (myMessage != null && myMessage.getType().equals("Error")) {
        printPageCreate(response, vars, "", "", "", "", "", "");
      } else {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(getJSON(purchaseOrderTabId, (!corderid.equals("")) ? corderid : null,
            RequisitionToOrder.strBDErrorMessage));
        out.close();

      }
    } else
      pageError(response);
  }

  private String getJSON(String tabId, String recordId, String message) throws ServletException {
    JSONObject json = null;

    try {
      OBContext.setAdminMode();

      json = new JSONObject();
      json.put("tabId", tabId);
      if (recordId != null) {
        json.put("recordId", recordId);
        json.put("msg", "");
      } else {
        json.put("recordId", "");
        json.put("msg", message);
      }

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

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strProductId,
      String strDateFrom, String strDateTo, String strRequesterId, String strVendorId,
      String strIncludeVendor, String strOrgId, String strProductGroupId, String strRequisitionId,
      String strProductTypeId, String strShowAll, String strShowExcluded, String strProjectId)
      throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    String strProductGroupId_paraLike = "%" + strProductGroupId.toLowerCase() + "%";

    String strTreeOrg = RequisitionToOrderData.treeOrg(this, vars.getClient());

    RequisitionToOrderData[] datalines = null;

    RequisitionToOrderData[] dataselected = null;

    /*
     * RequisitionToOrderData[] dataselected = RequisitionToOrderData.selectSelected(this,
     * vars.getLanguage(), vars.getUser(), Utility.getContext(this, vars, "#User_Client",
     * "RequisitionToOrder"), Tree.getMembers(this, strTreeOrg, strOrgId));
     */

    String discard[] = { "" };
    if (dataselected == null || dataselected.length == 0) {
      dataselected = RequisitionToOrderData.set();
      discard[0] = "funcSelectedEvenOddRow";
    }
    xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_forms/RequisitionToOrder", discard)
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RequisitionToOrder", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // VAFASTER GET NEW QUERY FOR PRODUCT QTY IN TRANSIT

    RequisitionToOrderData[] dataQTYProductInTransit = null;
    RequisitionToOrderData[] dataQTYProductInTransitResumen = null;
    if (!strProductId.equals("")) {
      Product prd_obj = OBDal.getInstance().get(Product.class, strProductId);

      // dataQTYProductInTransit = RequisitionToOrderData.ProductInTransitGet(this,strProductId,
      // prd_obj.getOrganization().getId(),
      // vars.getSessionValue("#AD_CLIENT_ID"));
      // dataQTYProductInTransitResumen =
      // RequisitionToOrderData.ProductInTransitGetResumen(this,strProductId,
      // prd_obj.getOrganization().getId(),
      // vars.getSessionValue("#AD_CLIENT_ID"));

      String ConcatenadoTransito = "";
      for (int i = 0; i < dataQTYProductInTransitResumen.length; i++) {
        ConcatenadoTransito = ConcatenadoTransito + "Cantidad: "
            + dataQTYProductInTransitResumen[i].qtytransitresumen + " Fecha Prometida: "
            + dataQTYProductInTransitResumen[i].qtydatetransit + " | ";
      }
      for (int i = 0; i < dataQTYProductInTransit.length; i++) {
        dataQTYProductInTransit[i].productresumenid = ConcatenadoTransito;
      }
    }
    if (dataQTYProductInTransit == null || dataQTYProductInTransit.length == 0) {
      discard[0] = "selEliminar";
      dataQTYProductInTransit = RequisitionToOrderData.set();
    }
    // xmlDocument.setData("structure3", dataQTYProductInTransit);

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.RequisitionToOrder");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "RequisitionToOrder.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "RequisitionToOrder.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("RequisitionToOrder");
      vars.removeMessage("RequisitionToOrder");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramProductId", strProductId);
    xmlDocument.setParameter("paramProductDescription", strProductId.equals("") ? ""
        : RequisitionToOrderData.mProductDescription(this, strProductId, vars.getLanguage()));
    xmlDocument.setParameter("displayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("paramRequester", strRequesterId);
    xmlDocument.setParameter("paramBPartnerId", strVendorId);

    xmlDocument.setParameter("paramBPartnerDescription",
        RequisitionToOrderData.selectBpartner(this, strVendorId));

    xmlDocument.setParameter("paramRequisition_ID", strRequisitionId);
    xmlDocument.setParameter("paramRequisitionDescription",
        RequisitionToOrderData.selectRequisition(this, strRequisitionId));

    xmlDocument.setParameter("paramShowNullVendor", strIncludeVendor);
    xmlDocument.setParameter("paramAdOrgId", strOrgId);
    // xmlDocument.setParameter("paramProductGroup_ID", strProductGroupId);
    xmlDocument.setParameter("paramProductCategory", strProductGroupId);
    xmlDocument.setParameter("paramProductType_ID", strProductTypeId);
    xmlDocument.setParameter("paramShowAll", strShowAll);
    xmlDocument.setParameter("paramShoExcluded", strShowExcluded);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
          "UsersWithRequisition",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strRequesterId);
      xmlDocument.setData("reportRequester_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "ABE594ACE1764B7799DEF0BA6E8A389B",
          Utility.getContext(this, vars, "#User_Org", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder", strOrgId);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "prdc_productgroup_id", "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strProductGroupId);
      xmlDocument.setData("reportProductGroup_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    /*
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "m_requisition_id", "", "sre_validation_requisitiontoCombo", Utility.getContext(this, vars,
     * "#User_Org", "RequisitionToOrder"), Utility.getContext(this, vars, "#User_Client",
     * "RequisitionToOrder"), 0); Utility.fillSQLParameters(this, vars, null, comboTableData,
     * "RequisitionToOrder", strRequisitionId); xmlDocument.setData("reportRequisition_ID",
     * "liststructure", comboTableData.select(false)); comboTableData = null; } catch (Exception ex)
     * { throw new ServletException(ex); }
     */

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "m_product_category_id", "", "",
          Utility.getContext(this, vars, "#User_Org", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strProductTypeId);
      xmlDocument.setData("reportProductType_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structureSearch", datalines);
    xmlDocument.setData("structureSelected", dataselected);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strProductId, String strDateFrom, String strDateTo, String strRequesterId,
      String strVendorId, String strIncludeVendor, String strOrgId, String strProductGroupId,
      String strRequisitionId, String strProductTypeId, String strShowService,
      String strShowExcluded, String strcBpartnetId, String strProjectId)
      throws IOException, ServletException {
    System.out.println("strProjectId:" + strProjectId);
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    String strProductGroupId_paraLike = "%" + strProductGroupId.toLowerCase() + "%";

    String strTreeOrg = RequisitionToOrderData.treeOrg(this, vars.getClient());

    if (strShowService == null || strShowService.equals(""))
      strShowService = "N";
    // strOrgTmpId Este parametro se envia en lugar de strOrgId
    // strOrgTmpId Sera null cuando el check de mostrar todo este activado (mostrar todo: mostrar
    // todos las necesidades de cualquier organizacion)
    String strProductType = "I";
    if (strShowService.equals("Y"))
      strProductType = "S";

    String strShwExludedProject = "N";
    if (strShowExcluded.equals("Y"))
      strShwExludedProject = "Y";

    // Original OPENBRAVO
    /*
     * RequisitionToOrderData[] datalines = RequisitionToOrderData.selectLines(this, vars
     * .getLanguage(), Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), Tree
     * .getMembers(this, strTreeOrg,
     * strOrgTmpId),strRequisitionId,strProductTypeId,strProductGroupId_paraLike ,strDateFrom,
     * DateTimeData.nDaysAfter(this, strDateTo, "1"), strProductId, strRequesterId,
     * (strIncludeVendor.equals("Y") ? strVendorId : null), (strIncludeVendor.equals("Y") ? null :
     * strVendorId),strcBpartnetId);
     */

    // EDIT BY VAFASTER - ORG
    RequisitionToOrderData[] datalines = RequisitionToOrderData.selectLines(this,
        vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"),
        strOrgId, strRequisitionId, strProductTypeId, strProductGroupId_paraLike, strDateFrom,
        DateTimeData.nDaysAfter(this, strDateTo, "1"), strProductId, strRequesterId,
        (strIncludeVendor.equals("Y") ? strVendorId : null),
        (strIncludeVendor.equals("Y") ? null : strVendorId), strcBpartnetId, strProductType,
        strShwExludedProject, strProjectId);

    List<RequisitionToOrderData> listData = new ArrayList<RequisitionToOrderData>();

    // String strOrgIdAll= null;
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(vars.getClient());
    String strOrgPadreId = osp.getParentOrg(strOrgId);

    // Orginal de Openbravo
    /*
     * RequisitionToOrderData[] dataselected = RequisitionToOrderData.selectSelected(this,
     * vars.getLanguage(), vars.getUser(), Utility.getContext(this, vars, "#User_Client",
     * "RequisitionToOrder"), Tree.getMembers(this, strTreeOrg, strOrgId));//edit: reeplace strOrgI
     * to strOrgIdAll
     */

    // RequisitionToOrderData[] dataselected =
    // RequisitionToOrderData.selectSelectedAllOrganization(this,
    // vars.getLanguage(), vars.getUser(), Utility.getContext(this, vars, "#User_Client",
    // "RequisitionToOrder"), strOrgPadreId, vars.getClient());

    // strOrgTmpId Este parametro se envia en lugar de strOrgId
    // strOrgTmpId Sera null cuando el check de mostrar todo este activado (mostrar todo: mostrar
    // todos las necesidades de cualquier organizacion)

    // ORIGINAL
    /*
     * RequisitionToOrderData[] dataselected = RequisitionToOrderData.selectSelectedAll(this,
     * vars.getLanguage(), vars.getUser(), Utility.getContext(this, vars, "#User_Client",
     * "RequisitionToOrder"), strPadreOrgTmpId);
     */
    // EDIT BY VAFASTER SEND PARAMENTER ORGCHILD strOrgId
    RequisitionToOrderData[] dataselected = RequisitionToOrderData.selectSelectedAll(this,
        vars.getLanguage(), vars.getUser(),
        Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), strOrgId,
        strProductType, strShwExludedProject, strProjectId);
    //////////////////

    String discard[] = { "" };
    if (dataselected == null || dataselected.length == 0) {
      dataselected = RequisitionToOrderData.set();
      discard[0] = "funcSelectedEvenOddRow";
    }
    xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_forms/RequisitionToOrder", discard)
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RequisitionToOrder", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // VAFASTER GET NEW QUERY FOR PRODUCT QTY IN TRANSIT

    RequisitionToOrderData[] dataQTYProductInTransit = null;
    RequisitionToOrderData[] dataQTYProductInTransitResumen = null;
    if (!strProductId.equals("")) {
      Product prd_obj = OBDal.getInstance().get(Product.class, strProductId);
      dataQTYProductInTransit = RequisitionToOrderData.ProductInTransitGet(this, strProductId,
          prd_obj.getOrganization().getId(), vars.getSessionValue("#AD_CLIENT_ID"));
      dataQTYProductInTransitResumen = RequisitionToOrderData.ProductInTransitGetResumen(this,
          strProductId, prd_obj.getOrganization().getId(), vars.getSessionValue("#AD_CLIENT_ID"));

      String ConcatenadoTransito = "";
      for (int i = 0; i < dataQTYProductInTransitResumen.length; i++) {
        ConcatenadoTransito = ConcatenadoTransito + "Cantidad: "
            + dataQTYProductInTransitResumen[i].qtytransitresumen + " Fecha Prometida: "
            + dataQTYProductInTransitResumen[i].qtydatetransit + " | ";
      }
      for (int i = 0; i < dataQTYProductInTransit.length; i++) {
        dataQTYProductInTransit[i].productresumenid = ConcatenadoTransito;
      }
    }
    if (dataQTYProductInTransit == null || dataQTYProductInTransit.length == 0) {
      discard[0] = "selEliminar";
      dataQTYProductInTransit = RequisitionToOrderData.set();
    }
    xmlDocument.setData("structure3", dataQTYProductInTransit);

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.RequisitionToOrder");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "RequisitionToOrder.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "RequisitionToOrder.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("RequisitionToOrder");
      vars.removeMessage("RequisitionToOrder");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramProductId", strProductId);
    xmlDocument.setParameter("paramProductDescription", strProductId.equals("") ? ""
        : RequisitionToOrderData.mProductDescription(this, strProductId, vars.getLanguage()));
    xmlDocument.setParameter("displayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("paramRequester", strRequesterId);

    /*
     * Originales de Vendor xmlDocument.setParameter("paramBPartnerId", strVendorId);
     * xmlDocument.setParameter("paramBPartnerDescription", strVendorId.equals("") ? "" :
     * RequisitionToOrderData.bPartnerDescription(this, strVendorId, vars.getLanguage()));
     */

    /*
     * xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
     * xmlDocument.setParameter("paramBPartnerDescription",
     * RequisitionToOrderData.selectBpartner(this, strcBpartnetId));
     */

    xmlDocument.setParameter("paramRequisitionId", strRequisitionId);
    xmlDocument.setParameter("paramRequisitionDescription",
        RequisitionToOrderData.selectRequisition(this, strRequisitionId));

    xmlDocument.setParameter("paramProjectId", strProjectId);
    xmlDocument.setParameter("paramProjectDescription",
        RequisitionToOrderData.selectProjectName(this, strProjectId));

    xmlDocument.setParameter("paramShowNullVendor", strIncludeVendor);
    xmlDocument.setParameter("paramAdOrgId", strOrgId);

    xmlDocument.setParameter("paramProductCategory", strProductGroupId);
    xmlDocument.setParameter("paramProductType_ID", strProductTypeId);
    xmlDocument.setParameter("paramShowAll", strShowService);
    xmlDocument.setParameter("paramShowExcluded", strShowExcluded);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
          "UsersWithRequisition",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strRequesterId);
      xmlDocument.setData("reportRequester_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "ABE594ACE1764B7799DEF0BA6E8A389B",
          Utility.getContext(this, vars, "#User_Org", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder", strOrgId);
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "prdc_productgroup_id", "", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strProductGroupId);
      xmlDocument.setData("reportProductGroup_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    /*
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
     * "m_requisition_id", "", "sre_validation_requisitiontoCombo", Utility.getContext(this, vars,
     * "#User_Org", "RequisitionToOrder"), Utility.getContext(this, vars, "#User_Client",
     * "RequisitionToOrder"), 0); Utility.fillSQLParameters(this, vars, null, comboTableData,
     * "RequisitionToOrder", strRequisitionId); xmlDocument.setData("reportRequisition_ID",
     * "liststructure", comboTableData.select(false)); comboTableData = null; } catch (Exception ex)
     * { throw new ServletException(ex); }
     */

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "m_product_category_id", "", "",
          Utility.getContext(this, vars, "#User_Org", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strProductTypeId);
      xmlDocument.setData("reportProductType_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("structureSearch", datalines);
    xmlDocument.setData("structureSelected", dataselected);
    out.println(xmlDocument.print());
    out.close();
  }

  private void lockRequisitionLines(VariablesSecureApp vars, String strRequisitionLines)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Locking requisition lines: " + strRequisitionLines);
    RequisitionToOrderData.lock(this, vars.getUser(), strRequisitionLines);
  }

  private void unlockRequisitionLines(VariablesSecureApp vars, String strRequisitionLines)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Unlocking requisition lines: " + strRequisitionLines);
    RequisitionToOrderData.unlock(this, strRequisitionLines);
  }

  private void updateLockedLines(VariablesSecureApp vars, String strOrgId, String Requisition_id,
      String strShowService, String strShowExcluded, String strProjectId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Update locked lines");
    String strTreeOrg = RequisitionToOrderData.treeOrg(this, vars.getClient());

    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(vars.getClient());
    String strOrgPadreId = osp.getParentOrg(strOrgId);

    String strShowAll = vars.getSessionValue("strShowAll");

    String strPadreOrgTmpId = strOrgPadreId;
    if (strShowAll.equals("Y"))
      strPadreOrgTmpId = null;

    String strProductType = "I";
    if (strShowService.equals("Y"))
      strProductType = "S";

    String strShwExludedProject = "N";
    if (strShowExcluded.equals("Y"))
      strShwExludedProject = "Y";

    RequisitionToOrderData[] dataselected = RequisitionToOrderData.selectSelectedAll(this,
        vars.getLanguage(), vars.getUser(),
        Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), strOrgId,
        strProductType, strShwExludedProject, strProjectId);

    for (int i = 0; dataselected != null && i < dataselected.length; i++) {
      String strLockQty = vars.getNumericParameter("inpQty" + dataselected[i].mRequisitionlineId);
      String strLockPrice = vars
          .getNumericParameter("inpPrice" + dataselected[i].mRequisitionlineId);
      RequisitionToOrderData.updateLock(this, strLockQty, strLockPrice,
          dataselected[i].mRequisitionlineId);
    }
  }

  private void checkSelectedRequisitionLines(HttpServletResponse response, VariablesSecureApp vars,
      String strSelected, String strShowAll, String strProjectId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Check selected requisition lines");

    String strVendorId = "";
    String strOrderDate = DateTimeData.today(this);
    String strPriceListId = "";
    String strOrgId = "";
    String strMessage = "";
    if (!strSelected.equals("")) {
      // Check unique partner
      // RequisitionToOrderData[] vendor = RequisitionToOrderData.selectVendor(this, strSelected);
      // if (vendor != null && vendor.length == 1) {
      // strVendorId = vendor[0].vendorId;
      // strMessage = Utility.messageBD(this, "AllLinesSameVendor", vars.getLanguage()) + ": "
      // + RequisitionToOrderData.bPartnerDescription(this, vendor[0].vendorId,
      // vars.getLanguage());
      // } else if (vendor != null && vendor.length > 1) {
      // // Error, the selected lines are of different vendors, it is
      // // necessary to set one.
      // strMessage = Utility.messageBD(this, "MoreThanOneVendor", vars.getLanguage());
      // } else {
      // // Error, it is necessary to select a vendor.
      // strMessage = Utility.messageBD(this, "AllLinesNullVendor", vars.getLanguage());
      // }
      // reading project
      RequisitionToOrderData[] project = RequisitionToOrderData.selectProject(this, strSelected);
      if (project != null && project.length == 1) {
        strMessage += "<br>" + Utility.messageBD(this, "Project", vars.getLanguage()) + ": "
            + project[0].project;
      } else {
        strMessage += "<br>"
            + Utility.messageBD(this, "SPR_MoreThanOneProject", vars.getLanguage());
      }

      // Check unique org
      RequisitionToOrderData[] org = RequisitionToOrderData.selectOrg(this, vars.getLanguage(),
          strSelected);
      if (strShowAll.equals("N")) {
        if (org != null && org.length == 1) {
          strOrgId = org[0].adOrgId;
          strMessage += "<br>" + Utility.messageBD(this, "AllLinesSameOrg", vars.getLanguage())
              + ": " + org[0].org;
        } else {
          strMessage += "<br>" + Utility.messageBD(this, "MoreThanOneOrg", vars.getLanguage());
        }
      }

      OBError myMessage = new OBError();
      myMessage.setTitle("");
      myMessage.setMessage(strMessage);
      myMessage.setType("Info");
      vars.setMessage("RequisitionToOrderCreate", myMessage);

    } else {
      OBError myMessage = new OBError();
      myMessage.setTitle("");
      myMessage.setType("Info");
      myMessage.setMessage(Utility.messageBD(this, "MustSelectLines", vars.getLanguage()));
      vars.setMessage("RequisitionToOrderCreate", myMessage);
    }

    printPageCreate(response, vars, strOrderDate, strVendorId, strPriceListId, strOrgId,
        strSelected, strProjectId);

  }

  private void printPageCreate(HttpServletResponse response, VariablesSecureApp vars,
      String strOrderDate, String strVendorId, String strPriceListId, String strOrgId,
      String strSelected, String strProjectId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Print Create Purchase order");
    String strDescription = Utility.messageBD(this, "RequisitionToOrderCreate", vars.getLanguage());
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_forms/RequisitionToOrderCreate")
        .createXmlDocument();
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("help", Replace.replace(strDescription, "\\n", "\n"));
    xmlDocument.setParameter("paramLoginWarehouseId", vars.getSessionValue("#M_WAREHOUSE_ID"));
    {
      OBError myMessage = vars.getMessage("RequisitionToOrderCreate");
      vars.removeMessage("RequisitionToOrderCreate");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("paramSelected", strSelected);

    xmlDocument.setParameter("orderDate", strOrderDate);
    xmlDocument.setParameter("displayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramOrderOrgId", strOrgId);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "B35372A52ED6403B840B3525B4B6D770",
          Utility.getContext(this, vars, "#User_Org", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder", strOrgId);
      xmlDocument.setData("reportOrderOrg_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
          "0192D01CC8FC4BB2BF31D312D63DFB54", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "RequisitionToOrder"),
          Utility.getContext(this, vars, "#User_Client", "RequisitionToOrder"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "RequisitionToOrder",
          strPriceListId);
      xmlDocument.setData("reportPriceList_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    RequisitionToOrderData[] projectinfo = RequisitionToOrderData.getProjectInfo(this, strProjectId,
        vars.getLanguage());
    if (projectinfo != null && projectinfo.length != 0) {
      xmlDocument.setParameter("paramPriceListId", projectinfo[0].mPricelistId);

      xmlDocument.setParameter("mWarehouseId", projectinfo[0].mWarehouseId);
      xmlDocument.setParameter("mWarehouseDescription",
          RequisitionToOrderData.selectWarehouseIdentifier(this, projectinfo[0].mWarehouseId));
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private OBError processPurchaseOrder(VariablesSecureApp vars, String strSelected,
      String strOrderDate, String strVendor, String strPriceListId, String strOrg,
      String strWarehouse) throws IOException, ServletException {
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(vars.getClient());
    String strOrgPadreId = osp.getParentOrg(strOrg);

    StringBuffer textMessage = new StringBuffer();
    Connection conn = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    /*
     * if(strShowAll==null || strShowAll.equals("")) strShowAll = "N";
     */

    /*
     * Validacion de Productos de otras organizaciones que tengan correspondencia con algun producto
     * en la Organizacion a crear OC
     */

    String strPriceListVersionId = RequisitionToOrderData.getPricelistVersion(this, strPriceListId,
        strOrderDate);

    /*
     * //Original de Openbravo, se reemplaza por selectNoPriceAllOrg //Se debe poder crear ordenes
     * de necesidades creadas en otras organizaciones RequisitionToOrderData[] noprice =
     * RequisitionToOrderData.selectNoPrice(this, vars.getLanguage(), strPriceListVersionId,
     * strSelected);
     */

    RequisitionToOrderData[] noprice = RequisitionToOrderData.selectNoPriceAllOrg(this,
        vars.getLanguage(), strOrgPadreId, strSelected, strPriceListVersionId);

    if (noprice != null && noprice.length > 0) {
      textMessage.append(Utility.messageBD(this, "LinesWithNoPrice", vars.getLanguage()))
          .append("<br><ul>");
      RequisitionToOrder.strBDErrorMessage = "LinesWithNoPrice";
      for (int i = 0; i < noprice.length; i++) {
        textMessage.append("<li>").append(noprice[i].product);
      }
      textMessage.append("</ul>");
      myMessage.setType("Error");
      myMessage.setMessage(textMessage.toString());
      return myMessage;
    }

    RequisitionToOrderData[] data1 = RequisitionToOrderData.selectVendorData(this, strVendor);
    if (data1[0].poPaymenttermId == null || data1[0].poPaymenttermId.equals("")) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "VendorWithNoPaymentTerm", vars.getLanguage()));
      RequisitionToOrder.strBDErrorMessage = "VendorWithNoPaymentTerm";
      return myMessage;
    }

    if ("".equals(RequisitionToOrderData.cBPartnerLocationId(this, strVendor))) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "NoBPLocation", vars.getLanguage()));
      RequisitionToOrder.strBDErrorMessage = "NoBPLocation";
      return myMessage;
    }

    try {
      conn = getTransactionConnection();
      String strCOrderId = SequenceIdData.getUUID();
      corderid = strCOrderId;
      String docTargetType = "";
      String strDocumentNo = "";

      String cCurrencyId = RequisitionToOrderData.selectCurrency(this, strPriceListId);

      /*
       * GET IF THIS REQUISITION IS FROM SERVICE
       */

      RequisitionToOrderData[] lines2 = RequisitionToOrderData.linesToOrderAllOrg(this,
          strOrderDate, strOrg, strWarehouse,
          RequisitionToOrderData.billto(this, strVendor).equals("")
              ? RequisitionToOrderData.cBPartnerLocationId(this, strVendor)
              : RequisitionToOrderData.billto(this, strVendor),
          RequisitionToOrderData.cBPartnerLocationId(this, strVendor), cCurrencyId, strOrgPadreId,
          strSelected, strPriceListVersionId);

      Product pr = OBDal.getInstance().get(Product.class, lines2[0].mProductId);

      String productype = pr != null ? pr.getProductType() : "";

      boolean isservice = false;
      if (productype.equals("S"))
        isservice = true;

      if (isservice) {
        Organization org = OBDal.getInstance().get(Organization.class, strOrg);
        DocumentType c_doctype = SCO_Utils.getDocTypeFromSpecial(org, "SREPURCHASEORDERSERVICE");
        docTargetType = c_doctype != null ? c_doctype.getId() : "";
        strDocumentNo = SCO_Utils.getDocumentNo(c_doctype, "C_Order");

      } else {

        docTargetType = RequisitionToOrderData.cDoctypeTarget(conn, this, vars.getClient(), strOrg);
        strDocumentNo = Utility.getDocumentNo(this, vars, "", "C_Order", docTargetType,
            docTargetType, false, true);

      }

      try {
        RequisitionToOrderData.insertCOrder(conn, this, strCOrderId, vars.getClient(), strOrg,
            vars.getUser(), strDocumentNo, "DR", "CO", "0", docTargetType, strOrderDate,
            strOrderDate, strOrderDate, strVendor,
            RequisitionToOrderData.cBPartnerLocationId(this, strVendor),
            RequisitionToOrderData.billto(this, strVendor).equals("")
                ? RequisitionToOrderData.cBPartnerLocationId(this, strVendor)
                : RequisitionToOrderData.billto(this, strVendor),
            cCurrencyId, isAlternativeFinancialFlow() ? "P" : data1[0].paymentrulepo,
            data1[0].poPaymenttermId, data1[0].invoicerule.equals("") ? "I" : data1[0].invoicerule,
            data1[0].deliveryrule.equals("") ? "A" : data1[0].deliveryrule, "I",
            data1[0].deliveryviarule.equals("") ? "D" : data1[0].deliveryviarule, strWarehouse,
            strPriceListId, "", "", "", data1[0].poPaymentmethodId);
      } catch (ServletException ex) {

        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        releaseRollbackConnection(conn);
        return myMessage;
      }

      int line = 0;
      String strCOrderlineID = "";
      BigDecimal qty = new BigDecimal("0");
      BigDecimal qtyOrder = new BigDecimal("0");
      boolean insertLine = false;

      /*
       * RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrder( this, strOrderDate,
       * strOrg, strWarehouse, RequisitionToOrderData.billto(this, strVendor).equals("") ?
       * RequisitionToOrderData .cBPartnerLocationId(this, strVendor) : RequisitionToOrderData
       * .billto(this, strVendor), RequisitionToOrderData.cBPartnerLocationId(this, strVendor),
       * cCurrencyId, strPriceListVersionId, strSelected);
       */

      RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrderAllOrg(this, strOrderDate,
          strOrg, strWarehouse,
          RequisitionToOrderData.billto(this, strVendor).equals("")
              ? RequisitionToOrderData.cBPartnerLocationId(this, strVendor)
              : RequisitionToOrderData.billto(this, strVendor),
          RequisitionToOrderData.cBPartnerLocationId(this, strVendor), cCurrencyId, strOrgPadreId,
          strSelected, strPriceListVersionId);

      String projectId = null;
      String strExcludeFromProject = "N";
      for (int i = 0; lines != null && i < lines.length; i++) {
        RequisitionLine rl = OBDal.getInstance().get(RequisitionLine.class,
            lines[i].mRequisitionlineId);

        if (rl.getRequisition().isSprExcludefromproject())
          strExcludeFromProject = "Y";

        if ("".equals(lines[i].tax)) {
          myMessage.setType("Error");
          myMessage.setMessage(String.format(OBMessageUtils.messageBD("NoTaxRequisition"),
              rl.getLineNo(), rl.getRequisition().getDocumentNo()));
          releaseRollbackConnection(conn);
          return myMessage;
        }

        if (i == 0) {
          projectId = rl.getRequisition().getSprProject().getId();
        } else {
          if (projectId.compareTo(rl.getRequisition().getSprProject().getId()) != 0) {
            myMessage.setMessage(String.format(OBMessageUtils.messageBD("SPR_MoreThanOneProject"),
                rl.getLineNo(), rl.getRequisition().getDocumentNo()));
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }

        if (i == 0)
          strCOrderlineID = SequenceIdData.getUUID();
        if (i == lines.length - 1) {
          insertLine = true;
          qtyOrder = qty;
        } else if (!lines[i + 1].mProductId.equals(lines[i].mProductId)
            || !lines[i + 1].mAttributesetinstanceId.equals(lines[i].mAttributesetinstanceId)
            || !lines[i + 1].description.equals(lines[i].description)
            || !lines[i + 1].priceactual.equals(lines[i].priceactual)) {
          insertLine = true;
          qtyOrder = qty;
          qty = new BigDecimal(0);
        } else {
          qty = qty.add(new BigDecimal(lines[i].lockqty));
        }
        lines[i].cOrderlineId = strCOrderlineID;
        if (insertLine) {
          insertLine = false;
          line += 1;

          BigDecimal qtyAux = new BigDecimal(lines[i].lockqty);
          qtyOrder = qtyOrder.add(qtyAux);
          if (log4j.isDebugEnabled())
            log4j.debug("Lockqty: " + lines[i].lockqty + " qtyorder: " + qtyOrder.toPlainString()
                + " new BigDecimal: " + (new BigDecimal(lines[i].lockqty)).toString() + " qtyAux: "
                + qtyAux.toString());

          try {

            // OrganizationStructureProvider osp =
            // OBContext.getOBContext().getOrganizationStructureProvider(vars.getClient());
            // String strOrgPadreId = osp.getParentOrg(strOrgId);

            RequisitionToOrderData.insertCOrderline(conn, this, strCOrderlineID, vars.getClient(),
                strOrg, vars.getUser(), strCOrderId, Integer.toString(line), strVendor,
                RequisitionToOrderData.cBPartnerLocationId(this, strVendor), strOrderDate,
                lines[i].needbydate, lines[i].description, lines[i].mProductId,
                lines[i].mAttributesetinstanceId, strWarehouse, lines[i].mProductUomId,
                lines[i].cUomId, lines[i].quantityorder, qtyOrder.toPlainString(), cCurrencyId,
                lines[i].pricelist, lines[i].priceactual, strPriceListId, lines[i].pricelimit,
                lines[i].tax, "", lines[i].discount, lines[i].grossUnit, lines[i].grossAmt,
                lines[i].inforeq, projectId, "Y");
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          strCOrderlineID = SequenceIdData.getUUID();
        }
      }

      try {
        RequisitionToOrderData.updateCOrderProject(conn, this, projectId, strExcludeFromProject,
            strCOrderId);
        RequisitionToOrderData.updateCOrderLineProject(conn, this, projectId, strCOrderId);
      } catch (ServletException ex) {

        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        releaseRollbackConnection(conn);
        return myMessage;
      }

      unlockRequisitionLines(vars, strSelected);
      for (int i = 0; lines != null && i < lines.length; i++) {
        String strRequisitionOrderId = SequenceIdData.getUUID();
        try {
          RequisitionToOrderData.insertRequisitionOrder(conn, this, strRequisitionOrderId,
              vars.getClient(), strOrg, vars.getUser(), lines[i].mRequisitionlineId,
              lines[i].cOrderlineId, lines[i].lockqty);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }
        if (lines[i].toClose.equals("Y"))
          RequisitionToOrderData.requisitionStatus(conn, this, lines[i].mRequisitionlineId,
              vars.getUser());
      }

      releaseCommitConnection(conn);
      String strWindowName = WindowTabsData.selectWindowInfo(this, vars.getLanguage(), "181");
      textMessage.append(strWindowName).append(" ").append(strDocumentNo).append(": ");
      textMessage.append(Utility.messageBD(this, "Success", vars.getLanguage()));
      myMessage.setType("Success");
      myMessage.setMessage(textMessage.toString());
      return myMessage;

    } catch (Exception e) {
      try {
        if (conn != null)
          releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
    }
  }

  private OBError processPurchaseOrderImport2(VariablesSecureApp vars, String strSelected,
      String strOrderDate, String strVendor, String strPriceListId, String strOrg,
      String strWarehouse) throws IOException, ServletException {

    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(vars.getClient());
    String strOrgPadreId = osp.getParentOrg(strOrg);

    StringBuffer textMessage = new StringBuffer();
    Connection conn = null;

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    String strPriceListVersionId = RequisitionToOrderData.getPricelistVersion(this, strPriceListId,
        strOrderDate);

    /*
     * Original de Openbravo, se reemplaza por AllOrg RequisitionToOrderData[] noprice =
     * RequisitionToOrderData.selectNoPrice(this, vars.getLanguage(), strPriceListVersionId,
     * strSelected);
     */

    RequisitionToOrderData[] noprice = RequisitionToOrderData.selectNoPriceAllOrg(this,
        vars.getLanguage(), strOrgPadreId, strSelected, strPriceListVersionId);

    if (noprice != null && noprice.length > 0) {
      textMessage.append(Utility.messageBD(this, "LinesWithNoPrice", vars.getLanguage()))
          .append("<br><ul>");
      for (int i = 0; i < noprice.length; i++) {
        textMessage.append("<li>").append(noprice[i].product);
      }
      textMessage.append("</ul>");
      myMessage.setType("Error");
      myMessage.setMessage(textMessage.toString());
      return myMessage;
    }

    RequisitionToOrderData[] data1 = RequisitionToOrderData.selectVendorData(this, strVendor);
    if (data1[0].poPaymenttermId == null || data1[0].poPaymenttermId.equals("")) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "VendorWithNoPaymentTerm", vars.getLanguage()));
      return myMessage;
    }
    if ("".equals(RequisitionToOrderData.cBPartnerLocationId(this, strVendor))) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "NoBPLocation", vars.getLanguage()));
      return myMessage;
    }

    PriceList pricelist = OBDal.getInstance().get(PriceList.class, strPriceListId);
    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
    BpartnerMiscData[] c_bpartner_data = BpartnerMiscData.select(this, strVendor);
    Query q = OBDal.getInstance().getSession().createSQLQuery(
        "SELECT c_paymentterm_id FROM c_paymentterm WHERE em_sco_specialpayterm='SCOINMEDIATETERM' AND ad_client_id='"
            + vars.getClient() + "' LIMIT 1");
    String C_PaymentTerm_ID = (String) q.uniqueResult();

    Query q2 = OBDal.getInstance().getSession().createSQLQuery(
        "SELECT c_tax_id FROM c_tax WHERE em_sco_specialtax='SCOEXEMPT' AND ad_client_id='"
            + vars.getClient() + "' LIMIT 1");
    String C_Tax_ID = (String) q2.uniqueResult();
    TaxRate tax = OBDal.getInstance().get(TaxRate.class, C_Tax_ID);

    if (c_bpartner_data[0].poPaymenttermId != null) {
      C_PaymentTerm_ID = (String) q.uniqueResult();
    }
    PaymentTerm payment_term = OBDal.getInstance().get(PaymentTerm.class, C_PaymentTerm_ID);
    FIN_PaymentMethod payment_method = OBDal.getInstance().get(FIN_PaymentMethod.class,
        c_bpartner_data[0].poPaymentmethodId);

    Client client = OBDal.getInstance().get(Client.class, vars.getClient());
    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    DocumentType c_doctype = SCO_Utils.getDocTypeFromSpecial(org, "SIMORDERIMPORT");
    if (c_doctype == null) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "SRE_DoctypeMissing", vars.getLanguage()));
      return myMessage;
    }

    BusinessPartner c_bpartner = OBDal.getInstance().get(BusinessPartner.class, strVendor);
    String DocumentNo = SCO_Utils.getDocumentNo(c_doctype, "SIM_OrderImport");
    String cCurrencyId = RequisitionToOrderData.selectCurrency(this, strPriceListId);

    // conn = getTransactionConnection();
    String strCOrderImportId = SequenceIdData.getUUID();

    OBCriteria<Incoterms> incoterms = OBDal.getInstance().createCriteria(Incoterms.class);
    incoterms.add(Restrictions.eq(Incoterms.PROPERTY_CLIENT, client));
    incoterms.add(Restrictions.ilike(Incoterms.PROPERTY_NAME, "FOB"));
    Incoterms incoterm = null;
    try {
      incoterm = (Incoterms) incoterms.uniqueResult();
      if (incoterm == null) {
        myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "SRE_NoIncoterm", vars.getLanguage()));
        return myMessage;
      }
    } catch (Exception e) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "SRE_NoIncoterm", vars.getLanguage()));
      return myMessage;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    Date docDate = null;
    try {
      docDate = formatter.parse(strOrderDate);
    } catch (Exception e) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "InvalidDateFormat", vars.getLanguage()));
      return myMessage;
    }

    SimOrderImport orderImport = OBProvider.getInstance().get(SimOrderImport.class);
    orderImport.setClient(client);
    orderImport.setOrganization(org);
    orderImport.setActive(true);
    orderImport.setUpdated(new Date());
    orderImport.setDocumentType(c_doctype);
    orderImport.setDocumentStatus("DR");
    orderImport.setDocumentNo(DocumentNo);
    orderImport.setOrderDate(docDate);
    orderImport.setScheduledDeliveryDate(docDate);
    orderImport.setEmployee(c_bpartner);
    orderImport.setPaymentTerms(payment_term);
    orderImport.setPaymentMethod(payment_method);
    orderImport.setPriceList(pricelist);
    orderImport.setWarehouse(warehouse);
    orderImport.setProcessed(false);
    orderImport.setPartnerAddress(c_bpartner.getBusinessPartnerLocationList().get(0));
    orderImport.setCurrency(pricelist.getCurrency());
    orderImport.setCountry(
        c_bpartner.getBusinessPartnerLocationList().get(0).getLocationAddress().getCountry());
    orderImport.setTypepurchaseorder("NO");
    orderImport.setApprove(false);
    orderImport.setDisapprove(false);
    orderImport.setReactivate(false);
    orderImport.setPartialGen(false);
    orderImport.setIncoterms(incoterm);
    OBDal.getInstance().save(orderImport);

    int line = 0;
    String strCOrderlineID = "";
    BigDecimal qty = new BigDecimal("0");
    BigDecimal qtyOrder = new BigDecimal("0");
    boolean insertLine = false;

    /*
     * RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrder( this, strOrderDate,
     * strOrg, strWarehouse, RequisitionToOrderData.billto(this, strVendor).equals("") ?
     * RequisitionToOrderData .cBPartnerLocationId(this, strVendor) : RequisitionToOrderData
     * .billto(this, strVendor), RequisitionToOrderData.cBPartnerLocationId(this, strVendor),
     * cCurrencyId, strPriceListVersionId, strSelected);
     */

    RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrderAllOrg(this, strOrderDate,
        strOrg, strWarehouse,
        RequisitionToOrderData.billto(this, strVendor).equals("")
            ? RequisitionToOrderData.cBPartnerLocationId(this, strVendor)
            : RequisitionToOrderData.billto(this, strVendor),
        RequisitionToOrderData.cBPartnerLocationId(this, strVendor), cCurrencyId, strOrgPadreId,
        strSelected, strPriceListVersionId);

    String projectId = null;
    long lineNo = 10L;
    for (int i = 0; lines != null && i < lines.length; i++) {
      RequisitionLine rl = OBDal.getInstance().get(RequisitionLine.class,
          lines[i].mRequisitionlineId);

      if ("".equals(lines[i].tax)) {
        myMessage.setType("Error");
        myMessage.setMessage(String.format(OBMessageUtils.messageBD("NoTaxRequisition"),
            rl.getLineNo(), rl.getRequisition().getDocumentNo()));
        // releaseRollbackConnection(conn);
        return myMessage;
      }

      if (i == 0) {
        projectId = rl.getRequisition().getSprProject().getId();
      } else {
        if (projectId.compareTo(rl.getRequisition().getSprProject().getId()) != 0) {
          myMessage.setMessage(String.format(OBMessageUtils.messageBD("SPR_MoreThanOneProject"),
              rl.getLineNo(), rl.getRequisition().getDocumentNo()));
          // releaseRollbackConnection(conn);
          return myMessage;
        }
      }

      if (i == 0)
        strCOrderlineID = SequenceIdData.getUUID();
      if (i == lines.length - 1) {
        insertLine = true;
        qtyOrder = qty;
      } else if (!lines[i + 1].mProductId.equals(lines[i].mProductId)
          || !lines[i + 1].mAttributesetinstanceId.equals(lines[i].mAttributesetinstanceId)
          || !lines[i + 1].description.equals(lines[i].description)
          || !lines[i + 1].priceactual.equals(lines[i].priceactual)) {
        insertLine = true;
        qtyOrder = qty;
        qty = new BigDecimal(0);
      } else {
        qty = qty.add(new BigDecimal(lines[i].lockqty));
      }

      if (insertLine) {
        insertLine = false;
        line += 10;
        Product product = OBDal.getInstance().get(Product.class, lines[i].mProductId);
        BigDecimal[] prices = selectPriceForProduct(vars.getClient(), product.getId(), strOrg,
            strPriceListId);
        if (prices == null) {
          myMessage.setType("Error");
          myMessage.setMessage(Utility.messageBD(this, "PricelistNotdefined", vars.getLanguage()));
          return myMessage;
        }
        BigDecimal priceList = prices[0];
        BigDecimal priceStd = prices[1];
        BigDecimal priceLimit = prices[2];
        BigDecimal qtyAux = new BigDecimal(lines[i].lockqty);
        qtyOrder = qtyOrder.add(qtyAux);

        SimOrderImportLine orderImportLine = OBProvider.getInstance().get(SimOrderImportLine.class);
        orderImportLine.setClient(client);
        orderImportLine.setOrganization(org);
        orderImportLine.setActive(true);
        orderImportLine.setUpdated(new Date());
        orderImportLine.setSIMOrderimport(orderImport);
        orderImportLine.setLineNo(lineNo);
        orderImportLine.setOrderDate(docDate);
        orderImportLine.setProduct(product);
        orderImportLine.setWarehouse(warehouse);
        orderImportLine.setUOM(product.getUOM());
        orderImportLine.setOrderedQuantity(qtyOrder.longValue());
        orderImportLine.setReservedQuantity(qtyOrder);
        orderImportLine.setCurrency(pricelist.getCurrency());
        orderImportLine.setListPrice(priceList);
        orderImportLine.setUnitPrice(priceStd);
        orderImportLine.setPriceLimit(priceLimit.longValue());
        orderImportLine
            .setLineNetAmount(new BigDecimal(qtyOrder.longValue() * priceStd.longValue()));
        orderImportLine.setTax(tax);
        orderImportLine.setDiscount(new BigDecimal(0));
        OBDal.getInstance().save(orderImportLine);

        lines[i].cOrderlineId = orderImportLine.getId();
      }

    }

    unlockRequisitionLines(vars, strSelected);
    for (int i = 0; lines != null && i < lines.length; i++) {
      RequisitionLine req_line = OBDal.getInstance().get(RequisitionLine.class,
          lines[i].mRequisitionlineId);
      SimOrderImportLine importLine = OBDal.getInstance().get(SimOrderImportLine.class,
          lines[i].cOrderlineId);
      RequisitionPOMatch requisitionorder = OBProvider.getInstance().get(RequisitionPOMatch.class);
      requisitionorder.setClient(client);
      requisitionorder.setOrganization(org);
      requisitionorder.setUpdated(new Date());
      requisitionorder.setRequisitionLine(req_line);
      requisitionorder.setQuantity(new BigDecimal(lines[i].lockqty));
      requisitionorder.setSimOrderimportline(importLine);
      OBDal.getInstance().save(requisitionorder);
      if (lines[i].toClose.equals("Y"))
        RequisitionToOrderData.requisitionImportStatus(conn, this, lines[i].mRequisitionlineId,
            vars.getUser());
    }
    String strWindowName = WindowTabsData.selectWindowInfo(this, vars.getLanguage(),
        "BCF18D4E9C224B4294B8D969189F4A23");
    textMessage.append(strWindowName).append(" ").append(DocumentNo).append(": ");
    myMessage.setType("success");
    myMessage.setMessage(textMessage.toString());
    return myMessage;

  }

  private OBError processOrderReposition(VariablesSecureApp vars, String strSelected,
      String strOrderDate, String strVendor, String strPriceListId, String strOrg,
      String strWarehouse) throws IOException, ServletException {

    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(vars.getClient());
    String strOrgPadreId = osp.getParentOrg(strOrg);

    StringBuffer textMessage = new StringBuffer();
    Connection conn = null;

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    String strPriceListVersionId = RequisitionToOrderData.getPricelistVersion(this, strPriceListId,
        strOrderDate);

    /*
     * Original de Openbravo, se reemplaza por AllOrg RequisitionToOrderData[] noprice =
     * RequisitionToOrderData.selectNoPrice(this, vars.getLanguage(), strPriceListVersionId,
     * strSelected);
     */

    RequisitionToOrderData[] noprice = RequisitionToOrderData.selectNoPriceAllOrg(this,
        vars.getLanguage(), strOrgPadreId, strSelected, strPriceListVersionId);

    if (noprice != null && noprice.length > 0) {
      textMessage.append(Utility.messageBD(this, "LinesWithNoPrice", vars.getLanguage()))
          .append("<br><ul>");
      for (int i = 0; i < noprice.length; i++) {
        textMessage.append("<li>").append(noprice[i].product);
      }
      textMessage.append("</ul>");
      myMessage.setType("Error");
      myMessage.setMessage(textMessage.toString());
      return myMessage;
    }

    RequisitionToOrderData[] data1 = RequisitionToOrderData.selectVendorData(this, strVendor);
    if (data1[0].poPaymenttermId == null || data1[0].poPaymenttermId.equals("")) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "VendorWithNoPaymentTerm", vars.getLanguage()));
      return myMessage;
    }
    if ("".equals(RequisitionToOrderData.cBPartnerLocationId(this, strVendor))) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "NoBPLocation", vars.getLanguage()));
      return myMessage;
    }

    PriceList pricelist = OBDal.getInstance().get(PriceList.class, strPriceListId);
    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, strWarehouse);
    BpartnerMiscData[] c_bpartner_data = BpartnerMiscData.select(this, strVendor);
    Query q = OBDal.getInstance().getSession().createSQLQuery(
        "SELECT c_paymentterm_id FROM c_paymentterm WHERE em_sco_specialpayterm='SCOINMEDIATETERM' AND ad_client_id='"
            + vars.getClient() + "' LIMIT 1");
    String C_PaymentTerm_ID = (String) q.uniqueResult();

    Query q2 = OBDal.getInstance().getSession().createSQLQuery(
        "SELECT c_tax_id FROM c_tax WHERE em_sco_specialtax='SCOEXEMPT' AND ad_client_id='"
            + vars.getClient() + "' LIMIT 1");
    String C_Tax_ID = (String) q2.uniqueResult();
    TaxRate tax = OBDal.getInstance().get(TaxRate.class, C_Tax_ID);

    if (c_bpartner_data[0].poPaymenttermId != null) {
      C_PaymentTerm_ID = (String) q.uniqueResult();
    }
    PaymentTerm payment_term = OBDal.getInstance().get(PaymentTerm.class, C_PaymentTerm_ID);
    FIN_PaymentMethod payment_method = OBDal.getInstance().get(FIN_PaymentMethod.class,
        c_bpartner_data[0].poPaymentmethodId);

    Client client = OBDal.getInstance().get(Client.class, vars.getClient());
    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    DocumentType c_doctype = SCO_Utils.getDocTypeFromSpecial(org, "SIMORDERIMPORT");
    if (c_doctype == null) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "SRE_DoctypeMissing", vars.getLanguage()));
      return myMessage;
    }

    BusinessPartner c_bpartner = OBDal.getInstance().get(BusinessPartner.class, strVendor);
    String DocumentNo = SCO_Utils.getDocumentNo(c_doctype, "SIM_OrderImport");
    String cCurrencyId = RequisitionToOrderData.selectCurrency(this, strPriceListId);

    // conn = getTransactionConnection();
    String strCOrderImportId = SequenceIdData.getUUID();

    OBCriteria<Incoterms> incoterms = OBDal.getInstance().createCriteria(Incoterms.class);
    incoterms.add(Restrictions.eq(Incoterms.PROPERTY_CLIENT, client));
    incoterms.add(Restrictions.ilike(Incoterms.PROPERTY_NAME, "FOB"));
    Incoterms incoterm = null;
    try {
      incoterm = (Incoterms) incoterms.uniqueResult();
      if (incoterm == null) {
        myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "SRE_NoIncoterm", vars.getLanguage()));
        return myMessage;
      }
    } catch (Exception e) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "SRE_NoIncoterm", vars.getLanguage()));
      return myMessage;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    Date docDate = null;
    try {
      docDate = formatter.parse(strOrderDate);
    } catch (Exception e) {
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "InvalidDateFormat", vars.getLanguage()));
      return myMessage;
    }

    /***
     * GENERATE SWAREPOSITION*
     * 
     */

    SwaRequerimientoReposicion reposition = OBProvider.getInstance()
        .get(SwaRequerimientoReposicion.class);
    reposition.setClient(client);
    reposition.setOrganization(org);
    reposition.setActive(true);
    reposition.setUpdated(new Date());
    reposition.setMWarehouse(warehouse);
    reposition.setFromMWarehouse(warehouse);
    reposition.setAlertStatus("DR");
    reposition.setPickinglistState("NG");
    reposition.setBusinessPartner(c_bpartner);
    reposition.setPartnerAddress(c_bpartner.getBusinessPartnerLocationList().get(0));
    reposition.setDocumentType(c_doctype);
    reposition.setDocumentNo(DocumentNo);
    reposition.setProcess(false);
    reposition.setDelivered("EM");
    reposition.setWarehouseType("NO");
    reposition.setDoctypeCreate(c_doctype);
    reposition.setWindowsnameList("ST");
    reposition.setSpecialdoctype("");
    reposition.setComboItem(null);
    OBDal.getInstance().save(reposition);

    int line = 0;
    String strCOrderlineID = "";
    BigDecimal qty = new BigDecimal("0");
    BigDecimal qtyOrder = new BigDecimal("0");
    boolean insertLine = false;

    /*
     * RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrder( this, strOrderDate,
     * strOrg, strWarehouse, RequisitionToOrderData.billto(this, strVendor).equals("") ?
     * RequisitionToOrderData .cBPartnerLocationId(this, strVendor) : RequisitionToOrderData
     * .billto(this, strVendor), RequisitionToOrderData.cBPartnerLocationId(this, strVendor),
     * cCurrencyId, strPriceListVersionId, strSelected);
     */

    RequisitionToOrderData[] lines = RequisitionToOrderData.linesToOrderAllOrg(this, strOrderDate,
        strOrg, strWarehouse,
        RequisitionToOrderData.billto(this, strVendor).equals("")
            ? RequisitionToOrderData.cBPartnerLocationId(this, strVendor)
            : RequisitionToOrderData.billto(this, strVendor),
        RequisitionToOrderData.cBPartnerLocationId(this, strVendor), cCurrencyId, strOrgPadreId,
        strSelected, strPriceListVersionId);

    String projectId = null;
    long lineNo = 10L;
    for (int i = 0; lines != null && i < lines.length; i++) {
      RequisitionLine rl = OBDal.getInstance().get(RequisitionLine.class,
          lines[i].mRequisitionlineId);

      if ("".equals(lines[i].tax)) {
        myMessage.setType("Error");
        myMessage.setMessage(String.format(OBMessageUtils.messageBD("NoTaxRequisition"),
            rl.getLineNo(), rl.getRequisition().getDocumentNo()));
        // releaseRollbackConnection(conn);
        return myMessage;
      }

      if (i == 0) {
        projectId = rl.getRequisition().getSprProject().getId();
      } else {
        if (projectId.compareTo(rl.getRequisition().getSprProject().getId()) != 0) {
          myMessage.setMessage(String.format(OBMessageUtils.messageBD("SPR_MoreThanOneProject"),
              rl.getLineNo(), rl.getRequisition().getDocumentNo()));
          // releaseRollbackConnection(conn);
          return myMessage;
        }
      }

      if (i == 0)
        strCOrderlineID = SequenceIdData.getUUID();
      if (i == lines.length - 1) {
        insertLine = true;
        qtyOrder = qty;
      } else if (!lines[i + 1].mProductId.equals(lines[i].mProductId)
          || !lines[i + 1].mAttributesetinstanceId.equals(lines[i].mAttributesetinstanceId)
          || !lines[i + 1].description.equals(lines[i].description)
          || !lines[i + 1].priceactual.equals(lines[i].priceactual)) {
        insertLine = true;
        qtyOrder = qty;
        qty = new BigDecimal(0);
      } else {
        qty = qty.add(new BigDecimal(lines[i].lockqty));
      }

      if (insertLine) {
        insertLine = false;
        line += 10;
        Product product = OBDal.getInstance().get(Product.class, lines[i].mProductId);
        BigDecimal[] prices = selectPriceForProduct(vars.getClient(), product.getId(), strOrg,
            strPriceListId);
        if (prices == null) {
          myMessage.setType("Error");
          myMessage.setMessage(Utility.messageBD(this, "PricelistNotdefined", vars.getLanguage()));
          return myMessage;
        }
        BigDecimal priceList = prices[0];
        BigDecimal priceStd = prices[1];
        BigDecimal priceLimit = prices[2];
        BigDecimal qtyAux = new BigDecimal(lines[i].lockqty);
        qtyOrder = qtyOrder.add(qtyAux);

        SimOrderImportLine orderImportLine = OBProvider.getInstance().get(SimOrderImportLine.class);
        orderImportLine.setClient(client);
        orderImportLine.setOrganization(org);
        orderImportLine.setActive(true);
        orderImportLine.setUpdated(new Date());
        /// orderImportLine.setSIMOrderimport(orderImport);
        orderImportLine.setLineNo(lineNo);
        orderImportLine.setOrderDate(docDate);
        orderImportLine.setProduct(product);
        orderImportLine.setWarehouse(warehouse);
        orderImportLine.setUOM(product.getUOM());
        orderImportLine.setOrderedQuantity(qtyOrder.longValue());
        orderImportLine.setReservedQuantity(qtyOrder);
        orderImportLine.setCurrency(pricelist.getCurrency());
        orderImportLine.setListPrice(priceList);
        orderImportLine.setUnitPrice(priceStd);
        orderImportLine.setPriceLimit(priceLimit.longValue());
        orderImportLine
            .setLineNetAmount(new BigDecimal(qtyOrder.longValue() * priceStd.longValue()));
        orderImportLine.setTax(tax);
        orderImportLine.setDiscount(new BigDecimal(0));
        OBDal.getInstance().save(orderImportLine);

        lines[i].cOrderlineId = orderImportLine.getId();
      }

    }

    unlockRequisitionLines(vars, strSelected);
    for (int i = 0; lines != null && i < lines.length; i++) {
      RequisitionLine req_line = OBDal.getInstance().get(RequisitionLine.class,
          lines[i].mRequisitionlineId);
      SimOrderImportLine importLine = OBDal.getInstance().get(SimOrderImportLine.class,
          lines[i].cOrderlineId);
      RequisitionPOMatch requisitionorder = OBProvider.getInstance().get(RequisitionPOMatch.class);
      requisitionorder.setClient(client);
      requisitionorder.setOrganization(org);
      requisitionorder.setUpdated(new Date());
      requisitionorder.setRequisitionLine(req_line);
      requisitionorder.setQuantity(new BigDecimal(lines[i].lockqty));
      requisitionorder.setSimOrderimportline(importLine);
      OBDal.getInstance().save(requisitionorder);
      if (lines[i].toClose.equals("Y"))
        RequisitionToOrderData.requisitionImportStatus(conn, this, lines[i].mRequisitionlineId,
            vars.getUser());
    }
    String strWindowName = WindowTabsData.selectWindowInfo(this, vars.getLanguage(),
        "BCF18D4E9C224B4294B8D969189F4A23");
    textMessage.append(strWindowName).append(" ").append(DocumentNo).append(": ");
    myMessage.setType("success");
    myMessage.setMessage(textMessage.toString());
    return myMessage;

  }

  public static BigDecimal[] selectPriceForProduct(String ADClientID, String MProductID,
      String CBPartnerID, String MPOPriceListID) throws ServletException {
    String strSql = "";
    strSql = "   SELECT PriceList, PriceStd, PriceLimit FROM M_ProductPrice WHERE M_Product_ID = ? "
        + "   AND M_PRICELIST_VERSION_ID = ( "
        + "   SELECT min(plv.M_PriceList_Version_ID) as M_PriceList_Version_ID "
        + "   FROM M_PriceList_Version plv " + "   WHERE plv.M_PriceList_ID = ? "
        + "   AND plv.IsActive= 'Y' " + "   AND plv.ValidFrom <= now() "
        + "   AND plv.AD_Client_ID =? " + "   AND ValidFrom = (SELECT max(ValidFrom) "
        + "   FROM M_PriceList pl, M_PriceList_Version plv "
        + "   WHERE pl.M_PriceList_ID=plv.M_PriceList_ID " + "AND plv.IsActive= 'Y' "
        + "   AND pl.M_PriceList_ID = ? " + "   AND plv.ValidFrom <= now() "
        + "   AND plv.AD_Client_ID =?));  ";

    BigDecimal[] price_data = new BigDecimal[3];
    try {
      Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);
      sqlQuery.setParameter(0, MProductID);
      sqlQuery.setParameter(1, MPOPriceListID);
      sqlQuery.setParameter(2, ADClientID);
      sqlQuery.setParameter(3, MPOPriceListID);
      sqlQuery.setParameter(4, ADClientID);

      List<Object> prods = sqlQuery.list();
      Object[] obj = (Object[]) prods.get(0);

      price_data[0] = new BigDecimal(obj[0].toString()); // pricelist obj[0]
      price_data[1] = new BigDecimal(obj[1].toString());// pricestd obj[1]
      price_data[2] = new BigDecimal(obj[2].toString()); // pricelimit obj[2]

    } catch (Exception ex) {
      throw new ServletException("@CODE=@" + ex.getMessage());
    }
    return price_data;
  }

  private OBError cOrderPost(Connection conn, VariablesSecureApp vars, String strcOrderId)
      throws IOException, ServletException {
    String pinstance = SequenceIdData.getUUID();

    PInstanceProcessData.insertPInstance(conn, this, pinstance, "104", strcOrderId, "N",
        vars.getUser(), vars.getClient(), vars.getOrg());
    RequisitionToOrderData.cOrderPost0(conn, this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.selectConnection(conn, this,
        pinstance);
    OBError myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    return myMessage;
  }

  /**
   * Checks if the any module implements and alternative Financial Management preference. It should
   * be the Advanced Payables and Receivables module.
   * 
   * @return true if any module implements and alternative Financial Management preference.
   */
  private boolean isAlternativeFinancialFlow() {
    try {
      try {
        Preferences.getPreferenceValue("FinancialManagement", true, null, null,
            OBContext.getOBContext().getUser(), null, null);
      } catch (PropertyNotFoundException e) {
        return false;
      }
    } catch (PropertyException e) {
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet RequisitionToOrder.";
  } // end of getServletInfo() method
}
