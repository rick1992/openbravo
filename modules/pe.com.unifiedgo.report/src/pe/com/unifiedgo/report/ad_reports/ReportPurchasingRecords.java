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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
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
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportPurchasingRecords extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportPurchasingRecords|DateFrom",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportPurchasingRecords|DateTo",
          SREDateTimeData.today(this));
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchasingRecords|Org", "0");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportPurchasingRecords|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportPurchasingRecords|Table", "");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportPurchasingRecords|ShowInSummary", "");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportPurchasingRecords|DescargarLibroElectronico", "");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strDescargarLibroElectronico, strTable, strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportPurchasingRecords|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportPurchasingRecords|Record");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportPurchasingRecords|ShowInSummary");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportPurchasingRecords|DescargarLibroElectronico", "");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportPurchasingRecords.initRecordNumber", "0");

      printPageDataSheet(response, vars, "", "", "", strShowInSummary, strDescargarLibroElectronico,
          strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportPurchasingRecords|DateFrom");
      String strShowInSummary = vars.getGlobalVariable("inpShowInSummary",
          "ReportPurchasingRecords|ShowInSummary");
      String strDescargarLibroElectronico = vars.getGlobalVariable("inpDescargarLibroElectronico",
          "ReportPurchasingRecords|DescargarLibroElectronico", "");

      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportPurchasingRecords|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchasingRecords|Org", "0");
      vars.setSessionValue("ReportPurchasingRecords.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strDescargarLibroElectronico, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportPurchasingRecords|DateFrom");
      String strShowInSummary = vars.getStringParameter("inpShowInSummary");
      String strDescargarLibroElectronico = vars.getStringParameter("inpDescargarLibroElectronico");

      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportPurchasingRecords|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchasingRecords|Org", "0");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");

      String strSoloContabilizadas = vars.getStringParameter("inpSoloContabilizados");

      String strPeriod = vars.getStringParameter("inpPeriodo");

      setHistoryCommand(request, "DEFAULT");

      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strShowInSummary,
          strTable, strRecord, strDescargarLibroElectronico, strSoloContabilizadas, strPeriod);
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
    ReportPurchasingRecordsData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportPurchasingRecords", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, null, 'ReportPurchasingRecords.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportPurchasingRecords", discard)
          .createXmlDocument();
      data = ReportPurchasingRecordsData.set("0");
      data[0].rownum = "0";
    }

    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportPurchasingRecords");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportPurchasingRecords.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportPurchasingRecords.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportPurchasingRecords");
      vars.removeMessage("ReportPurchasingRecords");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "", Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportPurchasingRecords"),
          Utility.getContext(this, vars, "#User_Client", "ReportPurchasingRecords"), '*');
      comboTableData.fillParameters(null, "ReportPurchasingRecords", "");
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
    vars.setSessionValue("ReportPurchasingRecords|Record", strRecord);
    vars.setSessionValue("ReportPurchasingRecords|Table", strTable);

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReportPurchasingRecordsData.select_periodos(this)));

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportPurchasingRecordsData[]
  // notshow(ReportPurchasingRecordsData[] data,
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
    ReportPurchasingRecordsCount counter = new ReportPurchasingRecordsCount();
    ReportPurchasingRecordsData[] data = getData(conn, vars, adUserClient, adUserOrg, strDateFrom,
        strDateTo, strOrg, strShowInSummary, strTable, strRecord, strPeriod, counter, "Y");

    StructPle sunatPle = getStringData(data, strDateFrom, strDateTo, strOrg, strShowInSummary,
        strTable, strRecord, strDescargarLibroElectronico, strPeriod);

    return sunatPle;
  }

  public static StructPle getPLENoDomiciliado(ConnectionProvider conn, VariablesSecureApp vars,
      String adUserClient, String adUserOrg, String strDateFrom, String strDateTo, String strOrg,
      String strShowInSummary, String strTable, String strRecord,
      String strDescargarLibroElectronico, String strPeriod) throws Exception {
    ReportPurchasingRecordsCount counter = new ReportPurchasingRecordsCount();
    ReportPurchasingRecordsData[] data = getData(conn, vars, adUserClient, adUserOrg, strDateFrom,
        strDateTo, strOrg, strShowInSummary, strTable, strRecord, strPeriod, counter, "N");

    StructPle sunatPle = getStringDataNoDomiciliado(data, strDateFrom, strDateTo, strOrg,
        strShowInSummary, strTable, strRecord, strDescargarLibroElectronico);

    return sunatPle;
  }

  private static ReportPurchasingRecordsData[] getData(ConnectionProvider conn,
      VariablesSecureApp vars, String adUserClient, String adUserOrg, String strDateFrom,
      String strDateTo, String strOrg, String strShowInSummary, String strTable, String strRecord,
      String strPeriod, ReportPurchasingRecordsCount counter, String fromPLE)
      throws IOException, ServletException {

    ReportPurchasingRecordsData[] data = null;
    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);

    if (strShowInSummary.equals("Y")) {
      data = ReportPurchasingRecordsData.selectInvoiceSummary(conn, adUserClient, strOrgFamily,
          strDateFrom, DateTimeData.nDaysAfter(conn, strDateTo, "1"));
    } else {
      data = ReportPurchasingRecordsData.selectInvoice(conn, adUserClient, strOrgFamily,
          strDateFrom, strDateTo, strPeriod, fromPLE);
    }

    ReportPurchasingRecordsData[] datatmp = ReportPurchasingRecordsData.selectSpecialTaxAllInvoices(
        conn, adUserClient, strOrgFamily, strDateFrom,
        DateTimeData.nDaysAfter(conn, strDateTo, "1"));

    ReportPurchasingRecordsData[] dataTmpDetraccion = ReportPurchasingRecordsData
        .selectDetractionAll(conn, adUserClient, strOrgFamily, strDateFrom,
            DateTimeData.nDaysAfter(conn, strDateTo, "1"));

    ReportPurchasingRecordsData[] dataTempPercepcionCompra = ReportPurchasingRecordsData
        .selectTaxPerceptionPurchaseAll(conn, adUserClient, strOrgFamily, strDateFrom,
            DateTimeData.nDaysAfter(conn, strDateTo, "1"));

    ArrayList<Integer> listFacturasOrigen = new ArrayList<Integer>();
    counter.init();

    String currencyPEN_id = "308";
    HashMap<String, String> hashAmtConverted = new HashMap<String, String>();

    for (int i = 0; i < data.length; i++) {
      ReportPurchasingRecordsData aaaaaaaaaa = data[i];

      data[i].basemonedaorigen = "0.00";
      data[i].igvmonedaorigen = "0.00";

      // T/C
      String fecUsar = data[i].fecEmi;

      if (data[i].idreferencia.compareToIgnoreCase("") != 0
          || (data[i].esreferenciamanual.compareTo("Y") == 0)) {
        Invoice inv = OBDal.getInstance().get(Invoice.class, data[i].cInvoiceId);
        Date dateUsar = AcctServer.getDateForInvoiceReference(inv);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        fecUsar = df.format(dateUsar);
      }

      /*
       * ConversionRateDoc conversionRateCurrentDoc = AcctServer
       * .getConversionRateDocStatic(AcctServer.TABLEID_Invoice, data[i].cInvoiceId,
       * data[i].cCurrencyId, currencyPEN_id);
       * 
       * if (conversionRateCurrentDoc != null) { amountConverted = AcctServer .applyRate(new
       * BigDecimal("10000000000.00").setScale(2, RoundingMode.HALF_UP), conversionRateCurrentDoc,
       * true) .setScale(2, BigDecimal.ROUND_HALF_UP).toString(); } else {
       */

      String monfec = data[i].cCurrencyId + "-" + fecUsar;
      String amountConverted = hashAmtConverted.get(monfec);

      if (amountConverted == null) {
        amountConverted = AcctServer.getConvertedAmt("10000000000.00", data[i].cCurrencyId,
            currencyPEN_id, fecUsar, "", vars.getClient(), strOrg, new DalConnectionProvider());
        hashAmtConverted.put(monfec, amountConverted);

      }

      // }

      if (amountConverted.contentEquals("")) {
        data = null;
        return data;
      }

      BigDecimal tipoCambio = new BigDecimal(amountConverted)
          .divide(new BigDecimal("10000000000.00"), 12, BigDecimal.ROUND_HALF_UP);

      if (tipoCambio.compareTo(new BigDecimal(1.00)) != 0)
        data[i].currate = tipoCambio.toPlainString();
      else
        data[i].currate = "";

      // if (data[i].tdcomp.equals("50")) {
      // datatmp = ReportPurchasingRecordsData.selectDUA(this,
      // data[i].cInvoiceId);
      // listDuas.add(i);// guardando posicion de Dua
      // } else {

      // datatmp = ReportPurchasingRecordsData.selectSpecialTax2(conn,
      // data[i].cInvoiceId);
      //

      if (data[i].emScoDuatype.equals("SO"))
        listFacturasOrigen.add(i);// Guardando posicion de facturas
      // origen
      // }

      data[i].montoigv = getMontoEnSoles(tipoCambio, data[i].montoigv);// ////////////////////////////////////////////////////////////

      BigDecimal importetotal = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
      boolean encontrado = false;
      for (int j = 0; j < datatmp.length; j++) {

        if (!datatmp[j].cInvoiceId.equals(data[i].cInvoiceId)) {
          if (encontrado)
            break;
          continue;
        } else {
          encontrado = true;
        }

        if (datatmp[j].specialtax.equals("SCOIGV")) {
          if (datatmp[j].taxvalue.equals("PCTIGV-Oper100-grab-export")) {

            if (datatmp[j].base != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].base).setScale(2, RoundingMode.HALF_UP));
            if (datatmp[j].igv != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].igv).setScale(2, RoundingMode.HALF_UP));

            data[i].igv1 = getMontoEnSoles(tipoCambio, datatmp[j].igv);

            data[i].bi1 = getMontoEnSoles(tipoCambio, datatmp[j].base);
            // data[i].b1compra = getMontoEnSoles(tipoCambioCompra, datatmp[j].base);
            data[i].basemonedaorigen = datatmp[j].base;
            data[i].igvmonedaorigen = datatmp[j].igv;

          } else if (datatmp[j].taxvalue.equals("PCTIGV-Oper-Parcial-Grab-Export")) {
            if (datatmp[j].base != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].base).setScale(2, RoundingMode.HALF_UP));
            if (datatmp[j].igv != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].igv).setScale(2, RoundingMode.HALF_UP));
            data[i].bi2 = getMontoEnSoles(tipoCambio, datatmp[j].base);
            data[i].igv2 = getMontoEnSoles(tipoCambio, datatmp[j].igv);

          } else if (datatmp[j].taxvalue.equals("PCTIGV-Oper-NoGrab")) {
            if (datatmp[j].base != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].base).setScale(2, RoundingMode.HALF_UP));
            if (datatmp[j].igv != null)
              importetotal = importetotal
                  .add(new BigDecimal(datatmp[j].igv).setScale(2, RoundingMode.HALF_UP));
            data[i].bi3 = getMontoEnSoles(tipoCambio, datatmp[j].base);
            data[i].igv3 = getMontoEnSoles(tipoCambio, datatmp[j].igv);

          }
        } else if (datatmp[j].specialtax.equals("SCOEXEMPT")) {
          if (datatmp[j].base != null) {
            importetotal = importetotal
                .add(new BigDecimal(datatmp[j].base).setScale(2, RoundingMode.HALF_UP));
            data[i].atrib = getMontoEnSoles(tipoCambio, datatmp[j].base);

            // data[i].atrib = this.getMontoEnSoles(tipoCambio,
            // data[i].atrib);
          }

        }
      }
      data[i].imptotal = getMontoEnSoles(tipoCambio, importetotal.toString()); // importetotal.toString();

      // PARA CONVERTIR A DOLARES LOS CAMPOS FALTANTES
      data[i].adqno = getMontoEnSoles(tipoCambio, data[i].adqno);
      data[i].atribyotros = getMontoEnSoles(tipoCambio, data[i].atribyotros);

      // PARA AGREGAR EL MONTO DE LA PERCEPTION POR COMPRA A: LOS
      // ATRIBUTOS Y OTROS && IMPORTE TOTAL

      // ReportPurchasingRecordsData[] dataTempPercepcionCompra = null;
      // dataTempPercepcionCompra = ReportPurchasingRecordsData
      // .selectTaxPerceptionPurchase2(conn, data[i].cInvoiceId);

      String montoPerception = "0";
      for (int k = 0; k < dataTempPercepcionCompra.length; i++) {
        if (dataTempPercepcionCompra[k].cInvoiceId.equals(data[i].cInvoiceId)) {
          montoPerception = dataTempPercepcionCompra[k].montoPercepcion;
          break;
        }
      }

      data[i].atribyotros = new BigDecimal(data[i].atribyotros)
          .add(new BigDecimal(montoPerception).setScale(2, RoundingMode.HALF_UP)).toString();

      data[i].imptotal = new BigDecimal(data[i].imptotal)
          .add(new BigDecimal(montoPerception).setScale(2, RoundingMode.HALF_UP)).toString();
      //

      if (data[i].tdcomp.equals("01")) {
        counter.facturaCount++;
        data[i].nombretd = "Facturas";
        data[i].grouptd = "01";
      } else if (data[i].tdcomp.equals("03")) {
        counter.boletaCount++;
        data[i].nombretd = "Boletas";
        data[i].grouptd = "03";
      } else if (data[i].tdcomp.equals("07") || data[i].tdcomp.equals("87")
          || data[i].tdcomp.equals("97")) {
        counter.notacreditoCount++;
        data[i].nombretd = "Notas de Crédito";
        data[i].grouptd = "07";
        // cuando es una nota de credito los montos deben ser negativos
        data[i].bi1 = new BigDecimal(data[i].bi1).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();

        data[i].basemonedaorigen = new BigDecimal(data[i].basemonedaorigen)
            .multiply(new BigDecimal("-1.0")).setScale(2, RoundingMode.HALF_UP).toString();
        data[i].igvmonedaorigen = new BigDecimal(data[i].igvmonedaorigen)
            .multiply(new BigDecimal("-1.0")).setScale(2, RoundingMode.HALF_UP).toString();

        // data[i].bi1Compra = new BigDecimal(data[i].bi1Compra).multiply(new BigDecimal("-1.0"))
        // .setScale(2, RoundingMode.HALF_UP).toString();

        data[i].igv1 = new BigDecimal(data[i].igv1).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].bi2 = new BigDecimal(data[i].bi2).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].igv2 = new BigDecimal(data[i].igv2).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].bi3 = new BigDecimal(data[i].bi3).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].igv3 = new BigDecimal(data[i].igv3).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].adqno = new BigDecimal(data[i].adqno).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].atribyotros = new BigDecimal(data[i].atribyotros).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].imptotal = new BigDecimal(data[i].imptotal).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();
        data[i].montoigv = new BigDecimal(data[i].montoigv).multiply(new BigDecimal("-1.0"))
            .setScale(2, RoundingMode.HALF_UP).toString();

      } else if (data[i].tdcomp.equals("08")) {
        counter.notadebitoCount++;
        data[i].nombretd = "Notas de Débito";
        data[i].grouptd = "08";
      } else if (data[i].tdcomp.equals("50")) {
        counter.duaCount++;
        data[i].nombretd = "DUAs";
        data[i].grouptd = "50";

        // String montoIgvCuenta40 =
        // ReportPurchasingRecordsData.selectInvoiceLine40(conn,
        // data[i].cInvoiceId);
        // BigDecimal monto= new BigDecimal(montoIgvCuenta40);
        // data[i].igv1= new BigDecimal(getMontoEnSoles(tipoCambio,
        // monto.toString())).add(new
        // BigDecimal(data[i].igv1)).toString()
        // ;//monto.setScale(2,RoundingMode.HALF_UP).toString();
        //
        String[] newDocNum = data[i].numdoc.split("-");
        if (newDocNum.length > 3)
          data[i].numdoc = newDocNum[3];

      } else if (data[i].tdcomp.equals("91")) {
        counter.facturanodocCount++;
        data[i].nombretd = "Comprobantes de No Domiciliado";
        data[i].grouptd = "91";
      } else {
        counter.otrosCount++;
        data[i].nombretd = "Otros";
        data[i].grouptd = "100";
      }

      String[] newDocNum = data[i].numdoc.split("-");
      if (newDocNum.length == 2)
        data[i].numdoc = newDocNum[1];

      // seteando detraccion

      // ReportPurchasingRecordsData[] dataTmpDetraccion = null;
      // dataTmpDetraccion = ReportPurchasingRecordsData.selectDetraction(
      // conn, data[i].cInvoiceId);

      data[i].nrefDet = "";
      data[i].fecEmiDet = "";

      for (int k = 0; k < dataTmpDetraccion.length; k++) {

        if (dataTmpDetraccion[k].cInvoiceId.equals(data[i].cInvoiceId)) {
          if (data[i].nrefDet == null || data[i].nrefDet.equals("")) {
            data[i].nrefDet = dataTmpDetraccion[k].nrefDet;
            data[i].fecEmiDet = dataTmpDetraccion[k].fecEmiDet;
          }
        }

      }

      // if (dataTmpDetraccion.length > 0) {
      // data[i].nrefDet = dataTmpDetraccion[0].nrefDet;
      // data[i].fecEmiDet = dataTmpDetraccion[0].fecEmiDet;
      // } else {
      // data[i].nrefDet = "";
      // data[i].fecEmiDet = "";
      // }
      //

      BigDecimal montoigvreal = new BigDecimal(data[i].igv1).add(new BigDecimal(data[i].igv2))
          .add(new BigDecimal(data[i].igv3)).setScale(2, RoundingMode.HALF_UP);
      BigDecimal montoigvnominal = new BigDecimal(data[i].montoigv).setScale(2,
          RoundingMode.HALF_UP);

      if (montoigvreal.compareTo(montoigvnominal) != 0) {
        if (data[i].bi1 != null && !data[i].bi1.equals("")) {
          data[i].igv1 = new BigDecimal(data[i].igv1).add(montoigvnominal).subtract(montoigvreal)
              .setScale(2, RoundingMode.HALF_UP).toString();
        } else if (data[i].bi2 != null && !data[i].bi2.equals("")) {
          data[i].igv2 = new BigDecimal(data[i].igv2).add(montoigvnominal).subtract(montoigvreal)
              .setScale(2, RoundingMode.HALF_UP).toString();
        } else if (data[i].bi3 != null && !data[i].bi3.equals("")) {
          data[i].igv3 = new BigDecimal(data[i].igv3).add(montoigvnominal).subtract(montoigvreal)
              .setScale(2, RoundingMode.HALF_UP).toString();
        }

        data[i].imptotal = new BigDecimal(data[i].imptotal).add(montoigvnominal)
            .subtract(montoigvreal).setScale(2, RoundingMode.HALF_UP).toString();
      }

      // Fix para el reporte pdf
      String serie = data[i].serie;
      String serieRef = data[i].refseriep;
      String[] splittedSerie = serie.split("-");
      String[] splittedSerieRef = serieRef.split("-");

      data[i].serieDocumento = splittedSerie.length > 1 ? splittedSerie[0] : "";
      if (data[i].tdcomp.equalsIgnoreCase("50")) {
        data[i].numeroDocumento = splittedSerie.length > 3 ? splittedSerie[3] : serie;
        data[i].anioDua = splittedSerie.length > 3 ? splittedSerie[1] : "";
      } else {
        data[i].numeroDocumento = splittedSerie.length > 1 ? splittedSerie[1] : serie;
      }

      data[i].serieDocRef = splittedSerieRef.length > 1 ? splittedSerieRef[0] : "";
      data[i].numeroDocRef = splittedSerieRef.length > 1 ? splittedSerieRef[1] : serieRef;
      // ($F{tdcomp}.equals("50"))?(($F{serie}.contains("-"))?$F{serie}.split("-")[3]:$F{serie}):($F{serie}.contains("-"))?$F{serie}.split("-")[1]:$F{serie}

      if (data[i].migracionBase != null && data[i].migracionIgv != null
          && !data[i].migracionBase.equalsIgnoreCase("")
          && !data[i].migracionBase.equalsIgnoreCase("")) {
        BigDecimal base = new BigDecimal(data[i].migracionBase);
        BigDecimal igv = new BigDecimal(data[i].migracionIgv);
        data[i].bi1 = data[i].migracionBase;
        data[i].igv1 = data[i].migracionIgv;
        data[i].imptotal = base.add(igv).toString();
        data[i].bi2 = "0.00";
        data[i].igv2 = "0.00";
        data[i].bi3 = "0.00";
        data[i].igv3 = "0.00";

        data[i].adqno = "0.00";
        data[i].atribyotros = "0.00";
      }

    }

    /*
     * BigDecimal bi1t, bi2t, bi3t, atribt, igv1t, igv2t, igv3t, importeTotalt;
     * 
     * for (Integer i : listDuas) { bi1t = new BigDecimal(data[i].bi1).setScale(2,
     * RoundingMode.HALF_UP); bi2t = new BigDecimal(data[i].bi2).setScale(2, RoundingMode.HALF_UP);
     * bi3t = new BigDecimal(data[i].bi3).setScale(2, RoundingMode.HALF_UP); igv1t = new
     * BigDecimal(data[i].igv1).setScale(2, RoundingMode.HALF_UP); igv2t = new
     * BigDecimal(data[i].igv2).setScale(2, RoundingMode.HALF_UP); igv3t = new
     * BigDecimal(data[i].igv3).setScale(2, RoundingMode.HALF_UP); atribt = new
     * BigDecimal(data[i].atrib).setScale(2, RoundingMode.HALF_UP); importeTotalt = new
     * BigDecimal(data[i].imptotal).setScale(2, RoundingMode.HALF_UP);
     * 
     * for (Integer j : listFacturasOrigen) { if (data[i].emScoDuaId.equals(data[j].emScoDuaId)) {
     * bi1t = bi1t.add(new BigDecimal(data[j].bi1).setScale(2, RoundingMode.HALF_UP)); bi2t =
     * bi2t.add(new BigDecimal(data[j].bi2).setScale(2, RoundingMode.HALF_UP)); bi3t = bi3t.add(new
     * BigDecimal(data[j].bi3).setScale(2, RoundingMode.HALF_UP)); atribt = atribt.add(new
     * BigDecimal(data[j].atrib).setScale(2, RoundingMode.HALF_UP)); } } data[i].bi1 =
     * bi1t.toString(); data[i].bi2 = bi2t.toString(); data[i].bi3 = bi3t.toString(); data[i].atrib
     * = atribt.toString();
     * 
     * igv1t = bi1t.multiply(BigDecimal.valueOf(0.18).setScale(2, RoundingMode.HALF_UP)); igv2t =
     * bi2t.multiply(BigDecimal.valueOf(0.18).setScale(2, RoundingMode.HALF_UP)); igv3t =
     * bi3t.multiply(BigDecimal.valueOf(0.18).setScale(2, RoundingMode.HALF_UP));
     * 
     * data[i].igv1 = igv1t.toString(); data[i].igv2 = igv2t.toString(); data[i].igv3 =
     * igv3t.toString();
     * 
     * System.out.println(data[i].imptotal);
     * 
     * importeTotalt = importeTotalt.add(bi1t).add(bi2t).add(bi3t)
     * .add(atribt).add(igv1t).add(igv2t).add(igv3t).setScale(2, RoundingMode.HALF_UP);
     * data[i].imptotal = importeTotalt.toString();
     * 
     * System.out.println(data[i].imptotal); // //////
     * 
     * igv1t = new BigDecimal(data[i].igv1).setScale(2, RoundingMode.HALF_UP); igv2t = new
     * BigDecimal(data[i].igv2).setScale(2, RoundingMode.HALF_UP); igv3t = new
     * BigDecimal(data[i].igv3).setScale(2, RoundingMode.HALF_UP);
     * 
     * BigDecimal montoigvreal = igv1t.add(igv2t).add(igv3t).setScale(2, RoundingMode.HALF_UP);
     * 
     * BigDecimal montoigvnominal = new BigDecimal(data[i].montoigv).setScale(2,
     * RoundingMode.HALF_UP);
     * 
     * BigDecimal saldoigv = (montoigvnominal.subtract(montoigvreal).abs());
     * 
     * BigDecimal saldoigvs = (montoigvnominal.subtract(montoigvreal) .abs());
     * 
     * // if()
     * 
     * // ////// }
     */

    // no fecVenc en notas de credito y debito
    /*
     * for (int i = 0; i < data.length; i++) { String tipoDoc = data[i].tdcomp;
     * 
     * if (tipoDoc != null && !tipoDoc.equals("")) { int tdcompInt = Integer.parseInt(tipoDoc); if
     * (tdcompInt == 7 || tdcompInt == 8 || tdcompInt == 87 || tdcompInt == 88 || tdcompInt == 97 ||
     * tdcompInt == 98) data[i].fecVenc = null; } }
     */

    return data;
  }

  private static StructPle getStringDataNoDomiciliado(ReportPurchasingRecordsData[] data,
      String strDateFrom, String strDateTo, String strOrg, String strShowInSummary, String strTable,
      String strRecord, String strDescargarLibroElectronico) {

    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    StringBuffer sb = new StringBuffer();

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

    String filename = "LE" + rucAdq + dt.format(dttFrom) + "00080200001";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT
    ReportPurchasingRecordsData[] le = data;

    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat formatter3 = new DecimalFormat("#0.000");
    int jj = 0;
    for (int i = 0; i < le.length; i++) {

      ReportPurchasingRecordsData led = le[i];

      Date dttAcct = null;
      try {
        dttAcct = formatterForm.parse(led.dateacct);
      } catch (Exception ex) {
      }
      String periodoTrib = dt.format(dttAcct) + "00";

      String regnumber = dt.format(dttAcct) + SunatUtil.LPad(led.nreg, 6, '0');

      if (led.nreg.equals(""))
        continue;

      String tipoAsiento = "M" + SunatUtil.LPad(String.valueOf(i + 1), 5, '0');
      String fecEmision = "";
      try {
        fecEmision = dt2.format(formatterForm.parse(led.fecEmi));
      } catch (Exception ex) {
      }

      String tipoDoc = led.tdcomp;

      if (!tipoDoc.equals("91") && !tipoDoc.equals("97") && !tipoDoc.equals("98"))
        continue;

      led.serie = led.serie.replace("/", "");
      led.serie = led.serie.replace(" ", "");

      String numComprobante = led.serie;
      String numSerie = "";
      String anioDUA = "";
      if (led.serie.contains("-")) {
        StringTokenizer st = new StringTokenizer(led.serie, "-");
        numSerie = st.nextToken();
        numComprobante = st.nextToken();
        if (tipoDoc.equals("50") || tipoDoc.equals("52")) {
          try {
            anioDUA = numComprobante;
            numComprobante = st.nextToken();
            numComprobante = st.nextToken();// el
            // 3er
            // digito
          } catch (Exception ex) {
          }
        }
      }

      numSerie = SunatUtil.LPad(numSerie, 4, '0');

      // trailing 0s
      String numDocPartner = led.bpdocid;
      String numDocPartnerTrim = led.bpdocid;

      if (numDocPartnerTrim.length() > 15)
        numDocPartnerTrim = numDocPartnerTrim.substring(0, 15);

      String nameDocPartner = led.bpname;
      if (nameDocPartner.length() > 100)
        nameDocPartner = led.bpname.substring(0, 100);

      String importeTotal = ((led.imptotal == null || led.imptotal.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.imptotal)));

      String tipoCambio = (led.currate.equals("") ? "1.00"
          : formatter3.format(Double.parseDouble(led.currate)));

      String monedaOrigen = SunatUtil.LPad(led.currencycode, 3, '0');

      // sunat de mela
      if (monedaOrigen.equals("001"))
        monedaOrigen = "PEN";
      else if (monedaOrigen.equals("002"))
        monedaOrigen = "USD";
      else
        monedaOrigen = "EUR";

      String estadoOp = "0";
      SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
      /*
       * try { Date dateAcctf = formatterYYMM.parse( (dttAcct.getYear()+1900) + "/" +
       * dttAcct.getMonth()); Date dateTrxf = formatterYYMM.parse( (dttTrx.getYear()+1900) + "/" +
       * dttTrx.getMonth()); if (dateTrxf.compareTo(dateAcctf) < 0){ estadoOp = "9";
       * 
       * } } catch (Exception e) { }
       */

      // OBTENER DUA DE REFERENCIA
      Set<String> setDuaTypes = new HashSet<String>();
      setDuaTypes.add("50");
      setDuaTypes.add("51");
      setDuaTypes.add("52");
      setDuaTypes.add("53");

      OBCriteria<Invoice> invoiceDua = OBDal.getInstance().createCriteria(Invoice.class);
      invoiceDua.add(Restrictions.eq(Invoice.PROPERTY_SCODUA + ".id", led.emScoDuaId));
      invoiceDua.createAlias(Invoice.PROPERTY_SCOPODOCTYPECOMBOITEM, "pd");
      invoiceDua.add(Restrictions.in("pd." + SCRComboItem.PROPERTY_CODE, setDuaTypes));
      invoiceDua.setMaxResults(1);

      Invoice invDua = null;

      if (invoiceDua.list() != null && invoiceDua.list().size() == 1)
        invDua = (Invoice) invoiceDua.uniqueResult();

      String tipoDua = "";
      String serieDua = "";
      String anioDua = "";
      String nroDua = "";

      if (invDua == null) {// buscar en lineas
        Invoice inv = OBDal.getInstance().get(Invoice.class, led.cInvoiceId);
        List<InvoiceLine> invLines = inv.getInvoiceLineList();

        for (int ii = 0; ii < invLines.size(); ii++) {
          InvoiceLine invLine = invLines.get(ii);
          if (invLine.getSimDua() != null) {

            OBCriteria<Invoice> invoiceDuaLine = OBDal.getInstance().createCriteria(Invoice.class);
            invoiceDuaLine
                .add(Restrictions.eq(Invoice.PROPERTY_SCODUA + ".id", invLine.getSimDua().getId()));
            invoiceDuaLine.createAlias(Invoice.PROPERTY_SCOPODOCTYPECOMBOITEM, "pd");
            invoiceDuaLine.add(Restrictions.in("pd." + SCRComboItem.PROPERTY_CODE, setDuaTypes));
            invoiceDuaLine.setMaxResults(1);

            if (invoiceDuaLine.list() != null && invoiceDuaLine.list().size() == 1) {
              invDua = (Invoice) invoiceDuaLine.uniqueResult();
              break;
            }
          }
        }
      }

      if (invDua != null && invDua.getScoDua() != null) {// obtener datos de dua

        tipoDua = invDua.getScoPodoctypeComboitem().getCode();
        String physical = invDua.getScoDua().getSCODuanumber();
        nroDua = physical;
        if (physical.contains("-")) {
          StringTokenizer st = new StringTokenizer(physical, "-");
          serieDua = st.nextToken();
          nroDua = st.nextToken();
          if (tipoDua.equals("50") || tipoDua.equals("52")) {
            try {
              anioDua = nroDua;
              nroDua = st.nextToken();
              nroDua = st.nextToken();// el
              // 3er
              // digito
            } catch (Exception ex) {
            }
          }
        }
      } /*
         * else{ tipoDua = "91"; nroDua = led.refseriep;
         * 
         * if (led.refseriep.contains("-")) { StringTokenizer st = new
         * StringTokenizer(led.refseriep, "-"); serieDua = st.nextToken().replaceAll("\\D+", "");
         * nroDua = st.nextToken().replaceAll("\\D+", ""); if (tipoDua.equals("50") ||
         * tipoDua.equals("52")) { try{ anioDua = nroDua; nroDua = st.nextToken().replaceAll("\\D+",
         * ""); nroDua = st.nextToken().replaceAll("\\D+", "");// el 3er // digito }catch(Exception
         * ex){} }
         * 
         * if (!serieDua.equals("") && !serieDua.equals("50") && !serieDua.equals("52")) serieDua =
         * SunatUtil.LPad(serieDua, 4, '0'); } }
         */
      Invoice inv = OBDal.getInstance().get(Invoice.class, led.cInvoiceId);

      // FIN OBTENER DUA DE REFERENCIA

      String otrosConceptosAdicionales = "0.00";
      String montoRetencionIGV = "0.00";

      String pais = "";
      
      /* recortar los numeros de documentos segun tipo */
      if (tipoDoc.contains("01") || tipoDoc.contains("03") || tipoDoc.contains("04") || tipoDoc.contains("06") 
    		  || tipoDoc.contains("07") || tipoDoc.contains("08")) {
            numSerie = validaLongitud(4, numSerie);
            numComprobante = validaLongitud(8, numComprobante);
      }
      
      if(tipoDoc.equalsIgnoreCase("02") && numComprobante.length()>7 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-7, numComprobante.length());
      }
      
      if(tipoDoc.equalsIgnoreCase("05") && numComprobante.length()>11 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-11, numComprobante.length());
      }
      
      if(tipoDoc.equalsIgnoreCase("11") && numComprobante.length()>15 ){
    	  numComprobante = numComprobante.substring(numComprobante.length()-15, numComprobante.length());
      }

      try {
        pais = inv.getBusinessPartner().getBusinessPartnerLocationList().get(0).getLocationAddress()
            .getCountry().getScoSunatcode();
        if (pais == null)
          pais = "9249";// eeuu
      } catch (Exception ex) {
      }

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + fecEmision + "|"
          + tipoDoc + "|" + numSerie + "|" + numComprobante + "|" + importeTotal + "|"
          + otrosConceptosAdicionales + "|" + importeTotal + "|" + tipoDua + "|" + serieDua + "|"
          + anioDua + "|" + nroDua + "|" + montoRetencionIGV + "|" + monedaOrigen + "|" + tipoCambio
          + "|" + pais + "|" + numDocPartnerTrim + "||" + numDocPartnerTrim + "||||||||||00||00|||"
          + estadoOp + "|" + numDocPartner + "|";
      if (jj > 0)
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;
      jj++;
    }

    if (sunatPle.numEntries > 0) {
      filename = filename + "111.TXT";
    } else {
      filename = filename + "011.TXT";
    }

    sunatPle.filename = filename;
    sunatPle.data = sb.toString();
    return sunatPle;
  }

  private static StructPle getStringData(ReportPurchasingRecordsData[] data, String strDateFrom,
      String strDateTo, String strOrg, String strShowInSummary, String strTable, String strRecord,
      String strDescargarLibroElectronico, String strPeriod) {
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

    String filename = "LE" + rucAdq + dt.format(dttFrom) + "00080100001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT
    ReportPurchasingRecordsData[] le = data;

    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat formatter3 = new DecimalFormat("#0.000");

    int jj = 0;

    for (int i = 0; i < le.length; i++) {

      ReportPurchasingRecordsData led = le[i];

      Date dttAcct = null;
      try {
        dttAcct = formatterForm.parse(led.dateacct);
      } catch (Exception ex) {
      }

      String periodoTrib = dt.format(dttAcct) + "00";

      String regnumber = led.invoiceregnumber; // dt.format(dttAcct) + SunatUtil.LPad(led.nreg, 6,
                                               // '0');

      if (regnumber.equalsIgnoreCase("")) {// PARA FILTRAR LOS DOCUMENTOS QUE NO HAN SIDO
                                           // CONTABILIZADAS
        continue;
      }

      if (led.nreg.equals(""))
        continue;

      String tipoDoc = led.tdcomp;

      String tipoAsiento = led.invoiceseqno; // "M" + SunatUtil.LPad(String.valueOf(i + 1), 5,
                                             // '0');
      String fecEmision = "";
      String fecVenc = "01/01/0001";
      try {
        fecEmision = dt2.format(formatterForm.parse(led.fecEmi));
        if (tipoDoc.equals("14"))
          fecVenc = dt2.format(formatterForm.parse(led.fecVenc));

      } catch (Exception ex) {
      }

      if (tipoDoc.equals("91") || tipoDoc.equals("97") || tipoDoc.equals("98"))
        continue;

      String numComprobante = led.serie;
      String numSerie = "";
      String anioDUA = "";
      if (led.serie.contains("-")) {
        StringTokenizer st = new StringTokenizer(led.serie, "-");
        numSerie = st.nextToken();
        numComprobante = st.nextToken();
        if (tipoDoc.equals("50") || tipoDoc.equals("52")) {
          try {
            anioDUA = numComprobante;
            numComprobante = st.nextToken();
            numComprobante = st.nextToken();// el
            // 3er
            // digito
          } catch (Exception ex) {
          }
        }
      }
      
      if(i==608){
    	  System.out.println("aqui");
      }
      
      if((tipoDoc.equalsIgnoreCase("01") || tipoDoc.equalsIgnoreCase("03") || tipoDoc.equalsIgnoreCase("07") 
      		|| tipoDoc.equalsIgnoreCase("08")) && numSerie.length()>0){
      	try{
      		if(numSerie.charAt(0)=='F' || numSerie.charAt(0)=='B'){
          		numSerie = String.valueOf(numSerie.charAt(0)) + SunatUtil.LPad(numSerie.substring(1,numSerie.length()), 3, '0');
          	}else{
          		numSerie =  SunatUtil.LPad(numSerie, 4, '0');
          	}
      	}catch(Exception ex){
      		System.out.println(i);
      		System.out.println("Error");
      	}
      	
      	
      }else if (!numSerie.equals("") && (tipoDoc.equals("50") || tipoDoc.equals("51")
          || tipoDoc.equals("53") || tipoDoc.equals("54"))) {
        numSerie = SunatUtil.LPad(numSerie, 3, '0');
        if (numSerie.length() > 3 && numSerie.charAt(0) == '0') {
          int len = numSerie.length();
          numSerie = numSerie.substring(len - 3, len);
        }
      } else if (!numSerie.equals("") && !tipoDoc.equals("50") && !tipoDoc.equals("05")
          && !tipoDoc.equals("55") && !tipoDoc.equals("52")) {
        numSerie = SunatUtil.LPad(numSerie, 4, '0');
        if (numSerie.length() > 4 && numSerie.charAt(0) == '0') {
          int len = numSerie.length();
          numSerie = numSerie.substring(len - 4, len);
        }
      }

      if (numSerie.equals("") && tipoDoc.equals("12"))
        numSerie = "-";

      String totalNoDerechoFiscal = "";
      String tipoDocPartner = String.valueOf(Integer.parseInt(led.bptd));// only
      // number,
      // no
      // trailing 0s
      String numDocPartner = led.bpdocid;
      String nameDocPartner = led.bpname;
      if (nameDocPartner.length() > 100)
        nameDocPartner = led.bpname.substring(0, 100);

      String baseImponible1 = ((led.bi1 == null || led.bi1.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.bi1)));
      String igv1 = ((led.igv1 == null || led.igv1.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.igv1)));
      String baseImponible2 = ((led.bi2 == null || led.bi2.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.bi2)));
      String igv2 = ((led.igv2 == null || led.igv2.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.igv2)));
      String baseImponible3 = ((led.bi3 == null || led.bi3.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.bi3)));
      String igv3 = ((led.igv3 == null || led.igv3.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.igv3)));

      String adqNoGravadas = "0.00";
      String isc = "0.00";
      String otrosTributos = ((led.atrib == null || led.atrib.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.atrib)));

      String importeTotal = ((led.imptotal == null || led.imptotal.equals("")) ? "0.00"
          : formatter.format(Double.parseDouble(led.imptotal)));

      String tipoCambio = (led.currate.equals("") ? "1.000"
          : formatter3.format(Double.parseDouble(led.currate)));

      String fecEmisionRef = "";
      try {
        fecEmisionRef = (led.refdate == null) ? "" : dt2.format(formatterForm.parse(led.refdate));
      } catch (Exception ex) {
      }

      String tipoRef = led.reftdcomp;

      String nroRef = led.refseriep;
      String serieRef = "";
      String anioDUARef = "";
      if (led.refseriep.contains("-")) {
        StringTokenizer st = new StringTokenizer(led.refseriep, "-");
        serieRef = st.nextToken().trim();
        nroRef = st.nextToken().trim();
        if (tipoRef.equals("50") || tipoDoc.equals("52")) {
          try {
            anioDUARef = nroRef;
            nroRef = st.nextToken().trim();
            nroRef = st.nextToken().trim();// el 3er
            // digito
          } catch (Exception ex) {
          }
        }
        
        if((tipoDoc.equalsIgnoreCase("01") || tipoDoc.equalsIgnoreCase("03") || tipoDoc.equalsIgnoreCase("07") 
        		|| tipoDoc.equalsIgnoreCase("08"))){
        	
        	if(serieRef.charAt(0)=='F' || serieRef.charAt(0)=='B'){
        		serieRef = String.valueOf(serieRef.charAt(0)) + SunatUtil.LPad(serieRef.substring(1,serieRef.length() ), 3, '0');
        	}else{
        		serieRef =  SunatUtil.LPad(serieRef, 4, '0');
        	}
        	
        }else if (!serieRef.equals("") && !tipoRef.equals("50") && !tipoRef.equals("52")
            && !tipoRef.equals("55") && !tipoRef.equals("05"))
          serieRef = SunatUtil.LPad(serieRef, 4, '0');
        if (serieRef.length() > 4 && serieRef.charAt(0) == '0') {
          int len = serieRef.length();
          serieRef = serieRef.substring(len - 4, len);
        }

        if (serieRef.equals("") && tipoRef.equals("12"))
          serieRef = "-";

      }

      String fecDetraccion = "";
      try {
        fecDetraccion = (led.fecEmiDet == null) ? ""
            : dt2.format(formatterForm.parse(led.fecEmiDet));
      } catch (Exception ex) {
      }

      String monedaOrigen = SunatUtil.LPad(led.currencycode, 3, '0');

      // sunat de mela
      if (monedaOrigen.equals("001"))
        monedaOrigen = "PEN";
      else if (monedaOrigen.equals("002"))
        monedaOrigen = "USD";
      else
        monedaOrigen = "EUR";

      String constDetraccion = led.nrefDet;
      String indReten = "";// FIX-ME

      Date dttTrx = null;
      try {
        dttTrx = formatterForm.parse(led.fecEmi);
      } catch (Exception ex) {
      }

      String estadoOp = "1";

      if (led.montoigv == null) {
        estadoOp = "0";
      } else if (led.montoigv.isEmpty()) {
        estadoOp = "0";
      } else if (new BigDecimal(data[i].montoigv).compareTo(new BigDecimal(0)) == 0) {
        estadoOp = "0";
      }

      SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
      try {
        Date dateAcctf = formatterYYMM.parse((dttAcct.getYear() + 1900) + "/" + dttAcct.getMonth());
        Date dateTrxf = formatterYYMM.parse((dttTrx.getYear() + 1900) + "/" + dttTrx.getMonth());
        if (dateTrxf.compareTo(dateAcctf) < 0) {
          estadoOp = "6";
        }
      } catch (Exception e) {
      }

      /*
       * if (tipoDoc.equals("02") || tipoDoc.equals("12") || tipoDoc.equals("03") ||
       * tipoDoc.equals("10") || tipoDoc.equals("21"))// no credito fiscal estadoOp = "0";
       */

      /*if (tipoDoc.contains("01") || tipoDoc.contains("02") || tipoDoc.contains("03") || tipoDoc.contains("04") 
    		  || tipoDoc.contains("05") || tipoDoc.contains("06") || tipoDoc.contains("07") || tipoDoc.contains("08")) {
    	  if(numSerie.charAt(0)=='F' || numSerie.charAt(0)=='F'){
    		  numSerie = String.valueOf(numSerie.charAt(0)) + validaLongitud(3, numSerie.substring(1, numSerie.length()));
    	  }else{
    		  numSerie = validaLongitud(4, numSerie);
    	  }
      }*/
      
      if (tipoDoc.contains("01") || tipoDoc.contains("03") || tipoDoc.contains("04") || tipoDoc.contains("06") 
    		  || tipoDoc.contains("07")){
    	  numComprobante = validaLongitud(8, numComprobante);
      }
      
      if (tipoDoc.contains("02")){
    	  numComprobante = validaLongitud(7, numComprobante);
      }

      if (!led.estadoop.equals("") && led.estadoop != null) {
        estadoOp = led.estadoop;
      }

      if (led.fromple8.equals("Y")) {
        // update period
        periodoTrib = periodoTrib.substring(0, 4) + led.newmonth + "00";
      }

      String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + fecEmision + "|"
          + fecVenc + "|" + tipoDoc + "|" + numSerie + "|" + anioDUA + "|" + numComprobante + "|"
          + totalNoDerechoFiscal + "|" + tipoDocPartner + "|" + numDocPartner + "|" + nameDocPartner
          + "|" + baseImponible1 + "|" + igv1 + "|" + baseImponible2 + "|" + igv2 + "|"
          + baseImponible3 + "|" + igv3 + "|" + adqNoGravadas + "|" + isc + "|" + otrosTributos
          + "|" + importeTotal + "|" + monedaOrigen + "|" + tipoCambio + "|" + fecEmisionRef + "|"
          + tipoRef + "|" + serieRef + "|" + anioDUARef + "|" + nroRef + "|" + fecDetraccion + "|"
          + constDetraccion + "|" + indReten + "||||||||" + estadoOp + "|";
      if (jj > 0)
        sb.append("\n");
      sb.append(linea);
      sunatPle.numEntries++;
      jj++;
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

    ReportPurchasingRecordsCount counter = new ReportPurchasingRecordsCount();
    ReportPurchasingRecordsData[] data = getData(this, vars,
        Utility.getContext(this, vars, "#User_Client", "ReportPurchasingRecords"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportPurchasingRecords"),
        strDateFrom, strDateTo, strOrg, strShowInSummary, strTable, strRecord, strPeriod, counter,
        "N");

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
          strTable, strRecord, strDescargarLibroElectronico, strPeriod);

      response.setContentType("text/plain");
      response.setHeader("Content-Disposition", "attachment;filename=" + sunatPle.filename);

      ServletOutputStream os = response.getOutputStream();

      os.write(sunatPle.data.getBytes());

      os.flush();
      os.close();

    } else {// PDF

      if (soloContabilizadas.equalsIgnoreCase("Y")) {
        ArrayList<ReportPurchasingRecordsData> lista = new ArrayList<ReportPurchasingRecordsData>();

        for (int i = 0; i < data.length; i++) {
          if (!data[i].invoiceregnumber.equalsIgnoreCase("")) {

            lista.add(data[i]);
          }
        }

        data = lista.toArray(new ReportPurchasingRecordsData[lista.size()]);
      }

      String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
          + ReportPurchasingRecordsData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
          + ReportPurchasingRecordsData.selectRucOrg(this, strOrg) + "\n";
      ;

      if (!("0".equals(strOrg)))
        strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
            + ReportPurchasingRecordsData.selectOrg(this, strOrg) + "\n";

      String strNombreArchivo = "Registro de Compras";
      String strOutput;
      String strReportName;
      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        if (strShowInSummary.equals("Y")) {
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportPurchasingRecordsResume.jrxml";
          strNombreArchivo = "Registro de Compras (Resumen)";
        }

        else
          strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportPurchasingRecords.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportPurchasingRecordsExcel.jrxml";
      }
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("Ruc", ReportPurchasingRecordsData.selectRucOrg(this, strOrg));
      parameters.put("Razon", ReportPurchasingRecordsData.selectOrg(this, strOrg));
      parameters.put("Subtitle", strSubtitle);
      // parameters.put("TaxID",
      // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));

      parameters.put("dateFrom", StringToDate(strDateFrom));
      parameters.put("dateTo", StringToDate(strDateTo));
      parameters.put("facturaCount", counter.facturaCount);
      parameters.put("boletaCount", counter.boletaCount);
      parameters.put("notacreditoCount", counter.notacreditoCount);
      parameters.put("notadebitoCount", counter.notadebitoCount);
      parameters.put("duaCount", counter.duaCount);
      parameters.put("facturanodocCount", counter.facturanodocCount);
      parameters.put("otrosCount", counter.otrosCount);

      if (data.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

      renderJR(vars, response, strReportName, strNombreArchivo, strOutput, parameters, data, null);
    }
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
    return "Servlet ReportPurchasingRecords. This Servlet was made by Jose Valdez and could be modified by everybody. Thanks";
  } // end of getServletInfo() method

  static private String getMontoEnSoles(BigDecimal rate, String monto) {
    if (monto == null)
      return null;

    if (!monto.isEmpty()) {
      BigDecimal montoME = new BigDecimal(monto).setScale(2, RoundingMode.HALF_UP);
      montoME = montoME.multiply(rate);
      return montoME.setScale(2, RoundingMode.HALF_UP).toString();

      // BigDecimal montoME = new BigDecimal(monto);
      // montoME = montoME.multiply(rate);
      // return montoME.toString();
    }
    return null;
  }
}

class ReportPurchasingRecordsCount {

  public ReportPurchasingRecordsCount() {
    facturaCount = 0;
    boletaCount = 0;
    notacreditoCount = 0;
    notadebitoCount = 0;
    duaCount = 0;
    facturanodocCount = 0;
    otrosCount = 0;
  }

  public void init() {
    facturaCount = 0;
    boletaCount = 0;
    notacreditoCount = 0;
    notadebitoCount = 0;
    duaCount = 0;
    facturanodocCount = 0;
    otrosCount = 0;
  }

  public void print() {
    System.out.println("facturaCount: " + facturaCount + " - boletaCount: " + boletaCount
        + " - notacreditoCount: " + notacreditoCount + " - notadebitoCount: " + notadebitoCount
        + " - duaCount: " + duaCount + " - facturanodocCount: " + facturanodocCount
        + " - otrosCount: " + otrosCount);
  }

  Integer facturaCount;
  Integer boletaCount;
  Integer notacreditoCount;
  Integer notadebitoCount;
  Integer duaCount;
  Integer facturanodocCount;
  Integer otrosCount;
}