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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.service.db.DbUtility;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateFromRetainer extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String strBpartnerId = "";
  private String strClientId = "";
  private String strOrgId = "";
  private String strTabId = "";
  private String strWindowId = "";
  private String statusAnticipo = "";

  private String strSalesInvoiceID = "";

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {

      // Enumeration<String> params = vars.getParameterNames(); while (params.hasMoreElements())
      // { System.out.println(params.nextElement()); }

      try {
        strWindowId = vars.getStringParameter("inpwindowId");
        strTabId = vars.getStringParameter("inpTabId");
        strClientId = vars.getStringParameter("inpadClientId");
        strOrgId = vars.getStringParameter("inpadOrgId");
        strBpartnerId = vars.getStringParameter("inpcBpartnerId");
        strSalesInvoiceID = vars.getStringParameter("inpcInvoiceId");

        Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
        statusAnticipo = "DO";
        if (invoice.getScoUseprepayment().equals("SCO_UNDOPREPAY")) {
          statusAnticipo = "UNDO";
        }
        printPageDataSheet(response, vars, strWindowId, strTabId, strBpartnerId, strSalesInvoiceID);

      } catch (Exception e) {
        OBError myError;
        myError = new OBError();
        myError.setType("Warning");
        myError.setTitle(OBMessageUtils.messageBD("Warning"));
        myError.setMessage(OBMessageUtils.messageBD("Sim_pressButtonOk"));

        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
      }

    } else if (vars.commandIn("SAVE")) {
      String[] selectedLines = null;
      try {
        String strProductID = vars.getRequiredInStringParameter("inpRownumId", IsIDFilter.instance);
        strProductID = strProductID.replace("(", "").replace(")", "").replace("'", "");
        selectedLines = strProductID.split(",");

      } catch (Exception e) {
        selectedLines = null;

        OBError myError = new OBError();
        myError.setType("Error");
        myError.setMessage(OBMessageUtils.messageBD(this, "SWA_noselected", vars.getLanguage()));
        vars.setMessage(strTabId, myError);
      }

      boolean success = true;
      if (selectedLines != null) {
        try {
          String str_invoice_amtToApply, str_prepaymt_amtToApply;
          BigDecimal inv_amtToApply, prepay_amtToApply;
          for (int i = 0; i < selectedLines.length; i++) {
            InvoiceLine prepaymentLine = OBDal.getInstance().get(InvoiceLine.class,
                selectedLines[i].trim());

            str_invoice_amtToApply = vars
                .getRequiredNumericParameter("inpOrderQty" + selectedLines[i].trim());
            str_prepaymt_amtToApply = vars
                .getNumericParameter("inpOrderQtyConverted" + selectedLines[i].trim());

            inv_amtToApply = new BigDecimal(str_invoice_amtToApply);
            prepay_amtToApply = new BigDecimal(str_prepaymt_amtToApply);

            if (inv_amtToApply.compareTo(BigDecimal.ZERO) < 0) {
              success = false;
              OBError myError = new OBError();
              myError.setType("Error");
              myError.setTitle(OBMessageUtils.messageBD("Error"));
              myError.setMessage(OBMessageUtils.messageBD("sim_lowerthanzero"));
              vars.setMessage(strTabId, myError);
              break;
            }
            if (prepay_amtToApply.compareTo(prepaymentLine.getLineNetAmount()
                .subtract(prepaymentLine.getScoCreditused())) == 1) {
              success = false;
              OBError myError = new OBError();
              myError.setType("Error");
              myError.setTitle(OBMessageUtils.messageBD("Error"));
              myError.setMessage(OBMessageUtils.messageBD("sim_tousegreater"));
              vars.setMessage(strTabId, myError);
              break;
            }

            InvoiceLine newInvoiceLine = generatePrepayLinefromInvLine(vars, strSalesInvoiceID,
                prepaymentLine, inv_amtToApply, prepay_amtToApply, prepaymentLine);

            prepaymentLine
                .setScoCreditused(prepaymentLine.getScoCreditused().add(prepay_amtToApply));
            OBDal.getInstance().save(prepaymentLine);
          }

          if (success) {
            OBError myError = new OBError();
            myError.setType("Success");
            myError.setTitle(OBMessageUtils.messageBD("Success"));
            vars.setMessage(strTabId, myError);

            Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
            invoice.setScoUseprepayment("SCO_UNDOPREPAY");
            invoice.setSsaUseprepaymentinline(true);
            OBDal.getInstance().save(invoice);

          } else {
            OBDal.getInstance().rollbackAndClose();
          }

        } catch (Exception e) {
          success = false;
          System.out.println("error:" + e.getMessage());
          String message = DbUtility.getUnderlyingSQLException(e).getMessage();
          if (message.contains("@"))
            message = OBMessageUtils.parseTranslation(message);
          log4j.error(message, e);
          OBError myError = new OBError();
          myError.setType("Error");
          myError.setTitle(OBMessageUtils.messageBD("Error"));
          myError.setMessage(message);
          vars.setMessage(strTabId, myError);

          OBDal.getInstance().rollbackAndClose();
        }

      }
      printPageClosePopUpAndRefreshParent(response, vars);

    } else if (vars.commandIn("SAVE_UNDO")) {
      OBError myError;
      Invoice invoice = OBDal.getInstance().get(Invoice.class, strSalesInvoiceID);
      try {
        List<InvoiceLine> invoiceLines = invoice.getInvoiceLineList();
        for (int i = 0; i < invoiceLines.size(); i++) {
          if (invoiceLines.get(i).isSsaIsprepaymentInline()) {
            InvoiceLine prepaymentLine = invoiceLines.get(i).getScoInvoicelinePrepay();

            BigDecimal amount = prepaymentLine.getScoCreditused()
                .subtract(invoiceLines.get(i).getSsaPrepaymentAmtApplied());

            final int changeStateBeforeRemove = CreateFromRetainerData
                .changeStateBeforeRemoveAnticipo(this, invoiceLines.get(i).getId());
            final int remove = CreateFromRetainerData.removeAnticipo(this,
                invoiceLines.get(i).getId());

            prepaymentLine.setScoCreditused(amount);
            OBDal.getInstance().save(prepaymentLine);
            OBDal.getInstance().flush();
          }
        }
        invoice.setScoUseprepayment("SCO_USEPREPAY");
        invoice.setSsaUseprepaymentinline(false);
        OBDal.getInstance().save(invoice);

        myError = new OBError();
        myError.setType("Success");
        myError.setTitle(OBMessageUtils.messageBD("Success"));
        vars.setMessage(strTabId, myError);

      } catch (Exception e) {
        System.out.println("error:" + e.getMessage());
        String message = DbUtility.getUnderlyingSQLException(e).getMessage();
        if (message.contains("@"))
          message = OBMessageUtils.parseTranslation(message);
        log4j.error(message, e);
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(message);
        vars.setMessage(strTabId, myError);

        OBDal.getInstance().rollbackAndClose();
      }
    }
    printPageClosePopUpAndRefreshParent(response, vars);
  }

  private InvoiceLine generatePrepayLinefromInvLine(VariablesSecureApp vars, String Invoice_id,
      InvoiceLine ObjInvoiceLine, BigDecimal CreditUsed, BigDecimal PrepaymentCreditUsed,
      InvoiceLine ObjPrepaymentLine) {
    Invoice ObjInvoice = OBDal.getInstance().get(Invoice.class, Invoice_id);
    Long lineNo = ObjInvoice.getInvoiceLineList().size() * 10 + 10L;
    try {
      InvoiceLine newInvoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
      newInvoiceLine.setOrganization(ObjInvoiceLine.getOrganization());
      newInvoiceLine.setInvoice(ObjInvoice);
      newInvoiceLine.setUpdated(new Date());
      newInvoiceLine.setLineNo(lineNo);
      newInvoiceLine.setFinancialInvoiceLine(ObjInvoiceLine.isFinancialInvoiceLine());
      newInvoiceLine.setAccount(ObjInvoiceLine.getAccount());
      newInvoiceLine.setInvoicedQuantity(ObjInvoiceLine.getInvoicedQuantity());
      newInvoiceLine.setListPrice(ObjInvoiceLine.getListPrice());

      newInvoiceLine.setUnitPrice(CreditUsed.negate());
      newInvoiceLine.setPriceLimit(ObjInvoiceLine.getPriceLimit());
      newInvoiceLine.setLineNetAmount(CreditUsed.negate());
      newInvoiceLine.setChargeAmount(ObjInvoiceLine.getChargeAmount());
      newInvoiceLine.setUOM(ObjInvoiceLine.getUOM());
      newInvoiceLine.setTax(ObjInvoiceLine.getTax());
      newInvoiceLine.setTaxAmount(ObjInvoiceLine.getTaxAmount());
      newInvoiceLine.setDescription(ObjInvoiceLine.getDescription());
      newInvoiceLine.setPriceAdjustment(ObjInvoiceLine.getPriceAdjustment());
      newInvoiceLine.setExcludeforwithholding(ObjInvoiceLine.isExcludeforwithholding());
      newInvoiceLine.setEditLineAmount(ObjInvoiceLine.isEditLineAmount());
      newInvoiceLine.setTaxableAmount(ObjInvoiceLine.getTaxableAmount());
      newInvoiceLine.setGrossAmount(ObjInvoiceLine.getGrossAmount());
      newInvoiceLine.setGrossUnitPrice(ObjInvoiceLine.getGrossUnitPrice());
      newInvoiceLine.setBusinessPartner(ObjInvoiceLine.getBusinessPartner());
      newInvoiceLine.setGrossListPrice(ObjInvoiceLine.getGrossListPrice());
      newInvoiceLine.setDeferred(ObjInvoiceLine.isDeferred());
      newInvoiceLine.setExplode(ObjInvoiceLine.isExplode());
      newInvoiceLine.setScoProrrateo(ObjInvoiceLine.isScoProrrateo());
      newInvoiceLine.setSCOPerceptionTaxed(ObjInvoiceLine.isSCOPerceptionTaxed());
      newInvoiceLine.setSCODetractionTaxed(ObjInvoiceLine.isSCODetractionTaxed());
      newInvoiceLine.setScoIsdeferredonreceipt(ObjInvoiceLine.isScoIsdeferredonreceipt());

      newInvoiceLine.setScoInvoicelinePrepay(newInvoiceLine);
      newInvoiceLine.setSsaIsprepaymentInline(true);

      newInvoiceLine.setScoCreditused(BigDecimal.ZERO);
      newInvoiceLine.setScrSpecialtax(ObjInvoiceLine.getScrSpecialtax());
      newInvoiceLine.setScoInvoicelinePrepay(ObjInvoiceLine);

      newInvoiceLine.setSsaPrepaymentAmtApplied(PrepaymentCreditUsed);
      OBDal.getInstance().save(newInvoiceLine);
      return newInvoiceLine;

    } catch (Exception e) {
      // TODO: handle exception
      return null;
    }

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strBpartner, String strInvoiceID)
      throws IOException, ServletException {

    CreateFromRetainerData[] dataHeader = null;
    CreateFromRetainerData[] dataLines = null;
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/sales/ad_actionButton/CreateFromRetainer")
        .createXmlDocument();

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    DecimalFormat dfPrices = Utility.getFormat(vars, "euroEdition");
    dfPrices.setRoundingMode(RoundingMode.HALF_UP);

    OBError myMessage = null;
    myMessage = new OBError();
    int conversionRatePrecision = 2;
    try {
      conversionRatePrecision = FIN_Utility.getConversionRatePrecision(vars);

      dataHeader = CreateFromRetainerData.selectInvoiceHeader(this, strSalesInvoiceID);
      dataLines = CreateFromRetainerData.selectPrepaymentLines(this, strClientId, strOrgId,
          strBpartner, strInvoiceID, conversionRatePrecision, dfPrices, sdf);

    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("statusAnticipo", statusAnticipo);
    xmlDocument.setParameter("currencyPrecision", String.valueOf(conversionRatePrecision));

    xmlDocument.setData("structure3", dataHeader);
    xmlDocument.setData("structure4", dataLines);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void getInvoiceLineList() {
    // TODO Auto-generated method stub

  }

  private OBError copyLines(VariablesSecureApp vars, String strRownums, String strKey)
      throws IOException, ServletException {
    OBError myError = null;
    return myError;
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
