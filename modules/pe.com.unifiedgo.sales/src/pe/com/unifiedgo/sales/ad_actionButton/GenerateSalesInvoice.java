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
package pe.com.unifiedgo.sales.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DbUtility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.ProcessOrderHook;
import pe.com.unifiedgo.core.SCR_Utils;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class GenerateSalesInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String strTabId = "";
  private String strWindowId = "";

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

      strWindowId = vars.getStringParameter("inpwindowId");
      strTabId = vars.getStringParameter("inpTabId");
      String strDate = vars.getGlobalVariable("inpDate", "GenerateSalesInvoice|Date",
          SREDateTimeData.today(this));
      String strSalesOrderID = vars.getGlobalVariable("inpcOrderId",
          "GenerateSalesInvoice|C_Order_ID", "");
      String strIsElectronicInv = vars.getGlobalVariable("inpIsElectronicInv",
          "GenerateSalesInvoice|IsElectronicInv", "");

      String strPhysicalDocNoFV = "", strSpecialDocType = "";
      OBError myError;
      try {
        Order order = OBDal.getInstance().get(Order.class, strSalesOrderID);
        User user = OBDal.getInstance().get(User.class, vars.getUser());

        strSpecialDocType = "SCOARINVOICE";
        if ("boletadeventa".compareTo(order.getSsaComboItem().getSearchKey()) == 0
            || "muestraboleta".compareTo(order.getSsaComboItem().getSearchKey()) == 0) {
          strSpecialDocType = "SCOARTICKET";
        }

        strPhysicalDocNoFV = SCR_Utils.getInvPhysicalDocumentNo(this, vars, user.getId(),
            order.getClient().getId(), order.getOrganization().getId(),
            order.getWarehouse().getId(), strSpecialDocType);

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
          strDate);

    } else if (vars.commandIn("SAVE")) {
      String strSalesOrderID = vars.getRequiredStringParameter("inpcOrderId");
      String strDate = vars.getStringParameter("inpDate");
      String strIsElectronicInv = vars.getStringParameter("inpIsElectronicInv");

      Order order = OBDal.getInstance().get(Order.class, strSalesOrderID);
      Date date = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDate, "dd-MM-yyyy");

      String strPhysicalDocNoFV = vars.getRequiredStringParameter("inpPhysicalDocNoFV");

      String strSalesInvoiceID = null;
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
        if ("Y".equals(strIsElectronicInv))
          invoice.setBillIsebill(true);
        else
          invoice.setBillIsebill(false);

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

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strSalesOrderId, String strPhysicalDocNoFV,
      String strDate) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/sales/ad_actionButton/GenerateSalesInvoice")
        .createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("key", strSalesOrderId);

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
        process = OBDal.getInstance().get(Process.class, "912D2BD6F93C420DBFD2DF9ADFD67B78");
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

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}