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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class ReportSalesRevenueRecords extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  public static final String TABLEID_Invoice = "318";
  public static final String TABLEID_Payment = "D1A97202E832470285C9B1EB026D54E2";
  public static final String TABLEID_Transaction = "4D8C3B3C31D1410DA046140C9F024D17";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportSalesRevenueRecords|DateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportSalesRevenueRecords|DateTo",
          "");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesRevenueRecords|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportSalesRevenueRecords|Record",
          "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportSalesRevenueRecords|Table", "");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportSalesRevenueRecords|ShowInSummary", "");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportSalesRevenueRecords|DescargarLibroElectronico", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strDescargarLibroElectronico, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportSalesRevenueRecords|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportSalesRevenueRecords|Record");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportSalesRevenueRecords|ShowInSummary");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportSalesRevenueRecords|DescargarLibroElectronico", "");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportSalesRevenueRecords.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strShowInSummary, strDescargarLibroElectronico,
          strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesRevenueRecords|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportSalesRevenueRecords|DateTo");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportPurchasingRecords|ShowInSummary", "");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportSalesRevenueRecords|DescargarLibroElectronico", "");

      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesRevenueRecords|Org", "0");
      vars.setSessionValue("ReportSalesRevenueRecords.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strDescargarLibroElectronico, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesRevenueRecords|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportSalesRevenueRecords|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesRevenueRecords|Org", "0");
      String strShowInSummary = vars.getStringParameter("inpShowInSummary");
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");
      String strSoloContabilizadas = vars.getStringParameter("inpSoloContabilizados");
      
      String strPeriod = vars.getStringParameter("inpPeriodo");

      setHistoryCommand(request, "DEFAULT");
      // printPagePDF(response, vars, strDateFrom, strDateTo, strOrg,
      // strTable, strRecord);
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary, "",
          "", strDescargarLibroElectronico, strSoloContabilizadas, strPeriod);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strShowInSummary,
      String strDescargarLibroElectronico, String strTable, String strRecord)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportSalesRevenueRecordsData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesRevenueRecords", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, null, 'ReportSalesRevenueRecords.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecords", discard)
          .createXmlDocument();
      data = ReportSalesRevenueRecordsData.set("0");
      data[0].rownum = "0";
    } else {

      // data = notshow(data, vars);

      // toolbar
      // .prepareRelationBarTemplate(true, true,
      // "submitCommandForm('XLS', false, null, 'ReportSalesRevenueRecords.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecords")
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportSalesRevenueRecords");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportSalesRevenueRecords.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesRevenueRecords.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesRevenueRecords");
      vars.removeMessage("ReportSalesRevenueRecords");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesRevenueRecords"),
          Utility.getContext(this, vars, "#User_Client", "ReportSalesRevenueRecords"), '*');
      comboTableData.fillParameters(null, "ReportSalesRevenueRecords", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportGeneralLedger"),
            Utility.getContext(this, vars, "#User_Client", "ReportGeneralLedger"), ""));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    xmlDocument.setParameter("paramShowInSummary", !strShowInSummary.equals("Y") ? "0" : "1");
    xmlDocument.setParameter("paramDescargarLibroElectronico",
        !strDescargarLibroElectronico.equals("Y") ? "0" : "1");
    vars.setSessionValue("ReportSalesRevenueRecords|Record", strRecord);
    vars.setSessionValue("ReportSalesRevenueRecords|Table", strTable);

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReportPurchasingRecordsData.select_periodos(this)));

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportSalesRevenueRecordsData[]
  // notshow(ReportSalesRevenueRecordsData[] data,
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

  static private String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree,
        (strChild == null || strChild.equals("")) ? "0" : strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String adUserClient, String adUserOrg, String strDateFrom, String strDateTo, String strOrg,
      String strShowInSummary, String strTable, String strRecord,
      String strDescargarLibroElectronico, String strPeriod) throws Exception {
    ReportSalesRevenueRecordsCount counter = new ReportSalesRevenueRecordsCount();
    ReportSalesRevenueRecordsData[] data = getData(conn, vars, adUserClient, adUserOrg, strDateFrom,
        strDateTo, strOrg, strShowInSummary, strTable, strRecord, strPeriod, counter, "Y");

    StructPle sunatPle = getStringData(data, strDateFrom, strDateTo, strOrg, strShowInSummary,
        strTable, strRecord, strDescargarLibroElectronico);

    return sunatPle;
  }

  private static BigDecimal convertToBigDecimal2(String str) {
    try {
      return new BigDecimal(str).setScale(2, BigDecimal.ROUND_HALF_UP);
    } catch (Exception ex) {
      return new BigDecimal(0.00);
    }

  }

  private static ReportSalesRevenueRecordsData[] getData(ConnectionProvider conn,
      VariablesSecureApp vars, String adUserClient, String adUserOrg, String strDateFrom,
      String strDateTo, String strOrg, String strShowInSummary, String strTable, String strRecord, String strPeriod,
      ReportSalesRevenueRecordsCount counter, String fromPLE) throws IOException, ServletException {
    ReportSalesRevenueRecordsData[] data = null;

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    if (strShowInSummary.equals("Y")) {
      data = ReportSalesRevenueRecordsData.selectInvoiceSummary(conn, adUserClient, strOrgFamily,
          strDateFrom, DateTimeData.nDaysAfter(conn, strDateTo, "1"));
    } else {
      data = ReportSalesRevenueRecordsData.selectInvoice(conn, adUserClient, strOrgFamily,
          strDateFrom, strDateTo, strPeriod, fromPLE);
    }

    ReportSalesRevenueRecordsData[] datatmp = null;
    datatmp = ReportSalesRevenueRecordsData.selectSpecialTaxAll(conn, adUserClient, strOrgFamily,
        strDateFrom, DateTimeData.nDaysAfter(conn, strDateTo, "1"));

    counter.init();

    String currencyPEN_id = "308";// ReportSalesRevenueRecordsData.selectCurrency(conn, "PEN");
    HashMap<String, String> hashAmtConverted = new HashMap<String, String>();

    HashMap<String, String> hashAmtConvertedCompra = new HashMap<String, String>();

    for (int i = 0; i < data.length; i++) {
      // ReportSalesRevenueRecordsData[] datatmp = null;
      // datatmp = ReportSalesRevenueRecordsData.selectSpecialTax(conn, data[i].cInvoiceId);

      // ConversionRateDoc conversionRateCurrentDoc =
      // AcctServer.getConversionRateDocStatic(AcctServer.TABLEID_Invoice, data[i].cInvoiceId,
      // data[i].cCurrencyId, currencyPEN_id);

      /*
       * if (conversionRateCurrentDoc != null) { amountConverted = AcctServer.applyRate(new
       * BigDecimal("10000000000.00"), conversionRateCurrentDoc, true).setScale(2,
       * BigDecimal.ROUND_HALF_UP).toString(); } else {
       */
      data[i].basemonedaorigen = "0.00";
      data[i].igvmonedaorigen = "0.00";
      data[i].baseimponibletccompra = "0.00";

      String fecUsar = data[i].fecEmi;

      if (data[i].idreferencia.compareToIgnoreCase("") != 0
          || (data[i].esreferenciamanual.compareTo("Y") == 0)) {
        Invoice inv = OBDal.getInstance().get(Invoice.class, data[i].cInvoiceId);
        Date dateUsar = AcctServer.getDateForInvoiceReference(inv);

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        fecUsar = df.format(dateUsar);
      }

      String monfec = data[i].cCurrencyId + "-" + fecUsar;
      String amountConverted = hashAmtConverted.get(monfec);
      String amountConvertedCompra = hashAmtConvertedCompra.get(monfec);

      if (amountConverted == null) {
        amountConverted = AcctServer.getConvertedAmt("10000000000.00", data[i].cCurrencyId,
            currencyPEN_id, fecUsar, "", vars.getClient(), strOrg, new DalConnectionProvider());
        hashAmtConverted.put(monfec, amountConverted);

        amountConvertedCompra = AcctServer.getConvertedAmt("10000000000.00", currencyPEN_id,
            data[i].cCurrencyId, fecUsar, "", vars.getClient(), strOrg,
            new DalConnectionProvider());
        hashAmtConvertedCompra.put(monfec, amountConvertedCompra);
      }
      // }

      if (amountConverted.contentEquals("")) {
        data = null;
        return data;
      }
      BigDecimal tipoCambio = new BigDecimal(amountConverted)
          .divide(new BigDecimal("10000000000.00"), 12, BigDecimal.ROUND_HALF_UP);

      BigDecimal tipoCambioCompra = new BigDecimal("10000000000.00")
          .divide(new BigDecimal(amountConvertedCompra), 12, BigDecimal.ROUND_HALF_UP);

      if (tipoCambio.compareTo(new BigDecimal(1.00)) != 0)
        data[i].currate = tipoCambio.toPlainString();
      else
        data[i].currate = "";
      // ConversionRateDoc conversionRateCurrentDoc =
      // getConversionRateDoc(TABLEID_Invoice,
      // data[i].cInvoiceId, data[i].cCurrencyId, currencyPEN);

      data[i].basemonedaorigen = data[i].imptotal;
      data[i].igvmonedaorigen = data[i].tIgv;

      for (int j = 0; j < datatmp.length; j++) {

        if (!datatmp[j].cInvoiceId.equals(data[i].cInvoiceId)) {
          continue;
        }

        if (datatmp[j].taxvalue.equals("PVTE-Exonerado"))
          data[i].exon = getMontoEnSoles(tipoCambio, datatmp[j].taxtotal);
        if (datatmp[j].taxvalue.equals("PVTE-Inafecto-porDefecto"))
          data[i].inaf = getMontoEnSoles(tipoCambio, datatmp[j].taxtotal);
        if (datatmp[j].taxvalue.equals("PVTE-OtrosTributos-Cargos"))
          data[i].atrib = getMontoEnSoles(tipoCambio, datatmp[j].taxtotal);

        if (!datatmp[j].taxvalue.equals("PVTE-Exonerado")
            && !datatmp[j].taxvalue.equals("PVTE-Inafecto-porDefecto")
            && !datatmp[j].taxvalue.equals("PVTE-OtrosTributos-Cargos")) {
          data[i].baseimponibletccompra = (new BigDecimal(data[i].baseimponibletccompra)
              .add(new BigDecimal(getMontoEnSoles(tipoCambioCompra, datatmp[j].taxtotal))))
                  .toString();
        } else {
          data[i].basemonedaorigen = (new BigDecimal(data[i].basemonedaorigen)
              .subtract(new BigDecimal(datatmp[j].taxtotal))).toString();
        }

      }

      // Conversion a Soles

      // data[i].base = getMontoEnSoles(tipoCambio, (new
      // BigDecimal(data[i].imptotal).setScale(12).subtract(new
      // BigDecimal(data[i].tIgv).setScale(12)).toString()));

      data[i].basemonedaorigen = (new BigDecimal(data[i].basemonedaorigen)
          .subtract(new BigDecimal(data[i].tIgv))).toString();

      data[i].tIgv = getMontoEnSoles(tipoCambio, data[i].tIgv);
      // data[i].base = this.getMontoEnSoles(tipoCambio, (new
      // BigDecimal(data[i].imptotal).subtract(new BigDecimal(
      // data[i].tIgv)).toString()));
      data[i].imptotal = getMontoEnSoles(tipoCambio, data[i].imptotal);

      data[i].base = (new BigDecimal(data[i].imptotal).setScale(2, BigDecimal.ROUND_HALF_UP)
          .subtract(convertToBigDecimal2(data[i].tIgv)).subtract(convertToBigDecimal2(data[i].exon))
          .subtract(convertToBigDecimal2(data[i].inaf))
          .subtract(convertToBigDecimal2(data[i].atrib)).toString());

      if (data[i].tdcomp.equals("01"))
        counter.invoiceMemoCount++;// Factura
      else if (data[i].tdcomp.equals("03"))
        counter.ticketMemoCount++;// Boleta
      else if (data[i].tdcomp.equals("07"))
        counter.creditMemoCount++;// nota de Credito
      else if (data[i].tdcomp.equals("08"))
        counter.debitMemoCount++;// nota de Debito

      if (data[i].docstatus.equals("VO")) {
        data[i].bpdocid = "";
        data[i].bpname = "A N U L A D O";
        data[i].base = "0.00";
        data[i].exon = "0.00";
        data[i].inaf = "0.00";
        data[i].tIgv = "0.00";
        data[i].atrib = "0.00";
        data[i].imptotal = "0.00";
      }

      if (data[i].tdcomp.equals("07")) {

        data[i].base = (data[i].base.contains("-") || data[i].base.isEmpty()) ? data[i].base
            : "-" + data[i].base;
        data[i].exon = (data[i].exon.contains("-") || data[i].exon.isEmpty()) ? data[i].exon
            : "-" + data[i].exon;
        data[i].inaf = (data[i].inaf.contains("-") || data[i].inaf.isEmpty()) ? data[i].inaf
            : "-" + data[i].inaf;
        data[i].tIgv = (data[i].tIgv.contains("-") || data[i].tIgv.isEmpty()) ? data[i].tIgv
            : "-" + data[i].tIgv;
        data[i].atrib = (data[i].atrib.contains("-") || data[i].atrib.isEmpty()) ? data[i].atrib
            : "-" + data[i].atrib;
        data[i].imptotal = (data[i].imptotal.contains("-") || data[i].imptotal.isEmpty())
            ? data[i].imptotal : "-" + data[i].imptotal;

        data[i].basemonedaorigen = (data[i].basemonedaorigen.contains("-")
            || data[i].basemonedaorigen.isEmpty()) ? data[i].basemonedaorigen
                : "-" + data[i].basemonedaorigen;
        data[i].igvmonedaorigen = (data[i].igvmonedaorigen.contains("-")
            || data[i].igvmonedaorigen.isEmpty()) ? data[i].igvmonedaorigen
                : "-" + data[i].igvmonedaorigen;

        data[i].baseimponibletccompra = (data[i].baseimponibletccompra.contains("-")
            || data[i].baseimponibletccompra.isEmpty()) ? data[i].baseimponibletccompra
                : "-" + data[i].baseimponibletccompra;

      }

      if (data[i].bpdocid.contains("-")) {
        StringTokenizer st = new StringTokenizer(data[i].bpdocid, "-");
        data[i].bpdocid = st.nextToken();
      }

    }

    return data;
  }

  private static StructPle getStringData(ReportSalesRevenueRecordsData[] data, String strDateFrom,
      String strDateTo, String strOrg, String strShowInSummary, String strTable, String strRecord,
      String strDescargarLibroElectronico) {
    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

    int correlativo = 0;
    String prevRegNumber = "";
    Organization org = OBDal.getInstance().get(Organization.class, strOrg);
    String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11, '0');

    SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
    Date dttFrom = null;
    try {
      dttFrom = formatterForm.parse(strDateFrom);
    } catch (Exception ex) {
      System.out.println("Exception: " + strDateFrom);
    }

    SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
    SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

    String filename = "LE" + rucAdq + dt.format(dttFrom) + "00140100001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT
    ReportSalesRevenueRecordsData[] le = data;

    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat formatter3 = new DecimalFormat("#0.000");

    for (int i = 0; i < le.length; i++) {

      ReportSalesRevenueRecordsData led = le[i];

      Date dttAcct = null;
      try {
        dttAcct = formatterForm.parse(led.dateacct);
      } catch (Exception ex) {
      }
      String periodoTrib = dt.format(dttAcct) + "00";

      String regnumber = led.invoiceregnumber; // dt.format(dttAcct) + SunatUtil.LPad(led.nreg, 6,
                                               // '0'); //cuo

      if (regnumber.equalsIgnoreCase("")) {
        continue;
      }

      if (led.nreg.equals(""))
        continue;

      String tipoDoc = led.tdcomp;

      // if (tipoDoc.contains("03"))// QUITANDO LAS BOLETAS
      // continue;

      String tipoAsiento = led.invoiceseqno; // "M" + SunatUtil.LPad(String.valueOf(i + 1), 5, '0');
                                             // //correlativo
      String fecEmision = "";

      String fecVenc = "01/01/0001";
      try {
        fecEmision = dt2.format(formatterForm.parse(led.dateacct));
        if (tipoDoc.equals("14"))
          // fecVenc = dt2.format(formatterForm.parse(led.fecVenc));
          fecVenc = led.fecVenc == null ? "01/01/0001"
              : dt2.format(formatterForm.parse(led.fecVenc));
      } catch (Exception ex) {
      }

      String numComprobante = led.serie;
      String numSerie = "";
      if (led.serie.contains("-")) {
        StringTokenizer st = new StringTokenizer(led.serie, "-");
        numSerie = st.nextToken().trim();
        numComprobante = st.nextToken().trim();
        if (tipoDoc.equals("50")) {
          try {
            numComprobante = st.nextToken().trim();
            numComprobante = st.nextToken().trim();// el
          } catch (Exception ex) {
          }
          // 3er
          // digito
        }
      }

      if (!numSerie.equals(""))
        numSerie = SunatUtil.LPad(numSerie, 4, '0');

      String ticket = "";

      String tipoDocPartner = led.bptd;
      try {
        if (tipoDocPartner.matches("[0-9]+"))
          tipoDocPartner = String.valueOf(Integer.parseInt(led.bptd));// only number, no trailing 0s
      } catch (Exception e) {
      }

      String numDocPartner = led.bpdocid;
      String nameDocPartner = led.bpname;
      if (nameDocPartner.length() > 100)
        nameDocPartner = led.bpname.substring(0, 100);
      String valorExpor = "0.00";
      String baseImponible = ((led.base == null || led.base.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.base)));
      String exonerada = ((led.exon == null || led.exon.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.exon)));
      String inafecta = ((led.inaf == null || led.inaf.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.inaf)));
      String isc = "0.00";
      String igv = ((led.tIgv == null || led.tIgv.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.tIgv)));
      String baseImponibleArroz = "0.00";
      String igvArroz = "0.00";
      String otrosTrib = ((led.atrib == null || led.atrib.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.atrib)));
      String importeTotal = ((led.imptotal == null || led.imptotal.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.imptotal)));

      String tipoCambio = (led.currate.equals("") ? "1.000"
          : formatter3.format(Double.parseDouble(led.currate)));

      String monedaOrigen = SunatUtil.LPad(led.currencycode, 3, '0');

      // sunat de mela
      if (monedaOrigen.equals("001"))
        monedaOrigen = "PEN";
      else if (monedaOrigen.equals("002"))
        monedaOrigen = "USD";
      else
        monedaOrigen = "EUR";

      String fecEmisionRef = "";
      try {
        fecEmisionRef = (led.refdate == null) ? "" : dt2.format(formatterForm.parse(led.refdate));
      } catch (Exception ex) {
      }

      String tipoRef = led.reftdcomp;

      String nroRef = led.refseriep;
      String serieRef = "";
      if (led.refseriep.contains("-")) {
        StringTokenizer st = new StringTokenizer(led.refseriep, "-");
        serieRef = st.nextToken().trim();
        nroRef = st.nextToken().trim();

        if (!serieRef.equals(""))
          serieRef = SunatUtil.LPad(serieRef, 4, '0');

      }

      Date dttTrx = null;
      try {
        dttTrx = formatterForm.parse(led.fecEmi);
      } catch (Exception ex) {
      }

      SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
      /*
       * try { Date dateAcctf = formatterYYMM.parse( (dttAcct.getYear()+1900) + "/" +
       * dttAcct.getMonth()); Date dateTrxf = formatterYYMM.parse( (dttTrx.getYear()+1900) + "/" +
       * dttTrx.getMonth()); if (dateTrxf.compareTo(dateAcctf) < 0){ estadoOp = "8"; } } catch
       * (Exception e) { }
       */

      if (numDocPartner.equals("")) {// anulados
        tipoDocPartner = "0";
        numDocPartner = "0";
        nameDocPartner = "ANULADO";
      }

      String estadoOp = "1";

      if (data[i].docstatus.equals("VO")) {

        estadoOp = "2";
        // NOTA DE CREDITO ANULADA
        if (data[i].tdcomp.equals("07") || data[i].tdcomp.equals("08")) {
          // data[i].refdate = "00/00/0000";
          // data[i].refseriep = "0000-0000000";
          // data[i].reftdcomp = "00";

          fecEmisionRef = "01/01/0001";
          serieRef = "-";
          nroRef = "-";
          tipoRef = "00";

        }
      }

      if (tipoDoc.equalsIgnoreCase("07") || tipoDoc.equalsIgnoreCase("08")) {
        // if (led.montoigv == null) {
        // estadoOp = "0";
        // } else if (led.montoigv.isEmpty()) {
        // estadoOp = "0";
        // } else if (new BigDecimal(data[i].montoigv).compareTo(new BigDecimal(0)) == 0) {
        // estadoOp = "0";
        // }
        if (serieRef.contains("B"))
          tipoRef = "03";

        // if (led.montoigv == null) {
        // continue;
        // } else if (led.montoigv.isEmpty()) {
        // continue;
        // } else if (new BigDecimal(data[i].montoigv).compareTo(new BigDecimal(0)) == 0) {
        // continue;
        // }
      }

      /* recortar numero de  documento con longitud mayor */
      if (tipoDoc.contains("01") || tipoDoc.contains("02") || tipoDoc.contains("03")
          || tipoDoc.contains("04") || tipoDoc.contains("06") || tipoDoc.contains("07") || tipoDoc.contains("08")) {
        numSerie = validaLongitud(4, numSerie);
        numComprobante = validaLongitud(8, numComprobante);
      } else if(tipoDoc.equalsIgnoreCase("05") && numComprobante.length()>11 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-11, numComprobante.length());
      } else if(tipoDoc.equalsIgnoreCase("11") && numComprobante.length()>15 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-15, numComprobante.length());
      } else if(numComprobante.length()>20 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-20, numComprobante.length());
      }
      
      /* rellenar numero de documento con longitud menor */
      if ((tipoDoc.contains("01") || tipoDoc.contains("02") || tipoDoc.contains("03")
              || tipoDoc.contains("04") || tipoDoc.contains("06") || tipoDoc.contains("07") || tipoDoc.contains("08"))   ){
    	  
      }
      
      
      if(!led.estadoop.equals("") && led.estadoop!=null){
    	  estadoOp = led.estadoop;
      }
      
      if(led.fromple8.equals("Y")){
    	  //update period
    	  periodoTrib = periodoTrib.substring(0, 4) + led.newmonth + "00";
      }

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + fecEmision + "|"
          + fecVenc + "|" + tipoDoc + "|" + numSerie + "|" + numComprobante + "|" + ticket + "|"
          + tipoDocPartner + "|" + numDocPartner + "|" + nameDocPartner + "|" + valorExpor + "|"
          + baseImponible + "||" + igv + "||" + exonerada + "|" + inafecta + "|" + isc + "|"
          + baseImponibleArroz + "|" + igvArroz + "|" + otrosTrib + "|" + importeTotal + "|"
          + monedaOrigen + "|" + tipoCambio + "|" + fecEmisionRef + "|" + tipoRef + "|" + serieRef
          + "|" + nroRef + "||||" + estadoOp + "|";
      if (i > 0)
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;
    }

    sunatPle.filename = filename;
    sunatPle.data = sb.toString();
    return sunatPle;
  }

  private static String validaLongitud(int longFija, String cadena) {

    String str = cadena;
    int longStr = cadena.length();

    if (longStr < longFija) {
      for (int i = 0; i < longFija - longStr; i++) {
        str = "0" + str;
      }
    } else if (longStr > longFija) {
      str = cadena.substring(longStr - longFija, longStr);
      String sobra = cadena.substring(0, longStr - longFija);
      for (int i = 0; i < sobra.length(); i++) {
        sobra = sobra.replaceAll("0", "");
      }
      if (!sobra.equalsIgnoreCase("")) {
        return cadena;
      }
    }

    return str;
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strShowInSummary, String strTable, String strRecord,
      String strDescargarLibroElectronico, String soloContabilizadas, String strPeriod)
      throws IOException, ServletException {

    System.out.println("strDateFrom: " + strDateFrom + " - strDateTo: " + strDateTo + " - strOrg: "
        + strOrg + " - strShowInSummary: " + strShowInSummary + " - strTable: " + strTable
        + " - strRecord: " + strRecord + " - strDescargarLibroElectronico: "
        + strDescargarLibroElectronico);

    ReportSalesRevenueRecordsCount counter = new ReportSalesRevenueRecordsCount();
    ReportSalesRevenueRecordsData[] data = getData(this, vars,
        Utility.getContext(this, vars, "#User_Client", "ReportSalesRevenueRecords"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesRevenueRecords"),
        strDateFrom, strDateTo, strOrg, strShowInSummary, strTable, strRecord, strPeriod, counter, "N");

    if (data == null) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          "No se puede generar el reporte, por que hay fechas de documentos con tipo de cambio no definido.");
      return;
    }

    boolean isLE = false;
    if (strDescargarLibroElectronico.equals("Y")) {
      isLE = true;
    }

    if (isLE) {

      StructPle sunatPle = getStringData(data, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strTable, strRecord, strDescargarLibroElectronico);

      response.setContentType("text/plain");
      response.setHeader("Content-Disposition", "attachment;filename=" + sunatPle.filename);

      ServletOutputStream os = response.getOutputStream();

      os.write(sunatPle.data.getBytes());

      os.flush();
      os.close();

    } else {// PDF

      if (soloContabilizadas.equalsIgnoreCase("Y")) {
        ArrayList<ReportSalesRevenueRecordsData> lista = new ArrayList<ReportSalesRevenueRecordsData>();

        for (int i = 0; i < data.length; i++) {
          if (data[i].invoiceregnumber.equalsIgnoreCase("")) {
            continue;
          }

          if (data[i].tdcomp.equalsIgnoreCase("07") || data[i].tdcomp.equalsIgnoreCase("08")) {

            if (data[i].reftdcomp.contains("B"))
              data[i].reftdcomp = "03";
          }

          lista.add(data[i]);
        }

        data = lista.toArray(new ReportSalesRevenueRecordsData[lista.size()]);
      }

      String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
          + ReportSalesRevenueRecordsData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
          + ReportSalesRevenueRecordsData.selectRucOrg(this, strOrg) + "\n";
      ;

      if (!("0".equals(strOrg)))
        strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
            + ReportSalesRevenueRecordsData.selectOrg(this, strOrg) + "\n";

      // if (!"".equals(strDateFrom) || !"".equals(strDateTo))
      // strSubtitle += (Utility.messageBD(this, "From",
      // vars.getLanguage()) + ": ") + strDateFrom
      // + " " + (Utility.messageBD(this, "OBUIAPP_To",
      // vars.getLanguage()) + ": ") + strDateTo
      // + "\n";
      String strNombreArchivo = "Registro de Ventas e Ingresos";
      String strOutput;
      String strReportName;
      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        if (strShowInSummary.equals("Y")) {
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecordsResumen.jrxml";
          strNombreArchivo = "Registro de Ventas e Ingresos (Resumen)";
        } else
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecords.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesRevenueRecordsXls.jrxml";
      }

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("Subtitle", strSubtitle);
      parameters.put("Ruc", ReportSalesRevenueRecordsData.selectRucOrg(this, strOrg));
      parameters.put("Razon", ReportSalesRevenueRecordsData.selectSocialName(this, strOrg));
      // parameters.put("TaxID",
      // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
      parameters.put("dateFrom", StringToDate(strDateFrom));
      parameters.put("dateTo", StringToDate(strDateTo));
      parameters.put("ticketMemoCount", counter.ticketMemoCount);
      parameters.put("invoiceMemoCount", counter.invoiceMemoCount);
      parameters.put("creditMemoCount", counter.creditMemoCount);
      parameters.put("debitMemoCount", counter.debitMemoCount);

      if (data.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      renderJR(vars, response, strReportName, strNombreArchivo, strOutput, parameters, data, null);
    }
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReportSalesRevenueRecords. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method

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

  static private String getMontoEnSoles(BigDecimal rate, String monto) {
    if (monto == null)
      return "0.00";

    if (!monto.isEmpty()) {
      BigDecimal montoME = new BigDecimal(monto).setScale(12, RoundingMode.HALF_UP);
      montoME = montoME.multiply(rate).setScale(2, RoundingMode.HALF_UP);
      return montoME.toString();
    }
    return "0.00";
  }

}

class ReportSalesRevenueRecordsCount {

  public ReportSalesRevenueRecordsCount() {
    ticketMemoCount = 0;
    invoiceMemoCount = 0;
    creditMemoCount = 0;
    debitMemoCount = 0;
  }

  public void init() {
    ticketMemoCount = 0;
    invoiceMemoCount = 0;
    creditMemoCount = 0;
    debitMemoCount = 0;
  }

  public void print() {
    System.out
        .println("ticketMemoCount: " + ticketMemoCount + " - invoiceMemoCount: " + invoiceMemoCount
            + " - creditMemoCount: " + creditMemoCount + " - debitMemoCount: " + debitMemoCount);
  }

  Integer ticketMemoCount = 0;
  Integer invoiceMemoCount = 0;
  Integer creditMemoCount = 0;
  Integer debitMemoCount = 0;
}