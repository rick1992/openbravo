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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package pe.com.unifiedgo.core.ad_reports;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class ReportTransaction extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strFinFinancialTransactionId = vars.getGlobalVariable("inpfinFinaccTransactionId",
          "ReportTransaction|FinFinaccTransactionID", "");
      String strFinFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", "");
      String strLastFieldChanged = vars.getGlobalVariable("inpLastFieldChanged", "");
      String strDateTo = vars.getGlobalVariable("inpdateto", "");
      printPageDataPDFReport(request, response, vars, strFinFinancialTransactionId, OBDal
          .getInstance().get(FIN_FinaccTransaction.class, strFinFinancialAccountId).getAccount()
          .getId(), strDateTo);
    }
  }

  public void postTransaction(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strFinFinancialTransactionId = vars.getGlobalVariable("inpfinFinaccTransactionId",
          "ReportTransaction|FinFinaccTransactionID", "");
      String strFinFinancialAccountId = vars.getGlobalVariable("inpfinFinancialAccountId", "");
      String strDateTo = vars.getGlobalVariable("inpdateto", "");

      printPageDataPDFReport(request, response, vars, strFinFinancialTransactionId,
          strFinFinancialAccountId, strDateTo);
    }
  }

  private void printPageDataPDFReport(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strFinFinancialTransactionId,
      String strFinFinancialAccountId, String strDateTo) throws IOException, ServletException {
    log4j.debug("Output: Transaction PDF report");
    // FIN_Finacc_Transaction
    String strMainReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/Rpt_VoucherLibro_ugo.jrxml";
    FIN_FinaccTransaction transaction = null;
    OBContext.setAdminMode(true);
    try {
      transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          strFinFinancialTransactionId);
    } finally {
      OBContext.restorePreviousMode();
    }
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    final Locale locale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));
    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(vars.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
    dfs.setGroupingSeparator(vars.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(
        vars.getSessionValue("#AD_ReportNumberFormat"), dfs);

    // Parameters
    parameters.put("DOCUMENT_ID", strFinFinancialTransactionId);
    parameters.put("FINANCIAL_ACCOUNT_ID", strFinFinancialAccountId);

    parameters.put("DOCUMENT_NAME", Utility.messageBD(this, "Transaction", strLanguage));

    parameters.put("REPORT_TITLE", "VOUCHER LIBRO BANCOS");
    parameters.put("REPORT_SUBTITLE", "VOUCHER LIBRO BANCOS - LINEAS");

    parameters.put("ATTACH", globalParameters.strFTPDirectory);
    parameters.put("BASE_WEB", strReplaceWithFull);
    parameters.put("BASE_DESIGN", globalParameters.strBaseDesignPath);
    parameters.put("IS_IGNORE_PAGINATION", false);
    parameters.put("USER_CLIENT", Utility.getContext(this, vars, "#User_Client", ""));
    parameters.put("USER_ORG", Utility.getContext(this, vars, "#User_Org", ""));

    parameters.put("LANGUAGE", strLanguage);

    parameters.put("LOCALE", locale);

    parameters.put("NUMBERFORMAT", NumberFormat);

    parameters.put("SHOW_LOGO", "Y");
    parameters.put("SHOW_COMPANYDATA", "Y");
    parameters.put("HEADER_MARGIN", "");

    parameters.put("PAYMENT_ID", (transaction.getFinPayment() != null) ? transaction
        .getFinPayment().getId() : null);

    OBContext.setAdminMode(true);
    try {
      parameters.put("ISTAXINCLUDED", "");
    } finally {
      OBContext.restorePreviousMode();
    }

    // SUB-REPORTS
    // Sub-Report 1: Lines
    JasperReport subReportVourcherLibroLines;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/pe/com/unifiedgo/report/ad_reports/Rpt_VoucherLibro_Lines_ugo.jrxml");
      subReportVourcherLibroLines = JasperCompileManager.compileReport(jasperDesignLines);

    } catch (JRException e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("SUBREP_Rpt_VoucherLibro_Lines_ugo", subReportVourcherLibroLines);

    // Sub-Report 2: Docs
    JasperReport subReportVourcherLibroDocs;
    try {
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign
          + "/pe/com/unifiedgo/report/ad_reports/Rpt_VoucherLibro_Docs_ugo.jrxml");
      subReportVourcherLibroDocs = JasperCompileManager.compileReport(jasperDesignLines);

    } catch (JRException e) {
      throw new ServletException(e.getMessage());
    }
    parameters.put("SUBREP_Rpt_VoucherLibro_Docs_ugo", subReportVourcherLibroDocs);

    response.setContentType("text/html; charset=UTF-8");
    renderJR(vars, response, strMainReportName, "pdf", parameters, null, null);
  }
}