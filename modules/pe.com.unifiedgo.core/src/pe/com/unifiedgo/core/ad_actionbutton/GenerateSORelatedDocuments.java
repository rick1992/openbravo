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
package pe.com.unifiedgo.core.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DbUtility;
import org.openbravo.warehouse.pickinglist.OBWPL_Utils;
import org.openbravo.warehouse.pickinglist.PickingList;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.ProcessOrderHook;
import pe.com.unifiedgo.core.SCR_Utils;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class GenerateSORelatedDocuments extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String strTabId = "";
  private String strWindowId = "";

  static public String NOT_PROCESSED = "NOT_PROCESSED";
  static public String PROCESSED = "PROCESSED";
  static public String PEND_PRINT_FV = "PEND_PRINT_FV";
  static public String PEND_PRINT_GR = "PEND_PRINT_GR";
  static public String PROCESSED_PRINTED = "PROCESSED_PRINTED";

  @Inject
  @Any
  private Instance<ProcessOrderHook> hooks;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      // Enumeration<String> params = vars.getParameterNames(); while
      // (params.hasMoreElements())
      // { System.out.println(params.nextElement()); }

      strWindowId = vars.getStringParameter("inpwindowId");
      strTabId = vars.getStringParameter("inpTabId");
      String strDate = vars.getGlobalVariable("inpDate", "GenerateSORelatedDocuments|Date",
          SREDateTimeData.today(this));
      String strSalesOrderID = vars.getGlobalVariable("inpcOrderId",
          "GenerateSORelatedDocuments|C_Order_ID", "");
      String strIsElectronicInv = vars.getGlobalVariable("inpIsElectronicInv",
          "GenerateSalesInvoice|IsElectronicInv", "");

      String strPhysicalDocNoFV = "", strPhysicalDocNoGR = "", strSpecialDocType = "";
      OBError myError;
      try {
        Order order = OBDal.getInstance().get(Order.class, strSalesOrderID);
        User user = OBDal.getInstance().get(User.class, vars.getUser());

        strSpecialDocType = "SCOAPCREDITMEMO";

      } catch (final Exception e) {
        OBDal.getInstance().rollbackAndClose();

        String message = DbUtility.getUnderlyingSQLException(e).getMessage();
        if (message.contains("@"))
          message = OBMessageUtils.parseTranslation(message);
        log4j.error(message, e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(message);
        vars.setMessage(strTabId, myError);
        printPageClosePopUpAndRefreshParent(response, vars);
      }

      printPageDataSheet(response, vars, strWindowId, strTabId, strSalesOrderID, strPhysicalDocNoFV,
          strPhysicalDocNoGR, strDate);

    } else if (vars.commandIn("SAVE")) {
      String strSalesOrderID = vars.getRequiredStringParameter("inpcOrderId");
      String strDate = vars.getStringParameter("inpDate");

      Order order = OBDal.getInstance().get(Order.class, strSalesOrderID);
      Date date = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDate, "dd-MM-yyyy");

      String strPhysicalDocNoFV = null;

      strPhysicalDocNoFV = "000-0000000";/// vars.getRequiredStringParameter("inpPhysicalDocNoFV");

      boolean isARInvoice = true;

      String strPhysicalDocNoGR = null;
      strPhysicalDocNoGR = "000-0000000";

      String strSalesInvoiceID = null, strSalesShipmentID = null;
      OBError myError;
      try {
        //
        // SALES INVOICE
        myError = generateSalesInvoice(order);
        if ("Error".equals(myError.getType()))
          throw new OBException(myError.getMessage());
        else if (!"Success".equals(myError.getType()))
          throw new OBException("@InvoiceCreateFailed@");

        // Success
        strSalesInvoiceID = myError.getMessage();
        if (strSalesInvoiceID == null || "".equals(strSalesInvoiceID))
          throw new OBException("@InvoiceCreateFailed@");

        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);

        invoice.setScrPhysicalDocumentno(strPhysicalDocNoFV);
        invoice.setAccountingDate(date);
        invoice.setInvoiceDate(date);

        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();

        myError = completeInvoice(invoice);
        if ("Error".equals(myError.getType()))
          throw new OBException(myError.getMessage());
        else if (!"Success".equals(myError.getType()))
          throw new OBException("@NotPossibleCompleteInvoice@");
        /*
         * // // SALES SHIPMENT // Success myError = generateSalesShipmentAndPicking(order, date);
         * if ("Error".equals(myError.getType())) throw new OBException(myError.getMessage()); else
         * if (!"Success".equals(myError.getType())) throw new OBException("@ShipmentError@");
         * 
         * // Success strSalesShipmentID = myError.getMessage(); if (strSalesShipmentID == null ||
         * "".equals(strSalesShipmentID)) throw new OBException("@ShipmentError@");
         * 
         * ShipmentInOut inout = OBDal.getInstance().get(ShipmentInOut.class, strSalesShipmentID);
         * if (!"P".equals(order.getDeliveryMethod()) && !"SCR_C".equals(order.getDeliveryMethod())
         * && !"SCR_VSGR".equals(order.getDeliveryMethod()) &&
         * !"SCR_P_NIC".equals(order.getDeliveryMethod()) &&
         * !"SCR_C_NIC".equals(order.getDeliveryMethod()) &&
         * !"SCR_VSGR_NIC".equals(order.getDeliveryMethod())) {
         * inout.setSCRPhysicalDocumentNo(strPhysicalDocNoGR);
         * 
         * } else { inout.setSCRPhysicalDocumentNo(strPhysicalDocNoGR); }
         * OBDal.getInstance().save(inout); OBDal.getInstance().flush();
         * 
         * myError = completeShipment(inout); if ("Error".equals(myError.getType())) throw new
         * OBException(myError.getMessage()); else if (!"Warning".equals(myError.getType()) &&
         * !"Success".equals(myError.getType())) throw new
         * OBException("@SSA_CannotCompleteShipment@");
         */
        //
        // UPDATING STATE
        OBDal.getInstance().getSession().refresh(order);
        OBDal.getInstance().save(order);
        OBDal.getInstance().flush();

        OBError message = new OBError();
        message.setType("Success");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@Success@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

      } catch (final OBException e) {
        OBDal.getInstance().rollbackAndClose();

        String resultMsg = OBMessageUtils.parseTranslation(e.getMessage());
        log4j.warn("Rollback in transaction");
        log4j.error(e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(resultMsg);
        vars.setMessage(strTabId, myError);
        printPageClosePopUpAndRefreshParent(response, vars);

      } catch (final Exception e) {
        OBDal.getInstance().rollbackAndClose();

        String message = DbUtility.getUnderlyingSQLException(e).getMessage();
        if (message.contains("@"))
          message = OBMessageUtils.parseTranslation(message);
        log4j.error(message, e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(message);
        vars.setMessage(strTabId, myError);
        printPageClosePopUpAndRefreshParent(response, vars);
      }
      // String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      // if (strWindowPath.equals(""))
      // strWindowPath = strDefaultServlet;
      // printPageClosePopUp(response, vars, strWindowPath);
    } else if (vars.commandIn("PHYSICALDOCNO")) {
      String strSalesOrderID = vars.getGlobalVariable("inpcOrderId", "");
      String strIsElectronicInv = vars.getGlobalVariable("inpIsElectronicInv", "");

      refreshPhysicalDocNo(response, vars, strSalesOrderID, strIsElectronicInv);
    }
  }

  private void refreshPhysicalDocNo(HttpServletResponse response, VariablesSecureApp vars,
      String strSalesOrderID, String strIsElectronicInv) throws IOException, ServletException {
    String strSpecialDocType = null, strPhysicalDocNoFV = null;
    OBError myError;
    try {
      Order order = OBDal.getInstance().get(Order.class, strSalesOrderID);
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      strSpecialDocType = "SCOARINVOICE";
      if ("boletadeventa".compareTo(order.getSsaComboItem().getSearchKey()) == 0
          || "muestraboleta".compareTo(order.getSsaComboItem().getSearchKey()) == 0) {
        strSpecialDocType = "SCOARTICKET";
      }

      // is electronic invoice
      if ("Y".equals(strIsElectronicInv)) {
        strPhysicalDocNoFV = SCR_Utils.getElectronicInvPhysicalDocumentNo(this, vars, user.getId(),
            order.getClient().getId(), order.getOrganization().getId(),
            order.getWarehouse().getId(), strSpecialDocType, null);
      } else {
        strPhysicalDocNoFV = SCR_Utils.getInvPhysicalDocumentNo(this, vars, user.getId(),
            order.getClient().getId(), order.getOrganization().getId(),
            order.getWarehouse().getId(), strSpecialDocType);
      }

    } catch (final Exception e) {
      System.out.println(DbUtility.getUnderlyingSQLException(e).getMessage());
    }

    JSONObject msg = new JSONObject();
    try {
      msg.put("PhysicalDocNo", strPhysicalDocNoFV);
      msg.put("isElectronicDocNo", ("Y".equals(strIsElectronicInv) ? "Y" : "N"));
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private String getDispatchPhysicalDocNoGR(boolean isARInvoice, String strPhysicalDocNoFV) {
    String strPhysicalDocNoDistpach = "000-0000000";
    if (isARInvoice) {
      strPhysicalDocNoDistpach = "F" + strPhysicalDocNoFV.substring(1);
    } else {
      strPhysicalDocNoDistpach = "B" + strPhysicalDocNoFV.substring(1);
    }
    return strPhysicalDocNoDistpach;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strSalesOrderId, String strPhysicalDocNoFV,
      String strPhysicalDocNoGR, String strDate) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/core/ad_actionbutton/GenerateSORelatedDocuments")
        .createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("key", strSalesOrderId);

    Order order = OBDal.getInstance().get(Order.class, strSalesOrderId);
    if ("P".equals(order.getDeliveryMethod()) || "SCR_C".equals(order.getDeliveryMethod())
        || "SCR_VSGR".equals(order.getDeliveryMethod())
        || "SCR_P_NIC".equals(order.getDeliveryMethod())
        || "SCR_C_NIC".equals(order.getDeliveryMethod())
        || "SCR_VSGR_NIC".equals(order.getDeliveryMethod())) {
      xmlDocument.setParameter("strHasShipment", "strHasShipment = \"N\";\r\n");
    } else {
      xmlDocument.setParameter("strHasShipment", "strHasShipment = \"Y\";\r\n");
    }

    xmlDocument.setParameter("physicalDocNoGR", strPhysicalDocNoGR);
    xmlDocument.setParameter("physicalDocNoFV", strPhysicalDocNoFV);

    xmlDocument.setParameter("date", strDate);
    xmlDocument.setParameter("datedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("datesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public OBError generateSalesInvoice(Order order) {
    OBError myMessage = null;
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());

    try {
      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "4934E75097744D5893184AA0C1897355");
      } finally {
        OBContext.restorePreviousMode();
      }
      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, order.getId(),
          parameters);

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);
      // final PInstanceProcessData[] pinstanceData =
      // PInstanceProcessData.select(this,
      // pinstance.getId());
      // myMessage = Utility.getProcessInstanceMessage(this, vars,
      // pinstanceData);
    } catch (Exception ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  public OBError completeInvoice(Invoice invoice) {

    System.out.println("Complete Invoice");

    OBError myMessage = null;
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());
    try {
      invoice.setDocumentAction("CO");
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "111");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, invoice.getId(),
          parameters);
      OBDal.getInstance().getSession().refresh(invoice);
      invoice.setAPRMProcessinvoice(invoice.getDocumentAction());
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);
    } catch (Exception ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  // reference: CreateActionHandler.java, function: private JSONObject
  // doCreate(JSONObject
  // jsonRequest)
  private OBError generateSalesShipmentAndPicking(Order order, Date date) throws Exception {
    OBError myMessage = null;
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());

    HashSet<String> notCompletedPL = new HashSet<String>();
    PickingList pickingList = null;
    String strSalesShipmentId, strDesc = "";
    try {

      if (!order.getWarehouse().isSwaNopicking()) {
        pickingList = generatePickingList(order, null, date);
        strDesc += OBMessageUtils.messageBD("OBWPL_docNo") + " " + order.getDocumentNo() + " "
            + OBMessageUtils.messageBD("OBWPL_BPartner") + order.getBusinessPartner().getName();
        if (strDesc.length() > 2000) {
          strDesc = strDesc.substring(0, 1997) + "...";
        }
        pickingList.setDescription(strDesc);
        OBDal.getInstance().save(pickingList);
      }

      strSalesShipmentId = generateSalesShipment(order, pickingList, notCompletedPL, date);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setMessage(strSalesShipmentId);

    } catch (Exception ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  // reference: CreateActionHandler.java, function: public PickingList
  // createPL(Order order, Locator
  // locator)
  public PickingList generatePickingList(Order order, Locator locator, Date date) {
    PickingList pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());

    if (date != null) {
      pickingList.setDocumentdate(date);
    } else {
      pickingList.setDocumentdate(new Date());
    }
    // Removing: FIXME-ENERO
    // pickingList.setDocumentdate(order.getOrderDate());
    boolean useOutbound = locator != null;
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype",
        useOutbound);
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    pickingList.setOutboundStorageBin(locator);
    pickingList.setSwaCOrder(order);
    pickingList.setSwaMWarehouse(order.getWarehouse());
    pickingList.setSwaDatepromised(order.getScheduledDeliveryDate());
    pickingList.setSWAUpdateReason(order.getSsaComboItem());
    OBDal.getInstance().save(pickingList);

    return pickingList;
  }

  // reference: CreateActionHandler.java, function: public String
  // processOrder(Order order,
  // PickingList pickingList, HashSet<String> notCompletedPL)
  public String generateSalesShipment(Order order, PickingList pickingList,
      HashSet<String> notCompletedPL, Date date) {
    long lineNo;

    int servicelinecount = 0;
    for (OrderLine orderLine : order.getOrderLineList()) {
      if (orderLine.getProduct() == null || !orderLine.getProduct().getProductType().equals("I")
          || (orderLine.getProduct().getProductType().equals("I")
              && !orderLine.getProduct().isStocked())) {
        servicelinecount++;
      }
    }

    if (servicelinecount >= order.getOrderLineList().size()) {
      throw new OBException(OBMessageUtils.messageBD("SSA_ServiceOrderNoShipment"));
    }

    if (order.isObwplIsinpickinglist()) {
      OBCriteria<PickingList> picking = OBDal.getInstance().createCriteria(PickingList.class);
      picking.add(Restrictions.eq(PickingList.PROPERTY_SWACORDER, order));
      picking.add(Restrictions.ne(PickingList.PROPERTY_PICKLISTSTATUS, "CA"));
      picking.setMaxResults(1);
      PickingList pickinglist = (PickingList) picking.uniqueResult();
      if (pickinglist != null)
        throw new OBException(OBMessageUtils.messageBD("swa_IsInPL") + order.getDocumentNo() + " - "
            + pickinglist.getDocumentNo());

      // TAMPOCO GUIA
      OBCriteria<ShipmentInOut> shipment = OBDal.getInstance().createCriteria(ShipmentInOut.class);
      shipment.add(Restrictions.eq(ShipmentInOut.PROPERTY_SALESORDER, order));
      List<String> lsStatus = new ArrayList<String>();
      lsStatus.add("DR");
      lsStatus.add("CO");
      shipment.add(Restrictions.in(ShipmentInOut.PROPERTY_DOCUMENTSTATUS, lsStatus));
      shipment.setMaxResults(1);
      ShipmentInOut shipmentList = (ShipmentInOut) shipment.uniqueResult();
      if (shipmentList != null)
        throw new OBException(OBMessageUtils.messageBD("swa_IsInPL") + order.getDocumentNo() + " - "
            + shipmentList.getDocumentNo());

    }

    if (order.isObwplReadypl()) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsExcluded") + order.getDocumentNo());
    }
    order.setObwplIsinpickinglist(true);

    String description = "";
    // Create In Out
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);

    if (order.getDocumentType().getScoSpecialdoctype().trim().equals("SSASAMPLEORDER"))
      shipment.setMovementType("ssa_S-");
    else
      shipment.setMovementType("C-");

    String specialDoc = "SCOMMSHIPMENT";
    String strMethod = order.getDeliveryMethod();
    String SpecialDocFromOrder = order.getDocumentType().getScoSpecialdoctype().trim();

    if (strMethod.compareTo("SCR_C") == 0 || strMethod.compareTo("P") == 0
        || strMethod.compareTo("SCR_VSGR") == 0 || strMethod.compareTo("SCR_P_NIC") == 0
        || strMethod.compareTo("SCR_C_NIC") == 0 || strMethod.compareTo("SCR_VSGR_NIC") == 0)
      specialDoc = "SWAMMDISPATCH";

    // MODIFY BY VAFASTER. GENERATE SHIPMENT WITH DOC TYPE. IN ORG,
    // DOCSPECIALTYPE, WAREHOUSE
    // ADD LINES
    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(order.getClient().getId());

    OBCriteria<DocumentType> shipDocType = OBDal.getInstance().createCriteria(DocumentType.class);
    shipDocType.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, specialDoc));
    shipDocType.add(Restrictions.in("organization.id",
        osp.getParentTree(order.getOrganization().getId(), true)));
    shipDocType.setMaxResults(1);
    DocumentType P_shipDocType = (DocumentType) shipDocType.uniqueResult();
    // END ADD LINES

    if ("".equals(P_shipDocType) || P_shipDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DocType_Shipment"));
    }

    shipment.setDocumentType(P_shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(P_shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner());
    shipment.setPartnerAddress(order.getPartnerAddress());
    shipment.setDeliveryLocation(order.getDeliveryLocation());
    shipment.setDeliveryMethod(order.getDeliveryMethod());
    shipment.setDeliveryTerms(order.getDeliveryTerms());

    if (date != null) {
      shipment.setMovementDate(date);
      shipment.setAccountingDate(date);
    } else {
      shipment.setMovementDate(new Date());
      shipment.setAccountingDate(new Date());
    }
    // Removing: FIXME-ENERO
    // shipment.setMovementDate(order.getOrderDate());
    // shipment.setAccountingDate(order.getOrderDate());
    shipment.setSalesOrder(order);
    shipment.setUserContact(order.getUserContact());
    shipment.setOrderReference(order.getOrderReference());
    shipment.setFreightCostRule(order.getFreightCostRule());
    shipment.setFreightAmount(order.getFreightAmount());
    shipment.setShippingCompany(order.getShippingCompany());
    shipment.setPriority(order.getPriority());
    shipment.setProject(order.getProject());
    shipment.setActivity(order.getActivity());
    shipment.setSalesCampaign(order.getSalesCampaign());
    shipment.setStDimension(order.getStDimension());
    shipment.setNdDimension(order.getNdDimension());
    shipment.setTrxOrganization(order.getTrxOrganization());

    /*
     * if (pickingList != null) description = "Picking List: " + pickingList.getDocumentNo(); else
     * description = "Sin Picking";
     */

    if (!"".equals(order.getDescription()) && order.getDescription() != null)
      description = description + order.getDescription();
    if (order.getDropShipPartner() != null)
      description = description + order.getDropShipPartner().getName();
    if (order.getDropShipLocation() != null)
      description = description + ": " + order.getDropShipLocation().getName();
    shipment.setDescription(description);

    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setProcessNow(false);
    shipment.setSWAUpdateReason(order.getSsaComboItem());

    OBDal.getInstance().save(shipment);

    lineNo = 10L;
    for (OrderLine orderLine : order.getOrderLineList()) {
      // Only consider pending to deliver lines of stocked item products.
      if (orderLine.getProduct() == null || !orderLine.getProduct().getProductType().equals("I")
          || (orderLine.getProduct().getProductType().equals("I")
              && !orderLine.getProduct().isStocked())
          || orderLine.getOrderedQuantity().signum() == 0
          || orderLine.getOrderedQuantity().compareTo(orderLine.getDeliveredQuantity()) <= 0
          || (orderLine.isObwplReadypl() != null && orderLine.isObwplReadypl())) {
        continue;
      }

      if (orderLine.getOrderedQuantity().signum() < 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      }
      // Removing: FIXME-ENERO(descomentar)
      if (orderLine.getProduct().isStocked()) {
        boolean createLine = processOrderLine(orderLine, shipment, pickingList, notCompletedPL,
            lineNo);
        if (createLine) {
          lineNo += 10L;
        }

      } else {
        // Create Shipment Line for non stocked products.
        BigDecimal qty = orderLine.getOrderedQuantity().subtract(orderLine.getDeliveredQuantity());
        // Removing: FIXME-ENERO reemplazar por funcion
        // createShipmentLine(...)
        createShipmentLine(orderLine.getAttributeSetValue(), null, qty, orderLine, shipment,
            pickingList, lineNo);
        // createShipmentLine_FIXME_ENERO_REPLACE(orderLine.getAttributeSetValue(),
        // null, qty,
        // orderLine, shipment, pickingList, lineNo);
        lineNo += 10L;
        OBDal.getInstance().flush();
      }
    }

    OBDal.getInstance().save(shipment);
    OBDal.getInstance().flush();

    OBDal.getInstance().refresh(shipment);
    if (shipment.getMaterialMgmtShipmentInOutLineList().size() == 0) {
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock"));
    }

    return shipment.getId();
  }

  // reference: CreateActionHandler.java, function: private void
  // createShipmentLine(AttributeSetInstance asi, Locator sb, BigDecimal qty,
  // OrderLine orderLine,
  // ShipmentInOut shipment, PickingList pickingList)
  private void createShipmentLine(AttributeSetInstance asi, Locator sb, BigDecimal qty,
      OrderLine orderLine, ShipmentInOut shipment, PickingList pickingList, long lineNo) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);
    line.setProject(orderLine.getProject());
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    line.setUOM(orderLine.getUOM());

    line.setAttributeSetValue(asi);
    line.setStorageBin(sb);
    line.setMovementQuantity(qty);
    line.setDescription(orderLine.getDescription());
    line.setExplode(orderLine.isExplode());

    if (orderLine.getBOMParent() != null) {
      OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance()
          .createCriteria(ShipmentInOutLine.class);
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, orderLine.getBOMParent()));
      obc.setMaxResults(1);
      line.setBOMParent((ShipmentInOutLine) obc.uniqueResult());
    }

    shipment.getMaterialMgmtShipmentInOutLineList().add(line);
    OBDal.getInstance().save(line);
    OBDal.getInstance().save(shipment);
  }

  // reference: CreateActionHandler.java, function: private void
  // processOrderLine(OrderLine
  // orderLine, ShipmentInOut shipment, PickingList pickingList,
  // HashSet<String> notCompletedPL)
  private boolean processOrderLine(OrderLine orderLine, ShipmentInOut shipment,
      PickingList pickingList, HashSet<String> notCompletedPL, long lineNo) {
    // Reserve Order Line
    boolean existsReservation = !orderLine.getMaterialMgmtReservationList().isEmpty(),
        iolineCreated = false;
    Reservation res = ReservationUtils.getReservationFromOrder(orderLine);
    if (res.getRESStatus().equals("DR")) {
      ReservationUtils.processReserve(res, "PR");
    } else if (res.getQuantity().compareTo(res.getReservedQty()) != 0) {
      ReservationUtils.reserveStockAuto(res);
    }
    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);

    List<ReservationStock> listResStock = new ArrayList<ReservationStock>();
    for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {
      if (!resStock.isAllocated()) {
        if (resStock.getStorageBin() != null) {
          OBCriteria<StorageDetail> critSD = OBDal.getInstance()
              .createCriteria(StorageDetail.class);
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_UOM, res.getUOM()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT, res.getProduct()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_STORAGEBIN, resStock.getStorageBin()));
          critSD.add(Restrictions.eq(StorageDetail.PROPERTY_ATTRIBUTESETVALUE,
              resStock.getAttributeSetValue()));
          critSD.add(Restrictions.isNull(StorageDetail.PROPERTY_ORDERUOM));
          critSD.setMaxResults(1);

          StorageDetail sd = (StorageDetail) critSD.uniqueResult();
          listResStock.add(
              ReservationUtils.reserveStockManual(res, sd, resStock.getQuantity().negate(), "N"));
          ReservationUtils.reserveStockManual(res, sd, resStock.getQuantity(), "Y");
        } else {
          listResStock.add(ReservationUtils.reserveStockManual(res, resStock.getSalesOrderLine(),
              resStock.getQuantity().negate(), "N"));
          /*
           * ReservationUtils.reserveStockManual(res, resStock.getSalesOrderLine(),
           * resStock.getQuantity(), "Y");
           */
          // System.out.println("resStock.getQuantity(): " +
          // resStock.getQuantity());
          ReservationUtils.reserveStockAutoMaxPermit(res, resStock.getQuantity().toString());
        }
      }
    }

    // refresh
    res = OBDal.getInstance().get(Reservation.class, res.getId());
    OBDal.getInstance().refresh(res);
    // OBDal.getInstance().refresh(orderLine);
    OBDal.getInstance().flush();

    /*
     * for (ReservationStock resStock : orderLine.getMaterialMgmtReservationStockList()) {
     * System.out.println( "**FF: "+resStock.getId()+" "+resStock.getQuantity()); }
     */

    if (!listResStock.isEmpty()) {
      for (ReservationStock resStock : listResStock) {
        if (resStock.getQuantity().equals(BigDecimal.ZERO) && !resStock.isAllocated()) {

          res.getMaterialMgmtReservationStockList().remove(resStock);
          // Agregado por PALK porque sino sale error
          orderLine.getMaterialMgmtReservationStockList().clear();

          OBDal.getInstance().remove(resStock);

        }
      }
    }

    OBDal.getInstance().flush();

    if (!existsReservation) {
      res.setOBWPLGeneratedByPickingList(true);
      OBDal.getInstance().save(res);
    }
    OBDal.getInstance().flush();

    if (res.getQuantity().compareTo(res.getReservedQty()) != 0 && pickingList != null) {
      notCompletedPL.add(pickingList.getDocumentNo());

      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock") + " "
          + res.getProduct().getSearchKey() + " - " + res.getProduct().getName());
    }

    for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {

      if (resStock.getStorageBin() == null) {
        // If pre-reserve is not yet reserve
        continue;

      }
      BigDecimal releasedQty = resStock.getReleased() == null ? BigDecimal.ZERO
          : resStock.getReleased();

      if (resStock.getQuantity().compareTo(releasedQty) <= 0) {
        // Ignore released stock
        continue;
      }

      BigDecimal quantity = resStock.getQuantity().subtract(releasedQty);
      // Create InOut line.
      // Removing: FIXME-ENERO descomentar llamada a createShipmentLine
      createShipmentLine(resStock.getAttributeSetValue(), resStock.getStorageBin(), quantity,
          orderLine, shipment, pickingList, lineNo);
      // createShipmentLine_FIXME_ENERO_REPLACE(resStock.getAttributeSetValue(),
      // resStock.getStorageBin(), quantity, orderLine, shipment,
      // pickingList, lineNo);
      iolineCreated = true;
    }

    OBDal.getInstance().flush();
    return iolineCreated;
  }

  public OBError completeShipment(ShipmentInOut inout) {
    OBError myMessage = null;
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());
    try {
      inout.setDocumentAction("CO");
      OBDal.getInstance().save(inout);
      OBDal.getInstance().flush();

      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "109");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, inout.getId(),
          parameters);
      OBDal.getInstance().getSession().refresh(inout);
      inout.setProcessGoodsJava(inout.getDocumentAction());
      OBDal.getInstance().save(inout);
      OBDal.getInstance().flush();

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);
    } catch (Exception ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}