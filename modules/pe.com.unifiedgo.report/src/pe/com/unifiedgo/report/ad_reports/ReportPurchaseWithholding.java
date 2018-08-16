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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportPurchaseWithholding extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " +
    // session.getAttribute(name));
    // }

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom",
          "ReportPurchaseWithholding|DateFrom", SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportPurchaseWithholding|DateTo",
          SREDateTimeData.today(this));
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseWithholding|Org", "0");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportPurchaseWithholding|BPartnerId", "");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportPurchaseWithholding|Record",
          "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportPurchaseWithholding|Table", "");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strcBpartnetId, strTable,
          strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportPurchaseWithholding|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportPurchaseWithholding|Record");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportPurchaseWithholding|BPartnerId", "");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportPurchaseWithholding.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strcBpartnetId, strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportPurchaseWithholding|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportPurchaseWithholding|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseWithholding|Org", "0");
      // String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
      // "ReportPurchaseWithholding|BPartnerId", "");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId", "");

      vars.setSessionValue("ReportPurchaseWithholding.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strcBpartnetId, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportPurchaseWithholding|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportPurchaseWithholding|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportPurchaseWithholding|Org", "0");
      // String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
      // "ReportPurchaseWithholding|BPartnerId", "");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId", "");

      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      setHistoryCommand(request, "DEFAULT");
      printPagePDF(request, response, vars, strDateFrom, strDateTo, strOrg, strcBpartnetId,
          strTable, strRecord);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strcBpartnetId, String strTable,
      String strRecord) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportPurchaseWithholdingData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportPurchaseWithholding", false, "",
        "", "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, null, 'ReportPurchaseWithholding.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportPurchaseWithholding", discard)
          .createXmlDocument();
      data = ReportPurchaseWithholdingData.set("0");
      data[0].rownum = "0";
    } else {

      // data = notshow(data, vars);

      // toolbar
      // .prepareRelationBarTemplate(true, true,
      // "submitCommandForm('XLS', false, null, 'ReportPurchaseWithholding.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportPurchaseWithholding")
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportPurchaseWithholding");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ReportPurchaseWithholding.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportPurchaseWithholding.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportPurchaseWithholding");
      vars.removeMessage("ReportPurchaseWithholding");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "D4DF252DEC3B44858454EE5292A8B836",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportPurchaseWithholding"),
          Utility.getContext(this, vars, "#User_Client", "ReportPurchaseWithholding"), '*');
      comboTableData.fillParameters(null, "ReportPurchaseWithholding", "");
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

    xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
    xmlDocument.setParameter("paramBPartnerDescription",
        ReportPurchaseWithholdingData.selectBPartner(this, strcBpartnetId));

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReportPurchaseWithholdingData.select_periodos(this)));

    vars.setSessionValue("ReportPurchaseWithholding|Record", strRecord);
    vars.setSessionValue("ReportPurchaseWithholding|Table", strTable);

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strcBpartnetId, String strTable, String strRecord)
      throws IOException, ServletException {

    Date fechaIni = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
    Date fechaFin = pe.com.unifiedgo.report.common.Utility.ParseFecha(

        strDateTo, "dd-MM-yyyy");

    List<RetenRegVendor> RetencionList = null;
    try {
      RetencionList = UnpaidToVendor.getRegistroRetenciones(strcBpartnetId, fechaIni, fechaFin);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();

    BigDecimal ultimoSaldoSoles = new BigDecimal(0.0);
    BigDecimal ultimoSaldoDol = new BigDecimal(0.0);
    BigDecimal ultimaTasa = new BigDecimal(0.0);

    String documento = "";

    if (RetencionList.size() > 0) {
      documento = RetencionList.get(0).serieDoc + RetencionList.get(0).numDoc;
    }

    // for (RetenReg retenciones : RetencionList) {
    for (int index = 0; index < RetencionList.size(); index++) {

      HashMap<String, String> hashMap = new HashMap<String, String>();
      SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");

      RetenRegVendor retenciones = RetencionList.get(index);

      String transdate = formato.format(retenciones.transDate);
      // hashMap.put("debe", retenciones.debe);
      hashMap.put("haber", retenciones.haber);
      hashMap.put("numComp", retenciones.numComp);
      hashMap.put("numDoc", retenciones.numDoc);
      hashMap.put("partnerCommName", retenciones.partnerCommName);
      hashMap.put("partnerRuc", retenciones.partnerRuc);
      hashMap.put("partnerUid", retenciones.partnerUid);
      // hashMap.put("saldo", retenciones.saldo);
      hashMap.put("serieComp", retenciones.serieComp);
      hashMap.put("serieDoc", retenciones.serieDoc);
      hashMap.put("tipoDoc", retenciones.tipoDoc);
      hashMap.put("tipoTrans", retenciones.tipoTrans);
      hashMap.put("totalComp", retenciones.totalComp);
      hashMap.put("totalDoc", retenciones.totalDoc);
      hashMap.put("groupNumber", String.valueOf(retenciones.groupNumber));
      hashMap.put("transDate", transdate);
      hashMap.put("currencySymbol", retenciones.currencySymbol);

      // /////////agregado por ricardo

      Invoice ivRetencion = retenciones.invo;

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

      Date dateFechaFactura = AcctServer.getTCenVigenciaFecha(ivRetencion, strDateFrom, null);

      String fecUsar = "";

      if (retenciones.tipoTrans.compareToIgnoreCase("Compra") == 0) {
        fecUsar = df.format(dateFechaFactura);
      } else {
        fecUsar = df.format(retenciones.transDate);
      }

      BigDecimal tipoCambio = getTasaCambioxFecha(ivRetencion, fecUsar, vars, strOrg, true);

      String debeSoles = this.getMontoEnSoles(tipoCambio, retenciones.debe);

      String saldostr;
      // cuendo es compra
      if (retenciones.tipoTrans.compareToIgnoreCase("Compra") == 0) {

        if (tipoCambio.compareTo(new BigDecimal(1.0000)) == 0) {
          saldostr = retenciones.saldo;
        } else {
          saldostr = this.getMontoEnSoles(tipoCambio, retenciones.saldo);

          ultimoSaldoDol = new BigDecimal(retenciones.saldo);
          ultimoSaldoSoles = new BigDecimal(saldostr);
          ultimaTasa = tipoCambio;
        }

      } else {

        if (tipoCambio.compareTo(new BigDecimal(1.0000)) == 0) {
          saldostr = retenciones.saldo;
        } else {

          BigDecimal montoAjuste = tipoCambio.subtract(ultimaTasa).multiply(ultimoSaldoDol);

          montoAjuste = montoAjuste.setScale(2, BigDecimal.ROUND_HALF_UP);

          ultimoSaldoDol = ultimoSaldoDol.subtract(new BigDecimal(retenciones.montoPagoOriginal));

          ultimoSaldoSoles = ultimoSaldoSoles.add(montoAjuste)
              .subtract(new BigDecimal(retenciones.haber));

          ultimaTasa = tipoCambio;

          saldostr = ultimoSaldoSoles.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

          HashMap<String, String> hashMapAjuste = new HashMap<String, String>();

          hashMapAjuste.put("numComp", "");
          hashMapAjuste.put("numDoc", retenciones.numDoc);
          hashMapAjuste.put("partnerCommName", retenciones.partnerCommName);
          hashMapAjuste.put("partnerRuc", retenciones.partnerRuc);
          hashMapAjuste.put("partnerUid", retenciones.partnerUid);
          hashMapAjuste.put("serieComp", "");
          hashMapAjuste.put("serieDoc", retenciones.serieDoc);
          hashMapAjuste.put("tipoDoc", retenciones.tipoDoc);
          hashMapAjuste.put("tipoTrans", retenciones.tipoTrans);
          hashMapAjuste.put("totalComp", "");
          hashMapAjuste.put("totalDoc", retenciones.totalDoc);
          hashMapAjuste.put("groupNumber", String.valueOf(retenciones.groupNumber));
          hashMapAjuste.put("transDate", transdate);
          hashMapAjuste.put("tipoTrans", "Diferencia de cambio");
          hashMapAjuste.put("currencySymbol", retenciones.currencySymbol);

          if (montoAjuste.compareTo(new BigDecimal(0.0)) > 0) {
            hashMapAjuste.put("debe", montoAjuste.abs().toString());
            hashMapAjuste.put("haber", "");

          } else {
            hashMapAjuste.put("debe", "");
            hashMapAjuste.put("haber", montoAjuste.abs().toString());
          }
          hashMapList.add(hashMapAjuste);
        }
      }

      hashMap.put("saldo", saldostr);
      hashMap.put("debe", debeSoles);
      hashMap.put("tasa", tipoCambio.toString());

      hashMapList.add(hashMap);

      Integer index2 = 0;

      if (RetencionList.size() == index + 1 || documento.compareToIgnoreCase(
          RetencionList.get(index + 1).serieDoc + RetencionList.get(index + 1).numDoc) != 0) {

        if (RetencionList.size() != index + 1)
          index2 = index + 1;
        else
          index2 = index;

        documento = RetencionList.get(index2).serieDoc + RetencionList.get(index2).numDoc;

        if (ultimoSaldoDol.compareTo(new BigDecimal(0)) > 0) {
          //
          // String monto = AcctServer.getConvertedAmt("1000.00", "100",
          // "308", strDateTo, "", vars.getClient(), strOrg,
          // this, true);
          //
          // tipoCambio = new BigDecimal(monto).divide(new BigDecimal(
          // "1000.00"), 4, BigDecimal.ROUND_HALF_UP);
          //
          Boolean esUltimaFecha = comparaConUltimaFechaMes(strDateTo);

          tipoCambio = getTasaCambioxFecha(ivRetencion, strDateTo, vars, strOrg, true);

          if (tipoCambio != null && esUltimaFecha) { // para ocultar el fin de cierre de mes cuando
            // la fecha hasta no tenga tipo de cambio o no sea fin de mes

            BigDecimal montoSaldo = ultimoSaldoDol.multiply(tipoCambio);

            BigDecimal difSaldosCierre = montoSaldo.subtract(ultimoSaldoSoles);

            montoSaldo = montoSaldo.setScale(2, BigDecimal.ROUND_HALF_UP);

            difSaldosCierre = difSaldosCierre.setScale(2, BigDecimal.ROUND_HALF_UP);

            HashMap<String, String> hashMapAjusteMes = new HashMap<String, String>();

            hashMapAjusteMes.put("numComp", "");
            hashMapAjusteMes.put("numDoc", retenciones.numDoc);
            hashMapAjusteMes.put("partnerCommName", retenciones.partnerCommName);
            hashMapAjusteMes.put("partnerRuc", retenciones.partnerRuc);
            hashMapAjusteMes.put("partnerUid", retenciones.partnerUid);
            hashMapAjusteMes.put("serieComp", "");
            hashMapAjusteMes.put("serieDoc", retenciones.serieDoc);
            hashMapAjusteMes.put("tipoDoc", retenciones.tipoDoc);
            hashMapAjusteMes.put("tipoTrans", retenciones.tipoTrans);
            hashMapAjusteMes.put("totalComp", "");
            hashMapAjusteMes.put("totalDoc", retenciones.totalDoc);
            hashMapAjusteMes.put("groupNumber", String.valueOf(retenciones.groupNumber));
            hashMapAjusteMes.put("transDate", transdate);
            hashMapAjusteMes.put("tipoTrans", "Cierre de fin de mes");

            hashMapAjusteMes.put("saldo", montoSaldo.abs().toString());
            hashMapAjusteMes.put("tasa", tipoCambio.toString());
            hashMapAjusteMes.put("currencySymbol", retenciones.currencySymbol);

            if (difSaldosCierre.compareTo(new BigDecimal(0.0)) > 0) {
              hashMapAjusteMes.put("debe", difSaldosCierre.abs().toString());
              hashMapAjusteMes.put("haber", "");

            } else {
              hashMapAjusteMes.put("debe", "");
              hashMapAjusteMes.put("haber", difSaldosCierre.abs().toString());
            }
            hashMapList.add(hashMapAjusteMes);

          }
          // //////////////
        }
      }
    }

    if (RetencionList.size() == 0 && !strcBpartnetId.isEmpty()) {
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
      if (bp != null) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("partnerCommName", bp.getName());
        hashMap.put("partnerRuc", bp.getTaxID());
        hashMapList.add(hashMap);
      }
    }

    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(hashMapList);

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportPurchaseWithholdingData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReportPurchaseWithholdingData.selectRucOrg(this, strOrg) + "\n";
    ;

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportPurchaseWithholdingData.selectOrg(this, strOrg) + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportPurchaseWithholding.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportPurchaseWithholdingExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    // parameters.put("TaxID",
    // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    parameters.put("totalLines", data.length);
    parameters.put("Ruc", ReportPurchaseWithholdingData.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportPurchaseWithholdingData.selectSocialName(this, strOrg));

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }

    renderJR(vars, response, strReportName, "Libro_Retenciones", strOutput, parameters, data, null);
  }

  private BigDecimal getTasaCambioxFecha(Invoice ivPercepcion, String fecUsar,
      VariablesSecureApp vars, String strOrg, Boolean modoCambio) {// true : ventas ; false :compra

    String amountConverted = "";
    String currencyPEN_id = "308";

    ConversionRateDoc conversionRateCurrentDoc = AcctServer.getConversionRateDocStatic(
        AcctServer.TABLEID_Invoice, ivPercepcion.getId(), ivPercepcion.getCurrency().getId(),
        currencyPEN_id);

    try {
      if (conversionRateCurrentDoc != null) {
        amountConverted = AcctServer
            .applyRate(new BigDecimal("1000.00"), conversionRateCurrentDoc, true)
            .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
      } else {

        if (modoCambio) {
          amountConverted = AcctServer.getConvertedAmt("1000.00",
              ivPercepcion.getCurrency().getId(), currencyPEN_id, fecUsar, "", vars.getClient(),
              strOrg, // cambio por
                      // la p
              new DalConnectionProvider());
        } else {
          amountConverted = AcctServer.getConvertedAmt("1000000.00", currencyPEN_id,
              ivPercepcion.getCurrency().getId(), fecUsar, "", vars.getClient(), strOrg,
              new DalConnectionProvider());
          amountConverted = new BigDecimal(amountConverted)
              .divide(new BigDecimal("1000000.00"), 6, BigDecimal.ROUND_HALF_UP).toString();
        }

      }
    } catch (Exception e) {
      return null;
    }

    BigDecimal tipoCambio;

    if (modoCambio) {
      tipoCambio = new BigDecimal(amountConverted).divide(new BigDecimal("1000.00"), 4,
          BigDecimal.ROUND_HALF_UP);
    } else {
      tipoCambio = new BigDecimal("1.00").divide(new BigDecimal(amountConverted), 8,
          BigDecimal.ROUND_HALF_UP);
    }

    tipoCambio = tipoCambio.setScale(4, BigDecimal.ROUND_HALF_UP);

    return tipoCambio;
  }

  // private BigDecimal getTasaCambioxFecha(Invoice ivRetencion, String fecUsar,
  // VariablesSecureApp vars, String strOrg) {
  //
  // String amountConverted = "";
  // String currencyPEN_id = "308";
  //
  // ConversionRateDoc conversionRateCurrentDoc = AcctServer
  // .getConversionRateDocStatic(AcctServer.TABLEID_Invoice,
  // ivRetencion.getId(), ivRetencion.getCurrency().getId(),
  // currencyPEN_id);
  //
  // if (conversionRateCurrentDoc != null) {
  // amountConverted = AcctServer
  // .applyRate(new BigDecimal("1000.00"),
  // conversionRateCurrentDoc, true)
  // .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
  // } else {
  // amountConverted = AcctServer.getConvertedAmt("1000.00", ivRetencion
  // .getCurrency().getId(), currencyPEN_id, fecUsar, "", vars
  // .getClient(), strOrg, new DalConnectionProvider());
  // }
  //
  // BigDecimal tipoCambio = new BigDecimal(amountConverted).divide(
  // new BigDecimal("1000.00"), 4, BigDecimal.ROUND_HALF_UP);
  //
  // tipoCambio = tipoCambio.setScale(4, BigDecimal.ROUND_HALF_UP);
  //
  // return tipoCambio;
  // }

  private Boolean comparaConUltimaFechaMes(String fechaString) {

    String[] frag = fechaString.split("-");
    Integer anio = new Integer(frag[2]);
    Integer mes = new Integer(frag[1]);
    Integer dia = new Integer(frag[0]);

    Calendar calendario = Calendar.getInstance();
    calendario.set(anio, mes - 1, 1);
    Integer lastDay = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);

    calendario.set(anio, mes - 1, dia);
    Date date1 = calendario.getTime();

    calendario.set(anio, mes - 1, lastDay);
    Date date2 = calendario.getTime();

    return date1.equals(date2);

  }

  // private BigDecimal getTasaCambioxFecha(Invoice ivRetencion, String fecUsar,
  // VariablesSecureApp vars, String strOrg) {
  //
  // String amountConverted = "";
  // String currencyPEN_id = "308";
  //
  // ConversionRateDoc conversionRateCurrentDoc = AcctServer
  // .getConversionRateDocStatic(AcctServer.TABLEID_Invoice,
  // ivRetencion.getId(), ivRetencion.getCurrency().getId(),
  // currencyPEN_id);
  //
  // if (conversionRateCurrentDoc != null) {
  // amountConverted = AcctServer
  // .applyRate(new BigDecimal("1000.00"),
  // conversionRateCurrentDoc, true)
  // .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
  // } else {
  // amountConverted = AcctServer.getConvertedAmt("1000.00", ivRetencion
  // .getCurrency().getId(), currencyPEN_id, fecUsar, "", vars
  // .getClient(), strOrg, new DalConnectionProvider());
  // }
  //
  // BigDecimal tipoCambio = new BigDecimal(amountConverted).divide(
  // new BigDecimal("1000.00"), 4, BigDecimal.ROUND_HALF_UP);
  //
  // tipoCambio = tipoCambio.setScale(4, BigDecimal.ROUND_HALF_UP);
  //
  // return tipoCambio;
  // }

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

  private String getMontoEnSoles(BigDecimal rate, String monto) {
    if (monto == null)
      return null;

    if (!monto.isEmpty()) {
      BigDecimal montoME = new BigDecimal(monto);
      montoME = montoME.multiply(rate);
      return montoME.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
    return "";
  }

  @Override
  public String getServletInfo() {
    return "Servlet ReportPurchaseWithholding. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
