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
package pe.com.unifiedgo.warehouse.ad_reports;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class ReportInventoryTransaction extends HttpSecureAppServlet {
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
      /*
       * printPageDataPDFReport(request, response, vars, strFinFinancialTransactionId, OBDal
       * .getInstance().get(FIN_FinaccTransaction.class, strFinFinancialAccountId).getAccount()
       * .getId(), strDateTo);
       */
    }
  }

  public void postTransaction(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strmInventoryId = vars.getGlobalVariable("inpmInventoryId", "");
      printPageDataPDFReport(request, response, vars, strmInventoryId);
    }
  }

  private void printPageDataPDFReport(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strInventoryId) throws IOException, ServletException {

    log4j.debug("Output: Transaction PDF report");

    String strMainReportName = "@basedesign@/pe/com/unifiedgo/warehouse/ad_reports/ReporteInventariofisicoExcel.jrxml";

    FIN_FinaccTransaction transaction = null;
    OBContext.setAdminMode(true);

    ReportInventoryTransactionData[] header = null;
    ReportInventoryTransactionData[] data = null;

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    final Locale locale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));
    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(vars.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
    dfs.setGroupingSeparator(vars.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(
        vars.getSessionValue("#AD_ReportNumberFormat"), dfs);

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

    try {
      header = ReportInventoryTransactionData.selectHeaderDoc(this, strInventoryId, strLanguage);
    } catch (ServletException ex) {
      header = ReportInventoryTransactionData.setHeader();
    }

    try {
      data = ReportInventoryTransactionData.selectData(this, strInventoryId, strLanguage);
    } catch (ServletException ex) {
      data = ReportInventoryTransactionData.set();
    }

    parameters.put("ORGPADRE", header[0].orgpadre);
    parameters.put("ORGANIZACION", header[0].organizacion);
    parameters.put("DOCUMENTO", header[0].documento);
    parameters.put("NUMERODOC", header[0].numerodoc);
    parameters.put("FECMOVIMIENTO", StringToDate(header[0].fecmovimiento));
    parameters.put("NOMBRE", header[0].nombre);
    parameters.put("ALMACEN", header[0].almacen);
    parameters.put("DESCRIPCION", header[0].descripcion);

    OBContext.setAdminMode(true);
    try {
      parameters.put("ISTAXINCLUDED", "");
    } finally {
      OBContext.restorePreviousMode();
    }

    response.setContentType("text/html; charset=UTF-8");
    renderJR(vars, response, strMainReportName, "xls", parameters, data, null);
  }

  private Date StringToDate(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    Date date;
    try {
      if (!strDate.equals("")) {
        date = formatter.parse(strDate);
        return date;
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}