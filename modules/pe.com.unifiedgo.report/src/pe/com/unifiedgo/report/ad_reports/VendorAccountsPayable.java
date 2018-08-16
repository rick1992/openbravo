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
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class VendorAccountsPayable extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final int TotalCargoPEN = 0;
  private static final int TotalAbonoPEN = 1;
  private static final int TotalSaldoPEN = 2;

  private static final int TotalCargoUSD = 3;
  private static final int TotalAbonoUSD = 4;
  private static final int TotalSaldoUSD = 5;

  private static final int TotalCargoEUR = 6;
  private static final int TotalAbonoEUR = 7;
  private static final int TotalSaldoEUR = 8;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {

      String strDateTo = vars.getGlobalVariable("inpDateTo", "VendorAccountsPayable|DateTo",
          SREDateTimeData.lastDayofCurrentMonth(this));
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getGlobalVariable("inpOrgId", "VendorAccountsPayable|OrgId", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "VendorAccountsPayable|CB_PARTNER_ID", "");
      String strSoloPendientes = vars.getGlobalVariable("inpSoloPendientes",
          "VendorAccountsPayable|SoloPendientes", "Y");
      String strRecord = vars.getGlobalVariable("inpRecord", "VendorAccountsPayable|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "VendorAccountsPayable|Table", "");

      vars.removeSessionValue("VendorAccountsPayable|unpaidtovendorlines");

      printPageDataSheet(response, vars, null, null, strDateTo, strOrgId, strcBpartnetId, strTable,
          strRecord, strDateFrom, strSoloPendientes, null, null, null);

    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "VendorAccountsPayable|DateFrom");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "VendorAccountsPayable|CB_PARTNER_ID", "");
      String strTable = vars.getGlobalVariable("inpTable", "VendorAccountsPayable|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "VendorAccountsPayable|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("VendorAccountsPayable.initRecordNumber", "0");

      printPageDataSheet(response, vars, null, null, "", "", strcBpartnetId, strTable, strRecord,
          strDateFrom, "", null, null, null);

    } else if (vars.commandIn("GRIDLIST")) {

      String strDateTo = vars.getStringParameter("inpDateTo");
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strSoloPendientes = vars.getStringParameter("inpSoloPendientes");

      String strDocIdToShow = vars.getStringParameter("inpDocIdToShow");
      String strSpecialdoctype = vars.getStringParameter("inpSpecialdoctype");
      String strOperation = vars.getStringParameter("inpOperation");

      List<UnpaidToVendorLine> unpaidtovendorlines = (List<UnpaidToVendorLine>) vars
          .getSessionObject("VendorAccountsPayable|unpaidtovendorlines");

      List<BigDecimal> total = (List<BigDecimal>) vars
          .getSessionObject("VendorAccountsPayable|totalallunpaidtovendor");

      printGrid(response, vars, unpaidtovendorlines, total, strDateTo, strOrgId, strcBpartnetId, "",
          "", strDateFrom, strSoloPendientes, strDocIdToShow, strSpecialdoctype, strOperation);
    } else if (vars.commandIn("FIND")) {

      String strDateTo = vars.getStringParameter("inpDateTo");
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strSoloPendientes = vars.getStringParameter("inpSoloPendientes");

      vars.setSessionValue("VendorAccountsPayable.initRecordNumber", "0");

      Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
      Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
      boolean onlyPendings = "Y".equals(strSoloPendientes) ? true : false;

      List<UnpaidToVendorLine> unpaidtovendorlines = getUnpaidToVendorLine(strcBpartnetId, from, to,
          onlyPendings, strOrgId);
      vars.setSessionObject("VendorAccountsPayable|unpaidtovendorlines", unpaidtovendorlines);

      List<BigDecimal> total = getTotalAmounts(unpaidtovendorlines);
      vars.setSessionObject("VendorAccountsPayable|totalallunpaidtovendor", total);

      printPageDataSheet(response, vars, unpaidtovendorlines, total, strDateTo, strOrgId,
          strcBpartnetId, "", "", strDateFrom, strSoloPendientes, null, null, null);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String strDateTo = vars.getStringParameter("inpDateTo");
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      String strSoloPendientes = vars.getStringParameter("inpSoloPendientes");

      String strMostrarTodosClientes = vars.getStringParameter("inpMostrarTodosClientes");

      setHistoryCommand(request, "DEFAULT");

      printPagePDF(response, vars, strDateTo, strOrgId, strcBpartnetId, strTable, strRecord,
          strDateFrom, strSoloPendientes, strMostrarTodosClientes, null, null);

    } else
      pageError(response);
  }

  private FieldProvider[] set() throws ServletException {
    HashMap<String, String> empty = new HashMap<String, String>();

    empty.put("rownum", "");
    empty.put("specialdoctype", "");
    empty.put("invoiceId", "");
    empty.put("Fecha", "");
    empty.put("FecVenc", "");
    empty.put("TipoDcto", "");
    empty.put("Documento", "");
    empty.put("documentId", "");
    empty.put("specialdoctypeplus", "");
    empty.put("Banco", "");
    empty.put("NDias", "");
    empty.put("Moneda", "");
    empty.put("Cargo", "");
    empty.put("Abono", "");
    empty.put("Saldo", "");
    empty.put("Percepcion", "");
    empty.put("groupNumber", "");
    empty.put("tipoOp", "");
    empty.put("Abono", "");
    empty.put("docRecepcion", "");
    empty.put("estaFacturado", "");
    empty.put("recepcionDate", "");
    empty.put("paymentMethod", "");
    empty.put("daysTillDue", "");

    ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
    result.add(empty);
    return FieldProviderFactory.getFieldProviderArray(result);
  }

  private void printGrid(HttpServletResponse response, VariablesSecureApp vars,
      List<UnpaidToVendorLine> unpaidtovendorlines, List<BigDecimal> totalallunpaidtovendor,
      String strDateTo, String strOrgId, String strcBpartnetId, String strTable, String strRecord,
      String strDateFrom, String strSoloPendientes, String strDocIdToShow, String strSpecialdoctype,
      String strOperation) throws IOException, ServletException {

    String[] discard = { "discard" };
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/VendorAccountsPayableGrid", discard)
        .createXmlDocument();

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
    Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");

    FieldProvider[] detailData = getDetailData(strDocIdToShow, strSpecialdoctype, from, to);
    if (detailData == null || detailData.length == 0) {
      detailData = VendorAccountsPayableData.set("0");
    }

    xmlDocument.setData("structure", (detailData == null) ? set() : detailData);

    String documentRefDescription = "";
    if (strDocIdToShow != null) {
      Invoice inv = OBDal.getInstance().get(Invoice.class, strDocIdToShow);
      if (inv != null) {
        documentRefDescription = strOperation + " " + inv.getIdentifier();
      } else {
        SCOPrepayment prepay = OBDal.getInstance().get(SCOPrepayment.class, strDocIdToShow);
        if (prepay != null) {
          documentRefDescription = strOperation + " " + prepay.getIdentifier();
        } else {
          ScoRendicioncuentas rendCuentas = OBDal.getInstance().get(ScoRendicioncuentas.class,
              strDocIdToShow);
          if (rendCuentas != null) {
            documentRefDescription = strOperation + " " + rendCuentas.getIdentifier();
          }
        }
      }
    }

    JSONObject msg = new JSONObject();
    try {
      msg.put("DocumentRef", documentRefDescription);
      msg.put("detailBody", xmlDocument.print());
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  private List<UnpaidToVendorLine> getUnpaidToVendorLine(String strcBpartnetId, Date from, Date to,
      boolean onlyPendings, String strOrgId) throws ServletException {
    List<UnpaidToVendorLine> unpaidtovendor = UnpaidToVendor.getUnpaidToVendor(this, strcBpartnetId,
        from, to, onlyPendings, strOrgId);
    return unpaidtovendor;
  }

  private FieldProvider[] getFormattedData(List<UnpaidToVendorLine> unpaidtovendor) {
    FieldProvider[] data = null;

    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    int countRecord = 0;
    for (UnpaidToVendorLine unpaidToVendorLine : unpaidtovendor) {
      if (unpaidToVendorLine.groupNumber == -1)
        continue;

      countRecord++;

      HashMap<String, String> hashMap = new HashMap<String, String>();

      hashMap.put("Fecha", unpaidToVendorLine.invoicedDate);
      hashMap.put("FecVenc", unpaidToVendorLine.dueDate);
      hashMap.put("TipoDcto", unpaidToVendorLine.docType);
      hashMap.put("tipoOp", String.valueOf(unpaidToVendorLine.tipoOp));
      hashMap.put("Documento",
          unpaidToVendorLine.documentNumber != null ? unpaidToVendorLine.documentNumber : "--");
      hashMap.put("NDias", unpaidToVendorLine.paymentCondition);
      hashMap.put("Moneda", unpaidToVendorLine.currency);
      hashMap.put("Cargo", String.valueOf(unpaidToVendorLine.debit.doubleValue()));
      hashMap.put("Abono", String.valueOf(unpaidToVendorLine.credit.doubleValue()));
      hashMap.put("Saldo", String.valueOf(unpaidToVendorLine.balance.doubleValue()));
      hashMap.put("Percepcion", String.valueOf(unpaidToVendorLine.percepcion.doubleValue()));
      hashMap.put("rownum", String.valueOf(countRecord));
      hashMap.put("groupDocument", String.valueOf(unpaidToVendorLine.groupDocument));
      hashMap.put("specialdoctype", unpaidToVendorLine.specialdoctype);
      hashMap.put("specialdoctypeplus", unpaidToVendorLine.specialdoctypeplus);
      hashMap.put("documentId", unpaidToVendorLine.documentId);
      hashMap.put("docRecepcion", unpaidToVendorLine.docRecepcion);
      hashMap.put("recepcionDate", unpaidToVendorLine.recepcionDate);
      hashMap.put("orgName", unpaidToVendorLine.orgName);
      hashMap.put("daysTillDue",
          unpaidToVendorLine.daysTillDue != null
              ? (String.valueOf(unpaidToVendorLine.daysTillDue) + " días")
              : "");
      hashMap.put("paymentMethod", unpaidToVendorLine.paymentMethod);
      hashMapList.add(hashMap);

    }
    data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    return data;
  }

  private FieldProvider[] getDetailData(String strDocumentId, String specialDocType, Date from,
      Date to) {
    FieldProvider[] data = null;
    if (strDocumentId == null || "".equals(strDocumentId))
      return null;
    if (specialDocType == null || "".equals(specialDocType))
      return null;

    List<UnpaidToVendorLine> unpaidtovendor = UnpaidToVendor.getDetailUnpaidToVendor(strDocumentId,
        from, to);
    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    int countRecord = 0;
    for (UnpaidToVendorLine unpaidToVendorLine : unpaidtovendor) {
      if (unpaidToVendorLine.groupNumber == -1)
        continue;

      countRecord++;

      HashMap<String, String> hashMap = new HashMap<String, String>();

      hashMap.put("Fecha", unpaidToVendorLine.invoicedDate);
      hashMap.put("FecVenc", unpaidToVendorLine.dueDate);
      hashMap.put("TipoDcto", unpaidToVendorLine.docType);
      hashMap.put("tipoOp", String.valueOf(unpaidToVendorLine.tipoOp));
      hashMap.put("Documento",
          unpaidToVendorLine.documentNumber != null ? unpaidToVendorLine.documentNumber : "--");
      hashMap.put("Banco", unpaidToVendorLine.bankAccount);
      hashMap.put("NDias", unpaidToVendorLine.paymentCondition);
      hashMap.put("Moneda", unpaidToVendorLine.currency);
      hashMap.put("Cargo", String.valueOf(unpaidToVendorLine.debit.doubleValue()));
      hashMap.put("Abono", String.valueOf(unpaidToVendorLine.credit.doubleValue()));
      hashMap.put("Saldo", String.valueOf(unpaidToVendorLine.balance.doubleValue()));
      hashMap.put("Percepcion", String.valueOf(unpaidToVendorLine.percepcion.doubleValue()));
      hashMap.put("rownum", String.valueOf(countRecord));
      hashMap.put("groupNumber", String.valueOf(unpaidToVendorLine.groupNumber));
      hashMap.put("specialdoctypeplus", unpaidToVendorLine.specialdoctypeplus);
      hashMap.put("documentId", unpaidToVendorLine.documentId);
      hashMap.put("docRecepcion", unpaidToVendorLine.docRecepcion);
      hashMap.put("estaFacturado", unpaidToVendorLine.estaFacturado);
      hashMap.put("recepcionDate", unpaidToVendorLine.recepcionDate);
      hashMap.put("paymentMethod", unpaidToVendorLine.paymentMethod);
      hashMap.put("daysTillDue",
          unpaidToVendorLine.daysTillDue != null
              ? (String.valueOf(unpaidToVendorLine.daysTillDue) + " días")
              : "");
      hashMapList.add(hashMap);

    }
    data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    return data;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      List<UnpaidToVendorLine> unpaidtovendorlines, List<BigDecimal> totalallunpaidtovendor,
      String strDateTo, String strOrgId, String strcBpartnetId, String strTable, String strRecord,
      String strDateFrom, String strSoloPendientes, String strDocIdToShow, String strSpecialdoctype,
      String strOperation) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    FieldProvider[] data = null;
    XmlDocument xmlDocument = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "VendorAccountsPayable", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
    Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
    boolean onlyPendings = "Y".equals(strSoloPendientes) ? true : false;

    if (unpaidtovendorlines == null) {
      unpaidtovendorlines = getUnpaidToVendorLine(strcBpartnetId, from, to, onlyPendings, strOrgId);
    }
    data = getFormattedData(unpaidtovendorlines);
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };

      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/VendorAccountsPayable", discard)
          .createXmlDocument();
      data = VendorAccountsPayableData.set("0");

    } else {
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/VendorAccountsPayable")
          .createXmlDocument();
    }

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.VendorAccountsPayable");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "VendorAccountsPayable.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "VendorAccountsPayable.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("VendorAccountsPayable");
      vars.removeMessage("VendorAccountsPayable");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    xmlDocument.setParameter("soloPendientes", strSoloPendientes);

    Organization org;
    org = OBDal.getInstance().get(Organization.class, strOrgId);
    xmlDocument.setParameter("OrgId", strOrgId);
    xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

    xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
    xmlDocument.setParameter("paramBPartnerDescription", selectBpartner(this, strcBpartnetId));

    String documentRefDescription = "";
    if (strDocIdToShow != null) {
      Invoice inv = OBDal.getInstance().get(Invoice.class, strDocIdToShow);
      if (inv != null) {
        documentRefDescription = strOperation + " " + inv.getIdentifier();
      } else {
        SCOPrepayment prepay = OBDal.getInstance().get(SCOPrepayment.class, strDocIdToShow);
        if (prepay != null) {
          documentRefDescription = strOperation + " " + prepay.getIdentifier();
        } else {
          ScoRendicioncuentas rendCuentas = OBDal.getInstance().get(ScoRendicioncuentas.class,
              strDocIdToShow);
          if (rendCuentas != null) {
            documentRefDescription = strOperation + " " + rendCuentas.getIdentifier();
          }
        }
      }
    }
    xmlDocument.setParameter("DocumentRef", documentRefDescription);

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    if (totalallunpaidtovendor == null) {
      totalallunpaidtovendor = getTotalAmounts(unpaidtovendorlines);
    }
    xmlDocument.setParameter("TotalCargoPEN", df.format(totalallunpaidtovendor.get(TotalCargoPEN)));
    xmlDocument.setParameter("TotalAbonoPEN", df.format(totalallunpaidtovendor.get(TotalAbonoPEN)));
    xmlDocument.setParameter("TotalSaldoPEN", df.format(totalallunpaidtovendor.get(TotalSaldoPEN)));

    xmlDocument.setParameter("TotalCargoUSD", df.format(totalallunpaidtovendor.get(TotalCargoUSD)));
    xmlDocument.setParameter("TotalAbonoUSD", df.format(totalallunpaidtovendor.get(TotalAbonoUSD)));
    xmlDocument.setParameter("TotalSaldoUSD", df.format(totalallunpaidtovendor.get(TotalSaldoUSD)));

    xmlDocument.setParameter("TotalCargoEUR", df.format(totalallunpaidtovendor.get(TotalCargoEUR)));
    xmlDocument.setParameter("TotalAbonoEUR", df.format(totalallunpaidtovendor.get(TotalAbonoEUR)));
    xmlDocument.setParameter("TotalSaldoEUR", df.format(totalallunpaidtovendor.get(TotalSaldoEUR)));
    //

    vars.setSessionValue("VendorAccountsPayable|Record", strRecord);
    vars.setSessionValue("VendorAccountsPayable|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private List<BigDecimal> getTotalAmounts(List<UnpaidToVendorLine> unpaidtovendor) {
    List<BigDecimal> total = new ArrayList<BigDecimal>(9);

    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);

    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);

    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);
    total.add(BigDecimal.ZERO);

    for (UnpaidToVendorLine unpaidToVendorLine : unpaidtovendor) {
      if (unpaidToVendorLine.groupNumber == -1)
        continue;

      if ("PEN".equals(unpaidToVendorLine.currency)) {
        total.set(TotalCargoPEN, total.get(TotalCargoPEN).add(unpaidToVendorLine.debit));
        total.set(TotalAbonoPEN, total.get(TotalAbonoPEN).add(unpaidToVendorLine.credit));
        total.set(TotalSaldoPEN, total.get(TotalSaldoPEN).add(unpaidToVendorLine.balance));

      } else if ("USD".equals(unpaidToVendorLine.currency)) {
        total.set(TotalCargoUSD, total.get(TotalCargoUSD).add(unpaidToVendorLine.debit));
        total.set(TotalAbonoUSD, total.get(TotalAbonoUSD).add(unpaidToVendorLine.credit));
        total.set(TotalSaldoUSD, total.get(TotalSaldoUSD).add(unpaidToVendorLine.balance));

      } else if ("EUR".equals(unpaidToVendorLine.currency)) {
        total.set(TotalCargoEUR, total.get(TotalCargoEUR).add(unpaidToVendorLine.debit));
        total.set(TotalAbonoEUR, total.get(TotalAbonoEUR).add(unpaidToVendorLine.credit));
        total.set(TotalSaldoEUR, total.get(TotalSaldoEUR).add(unpaidToVendorLine.balance));
      }
    }

    return total;
  }

  public String selectBpartner(ConnectionProvider connectionProvider, String cBpartnerId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT C_BPARTNER.TAXID || ' - ' || C_BPARTNER.NAME AS NAME"
        + "      FROM C_BPARTNER" + "      WHERE C_BPARTNER.C_BPARTNER_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cBpartnerId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException(
          "@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

  // private VendorAccountsPayableData[]
  // notshow(VendorAccountsPayableData[] data,
  // VariablesSecureApp vars) {
  // for (int i = 0; i < data.length - 1; i++) {
  // if ((data[i].identifier.toString().equals(data[i +
  // 1].identifier.toString()))
  // && (data[i].dateacct.toString().equals(data[i + 1].dateacct.toString())))
  // {
  // data[i + 1].newstyle = "visibility: hidden";
  // }
  // }
  // return data;
  // }

  private void printPagePDF(HttpServletResponse response, VariablesSecureApp vars, String strDateTo,
      String strOrgId, String strcBpartnetId, String strTable, String strRecord, String strDateFrom,
      String strSoloPendientes, String strMostrarTodosClientes, String strSpecialdoctype,
      String strIsGeneral) throws IOException, ServletException {

    Date f = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
    Date s = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");

    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();

    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrgId);

    boolean onlyPendings = "Y".equals(strSoloPendientes) ? true : false;

    if (strcBpartnetId == null || strcBpartnetId.equalsIgnoreCase("")) {

      HashMap<String, UnpaidToVendorLineWithPartner> map = UnpaidToVendor.getUnpaidByOrgToDate(this,
          vars.getClient(), strOrgId, s, f, onlyPendings);

      for (Map.Entry<String, UnpaidToVendorLineWithPartner> entry : map.entrySet()) {
        List<UnpaidToVendorLine> unpaidtovendor = entry.getValue().unpaidtovendors;
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class,
            entry.getValue().cBpartnerId);
        if (unpaidtovendor.size() == 0)
          continue;

        List<BigDecimal> totalallunpaidtovendor = getTotalAmounts(unpaidtovendor);

        BigDecimal totalcargopen = totalallunpaidtovendor.get(TotalCargoPEN);
        BigDecimal totalabonopen = totalallunpaidtovendor.get(TotalAbonoPEN);
        BigDecimal totalsaldopen = totalallunpaidtovendor.get(TotalSaldoPEN);

        BigDecimal totalcargousd = totalallunpaidtovendor.get(TotalCargoUSD);
        BigDecimal totalabonousd = totalallunpaidtovendor.get(TotalAbonoUSD);
        BigDecimal totalsaldousd = totalallunpaidtovendor.get(TotalSaldoUSD);

        BigDecimal totalcargoeur = totalallunpaidtovendor.get(TotalCargoEUR);
        BigDecimal totalabonoeur = totalallunpaidtovendor.get(TotalAbonoEUR);
        BigDecimal totalsaldoeur = totalallunpaidtovendor.get(TotalSaldoEUR);

        for (UnpaidToVendorLine unpaidToVendorLine : unpaidtovendor) {

          HashMap<String, String> hashMap = new HashMap<String, String>();

          hashMap.put("GroupNumber", String.valueOf(unpaidToVendorLine.groupNumber));

          hashMap.put("Fecha", unpaidToVendorLine.invoicedDate);
          hashMap.put("FecVenc", unpaidToVendorLine.dueDate);
          hashMap.put("TipoDcto", unpaidToVendorLine.docType);
          hashMap.put("tipoOp", unpaidToVendorLine.tipoOp);
          hashMap.put("Documento", unpaidToVendorLine.documentNumber);
          hashMap.put("Banco", unpaidToVendorLine.bankAccount);
          hashMap.put("NDias", unpaidToVendorLine.paymentCondition);
          hashMap.put("Moneda", unpaidToVendorLine.currency);
          hashMap.put("Cargo", quitaComma(String.valueOf(unpaidToVendorLine.debit.doubleValue())));
          hashMap.put("Abono", quitaComma(String.valueOf(unpaidToVendorLine.credit.doubleValue())));
          hashMap.put("Saldo",
              quitaComma(String.valueOf(unpaidToVendorLine.balance.doubleValue())));
          hashMap.put("Percepcion", String.valueOf(unpaidToVendorLine.percepcion.doubleValue()));
          hashMap.put("docRecepcion", unpaidToVendorLine.docRecepcion);
          hashMap.put("estaFacturado", unpaidToVendorLine.estaFacturado);
          hashMap.put("recepcionDate", unpaidToVendorLine.recepcionDate);
          hashMap.put("paymentMethod", unpaidToVendorLine.paymentMethod);
          hashMap.put("daysTillDue",
              unpaidToVendorLine.daysTillDue != null
                  ? (String.valueOf(unpaidToVendorLine.daysTillDue) + " días")
                  : "");
          // gdf
          hashMap.put("ultFactura", String.valueOf(unpaidToVendorLine.ultFactura));
          hashMap.put("ultPago", String.valueOf(unpaidToVendorLine.ultPago));
          hashMap.put("creditSol", String.valueOf(unpaidToVendorLine.creditSol));
          hashMap.put("creditDol", String.valueOf(unpaidToVendorLine.creditDol));
          hashMap.put("cliente",
              bp != null ? (bp.getTaxID() + " - " + bp.getName()) : entry.getKey());

          hashMap.put("orgname", String.valueOf(unpaidToVendorLine.orgName));

          hashMap.put("totalcargopen", totalcargopen.toString());
          hashMap.put("totalabonopen", totalabonopen.toString());
          hashMap.put("totalsaldopen", totalsaldopen.toString());

          hashMap.put("totalcargousd", totalcargousd.toString());
          hashMap.put("totalabonousd", totalabonousd.toString());
          hashMap.put("totalsaldousd", totalsaldousd.toString());

          hashMap.put("totalcargoeur", totalcargoeur.toString());
          hashMap.put("totalabonoeur", totalabonoeur.toString());
          hashMap.put("totalsaldoeur", totalsaldoeur.toString());
          // fsdf
          hashMapList.add(hashMap);
        }
      }
    } else {

      List<UnpaidToVendorLine> unpaidtovendor = UnpaidToVendor.getUnpaidToVendor(this,
          strcBpartnetId, s, f, onlyPendings, strOrgId);

      List<BigDecimal> totalallunpaidtovendor = getTotalAmounts(unpaidtovendor);

      BigDecimal totalcargopen = totalallunpaidtovendor.get(TotalCargoPEN);
      BigDecimal totalabonopen = totalallunpaidtovendor.get(TotalAbonoPEN);
      BigDecimal totalsaldopen = totalallunpaidtovendor.get(TotalSaldoPEN);

      BigDecimal totalcargousd = totalallunpaidtovendor.get(TotalCargoUSD);
      BigDecimal totalabonousd = totalallunpaidtovendor.get(TotalAbonoUSD);
      BigDecimal totalsaldousd = totalallunpaidtovendor.get(TotalSaldoUSD);

      BigDecimal totalcargoeur = totalallunpaidtovendor.get(TotalCargoEUR);
      BigDecimal totalabonoeur = totalallunpaidtovendor.get(TotalAbonoEUR);
      BigDecimal totalsaldoeur = totalallunpaidtovendor.get(TotalSaldoEUR);

      for (UnpaidToVendorLine unpaidToVendorLine : unpaidtovendor) {

        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("GroupNumber", String.valueOf(unpaidToVendorLine.groupNumber));

        hashMap.put("Fecha", unpaidToVendorLine.invoicedDate);
        hashMap.put("FecVenc", unpaidToVendorLine.dueDate);
        hashMap.put("TipoDcto", unpaidToVendorLine.docType);
        hashMap.put("tipoOp", unpaidToVendorLine.tipoOp);
        hashMap.put("Documento", unpaidToVendorLine.documentNumber);
        hashMap.put("Banco", unpaidToVendorLine.bankAccount);
        hashMap.put("NDias", unpaidToVendorLine.paymentCondition);
        hashMap.put("Moneda", unpaidToVendorLine.currency);
        hashMap.put("Cargo", quitaComma(String.valueOf(unpaidToVendorLine.debit.doubleValue())));
        hashMap.put("Abono", quitaComma(String.valueOf(unpaidToVendorLine.credit.doubleValue())));
        hashMap.put("Saldo", quitaComma(String.valueOf(unpaidToVendorLine.balance.doubleValue())));
        hashMap.put("Percepcion", String.valueOf(unpaidToVendorLine.percepcion.doubleValue()));
        hashMap.put("docRecepcion", unpaidToVendorLine.docRecepcion);
        hashMap.put("estaFacturado", unpaidToVendorLine.estaFacturado);
        hashMap.put("recepcionDate", unpaidToVendorLine.recepcionDate);
        hashMap.put("paymentMethod", unpaidToVendorLine.paymentMethod);
        hashMap.put("daysTillDue",
            unpaidToVendorLine.daysTillDue != null
                ? (String.valueOf(unpaidToVendorLine.daysTillDue) + " días")
                : "");

        hashMap.put("ultFactura", String.valueOf(unpaidToVendorLine.ultFactura));
        hashMap.put("ultPago", String.valueOf(unpaidToVendorLine.ultPago));
        hashMap.put("creditSol", String.valueOf(unpaidToVendorLine.creditSol));
        hashMap.put("creditDol", String.valueOf(unpaidToVendorLine.creditDol));
        hashMap.put("orgname", String.valueOf(unpaidToVendorLine.orgName));

        hashMap.put("totalcargopen", totalcargopen.toString());
        hashMap.put("totalabonopen", totalabonopen.toString());
        hashMap.put("totalsaldopen", totalsaldopen.toString());

        hashMap.put("totalcargousd", totalcargousd.toString());
        hashMap.put("totalabonousd", totalabonousd.toString());
        hashMap.put("totalsaldousd", totalsaldousd.toString());

        hashMap.put("totalcargoeur", totalcargoeur.toString());
        hashMap.put("totalabonoeur", totalabonoeur.toString());
        hashMap.put("totalsaldoeur", totalsaldoeur.toString());
        hashMapList.add(hashMap);
      }
    }

    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(hashMapList);

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + VendorAccountsPayableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + VendorAccountsPayableData.selectRucOrg(this, strOrgId) + "\n";

    if (!("0".equals(strOrgId)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + VendorAccountsPayableData.selectOrg(this, strOrgId) + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/VendorAccountsPayable.jrxml";
      if (strcBpartnetId == null || strcBpartnetId.equalsIgnoreCase(""))
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/VendorAccountsPayableAllVendors.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/VendorAccountsPayableExcel.jrxml";
    }

    String nombreOrganizacion = "";

    Organization ad_org = OBDal.getInstance().get(Organization.class, strOrgId);
    if (ad_org != null)

      nombreOrganizacion = ad_org.getName();

    BusinessPartner c_bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    // parameters.put("Subtitle", strSubtitle);
    // parameters.put("TaxID",
    // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrgId));
    if (c_bpartner != null)
      parameters.put("BusinessPartner", c_bpartner.getTaxID() + " - " + c_bpartner.getName());
    else
      parameters.put("BusinessPartner", "Todos");

    parameters.put("Organizacion", nombreOrganizacion);

    parameters.put("endingDate", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Vendor Accounts Payable", strOutput, parameters, data,
        null);
  }

  private String quitaComma(String cadena) {
    return cadena.replace(",", "");
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
  }

  public static Set<String> getDocuments(String org, String accSchema) {

    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    OBContext.setAdminMode();
    try {
      // Set<String> orgStrct =
      // OBContext.getOBContext().getOrganizationStructureProvider()
      // .getChildTree(org, true);
      Set<String> orgStrct = OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(org);
      whereClause.append(" as cd ,");
      whereClause.append(AcctSchemaTable.ENTITY_NAME);
      whereClause.append(" as ca ");
      whereClause.append(" where cd.");
      whereClause.append(DocumentType.PROPERTY_TABLE + ".id");
      whereClause.append("= ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_TABLE + ".id");
      whereClause.append(" and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACCOUNTINGSCHEMA + ".id");
      whereClause.append(" = ? ");
      parameters.add(accSchema);
      whereClause.append("and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACTIVE + "='Y'");
      whereClause.append(" and cd.");
      whereClause.append(DocumentType.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" and ca." + AcctSchemaTable.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" order by cd." + DocumentType.PROPERTY_DOCUMENTCATEGORY);
      final OBQuery<DocumentType> obqDt = OBDal.getInstance().createQuery(DocumentType.class,
          whereClause.toString());
      obqDt.setParameters(parameters);
      obqDt.setFilterOnReadableOrganization(false);
      TreeSet<String> docBaseTypes = new TreeSet<String>();
      for (DocumentType doc : obqDt.list()) {
        docBaseTypes.add(doc.getDocumentCategory());
      }
      return docBaseTypes;

    } finally {
      OBContext.restorePreviousMode();
    }

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

  @Override
  public String getServletInfo() {
    return "Servlet VendorAccountsPayable. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
