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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.report.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class FormTrialBalanceAcctDetailsJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strAD_Org_ID = "", strAccountId = "", strDateFrom = "", strDateTo = "";
    String strcAcctSchemaId = "", strcElementValueFrom = "", strcElementValueTo = "",
        strNotInitialBalance = "N", strtitleDetail;

    if (vars.commandIn("DEFAULT")) {
      strAD_Org_ID = vars.getStringParameter("inporgId");
      strAccountId = vars.getStringParameter("inpaccountId");
      strDateFrom = vars.getStringParameter("inpdateFrom");
      strDateTo = vars.getStringParameter("inpdateTo");

      strcAcctSchemaId = vars.getStringParameter("inpcAcctSchemaId");
      strcElementValueFrom = vars.getStringParameter("inpelementValueFrom");
      strcElementValueTo = vars.getStringParameter("inpelementValueTo");
      strNotInitialBalance = vars.getStringParameter("inpnotInitialBalance");

      strtitleDetail = vars.getStringParameter("inptitleDetail");

      String strcElementValueFromDes = "", strcElementValueToDes = "";
      if (!strcElementValueFrom.equals(""))
        strcElementValueFromDes = ReportTrialBalanceAcctDetailsData
            .selectSubaccountDescription(this, strcElementValueFrom);
      if (!strcElementValueTo.equals(""))
        strcElementValueToDes = ReportTrialBalanceAcctDetailsData.selectSubaccountDescription(this,
            strcElementValueTo);
      strcElementValueFromDes = (strcElementValueFromDes == null) ? "" : strcElementValueFromDes;
      strcElementValueToDes = (strcElementValueToDes == null) ? "" : strcElementValueToDes;

      String strcBpartnerId = "";
      String strmProductId = "";
      String strcProjectId = "";
      String strGroupBy = "";

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, "", strAD_Org_ID, "",
          strcElementValueFrom, strcElementValueTo, strcElementValueFromDes, strcElementValueToDes,
          strcBpartnerId, strmProductId, strcProjectId, strcAcctSchemaId, strNotInitialBalance,
          strGroupBy, strAccountId, strtitleDetail);

    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");

      String inpStrOrg = vars.getStringParameter("inpStrOrg");
      String inpStrClient = vars.getStringParameter("inpStrClient");
      String inpCuentaDesdeId = vars.getStringParameter("inpCuentaDesdeId");
      String inpCuentaHastaId = vars.getStringParameter("inpCuentaHastaId");

      String inpDateFrom = vars.getStringParameter("inpDateFrom");
      String inpDateTo = vars.getStringParameter("inpDateTo");
      String inpTituloCuenta = vars.getStringParameter("inpTituloCuenta");
      String inpCuentaSeleccionada = vars.getStringParameter("inpCuentaSeleccionada");

      // xmlDocument.setParameter("inpStrOrg", strOrgFamily);
      // xmlDocument.setParameter("inpStrClient", strClient);
      // xmlDocument.setParameter("inpCuentaDesdeId", strcElementValueFrom);
      // xmlDocument.setParameter("inpCuentaHastaId", strcElementValueTo);
      // xmlDocument.setParameter("inpDateFrom", strDateFrom);
      // xmlDocument.setParameter("inpDateTo", strDateTo);
      // xmlDocument.setParameter("inpTituloCuenta", strtitleDetail);

      printPageXLS(request, response, vars, inpStrOrg, inpStrClient, inpCuentaDesdeId,
          inpCuentaHastaId, inpDateFrom, inpDateTo, inpTituloCuenta, inpCuentaSeleccionada);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strPageNo,
      String strOrg, String strLevel, String strcElementValueFrom, String strcElementValueTo,
      String strcElementValueFromDes, String strcElementValueToDes, String strcBpartnerId,
      String strmProductId, String strcProjectId, String strcAcctSchemaId,
      String strNotInitialBalance, String strGroupBy, String accountValueSelected,
      String strtitleDetail) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportTrialBalanceAcctDetailsData[] dataFinal = null;

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    String discard[] = { "discard" };

    xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/report/ad_forms/FormTrialBalanceAcctDetailsFilterJR",
            discard)
        .createXmlDocument();

    System.out.println("acaa5");
    String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
    String strOrgFamily = getFamily(strTreeOrg, strOrg);

    ArrayList<ReportTrialBalanceAcctDetailsData> listData = new ArrayList<ReportTrialBalanceAcctDetailsData>();
    String strClient = Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance");

    dataFinal = getDetalleCuenta(strOrgFamily, strClient, strDateFrom, strDateTo,
        strcElementValueFrom, strcElementValueTo, accountValueSelected + "%");

    // for (int i = 0; i < dataFinal.length; i++) {
    // listData.add(dataFinal[i]);
    // }
    // dataFinal = configuraEstadosFinancieros(
    // listData.toArray(new ReportTrialBalanceAcctDetailsData[listData.size()]), strOrgFamily,
    // strClient);

    System.out.println("strtitleDetail:" + strtitleDetail);
    // FORMATTING DATA FOR HTML REPORT
    for (int i = 0; i < dataFinal.length; i++) {
      dataFinal[i].monto = (dataFinal[i].monto == "" || dataFinal[i].monto == null) ? "0.00"
          : df.format(new BigDecimal(dataFinal[i].monto));

      dataFinal[i].paramaccountdesc = (strtitleDetail != null) ? strtitleDetail : "";

      dataFinal[i].idcliente = "--";
    }

    System.out.println("acaa21");
    // Otherwise, the report is launched
    if (dataFinal == null || dataFinal.length == 0) {
      dataFinal = ReportTrialBalanceAcctDetailsData.set();
      System.out.println("acaa22");
      // No data has been found. Show warning message.
      xmlDocument.setParameter("messageType", "WARNING");
      xmlDocument.setParameter("messageTitle",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
      xmlDocument.setParameter("messageMessage",
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));

    } else {
      if (dataFinal == null || dataFinal.length == 0) {
        dataFinal = ReportTrialBalanceAcctDetailsData.set();
      }
      System.out.println("acaa24");
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "FormTrialBalanceAcctDetailsFilterJR",
        false, "", "", "imprimirDetallePDF();return false;", false, "ad_forms", strReplaceWith,
        false, true);
    toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirDetalleXLS();return false;");

    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_forms.FormTrialBalanceAcctDetailsJR");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "FormTrialBalanceAcctDetailsFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
          tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
          "FormTrialBalanceAcctDetailsFilterJR.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("FormTrialBalanceAcctDetailsJR");
      vars.removeMessage("FormTrialBalanceAcctDetailsJR");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");

    xmlDocument.setParameter("adOrg", strOrg);

    xmlDocument.setParameter("inpStrOrg", strOrgFamily);
    xmlDocument.setParameter("inpStrClient", strClient);
    xmlDocument.setParameter("inpCuentaDesdeId", strcElementValueFrom);
    xmlDocument.setParameter("inpCuentaHastaId", strcElementValueTo);
    xmlDocument.setParameter("inpDateFrom", strDateFrom);
    xmlDocument.setParameter("inpDateTo", strDateTo);
    xmlDocument.setParameter("inpTituloCuenta", strtitleDetail);
    xmlDocument.setParameter("inpCuentaSeleccionada", accountValueSelected);

    xmlDocument.setData("structure1", dataFinal);

    // Print document in the output
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String inpStrOrg, String inpStrClient, String inpCuentaDesdeId,
      String inpCuentaHastaId, String inpDateFrom, String inpDateTo, String inpTituloCuenta,
      String inpCuentaSeleccionada) throws IOException, ServletException {

    ReportTrialBalanceAcctDetailsData[] dataFinal = null;

    ArrayList<ReportTrialBalanceAcctDetailsData> listData = new ArrayList<ReportTrialBalanceAcctDetailsData>();

    // dataFinal = getDataBalance(inpStrOrg, inpStrClient, inpDateFrom, inpDateTo, inpCuentaDesdeId,
    // inpCuentaHastaId);

    dataFinal = getDetalleCuenta(inpStrOrg, inpStrClient, inpDateFrom, inpDateTo, inpCuentaDesdeId,
        inpCuentaHastaId, inpCuentaSeleccionada + "%");

    String strOutput = "";
    String strReportName;

    if (vars.commandIn("PDF")) {
      strOutput = "pdf";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormTrialBalanceAcctDetailsPDF.jrxml";
    } else {
      strOutput = "xls";
      strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_forms/FormTrialBalanceAcctDetailsEXCEL.jrxml";
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("TITULO_CUENTA", inpTituloCuenta);
    // parameters.put("CUENTA", strAccountId);
    // parameters.put("DESDE", StringToDate(strDateFrom));
    // parameters.put("HASTA", StringToDate(strDateTo));

    renderJR(vars, response, strReportName, "Reporte_Estado_Resultados_Cuentas_Detalle", strOutput,
        parameters, dataFinal, null);
  }

  private String quitaComma(String cadena) {
    return cadena.replace(",", "");
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

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree,
        (strChild == null || strChild.equals("")) ? "0" : strChild) + ",'0'";
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  public ReportTrialBalanceAcctDetailsData[] getDataWhenNotSubAccount(VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strOrg, String strOrgFamily,
      String strcAcctSchemaId, String strTreeAccount, String strNotInitialBalance)
      throws IOException, ServletException {
    ReportTrialBalanceAcctDetailsData[] data = null;
    ReportTrialBalanceAcctDetailsData[] dataAux = null;

    log4j.warn("\nstrDateFrom: " + strDateFrom + "\nstrDateTo: " + strDateTo + "\nstrOrg: " + strOrg
        + "\nstrTreeAccount: " + strTreeAccount + "\nstrcAcctSchemaId: " + strcAcctSchemaId
        + "\nstrNotInitialBalance: " + (strNotInitialBalance.equals("Y") ? "O" : "P")
        + "\nstrOrgFamily: " + strOrgFamily + "\nUser_Client: "
        + Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance")
        + "\nAccessibleOrgTree: "
        + Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance")
        + "\nstrDateTo: " + strDateFrom + "\nstrDateTo: "
        + DateTimeData.nDaysAfter(this, strDateTo, "1"));

    dataAux = ReportTrialBalanceAcctDetailsData.selectAccountsDetail(this, strDateFrom, strDateTo,
        strOrg, strTreeAccount, strcAcctSchemaId, strNotInitialBalance.equals("Y") ? "O" : "P",
        strOrgFamily, Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "ReportTrialBalance"), strDateFrom,
        DateTimeData.nDaysAfter(this, strDateTo, "1"), "", "");
    ReportTrialBalanceAcctDetailsData[] dataInitialBalance = ReportTrialBalanceAcctDetailsData
        .selectInitialBalance(this, strDateFrom, strcAcctSchemaId, "", "", "", strOrgFamily,
            Utility.getContext(this, vars, "#User_Client", "ReportTrialBalance"),
            strNotInitialBalance.equals("Y") ? "initial" : "notinitial",
            strNotInitialBalance.equals("Y") ? "initial" : "notinitial");
    log4j.debug("Calculating tree...");
    dataAux = calculateTree(dataAux, null, new Vector<Object>(), dataInitialBalance,
        strNotInitialBalance);
    // dataAux = levelFilter(dataAux, null, false, strLevel);
    dataAux = dataFilter(dataAux);

    for (int i = 0; i < dataAux.length; i++) {
      System.out.println("???:" + dataAux[i].debeInicial + " " + dataAux[i].haberInicial + " "
          + dataAux[i].saldoInicial + " " + dataAux[i].totalamtacctdr + " "
          + dataAux[i].totalamtacctcr);
    }

    log4j.debug("Tree calculated");

    /*
     * if (dataAux != null && dataAux.length > 0) { data = filterTree(dataAux, strLevel);
     * Arrays.sort(data, new ReportTrialBalanceDataComparator()); for (int i = 0; i < data.length;
     * i++) { data[i].rownum = "" + i; } } else { data = dataAux; }
     */

    data = dataAux;

    return data;

  }

  /*
   * private ReportTrialBalanceData[] filterTree(ReportTrialBalanceData[] data, String strLevel) {
   * ArrayList<Object> arrayList = new ArrayList<Object>(); for (int i = 0; data != null && i <
   * data.length; i++) { if (data[i].elementlevel.equals(strLevel)) arrayList.add(data[i]); }
   * ReportTrialBalanceData[] new_data = new ReportTrialBalanceData[arrayList.size()];
   * arrayList.toArray(new_data); return new_data; }
   */

  private ReportTrialBalanceAcctDetailsData[] calculateTree(
      ReportTrialBalanceAcctDetailsData[] data, String indice, Vector<Object> vecTotal,
      ReportTrialBalanceAcctDetailsData[] dataIB, String strNotInitialBalance) {
    if (data == null || data.length == 0)
      return data;
    if (indice == null)
      indice = "0";
    ReportTrialBalanceAcctDetailsData[] result = null;
    Vector<Object> vec = new Vector<Object>();
    // if (log4j.isDebugEnabled())
    // log4j.debug("ReportTrialBalanceData.calculateTree() - data: " +
    // data.length);
    if (vecTotal == null)
      vecTotal = new Vector<Object>();
    if (vecTotal.size() == 0) {
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
      vecTotal.addElement("0");
    }
    BigDecimal totalDR = new BigDecimal((String) vecTotal.elementAt(0));
    BigDecimal totalCR = new BigDecimal((String) vecTotal.elementAt(1));
    BigDecimal totalDRInicial = new BigDecimal((String) vecTotal.elementAt(2));
    BigDecimal totalCRInicial = new BigDecimal((String) vecTotal.elementAt(3));
    BigDecimal totalSumDRFinal = new BigDecimal((String) vecTotal.elementAt(4));
    BigDecimal totalSumCRFinal = new BigDecimal((String) vecTotal.elementAt(5));
    BigDecimal totalDRFinal = new BigDecimal((String) vecTotal.elementAt(6));
    BigDecimal totalCRFinal = new BigDecimal((String) vecTotal.elementAt(7));
    boolean encontrado = false;
    for (int i = 0; i < data.length; i++) {
      if (data[i].parentId.equals(indice)) {

        encontrado = true;
        Vector<Object> vecParcial = new Vector<Object>();
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        vecParcial.addElement("0");
        ReportTrialBalanceAcctDetailsData[] dataChilds = calculateTree(data, data[i].id, vecParcial,
            dataIB, strNotInitialBalance);
        BigDecimal parcialDR = new BigDecimal((String) vecParcial.elementAt(0));
        BigDecimal parcialCR = new BigDecimal((String) vecParcial.elementAt(1));
        BigDecimal parcialDRInicial = new BigDecimal((String) vecParcial.elementAt(2));
        BigDecimal parcialCRInicial = new BigDecimal((String) vecParcial.elementAt(3));
        BigDecimal parcialSumaDR = new BigDecimal((String) vecParcial.elementAt(4));
        BigDecimal parcialSumaCR = new BigDecimal((String) vecParcial.elementAt(5));
        BigDecimal parcialDRFinal = new BigDecimal((String) vecParcial.elementAt(6));
        BigDecimal parcialCRFinal = new BigDecimal((String) vecParcial.elementAt(7));

        data[i].amtacctdr = (new BigDecimal(data[i].amtacctdr).add(parcialDR)).toPlainString();
        data[i].amtacctcr = (new BigDecimal(data[i].amtacctcr).add(parcialCR)).toPlainString();
        data[i].haberInicial = (new BigDecimal(data[i].haberInicial).add(parcialCRInicial))
            .toPlainString();
        data[i].debeInicial = (new BigDecimal(data[i].debeInicial).add(parcialDRInicial))
            .toPlainString();

        // Set calculated Initial Balances
        for (int k = 0; k < dataIB.length; k++) {
          if (dataIB[k].accountId.equals(data[i].id)) {
            // if (strNotInitialBalance.equals("Y")) {
            data[i].debeInicial = (new BigDecimal(dataIB[k].amtacctdr).add(parcialDRInicial))
                .toPlainString();
            data[i].haberInicial = (new BigDecimal(dataIB[k].amtacctcr).add(parcialCRInicial))
                .toPlainString();
            /*
             * } else { data[i].amtacctdr = (new BigDecimal(dataIB[k].amtacctdr).add(new
             * BigDecimal(data[i].amtacctdr))) .toPlainString(); data[i].amtacctcr = (new
             * BigDecimal(dataIB[k].amtacctcr).add(new BigDecimal(data[i].amtacctcr)))
             * .toPlainString(); }
             */
            /*
             * data[i].sumadebeFinal = (new BigDecimal(dataIB[k].amtacctdr).add(parcialDR))
             * .toPlainString(); data[i].sumahaberFinal = (new
             * BigDecimal(dataIB[k].amtacctcr).add(parcialCR)) .toPlainString();
             */
          }
        }

        data[i].sumadebeFinal = (new BigDecimal(data[i].debeInicial)
            .add(new BigDecimal(data[i].amtacctdr))).toPlainString();
        data[i].sumahaberFinal = (new BigDecimal(data[i].haberInicial)
            .add(new BigDecimal(data[i].amtacctcr))).toPlainString();

        // Edit how the final balance is calculated
        BigDecimal bd = new BigDecimal(data[i].sumadebeFinal)
            .subtract(new BigDecimal(data[i].sumahaberFinal));

        if (bd.compareTo(new BigDecimal(0)) > 0) {
          data[i].debeFinal = bd.toPlainString();
        } else if (bd.compareTo(new BigDecimal(0)) <= 0) {
          data[i].haberFinal = bd.negate().toPlainString();
        }

        totalDR = totalDR.add(new BigDecimal(data[i].amtacctdr));
        totalCR = totalCR.add(new BigDecimal(data[i].amtacctcr));
        totalDRInicial = totalDRInicial.add(new BigDecimal(data[i].debeInicial));
        totalCRInicial = totalCRInicial.add(new BigDecimal(data[i].haberInicial));
        totalDRFinal = totalDRFinal.add(new BigDecimal(data[i].debeFinal));
        totalCRFinal = totalCRFinal.add(new BigDecimal(data[i].haberFinal));
        totalSumDRFinal = totalDRFinal.add(new BigDecimal(data[i].sumadebeFinal));
        totalSumCRFinal = totalCRFinal.add(new BigDecimal(data[i].sumahaberFinal));

        vec.addElement(data[i]);
        if (dataChilds != null && dataChilds.length > 0) {
          for (int j = 0; j < dataChilds.length; j++)
            vec.addElement(dataChilds[j]);
        }
      } else if (encontrado)
        break;
    }
    vecTotal.set(0, totalDR.toPlainString());
    vecTotal.set(1, totalCR.toPlainString());
    vecTotal.set(2, totalDRInicial.toPlainString());
    vecTotal.set(3, totalCRInicial.toPlainString());
    vecTotal.set(4, totalSumDRFinal.toPlainString());
    vecTotal.set(5, totalSumCRFinal.toPlainString());
    vecTotal.set(6, totalDRFinal.toPlainString());
    vecTotal.set(7, totalCRFinal.toPlainString());
    result = new ReportTrialBalanceAcctDetailsData[vec.size()];
    vec.copyInto(result);
    return result;
  }

  /**
   * Filters positions with amount credit, amount debit, initial balance and final balance distinct
   * to zero.
   * 
   * @param data
   * @return ReportTrialBalanceData array filtered.
   */
  private ReportTrialBalanceAcctDetailsData[] dataFilter(ReportTrialBalanceAcctDetailsData[] data) {
    if (data == null || data.length == 0)
      return data;
    Vector<Object> dataFiltered = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      if (new BigDecimal(data[i].amtacctdr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].amtacctcr).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].debeInicial).compareTo(BigDecimal.ZERO) != 0
          || new BigDecimal(data[i].haberInicial).compareTo(BigDecimal.ZERO) != 0) {
        dataFiltered.addElement(data[i]);
      }
    }
    ReportTrialBalanceAcctDetailsData[] result = new ReportTrialBalanceAcctDetailsData[dataFiltered
        .size()];
    dataFiltered.copyInto(result);
    return result;
  }

  private ReportTrialBalanceAcctDetailsData[] getDetalleCuenta(String strOrg, String strClient,
      String dateFrom, String dateTo, String cuentaDesdeId, String cuentaHastaId, String cuenta)
      throws ServletException {

    ReportTrialBalanceAcctDetailsData[] dataDetalle = null;

    dataDetalle = ReportTrialBalanceAcctDetailsData.select_detalle_cuenta(this, strOrg, strClient,
        dateFrom, dateTo, cuenta);

    return dataDetalle;
  }

  private ReportTrialBalanceAcctDetailsData[] getDataBalance(String strOrg, String strClient,
      String dateFrom, String dateTo, String cuentaDesdeId, String cuentaHastaId)
      throws ServletException {

    ReportTrialBalanceAcctDetailsData[] dataCuentas = null;
    ReportTrialBalanceAcctDetailsData[] dataSaldosIniciales = null;
    ReportTrialBalanceAcctDetailsData[] dataMovimientosEjercicio = null;

    HashMap<String, BigDecimal> grupoSaldoInicialDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldoInicialHaber = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoMovimientosEjercicioDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoMovimientosEjercicioHaber = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldosFinalesDebe = new HashMap<String, BigDecimal>();
    HashMap<String, BigDecimal> grupoSaldosFinalesHaber = new HashMap<String, BigDecimal>();

    BigDecimal debeFinal = BigDecimal.ZERO;
    BigDecimal haberFinal = BigDecimal.ZERO;

    String[] partesFecha = dateFrom.split("-");
    String fechaIniAnio = "01-01-" + partesFecha[2];

    dataCuentas = ReportTrialBalanceAcctDetailsData.select_arbol_de_cuentas(this, strOrg,
        strClient);

    dataSaldosIniciales = ReportTrialBalanceAcctDetailsData.select_saldos_iniciales(this, strOrg,
        strClient, fechaIniAnio, fechaIniAnio, dateFrom, cuentaDesdeId, cuentaHastaId);

    dataMovimientosEjercicio = ReportTrialBalanceAcctDetailsData.select_movimientos_ejercicio(this,
        strOrg, strClient, dateFrom, dateTo, cuentaDesdeId, cuentaHastaId);

    for (int i = 0; i < dataSaldosIniciales.length; i++) {

      BigDecimal debe = new BigDecimal(dataSaldosIniciales[i].amtacctdr);
      BigDecimal haber = new BigDecimal(dataSaldosIniciales[i].amtacctcr);

      for (int k = 0; k < dataSaldosIniciales[i].accountId.length(); k++) {
        String key = dataSaldosIniciales[i].accountId.substring(0, k + 1);

        if (grupoSaldoInicialDebe.containsKey(key)) {
          debeFinal = debe.add(grupoSaldoInicialDebe.get(key));
          haberFinal = haber.add(grupoSaldoInicialHaber.get(key));
        } else {
          debeFinal = debe;
          haberFinal = haber;
        }
        grupoSaldoInicialDebe.put(key, debeFinal);
        grupoSaldoInicialHaber.put(key, haberFinal);
      }
    }

    for (int i = 0; i < dataMovimientosEjercicio.length; i++) {

      BigDecimal debe = new BigDecimal(dataMovimientosEjercicio[i].amtacctdr);
      BigDecimal haber = new BigDecimal(dataMovimientosEjercicio[i].amtacctcr);

      for (int k = 0; k < dataMovimientosEjercicio[i].accountId.length(); k++) {
        String key = dataMovimientosEjercicio[i].accountId.substring(0, k + 1);

        if (grupoMovimientosEjercicioDebe.containsKey(key)) {
          debeFinal = debe.add(grupoMovimientosEjercicioDebe.get(key));
          haberFinal = haber.add(grupoMovimientosEjercicioHaber.get(key));
        } else {
          debeFinal = debe;
          haberFinal = haber;
        }
        grupoMovimientosEjercicioDebe.put(key, debeFinal);
        grupoMovimientosEjercicioHaber.put(key, haberFinal);
      }
    }

    BigDecimal debeInicial = BigDecimal.ZERO;
    BigDecimal haberInicial = BigDecimal.ZERO;
    BigDecimal debeMovEjercicio = BigDecimal.ZERO;
    BigDecimal haberMovEjercicio = BigDecimal.ZERO;
    BigDecimal debeSumaMayor = BigDecimal.ZERO;
    BigDecimal haberSumaMayor = BigDecimal.ZERO;

    for (int i = 0; i < dataCuentas.length; i++) {

      String key = dataCuentas[i].accountId;

      debeInicial = grupoSaldoInicialDebe.get(key) != null ? grupoSaldoInicialDebe.get(key)
          : BigDecimal.ZERO;
      haberInicial = grupoSaldoInicialHaber.get(key) != null ? grupoSaldoInicialHaber.get(key)
          : BigDecimal.ZERO;
      debeMovEjercicio = grupoMovimientosEjercicioDebe.get(key) != null
          ? grupoMovimientosEjercicioDebe.get(key) : BigDecimal.ZERO;
      haberMovEjercicio = grupoMovimientosEjercicioHaber.get(key) != null
          ? grupoMovimientosEjercicioHaber.get(key) : BigDecimal.ZERO;
      debeSumaMayor = debeInicial.add(debeMovEjercicio);
      haberSumaMayor = haberInicial.add(haberMovEjercicio);

      dataCuentas[i].debeInicial = debeInicial.toString();
      dataCuentas[i].haberInicial = haberInicial.toString();
      dataCuentas[i].amtacctdr = debeMovEjercicio.toString();
      dataCuentas[i].amtacctcr = haberMovEjercicio.toString();
      dataCuentas[i].sumadebeFinal = debeSumaMayor.toString();
      dataCuentas[i].sumahaberFinal = haberSumaMayor.toString();

      if (key.length() == 7) {
        dataCuentas[i].debeFinal = (debeSumaMayor.compareTo(haberSumaMayor) > 0
            ? debeSumaMayor.subtract(haberSumaMayor) : BigDecimal.ZERO).toString();
        dataCuentas[i].haberFinal = (haberSumaMayor.compareTo(debeSumaMayor) > 0
            ? haberSumaMayor.subtract(debeSumaMayor) : BigDecimal.ZERO).toString();

        // OBTENIENDO LOS SALDOS FINALES A PARTIR DE LAS CUENTAS BASES : 7 DIGITOS
        for (int j = 0; j < key.length(); j++) {

          String key2 = dataCuentas[i].accountId.substring(0, j + 1);
          BigDecimal debe = new BigDecimal(dataCuentas[i].debeFinal);
          BigDecimal haber = new BigDecimal(dataCuentas[i].haberFinal);

          if (grupoSaldosFinalesDebe.containsKey(key2)) {
            debeFinal = debe.add(grupoSaldosFinalesDebe.get(key2));
            haberFinal = haber.add(grupoSaldosFinalesHaber.get(key2));
          } else {
            debeFinal = debe;
            haberFinal = haber;
          }
          grupoSaldosFinalesDebe.put(key2, debeFinal);
          grupoSaldosFinalesHaber.put(key2, haberFinal);
        }
      }
    }

    dataCuentas = dataFilter(dataCuentas);

    // RECORREMOS NUEVAMENTE PARA AGREGAR LOS SALDOS FINALES A LAS CUENTAS MENORES DE 7 DIGITOS Y
    // LIMITARLA DE ACUERDO A LA CANTIDAD DE DIGITOS
    // SOLICITADA
    for (int i = 0; i < dataCuentas.length; i++) {

      String key = dataCuentas[i].accountId;
      if (key.length() < 7) {
        dataCuentas[i].debeFinal = (grupoSaldosFinalesDebe.get(key) != null
            ? grupoSaldosFinalesDebe.get(key) : BigDecimal.ZERO).toString();
        dataCuentas[i].haberFinal = (grupoSaldosFinalesHaber.get(key) != null
            ? grupoSaldosFinalesHaber.get(key) : BigDecimal.ZERO).toString();
      }
    }

    return dataCuentas;
  }

  private ReportTrialBalanceAcctDetailsData[] configuraEstadosFinancieros(
      ReportTrialBalanceAcctDetailsData[] dataFinal, String strOrg, String strClient)
      throws ServletException {

    ReportTrialBalanceAcctDetailsData[] temp = null;

    for (int i = 0; i < dataFinal.length; i++) {

      if (dataFinal[i].accountId.length() == 2) {

        temp = ReportTrialBalanceAcctDetailsData.select_analiza_cuentas(this,
            dataFinal[i].accountId + "%", strClient, strOrg);

        BigDecimal montox = new BigDecimal(dataFinal[i].debeFinal)
            .subtract(new BigDecimal(dataFinal[i].haberFinal));
        BigDecimal monto1;
        BigDecimal monto2;
        if (montox.compareTo(BigDecimal.ZERO) > 0) {
          monto1 = montox;
          monto2 = BigDecimal.ZERO;
        } else {
          monto1 = BigDecimal.ZERO;
          monto2 = montox.abs();
        }

        if (temp[0].esFuncion.compareTo("Y") == 0) {
          dataFinal[i].esFuncion = temp[0].esFuncion;
          dataFinal[i].montoFganancias = monto1.toString();
          dataFinal[i].montoFperdidas = monto2.toString();
        }
        if (temp[0].esNaturaleza.compareTo("Y") == 0) {
          dataFinal[i].esNaturaleza = temp[0].esNaturaleza;
          dataFinal[i].montoNganancias = monto1.toString();
          dataFinal[i].montoNperdidas = monto2.toString();
        }
        if (temp[0].esInventario.compareTo("Y") == 0) {
          dataFinal[i].esInventario = temp[0].esInventario;
          dataFinal[i].montoActivo = monto1.toString();
          dataFinal[i].montoPasivo = monto2.toString();
        }
      }
    }

    return dataFinal;
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
