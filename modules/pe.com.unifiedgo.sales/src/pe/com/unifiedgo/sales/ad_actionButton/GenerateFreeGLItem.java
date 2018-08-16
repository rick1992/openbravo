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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DbUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class GenerateFreeGLItem extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String strTabId = "";
  private String strWindowId = "";

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      // Enumeration<String> params = vars.getParameterNames(); while (params.hasMoreElements())
      // { System.out.println(params.nextElement()); }

      strWindowId = vars.getStringParameter("inpwindowId");
      strTabId = vars.getStringParameter("inpTabId");
      String strOrgId = vars.getStringParameter("inpadOrgId");

      String strSalesInvoiceID = vars.getStringParameter("inpcInvoiceId");

      Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
      String action = invoice.getSsaGenfreeglitem();

      printPageDataSheet(response, vars, strWindowId, strTabId, strOrgId, strSalesInvoiceID, action);

    } else if (vars.commandIn("SAVE")) {
      String strSalesInvoiceID = vars.getStringParameter("inpcInvoiceId");
      // String strCostCenterId = vars.getStringParameter("inpCCostcenterId");
      OBError myError;
      try {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);

        if (invoice.getGrandTotalAmount().compareTo(BigDecimal.ZERO) == 0)
          throw new Exception("SSA_InvforFreeZeroTotalAmt");
        if (invoice.getGrandTotalAmount().compareTo(BigDecimal.ZERO) < 0)
          throw new Exception("SSA_InvforFreeNegTotalAmt");

        generateFreeGLItemInvLine(strSalesInvoiceID, invoice.getGrandTotalAmount());

        invoice.setSsaGenfreeglitem("SSA_UNDOFREEGLITEM");
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();

        OBError message = new OBError();
        message.setType("Success");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@Success@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

      } catch (Exception e) {
        System.out.println("Un error:" + e.getMessage());
        e.printStackTrace();
        log4j.warn("Rollback in transaction");

        String message;
        Throwable exx = DbUtility.getUnderlyingSQLException(e);
        message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
        if (message.contains("@")) {
          message = OBMessageUtils.parseTranslation(message);
        }
        message = Utility.messageBD(this, message, vars.getLanguage());

        log4j.error(message, e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(message);
        vars.setMessage(strTabId, myError);
        printPageClosePopUpAndRefreshParent(response, vars);

        OBDal.getInstance().rollbackAndClose();
      }

      String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;
      printPageClosePopUp(response, vars, strWindowPath);

    } else if (vars.commandIn("SAVE_UNDO")) {
      String strSalesInvoiceID = vars.getStringParameter("inpcInvoiceId");
      OBError myError;
      try {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);

        GenerateFreeGLItemData.removeFreeGLItemLine(this, invoice.getClient().getId(),
            invoice.getId());
        GenerateFreeGLItemData.updateInvoiceForFree(this, invoice.getId());

        invoice.setSsaGenfreeglitem("SSA_GENFREEGLITEM");
        invoice.setScoIsforfree(false);
        OBDal.getInstance().save(invoice);
        OBDal.getInstance().flush();

        OBError message = new OBError();
        message.setType("Success");
        message.setTitle(Utility.parseTranslation(this, vars, vars.getLanguage(), "@Success@"));
        vars.setMessage(strTabId, message);
        printPageClosePopUpAndRefreshParent(response, vars);

      } catch (Exception e) {
        System.out.println("Un error:" + e.getMessage());
        e.printStackTrace();
        log4j.warn("Rollback in transaction");

        String message;
        Throwable exx = DbUtility.getUnderlyingSQLException(e);
        message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
        if (message.contains("@")) {
          message = OBMessageUtils.parseTranslation(message);
        }
        message = Utility.messageBD(this, message, vars.getLanguage());

        log4j.error(message, e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(message);
        vars.setMessage(strTabId, myError);
        printPageClosePopUpAndRefreshParent(response, vars);

        OBDal.getInstance().rollbackAndClose();
      }

    }
  }

  private InvoiceLine generateFreeGLItemInvLine(String Invoice_id, BigDecimal freeGLItemAmount)
      throws Exception, SQLException {
    Invoice ObjInvoice = OBDal.getInstance().get(Invoice.class, Invoice_id);
    Long lineNo = ObjInvoice.getInvoiceLineList().size() * 10 + 10L;

    if (ObjInvoice.getOrganization().getSsaInvforfreecostcenter() == null) {
      throw new Exception("SCO_InvLineNeedsCostCenter");
    }

    Query q2;
    q2 = OBDal
        .getInstance()
        .getSession()
        .createSQLQuery(
            "SELECT c_tax_id FROM c_tax WHERE em_sco_specialtax='SCOEXEMPT' AND ad_client_id='"
                + ObjInvoice.getClient().getId() + "' LIMIT 1");
    String C_Tax_ID = (String) q2.uniqueResult();
    TaxRate tax = OBDal.getInstance().get(TaxRate.class, C_Tax_ID);

    q2 = OBDal
        .getInstance()
        .getSession()
        .createSQLQuery(
            "SELECT c_glitem_id FROM c_glitem WHERE em_sco_specialglitem='SCOFREEIGV' AND ad_client_id='"
                + ObjInvoice.getClient().getId() + "' LIMIT 1");
    String C_GLItem_ID = (String) q2.uniqueResult();
    GLItem glitem = OBDal.getInstance().get(GLItem.class, C_GLItem_ID);

    InvoiceLine newInvoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
    newInvoiceLine.setOrganization(ObjInvoice.getOrganization());
    newInvoiceLine.setClient(ObjInvoice.getClient());
    newInvoiceLine.setInvoice(ObjInvoice);
    newInvoiceLine.setUpdated(new Date());
    newInvoiceLine.setLineNo(lineNo);
    newInvoiceLine.setFinancialInvoiceLine(true);
    newInvoiceLine.setAccount(glitem);
    newInvoiceLine.setInvoicedQuantity(BigDecimal.ONE);
    newInvoiceLine.setListPrice(BigDecimal.ZERO);
    newInvoiceLine.setUnitPrice(freeGLItemAmount.negate());
    newInvoiceLine.setStandardPrice(freeGLItemAmount.negate());
    newInvoiceLine.setPriceLimit(BigDecimal.ZERO);
    newInvoiceLine.setLineNetAmount(freeGLItemAmount.negate());
    newInvoiceLine.setChargeAmount(BigDecimal.ZERO);
    newInvoiceLine.setUOM(OBDal.getInstance().get(UOM.class, "100"));
    newInvoiceLine.setTaxAmount(BigDecimal.ZERO);
    newInvoiceLine.setDescription("");
    newInvoiceLine.setTax(tax);
    newInvoiceLine.setScrSpecialtax(tax.getScoSpecialtax());
    // newInvoiceLine.setPriceAdjustment(ObjInvoiceLine.getPriceAdjustment());
    // newInvoiceLine.setExcludeforwithholding(ObjInvoiceLine.isExcludeforwithholding());
    // newInvoiceLine.setEditLineAmount(ObjInvoiceLine.isEditLineAmount());
    newInvoiceLine.setTaxableAmount(freeGLItemAmount.negate());
    newInvoiceLine.setSsaTaxpriceactual(freeGLItemAmount.negate());
    newInvoiceLine.setGrossAmount(BigDecimal.ZERO);
    newInvoiceLine.setGrossUnitPrice(BigDecimal.ZERO);
    newInvoiceLine.setBusinessPartner(ObjInvoice.getBusinessPartner());
    newInvoiceLine.setGrossListPrice(BigDecimal.ZERO);
    // newInvoiceLine.setDeferred(ObjInvoiceLine.isDeferred());
    // newInvoiceLine.setExplode(ObjInvoiceLine.isExplode());
    // newInvoiceLine.setScoProrrateo(ObjInvoiceLine.isScoProrrateo());
    // newInvoiceLine.setSCOPerceptionTaxed(ObjInvoiceLine.isSCOPerceptionTaxed());
    // newInvoiceLine.setSCODetractionTaxed(ObjInvoiceLine.isSCODetractionTaxed());
    // newInvoiceLine.setScoIsdeferredonreceipt(ObjInvoiceLine.isScoIsdeferredonreceipt());
    newInvoiceLine.setSsaIsprepaymentInline(false);
    newInvoiceLine.setCostcenter(ObjInvoice.getOrganization().getSsaInvforfreecostcenter());
    OBDal.getInstance().save(newInvoiceLine);

    return newInvoiceLine;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strOrgId, String strInvoiceID,
      String statusGenFreeGLItem) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/sales/ad_actionButton/GenerateFreeGLItem").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("statusGenFreeGLItem", statusGenFreeGLItem);
    xmlDocument.setParameter("adOrgId", strOrgId);
    xmlDocument.setParameter("cInvoiceId", strInvoiceID);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
