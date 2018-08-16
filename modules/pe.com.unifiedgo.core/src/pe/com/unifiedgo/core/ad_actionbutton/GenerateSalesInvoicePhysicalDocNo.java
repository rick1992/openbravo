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
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.service.db.DbUtility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.SCR_Utils;
import pe.com.unifiedgo.ebilling.data.BILLPhyDocSequence;

public class GenerateSalesInvoicePhysicalDocNo extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String strTabId = "";
  private String strWindowId = "";

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

      String strSalesInvoiceID = vars.getGlobalVariable("inpcInvoiceId",
          "GenerateSalesInvoicePhysicalDocNo|C_Invoice_ID", "");

      // No se utiliza aqui porque por default genera Nro Fisico no-electronico
      String strIsElectronicInv = vars.getGlobalVariable("inpIsElectronicInv",
          "GenerateSalesInvoicePhysicalDocNo|IsElectronicInv", "");

      String strPhysicalDocNoFV = "", mWarehouseId = null, doctypereference = null,
          specialdoctype = null, invoiceorgid = null;
      BILLPhyDocSequence phyDocSeq = null;
      OBError myError;
      try {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
        User user = OBDal.getInstance().get(User.class, vars.getUser());

        // getting related order warehouse id for the invoice
        if (invoice.getSalesOrder() != null) {
          mWarehouseId = invoice.getSalesOrder().getWarehouse().getId();
        } else {
          List<InvoiceLine> invlines = invoice.getInvoiceLineList();
          for (int i = 0; i < invlines.size(); i++) {
            if (invlines.get(i).getSalesOrderLine() != null) {
              mWarehouseId = invlines.get(i).getSalesOrderLine().getSalesOrder().getWarehouse()
                  .getId();
              break;
            }
          }
        }

        if (invoice.getScoDoctyperef() != null) {
          doctypereference = invoice.getScoDoctyperef().getScoSpecialdoctype();
        }

        strPhysicalDocNoFV = SCR_Utils.getInvPhysicalDocumentNo(this, vars, user.getId(),
            invoice.getClient().getId(), invoice.getOrganization().getId(), mWarehouseId,
            invoice.getTransactionDocument().getScoSpecialdoctype());

        phyDocSeq = SCR_Utils.getElectronicInvPhysicalSerie(this, vars, user.getId(),
            invoice.getClient().getId(), invoice.getOrganization().getId(), mWarehouseId,
            invoice.getTransactionDocument().getScoSpecialdoctype(), doctypereference, null);

        specialdoctype = invoice.getTransactionDocument().getScoSpecialdoctype();
        invoiceorgid = invoice.getOrganization().getId();

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

      printPageDataSheet(response, vars, strWindowId, strTabId, strSalesInvoiceID,
          strPhysicalDocNoFV, phyDocSeq, doctypereference, specialdoctype, invoiceorgid);

    } else if (vars.commandIn("SAVE")) {
      String strSalesInvoiceID = vars.getRequiredStringParameter("inpcInvoiceId");
      String strPhysicalDocNoFV = vars.getStringParameter("inpPhysicalDocNoFV");
      String strIsElectronicInv = vars.getStringParameter("inpIsElectronicInv");

      OBError myError;
      try {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);

        String physicalDocNoSerieId = null;
        if ("Y".equals(strIsElectronicInv)) {
          physicalDocNoSerieId = vars.getGlobalVariable("inpsecEPhyDocNoId", "");

          invoice.setBillIsebill(true);
        } else {
          invoice.setBillIsebill(false);
        }
        invoice.setScrPhysicalDocumentno(strPhysicalDocNoFV);

        OBDal.getInstance().save(invoice);
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
      String strSalesInvoiceID = vars.getGlobalVariable("inpcInvoiceId", "");
      String strIsElectronicInv = vars.getGlobalVariable("inpIsElectronicInv", "");

      String physicalDocNoSerieId = null;
      if ("Y".equals(strIsElectronicInv)) {
        physicalDocNoSerieId = vars.getGlobalVariable("inpsecEPhyDocNoId", "");
      }

      refreshPhysicalDocNo(response, vars, strSalesInvoiceID, strIsElectronicInv,
          physicalDocNoSerieId);
    }
  }

  private void refreshPhysicalDocNo(HttpServletResponse response, VariablesSecureApp vars,
      String strSalesInvoiceID, String strIsElectronicInv, String physicalDocNoSerieId)
      throws IOException, ServletException {
    String strPhysicalDocNoFV = null, mWarehouseId = null;
    try {
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      // getting related order warehouse id for the invoice
      if (invoice.getSalesOrder() != null) {
        mWarehouseId = invoice.getSalesOrder().getWarehouse().getId();
      } else {
        List<InvoiceLine> invlines = invoice.getInvoiceLineList();
        for (int i = 0; i < invlines.size(); i++) {
          if (invlines.get(i).getSalesOrderLine() != null) {
            mWarehouseId = invlines.get(i).getSalesOrderLine().getSalesOrder().getWarehouse()
                .getId();
            break;
          }
        }
      }

      // is electronic invoice
      if ("Y".equals(strIsElectronicInv)) {
        String doctypereference = null;
        if (invoice.getScoDoctyperef() != null) {
          doctypereference = invoice.getScoDoctyperef().getScoSpecialdoctype();
        }

        BILLPhyDocSequence phyDocSeq = SCR_Utils.getElectronicInvPhysicalSerie(this, vars,
            user.getId(), invoice.getClient().getId(), invoice.getOrganization().getId(),
            mWarehouseId, invoice.getTransactionDocument().getScoSpecialdoctype(), doctypereference,
            physicalDocNoSerieId);
        if (phyDocSeq != null) {
          strPhysicalDocNoFV = SCR_Utils.getAutoElectronicPhyDocNo(phyDocSeq.getPrefix(),
              phyDocSeq.getNextAssignedNumber());
        }

      } else {
        strPhysicalDocNoFV = SCR_Utils.getInvPhysicalDocumentNo(this, vars, user.getId(),
            invoice.getClient().getId(), invoice.getOrganization().getId(), mWarehouseId,
            invoice.getTransactionDocument().getScoSpecialdoctype());
      }

    } catch (final Exception e) {
      e.printStackTrace();
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
      String strWindowId, String strTabId, String strSalesInvoiceID, String strPhysicalDocNoFV,
      BILLPhyDocSequence ePhyDocSeq, String strSpecialDoctypeRefFromInv,
      String strInvSpecialDocType, String strInvOrgId) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/core/ad_actionbutton/GenerateSalesInvoicePhysicalDocNo")
        .createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("key", strSalesInvoiceID);

    xmlDocument.setParameter("physicalDocNoFV", strPhysicalDocNoFV);

    GenerateSalesInvoicePhysicalDocNoData[] ePhyDocNoSerieArray;
    try {
      ePhyDocNoSerieArray = GenerateSalesInvoicePhysicalDocNoData.select_ePhyDocNoSerieByUser(this,
          vars.getUser(), strInvSpecialDocType, strSpecialDoctypeRefFromInv, strInvOrgId);
      xmlDocument.setData("report_eSecPhyDocNo_Id", "liststructure", ePhyDocNoSerieArray);
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (ePhyDocSeq != null) {
      xmlDocument.setParameter("secEPhyDocNoId", ePhyDocSeq.getId());
    } else {
      if (ePhyDocNoSerieArray != null && ePhyDocNoSerieArray.length > 0) {
        xmlDocument.setParameter("secEPhyDocNoId", ePhyDocNoSerieArray[0].id);
      } else {
        xmlDocument.setParameter("secEPhyDocNoId", "");
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}