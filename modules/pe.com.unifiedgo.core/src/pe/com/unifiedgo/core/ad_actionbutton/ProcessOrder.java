/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package pe.com.unifiedgo.core.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonUtility;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DbUtility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.core.ProcessOrderHook;
import pe.com.unifiedgo.core.Result;

public class ProcessOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String PEN_CURRID = "308";

  @Inject
  @Any
  private static Instance<ProcessOrderHook> hooks;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessOrder|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessOrder|Tab_ID",
          IsIDFilter.instance);

      final String strC_Order_ID = vars.getGlobalVariable("inpcOrderId", strWindowId
          + "|C_Order_ID", "", IsIDFilter.instance);
      final String strdocaction = vars.getStringParameter("inpdocaction");
      final String strProcessing = vars.getStringParameter("inpprocessing", "Y");

      final String strOrg = vars.getRequestGlobalVariable("inpadOrgId", "ProcessOrder|Org_ID",
          IsIDFilter.instance);
      final String strClient = vars.getGlobalVariable("inpadClientId", "ProcessOrder|Client_ID",
          IsIDFilter.instance);

      final String strdocstatus = vars.getRequiredStringParameter("inpdocstatus");
      final String stradTableId = "259";
      final int accesslevel = 1;

      if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(),
          strTabId))
          || !(Utility.isElementInList(
              Utility.getContext(this, vars, "#User_Client", strWindowId, accesslevel), strClient) && Utility
              .isElementInList(
                  Utility.getContext(this, vars, "#User_Org", strWindowId, accesslevel), strOrg))) {
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars);
      } else {
        printPageDocAction(response, vars, strC_Order_ID, strdocaction, strProcessing,
            strdocstatus, stradTableId, strWindowId);
      }

    } else if (vars.commandIn("SAVE_BUTTONDocAction104")) {
      final String strWindowId = vars.getGlobalVariable("inpwindowId", "ProcessOrder|Window_ID",
          IsIDFilter.instance);
      final String strTabId = vars.getGlobalVariable("inpTabId", "ProcessOrder|Tab_ID",
          IsIDFilter.instance);
      final String strC_Order_ID = vars
          .getGlobalVariable("inpKey", strWindowId + "|C_Order_ID", "");
      final String strdocaction = vars.getStringParameter("inpdocaction");

      User user = OBDal.getInstance().get(User.class, vars.getUser());

      Order order;
      OBError myMessage = null;
      boolean doRollback = true;
      try {
        order = OBDal.getInstance().get(Order.class, strC_Order_ID);
        order.setDocumentAction(strdocaction);
        OBDal.getInstance().save(order);
        OBDal.getInstance().flush();

        OBError msg = null;

        // if (!PEN_CURRID.equals(order.getCurrency().getId())) {
        if (SCO_Utils.getExchangeRateUSDSales(this, order.getClient().getId(), order
            .getOrganization().getId(), order.getOrderDate()) == null) {
          throw new Exception("@SCR_NoConversionRateFor@" + order.getOrderDate());
        }
        // }

        
        //for ebilling check that all locations are valid
        if("SCR_P_NIC".equals(order.getDeliveryMethod())
            || "SCR_C_NIC".equals(order.getDeliveryMethod()) || "SCR_VSGR_NIC".equals(order.getDeliveryMethod())){
          Location loc = order.getPartnerAddress().getLocationAddress();
          if(loc.getAddressLine1() == null || loc.getRegion() == null || loc.getScrProvince() == null || loc.getCity() == null){
            throw new Exception("@BILL_InvalidDeliveryAddress@");
          }
          
          loc = order.getInvoiceAddress().getLocationAddress();
          if(loc.getAddressLine1() == null || loc.getRegion() == null || loc.getScrProvince() == null || loc.getCity() == null){
            throw new Exception("@BILL_InvalidInvoiceAddress@");
          }
          
          loc = order.getWarehouse().getLocationAddress();
          if(loc.getAddressLine1() == null || loc.getRegion() == null || loc.getScrProvince() == null || loc.getCity() == null){
            throw new Exception("@BILL_InvalidWarehouseAddress@");
          }          
        }

        
        if ("CO".equals(strdocaction)
            && (order.getSsaSpecialdoctype().equals("SSASTANDARDORDER")
                || order.getSsaSpecialdoctype().equals("SSAPOSORDER") || order
                .getSsaSpecialdoctype().equals("SSAWAREHOUSEORDER"))) {
          if ("SCONOTDEFINED".equals(order.getPaymentMethod().getScoSpecialmethod())) {
            throw new Exception("@SSA_NotDefinedPaymentMethod@");
          }
        }

        // Checking if Business Partner is blocked (only for documents
        // with credit)
        if (!"SCOINMEDIATETERM".equals(order.getPaymentTerms().getScoSpecialpayterm())) {
          if ("CO".equals(strdocaction) && order.getBusinessPartner().isCustomerBlocking()
              && order.getBusinessPartner().isSalesOrder() && !order.getDocumentType().isReturn()) {
            String detail_msg = "";
            if (order.getBusinessPartner().getScrMsgBlocking() != null
                && !"".equals(order.getBusinessPartner().getScrMsgBlocking().trim()))
              detail_msg = order.getBusinessPartner().getScrMsgBlocking();
            else
              detail_msg = OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked");

            msg = Utility.translateError(
                this,
                vars,
                vars.getLanguage(),
                Utility.messageBD(this, "@ThebusinessPartner@" + " "
                    + order.getBusinessPartner().getName() + " " + detail_msg, vars.getLanguage()));
            throw new Exception(msg.getMessage());
          }
        }

        // Checking if Sales Order has Lines
        if ("CO".equals(strdocaction)) {
          if (order.getOrderLineList().size() <= 0) {
            throw new Exception("@OrderWithoutLines@");
          }
        }

        // Check for the SUNAT Restriction 15
        // lines(m_product_id,priceactual) max
        String strSunatlinescount = getOrderSunatLinesCount(this, order.getId());
        int sunatlinescount = new Long(strSunatlinescount).intValue();
        if (sunatlinescount > 15) {
          throw new Exception("@SCO_SUNATDocMax15@");
        }

        // post(request, response, strTabId, strdocaction, order);
        myMessage = postOrder(this, vars, order, strdocaction);
        if (myMessage != null && "Error".equals(myMessage.getType())) {
          throw new OBException(myMessage.getMessage());
        }

      } catch (Exception ex) {
        String msgType = "Error";
        String message = "";
        if (myMessage != null) {
          msgType = (myMessage.getType() != null) ? myMessage.getType() : msgType;
          message = Utility.translateError(this, vars, vars.getLanguage(),
              Utility.messageBD(this, "@CODE=@" + ex.getMessage(), vars.getLanguage()))
              .getMessage();
        }

        if (doRollback) {
          OBDal.getInstance().rollbackAndClose();
          Throwable exx = DbUtility.getUnderlyingSQLException(ex);
          message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
          if (message.contains("@")) {
            message = OBMessageUtils.parseTranslation(message);
          }

          message = Utility.messageBD(this, message, vars.getLanguage());
          // remove mysql message
          int pos = message.toLowerCase().indexOf("where: sql statement");
          if (pos != -1) {
            message = message.substring(0, pos);
          }
        }

        OBError errmsg = new OBError();
        errmsg.setType(msgType);
        errmsg.setMessage(message);
        vars.setMessage(strTabId, errmsg);

        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals("")) {
          strWindowPath = strDefaultServlet;
        }
        printPageClosePopUp(response, vars, strWindowPath);
        return;

      }

      vars.setMessage(strTabId, myMessage);

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals("")) {
        strWindowPath = strDefaultServlet;
      }
      printPageClosePopUp(response, vars, strWindowPath);

    }
  }

  public void post(HttpServletRequest request, HttpServletResponse response, String strTabId,
      String strdocaction, Order order) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    OBError myMessage = null;
    OBError msg = null;

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "104");
    } finally {
      OBContext.restorePreviousMode();
    }

    Map<String, String> parameters = null;
    ProcessInstance pinstance = CallProcess.getInstance().call(process, order.getId(), parameters);

    OBDal.getInstance().getSession().refresh(order);
    order.setScrProcessorder(order.getDocumentAction());
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();

    OBContext.setAdminMode();
    try {
      // on error close popup
      if (pinstance.getResult() == 0L) {
        OBDal.getInstance().commitAndClose();
        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
            pinstance.getId());
        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);
        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        printPageClosePopUp(response, vars, strWindowPath);
        return;
      }

    } finally {
      OBContext.restorePreviousMode();
    }

    for (ProcessOrderHook hook : hooks) {
      msg = hook.postProcess(order, strdocaction);
      if (msg != null && "Error".equals(msg.getType())) {
        vars.setMessage(strTabId, msg);
        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        printPageClosePopUp(response, vars, strWindowPath);
        OBDal.getInstance().rollbackAndClose();
        return;
      }
    }

    OBDal.getInstance().commitAndClose();
    final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
        pinstance.getId());
    myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
    log4j.debug(myMessage.getMessage());
    vars.setMessage(strTabId, myMessage);
    OBContext.setAdminMode();
    try {
      if (!"CO".equals(strdocaction)) {
        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        printPageClosePopUp(response, vars, strWindowPath);
        return;
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    if ("CO".equals(strdocaction)) {
      // Need to refresh the order again from the db
      order = OBDal.getInstance().get(Order.class, order.getId());

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;
      printPageClosePopUp(response, vars, strWindowPath);

      vars.removeSessionValue("ProcessOrder|Window_ID");
      vars.removeSessionValue("ProcessOrder|Tab_ID");
      vars.removeSessionValue("ProcessOrder|Org_ID");
    }

  }

  public static OBError postOrder(ConnectionProvider conn, VariablesSecureApp vars, Order order,
      String strdocaction) {
    OBError myMessage = null;

    try {
      order.setDocumentAction(strdocaction);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();

      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "104");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, order.getId(),
          parameters);
      OBDal.getInstance().getSession().refresh(order);
      order.setScrProcessorder(order.getDocumentAction());
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);

    } catch (Exception ex) {
      myMessage = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  void printPageDocAction(HttpServletResponse response, VariablesSecureApp vars,
      String strC_Order_ID, String strdocaction, String strProcessing, String strdocstatus,
      String stradTableId, String strWindowId) throws IOException, ServletException {
    log4j.debug("Output: Button process 104");
    String[] discard = { "newDiscard" };
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/DocAction", discard).createXmlDocument();
    xmlDocument.setParameter("key", strC_Order_ID);
    xmlDocument.setParameter("processing", strProcessing);
    xmlDocument.setParameter("form", "ProcessOrder.html");
    xmlDocument.setParameter("window", strWindowId);
    xmlDocument.setParameter("css", vars.getTheme());
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("processId", "104");
    xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
    xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));

    OBError myMessage = vars.getMessage("104");
    vars.removeMessage("104");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("docstatus", strdocstatus);
    xmlDocument.setParameter("adTableId", stradTableId);
    xmlDocument.setParameter("processId", "104");
    xmlDocument.setParameter("processDescription", "Process Order");
    xmlDocument.setParameter("docaction", (strdocaction.equals("--") ? "CL" : strdocaction));
    FieldProvider[] dataDocAction = ActionButtonUtility.docAction(this, vars, strdocaction,
        "FF80818130217A35013021A672400035", strdocstatus, strProcessing, stradTableId);
    xmlDocument.setData("reportdocaction", "liststructure", dataDocAction);
    StringBuffer dact = new StringBuffer();
    if (dataDocAction != null) {
      dact.append("var arrDocAction = new Array(\n");
      for (int i = 0; i < dataDocAction.length; i++) {
        dact.append("new Array(\"" + dataDocAction[i].getField("id") + "\", \""
            + dataDocAction[i].getField("name") + "\", \""
            + dataDocAction[i].getField("description") + "\")\n");
        if (i < dataDocAction.length - 1)
          dact.append(",\n");
      }
      dact.append(");");
    } else
      dact.append("var arrDocAction = null");
    xmlDocument.setParameter("array", dact.toString());

    out.println(xmlDocument.print());
    out.close();

  }

  public void pre_post_order(HttpServletRequest request, HttpServletResponse response,
      String strC_Order_ID, String strTabId, String strdocaction, Order order, String operation)
      throws ServletException, IOException {
    // Automatic Reserves/De-Reserves stock for order
    String orderType = order.getSsaComboItem().getSearchKey();
    if ("CompraextraordinariaentreEmpresasOut".equals(orderType)) {
      return;
    }

    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "340ABFFFBDB049458B3F24524135D8F3");
    } finally {
      OBContext.restorePreviousMode();
    }

    Map<String, String> parameters = null;
    parameters = new HashMap<String, String>();
    parameters.put("operation", operation);
    ProcessInstance pinstance = CallProcess.getInstance().call(process, order.getId(), parameters);
    OBContext.setAdminMode();
    try {
      // on error close popup
      if (pinstance.getResult() == 0L) {
        OBDal.getInstance().commitAndClose();

        final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this,
            pinstance.getId());

        myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        log4j.debug(myMessage.getMessage());
        vars.setMessage(strTabId, myMessage);

        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        printPageClosePopUp(response, vars, strWindowPath);
        return;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static String getOrderSunatLinesCount(ConnectionProvider connectionProvider,
      String cOrderId) throws ServletException {
    return ProcessOrderData.getOrderSunatLinesCount(connectionProvider, cOrderId);
  }

  public static OBError reactivateSOPendEvaluation(ConnectionProvider conn,
      VariablesSecureApp vars, Order order) {
    OBError myMessage = null;
    try {
      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "0FC70948E5AA488C968A26F395D57ADE");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, order.getId(),
          parameters);

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);
    } catch (Exception ex) {
      myMessage = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }


  public String getServletInfo() {
    return "Servlet to Process Order";
  }

}