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

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class CustomerAccountsReceivable extends HttpSecureAppServlet {
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

      String strDateTo = vars.getGlobalVariable("inpDateTo", "CustomerAccountsReceivable|DateTo",
          SREDateTimeData.lastDayofCurrentMonth(this));
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getGlobalVariable("inpOrgId", "CustomerAccountsReceivable|OrgId", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "CustomerAccountsReceivable|CB_PARTNER_ID", "");
      String strSoloPendientes = vars.getGlobalVariable("inpSoloPendientes",
          "CustomerAccountsReceivable|SoloPendientes", "Y");
      String strRecord = vars.getGlobalVariable("inpRecord", "CustomerAccountsReceivable|Record",
          "");
      String strTable = vars.getGlobalVariable("inpTable", "CustomerAccountsReceivable|Table", "");

      vars.removeSessionValue("CustomerAccountsReceivable|unpaidbyclientlines");

      printPageDataSheet(response, vars, null, null, strDateTo, strOrgId, strcBpartnetId, strTable,
          strRecord, strDateFrom, strSoloPendientes, null, null, null);

    } else if (vars.commandIn("DIRECT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "CustomerAccountsReceivable|DateFrom");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "CustomerAccountsReceivable|CB_PARTNER_ID", "");
      String strTable = vars.getGlobalVariable("inpTable", "CustomerAccountsReceivable|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "CustomerAccountsReceivable|Record");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("CustomerAccountsReceivable.initRecordNumber", "0");

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

      List<UnpaidByClientLine> unpaidbyclientlines = (List<UnpaidByClientLine>) vars
          .getSessionObject("CustomerAccountsReceivable|unpaidbyclientlines");

      List<BigDecimal> total = (List<BigDecimal>) vars
          .getSessionObject("CustomerAccountsReceivable|totalallunpaidbyclient");

      printGrid(response, vars, unpaidbyclientlines, total, strDateTo, strOrgId, strcBpartnetId, "",
          "", strDateFrom, strSoloPendientes, strDocIdToShow, strSpecialdoctype, strOperation);
    } else if (vars.commandIn("FIND")) {

      String strDateTo = vars.getStringParameter("inpDateTo");
      String strDateFrom = SREDateTimeData.firstDay(this, strDateTo);
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String strSoloPendientes = vars.getStringParameter("inpSoloPendientes");

      vars.setSessionValue("CustomerAccountsReceivable.initRecordNumber", "0");

      Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
      Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
      boolean onlyPendings = "Y".equals(strSoloPendientes) ? true : false;

      List<UnpaidByClientLine> unpaidbyclientlines = getUnpaidByClientLine(strcBpartnetId, from, to,
          onlyPendings, strOrgId);
      vars.setSessionObject("CustomerAccountsReceivable|unpaidbyclientlines", unpaidbyclientlines);

      List<BigDecimal> total = getTotalAmounts(unpaidbyclientlines);
      vars.setSessionObject("CustomerAccountsReceivable|totalallunpaidbyclient", total);

      printPageDataSheet(response, vars, unpaidbyclientlines, total, strDateTo, strOrgId,
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
      List<UnpaidByClientLine> unpaidbyclientlines, List<BigDecimal> totalallunpaidbyclient,
      String strDateTo, String strOrgId, String strcBpartnetId, String strTable, String strRecord,
      String strDateFrom, String strSoloPendientes, String strDocIdToShow, String strSpecialdoctype,
      String strOperation) throws IOException, ServletException {

    String[] discard = { "discard" };
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivableGrid",
            discard)
        .createXmlDocument();

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
    Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");

    FieldProvider[] detailData = getDetailData(strDocIdToShow, strSpecialdoctype, from, to);
    if (detailData == null || detailData.length == 0) {
      detailData = CustomerAccountsReceivableData.set("0");
    }

    xmlDocument.setData("structure", (detailData == null) ? set() : detailData);

    String documentRefDescription = "";
    if (!"SSASTANDARDORDER".equals(strSpecialdoctype) && !"SSAPOSORDER".equals(strSpecialdoctype)
        && !"SSAWAREHOUSEORDER".equals(strSpecialdoctype) && strSpecialdoctype != null
        && !"".equals(strSpecialdoctype)) {
      Invoice inv = OBDal.getInstance().get(Invoice.class, strDocIdToShow);
      if (inv != null) {
        documentRefDescription = strOperation + " " + inv.getIdentifier();
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

  private List<UnpaidByClientLine> getUnpaidByClientLine(String strcBpartnetId, Date from, Date to,
      boolean onlyPendings, String strOrgId) throws ServletException {
    List<UnpaidByClientLine> unpaidbyclient = UnpaidByClient.getUnpaidByClient(this, strcBpartnetId,
        from, to, onlyPendings, strOrgId);
    return unpaidbyclient;
  }

  private FieldProvider[] getFormattedData(List<UnpaidByClientLine> unpaidbyclient) {
    FieldProvider[] data = null;

    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    int countRecord = 0;
    for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {
      if (unpaidByClientLine.groupNumber == -1)
        continue;

      countRecord++;

      HashMap<String, String> hashMap = new HashMap<String, String>();

      hashMap.put("Fecha", unpaidByClientLine.invoicedDate);
      hashMap.put("FecVenc", unpaidByClientLine.dueDate);
      hashMap.put("TipoDcto", unpaidByClientLine.docType);
      hashMap.put("tipoOp", String.valueOf(unpaidByClientLine.tipoOp));
      hashMap.put("Documento",
          unpaidByClientLine.documentNumber != null ? unpaidByClientLine.documentNumber : "--");
      hashMap.put("NDias", unpaidByClientLine.paymentCondition);
      hashMap.put("Moneda", unpaidByClientLine.currency);
      hashMap.put("Cargo", String.valueOf(unpaidByClientLine.credit.doubleValue()));
      hashMap.put("Abono", String.valueOf(unpaidByClientLine.debit.doubleValue()));
      hashMap.put("Saldo", String.valueOf(unpaidByClientLine.balance.doubleValue() * -1));
      hashMap.put("Percepcion", String.valueOf(unpaidByClientLine.percepcion.doubleValue()));
      hashMap.put("rownum", String.valueOf(countRecord));
      hashMap.put("groupDocument", String.valueOf(unpaidByClientLine.groupDocument));
      hashMap.put("specialdoctype", unpaidByClientLine.specialdoctype);
      hashMap.put("specialdoctypeplus", unpaidByClientLine.specialdoctypeplus);
      hashMap.put("documentId", unpaidByClientLine.documentId);
      hashMap.put("docRecepcion", unpaidByClientLine.docRecepcion);
      hashMap.put("recepcionDate", unpaidByClientLine.recepcionDate);
      hashMap.put("orgName", unpaidByClientLine.orgName);
      hashMap.put("daysTillDue",
          unpaidByClientLine.daysTillDue != null
              ? (String.valueOf(unpaidByClientLine.daysTillDue) + " días")
              : "");
      hashMap.put("paymentMethod", unpaidByClientLine.paymentMethod);
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
    if (specialDocType.compareTo("SSASTANDARDORDER") == 0
        || specialDocType.compareTo("SSAPOSORDER") == 0
        || specialDocType.compareTo("SSAWAREHOUSEORDER") == 0)
      return null;

    List<UnpaidByClientLine> unpaidbyclient = UnpaidByClient.getDetailUnpaidByClient(strDocumentId,
        from, to);
    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    int countRecord = 0;
    for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {
      if (unpaidByClientLine.groupNumber == -1)
        continue;

      countRecord++;

      HashMap<String, String> hashMap = new HashMap<String, String>();

      hashMap.put("Fecha", unpaidByClientLine.invoicedDate);
      hashMap.put("FecVenc", unpaidByClientLine.dueDate);
      hashMap.put("TipoDcto", unpaidByClientLine.docType);
      hashMap.put("tipoOp", String.valueOf(unpaidByClientLine.tipoOp));
      hashMap.put("Documento",
          unpaidByClientLine.documentNumber != null ? unpaidByClientLine.documentNumber : "--");
      hashMap.put("Banco", unpaidByClientLine.bankAccount);
      hashMap.put("NDias", unpaidByClientLine.paymentCondition);
      hashMap.put("Moneda", unpaidByClientLine.currency);
      hashMap.put("Cargo", String.valueOf(unpaidByClientLine.credit.doubleValue()));
      hashMap.put("Abono", String.valueOf(unpaidByClientLine.debit.doubleValue()));
      hashMap.put("Saldo", String.valueOf(unpaidByClientLine.balance.doubleValue() * -1));
      hashMap.put("Percepcion", String.valueOf(unpaidByClientLine.percepcion.doubleValue()));
      hashMap.put("rownum", String.valueOf(countRecord));
      hashMap.put("groupNumber", String.valueOf(unpaidByClientLine.groupNumber));
      hashMap.put("specialdoctypeplus", unpaidByClientLine.specialdoctypeplus);
      hashMap.put("documentId", unpaidByClientLine.documentId);
      hashMap.put("docRecepcion", unpaidByClientLine.docRecepcion);
      hashMap.put("estaFacturado", unpaidByClientLine.estaFacturado);
      hashMap.put("recepcionDate", unpaidByClientLine.recepcionDate);
      hashMap.put("paymentMethod", unpaidByClientLine.paymentMethod);
      hashMap.put("daysTillDue",
          unpaidByClientLine.daysTillDue != null
              ? (String.valueOf(unpaidByClientLine.daysTillDue) + " días")
              : "");
      hashMapList.add(hashMap);

    }
    data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    return data;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      List<UnpaidByClientLine> unpaidbyclientlines, List<BigDecimal> totalallunpaidbyclient,
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
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CustomerAccountsReceivable", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Date to = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");
    Date from = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
    boolean onlyPendings = "Y".equals(strSoloPendientes) ? true : false;

    if (unpaidbyclientlines == null) {
      unpaidbyclientlines = getUnpaidByClientLine(strcBpartnetId, from, to, onlyPendings, strOrgId);
    }
    data = getFormattedData(unpaidbyclientlines);
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };

      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivable", discard)
          .createXmlDocument();
      data = CustomerAccountsReceivableData.set("0");

    } else {
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivable")
          .createXmlDocument();
    }

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.CustomerAccountsReceivable");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "CustomerAccountsReceivable.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "CustomerAccountsReceivable.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CustomerAccountsReceivable");
      vars.removeMessage("CustomerAccountsReceivable");
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

    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
    BusinessPartner salesRep = ((bp != null) ? bp.getSalesRepresentative() : null);
    xmlDocument.setParameter("SalesRep", (salesRep != null) ? salesRep.getName() : "--");

    String documentRefDescription = "";
    if (!"SSASTANDARDORDER".equals(strSpecialdoctype) && !"SSAPOSORDER".equals(strSpecialdoctype)
        && !"SSAWAREHOUSEORDER".equals(strSpecialdoctype) && strSpecialdoctype != null
        && !"".equals(strSpecialdoctype)) {
      Invoice inv = OBDal.getInstance().get(Invoice.class, strDocIdToShow);
      if (inv != null) {
        documentRefDescription = strOperation + " " + inv.getIdentifier();
      }
    }
    xmlDocument.setParameter("DocumentRef", documentRefDescription);

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    if (totalallunpaidbyclient == null) {
      totalallunpaidbyclient = getTotalAmounts(unpaidbyclientlines);
    }
    xmlDocument.setParameter("TotalCargoPEN", df.format(totalallunpaidbyclient.get(TotalCargoPEN)));
    xmlDocument.setParameter("TotalAbonoPEN", df.format(totalallunpaidbyclient.get(TotalAbonoPEN)));
    xmlDocument.setParameter("TotalSaldoPEN",
        df.format(totalallunpaidbyclient.get(TotalSaldoPEN).multiply(new BigDecimal(-1))));

    xmlDocument.setParameter("TotalCargoUSD", df.format(totalallunpaidbyclient.get(TotalCargoUSD)));
    xmlDocument.setParameter("TotalAbonoUSD", df.format(totalallunpaidbyclient.get(TotalAbonoUSD)));
    xmlDocument.setParameter("TotalSaldoUSD",
        df.format(totalallunpaidbyclient.get(TotalSaldoUSD).multiply(new BigDecimal(-1))));

    xmlDocument.setParameter("TotalCargoEUR", df.format(totalallunpaidbyclient.get(TotalCargoEUR)));
    xmlDocument.setParameter("TotalAbonoEUR", df.format(totalallunpaidbyclient.get(TotalAbonoEUR)));
    xmlDocument.setParameter("TotalSaldoEUR",
        df.format(totalallunpaidbyclient.get(TotalSaldoEUR).multiply(new BigDecimal(-1))));
    //

    vars.setSessionValue("CustomerAccountsReceivable|Record", strRecord);
    vars.setSessionValue("CustomerAccountsReceivable|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private List<BigDecimal> getTotalAmounts(List<UnpaidByClientLine> unpaidbyclient) {
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

    for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {
      if (unpaidByClientLine.groupNumber == -1)
        continue;

      if ("PEN".equals(unpaidByClientLine.currency)) {
        total.set(TotalCargoPEN, total.get(TotalCargoPEN).add(unpaidByClientLine.credit));
        total.set(TotalAbonoPEN, total.get(TotalAbonoPEN).add(unpaidByClientLine.debit));
        total.set(TotalSaldoPEN, total.get(TotalSaldoPEN).add(unpaidByClientLine.balance));

      } else if ("USD".equals(unpaidByClientLine.currency)) {
        total.set(TotalCargoUSD, total.get(TotalCargoUSD).add(unpaidByClientLine.credit));
        total.set(TotalAbonoUSD, total.get(TotalAbonoUSD).add(unpaidByClientLine.debit));
        total.set(TotalSaldoUSD, total.get(TotalSaldoUSD).add(unpaidByClientLine.balance));

      } else if ("EUR".equals(unpaidByClientLine.currency)) {
        total.set(TotalCargoEUR, total.get(TotalCargoEUR).add(unpaidByClientLine.credit));
        total.set(TotalAbonoEUR, total.get(TotalAbonoEUR).add(unpaidByClientLine.debit));
        total.set(TotalSaldoEUR, total.get(TotalSaldoEUR).add(unpaidByClientLine.balance));
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

  // private CustomerAccountsReceivableData[]
  // notshow(CustomerAccountsReceivableData[] data,
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

      HashMap<String, UnpaidByClientLineWithPartner> map = UnpaidByClient.getUnpaidByOrgToDate(this,
          vars.getClient(), strOrgId, s,f, onlyPendings);

      for (Map.Entry<String, UnpaidByClientLineWithPartner> entry : map.entrySet()) {
        List<UnpaidByClientLine> unpaidbyclient = entry.getValue().unpaidbyclients;
        BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class,
            entry.getValue().cBpartnerId);
        if (unpaidbyclient.size() == 0)
          continue;

        List<BigDecimal> totalallunpaidbyclient = getTotalAmounts(unpaidbyclient);

        BigDecimal totalcargopen = totalallunpaidbyclient.get(TotalCargoPEN);
        BigDecimal totalabonopen = totalallunpaidbyclient.get(TotalAbonoPEN);
        BigDecimal totalsaldopen = totalallunpaidbyclient.get(TotalSaldoPEN)
            .multiply(new BigDecimal(-1));

        BigDecimal totalcargousd = totalallunpaidbyclient.get(TotalCargoUSD);
        BigDecimal totalabonousd = totalallunpaidbyclient.get(TotalAbonoUSD);
        BigDecimal totalsaldousd = totalallunpaidbyclient.get(TotalSaldoUSD)
            .multiply(new BigDecimal(-1));

        BigDecimal totalcargoeur = totalallunpaidbyclient.get(TotalCargoEUR);
        BigDecimal totalabonoeur = totalallunpaidbyclient.get(TotalAbonoEUR);
        BigDecimal totalsaldoeur = totalallunpaidbyclient.get(TotalSaldoEUR)
            .multiply(new BigDecimal(-1));

        for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {

          HashMap<String, String> hashMap = new HashMap<String, String>();

          hashMap.put("GroupNumber", String.valueOf(unpaidByClientLine.groupNumber));

          hashMap.put("Fecha", unpaidByClientLine.invoicedDate);
          hashMap.put("FecVenc", unpaidByClientLine.dueDate);
          hashMap.put("TipoDcto", unpaidByClientLine.docType);
          hashMap.put("tipoOp", unpaidByClientLine.tipoOp);
          hashMap.put("Documento", unpaidByClientLine.documentNumber);
          hashMap.put("Banco", unpaidByClientLine.bankAccount);
          hashMap.put("NDias", unpaidByClientLine.paymentCondition);
          hashMap.put("Moneda", unpaidByClientLine.currency);
          hashMap.put("Cargo", quitaComma(String.valueOf(unpaidByClientLine.credit.doubleValue())));
          hashMap.put("Abono", quitaComma(String.valueOf(unpaidByClientLine.debit.doubleValue())));
          hashMap.put("Saldo",
              quitaComma(String.valueOf(unpaidByClientLine.balance.doubleValue() * -1)));
          hashMap.put("Percepcion", String.valueOf(unpaidByClientLine.percepcion.doubleValue()));
          hashMap.put("docRecepcion", unpaidByClientLine.docRecepcion);
          hashMap.put("estaFacturado", unpaidByClientLine.estaFacturado);
          hashMap.put("recepcionDate", unpaidByClientLine.recepcionDate);
          hashMap.put("paymentMethod", unpaidByClientLine.paymentMethod);
          hashMap.put("daysTillDue",
              unpaidByClientLine.daysTillDue != null
                  ? (String.valueOf(unpaidByClientLine.daysTillDue) + " días")
                  : "");
          // gdf
          hashMap.put("ultFactura", String.valueOf(unpaidByClientLine.ultFactura));
          hashMap.put("ultPago", String.valueOf(unpaidByClientLine.ultPago));
          hashMap.put("creditSol", String.valueOf(unpaidByClientLine.creditSol));
          hashMap.put("creditDol", String.valueOf(unpaidByClientLine.creditDol));
          hashMap.put("cliente",
              bp != null ? (bp.getTaxID() + " - " + bp.getName()) : entry.getKey());

          hashMap.put("orgname", String.valueOf(unpaidByClientLine.orgName));

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
      /*
       * CustomerAccountsReceivableData[] dataTerceros = CustomerAccountsReceivableData
       * .selectClientesActivos(this, strOrgFamily);
       * 
       * Date date = new Date(); SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
       * 
       * String firstDate = SREDateTimeData.firstDay(this, formatter.format(date));
       * 
       * f = pe.com.unifiedgo.report.common.Utility.ParseFecha(formatter.format(date),
       * "dd-MM-yyyy"); s = pe.com.unifiedgo.report.common.Utility.ParseFecha(firstDate,
       * "dd-MM-yyyy");
       * 
       * for (int i = 0; i < dataTerceros.length; i++) {
       * 
       * List<UnpaidByClientLine> unpaidbyclient = UnpaidByClient.getUnpaidByClientToDate(
       * dataTerceros[i].cBpartnerId, onlyPendings, dataTerceros[i].adOrgId);
       * 
       * if (unpaidbyclient.size() == 0) continue;
       * 
       * List<BigDecimal> totalallunpaidbyclient = getTotalAmounts(unpaidbyclient);
       * 
       * BigDecimal totalcargopen = totalallunpaidbyclient.get(TotalCargoPEN); BigDecimal
       * totalabonopen = totalallunpaidbyclient.get(TotalAbonoPEN); BigDecimal totalsaldopen =
       * totalallunpaidbyclient.get(TotalSaldoPEN) .multiply(new BigDecimal(-1));
       * 
       * BigDecimal totalcargousd = totalallunpaidbyclient.get(TotalCargoUSD); BigDecimal
       * totalabonousd = totalallunpaidbyclient.get(TotalAbonoUSD); BigDecimal totalsaldousd =
       * totalallunpaidbyclient.get(TotalSaldoUSD) .multiply(new BigDecimal(-1));
       * 
       * BigDecimal totalcargoeur = totalallunpaidbyclient.get(TotalCargoEUR); BigDecimal
       * totalabonoeur = totalallunpaidbyclient.get(TotalAbonoEUR); BigDecimal totalsaldoeur =
       * totalallunpaidbyclient.get(TotalSaldoEUR) .multiply(new BigDecimal(-1));
       * 
       * for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {
       * 
       * HashMap<String, String> hashMap = new HashMap<String, String>();
       * 
       * hashMap.put("GroupNumber", String.valueOf(unpaidByClientLine.groupNumber));
       * 
       * hashMap.put("Fecha", unpaidByClientLine.invoicedDate); hashMap.put("FecVenc",
       * unpaidByClientLine.dueDate); hashMap.put("TipoDcto", unpaidByClientLine.docType);
       * hashMap.put("tipoOp", unpaidByClientLine.tipoOp); hashMap.put("Documento",
       * unpaidByClientLine.documentNumber); hashMap.put("Banco", unpaidByClientLine.bankAccount);
       * hashMap.put("NDias", unpaidByClientLine.paymentCondition); hashMap.put("Moneda",
       * unpaidByClientLine.currency); hashMap.put("Cargo",
       * quitaComma(String.valueOf(unpaidByClientLine.credit.doubleValue()))); hashMap.put("Abono",
       * quitaComma(String.valueOf(unpaidByClientLine.debit.doubleValue()))); hashMap.put("Saldo",
       * quitaComma(String.valueOf(unpaidByClientLine.balance.doubleValue() * -1)));
       * hashMap.put("Percepcion", String.valueOf(unpaidByClientLine.percepcion.doubleValue()));
       * hashMap.put("docRecepcion", unpaidByClientLine.docRecepcion); hashMap.put("estaFacturado",
       * unpaidByClientLine.estaFacturado); hashMap.put("recepcionDate",
       * unpaidByClientLine.recepcionDate); hashMap.put("paymentMethod",
       * unpaidByClientLine.paymentMethod); hashMap.put("daysTillDue",
       * unpaidByClientLine.daysTillDue != null ? (String.valueOf(unpaidByClientLine.daysTillDue) +
       * " días") : ""); // gdf hashMap.put("ultFactura",
       * String.valueOf(unpaidByClientLine.ultFactura)); hashMap.put("ultPago",
       * String.valueOf(unpaidByClientLine.ultPago)); hashMap.put("creditSol",
       * String.valueOf(unpaidByClientLine.creditSol)); hashMap.put("creditDol",
       * String.valueOf(unpaidByClientLine.creditDol)); hashMap.put("cliente",
       * dataTerceros[i].cliente);
       * 
       * hashMap.put("orgname", String.valueOf(unpaidByClientLine.orgName));
       * 
       * hashMap.put("totalcargopen", totalcargopen.toString()); hashMap.put("totalabonopen",
       * totalabonopen.toString()); hashMap.put("totalsaldopen", totalsaldopen.toString());
       * 
       * hashMap.put("totalcargousd", totalcargousd.toString()); hashMap.put("totalabonousd",
       * totalabonousd.toString()); hashMap.put("totalsaldousd", totalsaldousd.toString());
       * 
       * hashMap.put("totalcargoeur", totalcargoeur.toString()); hashMap.put("totalabonoeur",
       * totalabonoeur.toString()); hashMap.put("totalsaldoeur", totalsaldoeur.toString()); // fsdf
       * hashMapList.add(hashMap); } }
       */
    } else {

      List<UnpaidByClientLine> unpaidbyclient = UnpaidByClient.getUnpaidByClient(this,
          strcBpartnetId, s, f, onlyPendings, strOrgId);

      List<BigDecimal> totalallunpaidbyclient = getTotalAmounts(unpaidbyclient);

      BigDecimal totalcargopen = totalallunpaidbyclient.get(TotalCargoPEN);
      BigDecimal totalabonopen = totalallunpaidbyclient.get(TotalAbonoPEN);
      BigDecimal totalsaldopen = totalallunpaidbyclient.get(TotalSaldoPEN)
          .multiply(new BigDecimal(-1));

      BigDecimal totalcargousd = totalallunpaidbyclient.get(TotalCargoUSD);
      BigDecimal totalabonousd = totalallunpaidbyclient.get(TotalAbonoUSD);
      BigDecimal totalsaldousd = totalallunpaidbyclient.get(TotalSaldoUSD)
          .multiply(new BigDecimal(-1));

      BigDecimal totalcargoeur = totalallunpaidbyclient.get(TotalCargoEUR);
      BigDecimal totalabonoeur = totalallunpaidbyclient.get(TotalAbonoEUR);
      BigDecimal totalsaldoeur = totalallunpaidbyclient.get(TotalSaldoEUR)
          .multiply(new BigDecimal(-1));

      for (UnpaidByClientLine unpaidByClientLine : unpaidbyclient) {

        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("GroupNumber", String.valueOf(unpaidByClientLine.groupNumber));

        hashMap.put("Fecha", unpaidByClientLine.invoicedDate);
        hashMap.put("FecVenc", unpaidByClientLine.dueDate);
        hashMap.put("TipoDcto", unpaidByClientLine.docType);
        hashMap.put("tipoOp", unpaidByClientLine.tipoOp);
        hashMap.put("Documento", unpaidByClientLine.documentNumber);
        hashMap.put("Banco", unpaidByClientLine.bankAccount);
        hashMap.put("NDias", unpaidByClientLine.paymentCondition);
        hashMap.put("Moneda", unpaidByClientLine.currency);
        hashMap.put("Cargo", quitaComma(String.valueOf(unpaidByClientLine.credit.doubleValue())));
        hashMap.put("Abono", quitaComma(String.valueOf(unpaidByClientLine.debit.doubleValue())));
        hashMap.put("Saldo",
            quitaComma(String.valueOf(unpaidByClientLine.balance.doubleValue() * -1)));
        hashMap.put("Percepcion", String.valueOf(unpaidByClientLine.percepcion.doubleValue()));
        hashMap.put("docRecepcion", unpaidByClientLine.docRecepcion);
        hashMap.put("estaFacturado", unpaidByClientLine.estaFacturado);
        hashMap.put("recepcionDate", unpaidByClientLine.recepcionDate);
        hashMap.put("paymentMethod", unpaidByClientLine.paymentMethod);
        hashMap.put("daysTillDue",
            unpaidByClientLine.daysTillDue != null
                ? (String.valueOf(unpaidByClientLine.daysTillDue) + " días")
                : "");

        hashMap.put("ultFactura", String.valueOf(unpaidByClientLine.ultFactura));
        hashMap.put("ultPago", String.valueOf(unpaidByClientLine.ultPago));
        hashMap.put("creditSol", String.valueOf(unpaidByClientLine.creditSol));
        hashMap.put("creditDol", String.valueOf(unpaidByClientLine.creditDol));
        hashMap.put("orgname", String.valueOf(unpaidByClientLine.orgName));

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
        + CustomerAccountsReceivableData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + CustomerAccountsReceivableData.selectRucOrg(this, strOrgId) + "\n";

    if (!("0".equals(strOrgId)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + CustomerAccountsReceivableData.selectOrg(this, strOrgId) + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivable.jrxml";
      if (strcBpartnetId == null || strcBpartnetId.equalsIgnoreCase(""))
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivableAllCustomers.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/CustomerAccountsReceivableExcel.jrxml";
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

    renderJR(vars, response, strReportName, "Customer Accounts Receivable", strOutput, parameters,
        data, null);
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
    return "Servlet CustomerAccountsReceivable. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
