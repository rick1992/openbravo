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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class ReportSalesPerception extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ReportSalesPerception|DateFrom",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ReportSalesPerception|DateTo",
          SREDateTimeData.today(this));
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesPerception|Org", "0");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportSalesPerception|CB_PARTNER_ID", "");

      String strRecord = vars.getGlobalVariable("inpRecord", "ReportSalesPerception|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "ReportSalesPerception|Table", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strcBpartnetId, strTable,
          strRecord);

    } else if (vars.commandIn("DIRECT")) {
      String strTable = vars.getGlobalVariable("inpTable", "ReportSalesPerception|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "ReportSalesPerception|Record");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportSalesPerception|CB_PARTNER_ID", "");

      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("ReportSalesPerception.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", strcBpartnetId, strTable, strRecord);

    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesPerception|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportSalesPerception|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesPerception|Org", "0");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportSalesPerception|CB_PARTNER_ID", "");

      vars.setSessionValue("ReportSalesPerception.initRecordNumber", "0");
      setHistoryCommand(request, "DEFAULT");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strOrg, strcBpartnetId, "", "");

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportSalesPerception|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "ReportSalesPerception|DateTo");
      String strOrg = vars.getGlobalVariable("inpOrg", "ReportSalesPerception|Org", "0");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportSalesPerception|CB_PARTNER_ID", "");

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
    ReportSalesPerceptionData[] data = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportSalesPerception", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, null, 'ReportSalesPerception.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportSalesPerception", discard)
          .createXmlDocument();
      data = ReportSalesPerceptionData.set("0");
      data[0].rownum = "0";
    } else {

      // data = notshow(data, vars);

      // toolbar
      // .prepareRelationBarTemplate(true, true,
      // "submitCommandForm('XLS', false, null, 'ReportSalesPerception.xls', 'EXCEL');return
      // false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/ReportSalesPerception")
          .createXmlDocument();
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.ReportSalesPerception");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ReportSalesPerception.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ReportSalesPerception.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ReportSalesPerception");
      vars.removeMessage("ReportSalesPerception");
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
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportSalesPerception"),
          Utility.getContext(this, vars, "#User_Client", "ReportSalesPerception"), '*');
      comboTableData.fillParameters(null, "ReportSalesPerception", "");
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
        ReportLibroConsignacionesData.selectBPartner(this, strcBpartnetId));
    vars.setSessionValue("ReportSalesPerception|Record", strRecord);
    vars.setSessionValue("ReportSalesPerception|Table", strTable);

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", ReportSalesPerceptionData.select_periodos(this)));

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  // private ReportSalesPerceptionData[] notshow(ReportSalesPerceptionData[]
  // data,
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

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strOrg,
      String strcBpartnetId, String strTable, String strRecord)
      throws IOException, ServletException {

    Date fechaIni = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateFrom, "dd-MM-yyyy");
    Date fechaFin = pe.com.unifiedgo.report.common.Utility.ParseFecha(strDateTo, "dd-MM-yyyy");

    List<PercepReg> PercepcionList = null;
    try {
      PercepcionList = UnpaidByClient.getRegistroPercepciones(strcBpartnetId, fechaIni, fechaFin);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    //
    // for (PercepReg percepciones : PercepcionList) {

    String idTercero = "";

    if (PercepcionList.size() > 0) {
      idTercero = PercepcionList.get(0).partnerUid;
    }

    BigDecimal ultimoSaldoSoles = new BigDecimal(0.0);
    BigDecimal ultimoSaldoDol = new BigDecimal(0.0);
    BigDecimal ultimaTasa = new BigDecimal(0.0);

    String documento = "";

    if (PercepcionList.size() > 0) {
      documento = PercepcionList.get(0).serieDoc + PercepcionList.get(0).numDoc;
    }

    Organization org = OBDal.getInstance().get(Organization.class, strOrg);

    ArrayList<PercepReg> listaPercepcionesContado = getRegistroPercepcionesContado(fechaIni,
        fechaFin, org, strcBpartnetId);

    for (int index = 0; index < PercepcionList.size(); index++) {

      PercepReg percepciones = PercepcionList.get(index);
      HashMap<String, String> hashMap = new HashMap<String, String>();
      SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
      String transdate = formato.format(percepciones.transDate);
      hashMap.put("debe", percepciones.debe);
      hashMap.put("haber", percepciones.haber);
      hashMap.put("numComp", percepciones.numComp);
      hashMap.put("numDoc", percepciones.numDoc);
      hashMap.put("partnerCommName", percepciones.partnerCommName);
      hashMap.put("partnerRuc", percepciones.partnerRuc);
      hashMap.put("partnerUid", percepciones.partnerUid);
      hashMap.put("saldo", percepciones.saldo);
      hashMap.put("serieComp", percepciones.serieComp);
      hashMap.put("serieDoc", percepciones.serieDoc);
      hashMap.put("tipoDoc", percepciones.tipoDoc);
      hashMap.put("tipoTrans", percepciones.tipoTrans);
      hashMap.put("totalComp", percepciones.totalComp);
      hashMap.put("totalDoc", percepciones.totalDoc);
      hashMap.put("groupNumber", String.valueOf(percepciones.groupNumber));
      hashMap.put("transDate", transdate);
      hashMap.put("currencySymbol", percepciones.currencySymbol);

      // / AHORA CONVERTIMOS LOS MONTOS A MONEDA NACIONAL PARA EL DEBE Y
      // HABER Y AGREGANDO LA DIFERENCIA DE CAMBIO AL CIERRE DE CADA MES

      // /////////agregado por ricardo

      Invoice ivPercepcion = percepciones.invo;

      DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

      Date dateFechaFactura = AcctServer.getTCenVigenciaFecha(ivPercepcion, strDateFrom, null);

      String fecUsar = "";

      if (percepciones.tipoTrans.compareToIgnoreCase("Venta") == 0) {
        fecUsar = df.format(dateFechaFactura);
      } else {
        fecUsar = df.format(percepciones.transDate);
      }

      BigDecimal tipoCambio = getTasaCambioxFecha(ivPercepcion, fecUsar, vars, strOrg, false);

      String debeSoles = this.getMontoEnSoles(tipoCambio, percepciones.debe);// cambiando estaaba el
                                                                             // debe antes

      String saldostr;
      // cuendo es venta
      if (percepciones.tipoTrans.compareToIgnoreCase("Venta") == 0) {

        if (tipoCambio.compareTo(new BigDecimal(1.0000)) == 0) {
          saldostr = percepciones.saldo;
        } else {
          saldostr = this.getMontoEnSoles(tipoCambio, percepciones.saldo);

          ultimoSaldoDol = new BigDecimal(percepciones.saldo);
          ultimoSaldoSoles = new BigDecimal(saldostr);
          ultimaTasa = tipoCambio;
        }

      } else {// cuando es cobro

        if (tipoCambio.compareTo(new BigDecimal(1.0000)) == 0) {
          saldostr = percepciones.saldo;
        } else {

          BigDecimal montoAjuste = tipoCambio.subtract(ultimaTasa).multiply(ultimoSaldoDol);

          montoAjuste = montoAjuste.setScale(2, BigDecimal.ROUND_HALF_UP);

          ultimoSaldoDol = ultimoSaldoDol.subtract(new BigDecimal(percepciones.montoPagoOriginal));

          ultimoSaldoSoles = ultimoSaldoSoles.add(montoAjuste)
              .subtract(new BigDecimal(percepciones.haber));

          ultimaTasa = tipoCambio;

          saldostr = ultimoSaldoSoles.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

          HashMap<String, String> hashMapAjuste = new HashMap<String, String>();

          hashMapAjuste.put("numComp", "");
          hashMapAjuste.put("numDoc", percepciones.numDoc);
          hashMapAjuste.put("partnerCommName", percepciones.partnerCommName);
          hashMapAjuste.put("partnerRuc", percepciones.partnerRuc);
          hashMapAjuste.put("partnerUid", percepciones.partnerUid);
          hashMapAjuste.put("serieComp", "");
          hashMapAjuste.put("serieDoc", percepciones.serieDoc);
          hashMapAjuste.put("tipoDoc", percepciones.tipoDoc);
          hashMapAjuste.put("tipoTrans", percepciones.tipoTrans);
          hashMapAjuste.put("totalComp", "");
          hashMapAjuste.put("totalDoc", percepciones.totalDoc);
          hashMapAjuste.put("groupNumber", String.valueOf(percepciones.groupNumber));
          hashMapAjuste.put("transDate", transdate);
          hashMapAjuste.put("tipoTrans", "Diferencia de cambio");
          hashMapAjuste.put("currencySymbol", percepciones.currencySymbol);

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

      if (PercepcionList.size() == index + 1 || documento.compareToIgnoreCase(
          PercepcionList.get(index + 1).serieDoc + PercepcionList.get(index + 1).numDoc) != 0) {

        if (PercepcionList.size() != index + 1)
          index2 = index + 1;
        else
          index2 = index;

        documento = PercepcionList.get(index2).serieDoc + PercepcionList.get(index2).numDoc;

        if (ultimoSaldoDol.compareTo(new BigDecimal(0)) > 0) {

          Boolean esUltimaFecha = comparaConUltimaFechaMes(strDateTo);

          tipoCambio = getTasaCambioxFecha(ivPercepcion, strDateTo, vars, strOrg, false);

          if (tipoCambio != null && esUltimaFecha) { // para ocultar el fin de cierre de mes cuando
            // la fecha hasta no tenga tipo de cambio o no sea fin de mes

            BigDecimal montoSaldo = ultimoSaldoDol.multiply(tipoCambio);

            BigDecimal difSaldosCierre = montoSaldo.subtract(ultimoSaldoSoles);

            montoSaldo = montoSaldo.setScale(2, BigDecimal.ROUND_HALF_UP);

            difSaldosCierre = difSaldosCierre.setScale(2, BigDecimal.ROUND_HALF_UP);

            HashMap<String, String> hashMapAjusteMes = new HashMap<String, String>();

            hashMapAjusteMes.put("numComp", "");
            hashMapAjusteMes.put("numDoc", percepciones.numDoc);
            hashMapAjusteMes.put("partnerCommName", percepciones.partnerCommName);
            hashMapAjusteMes.put("partnerRuc", percepciones.partnerRuc);
            hashMapAjusteMes.put("partnerUid", percepciones.partnerUid);
            hashMapAjusteMes.put("serieComp", "");
            hashMapAjusteMes.put("serieDoc", percepciones.serieDoc);
            hashMapAjusteMes.put("tipoDoc", percepciones.tipoDoc);
            hashMapAjusteMes.put("tipoTrans", percepciones.tipoTrans);
            hashMapAjusteMes.put("totalComp", "");
            hashMapAjusteMes.put("totalDoc", percepciones.totalDoc);
            hashMapAjusteMes.put("groupNumber", String.valueOf(percepciones.groupNumber));
            hashMapAjusteMes.put("transDate", strDateTo);
            hashMapAjusteMes.put("tipoTrans", "Cierre de fin de mes");

            hashMapAjusteMes.put("saldo", montoSaldo.abs().toString());
            hashMapAjusteMes.put("tasa", tipoCambio.toString());
            hashMapAjusteMes.put("currencySymbol", percepciones.currencySymbol);

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

      // / FIN CONVERSION DE MONTOS ...

      // hashMapList.add(hashMap);
      // AGREGANDO REGISTROS DE PERCEPCIONES DE FACTURAS AL CONTADO AL
      // FINAL DE LOS REGISTRO CON COMPROBANTE DE UN MISMO TERCERO
      if (PercepcionList.size() == (index + 1)
          || idTercero.compareToIgnoreCase(PercepcionList.get(index + 1).partnerUid) != 0) {

        for (int i = 0; i < listaPercepcionesContado.size(); i++) {

          PercepReg percepciones2 = listaPercepcionesContado.get(i);
          if (percepciones2 == null || idTercero.compareToIgnoreCase(percepciones2.partnerUid) != 0)
            continue;

          listaPercepcionesContado.set(i, null);

          HashMap<String, String> hashMap2 = new HashMap<String, String>();
          SimpleDateFormat formato2 = new SimpleDateFormat("dd-MM-yyyy");
          String transdate2 = formato2.format(percepciones2.transDate);
          hashMap2.put("debe", percepciones2.debe);
          hashMap2.put("haber", percepciones2.haber);
          hashMap2.put("numComp", percepciones2.numComp);
          hashMap2.put("numDoc", percepciones2.numDoc);
          hashMap2.put("partnerCommName", percepciones2.partnerCommName);
          hashMap2.put("partnerRuc", percepciones2.partnerRuc);
          hashMap2.put("partnerUid", percepciones2.partnerUid);
          hashMap2.put("saldo", percepciones2.saldo);
          hashMap2.put("serieComp", percepciones2.serieComp);
          hashMap2.put("serieDoc", percepciones2.serieDoc);
          hashMap2.put("tipoDoc", percepciones2.tipoDoc);
          hashMap2.put("tipoTrans", "Venta Contado");
          hashMap2.put("totalComp", percepciones2.totalComp);
          hashMap2.put("totalDoc", percepciones2.totalDoc);
          hashMap2.put("groupNumber", String.valueOf(percepciones2.groupNumber));
          hashMap2.put("transDate", transdate2);
          hashMap2.put("currencySymbol", percepciones2.currencySymbol);

          hashMapList.add(hashMap2);
        }
      }
    }

    if (PercepcionList.size() == 0 && !strcBpartnetId.isEmpty()) {
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
      if (bp != null) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("partnerCommName", bp.getName());
        hashMap.put("partnerRuc", bp.getTaxID());
        hashMapList.add(hashMap);
      }
    }

    // agregamos las percepciones faltantes de otros terceros

    for (int index2 = 0; index2 < listaPercepcionesContado.size(); index2++) {

      PercepReg percepciones2 = listaPercepcionesContado.get(index2);
      if (percepciones2 == null)
        continue;

      HashMap<String, String> hashMap2 = new HashMap<String, String>();
      SimpleDateFormat formato2 = new SimpleDateFormat("dd-MM-yyyy");
      String transdate2 = formato2.format(percepciones2.transDate);
      hashMap2.put("debe", percepciones2.debe);
      hashMap2.put("haber", percepciones2.haber);
      hashMap2.put("numComp", percepciones2.numComp);
      hashMap2.put("numDoc", percepciones2.numDoc);
      hashMap2.put("partnerCommName", percepciones2.partnerCommName);
      hashMap2.put("partnerRuc", percepciones2.partnerRuc);
      hashMap2.put("partnerUid", percepciones2.partnerUid);
      hashMap2.put("saldo", percepciones2.saldo);
      hashMap2.put("serieComp", percepciones2.serieComp);
      hashMap2.put("serieDoc", percepciones2.serieDoc);
      hashMap2.put("tipoDoc", percepciones2.tipoDoc);
      hashMap2.put("tipoTrans", "Venta Contado");
      hashMap2.put("totalComp", percepciones2.totalComp);
      hashMap2.put("totalDoc", percepciones2.totalDoc);
      hashMap2.put("groupNumber", String.valueOf(percepciones2.groupNumber));
      hashMap2.put("transDate", transdate2);
      hashMap2.put("currencySymbol", percepciones2.currencySymbol);

      hashMapList.add(hashMap2);

    }

    // / AHORA CONVIRTIENDO LOS MONTOS A MONEDA NACIONAL PARA EL DEBE Y
    // HABER Y AGREGANDO LA DIFERENCIA DE CAMBIO AL CIERRE DE CADA MES

    // /

    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(hashMapList);

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + ReportSalesPerceptionData.selectCompany(this, vars.getClient()) + "\n" + "RUC:"
        + ReportSalesPerceptionData.selectRucOrg(this, strOrg) + "\n";

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + ReportSalesPerceptionData.selectOrg(this, strOrg) + "\n";

    String strOutput;
    String strReportName;
    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesPerception.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/ReportSalesPerceptionExcel.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("Subtitle", strSubtitle);
    // parameters.put("TaxID",
    // ReportSalesRevenueRecordsData.selectOrgTaxID(this, strOrg));
    parameters.put("dateFrom", StringToDate(strDateFrom));
    parameters.put("dateTo", StringToDate(strDateTo));
    parameters.put("totalLines", data.length);
    parameters.put("Ruc", ReportSalesPerceptionData.selectRucOrg(this, strOrg));
    parameters.put("Razon", ReportSalesPerceptionData.selectSocialName(this, strOrg));

    if (data.length == 0) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      return;
    }
    renderJR(vars, response, strReportName, "Libro_Percepciones", strOutput, parameters, data,
        null);
  }

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

  private ArrayList<PercepReg> getRegistroPercepcionesContado(Date dttFrom, Date dttTo,
      Organization org, String strcBpartnetId) {

    DalConnectionProvider provider = new DalConnectionProvider();

    StringBuffer sb = new StringBuffer();
    // StructPdt sunatPdt = new StructPdt();
    // sunatPdt.numEntries = 0;

    OBCriteria<InvoiceTax> invFilter = OBDal.getInstance().createCriteria(InvoiceTax.class);
    invFilter.createAlias(InvoiceTax.PROPERTY_INVOICE, "inv");
    invFilter.add(Restrictions.between("inv." + Invoice.PROPERTY_INVOICEDATE, dttFrom, dttTo));
    invFilter.createAlias(InvoiceTax.PROPERTY_TAX, "tx");
    invFilter
        .add(Restrictions.eq("tx." + TaxRate.PROPERTY_SCOSPECIALTAX, "SCOSINMEDIATEPERCEPTION"));
    invFilter.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( this_.ad_org_id, " + "'"
        + org.getId() + "','" + org.getClient().getId() + "') > -1"));

    ArrayList<PercepReg> listaPercepcionesContado = new ArrayList<PercepReg>();

    List<InvoiceTax> invoiceTax = invFilter.list();

    for (int i = 0; i < invoiceTax.size(); i++) {

      InvoiceTax taxPerc = invoiceTax.get(i);
      Invoice invPerc = invoiceTax.get(i).getInvoice();

      System.out.println("id del proveedor: " + invPerc.getBusinessPartner().getId() + "  id fijo: "
          + strcBpartnetId);
      if (!invPerc.getDocumentStatus().equals("CO"))
        continue;

      if (strcBpartnetId.compareToIgnoreCase("") != 0) {
        if (invPerc.getBusinessPartner().getId().compareToIgnoreCase(strcBpartnetId) != 0)
          continue;
      }

      PercepReg percepcionesContado = new PercepReg();

      String docnumber = invPerc.getScrPhysicalDocumentno();
      if (docnumber == null || docnumber.equals(""))
        docnumber = "000-00000";
      StringTokenizer st = new StringTokenizer(docnumber, "-");

      String serieInvoice = st.nextToken().replaceAll("\\D+", "");
      if (serieInvoice.length() < 1 || serieInvoice.length() > 4)
        try {
          throw new Exception();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      String nroInvoice = st.nextToken().replaceAll("\\D+", "");
      if (nroInvoice.length() < 1 || nroInvoice.length() > 8)
        try {
          throw new Exception();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      percepcionesContado.serieDoc = serieInvoice;
      percepcionesContado.numDoc = nroInvoice;
      percepcionesContado.transDate = invPerc.getInvoiceDate();

      String doctype = invPerc.getDocumentType().getScoSpecialdoctype();
      String comprobante = "00";
      if (doctype != null) {
        if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE")
            || doctype.equals("SCOAPBOEINVOICE") || doctype.equals("SCOAPLOANINVOICE"))
          comprobante = invPerc.getScoPodoctypeComboitem().getCode(); // factura
        if (doctype.equals("SCOAPCREDITMEMO"))
          comprobante = "07"; // credito
      }
      percepcionesContado.tipoDoc = comprobante;

      percepcionesContado.totalDoc = invPerc.getScoGrandtotalNoperc().setScale(2).toString();

      // Calculando el debe y haber cuando la factura es de moneda
      // extranjera

      String montoDbHb = "0.0";

      try {
        montoDbHb = AcctServer.getConvertedAmt(percepcionesContado.totalDoc,
            invPerc.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
            OBDateUtils.formatDate(invPerc.getAccountingDate()), "", invPerc.getClient().getId(),
            invPerc.getOrganization().getId(), provider);
      } catch (ServletException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      percepcionesContado.debe = montoDbHb;
      percepcionesContado.haber = montoDbHb;

      String montoPercepcion = "0.0";

      try {
        montoPercepcion = AcctServer.getConvertedAmt(taxPerc.getTaxAmount().toString(),
            invPerc.getCurrency().getId(),
            Utility.stringBaseCurrencyId(provider, invPerc.getClient().getId()),
            OBDateUtils.formatDate(invPerc.getAccountingDate()), "", invPerc.getClient().getId(),
            invPerc.getOrganization().getId(), provider);
      } catch (ServletException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      percepcionesContado.totalComp = montoPercepcion;

      percepcionesContado.partnerCommName = invPerc.getBusinessPartner().getName();
      percepcionesContado.partnerRuc = invPerc.getBusinessPartner().getTaxID();
      percepcionesContado.partnerUid = invPerc.getBusinessPartner().getId();

      listaPercepcionesContado.add(percepcionesContado);

    }

    return listaPercepcionesContado;
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

  public static String LPad(String s, int n, char car) {
    return String.format("%1$" + n + "s", s).replace(' ', car);
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
    return "Servlet ReportSalesPerception. This Servlet was made by Pablo Sarobe modified by everybody";
  } // end of getServletInfo() method
}
